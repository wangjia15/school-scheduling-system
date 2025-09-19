package com.school.scheduling.service;

import com.school.scheduling.domain.ScheduleConflict;
import com.school.scheduling.websocket.ConflictWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConflictNotificationService {

    private final ConflictDetectionService conflictDetectionService;
    private final ConflictWebSocketHandler webSocketHandler;

    @Async
    public CompletableFuture<Void> notifyConflictDetected(ScheduleConflict conflict) {
        return CompletableFuture.runAsync(() -> {
            try {
                webSocketHandler.broadcastConflictDetected(conflict);
                log.info("Broadcasted conflict detection: {} for entity {}",
                    conflict.getConflictType(), conflict.getEntityId());
            } catch (Exception e) {
                log.error("Error broadcasting conflict detection", e);
            }
        });
    }

    @Async
    public CompletableFuture<Void> notifyConflictResolved(Long conflictId, String resolutionNotes) {
        return CompletableFuture.runAsync(() -> {
            try {
                webSocketHandler.broadcastConflictResolved(conflictId, resolutionNotes);
                log.info("Broadcasted conflict resolution: {}", conflictId);
            } catch (Exception e) {
                log.error("Error broadcasting conflict resolution", e);
            }
        });
    }

    @Async
    public CompletableFuture<Void> notifySystemAlert(String level, String title, String message,
                                                     String entityType, Long entityId) {
        return CompletableFuture.runAsync(() -> {
            try {
                ConflictWebSocketHandler.SystemAlertData alert = ConflictWebSocketHandler.SystemAlertData.builder()
                    .level(level)
                    .title(title)
                    .message(message)
                    .entityType(entityType)
                    .entityId(entityId)
                    .timestamp(LocalDateTime.now())
                    .build();

                webSocketHandler.broadcastSystemAlert(alert);
                log.info("Broadcasted system alert: {} - {}", title, message);
            } catch (Exception e) {
                log.error("Error broadcasting system alert", e);
            }
        });
    }

    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void sendConflictStatsUpdate() {
        try {
            List<ScheduleConflict> pendingConflicts = conflictDetectionService.getPendingConflicts();
            List<ScheduleConflict> criticalConflicts = conflictDetectionService.getCriticalConflicts();

            int totalConflicts = pendingConflicts.size();
            int criticalCount = criticalConflicts.size();
            int highCount = (int) pendingConflicts.stream()
                .filter(c -> c.getSeverity() == ScheduleConflict.Severity.HIGH)
                .count();
            int mediumCount = (int) pendingConflicts.stream()
                .filter(c -> c.getSeverity() == ScheduleConflict.Severity.MEDIUM)
                .count();
            int lowCount = (int) pendingConflicts.stream()
                .filter(c -> c.getSeverity() == ScheduleConflict.Severity.LOW)
                .count();

            ConflictWebSocketHandler.ConflictStatsData stats = ConflictWebSocketHandler.ConflictStatsData.builder()
                .totalConflicts(totalConflicts)
                .pendingConflicts(totalConflicts)
                .criticalConflicts(criticalCount)
                .highSeverityConflicts(highCount)
                .mediumSeverityConflicts(mediumCount)
                .lowSeverityConflicts(lowCount)
                .lastUpdated(LocalDateTime.now())
                .build();

            webSocketHandler.broadcastConflictStats(stats);
            log.debug("Sent conflict stats update: {} total conflicts", totalConflicts);
        } catch (Exception e) {
            log.error("Error sending conflict stats update", e);
        }
    }

    @Scheduled(fixedDelay = 60000) // Every minute
    public void sendHeartbeat() {
        try {
            webSocketHandler.sendHeartbeat();
            log.debug("Sent WebSocket heartbeat to {} clients", webSocketHandler.getActiveConnections());
        } catch (Exception e) {
            log.error("Error sending heartbeat", e);
        }
    }

    public void notifyCriticalConflictDetected(ScheduleConflict conflict) {
        if (conflict.getSeverity() == ScheduleConflict.Severity.CRITICAL) {
            notifySystemAlert(
                "critical",
                "Critical Conflict Detected",
                String.format("Critical %s conflict detected for %s: %s",
                    conflict.getConflictType(),
                    conflict.getEntityType(),
                    conflict.getDescription()),
                conflict.getEntityType() != null ? conflict.getEntityType().name() : null,
                conflict.getEntityId()
            );
        }
    }

    public void notifyHighConflictLoad(int conflictCount, int threshold) {
        if (conflictCount > threshold) {
            notifySystemAlert(
                "warning",
                "High Conflict Load",
                String.format("System currently has %d pending conflicts (threshold: %d)",
                    conflictCount, threshold),
                "system",
                null
            );
        }
    }

    public void notifyConflictTrendAnalysis(String trendType, String analysis) {
        notifySystemAlert(
            "info",
            "Conflict Trend Analysis",
            analysis,
            "analytics",
            null
        );
    }

    public int getActiveConnections() {
        return webSocketHandler.getActiveConnections();
    }

    public void notifyMaintenanceMode(String message) {
        notifySystemAlert(
            "info",
            "System Maintenance",
            message,
            "system",
            null
        );
    }

    public void notifyPerformanceAlert(String metric, String value, String threshold) {
        notifySystemAlert(
            "warning",
            "Performance Alert",
            String.format("%s has exceeded threshold: %s > %s", metric, value, threshold),
            "performance",
            null
        );
    }
}