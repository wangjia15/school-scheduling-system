package com.school.scheduling.dto.response;

import com.school.scheduling.domain.TeacherSpecialization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSpecializationResponse {
    private Long id;
    private String subjectCode;
    private String subjectName;
    private TeacherSpecialization.ProficiencyLevel proficiencyLevel;
    private Integer yearsOfExperience;
    private Boolean certified;
    private String certificationDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TeacherSpecializationResponse fromEntity(TeacherSpecialization specialization) {
        if (specialization == null) return null;

        TeacherSpecializationResponse response = new TeacherSpecializationResponse();
        response.setId(specialization.getId());
        response.setSubjectCode(specialization.getSubjectCode());
        response.setSubjectName(specialization.getSubjectName());
        response.setProficiencyLevel(specialization.getProficiencyLevel());
        response.setYearsOfExperience(specialization.getYearsOfExperience());
        response.setCertified(specialization.getCertified());
        response.setCertificationDetails(specialization.getCertificationDetails());
        response.setCreatedAt(specialization.getCreatedAt());
        response.setUpdatedAt(specialization.getUpdatedAt());

        return response;
    }
}