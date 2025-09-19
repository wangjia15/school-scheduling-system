package com.school.scheduling.controller;

import com.school.scheduling.dto.PageRequest;
import com.school.scheduling.dto.response.ApiResponse;
import com.school.scheduling.dto.response.PaginatedResponse;
import com.school.scheduling.dto.response.ScheduleResponse;
import com.school.scheduling.dto.request.ScheduleRequest;
import com.school.scheduling.exception.ResourceNotFoundException;
import com.school.scheduling.exception.BadRequestException;
import com.school.scheduling.mapper.ScheduleMapper;
import com.school.scheduling.validation.ScheduleValidator;
import com.school.scheduling.validation.ValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@Tag(name = "Schedules", description = "Schedule management endpoints")
public class ScheduleController {

    private final ScheduleMapper scheduleMapper;
    private final ScheduleValidator scheduleValidator;

    public ScheduleController(ScheduleMapper scheduleMapper, ScheduleValidator scheduleValidator) {
        this.scheduleMapper = scheduleMapper;
        this.scheduleValidator = scheduleValidator;
    }

    @GetMapping
    @Operation(summary = "Get all schedules", description = "Retrieve a paginated list of all schedules")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved schedules",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<PaginatedResponse<ScheduleResponse>>> getAllSchedules(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String direction,
            @Parameter(description = "Search term") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by date from") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Filter by date to") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "Filter by classroom") @RequestParam(required = false) Long classroomId,
            @Parameter(description = "Filter by teacher") @RequestParam(required = false) Long teacherId) {

        try {
            PageRequest pageRequest = new PageRequest(page, size, sort, direction);
            List<ScheduleResponse> schedules = scheduleMapper.findAllWithFilters(pageRequest, search, dateFrom, dateTo, classroomId, teacherId)
                    .stream()
                    .map(ScheduleResponse::fromEntity)
                    .toList();

            long total = scheduleMapper.countWithFilters(search, dateFrom, dateTo, classroomId, teacherId);
            PaginatedResponse<ScheduleResponse> response = PaginatedResponse.of(schedules, page, size, total);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve schedules", e);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get schedule by ID", description = "Retrieve a specific schedule by its ID")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved schedule",
                    content = @Content(schema = @Schema(implementation = ScheduleResponse.class))),
            @SwaggerApiResponse(responseCode = "404", description = "Schedule not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getScheduleById(
            @Parameter(description = "Schedule ID") @PathVariable Long id) {

        try {
            com.school.scheduling.domain.Schedule schedule = scheduleMapper.findById(id);
            if (schedule == null) {
                throw new ResourceNotFoundException("Schedule not found with id: " + id);
            }

            ScheduleResponse response = ScheduleResponse.fromEntity(schedule);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve schedule with id: " + id, e);
        }
    }

    @GetMapping("/by-date/{date}")
    @Operation(summary = "Get schedules by date", description = "Retrieve all schedules for a specific date")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved schedules",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByDate(
            @Parameter(description = "Schedule date") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            List<ScheduleResponse> schedules = scheduleMapper.findByDate(date)
                    .stream()
                    .map(ScheduleResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(schedules));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve schedules for date: " + date, e);
        }
    }

    @GetMapping("/by-classroom/{classroomId}")
    @Operation(summary = "Get schedules by classroom", description = "Retrieve all schedules for a specific classroom")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved schedules",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByClassroom(
            @Parameter(description = "Classroom ID") @PathVariable Long classroomId) {

        try {
            List<ScheduleResponse> schedules = scheduleMapper.findByClassroomId(classroomId)
                    .stream()
                    .map(ScheduleResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(schedules));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve schedules for classroom: " + classroomId, e);
        }
    }

    @GetMapping("/by-teacher/{teacherId}")
    @Operation(summary = "Get schedules by teacher", description = "Retrieve all schedules for a specific teacher")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved schedules",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByTeacher(
            @Parameter(description = "Teacher ID") @PathVariable Long teacherId) {

        try {
            List<ScheduleResponse> schedules = scheduleMapper.findByTeacherId(teacherId)
                    .stream()
                    .map(ScheduleResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(schedules));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve schedules for teacher: " + teacherId, e);
        }
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming schedules", description = "Retrieve all upcoming schedules")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved upcoming schedules",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getUpcomingSchedules(
            @Parameter(description = "Number of days to look ahead") @RequestParam(defaultValue = "7") int daysAhead) {

        try {
            List<ScheduleResponse> schedules = scheduleMapper.findUpcomingSchedules(daysAhead)
                    .stream()
                    .map(ScheduleResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(schedules));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve upcoming schedules", e);
        }
    }

    @PostMapping
    @Operation(summary = "Create a new schedule", description = "Create a new schedule record")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "201", description = "Schedule created successfully",
                    content = @Content(schema = @Schema(implementation = ScheduleResponse.class))),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid input"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
            @Valid @RequestBody ScheduleRequest scheduleRequest) {

        try {
            ValidationResult validationResult = scheduleValidator.validateForCreation(scheduleRequest);
            if (!validationResult.isValid()) {
                throw new com.school.scheduling.validation.ValidationException("Schedule validation failed", validationResult);
            }

            // Check for conflicts
            if (scheduleMapper.hasScheduleConflict(scheduleRequest.getClassroomId(), scheduleRequest.getTimeSlotId(), scheduleRequest.getScheduleDate())) {
                throw new BadRequestException("Schedule conflicts with existing schedule for the same classroom and time slot");
            }

            if (scheduleMapper.hasTeacherConflict(scheduleRequest.getTeacherIdFromCourseOffering(), scheduleRequest.getTimeSlotId(), scheduleRequest.getScheduleDate())) {
                throw new BadRequestException("Teacher conflict detected - teacher is already scheduled at this time");
            }

            com.school.scheduling.domain.Schedule schedule = new com.school.scheduling.domain.Schedule();
            schedule.setCourseOffering(new com.school.scheduling.domain.CourseOffering(scheduleRequest.getCourseOfferingId()));
            schedule.setClassroom(new com.school.scheduling.domain.Classroom(scheduleRequest.getClassroomId()));
            schedule.setTimeSlot(new com.school.scheduling.domain.TimeSlot(scheduleRequest.getTimeSlotId()));
            schedule.setScheduleDate(scheduleRequest.getScheduleDate());
            schedule.setIsRecurring(scheduleRequest.getIsRecurring());
            schedule.setRecurrencePattern(scheduleRequest.getRecurrencePattern());
            schedule.setNotes(scheduleRequest.getNotes());

            scheduleMapper.insert(schedule);

            ScheduleResponse response = ScheduleResponse.fromEntity(scheduleMapper.findById(schedule.getId()));
            return new ResponseEntity<>(ApiResponse.created(response), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create schedule", e);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a schedule", description = "Update an existing schedule record")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Schedule updated successfully",
                    content = @Content(schema = @Schema(implementation = ScheduleResponse.class))),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid input"),
            @SwaggerApiResponse(responseCode = "404", description = "Schedule not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @Parameter(description = "Schedule ID") @PathVariable Long id,
            @Valid @RequestBody ScheduleRequest scheduleRequest) {

        try {
            com.school.scheduling.domain.Schedule existingSchedule = scheduleMapper.findById(id);
            if (existingSchedule == null) {
                throw new ResourceNotFoundException("Schedule not found with id: " + id);
            }

            ValidationResult validationResult = scheduleValidator.validateForUpdate(existingSchedule, scheduleRequest);
            if (!validationResult.isValid()) {
                throw new com.school.scheduling.validation.ValidationException("Schedule validation failed", validationResult);
            }

            // Check for conflicts (excluding current schedule)
            if (scheduleMapper.hasScheduleConflictExcludingId(id, scheduleRequest.getClassroomId(), scheduleRequest.getTimeSlotId(), scheduleRequest.getScheduleDate())) {
                throw new BadRequestException("Schedule conflicts with existing schedule for the same classroom and time slot");
            }

            if (scheduleMapper.hasTeacherConflictExcludingId(id, scheduleRequest.getTeacherIdFromCourseOffering(), scheduleRequest.getTimeSlotId(), scheduleRequest.getScheduleDate())) {
                throw new BadRequestException("Teacher conflict detected - teacher is already scheduled at this time");
            }

            // Update fields
            existingSchedule.setCourseOffering(new com.school.scheduling.domain.CourseOffering(scheduleRequest.getCourseOfferingId()));
            existingSchedule.setClassroom(new com.school.scheduling.domain.Classroom(scheduleRequest.getClassroomId()));
            existingSchedule.setTimeSlot(new com.school.scheduling.domain.TimeSlot(scheduleRequest.getTimeSlotId()));
            existingSchedule.setScheduleDate(scheduleRequest.getScheduleDate());
            existingSchedule.setIsRecurring(scheduleRequest.getIsRecurring());
            existingSchedule.setRecurrencePattern(scheduleRequest.getRecurrencePattern());
            existingSchedule.setNotes(scheduleRequest.getNotes());

            scheduleMapper.update(existingSchedule);

            ScheduleResponse response = ScheduleResponse.fromEntity(scheduleMapper.findById(id));
            return ResponseEntity.ok(ApiResponse.updated(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update schedule with id: " + id, e);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a schedule", description = "Delete a schedule record")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Schedule deleted successfully"),
            @SwaggerApiResponse(responseCode = "404", description = "Schedule not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @Parameter(description = "Schedule ID") @PathVariable Long id) {

        try {
            com.school.scheduling.domain.Schedule schedule = scheduleMapper.findById(id);
            if (schedule == null) {
                throw new ResourceNotFoundException("Schedule not found with id: " + id);
            }

            // Check if schedule is in progress
            if (schedule.isCurrentlyInProgress()) {
                throw new BadRequestException("Cannot delete schedule that is currently in progress");
            }

            scheduleMapper.deleteById(id);
            return ResponseEntity.ok(ApiResponse.deleted("Schedule deleted successfully"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete schedule with id: " + id, e);
        }
    }

    @GetMapping("/conflicts/check")
    @Operation(summary = "Check for schedule conflicts", description = "Check if a proposed schedule has conflicts")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Conflict check completed"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Boolean>> checkScheduleConflicts(
            @Parameter(description = "Classroom ID") @RequestParam Long classroomId,
            @Parameter(description = "Time slot ID") @RequestParam Long timeSlotId,
            @Parameter(description = "Schedule date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Exclude schedule ID (for updates)") @RequestParam(required = false) Long excludeScheduleId) {

        try {
            boolean hasConflict;
            if (excludeScheduleId != null) {
                hasConflict = scheduleMapper.hasScheduleConflictExcludingId(excludeScheduleId, classroomId, timeSlotId, date);
            } else {
                hasConflict = scheduleMapper.hasScheduleConflict(classroomId, timeSlotId, date);
            }

            return ResponseEntity.ok(ApiResponse.success(hasConflict));
        } catch (Exception e) {
            throw new RuntimeException("Failed to check schedule conflicts", e);
        }
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's schedules", description = "Retrieve all schedules for today")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved today's schedules",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getTodaysSchedules() {

        try {
            List<ScheduleResponse> schedules = scheduleMapper.findTodaysSchedules()
                    .stream()
                    .map(ScheduleResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(schedules));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve today's schedules", e);
        }
    }
}