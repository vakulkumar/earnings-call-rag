package com.earningscall.rag.controller;

import com.earningscall.rag.model.dto.QuestionRequest;
import com.earningscall.rag.model.dto.QuestionResponse;
import com.earningscall.rag.service.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final RagService ragService;

    /**
     * Ask a question about the uploaded documents
     */
    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@Valid @RequestBody QuestionRequest request) {
        try {
            log.info("Received question: {}", request.getQuestion());

            QuestionResponse response = ragService.answerQuestion(request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of("Failed to process question: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("RAG service is running");
    }

    private record ErrorResponse(String error) {
        static ErrorResponse of(String message) {
            return new ErrorResponse(message);
        }
    }
}
