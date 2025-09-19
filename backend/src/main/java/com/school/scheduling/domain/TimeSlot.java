package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "time_slots", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"day_of_week", "start_time", "end_time"})
})
public class TimeSlot extends BaseEntity {

    @NotNull(message = "Day of week is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull(message = "Slot type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false, length = 20)
    private SlotType slotType;

    @NotNull(message = "Active status is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "timeSlot", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules = new ArrayList<>();

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    public enum SlotType {
        MORNING, AFTERNOON, EVENING
    }

    public int getDurationInMinutes() {
        if (startTime == null || endTime == null) return 0;
        return (int) ChronoUnit.MINUTES.between(startTime, endTime);
    }

    public double getDurationInHours() {
        return getDurationInMinutes() / 60.0;
    }

    public boolean isValidTimeRange() {
        if (startTime == null || endTime == null) return false;
        return endTime.isAfter(startTime) && getDurationInMinutes() > 0;
    }

    public boolean isStandardClassDuration() {
        int minutes = getDurationInMinutes();
        return minutes == 50 || minutes == 60 || minutes == 75 || minutes == 90 || minutes == 120;
    }

    public boolean isWeekday() {
        return dayOfWeek == DayOfWeek.MONDAY ||
               dayOfWeek == DayOfWeek.TUESDAY ||
               dayOfWeek == DayOfWeek.WEDNESDAY ||
               dayOfWeek == DayOfWeek.THURSDAY ||
               dayOfWeek == DayOfWeek.FRIDAY;
    }

    public boolean isWeekend() {
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public boolean isMonday() {
        return dayOfWeek == DayOfWeek.MONDAY;
    }

    public boolean isTuesday() {
        return dayOfWeek == DayOfWeek.TUESDAY;
    }

    public boolean isWednesday() {
        return dayOfWeek == DayOfWeek.WEDNESDAY;
    }

    public boolean isThursday() {
        return dayOfWeek == DayOfWeek.THURSDAY;
    }

    public boolean isFriday() {
        return dayOfWeek == DayOfWeek.FRIDAY;
    }

    public boolean isSaturday() {
        return dayOfWeek == DayOfWeek.SATURDAY;
    }

    public boolean isSunday() {
        return dayOfWeek == DayOfWeek.SUNDAY;
    }

    public boolean isMorningSlot() {
        return slotType == SlotType.MORNING;
    }

    public boolean isAfternoonSlot() {
        return slotType == SlotType.AFTERNOON;
    }

    public boolean isEveningSlot() {
        return slotType == SlotType.EVENING;
    }

    public boolean isEarlyMorning() {
        if (startTime == null) return false;
        return !startTime.isBefore(LocalTime.of(8, 0)) && startTime.isBefore(LocalTime.of(10, 0));
    }

    public boolean isLateMorning() {
        if (startTime == null) return false;
        return !startTime.isBefore(LocalTime.of(10, 0)) && startTime.isBefore(LocalTime.of(12, 0));
    }

    public boolean isEarlyAfternoon() {
        if (startTime == null) return false;
        return !startTime.isBefore(LocalTime.of(12, 0)) && startTime.isBefore(LocalTime.of(14, 0));
    }

    public boolean isLateAfternoon() {
        if (startTime == null) return false;
        return !startTime.isBefore(LocalTime.of(14, 0)) && startTime.isBefore(LocalTime.of(17, 0));
    }

    public boolean isEveningTime() {
        if (startTime == null) return false;
        return !startTime.isBefore(LocalTime.of(17, 0));
    }

    public String getTimeRange() {
        if (startTime == null || endTime == null) return "Unknown";
        return startTime + " - " + endTime;
    }

    public String getDayName() {
        return dayOfWeek != null ? dayOfWeek.toString() : "Unknown";
    }

    public String getShortDayName() {
        if (dayOfWeek == null) return "UNK";
        return dayOfWeek.toString().substring(0, 3);
    }

    public String getSlotDescription() {
        return getDayName() + " " + getTimeRange() + " (" + slotType + ")";
    }

    public String getShortDescription() {
        return getShortDayName() + " " + startTime + "-" + endTime;
    }

    public boolean overlapsWith(TimeSlot other) {
        if (other == null || this.equals(other)) return false;
        if (this.dayOfWeek != other.dayOfWeek) return false;

        return !(this.endTime.isBefore(other.startTime) || this.startTime.isAfter(other.endTime));
    }

    public boolean conflictsWith(TimeSlot other) {
        return overlapsWith(other);
    }

    public boolean isBackToBackWith(TimeSlot other) {
        if (other == null || this.dayOfWeek != other.dayOfWeek) return false;
        return this.endTime.equals(other.startTime) || this.startTime.equals(other.endTime);
    }

    public boolean isImmediatelyBefore(TimeSlot other) {
        if (other == null || this.dayOfWeek != other.dayOfWeek) return false;
        return this.endTime.equals(other.startTime);
    }

    public boolean isImmediatelyAfter(TimeSlot other) {
        if (other == null || this.dayOfWeek != other.dayOfWeek) return false;
        return this.startTime.equals(other.endTime);
    }

    public boolean containsTime(LocalTime time) {
        if (time == null) return false;
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public boolean startsBefore(LocalTime time) {
        if (time == null) return false;
        return startTime.isBefore(time);
    }

    public boolean endsAfter(LocalTime time) {
        if (time == null) return false;
        return endTime.isAfter(time);
    }

    public boolean isDuringBusinessHours() {
        if (startTime == null || endTime == null) return false;
        LocalTime businessStart = LocalTime.of(8, 0);
        LocalTime businessEnd = LocalTime.of(18, 0);
        return !startTime.isBefore(businessStart) && !endTime.isAfter(businessEnd);
    }

    public boolean isExtendedHours() {
        return !isDuringBusinessHours();
    }

    public String getTimeCategory() {
        if (isMorningSlot()) return "MORNING";
        if (isAfternoonSlot()) return "AFTERNOON";
        if (isEveningSlot()) return "EVENING";
        return "UNKNOWN";
    }

    public String getDurationCategory() {
        int minutes = getDurationInMinutes();
        if (minutes <= 60) return "SHORT";
        if (minutes <= 90) return "STANDARD";
        if (minutes <= 120) return "LONG";
        return "EXTENDED";
    }

    public boolean isStandardLectureTime() {
        return isStandardClassDuration() && isDuringBusinessHours();
    }

    public boolean isPrimeTime() {
        if (startTime == null) return false;
        return !startTime.isBefore(LocalTime.of(9, 0)) && startTime.isBefore(LocalTime.of(15, 0));
    }

    public boolean isOffPeakTime() {
        return !isPrimeTime();
    }

    public void addSchedule(Schedule schedule) {
        if (!schedules.contains(schedule)) {
            schedules.add(schedule);
            schedule.setTimeSlot(this);
        }
    }

    public void removeSchedule(Schedule schedule) {
        schedules.remove(schedule);
        schedule.setTimeSlot(null);
    }

    public List<Schedule> getActiveSchedules() {
        return schedules.stream()
                .filter(Schedule::isActive)
                .toList();
    }

    public int getScheduleCount() {
        return schedules.size();
    }

    public int getActiveScheduleCount() {
        return getActiveSchedules().size();
    }

    public boolean isAvailableForScheduling() {
        return isActive && isActive();
    }

    public boolean isOverbooked() {
        return getActiveScheduleCount() > 1; // Typically only one class per time slot
    }

    public boolean hasConflicts() {
        return getActiveScheduleCount() > 1;
    }

    public String getUtilizationStatus() {
        int count = getActiveScheduleCount();
        if (count == 0) return "AVAILABLE";
        if (count == 1) return "SCHEDULED";
        return "CONFLICT";
    }

    public boolean isValidTimeSlot() {
        return isValidTimeRange() &&
               dayOfWeek != null &&
               slotType != null &&
               isStandardClassDuration();
    }

    public String getValidationMessage() {
        if (!isValidTimeRange()) {
            return "Invalid time range: end time must be after start time";
        }
        if (!isStandardClassDuration()) {
            return "Non-standard class duration: " + getDurationInMinutes() + " minutes";
        }
        return "Valid time slot";
    }

    public boolean isSuitableForCourseType(Course.CourseLevel level) {
        if (level == null) return true;

        switch (level) {
            case UNDERGRADUATE:
                return isStandardLectureTime() || isAfternoonSlot();
            case GRADUATE:
                return isAfternoonSlot() || isEveningSlot();
            case PHD:
                return isEveningSlot() || isExtendedHours();
            default:
                return true;
        }
    }

    public boolean isPreferredTime() {
        return isPrimeTime() && isWeekday();
    }

    public boolean isSecondaryTime() {
        return !isPreferredTime() && isDuringBusinessHours();
    }

    public boolean isLessPreferredTime() {
        return isExtendedHours() || isWeekend();
    }

    public int getPreferenceScore() {
        if (isPreferredTime()) return 3;
        if (isSecondaryTime()) return 2;
        if (isLessPreferredTime()) return 1;
        return 0;
    }

    public String getPreferenceLevel() {
        int score = getPreferenceScore();
        switch (score) {
            case 3: return "HIGH";
            case 2: return "MEDIUM";
            case 1: return "LOW";
            default: return "NONE";
        }
    }

    public boolean isConsecutiveWith(TimeSlot other) {
        if (other == null || this.dayOfWeek != other.dayOfWeek) return false;
        return isBackToBackWith(other);
    }

    public boolean canBeCombinedWith(TimeSlot other) {
        return isConsecutiveWith(other) && this.getDurationInMinutes() + other.getDurationInMinutes() <= 180;
    }

    public TimeSlot combineWith(TimeSlot other) {
        if (!canBeCombinedWith(other)) return null;

        TimeSlot combined = new TimeSlot();
        combined.setDayOfWeek(this.dayOfWeek);
        combined.setStartTime(this.startTime.isBefore(other.startTime) ? this.startTime : other.startTime);
        combined.setEndTime(this.endTime.isAfter(other.endTime) ? this.endTime : other.endTime);
        combined.setSlotType(this.slotType); // Preserve the original slot type
        combined.setIsActive(this.isActive && other.isActive);

        return combined;
    }

    public boolean isSplitAvailable() {
        return getDurationInMinutes() >= 120;
    }

    public TimeSlot[] split() {
        if (!isSplitAvailable()) return new TimeSlot[0];

        int halfDuration = getDurationInMinutes() / 2;
        LocalTime midTime = startTime.plusMinutes(halfDuration);

        TimeSlot firstHalf = new TimeSlot();
        firstHalf.setDayOfWeek(this.dayOfWeek);
        firstHalf.setStartTime(this.startTime);
        firstHalf.setEndTime(midTime);
        firstHalf.setSlotType(this.slotType);
        firstHalf.setIsActive(this.isActive);

        TimeSlot secondHalf = new TimeSlot();
        secondHalf.setDayOfWeek(this.dayOfWeek);
        secondHalf.setStartTime(midTime);
        secondHalf.setEndTime(this.endTime);
        secondHalf.setSlotType(this.slotType);
        secondHalf.setIsActive(this.isActive);

        return new TimeSlot[]{firstHalf, secondHalf};
    }

    public String getDetailedInformation() {
        return String.format("%s %s-%s (%s, %d min, %s)",
                getDayName(),
                startTime,
                endTime,
                slotType,
                getDurationInMinutes(),
                getUtilizationStatus());
    }

    public boolean isEquivalentTo(TimeSlot other) {
        if (other == null) return false;
        return this.dayOfWeek == other.dayOfWeek &&
               this.startTime.equals(other.startTime) &&
               this.endTime.equals(other.endTime);
    }

    public boolean hasSameScheduleAs(TimeSlot other) {
        return isEquivalentTo(other);
    }
}