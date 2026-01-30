package com.earningscall.rag.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private UUID documentId;
    private String filename;
    private String companyName;
    private String status;
    private LocalDateTime uploadTimestamp;
    private String message;
}
