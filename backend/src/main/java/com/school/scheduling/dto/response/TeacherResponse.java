package com.school.scheduling.dto.response;

import com.school.scheduling.domain.Teacher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {
    private Long id;
    private String employeeId;
    private String fullName;
    private String email;
    private String departmentName;
    private Teacher.TeacherTitle title;
    private BigDecimal maxWeeklyHours;
    private Integer maxCoursesPerSemester;
    private String officeLocation;
    private String phone;
    private List<TeacherSpecializationResponse> specializations;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static TeacherResponse fromEntity(Teacher teacher) {
        if (teacher == null) return null;

        TeacherResponse response = new TeacherResponse();
        response.setId(teacher.getId());
        response.setEmployeeId(teacher.getEmployeeId());
        response.setFullName(teacher.getFullName());
        response.setEmail(teacher.getEmail());
        response.setDepartmentName(teacher.getDepartmentName());
        response.setTitle(teacher.getTitle());
        response.setMaxWeeklyHours(teacher.getMaxWeeklyHours());
        response.setMaxCoursesPerSemester(teacher.getMaxCoursesPerSemester());
        response.setOfficeLocation(teacher.getOfficeLocation());
        response.setPhone(teacher.getPhone());
        response.setActive(teacher.isActive());
        response.setCreatedAt(teacher.getCreatedAt());
        response.setUpdatedAt(teacher.getUpdatedAt());
        response.setCreatedBy(teacher.getCreatedBy());
        response.setUpdatedBy(teacher.getUpdatedBy());

        if (teacher.getSpecializations() != null) {
            response.setSpecializations(teacher.getSpecializations().stream()
                    .map(TeacherSpecializationResponse::fromEntity)
                    .toList());
        }

        return response;
    }
}