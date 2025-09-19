package com.school.scheduling.controller;

import com.school.scheduling.dto.request.ExportRequest;
import com.school.scheduling.dto.response.ApiResponse;
import com.school.scheduling.dto.response.ExportResponse;
import com.school.scheduling.exception.ExportException;
import com.school.scheduling.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for export operations
 */
@RestController
@RequestMapping("/api/v1/exports")
@Tag(name = "Exports", description = "Export and report generation endpoints")
@CrossOrigin(origins = "*")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping("/pdf")
    @Operation(summary = "Generate PDF report", description = "Generate a PDF schedule report with specified parameters")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "202", description = "PDF export job accepted",
                    content = @Content(schema = @Schema(implementation = ExportResponse.class))),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid export request"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<ExportResponse>> generatePdfReport(
            @Valid @RequestBody ExportRequest request) {

        try {
            ExportResponse response = exportService.generatePdfScheduleReport(request);
            return new ResponseEntity<>(ApiResponse.accepted(response), HttpStatus.ACCEPTED);
        } catch (ExportException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    @PostMapping("/excel")
    @Operation(summary = "Generate Excel report", description = "Generate an Excel schedule report with multiple worksheets")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "202", description = "Excel export job accepted",
                    content = @Content(schema = @Schema(implementation = ExportResponse.class))),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid export request"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<ExportResponse>> generateExcelReport(
            @Valid @RequestBody ExportRequest request) {

        try {
            ExportResponse response = exportService.generateExcelScheduleReport(request);
            return new ResponseEntity<>(ApiResponse.accepted(response), HttpStatus.ACCEPTED);
        } catch (ExportException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "Generate batch export", description = "Generate multiple exports for different entities")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "202", description = "Batch export job accepted",
                    content = @Content(schema = @Schema(implementation = ExportResponse.class))),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid batch export request"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExportResponse>> generateBatchExport(
            @Valid @RequestBody ExportRequest request) {

        try {
            ExportResponse response = exportService.generateBatchExport(request);
            return new ResponseEntity<>(ApiResponse.accepted(response), HttpStatus.ACCEPTED);
        } catch (ExportException e) {
            throw new RuntimeException("Failed to generate batch export", e);
        }
    }

    @GetMapping("/status/{jobId}")
    @Operation(summary = "Get export job status", description = "Retrieve the current status of an export job")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Export job status retrieved",
                    content = @Content(schema = @Schema(implementation = ExportService.ExportJob.class))),
            @SwaggerApiResponse(responseCode = "404", description = "Export job not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<ExportService.ExportJob>> getExportStatus(
            @Parameter(description = "Export job ID") @PathVariable String jobId) {

        try {
            ExportService.ExportJob job = exportService.getExportStatus(jobId);
            return ResponseEntity.ok(ApiResponse.success(job));
        } catch (ExportException e) {
            throw new RuntimeException("Failed to get export status", e);
        }
    }

    @GetMapping("/download/{fileName}")
    @Operation(summary = "Download exported file", description = "Download a completed export file")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @SwaggerApiResponse(responseCode = "404", description = "File not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Resource> downloadExport(
            @Parameter(description = "Export file name") @PathVariable String fileName) {

        try {
            // Find the job by file name
            List<ExportService.ExportJob> jobs = exportService.getExportHistory(1000);
            ExportService.ExportJob job = jobs.stream()
                    .filter(j -> j.getFileName() != null && j.getFileName().equals(fileName))
                    .findFirst()
                    .orElseThrow(() -> new ExportException("Export file not found: " + fileName));

            if (job.getStatus() != ExportService.ExportJob.Status.COMPLETED) {
                throw new ExportException("Export job is not completed: " + fileName);
            }

            Path filePath = Paths.get(job.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ExportException("File not found or not readable: " + fileName);
            }

            // Determine content type
            String contentType = "application/octet-stream";
            if (fileName.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (fileName.endsWith(".xlsx")) {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            } else if (fileName.endsWith(".csv")) {
                contentType = "text/csv";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new RuntimeException("Failed to download export file: " + fileName, e);
        }
    }

    @PostMapping("/email/{jobId}")
    @Operation(summary = "Email exported report", description = "Send an exported report via email")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Email sent successfully",
                    content = @Content(schema = @Schema(implementation = ExportResponse.class))),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid email request"),
            @SwaggerApiResponse(responseCode = "404", description = "Export job not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ExportResponse>> emailExport(
            @Parameter(description = "Export job ID") @PathVariable String jobId,
            @RequestBody EmailExportRequest emailRequest) {

        try {
            List<String> recipients = Arrays.asList(emailRequest.getRecipients().split(","));
            ExportResponse response = exportService.emailExport(jobId, recipients);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (ExportException e) {
            throw new RuntimeException("Failed to email export", e);
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get export history", description = "Retrieve the history of export jobs")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Export history retrieved",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<ExportService.ExportJob>>> getExportHistory(
            @Parameter(description = "Maximum number of records to return") @RequestParam(defaultValue = "50") int limit) {

        try {
            List<ExportService.ExportJob> history = exportService.getExportHistory(limit);
            return ResponseEntity.ok(ApiResponse.success(history));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get export history", e);
        }
    }

    @GetMapping("/templates")
    @Operation(summary = "Get available export templates", description = "Retrieve the list of available export templates")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Templates retrieved successfully"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ExportTemplate>>> getExportTemplates() {

        try {
            List<ExportTemplate> templates = Arrays.asList(
                    new ExportTemplate("default_schedule", "Default Schedule Template", "Standard schedule layout with all details"),
                    new ExportTemplate("compact_schedule", "Compact Schedule Template", "Space-efficient layout for large schedules"),
                    new ExportTemplate("teacher_workload", "Teacher Workload Template", "Focused on teacher workload analysis"),
                    new ExportTemplate("classroom_utilization", "Classroom Utilization Template", "Room usage and capacity analysis"),
                    new ExportTemplate("conflict_report", "Conflict Report Template", "Detailed conflict detection and analysis"),
                    new ExportTemplate("executive_summary", "Executive Summary Template", "High-level overview for management")
            );
            return ResponseEntity.ok(ApiResponse.success(templates));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get export templates", e);
        }
    }

    @GetMapping("/formats")
    @Operation(summary = "Get supported export formats", description = "Retrieve the list of supported export formats")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Formats retrieved successfully"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ExportFormatInfo>>> getExportFormats() {

        try {
            List<ExportFormatInfo> formats = Arrays.asList(
                    new ExportFormatInfo("PDF", "Portable Document Format", "High-quality printable documents"),
                    new ExportFormatInfo("EXCEL", "Microsoft Excel", "Spreadsheet with multiple worksheets"),
                    new ExportFormatInfo("CSV", "Comma-Separated Values", "Plain text data format"),
                    new ExportFormatInfo("JSON", "JavaScript Object Notation", "Structured data format")
            );
            return ResponseEntity.ok(ApiResponse.success(formats));
        } catch (Exception e) {
            throw new RuntimeException("Failed to get export formats", e);
        }
    }

    @GetMapping("/schedules/pdf/{date}")
    @Operation(summary = "Quick PDF export by date", description = "Generate a PDF for all schedules on a specific date")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "202", description = "PDF export job accepted"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<ExportResponse>> quickPdfExportByDate(
            @Parameter(description = "Export date") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            ExportRequest request = new ExportRequest();
            request.setReportType(ExportRequest.ReportType.SCHEDULE_OVERVIEW);
            request.setFormat(ExportRequest.ExportFormat.PDF);
            request.setStartDate(date);
            request.setEndDate(date);

            ExportResponse response = exportService.generatePdfScheduleReport(request);
            return new ResponseEntity<>(ApiResponse.accepted(response), HttpStatus.ACCEPTED);
        } catch (ExportException e) {
            throw new RuntimeException("Failed to generate quick PDF export", e);
        }
    }

    @GetMapping("/schedules/excel/{date}")
    @Operation(summary = "Quick Excel export by date", description = "Generate an Excel for all schedules on a specific date")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "202", description = "Excel export job accepted"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<ExportResponse>> quickExcelExportByDate(
            @Parameter(description = "Export date") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            ExportRequest request = new ExportRequest();
            request.setReportType(ExportRequest.ReportType.SCHEDULE_OVERVIEW);
            request.setFormat(ExportRequest.ExportFormat.EXCEL);
            request.setStartDate(date);
            request.setEndDate(date);

            ExportResponse response = exportService.generateExcelScheduleReport(request);
            return new ResponseEntity<>(ApiResponse.accepted(response), HttpStatus.ACCEPTED);
        } catch (ExportException e) {
            throw new RuntimeException("Failed to generate quick Excel export", e);
        }
    }

    @PostMapping("/upload-template")
    @Operation(summary = "Upload custom template", description = "Upload a custom export template")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Template uploaded successfully"),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid template file"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadTemplate(
            @Parameter(description = "Template file") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Template name") @RequestParam String templateName) {

        try {
            // Validate file
            if (file.isEmpty()) {
                throw new ExportException("Template file is empty");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.endsWith(".html")) {
                throw new ExportException("Template file must be an HTML file");
            }

            // Save template (in a real implementation, you would store it in a database or templates directory)
            String message = String.format("Template '%s' uploaded successfully", templateName);
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload template", e);
        }
    }

    // DTO classes

    public static class EmailExportRequest {
        @NotBlank(message = "Recipients are required")
        private String recipients;

        private String subject;
        private String body;

        public String getRecipients() { return recipients; }
        public void setRecipients(String recipients) { this.recipients = recipients; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }

    public static class ExportTemplate {
        private String id;
        private String name;
        private String description;

        public ExportTemplate(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    public static class ExportFormatInfo {
        private String id;
        private String name;
        private String description;

        public ExportFormatInfo(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
}