package com.school.scheduling.domain.constraint;

/**
 * Types of scheduling variables in the CSP.
 */
public enum VariableType {
    TEACHER_ASSIGNMENT("Teacher Assignment", "Assigns a teacher to a course offering"),
    CLASSROOM_ASSIGNMENT("Classroom Assignment", "Assigns a classroom to a course offering"),
    TIME_SLOT_ASSIGNMENT("Time Slot Assignment", "Assigns a time slot to a course offering"),
    COURSE_SCHEDULING("Course Scheduling", "Schedules a course offering in the timetable"),
    STUDENT_ENROLLMENT("Student Enrollment", "Enrolls students in course offerings"),
    ROOM_ALLOCATION("Room Allocation", "Allocates rooms for specific time periods"),
    TEACHER_SCHEDULE("Teacher Schedule", "Manages teacher's weekly schedule"),
    CLASSROOM_SCHEDULE("Classroom Schedule", "Manages classroom's weekly schedule");

    private final String displayName;
    private final String description;

    VariableType(String displayName, String description) {
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