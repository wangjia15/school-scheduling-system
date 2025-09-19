package com.school.scheduling.dto.response;

import com.school.scheduling.domain.Classroom;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomResponse {
    private Long id;
    private String buildingCode;
    private String roomNumber;
    private String name;
    private Integer capacity;
    private Classroom.RoomType roomType;
    private Boolean hasProjector;
    private Boolean hasComputer;
    private Boolean hasWhiteboard;
    private String specialEquipment;
    private Boolean isAvailable;
    private List<String> equipmentList;
    private String roomCode;
    private String fullDisplayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static ClassroomResponse fromEntity(Classroom classroom) {
        if (classroom == null) return null;

        ClassroomResponse response = new ClassroomResponse();
        response.setId(classroom.getId());
        response.setBuildingCode(classroom.getBuildingCode());
        response.setRoomNumber(classroom.getRoomNumber());
        response.setName(classroom.getName());
        response.setCapacity(classroom.getCapacity());
        response.setRoomType(classroom.getRoomType());
        response.setHasProjector(classroom.getHasProjector());
        response.setHasComputer(classroom.getHasComputer());
        response.setHasWhiteboard(classroom.getHasWhiteboard());
        response.setSpecialEquipment(classroom.getSpecialEquipment());
        response.setIsAvailable(classroom.getIsAvailable());
        response.setEquipmentList(classroom.getEquipmentList());
        response.setRoomCode(classroom.getRoomCode());
        response.setFullDisplayName(classroom.getFullDisplayName());
        response.setCreatedAt(classroom.getCreatedAt());
        response.setUpdatedAt(classroom.getUpdatedAt());
        response.setCreatedBy(classroom.getCreatedBy());
        response.setUpdatedBy(classroom.getUpdatedBy());

        return response;
    }
}