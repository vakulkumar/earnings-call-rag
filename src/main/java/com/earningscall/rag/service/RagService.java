package com.earningscall.rag.service;

import com.earningscall.rag.model.dto.QuestionRequest;
import com.earningscall.rag.model.dto.QuestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStorageService vectorStorageService;
    private final ChatClient.Builder chatClientBuilder;

    private static final String RAG_PROMPT_TEMPLATE = """
            You are an AI assistant specialized in analyzing earnings call transcripts.

            Based on the following context from earnings call transcripts, answer the user's question.
            If the answer cannot be found in the context, say "I cannot find this information in the provided transcripts."
            Always cite the source document and page number when providing information.

            Context:
            {context}

            Question: {question}

            Answer:
            """;

    /**
     * Answer a question using RAG pipeline
     */
    public QuestionResponse answerQuestion(QuestionRequest request) {
        long startTime = System.currentTimeMillis();

        log.info("Processing question: {}", request.getQuestion());

        try {
            // Step 1: Retrieval - Find relevant chunks using semantic search
            List<VectorStorageService.RetrievedChunk> retrievedChunks = vectorStorageService.searchSimilarChunks(
                    request.getQuestion(),
                    request.getDocumentId(),
                    request.getCompanyName());

            if (retrievedChunks.isEmpty()) {
                log.warn("No relevant chunks found for question");
                return QuestionResponse.builder()
                        .answer("I couldn't find any relevant information in the uploaded documents to answer this question.")
                        .confidenceScore(0.0)
                        .sources(List.of())
                        .processingTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            // Step 2: Augmentation - Build context from retrieved chunks
            String context = buildContext(retrievedChunks);

            log.debug("Built context from {} chunks", retrievedChunks.size());

            // Step 3: Generation - Use LLM to generate answer
            String answer = generateAnswer(request.getQuestion(), context);

            // Build source citations
            List<QuestionResponse.SourceCitation> sources = retrievedChunks.stream()
                    .map(chunk -> QuestionResponse.SourceCitation.builder()
                            .documentName(chunk.getDocumentName())
                            .pageNumber(chunk.getPageNumber())
                            .relevantText(truncateText(chunk.getText(), 200))
                            .similarityScore(chunk.getSimilarityScore())
                            .build())
                    .collect(Collectors.toList());

            // Calculate confidence score based on similarity scores
            double avgSimilarity = retrievedChunks.stream()
                    .mapToDouble(VectorStorageService.RetrievedChunk::getSimilarityScore)
                    .average()
                    .orElse(0.0);

            long processingTime = System.currentTimeMillis() - startTime;

            log.info("Question answered in {}ms with confidence {}", processingTime, avgSimilarity);

            return QuestionResponse.builder()
                    .answer(answer)
                    .confidenceScore(avgSimilarity)
                    .sources(sources)
                    .processingTimeMs(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("Error answering question", e);
            throw new RuntimeException("Failed to process question: " + e.getMessage(), e);
        }
    }

    /**
     * Build context from retrieved chunks
     */
    private String buildContext(List<VectorStorageService.RetrievedChunk> chunks) {
        return chunks.stream()
                .map(chunk -> String.format(
                        "[Document: %s, Page: %d]\n%s\n",
                        chunk.getDocumentName(),
                        chunk.getPageNumber(),
                        chunk.getText()))
                .collect(Collectors.joining("\n---\n"));
    }

    /**
     * Generate answer using LLM
     */
    private String generateAnswer(String question, String context) {
        ChatClient chatClient = chatClientBuilder.build();

        Map<String, Object> promptVariables = new HashMap<>();
        promptVariables.put("context", context);
        promptVariables.put("question", question);

        PromptTemplate promptTemplate = new PromptTemplate(RAG_PROMPT_TEMPLATE);
        Prompt prompt = promptTemplate.create(promptVariables);

        String response = chatClient.prompt(prompt)
                .call()
                .content();

        return response != null ? response.trim() : "Unable to generate answer";
    }

    /**
     * Truncate text for display
     */
    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
