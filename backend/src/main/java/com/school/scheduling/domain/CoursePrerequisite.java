package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "course_prerequisites", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"course_id", "prerequisite_course_id"})
})
public class CoursePrerequisite extends BaseEntity {

    @NotNull(message = "Course is required")
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @NotNull(message = "Prerequisite course is required")
    @ManyToOne
    @JoinColumn(name = "prerequisite_course_id", nullable = false)
    private Course prerequisiteCourse;

    @NotNull(message = "Mandatory status is required")
    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = true;

    @DecimalMin(value = "0.00", message = "Minimum grade cannot be negative")
    @DecimalMax(value = "100.00", message = "Minimum grade cannot exceed 100.00")
    @Column(name = "minimum_grade", precision = 3, scale = 2)
    private BigDecimal minimumGrade = new BigDecimal("60.00");

    public boolean isOptional() {
        return !isMandatory;
    }

    public boolean hasGradeRequirement() {
        return minimumGrade != null && minimumGrade.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean meetsGradeRequirement(BigDecimal achievedGrade) {
        if (!hasGradeRequirement()) {
            return true;
        }
        return achievedGrade != null && achievedGrade.compareTo(minimumGrade) >= 0;
    }

    public String getPrerequisiteDescription() {
        StringBuilder description = new StringBuilder();

        if (prerequisiteCourse != null) {
            description.append(prerequisiteCourse.getCourseIdentifier())
                      .append(" - ")
                      .append(prerequisiteCourse.getTitle());
        }

        if (hasGradeRequirement()) {
            description.append(" (Minimum grade: ")
                      .append(minimumGrade)
                      .append(")");
        }

        if (isOptional()) {
            description.append(" - Optional");
        }

        return description.toString();
    }

    public boolean isValidPrerequisiteRelationship() {
        if (course == null || prerequisiteCourse == null) {
            return false;
        }

        // A course cannot be a prerequisite for itself
        if (course.equals(prerequisiteCourse)) {
            return false;
        }

        // Check for circular prerequisites
        if (prerequisiteCourse.getIsPrerequisiteForCourses().contains(course)) {
            return false;
        }

        // Validate grade requirement is reasonable
        if (minimumGrade != null) {
            BigDecimal minValidGrade = new BigDecimal("0.00");
            BigDecimal maxValidGrade = new BigDecimal("100.00");
            return minimumGrade.compareTo(minValidGrade) >= 0 &&
                   minimumGrade.compareTo(maxValidGrade) <= 0;
        }

        return true;
    }

    public boolean isApplicableForStudent(Student student) {
        if (!hasGradeRequirement()) {
            return true;
        }

        // This would typically require checking the student's transcript
        // For now, we assume the requirement must be checked externally
        return true;
    }

    public String getRequirementType() {
        if (isMandatory) {
            return hasGradeRequirement() ? "MANDATORY_WITH_GRADE" : "MANDATORY";
        } else {
            return hasGradeRequirement() ? "OPTIONAL_WITH_GRADE" : "OPTIONAL";
        }
    }

    public String getShortDescription() {
        if (prerequisiteCourse == null) return "Unknown prerequisite";

        String courseCode = prerequisiteCourse.getCourseIdentifier();

        if (isMandatory) {
            if (hasGradeRequirement()) {
                return String.format("%s (min grade: %s)", courseCode, minimumGrade);
            } else {
                return courseCode;
            }
        } else {
            if (hasGradeRequirement()) {
                return String.format("%s (optional, min grade: %s)", courseCode, minimumGrade);
            } else {
                return courseCode + " (optional)";
            }
        }
    }

    public boolean isStrictRequirement() {
        return isMandatory && !hasGradeRequirement();
    }

    public boolean isFlexibleRequirement() {
        return isOptional || hasGradeRequirement();
    }

    public BigDecimal getMinimumGradeAsPercentage() {
        return minimumGrade != null ? minimumGrade : new BigDecimal("60.00");
    }

    public String getMinimumGradeDisplay() {
        if (!hasGradeRequirement()) {
            return "No minimum grade";
        }

        BigDecimal grade = getMinimumGradeAsPercentage();
        if (grade.compareTo(new BigDecimal("90.00")) >= 0) {
            return "A (" + grade + "%)";
        } else if (grade.compareTo(new BigDecimal("80.00")) >= 0) {
            return "B (" + grade + "%)";
        } else if (grade.compareTo(new BigDecimal("70.00")) >= 0) {
            return "C (" + grade + "%)";
        } else if (grade.compareTo(new BigDecimal("60.00")) >= 0) {
            return "D (" + grade + "%)";
        } else {
            return "F (" + grade + "%)";
        }
    }

    public boolean isPassingGrade(BigDecimal grade) {
        return grade != null && grade.compareTo(new BigDecimal("60.00")) >= 0;
    }

    public boolean requiresGoodAcademicStanding() {
        return hasGradeRequirement() &&
               getMinimumGradeAsPercentage().compareTo(new BigDecimal("70.00")) >= 0;
    }

    public boolean requiresExcellentAcademicStanding() {
        return hasGradeRequirement() &&
               getMinimumGradeAsPercentage().compareTo(new BigDecimal("80.00")) >= 0;
    }

    public boolean isGraduateLevelRequirement() {
        return hasGradeRequirement() &&
               getMinimumGradeAsPercentage().compareTo(new BigDecimal("75.00")) >= 0;
    }

    public String getAcademicLevel() {
        BigDecimal minGrade = getMinimumGradeAsPercentage();
        if (minGrade.compareTo(new BigDecimal("85.00")) >= 0) {
            return "HIGH";
        } else if (minGrade.compareTo(new BigDecimal("70.00")) >= 0) {
            return "MEDIUM";
        } else {
            return "STANDARD";
        }
    }

    public boolean isMoreStringentThan(CoursePrerequisite other) {
        if (other == null) return true;

        // Compare mandatory vs optional
        if (this.isMandatory && !other.isMandatory) return true;
        if (!this.isMandatory && other.isMandatory) return false;

        // If both are mandatory, compare grade requirements
        if (this.isMandatory && other.isMandatory) {
            if (this.hasGradeRequirement() && !other.hasGradeRequirement()) return true;
            if (!this.hasGradeRequirement() && other.hasGradeRequirement()) return false;
            if (this.hasGradeRequirement() && other.hasGradeRequirement()) {
                return this.minimumGrade.compareTo(other.minimumGrade) > 0;
            }
        }

        return false;
    }

    public boolean isLessStringentThan(CoursePrerequisite other) {
        if (other == null) return false;
        return other.isMoreStringentThan(this);
    }

    public boolean hasSameRequirementsAs(CoursePrerequisite other) {
        if (other == null) return false;

        return this.isMandatory == other.isMandatory &&
               (this.minimumGrade == null && other.minimumGrade == null ||
                (this.minimumGrade != null && other.minimumGrade != null &&
                 this.minimumGrade.compareTo(other.minimumGrade) == 0));
    }

    public boolean isValidForCourseLevel(Course.CourseLevel level) {
        if (!hasGradeRequirement()) return true;

        BigDecimal minGrade = getMinimumGradeAsPercentage();

        switch (level) {
            case UNDERGRADUATE:
                return minGrade.compareTo(new BigDecimal("60.00")) >= 0;
            case GRADUATE:
                return minGrade.compareTo(new BigDecimal("75.00")) >= 0;
            case PHD:
                return minGrade.compareTo(new BigDecimal("80.00")) >= 0;
            default:
                return true;
        }
    }

    public String getValidationMessage() {
        if (!isValidPrerequisiteRelationship()) {
            return "Invalid prerequisite relationship: circular dependency detected";
        }

        if (hasGradeRequirement()) {
            BigDecimal minGrade = getMinimumGradeAsPercentage();
            if (minGrade.compareTo(new BigDecimal("0.00")) < 0 ||
                minGrade.compareTo(new BigDecimal("100.00")) > 0) {
                return "Minimum grade must be between 0.00 and 100.00";
            }
        }

        return "Valid prerequisite";
    }
}