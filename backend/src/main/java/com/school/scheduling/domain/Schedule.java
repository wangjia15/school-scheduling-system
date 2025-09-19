package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "schedules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"classroom_id", "time_slot_id", "schedule_date"})
})
public class Schedule extends BaseEntity {

    @NotNull(message = "Course offering is required")
    @ManyToOne
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering courseOffering;

    @NotNull(message = "Classroom is required")
    @ManyToOne
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @NotNull(message = "Time slot is required")
    @ManyToOne
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @NotNull(message = "Schedule date is required")
    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @NotNull(message = "Recurring flag is required")
    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = true;

    @Size(max = 50, message = "Recurrence pattern must be less than 50 characters")
    @Column(name = "recurrence_pattern", length = 50)
    private String recurrencePattern;

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public boolean isUpcoming() {
        if (scheduleDate == null) return false;
        return !scheduleDate.isBefore(LocalDate.now());
    }

    public boolean isPast() {
        if (scheduleDate == null) return false;
        return scheduleDate.isBefore(LocalDate.now());
    }

    public boolean isToday() {
        if (scheduleDate == null) return false;
        return scheduleDate.equals(LocalDate.now());
    }

    public boolean isTomorrow() {
        if (scheduleDate == null) return false;
        return scheduleDate.equals(LocalDate.now().plusDays(1));
    }

    public boolean isThisWeek() {
        if (scheduleDate == null) return false;
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        return !scheduleDate.isBefore(weekStart) && !scheduleDate.isAfter(weekEnd);
    }

    public boolean isNextWeek() {
        if (scheduleDate == null) return false;
        LocalDate today = LocalDate.now();
        LocalDate nextWeekStart = today.plusDays(7 - today.getDayOfWeek().getValue() + 1);
        LocalDate nextWeekEnd = nextWeekStart.plusDays(6);
        return !scheduleDate.isBefore(nextWeekStart) && !scheduleDate.isAfter(nextWeekEnd);
    }

    public boolean conflictsWith(Schedule other) {
        if (other == null || this.equals(other)) return false;

        return this.classroom.equals(other.classroom) &&
               this.timeSlot.conflictsWith(other.timeSlot) &&
               this.scheduleDate.equals(other.scheduleDate);
    }

    public boolean hasTeacherConflict(Schedule other) {
        if (other == null || this.equals(other)) return false;

        Teacher thisTeacher = this.courseOffering != null ? this.courseOffering.getTeacher() : null;
        Teacher otherTeacher = other.courseOffering != null ? other.courseOffering.getTeacher() : null;

        return thisTeacher != null && thisTeacher.equals(otherTeacher) &&
               this.timeSlot.conflictsWith(other.timeSlot) &&
               this.scheduleDate.equals(other.scheduleDate);
    }

    public boolean hasClassroomConflict(Schedule other) {
        return conflictsWith(other);
    }

    public boolean isRecurringWeekly() {
        return isRecurring && "WEEKLY".equals(recurrencePattern);
    }

    public boolean isRecurringBiweekly() {
        return isRecurring && "BIWEEKLY".equals(recurrencePattern);
    }

    public boolean isSingleInstance() {
        return !isRecurring;
    }

    public String getScheduleSummary() {
        if (courseOffering == null || classroom == null || timeSlot == null) {
            return "Incomplete schedule";
        }

        return String.format("%s - %s (%s %s)",
                courseOffering.getCourseCode(),
                classroom.getRoomCode(),
                timeSlot.getShortDayName(),
                timeSlot.getTimeRange());
    }

    public String getFullDescription() {
        StringBuilder description = new StringBuilder();

        if (courseOffering != null) {
            description.append(courseOffering.getFullDisplayName());
        }

        if (classroom != null) {
            description.append(" in ").append(classroom.getFullDisplayName());
        }

        if (timeSlot != null) {
            description.append(" on ").append(timeSlot.getSlotDescription());
        }

        if (scheduleDate != null) {
            description.append(" starting ").append(scheduleDate);
        }

        if (isRecurring) {
            description.append(" (").append(recurrencePattern != null ? recurrencePattern : "Recurring").append(")");
        }

        return description.toString();
    }

    public boolean isValidSchedule() {
        return courseOffering != null &&
               classroom != null &&
               timeSlot != null &&
               scheduleDate != null &&
               isActive();
    }

    public boolean isReadyForActivation() {
        return isValidSchedule() && isUpcoming();
    }

    public String getStatus() {
        if (!isActive()) return "INACTIVE";
        if (isPast()) return "COMPLETED";
        if (isToday()) return "TODAY";
        if (isThisWeek()) return "THIS_WEEK";
        if (isUpcoming()) return "UPCOMING";
        return "UNKNOWN";
    }

    public long getDaysUntilSchedule() {
        if (scheduleDate == null) return 0;
        LocalDate today = LocalDate.now();
        if (scheduleDate.isBefore(today)) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(today, scheduleDate);
    }

    public boolean isWithinNextWeek() {
        long days = getDaysUntilSchedule();
        return days >= 0 && days <= 7;
    }

    public boolean isUrgent() {
        long days = getDaysUntilSchedule();
        return days >= 0 && days <= 3;
    }

    public boolean needsPreparation() {
        return isWithinNextWeek() && courseOffering != null && courseOffering.hasLabComponent();
    }

    public boolean requiresSpecialEquipment() {
        return classroom != null && classroom.getEquipmentList().size() > 2;
    }

    public boolean isLargeClass() {
        return classroom != null && classroom.isLargeClassroom();
    }

    public boolean isSmallClass() {
        return classroom != null && classroom.isSmallClassroom();
    }

    public String getClassSizeCategory() {
        if (courseOffering == null) return "UNKNOWN";
        int enrollment = courseOffering.getCurrentEnrollment();
        if (enrollment <= 15) return "SMALL";
        if (enrollment <= 30) return "MEDIUM";
        if (enrollment <= 60) return "LARGE";
        return "VERY_LARGE";
    }

    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }

    public String getShortNotes() {
        if (!hasNotes()) return "";
        String noteText = notes.trim();
        return noteText.length() > 50 ? noteText.substring(0, 47) + "..." : noteText;
    }

    public LocalDateTime getScheduleDateTime() {
        if (scheduleDate == null || timeSlot == null || timeSlot.getStartTime() == null) {
            return null;
        }
        return scheduleDate.atTime(timeSlot.getStartTime());
    }

    public LocalDateTime getScheduleEndDateTime() {
        if (scheduleDate == null || timeSlot == null || timeSlot.getEndTime() == null) {
            return null;
        }
        return scheduleDate.atTime(timeSlot.getEndTime());
    }

    public boolean isCurrentlyInProgress() {
        if (scheduleDate == null || timeSlot == null) return false;

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (!scheduleDate.equals(today)) return false;

        return !now.isBefore(timeSlot.getStartTime()) && !now.isAfter(timeSlot.getEndTime());
    }

    public boolean isAboutToStart() {
        if (scheduleDate == null || timeSlot == null) return false;

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (!scheduleDate.equals(today)) return false;

        LocalTime startTime = timeSlot.getStartTime();
        return now.isBefore(startTime) &&
               !now.isBefore(startTime.minusMinutes(15));
    }

    public boolean justEnded() {
        if (scheduleDate == null || timeSlot == null) return false;

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (!scheduleDate.equals(today)) return false;

        LocalTime endTime = timeSlot.getEndTime();
        return now.isAfter(endTime) &&
               !now.isAfter(endTime.plusMinutes(15));
    }

    public String getTimeUntilSchedule() {
        long days = getDaysUntilSchedule();
        if (days == 0) {
            if (isCurrentlyInProgress()) return "In progress";
            if (isAboutToStart()) return "Starting soon";
            if (justEnded()) return "Just ended";
            return "Today";
        } else if (days == 1) {
            return "Tomorrow";
        } else if (days <= 7) {
            return "In " + days + " days";
        } else {
            return "In " + days + " days (" + (days / 7) + " weeks)";
        }
    }

    public boolean isValidForCurrentSemester() {
        if (courseOffering == null || courseOffering.getSemester() == null) return false;
        Semester semester = courseOffering.getSemester();
        return semester.containsDate(scheduleDate);
    }

    public boolean isOutsideSemesterDates() {
        return !isValidForCurrentSemester();
    }

    public String getScheduleType() {
        if (isSingleInstance()) return "SINGLE";
        if (isRecurringWeekly()) return "WEEKLY";
        if (isRecurringBiweekly()) return "BIWEEKLY";
        return "CUSTOM";
    }

    public boolean canBeModified() {
        return isActive() && isUpcoming() && !isCurrentlyInProgress();
    }

    public boolean canBeCancelled() {
        return canBeModified();
    }

    public boolean requiresAttendanceTracking() {
        return courseOffering != null && courseOffering.getCourse() != null;
    }

    public boolean isExamSchedule() {
        return hasNotes() && notes.toUpperCase().contains("EXAM");
    }

    public boolean isSpecialSession() {
        return hasNotes() && (notes.toUpperCase().contains("MAKEUP") ||
                             notes.toUpperCase().contains("SPECIAL"));
    }

    public String getDetailedScheduleInfo() {
        return String.format("%s | %s | %s | %s | %s",
                getScheduleSummary(),
                getStatus(),
                getTimeUntilSchedule(),
                getClassSizeCategory(),
                getScheduleType());
    }
}