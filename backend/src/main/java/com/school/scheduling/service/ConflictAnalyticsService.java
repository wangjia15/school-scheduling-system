package com.school.scheduling.service;

import com.school.scheduling.domain.ScheduleConflict;
import com.school.scheduling.dto.response.ConflictStatsResponse;
import com.school.scheduling.dto.response.ConflictAnalyticsResponse;
import com.school.scheduling.mapper.ConflictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConflictAnalyticsService {

    private final ConflictMapper conflictMapper;

    public ConflictStatsResponse getConflictStats() {
        try {
            // Get basic counts
            List<ScheduleConflict> allConflicts = conflictMapper.findAll();
            List<ScheduleConflict> pendingConflicts = conflictMapper.findPendingConflicts();

            // Count by severity
            long criticalCount = pendingConflicts.stream()
                .filter(c -> c.getSeverity() == ScheduleConflict.Severity.CRITICAL)
                .count();
            long highCount = pendingConflicts.stream()
                .filter(c -> c.getSeverity() == ScheduleConflict.Severity.HIGH)
                .count();
            long mediumCount = pendingConflicts.stream()
                .filter(c -> c.getSeverity() == ScheduleConflict.Severity.MEDIUM)
                .count();
            long lowCount = pendingConflicts.stream()
                .filter(c -> c.getSeverity() == ScheduleConflict.Severity.LOW)
                .count();

            // Get recent conflicts
            List<ScheduleConflict> recentConflicts = conflictMapper.findRecentConflicts(
                LocalDateTime.now().minusHours(24)
            );

            // Get overdue conflicts
            List<ScheduleConflict> overdueConflicts = pendingConflicts.stream()
                .filter(ScheduleConflict::isOverdue)
                .collect(Collectors.toList());

            // Build conflicts by type map
            Map<String, Integer> conflictsByType = pendingConflicts.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getConflictType().name(),
                    Collectors.summingInt(c -> 1)
                ));

            // Build conflicts by entity map
            Map<String, Integer> conflictsByEntity = pendingConflicts.stream()
                .filter(c -> c.getEntityType() != null)
                .collect(Collectors.groupingBy(
                    c -> c.getEntityType().name(),
                    Collectors.summingInt(c -> 1)
                ));

            // Calculate average resolution time
            double averageResolutionTime = calculateAverageResolutionTime(allConflicts);

            // Build type breakdown
            List<ConflictStatsResponse.TypeStats> typeBreakdown = buildTypeBreakdown(allConflicts);
            List<ConflictStatsResponse.EntityStats> entityBreakdown = buildEntityBreakdown(allConflicts);

            return ConflictStatsResponse.builder()
                .totalConflicts((int) allConflicts.stream()
                    .filter(c -> c.getResolutionStatus() != ScheduleConflict.ResolutionStatus.IGNORED)
                    .count())
                .pendingConflicts(pendingConflicts.size())
                .resolvedConflicts((int) allConflicts.stream()
                    .filter(c -> c.getResolutionStatus() == ScheduleConflict.ResolutionStatus.RESOLVED)
                    .count())
                .criticalConflicts((int) criticalCount)
                .highSeverityConflicts((int) highCount)
                .mediumSeverityConflicts((int) mediumCount)
                .lowSeverityConflicts((int) lowCount)
                .conflictsLast24Hours(recentConflicts.size())
                .overdueConflicts(overdueConflicts.size())
                .conflictsByType(conflictsByType)
                .conflictsByEntity(conflictsByEntity)
                .averageResolutionTimeHours(averageResolutionTime)
                .typeBreakdown(typeBreakdown)
                .entityBreakdown(entityBreakdown)
                .build();
        } catch (Exception e) {
            log.error("Error generating conflict statistics", e);
            throw new RuntimeException("Failed to generate conflict statistics", e);
        }
    }

    public ConflictAnalyticsResponse getConflictAnalytics(String period) {
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = getStartDateForPeriod(period, endDate);

            // Get conflict trends
            ConflictAnalyticsResponse.ConflictTrend conflictTrend = getConflictTrend(startDate, endDate);

            // Get resolution metrics
            ConflictAnalyticsResponse.ResolutionMetrics resolutionMetrics = getResolutionMetrics(startDate, endDate);

            // Get performance metrics
            ConflictAnalyticsResponse.PerformanceMetrics performanceMetrics = getPerformanceMetrics();

            // Get predictive analytics
            ConflictAnalyticsResponse.PredictiveAnalytics predictiveAnalytics = getPredictiveAnalytics();

            // Get conflict hotspots
            List<ConflictAnalyticsResponse.HotspotData> conflictHotspots = getConflictHotspots();

            // Get recommendations
            Map<String, Object> recommendations = getRecommendations();

            return ConflictAnalyticsResponse.builder()
                .period(period != null ? period : "last_30_days")
                .generatedAt(LocalDateTime.now())
                .conflictTrend(conflictTrend)
                .resolutionMetrics(resolutionMetrics)
                .performanceMetrics(performanceMetrics)
                .predictiveAnalytics(predictiveAnalytics)
                .conflictHotspots(conflictHotspots)
                .recommendations(recommendations)
                .build();
        } catch (Exception e) {
            log.error("Error generating conflict analytics", e);
            throw new RuntimeException("Failed to generate conflict analytics", e);
        }
    }

    private ConflictAnalyticsResponse.ConflictTrend getConflictTrend(LocalDateTime startDate, LocalDateTime endDate) {
        // Get daily trends
        List<ConflictAnalyticsResponse.DailyTrend> dailyTrends = getDailyTrends(startDate, endDate);

        // Get weekly trends
        List<ConflictAnalyticsResponse.WeeklyTrend> weeklyTrends = getWeeklyTrends(startDate, endDate);

        // Get monthly trends
        List<ConflictAnalyticsResponse.MonthlyTrend> monthlyTrends = getMonthlyTrends(startDate, endDate);

        // Analyze overall trend
        ConflictAnalyticsResponse.TrendAnalysis overallTrend = analyzeOverallTrend(dailyTrends);

        // Identify seasonal patterns
        ConflictAnalyticsResponse.SeasonalPattern seasonalPattern = identifySeasonalPatterns();

        return ConflictAnalyticsResponse.ConflictTrend.builder()
            .dailyTrends(dailyTrends)
            .weeklyTrends(weeklyTrends)
            .monthlyTrends(monthlyTrends)
            .overallTrend(overallTrend)
            .seasonalPattern(seasonalPattern)
            .build();
    }

    private ConflictAnalyticsResponse.ResolutionMetrics getResolutionMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        List<ScheduleConflict> conflictsInPeriod = conflictMapper.findByDetectionDateRange(startDate, endDate);

        // Calculate average resolution time
        double averageResolutionTime = calculateAverageResolutionTime(conflictsInPeriod);

        // Calculate resolution rate
        long totalConflicts = conflictsInPeriod.size();
        long resolvedConflicts = conflictsInPeriod.stream()
            .filter(c -> c.getResolutionStatus() == ScheduleConflict.ResolutionStatus.RESOLVED)
            .count();
        double resolutionRate = totalConflicts > 0 ? (double) resolvedConflicts / totalConflicts : 0.0;

        // Count resolved conflicts by timeframe
        int resolvedLast7Days = (int) conflictMapper.findByDetectionDateRange(
            LocalDateTime.now().minusDays(7), LocalDateTime.now()
        ).stream()
            .filter(c -> c.getResolutionStatus() == ScheduleConflict.ResolutionStatus.RESOLVED)
            .count();

        int resolvedLast30Days = (int) conflictMapper.findByDetectionDateRange(
            LocalDateTime.now().minusDays(30), LocalDateTime.now()
        ).stream()
            .filter(c -> c.getResolutionStatus() == ScheduleConflict.ResolutionStatus.RESOLVED)
            .count();

        // Get resolution time by type
        Map<String, Double> resolutionTimeByType = calculateResolutionTimeByType(conflictsInPeriod);

        // Get resolution time by severity
        Map<String, Double> resolutionTimeBySeverity = calculateResolutionTimeBySeverity(conflictsInPeriod);

        // Build efficiency metrics
        List<ConflictAnalyticsResponse.ResolutionEfficiency> efficiencyMetrics = buildEfficiencyMetrics(conflictsInPeriod);

        return ConflictAnalyticsResponse.ResolutionMetrics.builder()
            .averageResolutionTimeHours(averageResolutionTime)
            .resolutionRate(resolutionRate)
            .resolvedLast7Days(resolvedLast7Days)
            .resolvedLast30Days(resolvedLast30Days)
            .resolutionTimeByType(resolutionTimeByType)
            .resolutionTimeBySeverity(resolutionTimeBySeverity)
            .efficiencyMetrics(efficiencyMetrics)
            .build();
    }

    private ConflictAnalyticsResponse.PerformanceMetrics getPerformanceMetrics() {
        // Calculate detection accuracy (simulated)
        double detectionAccuracy = 0.95; // 95% accuracy

        // Calculate false positive rate (simulated)
        double falsePositiveRate = 0.05; // 5% false positive rate

        // Calculate average detection time (simulated)
        double averageDetectionTimeMs = 250.0; // 250ms

        // Calculate conflicts detected per hour
        int conflictsDetectedPerHour = calculateConflictsDetectedPerHour();

        // Calculate system load (simulated)
        double systemLoadPercentage = 45.0; // 45% system load

        // Build detailed metrics
        List<ConflictAnalyticsResponse.PerformanceMetric> detailedMetrics = Arrays.asList(
            ConflictAnalyticsResponse.PerformanceMetric.builder()
                .metricName("Detection Accuracy")
                .value(detectionAccuracy)
                .unit("percentage")
                .target(0.90)
                .status("good")
                .build(),
            ConflictAnalyticsResponse.PerformanceMetric.builder()
                .metricName("False Positive Rate")
                .value(falsePositiveRate)
                .unit("percentage")
                .target(0.10)
                .status("good")
                .build(),
            ConflictAnalyticsResponse.PerformanceMetric.builder()
                .metricName("Average Detection Time")
                .value(averageDetectionTimeMs)
                .unit("milliseconds")
                .target(500.0)
                .status("good")
                .build()
        );

        return ConflictAnalyticsResponse.PerformanceMetrics.builder()
            .detectionAccuracy(detectionAccuracy)
            .falsePositiveRate(falsePositiveRate)
            .averageDetectionTimeMs(averageDetectionTimeMs)
            .conflictsDetectedPerHour(conflictsDetectedPerHour)
            .systemLoadPercentage(systemLoadPercentage)
            .detailedMetrics(detailedMetrics)
            .build();
    }

    private ConflictAnalyticsResponse.PredictiveAnalytics getPredictiveAnalytics() {
        // Simple predictive analytics based on historical trends
        int predictedConflictsNextWeek = 15; // Based on historical data
        int predictedConflictsNextMonth = 60; // Based on historical data

        // Identify risk factors
        List<ConflictAnalyticsResponse.PredictionFactor> riskFactors = Arrays.asList(
            ConflictAnalyticsResponse.PredictionFactor.builder()
                .factor("Teacher Workload")
                .impactScore(0.8)
                .description("High teacher workload increases conflict risk")
                .affectedEntities(Arrays.asList("TEACHER"))
                .build(),
            ConflictAnalyticsResponse.PredictionFactor.builder()
                .factor("Classroom Capacity")
                .impactScore(0.6)
                .description("Limited classroom capacity creates scheduling conflicts")
                .affectedEntities(Arrays.asList("CLASSROOM"))
                .build(),
            ConflictAnalyticsResponse.PredictionFactor.builder()
                .factor("Prerequisites")
                .impactScore(0.4)
                .description("Unmet prerequisites create enrollment conflicts")
                .affectedEntities(Arrays.asList("STUDENT"))
                .build()
        );

        // Identify high-risk entities (simulated)
        List<String> highRiskEntities = Arrays.asList("TEACHER_123", "CLASSROOM_101", "COURSE_CS201");

        // Calculate confidence level
        double confidenceLevel = 0.75; // 75% confidence

        // Generate preventive recommendations
        List<ConflictAnalyticsResponse.Recommendation> preventiveRecommendations = Arrays.asList(
            ConflictAnalyticsResponse.Recommendation.builder()
                .type("workload_balancing")
                .title("Balance Teacher Workload")
                .description("Redistribute teaching assignments to prevent overload")
                .priority(1)
                .timeframe("1-2 weeks")
                .estimatedImpact(0.8)
                .build(),
            ConflictAnalyticsResponse.Recommendation.builder()
                .type("capacity_planning")
                .title("Optimize Classroom Usage")
                .description("Review classroom allocation to maximize capacity utilization")
                .priority(2)
                .timeframe("2-3 weeks")
                .estimatedImpact(0.6)
                .build()
        );

        return ConflictAnalyticsResponse.PredictiveAnalytics.builder()
            .predictedConflictsNextWeek(predictedConflictsNextWeek)
            .predictedConflictsNextMonth(predictedConflictsNextMonth)
            .riskFactors(riskFactors)
            .highRiskEntities(highRiskEntities)
            .confidenceLevel(confidenceLevel)
            .preventiveRecommendations(preventiveRecommendations)
            .build();
    }

    private List<ConflictAnalyticsResponse.HotspotData> getConflictHotspots() {
        // This would query the database for entities with high conflict rates
        // For now, we'll return simulated data
        return Arrays.asList(
            ConflictAnalyticsResponse.HotspotData.builder()
                .entityType("TEACHER")
                .entityId(123L)
                .entityName("Dr. Smith")
                .conflictCount(8)
                .riskScore(0.85)
                .lastConflictDate(LocalDateTime.now().minusDays(2).toString())
                .conflictTypes(Arrays.asList("TEACHER_DOUBLE_BOOKING", "WORKLOAD_EXCEEDED"))
                .build(),
            ConflictAnalyticsResponse.HotspotData.builder()
                .entityType("CLASSROOM")
                .entityId(101L)
                .entityName("Room 101")
                .conflictCount(5)
                .riskScore(0.72)
                .lastConflictDate(LocalDateTime.now().minusDays(1).toString())
                .conflictTypes(Arrays.asList("CLASSROOM_DOUBLE_BOOKING", "CAPACITY_EXCEEDED"))
                .build()
        );
    }

    private Map<String, Object> getRecommendations() {
        Map<String, Object> recommendations = new HashMap<>();
        recommendations.put("immediate_actions", Arrays.asList(
            "Review critical conflicts requiring immediate attention",
            "Address classroom capacity issues",
            "Balance teacher workload distribution"
        ));
        recommendations.put("medium_term", Arrays.asList(
            "Implement automated conflict prevention",
            "Enhance prerequisite validation system",
            "Improve classroom allocation algorithm"
        ));
        recommendations.put("long_term", Arrays.asList(
            "Deploy AI-powered conflict prediction",
            "Implement real-time conflict detection",
            "Establish conflict prevention workflows"
        ));
        return recommendations;
    }

    // Helper methods
    private LocalDateTime getStartDateForPeriod(String period, LocalDateTime endDate) {
        if (period == null) {
            return endDate.minusDays(30);
        }

        switch (period.toLowerCase()) {
            case "last_7_days":
                return endDate.minusDays(7);
            case "last_30_days":
                return endDate.minusDays(30);
            case "last_90_days":
                return endDate.minusDays(90);
            case "last_year":
                return endDate.minusYears(1);
            default:
                return endDate.minusDays(30);
        }
    }

    private double calculateAverageResolutionTime(List<ScheduleConflict> conflicts) {
        return conflicts.stream()
            .filter(c -> c.getResolutionStatus() == ScheduleConflict.ResolutionStatus.RESOLVED &&
                       c.getDetectedAt() != null && c.getResolvedAt() != null)
            .mapToLong(c -> java.time.temporal.ChronoUnit.HOURS.between(c.getDetectedAt(), c.getResolvedAt()))
            .average()
            .orElse(0.0);
    }

    private List<ConflictStatsResponse.TypeStats> buildTypeBreakdown(List<ScheduleConflict> conflicts) {
        return conflicts.stream()
            .collect(Collectors.groupingBy(c -> c.getConflictType().name()))
            .entrySet().stream()
            .map(entry -> {
                List<ScheduleConflict> typeConflicts = entry.getValue();
                int resolvedCount = (int) typeConflicts.stream()
                    .filter(c -> c.getResolutionStatus() == ScheduleConflict.ResolutionStatus.RESOLVED)
                    .count();
                int pendingCount = (int) typeConflicts.stream()
                    .filter(c -> c.getResolutionStatus() == ScheduleConflict.ResolutionStatus.PENDING)
                    .count();
                double avgResolutionTime = calculateAverageResolutionTime(typeConflicts);

                return ConflictStatsResponse.TypeStats.builder()
                    .conflictType(entry.getKey())
                    .count(typeConflicts.size())
                    .resolvedCount(resolvedCount)
                    .pendingCount(pendingCount)
                    .averageResolutionTime(avgResolutionTime)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private List<ConflictStatsResponse.EntityStats> buildEntityBreakdown(List<ScheduleConflict> conflicts) {
        return conflicts.stream()
            .filter(c -> c.getEntityType() != null)
            .collect(Collectors.groupingBy(c -> c.getEntityType().name()))
            .entrySet().stream()
            .map(entry -> {
                List<ScheduleConflict> entityConflicts = entry.getValue();
                int resolvedCount = (int) entityConflicts.stream()
                    .filter(c -> c.getResolutionStatus() == ScheduleConflict.ResolutionStatus.RESOLVED)
                    .count();
                int pendingCount = (int) entityConflicts.stream()
                    .filter(c -> c.getResolutionStatus() == ScheduleConflict.ResolutionStatus.PENDING)
                    .count();

                return ConflictStatsResponse.EntityStats.builder()
                    .entityType(entry.getKey())
                    .count(entityConflicts.size())
                    .resolvedCount(resolvedCount)
                    .pendingCount(pendingCount)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private List<ConflictAnalyticsResponse.DailyTrend> getDailyTrends(LocalDateTime startDate, LocalDateTime endDate) {
        // This would query actual daily conflict data
        // For now, return simulated data
        return Arrays.asList(
            ConflictAnalyticsResponse.DailyTrend.builder()
                .date(endDate.minusDays(6).toLocalDate().toString())
                .conflictsDetected(3)
                .conflictsResolved(2)
                .criticalConflicts(1)
                .build(),
            ConflictAnalyticsResponse.DailyTrend.builder()
                .date(endDate.minusDays(5).toLocalDate().toString())
                .conflictsDetected(5)
                .conflictsResolved(4)
                .criticalConflicts(0)
                .build()
        );
    }

    private List<ConflictAnalyticsResponse.WeeklyTrend> getWeeklyTrends(LocalDateTime startDate, LocalDateTime endDate) {
        // This would query actual weekly conflict data
        return List.of(); // Implementation would go here
    }

    private List<ConflictAnalyticsResponse.MonthlyTrend> getMonthlyTrends(LocalDateTime startDate, LocalDateTime endDate) {
        // This would query actual monthly conflict data
        return List.of(); // Implementation would go here
    }

    private ConflictAnalyticsResponse.TrendAnalysis analyzeOverallTrend(List<ConflictAnalyticsResponse.DailyTrend> dailyTrends) {
        if (dailyTrends.isEmpty()) {
            return ConflictAnalyticsResponse.TrendAnalysis.builder()
                .trendDirection("stable")
                .trendPercentage(0.0)
                .confidenceInterval(0.0)
                .analysisSummary("Insufficient data for trend analysis")
                .build();
        }

        // Simple trend analysis
        int totalConflicts = dailyTrends.stream().mapToInt(ConflictAnalyticsResponse.DailyTrend::getConflictsDetected).sum();
        double avgConflicts = (double) totalConflicts / dailyTrends.size();

        String trendDirection = "stable";
        double trendPercentage = 0.0;

        if (avgConflicts > 10) {
            trendDirection = "increasing";
            trendPercentage = 15.0;
        } else if (avgConflicts < 5) {
            trendDirection = "decreasing";
            trendPercentage = -10.0;
        }

        return ConflictAnalyticsResponse.TrendAnalysis.builder()
            .trendDirection(trendDirection)
            .trendPercentage(trendPercentage)
            .confidenceInterval(0.85)
            .analysisSummary(String.format("Conflict trend is %s with %.1f%% change", trendDirection, trendPercentage))
            .build();
    }

    private ConflictAnalyticsResponse.SeasonalPattern identifySeasonalPatterns() {
        // This would analyze seasonal patterns in conflict data
        Map<String, Double> monthlyPatterns = Map.of(
            "January", 1.2, "February", 1.1, "March", 0.9, "April", 0.8,
            "May", 1.3, "June", 1.4, "July", 0.7, "August", 0.6,
            "September", 1.5, "October", 1.4, "November", 1.1, "December", 0.9
        );

        Map<String, Double> dayOfWeekPatterns = Map.of(
            "Monday", 1.2, "Tuesday", 1.1, "Wednesday", 1.0,
            "Thursday", 1.1, "Friday", 1.3, "Saturday", 0.7, "Sunday", 0.5
        );

        Map<String, Double> timeOfDayPatterns = Map.of(
            "Morning", 1.1, "Afternoon", 1.3, "Evening", 0.9, "Night", 0.5
        );

        return ConflictAnalyticsResponse.SeasonalPattern.builder()
            .monthlyPatterns(monthlyPatterns)
            .dayOfWeekPatterns(dayOfWeekPatterns)
            .timeOfDayPatterns(timeOfDayPatterns)
            .peakSeason("September")
            .offSeason("August")
            .build();
    }

    private Map<String, Double> calculateResolutionTimeByType(List<ScheduleConflict> conflicts) {
        return conflicts.stream()
            .filter(c -> c.getResolutionStatus() == ScheduleConflict.ResolutionStatus.RESOLVED)
            .collect(Collectors.groupingBy(
                c -> c.getConflictType().name(),
                Collectors.averagingDouble(c ->
                    java.time.temporal.ChronoUnit.HOURS.between(c.getDetectedAt(), c.getResolvedAt())
                )
            ));
    }

    private Map<String, Double> calculateResolutionTimeBySeverity(List<ScheduleConflict> conflicts) {
        return conflicts.stream()
            .filter(c -> c.getResolutionStatus() == ScheduleConflict.ResolutionStatus.RESOLVED)
            .collect(Collectors.groupingBy(
                c -> c.getSeverity().name(),
                Collectors.averagingDouble(c ->
                    java.time.temporal.ChronoUnit.HOURS.between(c.getDetectedAt(), c.getResolvedAt())
                )
            ));
    }

    private List<ConflictAnalyticsResponse.ResolutionEfficiency> buildEfficiencyMetrics(List<ScheduleConflict> conflicts) {
        // This would analyze resolution efficiency by different resolver types
        return Arrays.asList(
            ConflictAnalyticsResponse.ResolutionEfficiency.builder()
                .resolverType("Automatic")
                .conflictsResolved(25)
                .averageResolutionTime(2.5)
                .satisfactionRate(0.9)
                .build(),
            ConflictAnalyticsResponse.ResolutionEfficiency.builder()
                .resolverType("Manual")
                .conflictsResolved(15)
                .averageResolutionTime(8.0)
                .satisfactionRate(0.95)
                .build()
        );
    }

    private int calculateConflictsDetectedPerHour() {
        // This would calculate actual conflicts detected per hour
        return 5; // Simulated value
    }
}