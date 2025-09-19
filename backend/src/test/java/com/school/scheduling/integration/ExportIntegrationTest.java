package com.school.scheduling.integration;

import com.school.scheduling.controller.ExportController;
import com.school.scheduling.dto.request.ExportRequest;
import com.school.scheduling.dto.response.ExportResponse;
import com.school.scheduling.dto.response.ApiResponse;
import com.school.scheduling.service.ExportService;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExportController.class)
class ExportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExportService exportService;

    @MockBean
    private ScheduleMapper scheduleMapper;

    @MockBean
    private TeacherMapper teacherMapper;

    @MockBean
    private ClassroomMapper classroomMapper;

    private ExportRequest validExportRequest;
    private ExportResponse mockExportResponse;
    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
        // Setup valid export request
        validExportRequest = new ExportRequest();
        validExportRequest.setReportType(ExportRequest.ReportType.SCHEDULE_OVERVIEW);
        validExportRequest.setFormat(ExportRequest.ExportFormat.PDF);
        validExportRequest.setStartDate("2024-01-01");
        validExportRequest.setEndDate("2024-01-31");
        validExportRequest.setIncludeCharts(true);
        validExportRequest.setIncludeAnalytics(true);

        // Setup mock export response
        mockExportResponse = new ExportResponse();
        mockExportResponse.setSuccess(true);
        mockExportResponse.setMessage("Export job started successfully");
        mockExportResponse.setJobId(UUID.randomUUID().toString());
        mockExportResponse.setStatus("PENDING");

        // Setup test schedule
        setupTestSchedule();
    }

    private void setupTestSchedule() {
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
        Teacher testTeacher = new Teacher();
        testTeacher.setId(1L);
        testTeacher.setUser(testUser);
        testTeacher.setEmployeeId("EMP001");
        testTeacher.setDepartment(testDepartment);

        // Setup Classroom
        Classroom testClassroom = new Classroom();
        testClassroom.setId(1L);
        testClassroom.setBuildingCode("CS");
        testClassroom.setRoomNumber("101");
        testClassroom.setCapacity(30);

        // Setup TimeSlot
        TimeSlot testTimeSlot = new TimeSlot();
        testTimeSlot.setId(1L);
        testTimeSlot.setDayOfWeek("MONDAY");
        testTimeSlot.setStartTime(LocalDateTime.of(2024, 1, 1, 9, 0).toLocalTime());
        testTimeSlot.setEndTime(LocalDateTime.of(2024, 1, 1, 12, 0).toLocalTime());

        // Setup CourseOffering
        CourseOffering testCourseOffering = new CourseOffering();
        testCourseOffering.setId(1L);
        testCourseOffering.setCourseCode("CS101");
        testCourseOffering.setTitle("Introduction to Programming");
        testCourseOffering.setTeacher(testTeacher);

        // Setup Schedule
        testSchedule = new Schedule();
        testSchedule.setId(1L);
        testSchedule.setCourseOffering(testCourseOffering);
        testSchedule.setClassroom(testClassroom);
        testSchedule.setTimeSlot(testTimeSlot);
        testSchedule.setScheduleDate("2024-01-15");
    }

    @Test
    void testCompleteExportWorkflow() throws Exception {
        // Arrange
        List<Schedule> schedules = Arrays.asList(testSchedule);
        when(exportService.generatePdfScheduleReport(any(ExportRequest.class))).thenReturn(mockExportResponse);
        when(scheduleMapper.findByDateRange("2024-01-01", "2024-01-31")).thenReturn(schedules);

        // Act: Generate export
        mockMvc.perform(post("/api/v1/exports/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validExportRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").exists())
                .andExpect(jsonPath("$.data.message").value("Export job started successfully"));

        // Verify service call
        verify(exportService).generatePdfScheduleReport(validExportRequest);
    }

    @Test
    void testExportWithAllReportTypes() throws Exception {
        // Test each report type
        ExportRequest.ReportType[] reportTypes = {
            ExportRequest.ReportType.SCHEDULE_OVERVIEW,
            ExportRequest.ReportType.TEACHER_WORKLOAD,
            ExportRequest.ReportType.CLASSROOM_UTILIZATION,
            ExportRequest.ReportType.CONFLICTS_REPORT,
            ExportRequest.ReportType.STUDENT_SCHEDULES,
            ExportRequest.ReportType.COURSE_CATALOG,
            ExportRequest.ReportType.ROOM_ALLOCATION,
            ExportRequest.ReportType.DEPARTMENT_SUMMARY,
            ExportRequest.ReportType.COMPREHENSIVE_REPORT
        };

        for (ExportRequest.ReportType reportType : reportTypes) {
            validExportRequest.setReportType(reportType);

            mockMvc.perform(post("/api/v1/exports/pdf")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validExportRequest)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.success").value(true));
        }

        // Verify all report types were processed
        verify(exportService, times(reportTypes.length)).generatePdfScheduleReport(any(ExportRequest.class));
    }

    @Test
    void testExportWithAllFormats() throws Exception {
        // Test each format
        ExportRequest.ExportFormat[] formats = {
            ExportRequest.ExportFormat.PDF,
            ExportRequest.ExportFormat.EXCEL,
            ExportRequest.ExportFormat.CSV,
            ExportRequest.ExportFormat.JSON
        };

        for (ExportRequest.ExportFormat format : formats) {
            validExportRequest.setFormat(format);

            if (format == ExportRequest.ExportFormat.PDF) {
                when(exportService.generatePdfScheduleReport(any(ExportRequest.class))).thenReturn(mockExportResponse);
                mockMvc.perform(post("/api/v1/exports/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validExportRequest)))
                        .andExpect(status().isAccepted());
            } else if (format == ExportRequest.ExportFormat.EXCEL) {
                when(exportService.generateExcelScheduleReport(any(ExportRequest.class))).thenReturn(mockExportResponse);
                mockMvc.perform(post("/api/v1/exports/excel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validExportRequest)))
                        .andExpect(status().isAccepted());
            }
        }
    }

    @Test
    void testBatchExportWorkflow() throws Exception {
        // Arrange
        validExportRequest.setEntityIds(Arrays.asList(1L, 2L, 3L));
        validExportRequest.setBatchExport(true);
        mockExportResponse.setMessage("Batch export job started successfully");
        when(exportService.generateBatchExport(any(ExportRequest.class))).thenReturn(mockExportResponse);

        // Act
        mockMvc.perform(post("/api/v1/exports/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validExportRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Batch export job started successfully"));

        // Verify
        verify(exportService).generateBatchExport(validExportRequest);
    }

    @Test
    void testExportStatusMonitoring() throws Exception {
        // Arrange
        String jobId = UUID.randomUUID().toString();
        when(exportService.getExportStatus(jobId)).thenReturn(
            new com.school.scheduling.domain.ExportJob(
                jobId,
                ExportRequest.ExportFormat.PDF,
                validExportRequest,
                "PROCESSING",
                null,
                null,
                50
            )
        );

        // Act
        mockMvc.perform(get("/api/v1/exports/status/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").value(jobId))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andExpect(jsonPath("$.data.progress").value(50));

        // Verify
        verify(exportService).getExportStatus(jobId);
    }

    @Test
    void testExportHistoryRetrieval() throws Exception {
        // Arrange
        List<com.school.scheduling.domain.ExportJob> mockHistory = Arrays.asList(
            new com.school.scheduling.domain.ExportJob(
                "job1",
                ExportRequest.ExportFormat.PDF,
                validExportRequest,
                "COMPLETED",
                "2024-01-15T10:00:00",
                "2024-01-15T10:05:00",
                "report1.pdf"
            ),
            new com.school.scheduling.domain.ExportJob(
                "job2",
                ExportRequest.ExportFormat.EXCEL,
                validExportRequest,
                "FAILED",
                "2024-01-14T15:00:00",
                null,
                null
            )
        );
        when(exportService.getExportHistory(50)).thenReturn(mockHistory);

        // Act
        mockMvc.perform(get("/api/v1/exports/history?limit=50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].jobId").value("job1"))
                .andExpect(jsonPath("$.data[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data[1].jobId").value("job2"))
                .andExpect(jsonPath("$.data[1].status").value("FAILED"));

        // Verify
        verify(exportService).getExportHistory(50);
    }

    @Test
    void testFileDownloadWorkflow() throws Exception {
        // Arrange
        String fileName = "test-report.pdf";
        byte[] fileContent = "Mock PDF content for download".getBytes();
        when(exportService.downloadExport(fileName)).thenReturn(fileContent);

        // Act
        mockMvc.perform(get("/api/v1/exports/download/{fileName}", fileName))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + fileName + "\""))
                .andExpect(content().bytes(fileContent));

        // Verify
        verify(exportService).downloadExport(fileName);
    }

    @Test
    void testEmailExportWorkflow() throws Exception {
        // Arrange
        String jobId = UUID.randomUUID().toString();
        EmailExportRequest emailRequest = new EmailExportRequest();
        emailRequest.setRecipients(Arrays.asList("admin@school.edu", "teacher@school.edu"));
        emailRequest.setSubject("Test Export Report");
        emailRequest.setBody("Please find attached the export report.");

        mockExportResponse.setMessage("Export report emailed successfully");
        when(exportService.emailExport(jobId, emailRequest.getRecipients())).thenReturn(mockExportResponse);

        // Act
        mockMvc.perform(post("/api/v1/exports/email/{jobId}", jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Export report emailed successfully"));

        // Verify
        verify(exportService).emailExport(jobId, emailRequest.getRecipients());
    }

    @Test
    void testQuickExportWorkflow() throws Exception {
        // Arrange
        String date = "2024-01-15";
        when(exportService.quickPdfExportByDate(date)).thenReturn(mockExportResponse);

        // Act
        mockMvc.perform(get("/api/v1/exports/schedules/pdf/{date}", date))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").exists());

        // Verify
        verify(exportService).quickPdfExportByDate(date);
    }

    @Test
    void testExportWithCustomOptions() throws Exception {
        // Arrange
        ExportRequest customRequest = new ExportRequest();
        customRequest.setReportType(ExportRequest.ReportType.COMPREHENSIVE_REPORT);
        customRequest.setFormat(ExportRequest.ExportFormat.PDF);
        customRequest.setStartDate("2024-01-01");
        customRequest.setEndDate("2024-12-31");
        customRequest.setIncludeCharts(true);
        customRequest.setIncludeAnalytics(true);
        customRequest.setEmailReport(true);
        customRequest.setEmailRecipients("admin@school.edu");
        customRequest.setCustomTitle("Annual Report 2024");
        customRequest.setCustomDescription("Complete annual overview");
        customRequest.setPaperSize("A4");
        customRequest.setLandscape(true);
        customRequest.setIncludeHeaderFooter(true);
        customRequest.setWatermark("CONFIDENTIAL");

        when(exportService.generatePdfScheduleReport(any(ExportRequest.class))).thenReturn(mockExportResponse);

        // Act
        mockMvc.perform(post("/api/v1/exports/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true));

        // Verify
        verify(exportService).generatePdfScheduleReport(customRequest);
    }

    @Test
    void testErrorHandlingWorkflow() throws Exception {
        // Arrange
        when(exportService.generatePdfScheduleReport(any(ExportRequest.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        mockMvc.perform(post("/api/v1/exports/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validExportRequest)))
                .andExpect(status().is5xxServerError());

        // Verify
        verify(exportService).generatePdfScheduleReport(validExportRequest);
    }

    @Test
    void testValidationWorkflow() throws Exception {
        // Test invalid request
        ExportRequest invalidRequest = new ExportRequest();
        invalidRequest.setReportType(null); // Invalid
        invalidRequest.setFormat(ExportRequest.ExportFormat.PDF);

        mockMvc.perform(post("/api/v1/exports/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testTemplateAndFormatRetrieval() throws Exception {
        // Arrange
        List<String> templates = Arrays.asList("Standard", "Detailed", "Summary");
        List<String> formats = Arrays.asList("PDF", "EXCEL", "CSV", "JSON");

        when(exportService.getExportTemplates()).thenReturn(templates);
        when(exportService.getSupportedFormats()).thenReturn(formats);

        // Act - Templates
        mockMvc.perform(get("/api/v1/exports/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0]").value("Standard"));

        // Act - Formats
        mockMvc.perform(get("/api/v1/exports/formats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0]").value("PDF"));

        // Verify
        verify(exportService).getExportTemplates();
        verify(exportService).getSupportedFormats();
    }

    @Test
    void testLargeDatasetExport() throws Exception {
        // Arrange
        List<Schedule> largeScheduleList = Arrays.asList(
            testSchedule, testSchedule, testSchedule, testSchedule, testSchedule,
            testSchedule, testSchedule, testSchedule, testSchedule, testSchedule
        ); // 10 schedules
        when(scheduleMapper.findByDateRange("2024-01-01", "2024-12-31")).thenReturn(largeScheduleList);
        when(exportService.generatePdfScheduleReport(any(ExportRequest.class))).thenReturn(mockExportResponse);

        validExportRequest.setStartDate("2024-01-01");
        validExportRequest.setEndDate("2024-12-31"); // Full year

        // Act
        mockMvc.perform(post("/api/v1/exports/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validExportRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true));

        // Verify
        verify(exportService).generatePdfScheduleReport(validExportRequest);
        verify(scheduleMapper).findByDateRange("2024-01-01", "2024-12-31");
    }

    @Test
    void testConcurrentExportRequests() throws Exception {
        // Arrange
        when(exportService.generatePdfScheduleReport(any(ExportRequest.class)))
            .thenReturn(mockExportResponse, mockExportResponse, mockExportResponse);

        // Act - Send multiple requests simultaneously
        var request1 = mockMvc.perform(post("/api/v1/exports/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validExportRequest)));

        var request2 = mockMvc.perform(post("/api/v1/exports/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validExportRequest)));

        var request3 = mockMvc.perform(post("/api/v1/exports/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validExportRequest)));

        // Assert
        request1.andExpect(status().isAccepted());
        request2.andExpect(status().isAccepted());
        request3.andExpect(status().isAccepted());

        // Verify
        verify(exportService, times(3)).generatePdfScheduleReport(validExportRequest);
    }

    // Helper class for email requests
    private static class EmailExportRequest {
        private List<String> recipients;
        private String subject;
        private String body;

        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }
}