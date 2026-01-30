package com.earningscall.rag.service;

import com.earningscall.rag.model.DocumentChunk;
import com.earningscall.rag.repository.DocumentChunkRepository;
import com.earningscall.rag.util.TextChunker;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStorageService {

    private final VectorStore vectorStore;
    private final DocumentChunkRepository chunkRepository;

    @Value("${rag.retrieval.top-k:5}")
    private int topK;

    @Value("${rag.retrieval.similarity-threshold:0.7}")
    private double similarityThreshold;

    /**
     * Store document chunks with their embeddings
     */
    @Transactional
    public void storeChunks(UUID documentId, String documentName, List<TextChunker.TextChunk> chunks) {
        log.info("Storing {} chunks for document {}", chunks.size(), documentId);

        // Convert chunks to Spring AI Documents and store in vector database
        List<Document> documents = chunks.stream()
                .map(chunk -> {
                    Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
                    metadata.put("documentId", documentId.toString());
                    metadata.put("documentName", documentName);
                    metadata.put("chunkIndex", chunk.getChunkIndex());
                    metadata.put("pageNumber", chunk.getPageNumber());

                    return new Document(
                            UUID.randomUUID().toString(),
                            chunk.getText(),
                            metadata);
                })
                .collect(Collectors.toList());

        // Store in pgvector using Spring AI's VectorStore
        vectorStore.add(documents);

        // Also save chunks to our JPA repository for reference
        List<DocumentChunk> documentChunks = chunks.stream()
                .map(chunk -> DocumentChunk.builder()
                        .documentId(documentId)
                        .chunkText(chunk.getText())
                        .chunkIndex(chunk.getChunkIndex())
                        .pageNumber(chunk.getPageNumber())
                        .metadata(chunk.getMetadata())
                        .build())
                .collect(Collectors.toList());

        chunkRepository.saveAll(documentChunks);

        log.info("Successfully stored {} chunks", chunks.size());
    }

    /**
     * Perform semantic similarity search
     */
    public List<RetrievedChunk> searchSimilarChunks(String query, UUID documentId, String companyName) {
        log.info("Searching for chunks similar to query: {}", query);

        // Build search request
        SearchRequest.Builder requestBuilder = SearchRequest.defaults()
                .withTopK(topK)
                .withSimilarityThreshold(similarityThreshold);

        // Add filter expressions if provided
        if (documentId != null) {
            requestBuilder.withFilterExpression(
                    String.format("documentId == '%s'", documentId.toString()));
        }

        List<Document> results = vectorStore.similaritySearch(requestBuilder.build().withQuery(query));

        log.info("Found {} similar chunks", results.size());

        // Convert to RetrievedChunk with metadata
        return results.stream()
                .map(doc -> {
                    Map<String, Object> metadata = doc.getMetadata();
                    return RetrievedChunk.builder()
                            .text(doc.getContent())
                            .documentId(UUID.fromString((String) metadata.get("documentId")))
                            .documentName((String) metadata.get("documentName"))
                            .pageNumber((Integer) metadata.get("pageNumber"))
                            .chunkIndex((Integer) metadata.get("chunkIndex"))
                            .similarityScore((Double) metadata.getOrDefault("distance", 0.0))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Data
    @lombok.Builder
    public static class RetrievedChunk {
        private String text;
        private UUID documentId;
        private String documentName;
        private Integer pageNumber;
        private Integer chunkIndex;
        private Double similarityScore;
    }
}
