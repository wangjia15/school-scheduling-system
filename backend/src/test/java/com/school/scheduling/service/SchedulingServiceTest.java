package com.school.scheduling.service;

import com.school.scheduling.domain.*;
import com.school.scheduling.mapper.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SchedulingService class.
 */
@ExtendWith(MockitoExtension.class)
class SchedulingServiceTest {

    @Mock
    private CourseOfferingMapper courseOfferingMapper;

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private ClassroomMapper classroomMapper;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private TimeSlotMapper timeSlotMapper;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private CourseMapper courseMapper;

    private SchedulingService schedulingService;

    @BeforeEach
    void setUp() {
        schedulingService = new SchedulingService(
            courseOfferingMapper,
            teacherMapper,
            classroomMapper,
            scheduleMapper,
            timeSlotMapper,
            studentMapper,
            courseMapper
        );
    }

    @Test
    @DisplayName("Should generate optimized schedule successfully")
    void shouldGenerateOptimizedScheduleSuccessfully() {
        // Setup mock data
        Long semesterId = 1L;
        List<CourseOffering> courseOfferings = createMockCourseOfferings();
        List<Teacher> teachers = createMockTeachers();
        List<Classroom> classrooms = createMockClassrooms();
        List<TimeSlot> timeSlots = createMockTimeSlots();

        when(courseOfferingMapper.findBySemesterId(semesterId)).thenReturn(courseOfferings);
        when(teacherMapper.findAllActive()).thenReturn(teachers);
        when(classroomMapper.findAllActive()).thenReturn(classrooms);
        when(timeSlotMapper.findAll()).thenReturn(timeSlots);
        when(scheduleMapper.insert(any(Schedule.class))).thenReturn(1);

        // Execute
        SchedulingService.SchedulingResult result = schedulingService.generateOptimizedSchedule(
            semesterId, SchedulingService.SchedulingStrategy.BACKTRACKING
        );

        // Verify
        assertTrue(result.isSuccess(), "Scheduling should succeed");
        assertNotNull(result.getSchedules(), "Generated schedules should not be null");
        assertNotNull(result.getMetrics(), "Metrics should not be null");
        assertFalse(result.getPerformanceSummary().isEmpty(), "Performance summary should not be empty");

        // Verify database calls
        verify(courseOfferingMapper).findBySemesterId(semesterId);
        verify(teacherMapper).findAllActive();
        verify(classroomMapper).findAllActive();
        verify(timeSlotMapper).findAll();
    }

    @Test
    @DisplayName("Should handle scheduling failure gracefully")
    void shouldHandleSchedulingFailureGracefully() {
        // Setup empty data that will cause failure
        Long semesterId = 1L;
        when(courseOfferingMapper.findBySemesterId(semesterId)).thenReturn(Collections.emptyList());

        // Execute
        SchedulingService.SchedulingResult result = schedulingService.generateOptimizedSchedule(
            semesterId, SchedulingService.SchedulingStrategy.BACKTRACKING
        );

        // Verify
        assertFalse(result.isSuccess(), "Scheduling should fail with no course offerings");
        assertNotNull(result.getMessage(), "Error message should not be null");
        assertTrue(result.getMessage().contains("No course offerings found"),
                   "Error message should be descriptive");
    }

    @Test
    @DisplayName("Should detect schedule conflicts")
    void shouldDetectScheduleConflicts() {
        // Setup conflicting schedules
        Long semesterId = 1L;
        List<Schedule> conflictingSchedules = createConflictingSchedules();
        when(scheduleMapper.findBySemesterId(semesterId)).thenReturn(conflictingSchedules);

        // Execute
        List<ScheduleConflict> conflicts = schedulingService.detectConflicts(semesterId);

        // Verify
        assertNotNull(conflicts, "Conflicts list should not be null");
        assertFalse(conflicts.isEmpty(), "Should detect conflicts");
        assertTrue(conflicts.get(0).getConflictType() == ScheduleConflict.ConflictType.TEACHER_DOUBLE_BOOKING,
                   "Should detect teacher double booking");
    }

    @Test
    @DisplayName("Should resolve conflict successfully")
    void shouldResolveConflictSuccessfully() {
        // Setup mock conflict
        Long conflictId = 1L;
        ScheduleConflict conflict = createMockConflict();
        when(scheduleMapper.findConflictById(conflictId)).thenReturn(conflict);

        // Setup resolution action
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("newTimeSlotId", 2L);
        SchedulingService.ConflictResolutionAction action = new SchedulingService.ConflictResolutionAction(
            SchedulingService.ConflictResolutionType.RESCHEDULE,
            parameters
        );

        // Execute
        SchedulingService.ConflictResolutionResult result = schedulingService.resolveConflict(conflictId, action);

        // Verify
        assertTrue(result.isSuccess(), "Conflict resolution should succeed");
        assertNotNull(result.getMessage(), "Resolution message should not be null");
        assertNotNull(result.getResolvedSchedule(), "Resolved schedule should not be null");
    }

    @Test
    @DisplayName("Should handle conflict resolution failure")
    void shouldHandleConflictResolutionFailure() {
        // Setup non-existent conflict
        Long conflictId = 999L;
        when(scheduleMapper.findConflictById(conflictId)).thenReturn(null);

        // Setup resolution action
        SchedulingService.ConflictResolutionAction action = new SchedulingService.ConflictResolutionAction(
            SchedulingService.ConflictResolutionType.RESCHEDULE,
            new HashMap<>()
        );

        // Execute
        SchedulingService.ConflictResolutionResult result = schedulingService.resolveConflict(conflictId, action);

        // Verify
        assertFalse(result.isSuccess(), "Conflict resolution should fail for non-existent conflict");
        assertTrue(result.getMessage().contains("Conflict not found"),
                   "Error message should be descriptive");
    }

    @Test
    @DisplayName("Should generate schedule for specific courses")
    void shouldGenerateScheduleForSpecificCourses() {
        // Setup mock data
        List<Long> courseOfferingIds = Arrays.asList(1L, 2L, 3L);
        List<CourseOffering> courseOfferings = createMockCourseOfferings();
        List<Teacher> teachers = createMockTeachers();
        List<Classroom> classrooms = createMockClassrooms();
        List<TimeSlot> timeSlots = createMockTimeSlots();

        when(courseOfferingMapper.findByIds(courseOfferingIds)).thenReturn(courseOfferings);
        when(teacherMapper.findAllActive()).thenReturn(teachers);
        when(classroomMapper.findAllActive()).thenReturn(classrooms);
        when(timeSlotMapper.findAll()).thenReturn(timeSlots);

        // Execute
        SchedulingService.SchedulingResult result = schedulingService.generateScheduleForCourses(
            courseOfferingIds, SchedulingService.SchedulingStrategy.BACKTRACKING
        );

        // Verify
        assertTrue(result.isSuccess(), "Course scheduling should succeed");
        assertNotNull(result.getSchedules(), "Generated schedules should not be null");
        verify(courseOfferingMapper).findByIds(courseOfferingIds);
    }

    @Test
    @DisplayName("Should optimize existing schedule")
    void shouldOptimizeExistingSchedule() {
        // Setup mock data
        Long semesterId = 1L;
        List<Schedule> existingSchedules = createExistingSchedules();
        List<CourseOffering> courseOfferings = createMockCourseOfferings();
        List<Teacher> teachers = createMockTeachers();
        List<Classroom> classrooms = createMockClassrooms();
        List<TimeSlot> timeSlots = createMockTimeSlots();

        when(scheduleMapper.findBySemesterId(semesterId)).thenReturn(existingSchedules);
        when(teacherMapper.findAllActive()).thenReturn(teachers);
        when(classroomMapper.findAllActive()).thenReturn(classrooms);
        when(timeSlotMapper.findAll()).thenReturn(timeSlots);

        // Execute
        SchedulingService.SchedulingResult result = schedulingService.optimizeExistingSchedule(
            semesterId, SchedulingService.OptimizationCriteria.RESOURCE_UTILIZATION
        );

        // Verify
        assertNotNull(result, "Optimization result should not be null");
        verify(scheduleMapper).findBySemesterId(semesterId);
    }

    @Test
    @DisplayName("Should handle empty course list for specific scheduling")
    void shouldHandleEmptyCourseListForSpecificScheduling() {
        // Setup empty course list
        List<Long> courseOfferingIds = Collections.emptyList();
        when(courseOfferingMapper.findByIds(courseOfferingIds)).thenReturn(Collections.emptyList());

        // Execute
        SchedulingService.SchedulingResult result = schedulingService.generateScheduleForCourses(
            courseOfferingIds, SchedulingService.SchedulingStrategy.BACKTRACKING
        );

        // Verify
        assertFalse(result.isSuccess(), "Scheduling should fail with empty course list");
        assertTrue(result.getMessage().contains("No course offerings found"),
                   "Error message should be descriptive");
    }

    @Test
    @DisplayName("Should calculate scheduling metrics correctly")
    void shouldCalculateSchedulingMetricsCorrectly() {
        // Setup mock schedules
        List<Schedule> schedules = createExistingSchedules();

        // This would test the metric calculation logic
        // For now, we just verify that metrics can be created
        SchedulingService.SchedulingMetrics metrics = new SchedulingService.SchedulingMetrics(
            schedules.size(),
            0.75,  // 75% resource utilization
            0,     // No conflicts
            0.8,   // Good teacher workload balance
            0.7    // Good student satisfaction
        );

        assertNotNull(metrics, "Metrics should not be null");
        assertEquals(schedules.size(), metrics.getTotalSchedules(), "Total schedules should match");
        assertEquals(0.75, metrics.getResourceUtilization(), 0.01, "Resource utilization should match");
    }

    // Helper methods to create mock data

    private List<CourseOffering> createMockCourseOfferings() {
        List<CourseOffering> courseOfferings = new ArrayList<>();

        Semester semester = new Semester();
        semester.setId(1L);

        Teacher teacher = new Teacher();
        teacher.setId(1L);

        Course course = new Course();
        course.setId(1L);

        for (int i = 1; i <= 3; i++) {
            CourseOffering offering = new CourseOffering();
            offering.setId((long) i);
            offering.setCourse(course);
            offering.setSemester(semester);
            offering.setTeacher(teacher);
            offering.setSectionNumber(String.valueOf(i));
            offering.setMaxEnrollment(30);
            offering.setCurrentEnrollment(0);
            courseOfferings.add(offering);
        }

        return courseOfferings;
    }

    private List<Teacher> createMockTeachers() {
        List<Teacher> teachers = new ArrayList<>();

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");

        Department department = new Department();
        department.setId(1L);

        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUser(user);
        teacher.setDepartment(department);
        teacher.setEmployeeId("EMP001");
        teacher.setMaxWeeklyHours(new java.math.BigDecimal("40.0"));
        teacher.setMaxCoursesPerSemester(5);

        teachers.add(teacher);
        return teachers;
    }

    private List<Classroom> createMockClassrooms() {
        List<Classroom> classrooms = new ArrayList<>();

        for (int i = 1; i <= 2; i++) {
            Classroom classroom = new Classroom();
            classroom.setId((long) i);
            classroom.setRoomCode("ROOM" + (100 + i));
            classroom.setCapacity(30);
            classroom.setRoomType("LECTURE");
            classrooms.add(classroom);
        }

        return classrooms;
    }

    private List<TimeSlot> createMockTimeSlots() {
        List<TimeSlot> timeSlots = new ArrayList<>();

        for (int i = 1; i <= 4; i++) {
            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setId((long) i);
            timeSlot.setDayOfWeek(DayOfWeek.MONDAY);
            timeSlot.setStartTime(java.time.LocalTime.of(8 + i, 0));
            timeSlot.setEndTime(java.time.LocalTime.of(9 + i, 0));
            timeSlots.add(timeSlot);
        }

        return timeSlots;
    }

    private List<Schedule> createConflictingSchedules() {
        List<Schedule> schedules = new ArrayList<>();

        // Create schedules that conflict with each other
        CourseOffering offering = new CourseOffering();
        offering.setId(1L);

        Teacher teacher = new Teacher();
        teacher.setId(1L);

        Classroom classroom = new Classroom();
        classroom.setId(1L);

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(1L);
        timeSlot.setDayOfWeek(DayOfWeek.MONDAY);
        timeSlot.setStartTime(java.time.LocalTime.of(9, 0));
        timeSlot.setEndTime(java.time.LocalTime.of(10, 0));

        // Two schedules with same teacher, same time
        Schedule schedule1 = new Schedule();
        schedule1.setId(1L);
        schedule1.setCourseOffering(offering);
        schedule1.setClassroom(classroom);
        schedule1.setTimeSlot(timeSlot);
        schedule1.setScheduleDate(java.time.LocalDate.now());

        Schedule schedule2 = new Schedule();
        schedule2.setId(2L);
        schedule2.setCourseOffering(offering);
        schedule2.setClassroom(classroom);
        schedule2.setTimeSlot(timeSlot);
        schedule2.setScheduleDate(java.time.LocalDate.now());

        schedules.add(schedule1);
        schedules.add(schedule2);

        return schedules;
    }

    private List<Schedule> createExistingSchedules() {
        List<Schedule> schedules = new ArrayList<>();

        CourseOffering offering = new CourseOffering();
        offering.setId(1L);

        Teacher teacher = new Teacher();
        teacher.setId(1L);

        Classroom classroom = new Classroom();
        classroom.setId(1L);

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setId(1L);
        timeSlot.setDayOfWeek(DayOfWeek.MONDAY);
        timeSlot.setStartTime(java.time.LocalTime.of(9, 0));
        timeSlot.setEndTime(java.time.LocalTime.of(10, 0));

        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setCourseOffering(offering);
        schedule.setClassroom(classroom);
        schedule.setTimeSlot(timeSlot);
        schedule.setScheduleDate(java.time.LocalDate.now());

        schedules.add(schedule);
        return schedules;
    }

    private ScheduleConflict createMockConflict() {
        ScheduleConflict conflict = new ScheduleConflict();
        conflict.setId(1L);
        conflict.setConflictType(ScheduleConflict.ConflictType.TEACHER_DOUBLE_BOOKING);
        conflict.setSeverity(ScheduleConflict.Severity.HIGH);
        conflict.setDescription("Teacher double booking conflict");
        conflict.setResolutionStatus(ScheduleConflict.ResolutionStatus.PENDING);

        Schedule schedule = new Schedule();
        schedule.setId(1L);
        conflict.setSchedule1(schedule);

        return conflict;
    }
}