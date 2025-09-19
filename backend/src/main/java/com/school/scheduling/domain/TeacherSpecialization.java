package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "teacher_specializations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"teacher_id", "subject_code"})
})
public class TeacherSpecialization extends BaseEntity {

    @NotNull(message = "Teacher is required")
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @NotBlank(message = "Subject code is required")
    @Size(max = 20, message = "Subject code must be less than 20 characters")
    @Column(name = "subject_code", nullable = false, length = 20)
    private String subjectCode;

    @Size(max = 100, message = "Subject name must be less than 100 characters")
    @Column(name = "subject_name", length = 100)
    private String subjectName;

    @NotNull(message = "Proficiency level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level", nullable = false, length = 20)
    private ProficiencyLevel proficiencyLevel = ProficiencyLevel.INTERMEDIATE;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    @Column(name = "years_experience")
    private Integer yearsExperience = 0;

    @Column(name = "certified")
    private Boolean certified = false;

    @Size(max = 500, message = "Certification details must be less than 500 characters")
    @Column(name = "certification_details", length = 500)
    private String certificationDetails;

    public enum ProficiencyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public boolean isQualifiedFor(String subjectCode) {
        return this.subjectCode.equals(subjectCode) &&
               proficiencyLevel != ProficiencyLevel.BEGINNER;
    }

    public boolean isExpertIn(String subjectCode) {
        return this.subjectCode.equals(subjectCode) &&
               proficiencyLevel == ProficiencyLevel.EXPERT;
    }

    public boolean hasSufficientExperience(int requiredYears) {
        return yearsExperience >= requiredYears;
    }

    public String getSubjectDisplayName() {
        return subjectCode + " (" + proficiencyLevel + ")";
    }

    public int getProficiencyScore() {
        return proficiencyLevel.ordinal() + 1;
    }

    public boolean canTeachAdvancedCourses() {
        return proficiencyLevel == ProficiencyLevel.ADVANCED ||
               proficiencyLevel == ProficiencyLevel.EXPERT;
    }

    public boolean canTeachGraduateCourses() {
        return proficiencyLevel == ProficiencyLevel.EXPERT;
    }

    public String getSubjectDisplayName() {
        return subjectName != null ? subjectName : subjectCode;
    }

    public boolean hasCertification() {
        return certified != null && certified;
    }

    public boolean isSeniorLevel() {
        return proficiencyLevel == ProficiencyLevel.ADVANCED || proficiencyLevel == ProficiencyLevel.EXPERT;
    }

    public boolean canTeachUndergraduateCourses() {
        return proficiencyLevel != ProficiencyLevel.BEGINNER;
    }

    public int getProficiencyScore() {
        return proficiencyLevel.ordinal() + 1;
    }

    public String getExperienceLevel() {
        if (yearsExperience < 2) return "Entry Level"
        if (yearsExperience < 5) return "Junior"
        if (yearsExperience < 10) return "Mid-Level"
        if (yearsExperience < 15) return "Senior"
        return "Expert"
    }
}