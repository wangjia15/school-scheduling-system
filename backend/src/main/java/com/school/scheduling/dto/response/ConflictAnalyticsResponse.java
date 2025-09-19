package com.school.scheduling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictAnalyticsResponse {
    private String period;
    private LocalDateTime generatedAt;
    private ConflictTrend conflictTrend;
    private ResolutionMetrics resolutionMetrics;
    private PerformanceMetrics performanceMetrics;
    private PredictiveAnalytics predictiveAnalytics;
    private List<HotspotData> conflictHotspots;
    private Map<String, Object> recommendations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictTrend {
        private List<DailyTrend> dailyTrends;
        private List<WeeklyTrend> weeklyTrends;
        private List<MonthlyTrend> monthlyTrends;
        private TrendAnalysis overallTrend;
        private SeasonalPattern seasonalPattern;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResolutionMetrics {
        private double averageResolutionTimeHours;
        private double resolutionRate;
        private int resolvedLast7Days;
        private int resolvedLast30Days;
        private Map<String, Double> resolutionTimeByType;
        private Map<String, Double> resolutionTimeBySeverity;
        private List<ResolutionEfficiency> efficiencyMetrics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private double detectionAccuracy;
        private double falsePositiveRate;
        private double averageDetectionTimeMs;
        private int conflictsDetectedPerHour;
        private int systemLoadPercentage;
        private List<PerformanceMetric> detailedMetrics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictiveAnalytics {
        private int predictedConflictsNextWeek;
        private int predictedConflictsNextMonth;
        private List<PredictionFactor> riskFactors;
        private List<String> highRiskEntities;
        private double confidenceLevel;
        private List<Recommendation> preventiveRecommendations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrend {
        private String date;
        private int conflictsDetected;
        private int conflictsResolved;
        private int criticalConflicts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyTrend {
        private String week;
        private int totalConflicts;
        private int resolvedConflicts;
        private double resolutionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        private String month;
        private int totalConflicts;
        private int resolvedConflicts;
        private double averageResolutionTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendAnalysis {
        private String trendDirection; // "increasing", "decreasing", "stable"
        private double trendPercentage;
        private double confidenceInterval;
        private String analysisSummary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeasonalPattern {
        private Map<String, Double> monthlyPatterns;
        private Map<String, Double> dayOfWeekPatterns;
        private Map<String, Double> timeOfDayPatterns;
        private String peakSeason;
        private String offSeason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResolutionEfficiency {
        private String resolverType;
        private int conflictsResolved;
        private double averageResolutionTime;
        private double satisfactionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetric {
        private String metricName;
        private double value;
        private String unit;
        private double target;
        private String status; // "good", "warning", "critical"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionFactor {
        private String factor;
        private double impactScore;
        private String description;
        private List<String> affectedEntities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private String type;
        private String title;
        private String description;
        private int priority;
        private String timeframe;
        private double estimatedImpact;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotspotData {
        private String entityType;
        private Long entityId;
        private String entityName;
        private int conflictCount;
        private double riskScore;
        private String lastConflictDate;
        private List<String> conflictTypes;
    }
}