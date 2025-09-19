package com.school.scheduling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictStatsResponse {
    private int totalConflicts;
    private int pendingConflicts;
    private int resolvedConflicts;
    private int criticalConflicts;
    private int highSeverityConflicts;
    private int mediumSeverityConflicts;
    private int lowSeverityConflicts;
    private int conflictsLast24Hours;
    private int overdueConflicts;
    private Map<String, Integer> conflictsByType;
    private Map<String, Integer> conflictsByEntity;
    private double averageResolutionTimeHours;
    private List<TypeStats> typeBreakdown;
    private List<EntityStats> entityBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeStats {
        private String conflictType;
        private int count;
        private int resolvedCount;
        private int pendingCount;
        private double averageResolutionTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityStats {
        private String entityType;
        private int count;
        private int resolvedCount;
        private int pendingCount;
    }
}