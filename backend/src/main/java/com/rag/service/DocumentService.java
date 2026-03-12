package com.rag.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rag.common.BizException;
import com.rag.entity.DocumentEntity;
import com.rag.mapper.DocumentMapper;
import com.rag.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentMapper documentMapper;
    private final SecurityUtil securityUtil;
    private final String storageRoot;
    private final String documentsDir;

    public DocumentService(DocumentMapper documentMapper, SecurityUtil securityUtil,
                           @Value("${app.storage.root}") String storageRoot,
                           @Value("${app.storage.documents-dir}") String documentsDir) {
        this.documentMapper = documentMapper;
        this.securityUtil = securityUtil;
        this.storageRoot = storageRoot;
        this.documentsDir = documentsDir;
    }

    /**
     * Upload, validate, and persist a document.
     */
    public DocumentEntity uploadDocument(MultipartFile file) throws IOException {
        String userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException("UNAUTHORIZED", "Not authenticated");
        }

        // Validate file
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BizException("VALIDATION_ERROR", "File name is required");
        }

        if (file.isEmpty()) {
            throw new BizException("VALIDATION_ERROR", "File is empty");
        }

        String fileExt = extractExtension(originalFilename);
        if (!isSupportedExtension(fileExt)) {
            throw new BizException("VALIDATION_ERROR", "Unsupported file type: " + fileExt);
        }

        byte[] bytes = file.getBytes();

        // Detect encoding
        String encoding = detectEncoding(bytes);

        // Compute file hash
        String fileHash = computeHash(bytes);

        // Store file on disk
        String docId = UUID.randomUUID().toString();
        Path docDir = Paths.get(storageRoot, documentsDir, userId);
        Files.createDirectories(docDir);
        Path filePath = docDir.resolve(docId + "." + fileExt);
        Files.write(filePath, bytes);

        // Persist metadata
        DocumentEntity entity = new DocumentEntity();
        entity.setId(docId);
        entity.setUserId(userId);
        entity.setFileName(originalFilename);
        entity.setFileExt(fileExt);
        entity.setFileSizeBytes(file.getSize());
        entity.setFileHash(fileHash);
        entity.setStoragePath(filePath.toString());
        entity.setSourceEncoding(encoding);
        entity.setDocumentStatus("UPLOADED");

        documentMapper.insert(entity);
        log.info("Document uploaded: id={}, fileName={}, userId={}", docId, originalFilename, userId);

        return entity;
    }

    public DocumentEntity getDocument(String documentId) {
        return documentMapper.selectById(documentId);
    }

    public DocumentEntity getDocumentForCurrentUser(String documentId) {
        DocumentEntity doc = documentMapper.selectById(documentId);
        if (doc == null) {
            throw new BizException("NOT_FOUND", "Document not found");
        }
        String userId = securityUtil.getCurrentUserId();
        if (!doc.getUserId().equals(userId)) {
            throw new BizException("FORBIDDEN", "Document does not belong to current user");
        }
        return doc;
    }

    public Page<DocumentEntity> listDocuments(int page, int size, String status) {
        String userId = securityUtil.getCurrentUserId();
        LambdaQueryWrapper<DocumentEntity> query = new LambdaQueryWrapper<DocumentEntity>()
                .eq(DocumentEntity::getUserId, userId)
                .orderByDesc(DocumentEntity::getCreatedAt);

        if (status != null && !status.isBlank()) {
            query.eq(DocumentEntity::getDocumentStatus, status);
        }

        return documentMapper.selectPage(new Page<>(page, size), query);
    }

    public void updateDocument(DocumentEntity entity) {
        documentMapper.updateById(entity);
    }

    /**
     * Read document text from stored file on disk, detecting encoding.
     */
    public String readDocumentText(DocumentEntity doc) throws IOException {
        Path filePath = Paths.get(doc.getStoragePath());
        byte[] bytes = Files.readAllBytes(filePath);
        return detectAndDecode(bytes);
    }

    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) return "";
        return filename.substring(lastDot + 1).toLowerCase();
    }

    private boolean isSupportedExtension(String ext) {
        return "txt".equals(ext) || "md".equals(ext) || "text".equals(ext);
    }

    private String detectEncoding(byte[] bytes) {
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        if (!utf8.contains("\uFFFD")) {
            return "UTF-8";
        }
        return "GBK";
    }

    private String computeHash(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "";
        }
    }

    private String detectAndDecode(byte[] bytes) {
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        if (!utf8.contains("\uFFFD")) {
            if (utf8.startsWith("\uFEFF")) {
                utf8 = utf8.substring(1);
            }
            return utf8;
        }
        try {
            Charset gbk = Charset.forName("GBK");
            return new String(bytes, gbk);
        } catch (Exception e) {
            return utf8;
        }
    }
}
