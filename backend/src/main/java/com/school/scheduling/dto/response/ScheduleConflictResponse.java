package com.school.scheduling.dto.response;

import com.school.scheduling.domain.ScheduleConflict;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleConflictResponse {
    private Long id;
    private ScheduleResponse schedule1;
    private ScheduleResponse schedule2;
    private ScheduleConflict.ConflictType conflictType;
    private String description;
    private ScheduleConflict.ResolutionStatus resolutionStatus;
    private String resolutionNotes;
    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ScheduleConflictResponse fromEntity(ScheduleConflict conflict) {
        if (conflict == null) return null;

        ScheduleConflictResponse response = new ScheduleConflictResponse();
        response.setId(conflict.getId());
        response.setSchedule1(ScheduleResponse.fromEntity(conflict.getSchedule1()));
        response.setSchedule2(ScheduleResponse.fromEntity(conflict.getSchedule2()));
        response.setConflictType(conflict.getConflictType());
        response.setDescription(conflict.getDescription());
        response.setResolutionStatus(conflict.getResolutionStatus());
        response.setResolutionNotes(conflict.getResolutionNotes());
        response.setDetectedAt(conflict.getDetectedAt());
        response.setResolvedAt(conflict.getResolvedAt());
        response.setResolvedBy(conflict.getResolvedBy());
        response.setCreatedAt(conflict.getCreatedAt());
        response.setUpdatedAt(conflict.getUpdatedAt());

        return response;
    }
}