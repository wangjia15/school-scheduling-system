package com.school.scheduling.dto.response;

import com.school.scheduling.domain.Semester;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemesterResponse {
    private Long id;
    private String name;
    private String code;
    private String academicYear;
    private Semester.SemesterType semesterType;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SemesterResponse fromEntity(Semester semester) {
        if (semester == null) return null;

        SemesterResponse response = new SemesterResponse();
        response.setId(semester.getId());
        response.setName(semester.getName());
        response.setCode(semester.getCode());
        response.setAcademicYear(semester.getAcademicYear());
        response.setSemesterType(semester.getSemesterType());
        response.setStartDate(semester.getStartDate());
        response.setEndDate(semester.getEndDate());
        response.setIsActive(semester.getIsActive());
        response.setCreatedAt(semester.getCreatedAt());
        response.setUpdatedAt(semester.getUpdatedAt());

        return response;
    }
}