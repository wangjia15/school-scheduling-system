package com.school.scheduling.dto.response;

import com.school.scheduling.domain.TeacherAvailability;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAvailabilityResponse {

    private Long id;
    private Long teacherId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private TeacherAvailabilityType availabilityType;
    private Integer maxClasses;
    private java.math.BigDecimal breakDuration;
    private Boolean requiresBreakBetweenClasses;
    private Boolean isRecurring;
    private String notes;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public enum TeacherAvailabilityType {
        PREFERRED, AVAILABLE, UNAVAILABLE, RESTRICTED
    }

    public static TeacherAvailabilityResponse fromEntity(TeacherAvailability availability) {
        if (availability == null) {
            return null;
        }

        return new TeacherAvailabilityResponse(
            availability.getId(),
            availability.getTeacher() != null ? availability.getTeacher().getId() : null,
            availability.getDayOfWeek(),
            availability.getStartTime(),
            availability.getEndTime(),
            TeacherAvailabilityType.valueOf(availability.getAvailabilityType().name()),
            availability.getMaxClasses(),
            availability.getBreakDuration(),
            availability.getRequiresBreakBetweenClasses(),
            availability.getIsRecurring(),
            availability.getNotes(),
            availability.getCreatedAt(),
            availability.getUpdatedAt()
        );
    }

    public String getDayOfWeekDisplayName() {
        if (dayOfWeek == null) {
            return "";
        }
        return dayOfWeek.toString().charAt(0) + dayOfWeek.toString().substring(1).toLowerCase();
    }

    public String getTimeRangeDisplay() {
        if (startTime == null || endTime == null) {
            return "";
        }
        return startTime.toString() + " - " + endTime.toString();
    }

    public String getAvailabilityTypeDisplay() {
        if (availabilityType == null) {
            return "";
        }
        return availabilityType.toString().charAt(0) + availabilityType.toString().substring(1).toLowerCase();
    }

    public boolean isAvailable() {
        return availabilityType == TeacherAvailabilityType.PREFERRED ||
               availabilityType == TeacherAvailabilityType.AVAILABLE;
    }

    public String getFormattedBreakDuration() {
        if (breakDuration == null || breakDuration.compareTo(java.math.BigDecimal.ZERO) == 0) {
            return "No break required";
        }
        return breakDuration + " hours";
    }
}