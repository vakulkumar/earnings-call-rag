package com.earningscall.rag.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    private String answer;
    private Double confidenceScore;
    private List<SourceCitation> sources;
    private Long processingTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceCitation {
        private String documentName;
        private String companyName;
        private Integer pageNumber;
        private String relevantText;
        private Double similarityScore;
    }
}
