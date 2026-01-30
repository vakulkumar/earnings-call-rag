package com.earningscall.rag.service;

import com.earningscall.rag.util.TextChunker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TextChunkerTest {

    @Autowired
    private TextChunker textChunker;

    @Test
    void testChunkingCreatesMultipleChunks() {
        // Prepare test data - a long text that will be split
        String longText = "This is a test sentence. ".repeat(100);

        com.earningscall.rag.util.PdfTextExtractor.PageContent page = new com.earningscall.rag.util.PdfTextExtractor.PageContent(
                1, longText);

        List<com.earningscall.rag.util.PdfTextExtractor.PageContent> pages = List.of(page);

        // Execute chunking
        List<TextChunker.TextChunk> chunks = textChunker.chunkText(pages);

        // Verify
        assertNotNull(chunks);
        assertTrue(chunks.size() > 1, "Long text should be split into multiple chunks");

        // Verify chunk indices are sequential
        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).getChunkIndex());
        }
    }

    @Test
    void testChunkingPreservesContent() {
        String testText = "This is a test document. It has multiple sentences. " +
                "We want to ensure that chunking works correctly. " +
                "Each chunk should maintain context.";

        com.earningscall.rag.util.PdfTextExtractor.PageContent page = new com.earningscall.rag.util.PdfTextExtractor.PageContent(
                1, testText);

        List<TextChunker.TextChunk> chunks = textChunker.chunkText(List.of(page));

        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());

        // Verify page numbers are preserved
        chunks.forEach(chunk -> assertEquals(1, chunk.getPageNumber()));
    }
}
