package com.school.scheduling.dto.response;

import com.school.scheduling.domain.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String headOfDepartment;
    private String contactEmail;
    private String contactPhone;
    private String location;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DepartmentResponse fromEntity(Department department) {
        if (department == null) return null;

        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setCode(department.getCode());
        response.setName(department.getName());
        response.setDescription(department.getDescription());
        response.setHeadOfDepartment(department.getHeadOfDepartment());
        response.setContactEmail(department.getContactEmail());
        response.setContactPhone(department.getContactPhone());
        response.setLocation(department.getLocation());
        response.setIsActive(department.getIsActive());
        response.setCreatedAt(department.getCreatedAt());
        response.setUpdatedAt(department.getUpdatedAt());

        return response;
    }
}