package com.earningscall.rag.controller;

import com.earningscall.rag.model.Document;
import com.earningscall.rag.model.dto.UploadResponse;
import com.earningscall.rag.service.PdfProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final PdfProcessingService pdfProcessingService;

    /**
     * Upload a PDF document for processing
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "companyName", required = false) String companyName) {

        try {
            log.info("Received upload request for file: {}", file.getOriginalFilename());

            Document document = pdfProcessingService.uploadDocument(file, companyName);

            UploadResponse response = UploadResponse.builder()
                    .documentId(document.getId())
                    .filename(document.getFilename())
                    .companyName(document.getCompanyName())
                    .status(document.getProcessingStatus().name())
                    .uploadTimestamp(document.getUploadTimestamp())
                    .message("Document uploaded successfully. Processing has started.")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid upload request", e);
            return ResponseEntity.badRequest()
                    .body(UploadResponse.builder()
                            .message("Invalid request: " + e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error uploading document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(UploadResponse.builder()
                            .message("Failed to upload document: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get document status by ID
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getDocumentStatus(@PathVariable UUID id) {
        try {
            Document document = pdfProcessingService.getDocument(id);

            UploadResponse response = UploadResponse.builder()
                    .documentId(document.getId())
                    .filename(document.getFilename())
                    .companyName(document.getCompanyName())
                    .status(document.getProcessingStatus().name())
                    .uploadTimestamp(document.getUploadTimestamp())
                    .message(getStatusMessage(document))
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching document status", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Document not found"));
        }
    }

    /**
     * Get all documents
     */
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments(
            @RequestParam(value = "company", required = false) String companyName) {

        try {
            List<Document> documents;

            if (companyName != null && !companyName.isBlank()) {
                documents = pdfProcessingService.getDocumentsByCompany(companyName);
            } else {
                documents = pdfProcessingService.getAllDocuments();
            }

            return ResponseEntity.ok(documents);

        } catch (Exception e) {
            log.error("Error fetching documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get document by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable UUID id) {
        try {
            Document document = pdfProcessingService.getDocument(id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            log.error("Error fetching document", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Document not found"));
        }
    }

    private String getStatusMessage(Document document) {
        return switch (document.getProcessingStatus()) {
            case PENDING -> "Document is waiting to be processed";
            case PROCESSING -> "Document is currently being processed";
            case COMPLETED -> String.format("Document processed successfully. %d chunks created.",
                    document.getTotalChunks());
            case FAILED -> "Document processing failed";
        };
    }

    private static class Map {
        static java.util.Map<String, String> of(String key, String value) {
            return java.util.Collections.singletonMap(key, value);
        }
    }
}
