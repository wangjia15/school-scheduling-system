package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "semesters", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"academic_year", "semester_type"})
})
public class Semester extends BaseEntity {

    @NotBlank(message = "Semester name is required")
    @Size(max = 50, message = "Semester name must be less than 50 characters")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotBlank(message = "Academic year is required")
    @Size(max = 9, message = "Academic year must be less than 9 characters")
    @Column(name = "academic_year", nullable = false, length = 9)
    private String academicYear;

    @NotNull(message = "Semester type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "semester_type", nullable = false, length = 20)
    private SemesterType semesterType;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Current semester flag is required")
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    @NotNull(message = "Registration deadline is required")
    @Column(name = "registration_deadline", nullable = false)
    private LocalDate registrationDeadline;

    @OneToMany(mappedBy = "semester", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<CourseOffering> courseOfferings = new ArrayList<>();

    public enum SemesterType {
        FALL, SPRING, SUMMER, WINTER
    }

    public String getSemesterIdentifier() {
        return academicYear + " " + semesterType.toString();
    }

    public String getFullDisplayName() {
        return name + " (" + academicYear + ")";
    }

    public String getShortDisplayName() {
        return semesterType.toString().charAt(0) + semesterType.toString().substring(1).toLowerCase() + " " + academicYear.substring(5);
    }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public boolean isUpcoming() {
        LocalDate today = LocalDate.now();
        return today.isBefore(startDate);
    }

    public boolean isCompleted() {
        LocalDate today = LocalDate.now();
        return today.isAfter(endDate);
    }

    public boolean isRegistrationOpen() {
        LocalDate today = LocalDate.now();
        return !today.isAfter(registrationDeadline) && (isUpcoming() || isActive());
    }

    public boolean isRegistrationClosed() {
        return !isRegistrationOpen();
    }

    public boolean isPastRegistrationDeadline() {
        LocalDate today = LocalDate.now();
        return today.isAfter(registrationDeadline);
    }

    public long getDurationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public long getDurationInWeeks() {
        return ChronoUnit.WEEKS.between(startDate, endDate);
    }

    public long getDaysUntilStart() {
        LocalDate today = LocalDate.now();
        if (isUpcoming()) {
            return ChronoUnit.DAYS.between(today, startDate);
        }
        return 0;
    }

    public long getDaysUntilEnd() {
        LocalDate today = LocalDate.now();
        if (!isCompleted()) {
            return ChronoUnit.DAYS.between(today, endDate);
        }
        return 0;
    }

    public long getDaysUntilRegistrationDeadline() {
        LocalDate today = LocalDate.now();
        if (isRegistrationOpen()) {
            return ChronoUnit.DAYS.between(today, registrationDeadline);
        }
        return 0;
    }

    public double getProgressPercentage() {
        LocalDate today = LocalDate.now();
        if (isUpcoming()) return 0.0;
        if (isCompleted()) return 100.0;

        long totalDays = getDurationInDays();
        long daysElapsed = ChronoUnit.DAYS.between(startDate, today);

        if (totalDays == 0) return 0.0;
        return (double) daysElapsed / totalDays * 100.0;
    }

    public boolean isFirstSemesterOfYear() {
        return semesterType == SemesterType.FALL || semesterType == SemesterType.WINTER;
    }

    public boolean isSecondSemesterOfYear() {
        return semesterType == SemesterType.SPRING || semesterType == SemesterType.SUMMER;
    }

    public boolean isFallSemester() {
        return semesterType == SemesterType.FALL;
    }

    public boolean isSpringSemester() {
        return semesterType == SemesterType.SPRING;
    }

    public boolean isSummerSemester() {
        return semesterType == SemesterType.SUMMER;
    }

    public boolean isWinterSemester() {
        return semesterType == SemesterType.WINTER;
    }

    public boolean isRegularSemester() {
        return isFallSemester() || isSpringSemester();
    }

    public boolean isSpecialSemester() {
        return isSummerSemester() || isWinterSemester();
    }

    public String getSeason() {
        switch (semesterType) {
            case FALL: return "Fall";
            case SPRING: return "Spring";
            case SUMMER: return "Summer";
            case WINTER: return "Winter";
            default: return "Unknown";
        }
    }

    public String getAcademicYearStart() {
        return academicYear.substring(0, 4);
    }

    public String getAcademicYearEnd() {
        return academicYear.substring(5, 9);
    }

    public boolean isValidDateRange() {
        return startDate != null && endDate != null &&
               !startDate.isAfter(endDate) &&
               registrationDeadline != null &&
               !registrationDeadline.isAfter(endDate) &&
               !registrationDeadline.isBefore(startDate);
    }

    public boolean hasValidDuration() {
        long weeks = getDurationInWeeks();
        return weeks >= 12 && weeks <= 20; // Typical semester duration
    }

    public boolean hasValidRegistrationPeriod() {
        long daysBeforeStart = ChronoUnit.DAYS.between(registrationDeadline, startDate);
        return daysBeforeStart >= 7 && daysBeforeStart <= 30; // Registration closes 1-4 weeks before start
    }

    public boolean isValidSemester() {
        return isValidDateRange() && hasValidDuration() && hasValidRegistrationPeriod();
    }

    public void addCourseOffering(CourseOffering offering) {
        if (!courseOfferings.contains(offering)) {
            courseOfferings.add(offering);
            offering.setSemester(this);
        }
    }

    public void removeCourseOffering(CourseOffering offering) {
        courseOfferings.remove(offering);
        offering.setSemester(null);
    }

    public List<CourseOffering> getActiveOfferings() {
        return courseOfferings.stream()
                .filter(CourseOffering::isActive)
                .filter(CourseOffering::isOpen)
                .toList();
    }

    public List<CourseOffering> getScheduledOfferings() {
        return courseOfferings.stream()
                .filter(CourseOffering::hasScheduleAssigned)
                .toList();
    }

    public List<CourseOffering> getUnscheduledOfferings() {
        return courseOfferings.stream()
                .filter(offering -> !offering.hasScheduleAssigned())
                .toList();
    }

    public int getTotalOfferingsCount() {
        return courseOfferings.size();
    }

    public int getActiveOfferingsCount() {
        return getActiveOfferings().size();
    }

    public int getScheduledOfferingsCount() {
        return getScheduledOfferings().size();
    }

    public int getUnscheduledOfferingsCount() {
        return getUnscheduledOfferings().size();
    }

    public int getTotalEnrollment() {
        return courseOfferings.stream()
                .mapToInt(CourseOffering::getCurrentEnrollment)
                .sum();
    }

    public int getTotalCapacity() {
        return courseOfferings.stream()
                .mapToInt(CourseOffering::getMaxEnrollment)
                .sum();
    }

    public double getEnrollmentRate() {
        int totalEnrollment = getTotalEnrollment();
        int totalCapacity = getTotalCapacity();
        return totalCapacity > 0 ? (double) totalEnrollment / totalCapacity : 0.0;
    }

    public String getEnrollmentStatus() {
        double rate = getEnrollmentRate();
        if (rate >= 0.8) return "HIGH_ENROLLMENT";
        if (rate >= 0.6) return "GOOD_ENROLLMENT";
        if (rate >= 0.4) return "MODERATE_ENROLLMENT";
        return "LOW_ENROLLMENT";
    }

    public boolean isWellUtilized() {
        double rate = getEnrollmentRate();
        return rate >= 0.6 && rate <= 0.9;
    }

    public boolean isUnderUtilized() {
        return getEnrollmentRate() < 0.6;
    }

    public boolean isOverUtilized() {
        return getEnrollmentRate() > 0.9;
    }

    public String getStatus() {
        if (!isActive()) return "INACTIVE";
        if (isCurrent) return "CURRENT";
        if (isUpcoming()) return "UPCOMING";
        if (isCompleted()) return "COMPLETED";
        return "UNKNOWN";
    }

    public boolean canBeSetAsCurrent() {
        return isActive() || isUpcoming();
    }

    public void setCurrentSemester(boolean isCurrent) {
        this.isCurrent = isCurrent;
        if (isCurrent) {
            // When setting this as current, unset all others
            // This would be handled by a service method
        }
    }

    public boolean overlapsWith(Semester other) {
        if (other == null) return false;
        return !(this.endDate.isBefore(other.startDate) || this.startDate.isAfter(other.endDate));
    }

    public boolean containsDate(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean isWithinAcademicYear(String year) {
        return academicYear.equals(year);
    }

    public boolean isSameAcademicYearAs(Semester other) {
        return other != null && this.academicYear.equals(other.academicYear);
    }

    public boolean isImmediatelyBefore(Semester other) {
        if (other == null) return false;
        return this.endDate.plusDays(1).equals(other.startDate);
    }

    public boolean isImmediatelyAfter(Semester other) {
        if (other == null) return false;
        return this.startDate.minusDays(1).equals(other.endDate);
    }

    public String getSummary() {
        return String.format("%s: %s to %s (%d days, %.1f%% complete)",
                getFullDisplayName(),
                startDate,
                endDate,
                getDurationInDays(),
                getProgressPercentage());
    }

    public boolean isReadyForScheduling() {
        return isValidSemester() && isUpcoming() && isActive();
    }

    public boolean isPastSchedulingWindow() {
        if (startDate == null) return false;
        LocalDate today = LocalDate.now();
        return today.isAfter(startDate.minusMonths(1)); // Scheduling typically done 1 month before start
    }

    public boolean isWithinSchedulingWindow() {
        return isUpcoming() && !isPastSchedulingWindow();
    }

    public String getSchedulingStatus() {
        if (!isReadyForScheduling()) return "NOT_READY";
        if (isPastSchedulingWindow()) return "SCHEDULING_CLOSED";
        if (isWithinSchedulingWindow()) return "SCHEDULING_OPEN";
        return "PENDING";
    }
}