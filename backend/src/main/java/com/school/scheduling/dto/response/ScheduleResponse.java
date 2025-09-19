package com.school.scheduling.dto.response;

import com.school.scheduling.domain.Schedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Long id;
    private CourseOfferingResponse courseOffering;
    private ClassroomResponse classroom;
    private TimeSlotResponse timeSlot;
    private java.time.LocalDate scheduleDate;
    private Boolean isRecurring;
    private String recurrencePattern;
    private String notes;
    private String status;
    private String scheduleSummary;
    private String fullDescription;
    private Boolean isValidSchedule;
    private Long daysUntilSchedule;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static ScheduleResponse fromEntity(Schedule schedule) {
        if (schedule == null) return null;

        ScheduleResponse response = new ScheduleResponse();
        response.setId(schedule.getId());
        response.setCourseOffering(CourseOfferingResponse.fromEntity(schedule.getCourseOffering()));
        response.setClassroom(ClassroomResponse.fromEntity(schedule.getClassroom()));
        response.setTimeSlot(TimeSlotResponse.fromEntity(schedule.getTimeSlot()));
        response.setScheduleDate(schedule.getScheduleDate());
        response.setIsRecurring(schedule.getIsRecurring());
        response.setRecurrencePattern(schedule.getRecurrencePattern());
        response.setNotes(schedule.getNotes());
        response.setStatus(schedule.getStatus());
        response.setScheduleSummary(schedule.getScheduleSummary());
        response.setFullDescription(schedule.getFullDescription());
        response.setIsValidSchedule(schedule.isValidSchedule());
        response.setDaysUntilSchedule(schedule.getDaysUntilSchedule());
        response.setCreatedAt(schedule.getCreatedAt());
        response.setUpdatedAt(schedule.getUpdatedAt());
        response.setCreatedBy(schedule.getCreatedBy());
        response.setUpdatedBy(schedule.getUpdatedBy());

        return response;
    }
}