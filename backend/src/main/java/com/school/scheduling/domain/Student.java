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
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "students", uniqueConstraints = {
    @UniqueConstraint(columnNames = "user_id"),
    @UniqueConstraint(columnNames = "student_id")
})
public class Student extends BaseEntity {

    @NotNull(message = "User is required")
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank(message = "Student ID is required")
    @Size(max = 20, message = "Student ID must be less than 20 characters")
    @Column(name = "student_id", nullable = false, unique = true, length = 20)
    private String studentId;

    @NotNull(message = "Department is required")
    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotNull(message = "Enrollment year is required")
    @Min(value = 1900, message = "Enrollment year must be after 1900")
    @Max(value = 2100, message = "Enrollment year cannot exceed 2100")
    @Column(name = "enrollment_year", nullable = false)
    private Integer enrollmentYear;

    @Min(value = 1900, message = "Graduation year must be after 1900")
    @Max(value = 2100, message = "Graduation year cannot exceed 2100")
    @Column(name = "graduation_year")
    private Integer graduationYear;

    @NotNull(message = "Current semester is required")
    @Min(value = 1, message = "Current semester must be at least 1")
    @Max(value = 12, message = "Current semester cannot exceed 12")
    @Column(name = "current_semester", nullable = false)
    private Integer currentSemester = 1;

    @DecimalMin(value = "0.00", message = "GPA cannot be negative")
    @DecimalMax(value = "4.00", message = "GPA cannot exceed 4.00")
    @Column(name = "gpa", precision = 3, scale = 2)
    private BigDecimal gpa = new BigDecimal("0.00");

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StudentStatus status = StudentStatus.ACTIVE;

    @OneToMany(mappedBy = "student", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments = new ArrayList<>();

    public enum StudentStatus {
        ACTIVE, INACTIVE, GRADUATED, SUSPENDED
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

    public boolean isActive() {
        return status == StudentStatus.ACTIVE;
    }

    public boolean isGraduated() {
        return status == StudentStatus.GRADUATED;
    }

    public boolean isSuspended() {
        return status == StudentStatus.SUSPENDED;
    }

    public boolean canEnrollInCourses() {
        return isActive() && !isGraduated();
    }

    public boolean hasSufficientGPA(BigDecimal requiredGpa) {
        return gpa.compareTo(requiredGpa) >= 0;
    }

    public boolean meetsPrerequisiteGpa(BigDecimal prerequisiteGpa) {
        return gpa.compareTo(prerequisiteGpa) >= 0;
    }

    public int getAcademicYear() {
        int currentYear = Year.now().getValue();
        return currentYear - enrollmentYear + 1;
    }

    public boolean isUpperclassman() {
        return getAcademicYear() >= 3;
    }

    public boolean isFreshman() {
        return getAcademicYear() == 1;
    }

    public boolean isSophomore() {
        return getAcademicYear() == 2;
    }

    public boolean isJunior() {
        return getAcademicYear() == 3;
    }

    public boolean isSenior() {
        return getAcademicYear() == 4;
    }

    public boolean isGraduateStudent() {
        return getAcademicYear() > 4;
    }

    public boolean hasValidStudentId() {
        return studentId != null && !studentId.trim().isEmpty();
    }

    public void enrollInCourse(Enrollment enrollment) {
        if (!enrollments.contains(enrollment)) {
            enrollments.add(enrollment);
            enrollment.setStudent(this);
        }
    }

    public void withdrawFromCourse(Enrollment enrollment) {
        enrollments.remove(enrollment);
        enrollment.setStudent(null);
    }

    public boolean isEnrolledIn(Long courseOfferingId) {
        return enrollments.stream()
                .anyMatch(e -> e.getCourseOffering().getId().equals(courseOfferingId) &&
                           e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED);
    }

    public int getEnrolledCourseCount() {
        return (int) enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .count();
    }

    public boolean canTakeMoreCourses(int maxCourses) {
        return getEnrolledCourseCount() < maxCourses;
    }
}