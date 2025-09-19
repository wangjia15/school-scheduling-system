package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "course_offerings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"course_id", "semester_id", "section_number"})
})
public class CourseOffering extends BaseEntity {

    @NotNull(message = "Course is required")
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @NotNull(message = "Semester is required")
    @ManyToOne
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @NotBlank(message = "Section number is required")
    @Size(max = 10, message = "Section number must be less than 10 characters")
    @Column(name = "section_number", nullable = false, length = 10)
    private String sectionNumber;

    @NotNull(message = "Teacher is required")
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @NotNull(message = "Max enrollment is required")
    @Min(value = 1, message = "Max enrollment must be at least 1")
    @Max(value = 500, message = "Max enrollment cannot exceed 500")
    @Column(name = "max_enrollment", nullable = false)
    private Integer maxEnrollment = 30;

    @NotNull(message = "Current enrollment is required")
    @Min(value = 0, message = "Current enrollment cannot be negative")
    @Column(name = "current_enrollment", nullable = false)
    private Integer currentEnrollment = 0;

    @NotNull(message = "Schedule type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 20)
    private ScheduleType scheduleType = ScheduleType.REGULAR;

    @NotNull(message = "Open status is required")
    @Column(name = "is_open", nullable = false)
    private Boolean isOpen = true;

    @Size(max = 255, message = "Syllabus URL must be less than 255 characters")
    @Column(name = "syllabus_url", length = 255)
    private String syllabusUrl;

    @OneToMany(mappedBy = "courseOffering", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "courseOffering", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments = new ArrayList<>();

    public enum ScheduleType {
        REGULAR, WEEKEND, EVENING, ONLINE
    }

    public String getCourseCode() {
        return course != null ? course.getCourseCode() : "";
    }

    public String getCourseTitle() {
        return course != null ? course.getTitle() : "";
    }

    public String getDepartmentCode() {
        return course != null ? course.getDepartmentCode() : "";
    }

    public String getSemesterName() {
        return semester != null ? semester.getName() : "";
    }

    public String getTeacherName() {
        return teacher != null ? teacher.getFullName() : "";
    }

    public String getOfferingIdentifier() {
        return getCourseCode() + "-" + sectionNumber + " (" + getSemesterName() + ")";
    }

    public String getFullDisplayName() {
        return getCourseCode() + " " + getCourseTitle() + " - Section " + sectionNumber;
    }

    public boolean hasAvailableSeats() {
        return currentEnrollment < maxEnrollment;
    }

    public int getAvailableSeats() {
        return Math.max(0, maxEnrollment - currentEnrollment);
    }

    public double getEnrollmentRate() {
        if (maxEnrollment == 0) return 0.0;
        return (double) currentEnrollment / maxEnrollment;
    }

    public boolean isFull() {
        return currentEnrollment >= maxEnrollment;
    }

    public boolean isNearlyFull() {
        double rate = getEnrollmentRate();
        return rate >= 0.8 && rate < 1.0;
    }

    public boolean hasLowEnrollment() {
        double rate = getEnrollmentRate();
        return rate < 0.3 && currentEnrollment > 0;
    }

    public boolean hasVeryLowEnrollment() {
        return currentEnrollment < 3;
    }

    public boolean canEnrollStudent() {
        return isOpen && hasAvailableSeats() && isActive();
    }

    public boolean isRegistrationOpen() {
        return isOpen && semester != null && semester.isRegistrationOpen();
    }

    public boolean isRegularSchedule() {
        return scheduleType == ScheduleType.REGULAR;
    }

    public boolean isWeekendSchedule() {
        return scheduleType == ScheduleType.WEEKEND;
    }

    public boolean isEveningSchedule() {
        return scheduleType == ScheduleType.EVENING;
    }

    public boolean isOnlineCourse() {
        return scheduleType == ScheduleType.ONLINE;
    }

    public boolean requiresPhysicalClassroom() {
        return !isOnlineCourse();
    }

    public boolean hasScheduleAssigned() {
        return !schedules.isEmpty();
    }

    public boolean isScheduled() {
        return hasScheduleAssigned();
    }

    public List<Schedule> getActiveSchedules() {
        return schedules.stream()
                .filter(schedule -> schedule.isActive() && schedule.isUpcoming())
                .toList();
    }

    public boolean hasConflictWith(CourseOffering other) {
        if (other == null || other.equals(this)) return false;

        return schedules.stream()
                .anyMatch(thisSchedule ->
                        other.schedules.stream()
                                .anyMatch(otherSchedule ->
                                        thisSchedule.conflictsWith(otherSchedule)));
    }

    public boolean canAcceptMoreStudents() {
        return canEnrollStudent();
    }

    public void incrementEnrollment() {
        if (hasAvailableSeats()) {
            currentEnrollment++;
        }
    }

    public void decrementEnrollment() {
        if (currentEnrollment > 0) {
            currentEnrollment--;
        }
    }

    public void setEnrollmentCount(int count) {
        currentEnrollment = Math.max(0, Math.min(count, maxEnrollment));
    }

    public boolean isValidEnrollmentCount() {
        return currentEnrollment >= 0 && currentEnrollment <= maxEnrollment;
    }

    public boolean meetsMinimumEnrollment() {
        return course != null && currentEnrollment >= course.getMinStudents();
    }

    public boolean shouldCancelDueToLowEnrollment() {
        return !meetsMinimumEnrollment() && hasVeryLowEnrollment();
    }

    public String getEnrollmentStatus() {
        if (!isOpen) return "CLOSED";
        if (isFull()) return "FULL";
        if (hasAvailableSeats()) return "OPEN";
        return "CLOSED";
    }

    public String getCapacityStatus() {
        double rate = getEnrollmentRate();
        if (rate >= 0.9) return "HIGH_UTILIZATION";
        if (rate >= 0.7) return "GOOD_UTILIZATION";
        if (rate >= 0.5) return "MODERATE_UTILIZATION";
        return "LOW_UTILIZATION";
    }

    public int getEstimatedFinalEnrollment() {
        if (isRegistrationOpen()) {
            // Estimate based on current enrollment rate and time remaining
            double estimatedRate = Math.min(1.0, getEnrollmentRate() + 0.2);
            return (int) Math.ceil(maxEnrollment * estimatedRate);
        }
        return currentEnrollment;
    }

    public boolean isPopularCourse() {
        return getEnrollmentRate() >= 0.8 && maxEnrollment >= 40;
    }

    public boolean isHighDemandCourse() {
        return isFull() && currentEnrollment >= maxEnrollment;
    }

    public boolean isNewOffering() {
        if (createdAt == null) return false;
        return createdAt.isAfter(LocalDate.now().minusMonths(1).atStartOfDay());
    }

    public boolean hasSyllabus() {
        return syllabusUrl != null && !syllabusUrl.trim().isEmpty();
    }

    public String getSectionType() {
        if (sectionNumber.matches("^[A-Za-z]+$")) {
            return "LAB";
        } else if (sectionNumber.matches("^[0-9]+$")) {
            return "LECTURE";
        } else {
            return "MIXED";
        }
    }

    public boolean isLabSection() {
        return "LAB".equals(getSectionType());
    }

    public boolean isLectureSection() {
        return "LECTURE".equals(getSectionType());
    }

    public String getScheduleSummary() {
        if (!hasScheduleAssigned()) {
            return "No schedule assigned";
        }

        StringBuilder summary = new StringBuilder();
        for (Schedule schedule : schedules) {
            if (summary.length() > 0) {
                summary.append(", ");
            }
            summary.append(schedule.getScheduleSummary());
        }

        return summary.toString();
    }

    public void addSchedule(Schedule schedule) {
        if (!schedules.contains(schedule)) {
            schedules.add(schedule);
            schedule.setCourseOffering(this);
        }
    }

    public void removeSchedule(Schedule schedule) {
        schedules.remove(schedule);
        schedule.setCourseOffering(null);
    }

    public void addEnrollment(Enrollment enrollment) {
        if (!enrollments.contains(enrollment)) {
            enrollments.add(enrollment);
            enrollment.setCourseOffering(this);
        }
    }

    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
        enrollment.setCourseOffering(null);
    }

    public List<Student> getEnrolledStudents() {
        return enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .map(Enrollment::getStudent)
                .toList();
    }

    public boolean hasStudentEnrolled(Student student) {
        return enrollments.stream()
                .anyMatch(e -> e.getStudent().equals(student) &&
                           e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED);
    }

    public int getWaitlistedCount() {
        return (int) enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.WAITLISTED)
                .count();
    }

    public boolean hasWaitlist() {
        return getWaitlistedCount() > 0;
    }

    public boolean isValidOffering() {
        return course != null && teacher != null && semester != null &&
               isValidEnrollmentCount() &&
               maxEnrollment > 0 &&
               !sectionNumber.trim().isEmpty();
    }

    public String getOfferingStatus() {
        if (!isActive()) return "INACTIVE";
        if (!isOpen) return "CLOSED";
        if (!hasScheduleAssigned()) return "UNSCHEDULED";
        if (shouldCancelDueToLowEnrollment()) return "AT_RISK";
        return "ACTIVE";
    }

    public boolean isReadyForScheduling() {
        return isValidOffering() && isActive() && isOpen;
    }

    public boolean canBeScheduled() {
        return isReadyForScheduling() && !hasScheduleAssigned();
    }

    public String getDetailedInformation() {
        return String.format("%s - %s (Section %s) - %s - %s",
                getCourseCode(), getCourseTitle(), sectionNumber,
                getTeacherName(), getSemesterName());
    }
}