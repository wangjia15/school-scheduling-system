package com.school.scheduling.dto.request;

import com.school.scheduling.domain.Teacher;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherUpdateRequest {
    @Size(max = 20, message = "Employee ID must be less than 20 characters")
    private String employeeId;

    private Long departmentId;

    private Teacher.TeacherTitle title;

    @DecimalMin(value = "1.0", message = "Max weekly hours must be at least 1.0")
    @DecimalMax(value = "60.0", message = "Max weekly hours cannot exceed 60.0")
    private BigDecimal maxWeeklyHours;

    @Min(value = 1, message = "Max courses per semester must be at least 1")
    @Max(value = 10, message = "Max courses per semester cannot exceed 10")
    private Integer maxCoursesPerSemester;

    @Size(max = 100, message = "Office location must be less than 100 characters")
    private String officeLocation;

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phone;

    private List<TeacherRequest.TeacherSpecializationRequest> specializations;
}