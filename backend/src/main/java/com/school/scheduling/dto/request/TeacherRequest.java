package com.school.scheduling.dto.request;

import com.school.scheduling.domain.Teacher;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRequest {
    @NotBlank(message = "Employee ID is required")
    @Size(max = 20, message = "Employee ID must be less than 20 characters")
    private String employeeId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Title is required")
    private Teacher.TeacherTitle title;

    @NotNull(message = "Max weekly hours is required")
    @DecimalMin(value = "1.0", message = "Max weekly hours must be at least 1.0")
    @DecimalMax(value = "60.0", message = "Max weekly hours cannot exceed 60.0")
    private BigDecimal maxWeeklyHours = new BigDecimal("40.0");

    @NotNull(message = "Max courses per semester is required")
    @Min(value = 1, message = "Max courses per semester must be at least 1")
    @Max(value = 10, message = "Max courses per semester cannot exceed 10")
    private Integer maxCoursesPerSemester = 5;

    @Size(max = 100, message = "Office location must be less than 100 characters")
    private String officeLocation;

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phone;

    private List<TeacherSpecializationRequest> specializations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherSpecializationRequest {
        @NotBlank(message = "Subject code is required")
        @Size(max = 20, message = "Subject code must be less than 20 characters")
        private String subjectCode;

        @NotBlank(message = "Subject name is required")
        @Size(max = 100, message = "Subject name must be less than 100 characters")
        private String subjectName;

        @NotNull(message = "Proficiency level is required")
        private TeacherSpecialization.ProficiencyLevel proficiencyLevel;

        @Min(value = 0, message = "Years of experience cannot be negative")
        @Max(value = 50, message = "Years of experience cannot exceed 50")
        private Integer yearsOfExperience = 0;

        private Boolean certified = false;

        @Size(max = 500, message = "Certification details must be less than 500 characters")
        private String certificationDetails;
    }
}