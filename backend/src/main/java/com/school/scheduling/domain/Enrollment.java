package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "course_offering_id"})
})
public class Enrollment extends BaseEntity {

    @NotNull(message = "Student is required")
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull(message = "Course offering is required")
    @ManyToOne
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering courseOffering;

    @NotNull(message = "Enrollment date is required")
    @PastOrPresent(message = "Enrollment date cannot be in the future")
    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    @DecimalMin(value = "0.00", message = "Grade cannot be negative")
    @DecimalMax(value = "100.00", message = "Grade cannot exceed 100.00")
    @Column(name = "grade", precision = 3, scale = 2)
    private BigDecimal grade;

    @Size(max = 2, message = "Grade letter must be less than 2 characters")
    @Column(name = "grade_letter", length = 2)
    private String gradeLetter;

    @NotNull(message = "Attendance status is required")
    @Column(name = "is_attending", nullable = false)
    private Boolean isAttending = true;

    public enum EnrollmentStatus {
        ENROLLED, DROPPED, COMPLETED, WITHDRAWN, FAILED
    }

    public boolean isActive() {
        return status == EnrollmentStatus.ENROLLED && isAttending;
    }

    public boolean isCompleted() {
        return status == EnrollmentStatus.COMPLETED;
    }

    public boolean isDropped() {
        return status == EnrollmentStatus.DROPPED;
    }

    public boolean isWithdrawn() {
        return status == EnrollmentStatus.WITHDRAWN;
    }

    public boolean isFailed() {
        return status == EnrollmentStatus.FAILED;
    }

    public boolean hasPassed() {
        return grade != null && grade.compareTo(new BigDecimal("60.00")) >= 0;
    }

    public boolean hasFailed() {
        return grade != null && grade.compareTo(new BigDecimal("60.00")) < 0;
    }

    public boolean isEligibleForCertificate() {
        return isCompleted() && hasPassed();
    }

    public boolean canBeDropped() {
        return isActive() && !isPastDropDeadline();
    }

    public boolean isPastDropDeadline() {
        if (enrollmentDate == null) return false;
        LocalDate today = LocalDate.now();
        LocalDate deadline = enrollmentDate.plusWeeks(2); // 2-week drop period
        return today.isAfter(deadline);
    }

    public String getGradeDisplay() {
        if (gradeLetter != null) return gradeLetter;
        if (grade != null) return grade.toString();
        return "Not Graded";
    }

    public String getStatusDisplay() {
        return status.toString().charAt(0) + status.toString().substring(1).toLowerCase().replace('_', ' ');
    }

    public String getEnrollmentPeriod() {
        if (enrollmentDate == null) return "Unknown";
        if (isCompleted()) {
            return "Completed";
        } else if (isActive()) {
            return "Active";
        } else {
            return "Inactive";
        }
    }

    public boolean isValidGrade() {
        return grade == null || 
               (grade.compareTo(new BigDecimal("0.00")) >= 0 && 
                grade.compareTo(new BigDecimal("100.00")) <= 0);
    }

    public boolean isValidEnrollment() {
        return student != null && courseOffering != null && enrollmentDate != null;
    }

    public String getDetailedInfo() {
        return String.format("%s - %s: %s (%s)",
                student != null ? student.getFullName() : "Unknown Student",
                courseOffering != null ? courseOffering.getCourseTitle() : "Unknown Course",
                getStatusDisplay(),
                getGradeDisplay());
    }

    public boolean isRecentEnrollment() {
        if (enrollmentDate == null) return false;
        LocalDate today = LocalDate.now();
        return !enrollmentDate.isBefore(today.minusWeeks(1));
    }

    public boolean isOldEnrollment() {
        if (enrollmentDate == null) return false;
        LocalDate today = LocalDate.now();
        return enrollmentDate.isBefore(today.minusMonths(6));
    }

    public long getEnrollmentDurationInDays() {
        if (enrollmentDate == null) return 0;
        LocalDate today = LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(enrollmentDate, today);
    }

    public boolean requiresAttention() {
        return isActive() && !isAttending;
    }

    public boolean isAtRisk() {
        return isActive() && isAttending && 
               courseOffering != null && 
               courseOffering.shouldCancelDueToLowEnrollment();
    }

    public boolean hasValidGradeLetter() {
        if (gradeLetter == null) return true;
        return gradeLetter.matches("[A-F][+-]?");
    }

    public BigDecimal getGradePoints() {
        if (grade == null) return BigDecimal.ZERO;
        
        double points = grade.doubleValue();
        if (points >= 90) return new BigDecimal("4.0");
        if (points >= 80) return new BigDecimal("3.0");
        if (points >= 70) return new BigDecimal("2.0");
        if (points >= 60) return new BigDecimal("1.0");
        return BigDecimal.ZERO;
    }

    public String getPerformanceLevel() {
        if (!hasPassed()) return "FAILING";
        if (grade.compareTo(new BigDecimal("90.00")) >= 0) return "EXCELLENT";
        if (grade.compareTo(new BigDecimal("80.00")) >= 0) return "GOOD";
        if (grade.compareTo(new BigDecimal("70.00")) >= 0) return "SATISFACTORY";
        return "PASSING";
    }

    public boolean canReceiveCertificate() {
        return isEligibleForCertificate() && hasValidGradeLetter();
    }

    public String getEnrollmentSummary() {
        return String.format("%s enrolled in %s on %s - Status: %s, Grade: %s",
                student != null ? student.getFullName() : "Student",
                courseOffering != null ? courseOffering.getFullDisplayName() : "Course",
                enrollmentDate,
                getStatusDisplay(),
                getGradeDisplay());
    }

    public boolean isValidStatusTransition(EnrollmentStatus newStatus) {
        if (newStatus == null) return false;
        
        switch (status) {
            case ENROLLED:
                return newStatus == EnrollmentStatus.DROPPED || 
                       newStatus == EnrollmentStatus.WITHDRAWN ||
                       newStatus == EnrollmentStatus.COMPLETED ||
                       newStatus == EnrollmentStatus.FAILED;
            case DROPPED:
            case WITHDRAWN:
                return false; // Cannot change from dropped/withdrawn
            case COMPLETED:
            case FAILED:
                return false; // Cannot change from completed/failed
            default:
                return false;
        }
    }

    public boolean canBeReactivated() {
        return isDropped() && !isPastDropDeadline();
    }

    public String getEnrollmentType() {
        if (courseOffering == null) return "UNKNOWN";
        
        switch (courseOffering.getScheduleType()) {
            case ONLINE: return "ONLINE";
            case WEEKEND: return "WEEKEND";
            case EVENING: return "EVENING";
            default: return "REGULAR";
        }
    }

    public boolean isOnlineEnrollment() {
        return "ONLINE".equals(getEnrollmentType());
    }

    public boolean isWeekendEnrollment() {
        return "WEEKEND".equals(getEnrollmentType());
    }

    public boolean isEveningEnrollment() {
        return "EVENING".equals(getEnrollmentType());
    }
}