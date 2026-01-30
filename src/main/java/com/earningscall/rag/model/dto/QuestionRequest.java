package com.earningscall.rag.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest {

    @NotBlank(message = "Question cannot be empty")
    private String question;

    private UUID documentId; // Optional: search only within specific document

    private String companyName; // Optional: filter by company
}
