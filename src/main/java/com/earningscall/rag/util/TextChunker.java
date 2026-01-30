package com.earningscall.rag.util;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TextChunker {

    @Value("${rag.chunk.size:800}")
    private int chunkSize;

    @Value("${rag.chunk.overlap:150}")
    private int overlapSize;

    public List<TextChunk> chunkText(List<PdfTextExtractor.PageContent> pages) {
        List<TextChunk> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        int currentLength = 0;
        int chunkIndex = 0;
        Integer currentPageNumber = null;

        for (PdfTextExtractor.PageContent page : pages) {
            String pageText = page.text();

            // Split into sentences to avoid breaking mid-sentence
            List<String> sentences = splitIntoSentences(pageText);

            for (String sentence : sentences) {
                int sentenceLength = sentence.length();

                // If adding this sentence would exceed chunk size, save current chunk
                if (currentLength + sentenceLength > chunkSize && currentLength > 0) {
                    String chunkText = currentChunk.toString().trim();

                    if (!chunkText.isEmpty()) {
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("pageNumber", currentPageNumber);
                        metadata.put("chunkIndex", chunkIndex);

                        chunks.add(TextChunk.builder()
                                .text(chunkText)
                                .chunkIndex(chunkIndex)
                                .pageNumber(currentPageNumber)
                                .metadata(metadata)
                                .build());

                        chunkIndex++;
                    }

                    // Start new chunk with overlap
                    String overlapText = getOverlapText(currentChunk.toString());
                    currentChunk = new StringBuilder(overlapText);
                    currentLength = overlapText.length();
                }

                currentChunk.append(sentence).append(" ");
                currentLength += sentenceLength + 1;

                if (currentPageNumber == null) {
                    currentPageNumber = page.pageNumber();
                }
            }
        }

        // Add final chunk if there's remaining text
        if (currentLength > 0) {
            String chunkText = currentChunk.toString().trim();

            if (!chunkText.isEmpty()) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("pageNumber", currentPageNumber);
                metadata.put("chunkIndex", chunkIndex);

                chunks.add(TextChunk.builder()
                        .text(chunkText)
                        .chunkIndex(chunkIndex)
                        .pageNumber(currentPageNumber)
                        .metadata(metadata)
                        .build());
            }
        }

        log.info("Created {} chunks from {} pages", chunks.size(), pages.size());
        return chunks;
    }

    private List<String> splitIntoSentences(String text) {
        // Simple sentence splitting - can be enhanced with NLP libraries
        String[] sentences = text.split("(?<=[.!?])\\s+");
        return List.of(sentences);
    }

    private String getOverlapText(String text) {
        if (text.length() <= overlapSize) {
            return text;
        }

        // Get last 'overlapSize' characters, but try to start at a word boundary
        String overlap = text.substring(Math.max(0, text.length() - overlapSize));
        int firstSpace = overlap.indexOf(' ');

        if (firstSpace > 0 && firstSpace < overlap.length() / 2) {
            overlap = overlap.substring(firstSpace + 1);
        }

        return overlap;
    }

    @Data
    @Builder
    public static class TextChunk {
        private String text;
        private Integer chunkIndex;
        private Integer pageNumber;
        private Map<String, Object> metadata;
    }
}
