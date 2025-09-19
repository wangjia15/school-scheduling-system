package com.school.scheduling.domain.constraint;

/**
 * Types of constraints in the scheduling system.
 */
public enum ConstraintType {
    TEACHER_AVAILABILITY("Teacher Availability", "Ensures teachers are available during scheduled times"),
    TEACHER_WORKLOAD("Teacher Workload", "Limits maximum teaching hours per teacher"),
    CLASSROOM_CAPACITY("Classroom Capacity", "Ensures classroom capacity is not exceeded"),
    CLASSROOM_AVAILABILITY("Classroom Availability", "Ensures classrooms are available during scheduled times"),
    EQUIPMENT_REQUIREMENT("Equipment Requirement", "Ensures required equipment is available"),
    STUDENT_CONFLICT("Student Conflict", "Prevents student schedule conflicts"),
    PREREQUISITE_SATISFACTION("Prerequisite Satisfaction", "Ensures course prerequisites are met"),
    DEPARTMENT_POLICY("Department Policy", "Enforces department-specific scheduling rules"),
    PREFERRED_TIME_SLOT("Preferred Time Slot", "Optimizes for preferred scheduling times"),
    CONSECUTIVE_CLASS_LIMIT("Consecutive Class Limit", "Limits consecutive classes for students"),
    BACK_TO_BACK_CLASS_LIMIT("Back-to-Back Class Limit", "Limits back-to-back classes in different locations"),
    ROOM_TYPE_REQUIREMENT("Room Type Requirement", "Ensures appropriate room type for course"),
    TIME_PREFERENCE("Time Preference", "Optimizes for preferred scheduling times"),
    LOCATION_PROXIMITY("Location Proximity", "Optimizes for proximity between consecutive classes");

    private final String displayName;
    private final String description;

    ConstraintType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}