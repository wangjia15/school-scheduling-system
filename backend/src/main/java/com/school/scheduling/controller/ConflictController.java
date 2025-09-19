package com.school.scheduling.controller;

import com.school.scheduling.domain.ScheduleConflict;
import com.school.scheduling.dto.response.ApiResponse;
import com.school.scheduling.dto.response.ConflictResponse;
import com.school.scheduling.dto.response.ConflictStatsResponse;
import com.school.scheduling.dto.response.ConflictAnalyticsResponse;
import com.school.scheduling.dto.request.ConflictResolutionRequest;
import com.school.scheduling.dto.request.ConflictSearchRequest;
import com.school.scheduling.service.ConflictDetectionService;
import com.school.scheduling.service.ConflictAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/conflicts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConflictController {

    private final ConflictDetectionService conflictDetectionService;
    private final ConflictAnalyticsService conflictAnalyticsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<List<ConflictResponse>>> getAllConflicts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {

        try {
            List<ScheduleConflict> conflicts = conflictDetectionService.getPendingConflicts();

            // Apply filters
            if (severity != null) {
                conflicts = conflicts.stream()
                    .filter(c -> c.getSeverity().name().equalsIgnoreCase(severity))
                    .collect(Collectors.toList());
            }
            if (status != null) {
                conflicts = conflicts.stream()
                    .filter(c -> c.getResolutionStatus().name().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
            }
            if (type != null) {
                conflicts = conflicts.stream()
                    .filter(c -> c.getConflictType().name().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
            }

            List<ConflictResponse> response = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(
                ApiResponse.<List<ConflictResponse>>builder()
                    .success(true)
                    .message("Conflicts retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving conflicts", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<List<ConflictResponse>>builder()
                    .success(false)
                    .message("Error retrieving conflicts: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<List<ConflictResponse>>> getPendingConflicts() {
        try {
            List<ScheduleConflict> conflicts = conflictDetectionService.getPendingConflicts();
            List<ConflictResponse> response = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(
                ApiResponse.<List<ConflictResponse>>builder()
                    .success(true)
                    .message("Pending conflicts retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving pending conflicts", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<List<ConflictResponse>>builder()
                    .success(false)
                    .message("Error retrieving pending conflicts: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @GetMapping("/critical")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<List<ConflictResponse>>> getCriticalConflicts() {
        try {
            List<ScheduleConflict> conflicts = conflictDetectionService.getCriticalConflicts();
            List<ConflictResponse> response = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(
                ApiResponse.<List<ConflictResponse>>builder()
                    .success(true)
                    .message("Critical conflicts retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving critical conflicts", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<List<ConflictResponse>>builder()
                    .success(false)
                    .message("Error retrieving critical conflicts: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @GetMapping("/high-priority")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<List<ConflictResponse>>> getHighPriorityConflicts() {
        try {
            List<ScheduleConflict> conflicts = conflictDetectionService.getHighPriorityConflicts();
            List<ConflictResponse> response = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(
                ApiResponse.<List<ConflictResponse>>builder()
                    .success(true)
                    .message("High priority conflicts retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving high priority conflicts", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<List<ConflictResponse>>builder()
                    .success(false)
                    .message("Error retrieving high priority conflicts: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<ConflictResponse>> getConflictById(@PathVariable Long id) {
        try {
            // This would need to be implemented in ConflictMapper
            // For now, we'll return a not found response
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving conflict by ID: {}", id, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<ConflictResponse>builder()
                    .success(false)
                    .message("Error retrieving conflict: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @PostMapping("/detect")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<List<ConflictResponse>>> detectConflicts() {
        try {
            List<ScheduleConflict> conflicts = conflictDetectionService.detectAllSystemConflicts();
            List<ConflictResponse> response = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(
                ApiResponse.<List<ConflictResponse>>builder()
                    .success(true)
                    .message("Conflict detection completed successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error during conflict detection", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<List<ConflictResponse>>builder()
                    .success(false)
                    .message("Error during conflict detection: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @PostMapping("/resolve/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<String>> resolveConflict(
            @PathVariable Long id,
            @Valid @RequestBody ConflictResolutionRequest request) {
        try {
            conflictDetectionService.resolveConflict(id, request.getResolutionNotes());

            return ResponseEntity.ok(
                ApiResponse.<String>builder()
                    .success(true)
                    .message("Conflict resolved successfully")
                    .data("Conflict ID " + id + " has been resolved")
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error resolving conflict: {}", id, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Error resolving conflict: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @PostMapping("/ignore/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<String>> ignoreConflict(
            @PathVariable Long id,
            @Valid @RequestBody ConflictResolutionRequest request) {
        try {
            conflictDetectionService.ignoreConflict(id, request.getResolutionNotes());

            return ResponseEntity.ok(
                ApiResponse.<String>builder()
                    .success(true)
                    .message("Conflict ignored successfully")
                    .data("Conflict ID " + id + " has been ignored")
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error ignoring conflict: {}", id, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Error ignoring conflict: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @PostMapping("/batch-resolve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<String>> batchResolveConflicts(
            @RequestBody List<Long> conflictIds) {
        try {
            for (Long conflictId : conflictIds) {
                conflictDetectionService.resolveConflict(conflictId, "Batch resolved by administrator");
            }

            return ResponseEntity.ok(
                ApiResponse.<String>builder()
                    .success(true)
                    .message("Batch resolve completed successfully")
                    .data("Resolved " + conflictIds.size() + " conflicts")
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error batch resolving conflicts", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Error batch resolving conflicts: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<ConflictStatsResponse>> getConflictStats() {
        try {
            ConflictStatsResponse stats = conflictAnalyticsService.getConflictStats();

            return ResponseEntity.ok(
                ApiResponse.<ConflictStatsResponse>builder()
                    .success(true)
                    .message("Conflict statistics retrieved successfully")
                    .data(stats)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving conflict statistics", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<ConflictStatsResponse>builder()
                    .success(false)
                    .message("Error retrieving conflict statistics: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<ConflictAnalyticsResponse>> getConflictAnalytics(
            @RequestParam(required = false) String period) {
        try {
            ConflictAnalyticsResponse analytics = conflictAnalyticsService.getConflictAnalytics(period);

            return ResponseEntity.ok(
                ApiResponse.<ConflictAnalyticsResponse>builder()
                    .success(true)
                    .message("Conflict analytics retrieved successfully")
                    .data(analytics)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving conflict analytics", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<ConflictAnalyticsResponse>builder()
                    .success(false)
                    .message("Error retrieving conflict analytics: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<List<ConflictResponse>>> searchConflicts(
            @Valid @RequestBody ConflictSearchRequest request) {
        try {
            // This would need to be implemented in ConflictMapper
            // For now, return empty list
            List<ConflictResponse> response = List.of();

            return ResponseEntity.ok(
                ApiResponse.<List<ConflictResponse>>builder()
                    .success(true)
                    .message("Conflict search completed")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error searching conflicts", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<List<ConflictResponse>>builder()
                    .success(false)
                    .message("Error searching conflicts: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<List<ConflictResponse>>> getConflictsForTeacher(@PathVariable Long teacherId) {
        try {
            List<ScheduleConflict> conflicts = conflictDetectionService.detectConflictsForTeacher(teacherId);
            List<ConflictResponse> response = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(
                ApiResponse.<List<ConflictResponse>>builder()
                    .success(true)
                    .message("Teacher conflicts retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving conflicts for teacher: {}", teacherId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<List<ConflictResponse>>builder()
                    .success(false)
                    .message("Error retrieving teacher conflicts: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @GetMapping("/classroom/{classroomId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<List<ConflictResponse>>> getConflictsForClassroom(@PathVariable Long classroomId) {
        try {
            List<ScheduleConflict> conflicts = conflictDetectionService.detectConflictsForClassroom(classroomId);
            List<ConflictResponse> response = conflicts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(
                ApiResponse.<List<ConflictResponse>>builder()
                    .success(true)
                    .message("Classroom conflicts retrieved successfully")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving conflicts for classroom: {}", classroomId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<List<ConflictResponse>>builder()
                    .success(false)
                    .message("Error retrieving classroom conflicts: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    @GetMapping("/schedule/{scheduleId}/has-conflicts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ApiResponse<Boolean>> hasConflicts(@PathVariable Long scheduleId) {
        try {
            boolean hasConflicts = conflictDetectionService.hasConflicts(scheduleId);

            return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                    .success(true)
                    .message("Conflict check completed")
                    .data(hasConflicts)
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error checking conflicts for schedule: {}", scheduleId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.<Boolean>builder()
                    .success(false)
                    .message("Error checking conflicts: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    private ConflictResponse convertToResponse(ScheduleConflict conflict) {
        ConflictResponse response = new ConflictResponse();
        response.setId(conflict.getId());
        response.setConflictType(conflict.getConflictType().name());
        response.setSeverity(conflict.getSeverity().name());
        response.setDescription(conflict.getDescription());
        response.setResolutionStatus(conflict.getResolutionStatus().name());
        response.setDetectedAt(conflict.getDetectedAt());
        response.setResolvedAt(conflict.getResolvedAt());
        response.setResolutionNotes(conflict.getResolutionNotes());
        response.setEntityType(conflict.getEntityType() != null ? conflict.getEntityType().name() : null);
        response.setEntityId(conflict.getEntityId());

        // Add related schedule information if available
        if (conflict.getSchedule1() != null) {
            response.setSchedule1Id(conflict.getSchedule1().getId());
            response.setSchedule1Summary(conflict.getSchedule1().getScheduleSummary());
        }
        if (conflict.getSchedule2() != null) {
            response.setSchedule2Id(conflict.getSchedule2().getId());
            response.setSchedule2Summary(conflict.getSchedule2().getScheduleSummary());
        }

        // Add calculated fields
        response.setRequiresImmediateAttention(conflict.requiresImmediateAttention());
        response.setHoursSinceDetection(conflict.getHoursSinceDetection());
        response.setConflictSummary(conflict.getConflictSummary());

        return response;
    }
}