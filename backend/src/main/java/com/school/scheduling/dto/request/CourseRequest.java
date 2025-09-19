package com.school.scheduling.dto.request;

import com.school.scheduling.domain.Course;
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
public class CourseRequest {
    @NotBlank(message = "Course code is required")
    @Size(max = 20, message = "Course code must be less than 20 characters")
    private String courseCode;

    @NotBlank(message = "Course title is required")
    @Size(max = 200, message = "Course title must be less than 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Credits are required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 6, message = "Credits cannot exceed 6")
    private Integer credits;

    @NotNull(message = "Contact hours per week are required")
    @DecimalMin(value = "0.5", message = "Contact hours must be at least 0.5")
    @DecimalMax(value = "20.0", message = "Contact hours cannot exceed 20.0")
    private BigDecimal contactHoursPerWeek;

    @DecimalMin(value = "0.0", message = "Theory hours cannot be negative")
    @DecimalMax(value = "20.0", message = "Theory hours cannot exceed 20.0")
    private BigDecimal theoryHours = new BigDecimal("0.0");

    @DecimalMin(value = "0.0", message = "Lab hours cannot be negative")
    @DecimalMax(value = "20.0", message = "Lab hours cannot exceed 20.0")
    private BigDecimal labHours = new BigDecimal("0.0");

    @NotNull(message = "Course level is required")
    private Course.CourseLevel level;

    @Min(value = 1, message = "Max students must be at least 1")
    @Max(value = 500, message = "Max students cannot exceed 500")
    private Integer maxStudents = 30;

    @Min(value = 1, message = "Min students must be at least 1")
    @Max(value = 500, message = "Min students cannot exceed 500")
    private Integer minStudents = 5;

    private Boolean requiresLab = false;

    private List<CoursePrerequisiteRequest> prerequisites;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoursePrerequisiteRequest {
        @NotNull(message = "Prerequisite course ID is required")
        private Long prerequisiteCourseId;

        private Boolean isMandatory = true;

        @Min(value = 0, message = "Minimum grade cannot be negative")
        @Max(value = 100, message = "Minimum grade cannot exceed 100")
        private Integer minimumGrade = 60;

        @Size(max = 500, message = "Notes must be less than 500 characters")
        private String notes;
    }
}