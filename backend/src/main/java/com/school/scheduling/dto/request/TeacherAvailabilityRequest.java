package com.school.scheduling.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class TeacherAvailabilityRequest {

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Availability type is required")
    private TeacherAvailabilityType availabilityType;

    @Min(value = 1, message = "Max classes must be at least 1")
    @Max(value = 10, message = "Max classes cannot exceed 10")
    private Integer maxClasses = 1;

    @DecimalMin(value = "0.0", message = "Break duration cannot be negative")
    private BigDecimal breakDuration = BigDecimal.ZERO;

    private Boolean requiresBreakBetweenClasses = false;

    private Boolean isRecurring = true;

    @Size(max = 500, message = "Notes must be less than 500 characters")
    private String notes;

    public enum TeacherAvailabilityType {
        PREFERRED, AVAILABLE, UNAVAILABLE, RESTRICTED
    }
}