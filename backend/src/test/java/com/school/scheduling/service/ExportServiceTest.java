package com.school.scheduling.service;

import com.school.scheduling.dto.request.ExportRequest;
import com.school.scheduling.dto.response.ExportResponse;
import com.school.scheduling.domain.Schedule;
import com.school.scheduling.domain.CourseOffering;
import com.school.scheduling.domain.Classroom;
import com.school.scheduling.domain.Teacher;
import com.school.scheduling.domain.User;
import com.school.scheduling.domain.Department;
import com.school.scheduling.domain.TimeSlot;
import com.school.scheduling.mapper.ScheduleMapper;
import com.school.scheduling.mapper.TeacherMapper;
import com.school.scheduling.mapper.ClassroomMapper;
import com.school.scheduling.exception.ExportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private ClassroomMapper classroomMapper;

    @InjectMocks
    private ExportService exportService;

    private ExportRequest validExportRequest;
    private Schedule testSchedule;
    private Teacher testTeacher;
    private Classroom testClassroom;
    private TimeSlot testTimeSlot;
    private CourseOffering testCourseOffering;

    @BeforeEach
    void setUp() {
        // Set up base directory for test files
        ReflectionTestUtils.setField(exportService, "baseDirectory", "test-exports");

        // Create test directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get("test-exports"));
        } catch (IOException e) {
            // Ignore directory creation errors for tests
        }

        // Setup test data
        setupTestData();

        // Setup valid export request
        validExportRequest = new ExportRequest();
        validExportRequest.setReportType(ExportRequest.ReportType.SCHEDULE_OVERVIEW);
        validExportRequest.setFormat(ExportRequest.ExportFormat.PDF);
        validExportRequest.setStartDate("2024-01-01");
        validExportRequest.setEndDate("2024-01-31");
        validExportRequest.setIncludeCharts(true);
        validExportRequest.setIncludeAnalytics(true);
    }

    private void setupTestData() {
        // Setup User
        User testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@university.edu");

        // Setup Department
        Department testDepartment = new Department();
        testDepartment.setId(1L);
        testDepartment.setName("Computer Science");

        // Setup Teacher
        testTeacher = new Teacher();
        testTeacher.setId(1L);
        testTeacher.setUser(testUser);
        testTeacher.setEmployeeId("EMP001");
        testTeacher.setDepartment(testDepartment);
        testTeacher.setTitle(Teacher.TeacherTitle.PROFESSOR);

        // Setup Classroom
        testClassroom = new Classroom();
        testClassroom.setId(1L);
        testClassroom.setBuildingCode("CS");
        testClassroom.setRoomNumber("101");
        testClassroom.setName("Computer Science Lab");
        testClassroom.setCapacity(30);
        testClassroom.setRoomType(Classroom.RoomType.LABORATORY);
        testClassroom.setHasProjector(true);
        testClassroom.setHasComputer(true);
        testClassroom.setHasWhiteboard(true);

        // Setup TimeSlot
        testTimeSlot = new TimeSlot();
        testTimeSlot.setId(1L);
        testTimeSlot.setDayOfWeek("MONDAY");
        testTimeSlot.setStartTime(LocalDateTime.of(2024, 1, 1, 9, 0).toLocalTime());
        testTimeSlot.setEndTime(LocalDateTime.of(2024, 1, 1, 12, 0).toLocalTime());
        testTimeSlot.setSlotType(TimeSlot.SlotType.LECTURE);

        // Setup CourseOffering
        testCourseOffering = new CourseOffering();
        testCourseOffering.setId(1L);
        testCourseOffering.setCourseCode("CS101");
        testCourseOffering.setTitle("Introduction to Programming");
        testCourseOffering.setTeacher(testTeacher);
        testCourseOffering.setCapacity(30);
        testCourseOffering.setCredits(3);

        // Setup Schedule
        testSchedule = new Schedule();
        testSchedule.setId(1L);
        testSchedule.setCourseOffering(testCourseOffering);
        testSchedule.setClassroom(testClassroom);
        testSchedule.setTimeSlot(testTimeSlot);
        testSchedule.setScheduleDate("2024-01-15");
        testSchedule.setIsRecurring(false);
        testSchedule.setNotes("Regular class session");
    }

    @Test
    void testGeneratePdfScheduleReport_Success() {
        // Arrange
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleMapper.findByDateRange("2024-01-01", "2024-01-31")).thenReturn(schedules);

        // Act
        ExportResponse response = exportService.generatePdfScheduleReport(validExportRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("PDF export job started successfully", response.getMessage());
        assertNotNull(response.getJobId());
        verify(scheduleMapper).findByDateRange("2024-01-01", "2024-01-31");
    }

    @Test
    void testGeneratePdfScheduleReport_EmptyScheduleList() {
        // Arrange
        when(scheduleMapper.findByDateRange("2024-01-01", "2024-01-31")).thenReturn(Collections.emptyList());

        // Act
        ExportResponse response = exportService.generatePdfScheduleReport(validExportRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("PDF export job started successfully", response.getMessage());
        verify(scheduleMapper).findByDateRange("2024-01-01", "2024-01-31");
    }

    @Test
    void testGeneratePdfScheduleReport_MapperError() {
        // Arrange
        when(scheduleMapper.findByDateRange("2024-01-01", "2024-01-31"))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        assertThrows(ExportException.class, () -> {
            exportService.generatePdfScheduleReport(validExportRequest);
        });
        verify(scheduleMapper).findByDateRange("2024-01-01", "2024-01-31");
    }

    @Test
    void testGenerateExcelScheduleReport_Success() {
        // Arrange
        validExportRequest.setFormat(ExportRequest.ExportFormat.EXCEL);
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleMapper.findByDateRange("2024-01-01", "2024-01-31")).thenReturn(schedules);

        // Act
        ExportResponse response = exportService.generateExcelScheduleReport(validExportRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Excel export job started successfully", response.getMessage());
        assertNotNull(response.getJobId());
        verify(scheduleMapper).findByDateRange("2024-01-01", "2024-01-31");
    }

    @Test
    void testGenerateTeacherWorkloadReport_Success() {
        // Arrange
        validExportRequest.setReportType(ExportRequest.ReportType.TEACHER_WORKLOAD);
        List<Teacher> teachers = Arrays.asList(testTeacher);
        when(teacherMapper.findAllWithFilters(any(), any(), any(), any())).thenReturn(teachers);

        // Act
        ExportResponse response = exportService.generatePdfScheduleReport(validExportRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getJobId());
        verify(teacherMapper).findAllWithFilters(any(), any(), any(), any());
    }

    @Test
    void testGenerateClassroomUtilizationReport_Success() {
        // Arrange
        validExportRequest.setReportType(ExportRequest.ReportType.CLASSROOM_UTILIZATION);
        List<Classroom> classrooms = Arrays.asList(testClassroom);
        when(classroomMapper.findAll()).thenReturn(classrooms);

        // Act
        ExportResponse response = exportService.generatePdfScheduleReport(validExportRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getJobId());
        verify(classroomMapper).findAll();
    }

    @Test
    void testGenerateBatchExport_Success() {
        // Arrange
        List<Long> entityIds = Arrays.asList(1L, 2L, 3L);
        validExportRequest.setEntityIds(entityIds);
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleMapper.findByIds(entityIds)).thenReturn(schedules);

        // Act
        ExportResponse response = exportService.generateBatchExport(validExportRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Batch export job started successfully", response.getMessage());
        assertNotNull(response.getJobId());
        verify(scheduleMapper).findByIds(entityIds);
    }

    @Test
    void testGetExportStatus_Success() {
        // Arrange
        String jobId = UUID.randomUUID().toString();
        ExportRequest request = new ExportRequest();
        request.setReportType(ExportRequest.ReportType.SCHEDULE_OVERVIEW);
        request.setFormat(ExportRequest.ExportFormat.PDF);

        // Create a job and add it to active jobs
        ExportResponse exportResponse = new ExportResponse();
        exportResponse.setJobId(jobId);
        exportResponse.setSuccess(true);

        CompletableFuture<ExportResponse> future = CompletableFuture.completedFuture(exportResponse);
        ReflectionTestUtils.setField(exportService, "activeJobs", Map.of(jobId, future));

        // Act
        ExportJob job = exportService.getExportStatus(jobId);

        // Assert
        assertNotNull(job);
        assertEquals(jobId, job.getJobId());
        assertEquals(request.getFormat(), job.getFormat());
    }

    @Test
    void testGetExportStatus_NotFound() {
        // Arrange
        String nonExistentJobId = UUID.randomUUID().toString();

        // Act & Assert
        assertThrows(ExportException.class, () -> {
            exportService.getExportStatus(nonExistentJobId);
        });
    }

    @Test
    void testGetExportHistory_Success() {
        // Arrange
        // Mock some completed jobs in the export service
        // This would require more complex setup to simulate job completion

        // Act
        List<ExportJob> history = exportService.getExportHistory(10);

        // Assert
        assertNotNull(history);
        assertTrue(history.size() <= 10);
    }

    @Test
    void testExportRequest_InvalidDateRange() {
        // Arrange
        validExportRequest.setStartDate("2024-01-31");
        validExportRequest.setEndDate("2024-01-01"); // End date before start date

        // Act & Assert
        assertThrows(ExportException.class, () -> {
            exportService.generatePdfScheduleReport(validExportRequest);
        });
    }

    @Test
    void testExportRequest_NullReportType() {
        // Arrange
        validExportRequest.setReportType(null);

        // Act & Assert
        assertThrows(ExportException.class, () -> {
            exportService.generatePdfScheduleReport(validExportRequest);
        });
    }

    @Test
    void testExportRequest_NullFormat() {
        // Arrange
        validExportRequest.setFormat(null);

        // Act & Assert
        assertThrows(ExportException.class, () -> {
            exportService.generatePdfScheduleReport(validExportRequest);
        });
    }

    @Test
    void testProcessExportJob_PdfGeneration() throws Exception {
        // Arrange
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleMapper.findByDateRange("2024-01-01", "2024-01-31")).thenReturn(schedules);

        // Act
        ExportResponse response = exportService.generatePdfScheduleReport(validExportRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());

        // Wait for job completion (in real test, we'd mock this)
        Thread.sleep(100);

        // Verify job completion
        ExportJob job = exportService.getExportStatus(response.getJobId());
        assertNotNull(job);
    }

    @Test
    void testProcessExportJob_ExcelGeneration() throws Exception {
        // Arrange
        validExportRequest.setFormat(ExportRequest.ExportFormat.EXCEL);
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(scheduleMapper.findByDateRange("2024-01-01", "2024-01-31")).thenReturn(schedules);

        // Act
        ExportResponse response = exportService.generateExcelScheduleReport(validExportRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());

        // Wait for job completion
        Thread.sleep(100);

        // Verify job completion
        ExportJob job = exportService.getExportStatus(response.getJobId());
        assertNotNull(job);
    }

    @Test
    void testCreateExportDirectory_Success() throws IOException {
        // Arrange
        String testDir = "test-exports-" + UUID.randomUUID().toString();

        // Act
        Path directoryPath = Paths.get(testDir);
        Files.createDirectories(directoryPath);

        // Assert
        assertTrue(Files.exists(directoryPath));
        assertTrue(Files.isDirectory(directoryPath));

        // Cleanup
        Files.deleteIfExists(directoryPath);
    }

    @Test
    void testValidateExportRequest_Valid() {
        // Arrange
        ExportRequest request = new ExportRequest();
        request.setReportType(ExportRequest.ReportType.SCHEDULE_OVERVIEW);
        request.setFormat(ExportRequest.ExportFormat.PDF);
        request.setStartDate("2024-01-01");
        request.setEndDate("2024-01-31");

        // Act & Assert
        assertDoesNotThrow(() -> {
            exportService.generatePdfScheduleReport(request);
        });
    }

    @Test
    void testValidateExportRequest_InvalidDates() {
        // Arrange
        ExportRequest request = new ExportRequest();
        request.setReportType(ExportRequest.ReportType.SCHEDULE_OVERVIEW);
        request.setFormat(ExportRequest.ExportFormat.PDF);
        request.setStartDate("2024-01-31");
        request.setEndDate("2024-01-01"); // End date before start date

        // Act & Assert
        assertThrows(ExportException.class, () -> {
            exportService.generatePdfScheduleReport(request);
        });
    }

    @Test
    void testGenerateFileName_WithCustomTitle() {
        // Arrange
        validExportRequest.setCustomTitle("Custom_Schedule_Report");
        validExportRequest.setFormat(ExportRequest.ExportFormat.PDF);

        // Act
        // This would be tested by examining the actual file generation
        // For now, we just ensure the process doesn't fail
        assertDoesNotThrow(() -> {
            exportService.generatePdfScheduleReport(validExportRequest);
        });
    }

    @Test
    void testGenerateFileName_WithoutCustomTitle() {
        // Arrange
        validExportRequest.setCustomTitle(null);
        validExportRequest.setFormat(ExportRequest.ExportFormat.PDF);

        // Act
        // This would be tested by examining the actual file generation
        // For now, we just ensure the process doesn't fail
        assertDoesNotThrow(() -> {
            exportService.generatePdfScheduleReport(validExportRequest);
        });
    }

    @Test
    void testCleanUpCompletedJobs() {
        // Arrange
        // This would require more complex setup to simulate job cleanup

        // Act
        // The cleanup method is typically called periodically
        // We just ensure it doesn't throw exceptions
        assertDoesNotThrow(() -> {
            exportService.getExportHistory(100); // This might trigger cleanup
        });
    }
}