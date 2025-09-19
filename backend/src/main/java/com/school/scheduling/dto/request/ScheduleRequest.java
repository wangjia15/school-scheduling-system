package com.school.scheduling.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {
    @NotNull(message = "Course offering ID is required")
    private Long courseOfferingId;

    @NotNull(message = "Classroom ID is required")
    private Long classroomId;

    @NotNull(message = "Time slot ID is required")
    private Long timeSlotId;

    @NotNull(message = "Schedule date is required")
    private LocalDate scheduleDate;

    private Boolean isRecurring = true;

    @Size(max = 50, message = "Recurrence pattern must be less than 50 characters")
    private String recurrencePattern;

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes;
}