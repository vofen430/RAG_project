package com.rag.service;

import com.rag.model.DocumentInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentService {

    private final Map<String, String> documentTexts = new ConcurrentHashMap<>();
    private final Map<String, DocumentInfo> documents = new ConcurrentHashMap<>();

    /**
     * Upload and store a document. Auto-detects encoding (UTF-8 or GBK).
     */
    public DocumentInfo uploadDocument(MultipartFile file) throws IOException {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String fileName = file.getOriginalFilename();
        byte[] bytes = file.getBytes();

        // Auto-detect encoding
        String text = detectAndDecode(bytes);

        documentTexts.put(id, text);
        DocumentInfo info = new DocumentInfo(id, fileName, file.getSize());
        documents.put(id, info);
        return info;
    }

    public String getDocumentText(String documentId) {
        return documentTexts.get(documentId);
    }

    public DocumentInfo getDocumentInfo(String documentId) {
        return documents.get(documentId);
    }

    public List<DocumentInfo> listDocuments() {
        return new ArrayList<>(documents.values());
    }

    public void updateDocumentInfo(String documentId, DocumentInfo info) {
        documents.put(documentId, info);
    }

    /**
     * Try UTF-8 first; if it contains replacement chars, fall back to GBK.
     */
    private String detectAndDecode(byte[] bytes) {
        // Try UTF-8
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        if (!utf8.contains("\uFFFD")) {
            // Remove BOM if present
            if (utf8.startsWith("\uFEFF")) {
                utf8 = utf8.substring(1);
            }
            return utf8;
        }

        // Try GBK
        try {
            Charset gbk = Charset.forName("GBK");
            return new String(bytes, gbk);
        } catch (Exception e) {
            // Fallback to UTF-8 anyway
            return utf8;
        }
    }
}
