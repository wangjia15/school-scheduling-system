package com.school.scheduling.dto.request;

import com.school.scheduling.domain.Classroom;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomRequest {
    @NotBlank(message = "Building code is required")
    @Size(max = 20, message = "Building code must be less than 20 characters")
    private String buildingCode;

    @NotBlank(message = "Room number is required")
    @Size(max = 20, message = "Room number must be less than 20 characters")
    private String roomNumber;

    @NotBlank(message = "Classroom name is required")
    @Size(max = 100, message = "Classroom name must be less than 100 characters")
    private String name;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 500, message = "Capacity cannot exceed 500")
    private Integer capacity;

    @NotNull(message = "Room type is required")
    private Classroom.RoomType roomType;

    private Boolean hasProjector = true;

    private Boolean hasComputer = false;

    private Boolean hasWhiteboard = true;

    @Size(max = 1000, message = "Special equipment must be less than 1000 characters")
    private String specialEquipment;

    private Boolean isAvailable = true;
}