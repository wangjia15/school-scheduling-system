package com.school.scheduling.dto.response;

import com.school.scheduling.domain.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotResponse {
    private Long id;
    private TimeSlot.DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String shortDayName;
    private String timeRange;
    private String slotDescription;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TimeSlotResponse fromEntity(TimeSlot timeSlot) {
        if (timeSlot == null) return null;

        TimeSlotResponse response = new TimeSlotResponse();
        response.setId(timeSlot.getId());
        response.setDayOfWeek(timeSlot.getDayOfWeek());
        response.setStartTime(timeSlot.getStartTime());
        response.setEndTime(timeSlot.getEndTime());
        response.setShortDayName(timeSlot.getShortDayName());
        response.setTimeRange(timeSlot.getTimeRange());
        response.setSlotDescription(timeSlot.getSlotDescription());
        response.setIsActive(timeSlot.getIsActive());
        response.setCreatedAt(timeSlot.getCreatedAt());
        response.setUpdatedAt(timeSlot.getUpdatedAt());

        return response;
    }
}