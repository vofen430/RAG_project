package com.rag.controller;

import com.rag.model.DocumentInfo;
import com.rag.service.DocumentService;
import com.rag.service.RagPipelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final RagPipelineService ragPipelineService;

    public DocumentController(DocumentService documentService, RagPipelineService ragPipelineService) {
        this.documentService = documentService;
        this.ragPipelineService = ragPipelineService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            DocumentInfo info = documentService.uploadDocument(file);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<DocumentInfo>> listDocuments() {
        return ResponseEntity.ok(documentService.listDocuments());
    }

    @PostMapping("/{id}/index")
    public ResponseEntity<?> indexDocument(@PathVariable String id) {
        DocumentInfo info = documentService.getDocumentInfo(id);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        ragPipelineService.indexDocument(id);
        return ResponseEntity.ok(Map.of("message", "Indexing started", "documentId", id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> getDocumentStatus(@PathVariable String id) {
        DocumentInfo info = documentService.getDocumentInfo(id);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }
}
