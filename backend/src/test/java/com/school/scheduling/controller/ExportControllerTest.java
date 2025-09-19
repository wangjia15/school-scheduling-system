package com.school.scheduling.controller;

import com.school.scheduling.dto.request.ExportRequest;
import com.school.scheduling.dto.response.ExportResponse;
import com.school.scheduling.dto.response.ApiResponse;
import com.school.scheduling.service.ExportService;
import com.school.scheduling.domain.Schedule;
import com.school.scheduling.domain.ExportJob;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExportControllerTest {

    @Mock
    private ExportService exportService;

    @InjectMocks
    private ExportController exportController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ExportRequest validExportRequest;
    private ExportResponse mockExportResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(exportController).build();
        objectMapper = new ObjectMapper();

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
    }

    @Test
    void testGeneratePdfReport_Success() throws Exception {
        // Arrange
        when(exportService.generatePdfScheduleReport(any(ExportRequest.class))).thenReturn(mockExportResponse);

        // Act
        ResponseEntity<ApiResponse<ExportResponse>> response = exportController.generatePdfReport(validExportRequest);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals(mockExportResponse.getJobId(), response.getBody().getData().getJobId());
        verify(exportService).generatePdfScheduleReport(validExportRequest);
    }

    @Test
    void testGeneratePdfReport_InvalidRequest() throws Exception {
        // Arrange
        ExportRequest invalidRequest = new ExportRequest();
        invalidRequest.setReportType(null); // Invalid - null report type
        invalidRequest.setFormat(ExportRequest.ExportFormat.PDF);

        when(exportService.generatePdfScheduleReport(any(ExportRequest.class)))
            .thenThrow(new IllegalArgumentException("Report type is required"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            exportController.generatePdfReport(invalidRequest);
        });
    }

    @Test
    void testGenerateExcelReport_Success() throws Exception {
        // Arrange
        validExportRequest.setFormat(ExportRequest.ExportFormat.EXCEL);
        when(exportService.generateExcelScheduleReport(any(ExportRequest.class))).thenReturn(mockExportResponse);

        // Act
        ResponseEntity<ApiResponse<ExportResponse>> response = exportController.generateExcelReport(validExportRequest);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals(mockExportResponse.getJobId(), response.getBody().getData().getJobId());
        verify(exportService).generateExcelScheduleReport(validExportRequest);
    }

    @Test
    void testGenerateBatchExport_Success() throws Exception {
        // Arrange
        validExportRequest.setEntityIds(Arrays.asList(1L, 2L, 3L));
        mockExportResponse.setMessage("Batch export job started successfully");
        when(exportService.generateBatchExport(any(ExportRequest.class))).thenReturn(mockExportResponse);

        // Act
        ResponseEntity<ApiResponse<ExportResponse>> response = exportController.generateBatchExport(validExportRequest);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Batch export job started successfully", response.getBody().getData().getMessage());
        verify(exportService).generateBatchExport(validExportRequest);
    }

    @Test
    void testGetExportStatus_Success() throws Exception {
        // Arrange
        String jobId = UUID.randomUUID().toString();
        ExportJob mockJob = new ExportJob();
        mockJob.setJobId(jobId);
        mockJob.setStatus("PROCESSING");
        mockJob.setProgress(50);

        when(exportService.getExportStatus(jobId)).thenReturn(mockJob);

        // Act
        ResponseEntity<ApiResponse<ExportJob>> response = exportController.getExportStatus(jobId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals(jobId, response.getBody().getData().getJobId());
        assertEquals("PROCESSING", response.getBody().getData().getStatus());
        assertEquals(50, response.getBody().getData().getProgress());
        verify(exportService).getExportStatus(jobId);
    }

    @Test
    void testGetExportStatus_NotFound() throws Exception {
        // Arrange
        String nonExistentJobId = UUID.randomUUID().toString();
        when(exportService.getExportStatus(nonExistentJobId))
            .thenThrow(new RuntimeException("Export job not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            exportController.getExportStatus(nonExistentJobId);
        });
    }

    @Test
    void testGetExportHistory_Success() throws Exception {
        // Arrange
        List<ExportJob> mockHistory = Arrays.asList(
            createMockExportJob("job1", "COMPLETED"),
            createMockExportJob("job2", "PROCESSING"),
            createMockExportJob("job3", "FAILED")
        );

        when(exportService.getExportHistory(50)).thenReturn(mockHistory);

        // Act
        ResponseEntity<ApiResponse<List<ExportJob>>> response = exportController.getExportHistory(50);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals(3, response.getBody().getData().size());
        assertEquals("job1", response.getBody().getData().get(0).getJobId());
        assertEquals("COMPLETED", response.getBody().getData().get(0).getStatus());
        verify(exportService).getExportHistory(50);
    }

    @Test
    void testGetExportHistory_Empty() throws Exception {
        // Arrange
        when(exportService.getExportHistory(50)).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<ApiResponse<List<ExportJob>>> response = exportController.getExportHistory(50);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals(0, response.getBody().getData().size());
        verify(exportService).getExportHistory(50);
    }

    @Test
    void testDownloadExport_Success() throws Exception {
        // Arrange
        String fileName = "test-report.pdf";
        byte[] mockFileContent = "Mock PDF content".getBytes();

        when(exportService.downloadExport(fileName)).thenReturn(mockFileContent);

        // Act
        ResponseEntity<byte[]> response = exportController.downloadExport(fileName);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockFileContent.length, response.getBody().length);
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().containsKey("Content-Disposition"));
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("attachment"));
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains(fileName));
        verify(exportService).downloadExport(fileName);
    }

    @Test
    void testDownloadExport_FileNotFound() throws Exception {
        // Arrange
        String fileName = "nonexistent-file.pdf";
        when(exportService.downloadExport(fileName))
            .thenThrow(new RuntimeException("File not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            exportController.downloadExport(fileName);
        });
    }

    @Test
    void testEmailExport_Success() throws Exception {
        // Arrange
        String jobId = UUID.randomUUID().toString();
        List<String> recipients = Arrays.asList("admin@school.edu", "teacher@school.edu");
        EmailExportRequest emailRequest = new EmailExportRequest();
        emailRequest.setRecipients(recipients);
        emailRequest.setSubject("Test Export Report");
        emailRequest.setBody("Please find attached the export report.");

        ExportResponse emailResponse = new ExportResponse();
        emailResponse.setSuccess(true);
        emailResponse.setMessage("Export report emailed successfully");

        when(exportService.emailExport(jobId, recipients)).thenReturn(emailResponse);

        // Act
        ResponseEntity<ApiResponse<ExportResponse>> response = exportController.emailExport(jobId, emailRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Export report emailed successfully", response.getBody().getData().getMessage());
        verify(exportService).emailExport(jobId, recipients);
    }

    @Test
    void testEmailExport_InvalidRecipients() throws Exception {
        // Arrange
        String jobId = UUID.randomUUID().toString();
        EmailExportRequest emailRequest = new EmailExportRequest();
        emailRequest.setRecipients(Arrays.asList()); // Empty recipients list

        when(exportService.emailExport(jobId, Arrays.asList()))
            .thenThrow(new IllegalArgumentException("At least one recipient is required"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            exportController.emailExport(jobId, emailRequest);
        });
    }

    @Test
    void testGetExportTemplates_Success() throws Exception {
        // Arrange
        List<String> templates = Arrays.asList("Standard", "Detailed", "Summary", "Analytics");
        when(exportService.getExportTemplates()).thenReturn(templates);

        // Act
        ResponseEntity<ApiResponse<List<String>>> response = exportController.getExportTemplates();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals(4, response.getBody().getData().size());
        assertEquals("Standard", response.getBody().getData().get(0));
        verify(exportService).getExportTemplates();
    }

    @Test
    void testGetSupportedFormats_Success() throws Exception {
        // Arrange
        List<String> formats = Arrays.asList("PDF", "EXCEL", "CSV", "JSON");
        when(exportService.getSupportedFormats()).thenReturn(formats);

        // Act
        ResponseEntity<ApiResponse<List<String>>> response = exportController.getSupportedFormats();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals(4, response.getBody().getData().size());
        assertEquals("PDF", response.getBody().getData().get(0));
        verify(exportService).getSupportedFormats();
    }

    @Test
    void testQuickPdfExportByDate_Success() throws Exception {
        // Arrange
        String date = "2024-01-15";
        mockExportResponse.setMessage("Quick PDF export started successfully");
        when(exportService.quickPdfExportByDate(date)).thenReturn(mockExportResponse);

        // Act
        ResponseEntity<ApiResponse<ExportResponse>> response = exportController.quickPdfExportByDate(date);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Quick PDF export started successfully", response.getBody().getData().getMessage());
        verify(exportService).quickPdfExportByDate(date);
    }

    @Test
    void testQuickExcelExportByDate_Success() throws Exception {
        // Arrange
        String date = "2024-01-15";
        mockExportResponse.setMessage("Quick Excel export started successfully");
        when(exportService.quickExcelExportByDate(date)).thenReturn(mockExportResponse);

        // Act
        ResponseEntity<ApiResponse<ExportResponse>> response = exportController.quickExcelExportByDate(date);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Quick Excel export started successfully", response.getBody().getData().getMessage());
        verify(exportService).quickExcelExportByDate(date);
    }

    @Test
    void testQuickExport_InvalidDateFormat() throws Exception {
        // Arrange
        String invalidDate = "2024/01/15"; // Invalid format
        when(exportService.quickPdfExportByDate(invalidDate))
            .thenThrow(new IllegalArgumentException("Invalid date format"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            exportController.quickPdfExportByDate(invalidDate);
        });
    }

    @Test
    void testExportWithAllOptions() throws Exception {
        // Arrange
        ExportRequest requestWithAllOptions = new ExportRequest();
        requestWithAllOptions.setReportType(ExportRequest.ReportType.COMPREHENSIVE_REPORT);
        requestWithAllOptions.setFormat(ExportRequest.ExportFormat.PDF);
        requestWithAllOptions.setStartDate("2024-01-01");
        requestWithAllOptions.setEndDate("2024-12-31");
        requestWithAllOptions.setIncludeCharts(true);
        requestWithAllOptions.setIncludeAnalytics(true);
        requestWithAllOptions.setEmailReport(true);
        requestWithAllOptions.setEmailRecipients("admin@school.edu");
        requestWithAllOptions.setCustomTitle("Annual Comprehensive Report");
        requestWithAllOptions.setCustomDescription("Complete yearly overview");
        requestWithAllOptions.setPaperSize("A4");
        requestWithAllOptions.setLandscape(true);
        requestWithAllOptions.setIncludeHeaderFooter(true);
        requestWithAllOptions.setWatermark("CONFIDENTIAL");

        when(exportService.generatePdfScheduleReport(any(ExportRequest.class))).thenReturn(mockExportResponse);

        // Act
        ResponseEntity<ApiResponse<ExportResponse>> response = exportController.generatePdfReport(requestWithAllOptions);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        verify(exportService).generatePdfScheduleReport(requestWithAllOptions);
    }

    @Test
    void testExportController_ErrorHandling() throws Exception {
        // Arrange
        when(exportService.generatePdfScheduleReport(any(ExportRequest.class)))
            .thenThrow(new RuntimeException("Internal server error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            exportController.generatePdfReport(validExportRequest);
        });
    }

    // Helper method to create mock export jobs
    private ExportJob createMockExportJob(String jobId, String status) {
        ExportJob job = new ExportJob();
        job.setJobId(jobId);
        job.setStatus(status);
        job.setFormat(ExportRequest.ExportFormat.PDF);
        job.setRequest(validExportRequest);
        job.setCreatedAt("2024-01-15T10:00:00");
        if ("COMPLETED".equals(status)) {
            job.setCompletedAt("2024-01-15T10:05:00");
            job.setFileName("report-" + jobId + ".pdf");
        }
        return job;
    }
}