package com.school.scheduling.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictSearchRequest {
    private String searchText;
    private String conflictType;
    private String severity;
    private String resolutionStatus;
    private String entityType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String sortBy;
    private boolean ascending;
    private int page = 0;
    private int size = 20;
}