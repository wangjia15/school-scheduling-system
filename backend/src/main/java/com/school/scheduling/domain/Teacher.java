package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "teachers", uniqueConstraints = {
    @UniqueConstraint(columnNames = "user_id"),
    @UniqueConstraint(columnNames = "employee_id")
})
public class Teacher extends BaseEntity {

    @NotNull(message = "User is required")
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "Employee ID is required")
    @Size(max = 20, message = "Employee ID must be less than 20 characters")
    @Column(name = "employee_id", nullable = false, unique = true, length = 20)
    private String employeeId;

    @NotNull(message = "Department is required")
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotNull(message = "Title is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "title", nullable = false, length = 30)
    private TeacherTitle title;

    @NotNull(message = "Max weekly hours is required")
    @DecimalMin(value = "1.0", message = "Max weekly hours must be at least 1.0")
    @DecimalMax(value = "60.0", message = "Max weekly hours cannot exceed 60.0")
    @Column(name = "max_weekly_hours", nullable = false, precision = 4, scale = 1)
    private BigDecimal maxWeeklyHours = new BigDecimal("40.0");

    @NotNull(message = "Max courses per semester is required")
    @Min(value = 1, message = "Max courses per semester must be at least 1")
    @Max(value = 10, message = "Max courses per semester cannot exceed 10")
    @Column(name = "max_courses_per_semester", nullable = false)
    private Integer maxCoursesPerSemester = 5;

    @Size(max = 100, message = "Office location must be less than 100 characters")
    @Column(name = "office_location", length = 100)
    private String officeLocation;

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;

    @OneToMany(mappedBy = "teacher", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<TeacherSpecialization> specializations = new ArrayList<>();

    public enum TeacherTitle {
        PROFESSOR, ASSOCIATE_PROFESSOR, ASSISTANT_PROFESSOR, INSTRUCTOR, ADJUNCT
    }

    public String getFullName() {
        return user != null ? user.getFullName() : "";
    }

    public String getEmail() {
        return user != null ? user.getEmail() : "";
    }

    public String getDepartmentName() {
        return department != null ? department.getName() : "";
    }

    public boolean canTeachSubject(String subjectCode) {
        return specializations.stream()
                .anyMatch(spec -> subjectCode.equals(spec.getSubjectCode()));
    }

    public boolean isAvailableForAdditionalHours(BigDecimal additionalHours) {
        return getMaxWeeklyHours().compareTo(additionalHours) >= 0;
    }

    public boolean canTeachMoreCourses(int additionalCourses) {
        return getMaxCoursesPerSemester() >= additionalCourses;
    }

    public boolean hasSpecializationIn(String subjectCode) {
        return specializations.stream()
                .anyMatch(spec -> subjectCode.equals(spec.getSubjectCode()));
    }

    public int getProficiencyLevel(String subjectCode) {
        return specializations.stream()
                .filter(spec -> subjectCode.equals(spec.getSubjectCode()))
                .mapToInt(spec -> spec.getProficiencyLevel().ordinal())
                .findFirst()
                .orElse(-1);
    }

    public void addSpecialization(TeacherSpecialization specialization) {
        if (!specializations.contains(specialization)) {
            specializations.add(specialization);
            specialization.setTeacher(this);
        }
    }

    public void removeSpecialization(TeacherSpecialization specialization) {
        specializations.remove(specialization);
        specialization.setTeacher(null);
    }
}