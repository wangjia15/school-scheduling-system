package com.school.scheduling.algorithm.constraint;

import com.school.scheduling.domain.constraint.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TeacherAvailabilityConstraint class.
 */
class TeacherAvailabilityConstraintTest {

    private Map<Long, TeacherAvailabilityConstraint.TeacherAvailability> teacherAvailabilities;
    private TeacherAvailabilityConstraint constraint;

    @BeforeEach
    void setUp() {
        // Create teacher availability data
        teacherAvailabilities = new HashMap<>();

        // Teacher 1: Available Monday-Wednesday 9 AM - 5 PM
        Map<DayOfWeek, List<TeacherAvailabilityConstraint.TimeRange>> teacher1Availability = new HashMap<>();
        teacher1Availability.put(DayOfWeek.MONDAY, Arrays.asList(
            new TeacherAvailabilityConstraint.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))
        ));
        teacher1Availability.put(DayOfWeek.TUESDAY, Arrays.asList(
            new TeacherAvailabilityConstraint.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))
        ));
        teacher1Availability.put(DayOfWeek.WEDNESDAY, Arrays.asList(
            new TeacherAvailabilityConstraint.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))
        ));

        teacherAvailabilities.put(1L, new TeacherAvailabilityConstraint.TeacherAvailability(
            1L, teacher1Availability, 40.0, 5
        ));

        // Teacher 2: Available Thursday-Friday 10 AM - 6 PM
        Map<DayOfWeek, List<TeacherAvailabilityConstraint.TimeRange>> teacher2Availability = new HashMap<>();
        teacher2Availability.put(DayOfWeek.THURSDAY, Arrays.asList(
            new TeacherAvailabilityConstraint.TimeRange(LocalTime.of(10, 0), LocalTime.of(18, 0))
        ));
        teacher2Availability.put(DayOfWeek.FRIDAY, Arrays.asList(
            new TeacherAvailabilityConstraint.TimeRange(LocalTime.of(10, 0), LocalTime.of(18, 0))
        ));

        teacherAvailabilities.put(2L, new TeacherAvailabilityConstraint.TeacherAvailability(
            2L, teacher2Availability, 35.0, 4
        ));

        constraint = new TeacherAvailabilityConstraint(teacherAvailabilities, 4, 10);
    }

    @Test
    @DisplayName("Should validate teacher availability correctly")
    void shouldValidateTeacherAvailabilityCorrectly() {
        SchedulingAssignment assignment = createValidAssignment();

        ConstraintResult result = constraint.validate(assignment);
        assertTrue(result.isSatisfied(), "Valid assignment should satisfy constraint");
    }

    @Test
    @DisplayName("Should detect teacher availability conflicts")
    void shouldDetectTeacherAvailabilityConflicts() {
        SchedulingAssignment assignment = createInvalidAvailabilityAssignment();

        ConstraintResult result = constraint.validate(assignment);
        assertFalse(result.isSatisfied(), "Invalid availability should violate constraint");
        assertTrue(result.getMessage().contains("not available"),
                   "Error message should mention availability issue");
    }

    @Test
    @DisplayName("Should detect teacher workload exceedance")
    void shouldDetectTeacherWorkloadExceedance() {
        SchedulingAssignment assignment = createOverloadedAssignment();

        ConstraintResult result = constraint.validate(assignment);
        assertFalse(result.isSatisfied(), "Overloaded assignment should violate constraint");
        assertTrue(result.getMessage().contains("exceeds maximum weekly hours"),
                   "Error message should mention workload issue");
    }

    @Test
    @DisplayName("Should detect course limit exceedance")
    void shouldDetectCourseLimitExceedance() {
        SchedulingAssignment assignment = createCourseLimitExceededAssignment();

        ConstraintResult result = constraint.validate(assignment);
        assertFalse(result.isSatisfied(), "Course limit exceeded assignment should violate constraint");
        assertTrue(result.getMessage().contains("exceeds maximum courses per semester"),
                   "Error message should mention course limit issue");
    }

    @Test
    @DisplayName("Should detect consecutive hours violation")
    void shouldDetectConsecutiveHoursViolation() {
        TeacherAvailabilityConstraint strictConstraint = new TeacherAvailabilityConstraint(
            teacherAvailabilities, 3, 10
        );

        SchedulingAssignment assignment = createConsecutiveHoursAssignment();

        ConstraintResult result = strictConstraint.validate(assignment);
        assertFalse(result.isSatisfied(), "Consecutive hours should violate constraint");
        assertTrue(result.getMessage().contains("consecutive hours"),
                   "Error message should mention consecutive hours");
    }

    @Test
    @DisplayName("Should detect insufficient break time")
    void shouldDetectInsufficientBreakTime() {
        TeacherAvailabilityConstraint strictConstraint = new TeacherAvailabilityConstraint(
            teacherAvailabilities, 4, 15
        );

        SchedulingAssignment assignment = createInsufficientBreakAssignment();

        ConstraintResult result = strictConstraint.validate(assignment);
        assertFalse(result.isSatisfied(), "Insufficient break should violate constraint");
        assertTrue(result.getMessage().contains("Insufficient break time"),
                   "Error message should mention break time");
    }

    @Test
    @DisplayName("Should filter valid values correctly")
    void shouldFilterValidValuesCorrectly() {
        SchedulingAssignment assignment = new SchedulingAssignment();
        SchedulingVariable variable = new SchedulingVariable(
            VariableType.TEACHER_ASSIGNMENT,
            "teacher_1",
            "Teacher Assignment"
        );

        List<SchedulingValue> allValues = Arrays.asList(
            new SchedulingValue(ValueType.TIME_SLOT, "monday_9am", "Monday 9 AM"),
            new SchedulingValue(ValueType.TIME_SLOT, "sunday_9am", "Sunday 9 AM")
        );

        Map<SchedulingVariable, List<SchedulingValue>> domains = new HashMap<>();
        domains.put(variable, allValues);

        List<SchedulingValue> validValues = constraint.getValidValues(assignment, domains);

        // Should filter out Sunday slot for Teacher 1
        assertFalse(validValues.stream().anyMatch(v -> v.getDisplayName().contains("Sunday")),
                   "Should filter out unavailable time slots");
    }

    @Test
    @DisplayName("Should return all variables in scope")
    void shouldReturnAllVariablesInScope() {
        Set<SchedulingVariable> scope = constraint.getScope();
        assertNotNull(scope, "Scope should not be null");
    }

    // Helper methods to create test assignments

    private SchedulingAssignment createValidAssignment() {
        SchedulingAssignment assignment = new SchedulingAssignment();

        // Add valid schedule for Teacher 1
        TeacherAvailabilityConstraint.ScheduledClass validClass = new TeacherAvailabilityConstraint.ScheduledClass(
            "course_101", 1L, "room_101", DayOfWeek.MONDAY,
            LocalTime.of(9, 0), LocalTime.of(10, 30), 1.5
        );

        assignment.addMetadata("teacher_1_schedules", Arrays.asList(validClass));
        return assignment;
    }

    private SchedulingAssignment createInvalidAvailabilityAssignment() {
        SchedulingAssignment assignment = new SchedulingAssignment();

        // Add invalid schedule for Teacher 1 (not available on Sunday)
        TeacherAvailabilityConstraint.ScheduledClass invalidClass = new TeacherAvailabilityConstraint.ScheduledClass(
            "course_101", 1L, "room_101", DayOfWeek.SUNDAY,
            LocalTime.of(9, 0), LocalTime.of(10, 30), 1.5
        );

        assignment.addMetadata("teacher_1_schedules", Arrays.asList(invalidClass));
        return assignment;
    }

    private SchedulingAssignment createOverloadedAssignment() {
        SchedulingAssignment assignment = new SchedulingAssignment();

        // Add many classes to exceed 40-hour limit
        List<TeacherAvailabilityConstraint.ScheduledClass> classes = new ArrayList<>();
        for (int i = 0; i < 30; i++) { // 30 classes * 2 hours = 60 hours
            TeacherAvailabilityConstraint.ScheduledClass overloadedClass = new TeacherAvailabilityConstraint.ScheduledClass(
                "course_" + i, 1L, "room_101", DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 0), 2.0
            );
            classes.add(overloadedClass);
        }

        assignment.addMetadata("teacher_1_schedules", classes);
        return assignment;
    }

    private SchedulingAssignment createCourseLimitExceededAssignment() {
        SchedulingAssignment assignment = new SchedulingAssignment();

        // Add many different courses to exceed 5-course limit
        List<TeacherAvailabilityConstraint.ScheduledClass> classes = new ArrayList<>();
        for (int i = 0; i < 8; i++) { // 8 different courses
            TeacherAvailabilityConstraint.ScheduledClass courseClass = new TeacherAvailabilityConstraint.ScheduledClass(
                "course_" + i, 1L, "room_101", DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 1.0
            );
            classes.add(courseClass);
        }

        assignment.addMetadata("teacher_1_schedules", classes);
        return assignment;
    }

    private SchedulingAssignment createConsecutiveHoursAssignment() {
        SchedulingAssignment assignment = new SchedulingAssignment();

        // Add consecutive classes exceeding 4-hour limit
        List<TeacherAvailabilityConstraint.ScheduledClass> consecutiveClasses = Arrays.asList(
            new TeacherAvailabilityConstraint.ScheduledClass(
                "course_101", 1L, "room_101", DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 0), 2.0
            ),
            new TeacherAvailabilityConstraint.ScheduledClass(
                "course_102", 1L, "room_102", DayOfWeek.MONDAY,
                LocalTime.of(11, 0), LocalTime.of(13, 0), 2.0
            ),
            new TeacherAvailabilityConstraint.ScheduledClass(
                "course_103", 1L, "room_103", DayOfWeek.MONDAY,
                LocalTime.of(13, 0), LocalTime.of(15, 0), 2.0
            )
        );

        assignment.addMetadata("teacher_1_schedules", consecutiveClasses);
        return assignment;
    }

    private SchedulingAssignment createInsufficientBreakAssignment() {
        SchedulingAssignment assignment = new SchedulingAssignment();

        // Add classes with insufficient break time
        List<TeacherAvailabilityConstraint.ScheduledClass> tightSchedule = Arrays.asList(
            new TeacherAvailabilityConstraint.ScheduledClass(
                "course_101", 1L, "room_101", DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 50), 1.83
            ),
            new TeacherAvailabilityConstraint.ScheduledClass(
                "course_102", 1L, "room_102", DayOfWeek.MONDAY,
                LocalTime.of(11, 0), LocalTime.of(12, 50), 1.83
            )
        );

        assignment.addMetadata("teacher_1_schedules", tightSchedule);
        return assignment;
    }
}