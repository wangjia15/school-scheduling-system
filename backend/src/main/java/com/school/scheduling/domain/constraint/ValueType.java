package com.school.scheduling.domain.constraint;

/**
 * Types of values that can be assigned to scheduling variables.
 */
public enum ValueType {
    TIME_SLOT("Time Slot", "Specific time slot assignment"),
    CLASSROOM("Classroom", "Classroom assignment"),
    TEACHER("Teacher", "Teacher assignment"),
    DATE("Date", "Date assignment"),
    LOCATION("Location", "Location assignment"),
    DAY_OF_WEEK("Day of Week", "Day of week assignment"),
    TIME_PERIOD("Time Period", "Time period assignment"),
    COURSE_OFFERING("Course Offering", "Course offering assignment"),
    SEMESTER("Semester", "Semester assignment"),
    ROOM_TYPE("Room Type", "Room type assignment"),
    EQUIPMENT("Equipment", "Equipment requirement");

    private final String displayName;
    private final String description;

    ValueType(String displayName, String description) {
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