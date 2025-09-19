package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "classrooms", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"building_code", "room_number"})
})
public class Classroom extends BaseEntity {

    @NotBlank(message = "Building code is required")
    @Size(max = 20, message = "Building code must be less than 20 characters")
    @Column(name = "building_code", nullable = false, length = 20)
    private String buildingCode;

    @NotBlank(message = "Room number is required")
    @Size(max = 20, message = "Room number must be less than 20 characters")
    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @NotBlank(message = "Classroom name is required")
    @Size(max = 100, message = "Classroom name must be less than 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 500, message = "Capacity cannot exceed 500")
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @NotNull(message = "Room type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 30)
    private RoomType roomType;

    @NotNull(message = "Projector availability is required")
    @Column(name = "has_projector", nullable = false)
    private Boolean hasProjector = true;

    @NotNull(message = "Computer availability is required")
    @Column(name = "has_computer", nullable = false)
    private Boolean hasComputer = false;

    @NotNull(message = "Whiteboard availability is required")
    @Column(name = "has_whiteboard", nullable = false)
    private Boolean hasWhiteboard = true;

    @Column(name = "special_equipment", columnDefinition = "TEXT")
    private String specialEquipment;

    @NotNull(message = "Availability status is required")
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    public enum RoomType {
        LECTURE_HALL, LABORATORY, SEMINAR_ROOM, COMPUTER_LAB, STUDIO, CONFERENCE_ROOM
    }

    public String getRoomCode() {
        return buildingCode + "-" + roomNumber;
    }

    public String getFullDisplayName() {
        return name + " (" + getRoomCode() + ")";
    }

    public boolean isAvailableForScheduling() {
        return isAvailable && isActive();
    }

    public boolean hasSufficientCapacity(int requiredCapacity) {
        return capacity >= requiredCapacity;
    }

    public boolean isLaboratory() {
        return roomType == RoomType.LABORATORY || roomType == RoomType.COMPUTER_LAB;
    }

    public boolean isLectureHall() {
        return roomType == RoomType.LECTURE_HALL;
    }

    public boolean isSuitableForLabWork() {
        return isLaboratory() || hasComputer;
    }

    public boolean hasRequiredEquipment(List<String> requiredEquipment) {
        if (requiredEquipment == null || requiredEquipment.isEmpty()) {
            return true;
        }

        List<String> availableEquipment = getEquipmentList();
        return requiredEquipment.stream().allMatch(availableEquipment::contains);
    }

    public List<String> getEquipmentList() {
        List<String> equipment = new java.util.ArrayList<>();

        if (hasProjector) {
            equipment.add("PROJECTOR");
        }
        if (hasComputer) {
            equipment.add("COMPUTER");
        }
        if (hasWhiteboard) {
            equipment.add("WHITEBOARD");
        }

        if (specialEquipment != null && !specialEquipment.trim().isEmpty()) {
            try {
                String[] specialItems = specialEquipment.split(",");
                Arrays.stream(specialItems)
                      .map(String::trim)
                      .map(String::toUpperCase)
                      .forEach(equipment::add);
            } catch (Exception e) {
                equipment.add("SPECIAL");
            }
        }

        return equipment;
    }

    public boolean isComputerLab() {
        return roomType == RoomType.COMPUTER_LAB;
    }

    public boolean isStudio() {
        return roomType == RoomType.STUDIO;
    }

    public boolean isConferenceRoom() {
        return roomType == RoomType.CONFERENCE_ROOM;
    }

    public boolean isLargeClassroom() {
        return capacity > 100;
    }

    public boolean isSmallClassroom() {
        return capacity <= 30;
    }

    public boolean isMediumClassroom() {
        return capacity > 30 && capacity <= 100;
    }

    public String getCapacityCategory() {
        if (isSmallClassroom()) return "SMALL";
        if (isMediumClassroom()) return "MEDIUM";
        return "LARGE";
    }

    public boolean supportsTechnology() {
        return hasProjector || hasComputer;
    }

    public boolean isTraditionalClassroom() {
        return !supportsTechnology() && hasWhiteboard;
    }

    public String getEquipmentSummary() {
        List<String> equipment = getEquipmentList();
        return equipment.stream().collect(Collectors.joining(", "));
    }

    public boolean canAccomodateCourseWithLab() {
        return isLaboratory() || hasComputer;
    }

    public boolean isSuitableForExams() {
        return isAvailable && capacity >= 20 && hasWhiteboard;
    }

    public boolean isSuitableForPresentation() {
        return hasProjector && capacity >= 15;
    }

    public boolean isSuitableForGroupWork() {
        return roomType == RoomType.SEMINAR_ROOM ||
               roomType == RoomType.CONFERENCE_ROOM ||
               (roomType == RoomType.LABORATORY && capacity <= 30);
    }

    public double getUtilizationRate(int enrolledStudents) {
        if (capacity == 0) return 0.0;
        return (double) enrolledStudents / capacity;
    }

    public boolean isWellUtilized(int enrolledStudents) {
        double rate = getUtilizationRate(enrolledStudents);
        return rate >= 0.6 && rate <= 0.9;
    }

    public boolean isUnderUtilized(int enrolledStudents) {
        return getUtilizationRate(enrolledStudents) < 0.6;
    }

    public boolean isOverUtilized(int enrolledStudents) {
        return getUtilizationRate(enrolledStudents) > 0.9;
    }
}