package com.school.scheduling.controller;

import com.school.scheduling.dto.response.ApiResponse;
import com.school.scheduling.exception.BadRequestException;
import com.school.scheduling.mapper.ScheduleMapper;
import com.school.scheduling.mapper.ScheduleConflictMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administration", description = "System administration endpoints")
public class AdminController {

    private final ScheduleMapper scheduleMapper;
    private final ScheduleConflictMapper scheduleConflictMapper;

    public AdminController(ScheduleMapper scheduleMapper, ScheduleConflictMapper scheduleConflictMapper) {
        this.scheduleMapper = scheduleMapper;
        this.scheduleConflictMapper = scheduleConflictMapper;
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get dashboard statistics", description = "Retrieve system statistics for admin dashboard")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {

        try {
            Map<String, Object> stats = Map.of(
                    "totalSchedules", scheduleMapper.countAll(),
                    "activeConflicts", scheduleConflictMapper.countActiveConflicts(),
                    "todaySchedules", scheduleMapper.countTodaysSchedules(),
                    "upcomingSchedules", scheduleMapper.countUpcomingSchedules(7)
            );

            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve dashboard statistics", e);
        }
    }

    @PostMapping("/schedules/cleanup-expired")
    @Operation(summary = "Clean up expired schedules", description = "Remove expired and past schedules from the system")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Cleanup completed successfully"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> cleanupExpiredSchedules() {

        try {
            int cleanedCount = scheduleMapper.cleanupExpiredSchedules();
            return ResponseEntity.ok(ApiResponse.success("Cleaned up " + cleanedCount + " expired schedules"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to cleanup expired schedules", e);
        }
    }

    @PostMapping("/conflicts/detect-all")
    @Operation(summary = "Detect all conflicts", description = "Run conflict detection on all schedules")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Conflict detection completed"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> detectAllConflicts() {

        try {
            int conflictCount = scheduleConflictMapper.detectAllConflicts();
            return ResponseEntity.ok(ApiResponse.success("Detected " + conflictCount + " schedule conflicts"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to detect conflicts", e);
        }
    }
}