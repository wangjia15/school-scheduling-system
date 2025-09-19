package com.school.scheduling.dto.response;

import com.school.scheduling.domain.CoursePrerequisite;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoursePrerequisiteResponse {
    private Long id;
    private CourseResponse prerequisiteCourse;
    private Boolean isMandatory;
    private Integer minimumGrade;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CoursePrerequisiteResponse fromEntity(CoursePrerequisite prerequisite) {
        if (prerequisite == null) return null;

        CoursePrerequisiteResponse response = new CoursePrerequisiteResponse();
        response.setId(prerequisite.getId());
        response.setPrerequisiteCourse(CourseResponse.fromEntity(prerequisite.getPrerequisiteCourse()));
        response.setMandatory(prerequisite.getIsMandatory());
        response.setMinimumGrade(prerequisite.getMinimumGrade());
        response.setNotes(prerequisite.getNotes());
        response.setCreatedAt(prerequisite.getCreatedAt());
        response.setUpdatedAt(prerequisite.getUpdatedAt());

        return response;
    }
}