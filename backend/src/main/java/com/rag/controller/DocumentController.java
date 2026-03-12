package com.rag.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rag.common.ApiResponse;
import com.rag.entity.DocumentChunkEntity;
import com.rag.entity.DocumentEntity;
import com.rag.entity.IndexingJobEntity;
import com.rag.mapper.DocumentChunkMapper;
import com.rag.service.DocumentService;
import com.rag.service.IndexingJobService;
import com.rag.service.RagPipelineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;
    private final RagPipelineService ragPipelineService;
    private final IndexingJobService indexingJobService;
    private final DocumentChunkMapper documentChunkMapper;

    public DocumentController(DocumentService documentService, RagPipelineService ragPipelineService,
                              IndexingJobService indexingJobService, DocumentChunkMapper documentChunkMapper) {
        this.documentService = documentService;
        this.ragPipelineService = ragPipelineService;
        this.indexingJobService = indexingJobService;
        this.documentChunkMapper = documentChunkMapper;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> uploadDocument(@RequestParam("file") MultipartFile file) throws Exception {
        DocumentEntity doc = documentService.uploadDocument(file);
        log.info("Document uploaded: {}", doc.getId());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", doc.getId());
        data.put("fileName", doc.getFileName());
        data.put("fileSizeBytes", doc.getFileSizeBytes());
        data.put("documentStatus", doc.getDocumentStatus());
        data.put("createdAt", doc.getCreatedAt());

        return ApiResponse.ok("Upload succeeded", data);
    }

    @GetMapping
    public ApiResponse<Page<DocumentEntity>> listDocuments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(documentService.listDocuments(page, size, status));
    }

    @GetMapping("/{documentId}")
    public ApiResponse<DocumentEntity> getDocument(@PathVariable String documentId) {
        DocumentEntity doc = documentService.getDocumentForCurrentUser(documentId);
        return ApiResponse.ok(doc);
    }

    @PostMapping("/{documentId}/index")
    public ApiResponse<Map<String, Object>> startIndexing(@PathVariable String documentId) {
        documentService.getDocumentForCurrentUser(documentId);
        IndexingJobEntity job = ragPipelineService.indexDocument(documentId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("jobId", job.getId());
        data.put("documentId", documentId);
        data.put("jobStatus", job.getJobStatus());

        return ApiResponse.ok("Indexing started", data);
    }

    @GetMapping("/{documentId}/jobs/latest")
    public ApiResponse<IndexingJobEntity> getLatestJob(@PathVariable String documentId) {
        documentService.getDocumentForCurrentUser(documentId);
        IndexingJobEntity job = indexingJobService.getLatestJob(documentId);
        return ApiResponse.ok(job);
    }

    @GetMapping("/{documentId}/chunks")
    public ApiResponse<Page<DocumentChunkEntity>> listChunks(
            @PathVariable String documentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        documentService.getDocumentForCurrentUser(documentId);

        Page<DocumentChunkEntity> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<DocumentChunkEntity> query = new LambdaQueryWrapper<DocumentChunkEntity>()
                .eq(DocumentChunkEntity::getDocumentId, documentId)
                .orderByAsc(DocumentChunkEntity::getChunkIndex);
        documentChunkMapper.selectPage(pageObj, query);

        return ApiResponse.ok(pageObj);
    }
}
