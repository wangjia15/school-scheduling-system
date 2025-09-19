package com.school.scheduling.dto.response;

import com.school.scheduling.domain.CourseOffering;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseOfferingResponse {
    private Long id;
    private String courseCode;
    private String courseTitle;
    private TeacherResponse teacher;
    private SemesterResponse semester;
    private String section;
    private Integer currentEnrollment;
    private Integer maxEnrollment;
    private String schedule;
    private String classroom;
    private Boolean isOpenForRegistration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseOfferingResponse fromEntity(CourseOffering offering) {
        if (offering == null) return null;

        CourseOfferingResponse response = new CourseOfferingResponse();
        response.setId(offering.getId());
        response.setCourseCode(offering.getCourseCode());
        response.setCourseTitle(offering.getCourseTitle());
        response.setTeacher(TeacherResponse.fromEntity(offering.getTeacher()));
        response.setSemester(SemesterResponse.fromEntity(offering.getSemester()));
        response.setSection(offering.getSection());
        response.setCurrentEnrollment(offering.getCurrentEnrollment());
        response.setMaxEnrollment(offering.getMaxEnrollment());
        response.setSchedule(offering.getSchedule());
        response.setClassroom(offering.getClassroom());
        response.setIsOpenForRegistration(offering.getIsOpenForRegistration());
        response.setCreatedAt(offering.getCreatedAt());
        response.setUpdatedAt(offering.getUpdatedAt());

        return response;
    }
}