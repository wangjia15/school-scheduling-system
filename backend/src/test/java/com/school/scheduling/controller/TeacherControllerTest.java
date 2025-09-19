package com.school.scheduling.controller;

import com.school.scheduling.controller.TeacherController;
import com.school.scheduling.dto.PageRequest;
import com.school.scheduling.dto.response.ApiResponse;
import com.school.scheduling.dto.response.PaginatedResponse;
import com.school.scheduling.dto.response.TeacherResponse;
import com.school.scheduling.dto.request.TeacherRequest;
import com.school.scheduling.dto.request.TeacherUpdateRequest;
import com.school.scheduling.dto.request.TeacherAvailabilityRequest;
import com.school.scheduling.dto.response.TeacherAvailabilityResponse;
import com.school.scheduling.domain.Teacher;
import com.school.scheduling.domain.TeacherSpecialization;
import com.school.scheduling.domain.TeacherAvailability;
import com.school.scheduling.domain.Department;
import com.school.scheduling.domain.User;
import com.school.scheduling.mapper.TeacherMapper;
import com.school.scheduling.mapper.TeacherAvailabilityMapper;
import com.school.scheduling.validation.TeacherValidator;
import com.school.scheduling.validation.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TeacherControllerTest {

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private TeacherAvailabilityMapper teacherAvailabilityMapper;

    @Mock
    private TeacherValidator teacherValidator;

    @InjectMocks
    private TeacherController teacherController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Teacher testTeacher;
    private TeacherResponse testTeacherResponse;
    private TeacherAvailability testAvailability;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(teacherController).build();
        objectMapper = new ObjectMapper();

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
        testTeacher.setOfficeLocation("Building A, Room 101");
        testTeacher.setPhone("+1 (555) 123-4567");

        testTeacherResponse = TeacherResponse.fromEntity(testTeacher);

        testAvailability = new TeacherAvailability();
        testAvailability.setId(1L);
        testAvailability.setTeacher(testTeacher);
        testAvailability.setDayOfWeek(DayOfWeek.MONDAY);
        testAvailability.setStartTime(LocalTime.of(9, 0));
        testAvailability.setEndTime(LocalTime.of(12, 0));
        testAvailability.setAvailabilityType(TeacherAvailability.AvailabilityType.PREFERRED);
        testAvailability.setMaxClasses(2);
        testAvailability.setBreakDuration(BigDecimal.ZERO);
        testAvailability.setRequiresBreakBetweenClasses(false);
        testAvailability.setIsRecurring(true);
    }

    @Test
    void testGetAllTeachers_Success() throws Exception {
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherMapper.findAllWithFilters(any(), any(), any(), any())).thenReturn(teachers);
        when(teacherMapper.countWithFilters(any(), any(), any())).thenReturn(1L);

        ResponseEntity<ApiResponse<PaginatedResponse<TeacherResponse>>> response =
            teacherController.getAllTeachers(0, 20, "id", "ASC", null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().getContent().size());
        assertEquals("John Doe", response.getBody().getData().getContent().get(0).getUser().getFirstName() + " " +
                     response.getBody().getData().getContent().get(0).getUser().getLastName());
    }

    @Test
    void testGetTeacherById_Success() throws Exception {
        when(teacherMapper.findById(1L)).thenReturn(testTeacher);

        ResponseEntity<ApiResponse<TeacherResponse>> response = teacherController.getTeacherById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("EMP001", response.getBody().getData().getEmployeeId());
    }

    @Test
    void testGetTeacherById_NotFound() throws Exception {
        when(teacherMapper.findById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            teacherController.getTeacherById(999L);
        });
    }

    @Test
    void testCreateTeacher_Success() throws Exception {
        TeacherRequest request = new TeacherRequest();
        request.setEmployeeId("EMP001");
        request.setUserId(1L);
        request.setDepartmentId(1L);
        request.setTitle("PROFESSOR");
        request.setMaxWeeklyHours(new BigDecimal("40.0"));
        request.setMaxCoursesPerSemester(5);
        request.setOfficeLocation("Building A, Room 101");
        request.setPhone("+1 (555) 123-4567");

        ValidationResult validationResult = new ValidationResult(Collections.emptyList());
        when(teacherValidator.validateForCreation(any())).thenReturn(validationResult);
        when(teacherMapper.findByEmployeeId("EMP001")).thenReturn(null);
        when(teacherMapper.findByUserId(1L)).thenReturn(null);
        when(teacherMapper.insert(any(Teacher.class))).thenReturn(1);
        when(teacherMapper.findById(1L)).thenReturn(testTeacher);

        ResponseEntity<ApiResponse<TeacherResponse>> response = teacherController.createTeacher(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("EMP001", response.getBody().getData().getEmployeeId());
    }

    @Test
    void testCreateTeacher_DuplicateEmployeeId() throws Exception {
        TeacherRequest request = new TeacherRequest();
        request.setEmployeeId("EMP001");
        request.setUserId(1L);

        ValidationResult validationResult = new ValidationResult(Collections.emptyList());
        when(teacherValidator.validateForCreation(any())).thenReturn(validationResult);
        when(teacherMapper.findByEmployeeId("EMP001")).thenReturn(testTeacher);

        assertThrows(RuntimeException.class, () -> {
            teacherController.createTeacher(request);
        });
    }

    @Test
    void testUpdateTeacher_Success() throws Exception {
        TeacherUpdateRequest request = new TeacherUpdateRequest();
        request.setOfficeLocation("Building B, Room 202");

        ValidationResult validationResult = new ValidationResult(Collections.emptyList());
        when(teacherMapper.findById(1L)).thenReturn(testTeacher);
        when(teacherValidator.validateForUpdate(any(), any())).thenReturn(validationResult);
        when(teacherMapper.update(any(Teacher.class))).thenReturn(1);
        when(teacherMapper.findById(1L)).thenReturn(testTeacher);

        ResponseEntity<ApiResponse<TeacherResponse>> response = teacherController.updateTeacher(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteTeacher_Success() throws Exception {
        when(teacherMapper.findById(1L)).thenReturn(testTeacher);
        when(teacherMapper.hasActiveSchedules(1L)).thenReturn(false);
        when(teacherMapper.deleteById(1L)).thenReturn(1);

        ResponseEntity<ApiResponse<Void>> response = teacherController.deleteTeacher(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteTeacher_HasActiveSchedules() throws Exception {
        when(teacherMapper.findById(1L)).thenReturn(testTeacher);
        when(teacherMapper.hasActiveSchedules(1L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            teacherController.deleteTeacher(1L);
        });
    }

    @Test
    void testGetTeacherAvailability_Success() throws Exception {
        List<TeacherAvailability> availabilities = Arrays.asList(testAvailability);
        when(teacherMapper.findById(1L)).thenReturn(testTeacher);
        when(teacherAvailabilityMapper.findByTeacherId(1L)).thenReturn(availabilities);

        ResponseEntity<ApiResponse<List<TeacherAvailabilityResponse>>> response =
            teacherController.getTeacherAvailability(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(DayOfWeek.MONDAY, response.getBody().getData().get(0).getDayOfWeek());
    }

    @Test
    void testAddTeacherAvailability_Success() throws Exception {
        TeacherAvailabilityRequest request = new TeacherAvailabilityRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime("09:00");
        request.setEndTime("12:00");
        request.setAvailabilityType(TeacherAvailabilityRequest.TeacherAvailabilityType.PREFERRED);
        request.setMaxClasses(2);
        request.setBreakDuration(BigDecimal.ZERO);
        request.setRequiresBreakBetweenClasses(false);
        request.setIsRecurring(true);

        when(teacherMapper.findById(1L)).thenReturn(testTeacher);
        when(teacherAvailabilityMapper.findOverlappingAvailability(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(teacherAvailabilityMapper.insert(any(TeacherAvailability.class))).thenReturn(1);
        when(teacherAvailabilityMapper.findById(1L)).thenReturn(Optional.of(testAvailability));

        ResponseEntity<ApiResponse<TeacherAvailabilityResponse>> response =
            teacherController.addTeacherAvailability(1L, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(DayOfWeek.MONDAY, response.getBody().getData().getDayOfWeek());
    }

    @Test
    void testAddTeacherAvailability_OverlappingTime() throws Exception {
        TeacherAvailabilityRequest request = new TeacherAvailabilityRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime("09:00");
        request.setEndTime("12:00");

        when(teacherMapper.findById(1L)).thenReturn(testTeacher);
        when(teacherAvailabilityMapper.findOverlappingAvailability(any(), any(), any(), any())).thenReturn(Arrays.asList(testAvailability));

        assertThrows(RuntimeException.class, () -> {
            teacherController.addTeacherAvailability(1L, request);
        });
    }

    @Test
    void testAddTeacherAvailability_InvalidTimeRange() throws Exception {
        TeacherAvailabilityRequest request = new TeacherAvailabilityRequest();
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime("12:00");
        request.setEndTime("09:00");

        when(teacherMapper.findById(1L)).thenReturn(testTeacher);

        assertThrows(RuntimeException.class, () -> {
            teacherController.addTeacherAvailability(1L, request);
        });
    }

    @Test
    void testGetTeachersByDepartment_Success() throws Exception {
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherMapper.findByDepartmentId(1L)).thenReturn(teachers);

        ResponseEntity<ApiResponse<List<TeacherResponse>>> response = teacherController.getTeachersByDepartment(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testGetTeachersBySpecialization_Success() throws Exception {
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherMapper.findBySpecialization("CS101")).thenReturn(teachers);

        ResponseEntity<ApiResponse<List<TeacherResponse>>> response = teacherController.getTeachersBySpecialization("CS101");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testFindAvailableTeachersForTimeSlot_Success() throws Exception {
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherAvailabilityMapper.findAvailableTeachersForTimeSlot(any(), any(), any(), any())).thenReturn(teachers);

        ResponseEntity<ApiResponse<List<TeacherResponse>>> response =
            teacherController.findAvailableTeachersForTimeSlot("MONDAY", "09:00", "12:00", new BigDecimal("3.0"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
    }
}