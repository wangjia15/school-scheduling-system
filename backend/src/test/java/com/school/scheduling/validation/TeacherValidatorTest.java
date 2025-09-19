package com.school.scheduling.validation;

import com.school.scheduling.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherValidatorTest {

    private TeacherValidator teacherValidator;

    @Mock
    private Teacher teacher;

    @Mock
    private User user;

    @Mock
    private Department department;

    @Mock
    private Course course;

    @Mock
    private Schedule schedule;

    @Mock
    private TimeSlot timeSlot;

    @BeforeEach
    void setUp() {
        teacherValidator = new TeacherValidator();
    }

    @Test
    void validateTeacherForCreation_WithValidTeacher_ShouldReturnValidResult() {
        when(teacher.getUser()).thenReturn(user);
        when(teacher.getDepartment()).thenReturn(department);
        when(teacher.getTitle()).thenReturn(Teacher.TeacherTitle.PROFESSOR);
        when(teacher.getEmployeeId()).thenReturn("EMP001");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("40.0"));
        when(teacher.getMaxCoursesPerSemester()).thenReturn(5);
        when(user.getEmail()).thenReturn("teacher@school.edu");

        ValidationResult result = teacherValidator.validateTeacherForCreation(teacher);

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
    }

    @Test
    void validateTeacherForCreation_WithNullTeacher_ShouldReturnInvalidResult() {
        ValidationResult result = teacherValidator.validateTeacherForCreation(null);

        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Teacher is required"));
    }

    @Test
    void validateTeacherForCreation_WithNullUser_ShouldReturnInvalidResult() {
        when(teacher.getDepartment()).thenReturn(department);
        when(teacher.getTitle()).thenReturn(Teacher.TeacherTitle.PROFESSOR);
        when(teacher.getEmployeeId()).thenReturn("EMP001");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("40.0"));
        when(teacher.getMaxCoursesPerSemester()).thenReturn(5);

        ValidationResult result = teacherValidator.validateTeacherForCreation(teacher);

        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("User is required"));
    }

    @Test
    void validateTeacherForCreation_WithInvalidEmployeeId_ShouldReturnInvalidResult() {
        when(teacher.getUser()).thenReturn(user);
        when(teacher.getDepartment()).thenReturn(department);
        when(teacher.getTitle()).thenReturn(Teacher.TeacherTitle.PROFESSOR);
        when(teacher.getEmployeeId()).thenReturn("");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("40.0"));
        when(teacher.getMaxCoursesPerSemester()).thenReturn(5);
        when(user.getEmail()).thenReturn("teacher@school.edu");

        ValidationResult result = teacherValidator.validateTeacherForCreation(teacher);

        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Employee ID is required"));
    }

    @Test
    void validateTeacherForCreation_WithExcessiveHours_ShouldReturnWarning() {
        when(teacher.getUser()).thenReturn(user);
        when(teacher.getDepartment()).thenReturn(department);
        when(teacher.getTitle()).thenReturn(Teacher.TeacherTitle.PROFESSOR);
        when(teacher.getEmployeeId()).thenReturn("EMP001");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("55.0"));
        when(teacher.getMaxCoursesPerSemester()).thenReturn(5);
        when(user.getEmail()).thenReturn("teacher@school.edu");

        ValidationResult result = teacherValidator.validateTeacherForCreation(teacher);

        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("very high")));
    }

    @Test
    void validateTeacherAvailability_WithNoConflicts_ShouldReturnValidResult() {
        when(teacher.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("teacher@school.edu");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("40.0"));

        Schedule mockSchedule = createMockSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0));
        List<Schedule> existingSchedules = Collections.singletonList(mockSchedule);

        ValidationResult result = teacherValidator.validateTeacherAvailability(
            teacher, existingSchedules, DayOfWeek.TUESDAY, LocalTime.of(14, 0), LocalTime.of(15, 0));

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }

    @Test
    void validateTeacherAvailability_WithTimeConflict_ShouldReturnInvalidResult() {
        when(teacher.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("teacher@school.edu");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("40.0"));

        Schedule conflictingSchedule = createMockSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        List<Schedule> existingSchedules = Collections.singletonList(conflictingSchedule);

        ValidationResult result = teacherValidator.validateTeacherAvailability(
            teacher, existingSchedules, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0));

        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("scheduling conflict")));
    }

    @Test
    void validateTeacherAvailability_WithWorkloadExceeded_ShouldReturnInvalidResult() {
        when(teacher.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("teacher@school.edu");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("10.0"));

        Schedule existingSchedule = createMockSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        List<Schedule> existingSchedules = Collections.singletonList(existingSchedule);

        ValidationResult result = teacherValidator.validateTeacherAvailability(
            teacher, existingSchedules, DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("exceed maximum weekly hours")));
    }

    @Test
    void validateTeacherCourseAssignment_WithValidAssignment_ShouldReturnValidResult() {
        when(teacher.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("teacher@school.edu");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("40.0"));
        when(teacher.getMaxCoursesPerSemester()).thenReturn(5);
        when(teacher.getTitle()).thenReturn(Teacher.TeacherTitle.PROFESSOR);

        TeacherSpecialization specialization = new TeacherSpecialization();
        specialization.setSubjectCode("CS");
        when(teacher.getSpecializations()).thenReturn(Collections.singletonList(specialization));

        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getContactHoursPerWeek()).thenReturn(new BigDecimal("3.0"));
        when(course.getDifficultyLevel()).thenReturn(1);

        ValidationResult result = teacherValidator.validateTeacherCourseAssignment(teacher, course);

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }

    @Test
    void validateTeacherCourseAssignment_WithInsufficientQualification_ShouldReturnInvalidResult() {
        when(teacher.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("teacher@school.edu");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("40.0"));
        when(teacher.getMaxCoursesPerSemester()).thenReturn(5);
        when(teacher.getTitle()).thenReturn(Teacher.TeacherTitle.PROFESSOR);

        TeacherSpecialization specialization = new TeacherSpecialization();
        specialization.setSubjectCode("MATH");
        when(teacher.getSpecializations()).thenReturn(Collections.singletonList(specialization));

        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getContactHoursPerWeek()).thenReturn(new BigDecimal("3.0"));
        when(course.getDifficultyLevel()).thenReturn(1);

        ValidationResult result = teacherValidator.validateTeacherCourseAssignment(teacher, course);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("does not have required specialization")));
    }

    @Test
    void validateTeacherCourseAssignment_WithPhdCourseByInstructor_ShouldReturnInvalidResult() {
        when(teacher.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("teacher@school.edu");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("40.0"));
        when(teacher.getMaxCoursesPerSemester()).thenReturn(5);
        when(teacher.getTitle()).thenReturn(Teacher.TeacherTitle.INSTRUCTOR);

        when(course.getCourseCode()).thenReturn("CS801");
        when(course.getContactHoursPerWeek()).thenReturn(new BigDecimal("3.0"));
        when(course.isPhD()).thenReturn(true);

        ValidationResult result = teacherValidator.validateTeacherCourseAssignment(teacher, course);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("cannot teach PhD courses")));
    }

    @Test
    void validateTeacherWorkload_WithReasonableWorkload_ShouldReturnValidResult() {
        when(teacher.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("teacher@school.edu");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("40.0"));
        when(teacher.getMaxCoursesPerSemester()).thenReturn(5);

        Schedule schedule = createMockSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0));
        List<Schedule> schedules = Collections.singletonList(schedule);

        ValidationResult result = teacherValidator.validateTeacherWorkload(teacher, schedules);

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }

    @Test
    void validateTeacherWorkload_WithExceededHours_ShouldReturnInvalidResult() {
        when(teacher.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("teacher@school.edu");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("10.0"));

        Schedule schedule = createMockSchedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        List<Schedule> schedules = Collections.singletonList(schedule);

        ValidationResult result = teacherValidator.validateTeacherWorkload(teacher, schedules);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("exceeds maximum weekly hours")));
    }

    @Test
    void validateTeacherSpecialization_WithValidSpecialization_ShouldReturnValidResult() {
        TeacherSpecialization specialization = new TeacherSpecialization();
        specialization.setSubjectCode("CS");
        specialization.setProficiencyLevel(TeacherSpecialization.ProficiencyLevel.ADVANCED);

        ValidationResult result = teacherValidator.validateTeacherSpecialization(specialization);

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }

    @Test
    void validateTeacherSpecialization_WithNullSubjectCode_ShouldReturnInvalidResult() {
        TeacherSpecialization specialization = new TeacherSpecialization();
        specialization.setProficiencyLevel(TeacherSpecialization.ProficiencyLevel.ADVANCED);

        ValidationResult result = teacherValidator.validateTeacherSpecialization(specialization);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Subject code is required"));
    }

    @Test
    void validateTeacherSpecialization_WithNullProficiencyLevel_ShouldReturnInvalidResult() {
        TeacherSpecialization specialization = new TeacherSpecialization();
        specialization.setSubjectCode("CS");

        ValidationResult result = teacherValidator.validateTeacherSpecialization(specialization);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Proficiency level is required"));
    }

    private Schedule createMockSchedule(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        Schedule schedule = new Schedule();
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setDayOfWeek(dayOfWeek);
        timeSlot.setStartTime(startTime);
        timeSlot.setEndTime(endTime);
        schedule.setTimeSlot(timeSlot);
        return schedule;
    }

    @Test
    void validateTeacherForUpdate_WithValidTeacher_ShouldReturnValidResult() {
        when(teacher.getUser()).thenReturn(user);
        when(teacher.getDepartment()).thenReturn(department);
        when(teacher.getTitle()).thenReturn(Teacher.TeacherTitle.PROFESSOR);
        when(teacher.getEmployeeId()).thenReturn("EMP001");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("40.0"));
        when(teacher.getMaxCoursesPerSemester()).thenReturn(5);
        when(user.getEmail()).thenReturn("teacher@school.edu");

        ValidationResult result = teacherValidator.validateTeacherForUpdate(teacher);

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }

    @Test
    void validateTeacherCourseAssignment_WithHighWorkload_ShouldReturnWarning() {
        when(teacher.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("teacher@school.edu");
        when(teacher.getMaxWeeklyHours()).thenReturn(new BigDecimal("40.0"));
        when(teacher.getMaxCoursesPerSemester()).thenReturn(5);
        when(teacher.getTitle()).thenReturn(Teacher.TeacherTitle.PROFESSOR);

        TeacherSpecialization specialization = new TeacherSpecialization();
        specialization.setSubjectCode("CS");
        when(teacher.getSpecializations()).thenReturn(Collections.singletonList(specialization));

        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getContactHoursPerWeek()).thenReturn(new BigDecimal("25.0"));
        when(course.getDifficultyLevel()).thenReturn(1);

        ValidationResult result = teacherValidator.validateTeacherCourseAssignment(teacher, course);

        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("more than 50%")));
    }
}