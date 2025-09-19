package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "teacher_availability", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"teacher_id", "day_of_week", "start_time", "end_time"})
})
public class TeacherAvailability extends BaseEntity {

    @NotNull(message = "Teacher is required")
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @NotNull(message = "Day of week is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_type", nullable = false, length = 20)
    private AvailabilityType availabilityType = AvailabilityType.PREFERRED;

    @Column(name = "max_classes", nullable = false)
    private Integer maxClasses = 1;

    @Column(name = "break_duration", precision = 4, scale = 1)
    private BigDecimal breakDuration = BigDecimal.ZERO;

    @Column(name = "requires_break_between_classes")
    private Boolean requiresBreakBetweenClasses = false;

    @Column(name = "is_recurring")
    private Boolean isRecurring = true;

    @Column(name = "notes", length = 500)
    private String notes;

    public enum AvailabilityType {
        PREFERRED, AVAILABLE, UNAVAILABLE, RESTRICTED
    }

    public boolean isOverlapping(LocalTime otherStart, LocalTime otherEnd) {
        return !otherEnd.isBefore(startTime) && !otherStart.isAfter(endTime);
    }

    public boolean isValidTimeRange() {
        return endTime.isAfter(startTime);
    }

    public boolean isWithinWorkingHours(LocalTime time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public boolean canScheduleClass(LocalTime classStart, LocalTime classEnd) {
        if (availabilityType == AvailabilityType.UNAVAILABLE) {
            return false;
        }
        if (availabilityType == AvailabilityType.RESTRICTED) {
            return false;
        }
        return isOverlapping(classStart, classEnd) && isValidTimeRange();
    }

    public boolean hasSufficientBreakTime(LocalTime previousEnd, LocalTime currentStart, BigDecimal requiredBreak) {
        if (!requiresBreakBetweenClasses) {
            return true;
        }
        BigDecimal actualBreak = BigDecimal.valueOf(currentStart.until(previousEnd).toMinutes())
                .divide(BigDecimal.valueOf(60));
        return actualBreak.compareTo(requiredBreak) >= 0;
    }

    public boolean canHandleMoreClasses(int currentClasses) {
        return currentClasses < maxClasses;
    }

    public boolean isPreferredTimeSlot() {
        return availabilityType == AvailabilityType.PREFERRED;
    }

    public boolean isAvailableTimeSlot() {
        return availabilityType == AvailabilityType.PREFERRED || availabilityType == AvailabilityType.AVAILABLE;
    }

    public int getDurationInMinutes() {
        return (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }

    public boolean conflictsWith(TeacherAvailability other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        return this.isOverlapping(other.startTime, other.endTime);
    }
}