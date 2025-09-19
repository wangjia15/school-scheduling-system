package com.school.scheduling.service;

import com.school.scheduling.domain.Teacher;
import com.school.scheduling.domain.TeacherSpecialization;
import com.school.scheduling.domain.TeacherAvailability;
import com.school.scheduling.domain.Department;
import com.school.scheduling.domain.User;
import com.school.scheduling.mapper.TeacherMapper;
import com.school.scheduling.mapper.TeacherAvailabilityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private TeacherAvailabilityMapper teacherAvailabilityMapper;

    @InjectMocks
    private TeacherService teacherService;

    private Teacher testTeacher;
    private TeacherAvailability testAvailability;

    @BeforeEach
    void setUp() {
        // Setup test data
        User testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@university.edu");

        Department testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setName("Computer Science");

        testTeacher = new Teacher();
        testTeacher.setId(1L);
        testTeacher.setUser(testUser);
        testTeacher.setEmployeeId("EMP001");
        testTeacher.setDepartment(testDepartment);
        testTeacher.setTitle(Teacher.TeacherTitle.PROFESSOR);
        testTeacher.setMaxWeeklyHours(new BigDecimal("40.0"));
        testTeacher.setMaxCoursesPerSemester(5);

        testAvailability = new TeacherAvailability();
        testAvailability.setId(1L);
        testAvailability.setTeacher(testTeacher);
        testAvailability.setDayOfWeek(DayOfWeek.MONDAY);
        testAvailability.setStartTime(LocalTime.of(9, 0));
        testAvailability.setEndTime(LocalTime.of(12, 0));
        testAvailability.setAvailabilityType(TeacherAvailability.AvailabilityType.PREFERRED);
        testAvailability.setMaxClasses(2);
    }

    @Test
    void testGetTeacherById_Success() {
        when(teacherMapper.findById(1L)).thenReturn(Optional.of(testTeacher));

        Teacher result = teacherService.getTeacherById(1L);

        assertNotNull(result);
        assertEquals("EMP001", result.getEmployeeId());
        assertEquals("John Doe", result.getFullName());
        verify(teacherMapper, times(1)).findById(1L);
    }

    @Test
    void testGetTeacherById_NotFound() {
        when(teacherMapper.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            teacherService.getTeacherById(999L);
        });
    }

    @Test
    void testGetAllTeachers() {
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherMapper.findAll()).thenReturn(teachers);

        List<Teacher> result = teacherService.getAllTeachers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EMP001", result.get(0).getEmployeeId());
    }

    @Test
    void testCreateTeacher_Success() {
        when(teacherMapper.findByEmployeeId("EMP001")).thenReturn(null);
        when(teacherMapper.findByUserId(1L)).thenReturn(null);
        when(teacherMapper.insert(any(Teacher.class))).thenReturn(1);
        when(teacherMapper.findById(1L)).thenReturn(Optional.of(testTeacher));

        Teacher result = teacherService.createTeacher(testTeacher);

        assertNotNull(result);
        assertEquals("EMP001", result.getEmployeeId());
        verify(teacherMapper, times(1)).insert(any(Teacher.class));
    }

    @Test
    void testCreateTeacher_DuplicateEmployeeId() {
        when(teacherMapper.findByEmployeeId("EMP001")).thenReturn(Optional.of(testTeacher));

        assertThrows(RuntimeException.class, () -> {
            teacherService.createTeacher(testTeacher);
        });
    }

    @Test
    void testUpdateTeacher_Success() {
        when(teacherMapper.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherMapper.update(any(Teacher.class))).thenReturn(1);
        when(teacherMapper.findById(1L)).thenReturn(Optional.of(testTeacher));

        Teacher updatedTeacher = testTeacher;
        updatedTeacher.setOfficeLocation("Building B, Room 202");

        Teacher result = teacherService.updateTeacher(1L, updatedTeacher);

        assertNotNull(result);
        verify(teacherMapper, times(1)).update(any(Teacher.class));
    }

    @Test
    void testUpdateTeacher_NotFound() {
        when(teacherMapper.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            teacherService.updateTeacher(999L, testTeacher);
        });
    }

    @Test
    void testDeleteTeacher_Success() {
        when(teacherMapper.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherMapper.hasActiveSchedules(1L)).thenReturn(false);
        when(teacherMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> {
            teacherService.deleteTeacher(1L);
        });

        verify(teacherMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTeacher_HasActiveSchedules() {
        when(teacherMapper.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherMapper.hasActiveSchedules(1L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            teacherService.deleteTeacher(1L);
        });
    }

    @Test
    void testGetTeacherAvailability_Success() {
        List<TeacherAvailability> availabilities = Arrays.asList(testAvailability);
        when(teacherAvailabilityMapper.findByTeacherId(1L)).thenReturn(availabilities);

        List<TeacherAvailability> result = teacherService.getTeacherAvailability(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(DayOfWeek.MONDAY, result.get(0).getDayOfWeek());
    }

    @Test
    void testAddTeacherAvailability_Success() {
        when(teacherMapper.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherAvailabilityMapper.findOverlappingAvailability(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(teacherAvailabilityMapper.insert(any(TeacherAvailability.class))).thenReturn(1);
        when(teacherAvailabilityMapper.findById(1L)).thenReturn(Optional.of(testAvailability));

        TeacherAvailability result = teacherService.addTeacherAvailability(1L, testAvailability);

        assertNotNull(result);
        assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek());
        verify(teacherAvailabilityMapper, times(1)).insert(any(TeacherAvailability.class));
    }

    @Test
    void testAddTeacherAvailability_TeacherNotFound() {
        when(teacherMapper.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            teacherService.addTeacherAvailability(999L, testAvailability);
        });
    }

    @Test
    void testAddTeacherAvailability_OverlappingTime() {
        when(teacherMapper.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherAvailabilityMapper.findOverlappingAvailability(any(), any(), any(), any())).thenReturn(Arrays.asList(testAvailability));

        assertThrows(RuntimeException.class, () -> {
            teacherService.addTeacherAvailability(1L, testAvailability);
        });
    }

    @Test
    void testUpdateTeacherAvailability_Success() {
        when(teacherMapper.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherAvailabilityMapper.findById(1L)).thenReturn(Optional.of(testAvailability));
        when(teacherAvailabilityMapper.findOverlappingAvailability(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(teacherAvailabilityMapper.update(any(TeacherAvailability.class))).thenReturn(1);
        when(teacherAvailabilityMapper.findById(1L)).thenReturn(Optional.of(testAvailability));

        TeacherAvailability updatedAvailability = testAvailability;
        updatedAvailability.setNotes("Updated notes");

        TeacherAvailability result = teacherService.updateTeacherAvailability(1L, 1L, updatedAvailability);

        assertNotNull(result);
        verify(teacherAvailabilityMapper, times(1)).update(any(TeacherAvailability.class));
    }

    @Test
    void testDeleteTeacherAvailability_Success() {
        when(teacherMapper.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherAvailabilityMapper.findById(1L)).thenReturn(Optional.of(testAvailability));
        when(teacherAvailabilityMapper.hardDelete(1L)).thenReturn(1);

        assertDoesNotThrow(() -> {
            teacherService.deleteTeacherAvailability(1L, 1L);
        });

        verify(teacherAvailabilityMapper, times(1)).hardDelete(1L);
    }

    @Test
    void testFindAvailableTeachersForTimeSlot_Success() {
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherAvailabilityMapper.findAvailableTeachersForTimeSlot(any(), any(), any(), any())).thenReturn(teachers);

        List<Teacher> result = teacherService.findAvailableTeachersForTimeSlot(
            DayOfWeek.MONDAY.toString(), LocalTime.of(9, 0), LocalTime.of(12, 0), BigDecimal.valueOf(3.0)
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EMP001", result.get(0).getEmployeeId());
    }

    @Test
    void testGetTeachersByDepartment_Success() {
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherMapper.findByDepartmentId(1L)).thenReturn(teachers);

        List<Teacher> result = teacherService.getTeachersByDepartment(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Computer Science", result.get(0).getDepartmentName());
    }

    @Test
    void testGetTeachersBySpecialization_Success() {
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherMapper.findBySpecialization("CS101")).thenReturn(teachers);

        List<Teacher> result = teacherService.getTeachersBySpecialization("CS101");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testCalculateTeacherWorkload() {
        when(teacherMapper.calculateTotalWeeklyHours(any(), any(), any())).thenReturn(new BigDecimal("25.5"));
        when(teacherMapper.countCurrentSemesterCourses(1L)).thenReturn(3);

        BigDecimal weeklyHours = teacherService.calculateTotalWeeklyHours(1L, "2024-01-01", "2024-12-31");
        int courseCount = teacherService.countCurrentSemesterCourses(1L);

        assertEquals(new BigDecimal("25.5"), weeklyHours);
        assertEquals(3, courseCount);
    }

    @Test
    void testValidateTeacherAvailability() {
        // Test valid availability
        assertTrue(teacherService.isAvailableAtTime(testTeacher, DayOfWeek.MONDAY, LocalTime.of(10, 0)));

        // Test unavailable time
        assertFalse(teacherService.isAvailableAtTime(testTeacher, DayOfWeek.MONDAY, LocalTime.of(8, 0)));

        // Test different day
        assertFalse(teacherService.isAvailableAtTime(testTeacher, DayOfWeek.TUESDAY, LocalTime.of(10, 0)));
    }

    @Test
    void testCheckTeacherWorkloadCapacity() {
        when(teacherMapper.calculateTotalWeeklyHours(any(), any(), any())).thenReturn(new BigDecimal("35.0"));
        when(teacherMapper.countCurrentSemesterCourses(1L)).thenReturn(4);

        assertTrue(teacherService.canHandleAdditionalHours(1L, new BigDecimal("5.0")));
        assertFalse(teacherService.canHandleAdditionalHours(1L, new BigDecimal("10.0")));

        assertTrue(teacherService.canTeachMoreCourses(1L, 1));
        assertFalse(teacherService.canTeachMoreCourses(1L, 2));
    }
}