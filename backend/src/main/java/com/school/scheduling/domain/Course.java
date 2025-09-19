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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "courses", uniqueConstraints = {
    @UniqueConstraint(columnNames = "course_code")
})
public class Course extends BaseEntity {

    @NotBlank(message = "Course code is required")
    @Size(max = 20, message = "Course code must be less than 20 characters")
    @Column(name = "course_code", nullable = false, unique = true, length = 20)
    private String courseCode;

    @NotBlank(message = "Course title is required")
    @Size(max = 200, message = "Course title must be less than 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Department is required")
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotNull(message = "Credits are required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 6, message = "Credits cannot exceed 6")
    @Column(name = "credits", nullable = false)
    private Integer credits;

    @NotNull(message = "Contact hours per week are required")
    @DecimalMin(value = "0.5", message = "Contact hours must be at least 0.5")
    @DecimalMax(value = "20.0", message = "Contact hours cannot exceed 20.0")
    @Column(name = "contact_hours_per_week", nullable = false, precision = 3, scale = 1)
    private BigDecimal contactHoursPerWeek;

    @DecimalMin(value = "0.0", message = "Theory hours cannot be negative")
    @DecimalMax(value = "20.0", message = "Theory hours cannot exceed 20.0")
    @Column(name = "theory_hours", precision = 3, scale = 1)
    private BigDecimal theoryHours = new BigDecimal("0.0");

    @DecimalMin(value = "0.0", message = "Lab hours cannot be negative")
    @DecimalMax(value = "20.0", message = "Lab hours cannot exceed 20.0")
    @Column(name = "lab_hours", precision = 3, scale = 1)
    private BigDecimal labHours = new BigDecimal("0.0");

    @NotNull(message = "Course level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private CourseLevel level;

    @NotNull(message = "Active status is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Min(value = 1, message = "Max students must be at least 1")
    @Max(value = 500, message = "Max students cannot exceed 500")
    @Column(name = "max_students")
    private Integer maxStudents = 30;

    @Min(value = 1, message = "Min students must be at least 1")
    @Max(value = 500, message = "Min students cannot exceed 500")
    @Column(name = "min_students")
    private Integer minStudents = 5;

    @NotNull(message = "Lab requirement status is required")
    @Column(name = "requires_lab", nullable = false)
    private Boolean requiresLab = false;

    @OneToMany(mappedBy = "course", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<CoursePrerequisite> prerequisites = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<CourseOffering> offerings = new ArrayList<>();

    @OneToMany(mappedBy = "prerequisiteCourse", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<CoursePrerequisite> isPrerequisiteFor = new ArrayList<>();

    public enum CourseLevel {
        UNDERGRADUATE, GRADUATE, PHD
    }

    public String getDepartmentCode() {
        return department != null ? department.getCode() : "";
    }

    public String getDepartmentName() {
        return department != null ? department.getName() : "";
    }

    public String getCourseIdentifier() {
        return getDepartmentCode() + " " + courseCode;
    }

    public String getFullDisplayName() {
        return getCourseIdentifier() + " - " + title;
    }

    public boolean isUndergraduate() {
        return level == CourseLevel.UNDERGRADUATE;
    }

    public boolean isGraduate() {
        return level == CourseLevel.GRADUATE;
    }

    public boolean isPhD() {
        return level == CourseLevel.PHD;
    }

    public boolean hasLabComponent() {
        return requiresLab || labHours.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isTheoryOnly() {
        return !hasLabComponent();
    }

    public boolean isActiveForRegistration() {
        return isActive && isActive();
    }

    public boolean canBeOfferedThisSemester() {
        return isActiveForRegistration();
    }

    public boolean hasMinimumEnrollmentRequirements() {
        return minStudents != null && minStudents > 0;
    }

    public boolean hasMaximumEnrollmentLimit() {
        return maxStudents != null && maxStudents > 0;
    }

    public boolean isValidEnrollmentRange() {
        return minStudents != null && maxStudents != null &&
               minStudents <= maxStudents;
    }

    public BigDecimal getTotalWeeklyHours() {
        return theoryHours.add(labHours);
    }

    public boolean hasValidHoursDistribution() {
        return getTotalWeeklyHours().compareTo(contactHoursPerWeek) == 0;
    }

    public boolean isLabCourse() {
        return requiresLab;
    }

    public boolean isLectureCourse() {
        return theoryHours.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isHybridCourse() {
        return isLectureCourse() && isLabCourse();
    }

    public String getCourseType() {
        if (isHybridCourse()) return "HYBRID";
        if (isLabCourse()) return "LAB";
        return "LECTURE";
    }

    public boolean requiresComputerLab() {
        return requiresLab && courseCode.toUpperCase().contains("CS");
    }

    public boolean requiresScienceLab() {
        return requiresLab && (courseCode.toUpperCase().contains("BIO") ||
                              courseCode.toUpperCase().contains("CHEM") ||
                              courseCode.toUpperCase().contains("PHY"));
    }

    public String getCourseLevelDisplay() {
        return level.toString().charAt(0) + level.toString().substring(1).toLowerCase();
    }

    public boolean isCoreCourse() {
        return courseCode.matches("^[A-Z]{2,4}[0-9]{3}$");
    }

    public boolean isElectiveCourse() {
        return courseCode.matches("^[A-Z]{2,4}[0-9]{4}[A-Z]?$");
    }

    public String getCourseCategory() {
        if (isCoreCourse()) return "CORE";
        if (isElectiveCourse()) return "ELECTIVE";
        return "SPECIAL";
    }

    public boolean canBeTakenByUndergraduates() {
        return isUndergraduate() || (isGraduate() && credits <= 3);
    }

    public boolean canBeTakenByGraduates() {
        return isGraduate() || isPhD();
    }

    public boolean requiresInstructorApproval() {
        return isGraduate() || isPhD();
    }

    public int getDifficultyLevel() {
        if (isUndergraduate()) {
            int levelNum = Integer.parseInt(courseCode.replaceAll("[^0-9]", ""));
            if (levelNum <= 200) return 1;
            if (levelNum <= 300) return 2;
            return 3;
        } else if (isGraduate()) {
            return 4;
        } else {
            return 5;
        }
    }

    public String getDifficultyRating() {
        int level = getDifficultyLevel();
        if (level <= 2) return "BEGINNER";
        if (level <= 3) return "INTERMEDIATE";
        if (level <= 4) return "ADVANCED";
        return "EXPERT";
    }

    public void addPrerequisite(CoursePrerequisite prerequisite) {
        if (!prerequisites.contains(prerequisite)) {
            prerequisites.add(prerequisite);
            prerequisite.setCourse(this);
        }
    }

    public void removePrerequisite(CoursePrerequisite prerequisite) {
        prerequisites.remove(prerequisite);
        prerequisite.setCourse(null);
    }

    public void addOffering(CourseOffering offering) {
        if (!offerings.contains(offering)) {
            offerings.add(offering);
            offering.setCourse(this);
        }
    }

    public void removeOffering(CourseOffering offering) {
        offerings.remove(offering);
        offering.setCourse(null);
    }

    public boolean hasPrerequisites() {
        return !prerequisites.isEmpty();
    }

    public boolean isPrerequisiteForOtherCourses() {
        return !isPrerequisiteFor.isEmpty();
    }

    public List<Course> getPrerequisiteCourses() {
        return prerequisites.stream()
                .map(CoursePrerequisite::getPrerequisiteCourse)
                .toList();
    }

    public List<Course> getIsPrerequisiteForCourses() {
        return isPrerequisiteFor.stream()
                .map(CoursePrerequisite::getCourse)
                .toList();
    }

    public boolean isDirectPrerequisiteFor(Course course) {
        return isPrerequisiteFor.stream()
                .anyMatch(prereq -> prereq.getCourse().equals(course));
    }

    public boolean isIndirectPrerequisiteFor(Course course) {
        return getIsPrerequisiteForCourses().stream()
                .anyMatch(prereqCourse ->
                    prereqCourse.equals(course) ||
                    prereqCourse.isIndirectPrerequisiteFor(course));
    }

    public boolean hasPrerequisiteChainWith(Course course) {
        return isDirectPrerequisiteFor(course) || isIndirectPrerequisiteFor(course);
    }

    public String getPrerequisiteSummary() {
        if (!hasPrerequisites()) return "No prerequisites";

        List<String> prereqCodes = prerequisites.stream()
                .map(prereq -> prereq.getPrerequisiteCourse().getCourseIdentifier())
                .toList();

        return String.join(", ", prereqCodes);
    }

    public boolean isValidCourseStructure() {
        return isValidEnrollmentRange() &&
               hasValidHoursDistribution() &&
               credits > 0 &&
               contactHoursPerWeek.compareTo(BigDecimal.ZERO) > 0;
    }

    public String getCourseDescriptionSummary() {
        if (description == null || description.length() <= 100) {
            return description;
        }
        return description.substring(0, 97) + "...";
    }
}