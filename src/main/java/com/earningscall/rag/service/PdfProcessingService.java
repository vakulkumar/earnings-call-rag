package com.earningscall.rag.service;

import com.earningscall.rag.model.Document;
import com.earningscall.rag.repository.DocumentRepository;
import com.earningscall.rag.util.PdfTextExtractor;
import com.earningscall.rag.util.TextChunker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfProcessingService {

    private final DocumentRepository documentRepository;
    private final PdfTextExtractor pdfTextExtractor;
    private final TextChunker textChunker;
    private final VectorStorageService vectorStorageService;

    /**
     * Upload PDF and initiate async processing
     */
    @Transactional
    public Document uploadDocument(MultipartFile file, String companyName) throws IOException {
        log.info("Uploading document: {} for company: {}", file.getOriginalFilename(), companyName);

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("File must be PDF");
        }

        // Create document record
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("originalFilename", file.getOriginalFilename());
        metadata.put("fileSize", file.getSize());
        metadata.put("contentType", file.getContentType());

        Document document = Document.builder()
                .filename(file.getOriginalFilename())
                .companyName(companyName)
                .uploadTimestamp(LocalDateTime.now())
                .processingStatus(Document.ProcessingStatus.PENDING)
                .totalChunks(0)
                .metadata(metadata)
                .build();

        document = documentRepository.save(document);

        log.info("Document created with ID: {}", document.getId());

        // Process asynchronously
        processDocumentAsync(document.getId(), file);

        return document;
    }

    /**
     * Process PDF asynchronously using CompletableFuture
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> processDocumentAsync(UUID documentId, MultipartFile file) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting async processing for document: {}", documentId);

                // Update status to PROCESSING
                updateDocumentStatus(documentId, Document.ProcessingStatus.PROCESSING);

                // Extract text from PDF
                List<PdfTextExtractor.PageContent> pages = pdfTextExtractor.extractText(
                        file.getInputStream());

                if (pages.isEmpty()) {
                    throw new RuntimeException("No text could be extracted from PDF");
                }

                // Chunk the text
                List<TextChunker.TextChunk> chunks = textChunker.chunkText(pages);

                if (chunks.isEmpty()) {
                    throw new RuntimeException("No chunks created from PDF text");
                }

                // Get document info
                Document document = documentRepository.findById(documentId)
                        .orElseThrow(() -> new RuntimeException("Document not found"));

                // Store chunks with embeddings in vector database
                vectorStorageService.storeChunks(
                        documentId,
                        document.getFilename(),
                        chunks);

                // Update document status and chunk count
                updateDocumentCompletion(documentId, chunks.size());

                log.info("Successfully completed processing for document: {}", documentId);

            } catch (Exception e) {
                log.error("Error processing document: {}", documentId, e);
                updateDocumentStatus(documentId, Document.ProcessingStatus.FAILED);
                throw new RuntimeException("Failed to process document", e);
            }
        });
    }

    @Transactional
    protected void updateDocumentStatus(UUID documentId, Document.ProcessingStatus status) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            doc.setProcessingStatus(status);
            documentRepository.save(doc);
            log.info("Updated document {} status to {}", documentId, status);
        });
    }

    @Transactional
    protected void updateDocumentCompletion(UUID documentId, int chunkCount) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            doc.setProcessingStatus(Document.ProcessingStatus.COMPLETED);
            doc.setTotalChunks(chunkCount);
            documentRepository.save(doc);
            log.info("Document {} completed with {} chunks", documentId, chunkCount);
        });
    }

    /**
     * Get document by ID
     */
    public Document getDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
    }

    /**
     * Get all documents
     */
    public List<Document> getAllDocuments() {
        return documentRepository.findAllByOrderByUploadTimestampDesc();
    }

    /**
     * Get documents by company
     */
    public List<Document> getDocumentsByCompany(String companyName) {
        return documentRepository.findByCompanyNameContainingIgnoreCase(companyName);
    }
}
