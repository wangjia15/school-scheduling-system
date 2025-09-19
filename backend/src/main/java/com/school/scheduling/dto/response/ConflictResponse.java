package com.school.scheduling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictResponse {
    private Long id;
    private String conflictType;
    private String severity;
    private String description;
    private String resolutionStatus;
    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private String entityType;
    private Long entityId;
    private Long schedule1Id;
    private String schedule1Summary;
    private Long schedule2Id;
    private String schedule2Summary;
    private boolean requiresImmediateAttention;
    private long hoursSinceDetection;
    private String conflictSummary;
}