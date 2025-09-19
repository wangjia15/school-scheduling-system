package com.school.scheduling.dto.response;

import com.school.scheduling.domain.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String courseCode;
    private String title;
    private String description;
    private String departmentName;
    private String departmentCode;
    private Integer credits;
    private BigDecimal contactHoursPerWeek;
    private BigDecimal theoryHours;
    private BigDecimal labHours;
    private Course.CourseLevel level;
    private Boolean isActive;
    private Integer maxStudents;
    private Integer minStudents;
    private Boolean requiresLab;
    private List<CoursePrerequisiteResponse> prerequisites;
    private List<CourseOfferingResponse> offerings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static CourseResponse fromEntity(Course course) {
        if (course == null) return null;

        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setCourseCode(course.getCourseCode());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setDepartmentName(course.getDepartmentName());
        response.setDepartmentCode(course.getDepartmentCode());
        response.setCredits(course.getCredits());
        response.setContactHoursPerWeek(course.getContactHoursPerWeek());
        response.setTheoryHours(course.getTheoryHours());
        response.setLabHours(course.getLabHours());
        response.setLevel(course.getLevel());
        response.setIsActive(course.getIsActive());
        response.setMaxStudents(course.getMaxStudents());
        response.setMinStudents(course.getMinStudents());
        response.setRequiresLab(course.getRequiresLab());
        response.setCreatedAt(course.getCreatedAt());
        response.setUpdatedAt(course.getUpdatedAt());
        response.setCreatedBy(course.getCreatedBy());
        response.setUpdatedBy(course.getUpdatedBy());

        if (course.getPrerequisites() != null) {
            response.setPrerequisites(course.getPrerequisites().stream()
                    .map(CoursePrerequisiteResponse::fromEntity)
                    .toList());
        }

        if (course.getOfferings() != null) {
            response.setOfferings(course.getOfferings().stream()
                    .map(CourseOfferingResponse::fromEntity)
                    .toList());
        }

        return response;
    }
}