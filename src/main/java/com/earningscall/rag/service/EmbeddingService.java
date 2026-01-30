package com.earningscall.rag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    /**
     * Generate embedding for a single text
     */
    public List<Double> generateEmbedding(String text) {
        log.debug("Generating embedding for text of length: {}", text.length());

        EmbeddingResponse response = embeddingModel.call(
                new EmbeddingRequest(List.of(text), null));

        return response.getResults().get(0).getOutput();
    }

    /**
     * Generate embeddings for multiple texts in batch
     */
    public List<List<Double>> generateEmbeddingsBatch(List<String> texts) {
        log.info("Generating embeddings for {} texts", texts.size());

        return texts.stream()
                .map(this::generateEmbedding)
                .collect(Collectors.toList());
    }
}
