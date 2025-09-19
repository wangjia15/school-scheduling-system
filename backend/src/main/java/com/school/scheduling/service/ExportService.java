package com.school.scheduling.service;

import com.school.scheduling.domain.*;
import com.school.scheduling.dto.request.ExportRequest;
import com.school.scheduling.dto.response.ExportResponse;
import com.school.scheduling.exception.ExportException;
import com.school.scheduling.mapper.ScheduleMapper;
import com.school.scheduling.mapper.TeacherMapper;
import com.school.scheduling.mapper.ClassroomMapper;
import com.school.scheduling.mapper.CourseMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Comprehensive export service for generating PDF and Excel reports
 * from scheduling data with template support and batch processing.
 */
@Service
@Transactional
public class ExportService {

    private final ScheduleMapper scheduleMapper;
    private final TeacherMapper teacherMapper;
    private final ClassroomMapper classroomMapper;
    private final CourseMapper courseMapper;
    private final JavaMailSender mailSender;

    @Value("${app.export.storage.path:./exports}")
    private String exportStoragePath;

    @Value("${app.export.max-file-size:10485760}")
    private long maxFileSize;

    private final ExecutorService exportExecutor = Executors.newFixedThreadPool(4);
    private final Map<String, ExportJob> activeJobs = new ConcurrentHashMap<>();

    @Autowired
    public ExportService(ScheduleMapper scheduleMapper,
                        TeacherMapper teacherMapper,
                        ClassroomMapper classroomMapper,
                        CourseMapper courseMapper,
                        JavaMailSender mailSender) {
        this.scheduleMapper = scheduleMapper;
        this.teacherMapper = teacherMapper;
        this.classroomMapper = classroomMapper;
        this.courseMapper = courseMapper;
        this.mailSender = mailSender;

        // Create export directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(exportStoragePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create export directory", e);
        }
    }

    /**
     * Generate a PDF schedule report
     */
    public ExportResponse generatePdfScheduleReport(ExportRequest request) {
        try {
            String jobId = UUID.randomUUID().toString();
            ExportJob job = new ExportJob(jobId, ExportRequest.ExportFormat.PDF, request);
            activeJobs.put(jobId, job);

            // Execute export asynchronously
            CompletableFuture<ExportResponse> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return executePdfExport(request, jobId);
                } catch (Exception e) {
                    job.setStatus(ExportJob.Status.FAILED);
                    job.setErrorMessage(e.getMessage());
                    throw new ExportException("Failed to generate PDF report", e);
                }
            }, exportExecutor);

            return ExportResponse.accepted(jobId, "PDF export job started");
        } catch (Exception e) {
            throw new ExportException("Failed to start PDF export", e);
        }
    }

    /**
     * Generate an Excel schedule report
     */
    public ExportResponse generateExcelScheduleReport(ExportRequest request) {
        try {
            String jobId = UUID.randomUUID().toString();
            ExportJob job = new ExportJob(jobId, ExportRequest.ExportFormat.EXCEL, request);
            activeJobs.put(jobId, job);

            // Execute export asynchronously
            CompletableFuture<ExportResponse> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return executeExcelExport(request, jobId);
                } catch (Exception e) {
                    job.setStatus(ExportJob.Status.FAILED);
                    job.setErrorMessage(e.getMessage());
                    throw new ExportException("Failed to generate Excel report", e);
                }
            }, exportExecutor);

            return ExportResponse.accepted(jobId, "Excel export job started");
        } catch (Exception e) {
            throw new ExportException("Failed to start Excel export", e);
        }
    }

    /**
     * Generate batch exports for multiple entities
     */
    public ExportResponse generateBatchExport(ExportRequest request) {
        try {
            String jobId = UUID.randomUUID().toString();
            ExportJob job = new ExportJob(jobId, request.getFormat(), request);
            job.setBatchExport(true);
            activeJobs.put(jobId, job);

            // Execute batch export asynchronously
            CompletableFuture<ExportResponse> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return executeBatchExport(request, jobId);
                } catch (Exception e) {
                    job.setStatus(ExportJob.Status.FAILED);
                    job.setErrorMessage(e.getMessage());
                    throw new ExportException("Failed to generate batch export", e);
                }
            }, exportExecutor);

            return ExportResponse.accepted(jobId, "Batch export job started");
        } catch (Exception e) {
            throw new ExportException("Failed to start batch export", e);
        }
    }

    /**
     * Get export job status
     */
    public ExportJob getExportStatus(String jobId) {
        ExportJob job = activeJobs.get(jobId);
        if (job == null) {
            throw new ExportException("Export job not found: " + jobId);
        }
        return job;
    }

    /**
     * Download exported file
     */
    public byte[] downloadExport(String jobId) {
        ExportJob job = activeJobs.get(jobId);
        if (job == null) {
            throw new ExportException("Export job not found: " + jobId);
        }

        if (job.getStatus() != ExportJob.Status.COMPLETED) {
            throw new ExportException("Export job is not completed: " + jobId);
        }

        try {
            return Files.readAllBytes(Paths.get(job.getFilePath()));
        } catch (IOException e) {
            throw new ExportException("Failed to read export file: " + jobId, e);
        }
    }

    /**
     * Email exported report
     */
    public ExportResponse emailExport(String jobId, List<String> recipients) {
        ExportJob job = activeJobs.get(jobId);
        if (job == null) {
            throw new ExportException("Export job not found: " + jobId);
        }

        if (job.getStatus() != ExportJob.Status.COMPLETED) {
            throw new ExportException("Export job is not completed: " + jobId);
        }

        try {
            sendEmailWithAttachment(job.getFilePath(), recipients, job.getFileName(), job.getReportType());
            return ExportResponse.success("Email sent successfully");
        } catch (Exception e) {
            throw new ExportException("Failed to send email: " + jobId, e);
        }
    }

    /**
     * Get export history
     */
    public List<ExportJob> getExportHistory(int limit) {
        return activeJobs.values().stream()
                .sorted(Comparator.comparing(ExportJob::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Clean up old export files
     */
    public void cleanupOldExports(int daysToKeep) {
        try {
            Path exportDir = Paths.get(exportStoragePath);
            if (Files.exists(exportDir)) {
                Files.walk(exportDir)
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            try {
                                return Files.getLastModifiedTime(path).toInstant()
                                        .isBefore(Instant.now().minus(daysToKeep, ChronoUnit.DAYS));
                            } catch (IOException e) {
                                return false;
                            }
                        })
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                // Log error but continue
                                System.err.println("Failed to delete file: " + path);
                            }
                        });
            }
        } catch (IOException e) {
            System.err.println("Failed to cleanup old exports: " + e.getMessage());
        }
    }

    // Private implementation methods

    private ExportResponse executePdfExport(ExportRequest request, String jobId) {
        ExportJob job = activeJobs.get(jobId);
        job.setStatus(ExportJob.Status.PROCESSING);

        try {
            // Load data based on request type
            List<Schedule> schedules = loadSchedulesForExport(request);
            List<Teacher> teachers = loadTeachersForExport(request);
            List<Classroom> classrooms = loadClassroomsForExport(request);

            // Generate filename
            String fileName = generateFileName(request.getReportType(), ExportRequest.ExportFormat.PDF);
            Path filePath = Paths.get(exportStoragePath, fileName);

            // Generate PDF
            generatePdfReport(schedules, teachers, classrooms, request, filePath.toString());

            // Update job status
            job.setStatus(ExportJob.Status.COMPLETED);
            job.setFilePath(filePath.toString());
            job.setFileName(fileName);
            job.setCompletedAt(LocalDateTime.now());

            return ExportResponse.success("PDF report generated successfully", fileName, filePath.toString());
        } catch (Exception e) {
            job.setStatus(ExportJob.Status.FAILED);
            job.setErrorMessage(e.getMessage());
            throw new ExportException("Failed to generate PDF report", e);
        }
    }

    private ExportResponse executeExcelExport(ExportRequest request, String jobId) {
        ExportJob job = activeJobs.get(jobId);
        job.setStatus(ExportJob.Status.PROCESSING);

        try {
            // Load data based on request type
            List<Schedule> schedules = loadSchedulesForExport(request);
            List<Teacher> teachers = loadTeachersForExport(request);
            List<Classroom> classrooms = loadClassroomsForExport(request);

            // Generate filename
            String fileName = generateFileName(request.getReportType(), ExportRequest.ExportFormat.EXCEL);
            Path filePath = Paths.get(exportStoragePath, fileName);

            // Generate Excel
            generateExcelReport(schedules, teachers, classrooms, request, filePath.toString());

            // Update job status
            job.setStatus(ExportJob.Status.COMPLETED);
            job.setFilePath(filePath.toString());
            job.setFileName(fileName);
            job.setCompletedAt(LocalDateTime.now());

            return ExportResponse.success("Excel report generated successfully", fileName, filePath.toString());
        } catch (Exception e) {
            job.setStatus(ExportJob.Status.FAILED);
            job.setErrorMessage(e.getMessage());
            throw new ExportException("Failed to generate Excel report", e);
        }
    }

    private ExportResponse executeBatchExport(ExportRequest request, String jobId) {
        ExportJob job = activeJobs.get(jobId);
        job.setStatus(ExportJob.Status.PROCESSING);

        try {
            List<ExportResponse> results = new ArrayList<>();
            int totalProcessed = 0;
            int successful = 0;
            int failed = 0;

            // Process each entity in batch
            for (Long entityId : request.getEntityIds()) {
                try {
                    ExportRequest singleRequest = new ExportRequest();
                    singleRequest.setReportType(request.getReportType());
                    singleRequest.setFormat(request.getFormat());
                    singleRequest.setStartDate(request.getStartDate());
                    singleRequest.setEndDate(request.getEndDate());
                    singleRequest.setIncludeCharts(request.isIncludeCharts());
                    singleRequest.setTemplate(request.getTemplate());
                    singleRequest.setEntityIds(Collections.singletonList(entityId));

                    ExportResponse response;
                    if (request.getFormat() == ExportRequest.ExportFormat.PDF) {
                        response = executePdfExport(singleRequest, jobId + "_" + entityId);
                    } else {
                        response = executeExcelExport(singleRequest, jobId + "_" + entityId);
                    }

                    results.add(response);
                    successful++;
                } catch (Exception e) {
                    results.add(ExportResponse.failure("Failed to export entity " + entityId + ": " + e.getMessage()));
                    failed++;
                }
                totalProcessed++;
            }

            // Update job status
            job.setStatus(ExportJob.Status.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setBatchResults(results);

            return ExportResponse.batchSuccess("Batch export completed", successful, failed, totalProcessed);
        } catch (Exception e) {
            job.setStatus(ExportJob.Status.FAILED);
            job.setErrorMessage(e.getMessage());
            throw new ExportException("Failed to generate batch export", e);
        }
    }

    private void generatePdfReport(List<Schedule> schedules, List<Teacher> teachers, List<Classroom> classrooms,
                                 ExportRequest request, String outputPath) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputPath));

        document.open();

        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Paragraph title = new Paragraph(getReportTitle(request.getReportType()), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Add date range
        Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.DARK_GRAY);
        Paragraph dateRange = new Paragraph(getDateRangeText(request), dateFont);
        dateRange.setAlignment(Element.ALIGN_CENTER);
        document.add(dateRange);

        document.add(Chunk.NEWLINE);

        // Add content based on report type
        switch (request.getReportType()) {
            case SCHEDULE_OVERVIEW:
                addScheduleOverviewPdf(document, schedules, request);
                break;
            case TEACHER_WORKLOAD:
                addTeacherWorkloadPdf(document, teachers, schedules, request);
                break;
            case CLASSROOM_UTILIZATION:
                addClassroomUtilizationPdf(document, classrooms, schedules, request);
                break;
            case CONFLICTS_REPORT:
                addConflictsReportPdf(document, schedules, request);
                break;
            default:
                addGenericReportPdf(document, schedules, teachers, classrooms, request);
        }

        document.close();
    }

    private void generateExcelReport(List<Schedule> schedules, List<Teacher> teachers, List<Classroom> classrooms,
                                    ExportRequest request, String outputPath) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Create sheets based on report type
            switch (request.getReportType()) {
                case SCHEDULE_OVERVIEW:
                    createScheduleOverviewSheet(workbook, schedules, request);
                    createSummarySheet(workbook, schedules, teachers, classrooms, request);
                    break;
                case TEACHER_WORKLOAD:
                    createTeacherWorkloadSheet(workbook, teachers, schedules, request);
                    createDepartmentSummarySheet(workbook, teachers, schedules, request);
                    break;
                case CLASSROOM_UTILIZATION:
                    createClassroomUtilizationSheet(workbook, classrooms, schedules, request);
                    createRoomTypeSummarySheet(workbook, classrooms, schedules, request);
                    break;
                case CONFLICTS_REPORT:
                    createConflictsReportSheet(workbook, schedules, request);
                    break;
                default:
                    createGenericReportSheet(workbook, schedules, teachers, classrooms, request);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
        }
    }

    private void addScheduleOverviewPdf(Document document, List<Schedule> schedules, ExportRequest request) throws Exception {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        // Create table
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        // Add headers
        table.addCell(createCell("Date", headerFont));
        table.addCell(createCell("Time", headerFont));
        table.addCell(createCell("Course", headerFont));
        table.addCell(createCell("Teacher", headerFont));
        table.addCell(createCell("Classroom", headerFont));
        table.addCell(createCell("Status", headerFont));

        // Add data
        for (Schedule schedule : schedules) {
            table.addCell(createCell(schedule.getScheduleDate().toString(), dataFont));
            table.addCell(createCell(schedule.getTimeSlot().getSlotDescription(), dataFont));
            table.addCell(createCell(schedule.getCourseOffering().getCourseCode(), dataFont));
            table.addCell(createCell(schedule.getCourseOffering().getTeacher().getFullName(), dataFont));
            table.addCell(createCell(schedule.getClassroom().getRoomCode(), dataFont));
            table.addCell(createCell(schedule.getStatus().toString(), dataFont));
        }

        document.add(table);
    }

    private void createScheduleOverviewSheet(XSSFWorkbook workbook, List<Schedule> schedules, ExportRequest request) {
        XSSFSheet sheet = workbook.createSheet("Schedule Overview");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date", "Time", "Course", "Teacher", "Classroom", "Status", "Enrollment"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderStyle(workbook));
        }

        // Add data
        int rowNum = 1;
        for (Schedule schedule : schedules) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(schedule.getScheduleDate().toString());
            row.createCell(1).setCellValue(schedule.getTimeSlot().getSlotDescription());
            row.createCell(2).setCellValue(schedule.getCourseOffering().getCourseCode());
            row.createCell(3).setCellValue(schedule.getCourseOffering().getTeacher().getFullName());
            row.createCell(4).setCellValue(schedule.getClassroom().getRoomCode());
            row.createCell(5).setCellValue(schedule.getStatus().toString());
            row.createCell(6).setCellValue(schedule.getCourseOffering().getCurrentEnrollment());
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // Helper methods for data loading

    private List<Schedule> loadSchedulesForExport(ExportRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            return scheduleMapper.findByDateRange(request.getStartDate(), request.getEndDate());
        } else if (!request.getEntityIds().isEmpty()) {
            return scheduleMapper.findByIds(request.getEntityIds());
        } else {
            return scheduleMapper.findAll();
        }
    }

    private List<Teacher> loadTeachersForExport(ExportRequest request) {
        if (!request.getEntityIds().isEmpty()) {
            return teacherMapper.findByIds(request.getEntityIds());
        } else {
            return teacherMapper.findAllActive();
        }
    }

    private List<Classroom> loadClassroomsForExport(ExportRequest request) {
        if (!request.getEntityIds().isEmpty()) {
            return classroomMapper.findByIds(request.getEntityIds());
        } else {
            return classroomMapper.findAllActive();
        }
    }

    // Utility methods

    private String generateFileName(ExportRequest.ReportType reportType, ExportRequest.ExportFormat format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return String.format("%s_%s.%s",
                reportType.toString().toLowerCase(),
                timestamp,
                format.toString().toLowerCase());
    }

    private String getReportTitle(ExportRequest.ReportType reportType) {
        switch (reportType) {
            case SCHEDULE_OVERVIEW:
                return "Schedule Overview Report";
            case TEACHER_WORKLOAD:
                return "Teacher Workload Analysis";
            case CLASSROOM_UTILIZATION:
                return "Classroom Utilization Report";
            case CONFLICTS_REPORT:
                return "Schedule Conflicts Report";
            default:
                return "Scheduling Report";
        }
    }

    private String getDateRangeText(ExportRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            return String.format("Period: %s to %s",
                    request.getStartDate().toString(),
                    request.getEndDate().toString());
        }
        return "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorder(Rectangle.BOX);
        return cell;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        XSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    private void sendEmailWithAttachment(String filePath, List<String> recipients, String fileName, ExportRequest.ReportType reportType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject(getReportTitle(reportType) + " - Export Complete");
            helper.setText("Please find attached the exported report.");

            FileSystemResource file = new FileSystemResource(new File(filePath));
            helper.addAttachment(fileName, file);

            mailSender.send(message);
        } catch (Exception e) {
            throw new ExportException("Failed to send email", e);
        }
    }

    // Template and formatting system

    private void addTeacherWorkloadPdf(Document document, List<Teacher> teachers, List<Schedule> schedules, ExportRequest request) throws Exception {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        // Create title
        Paragraph title = new Paragraph("Teacher Workload Analysis", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // Create table
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        // Add headers
        table.addCell(createCell("Teacher", headerFont));
        table.addCell(createCell("Department", headerFont));
        table.addCell(createCell("Courses", headerFont));
        table.addCell(createCell("Hours/Week", headerFont));
        table.addCell(createCell("Utilization", headerFont));

        // Calculate teacher workloads
        Map<Long, TeacherWorkload> workloads = calculateTeacherWorkloads(teachers, schedules);

        // Add data
        for (Teacher teacher : teachers) {
            TeacherWorkload workload = workloads.get(teacher.getId());
            table.addCell(createCell(teacher.getFullName(), dataFont));
            table.addCell(createCell(teacher.getDepartment().getName(), dataFont));
            table.addCell(createCell(String.valueOf(workload != null ? workload.courseCount : 0), dataFont));
            table.addCell(createCell(String.valueOf(workload != null ? workload.totalHours : 0), dataFont));
            table.addCell(createCell(workload != null ? workload.utilizationPercentage + "%" : "0%", dataFont));
        }

        document.add(table);
    }

    private void addClassroomUtilizationPdf(Document document, List<Classroom> classrooms, List<Schedule> schedules, ExportRequest request) throws Exception {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        // Create title
        Paragraph title = new Paragraph("Classroom Utilization Report", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // Create table
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        // Add headers
        table.addCell(createCell("Classroom", headerFont));
        table.addCell(createCell("Type", headerFont));
        table.addCell(createCell("Capacity", headerFont));
        table.addCell(createCell("Scheduled", headerFont));
        table.addCell(createCell("Available", headerFont));
        table.addCell(createCell("Utilization", headerFont));

        // Calculate classroom utilization
        Map<Long, ClassroomUtilization> utilization = calculateClassroomUtilization(classrooms, schedules);

        // Add data
        for (Classroom classroom : classrooms) {
            ClassroomUtilization util = utilization.get(classroom.getId());
            table.addCell(createCell(classroom.getRoomCode(), dataFont));
            table.addCell(createCell(classroom.getRoomType(), dataFont));
            table.addCell(createCell(String.valueOf(classroom.getCapacity()), dataFont));
            table.addCell(createCell(String.valueOf(util != null ? util.scheduledHours : 0), dataFont));
            table.addCell(createCell(String.valueOf(util != null ? util.availableHours : 0), dataFont));
            table.addCell(createCell(util != null ? util.utilizationPercentage + "%" : "0%", dataFont));
        }

        document.add(table);
    }

    private void addConflictsReportPdf(Document document, List<Schedule> schedules, ExportRequest request) throws Exception {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        Font conflictFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.RED);

        // Create title
        Paragraph title = new Paragraph("Schedule Conflicts Report", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // Find conflicts
        List<ScheduleConflict> conflicts = findScheduleConflicts(schedules);

        if (conflicts.isEmpty()) {
            Paragraph noConflicts = new Paragraph("No schedule conflicts detected.", dataFont);
            noConflicts.setAlignment(Element.ALIGN_CENTER);
            document.add(noConflicts);
            return;
        }

        // Create table
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        // Add headers
        table.addCell(createCell("Date", headerFont));
        table.addCell(createCell("Time", headerFont));
        table.addCell(createCell("Type", headerFont));
        table.addCell(createCell("Classroom", headerFont));
        table.addCell(createCell("Severity", headerFont));

        // Add conflicts
        for (ScheduleConflict conflict : conflicts) {
            table.addCell(createCell(conflict.getDate().toString(), dataFont));
            table.addCell(createCell(conflict.getTimeRange(), dataFont));
            table.addCell(createCell(conflict.getType(), conflictFont));
            table.addCell(createCell(conflict.getClassroom(), dataFont));
            table.addCell(createCell(conflict.getSeverity(), conflictFont));
        }

        document.add(table);
    }

    private void createTeacherWorkloadSheet(XSSFWorkbook workbook, List<Teacher> teachers, List<Schedule> schedules, ExportRequest request) {
        XSSFSheet sheet = workbook.createSheet("Teacher Workload");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Teacher ID", "Teacher Name", "Department", "Title", "Courses Assigned",
                          "Weekly Hours", "Max Hours", "Utilization %", "Status"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderStyle(workbook));
        }

        // Calculate workloads
        Map<Long, TeacherWorkload> workloads = calculateTeacherWorkloads(teachers, schedules);

        // Add data
        int rowNum = 1;
        for (Teacher teacher : teachers) {
            Row row = sheet.createRow(rowNum++);
            TeacherWorkload workload = workloads.get(teacher.getId());

            row.createCell(0).setCellValue(teacher.getId());
            row.createCell(1).setCellValue(teacher.getFullName());
            row.createCell(2).setCellValue(teacher.getDepartment().getName());
            row.createCell(3).setCellValue(teacher.getTitle().toString());
            row.createCell(4).setCellValue(workload != null ? workload.courseCount : 0);
            row.createCell(5).setCellValue(workload != null ? workload.totalHours : 0);
            row.createCell(6).setCellValue(teacher.getMaxWeeklyHours().doubleValue());

            double utilization = 0;
            if (workload != null && teacher.getMaxWeeklyHours() != null) {
                utilization = (workload.totalHours / teacher.getMaxWeeklyHours().doubleValue()) * 100;
            }
            row.createCell(7).setCellValue(utilization);

            String status = "Normal";
            if (utilization > 100) {
                status = "Overloaded";
            } else if (utilization < 50) {
                status = "Underutilized";
            }
            row.createCell(8).setCellValue(status);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Add conditional formatting
        addConditionalFormatting(sheet, 7, 8); // Utilization column
    }

    private void createClassroomUtilizationSheet(XSSFWorkbook workbook, List<Classroom> classrooms, List<Schedule> schedules, ExportRequest request) {
        XSSFSheet sheet = workbook.createSheet("Classroom Utilization");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Room Code", "Building", "Room Type", "Capacity", "Scheduled Hours",
                          "Available Hours", "Utilization %", "Equipment", "Status"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderStyle(workbook));
        }

        // Calculate utilization
        Map<Long, ClassroomUtilization> utilization = calculateClassroomUtilization(classrooms, schedules);

        // Add data
        int rowNum = 1;
        for (Classroom classroom : classrooms) {
            Row row = sheet.createRow(rowNum++);
            ClassroomUtilization util = utilization.get(classroom.getId());

            row.createCell(0).setCellValue(classroom.getRoomCode());
            row.createCell(1).setCellValue(classroom.getBuildingCode());
            row.createCell(2).setCellValue(classroom.getRoomType());
            row.createCell(3).setCellValue(classroom.getCapacity());
            row.createCell(4).setCellValue(util != null ? util.scheduledHours : 0);
            row.createCell(5).setCellValue(util != null ? util.availableHours : 40); // Assume 40 hours available

            double utilizationPercent = 0;
            if (util != null && util.availableHours > 0) {
                utilizationPercent = (util.scheduledHours / util.availableHours) * 100;
            }
            row.createCell(6).setCellValue(utilizationPercent);

            row.createCell(7).setCellValue(String.join(", ", classroom.getEquipmentList()));

            String status = "Available";
            if (utilizationPercent > 90) {
                status = "High Demand";
            } else if (utilizationPercent > 75) {
                status = "Well Utilized";
            } else if (utilizationPercent < 25) {
                status = "Underutilized";
            }
            row.createCell(8).setCellValue(status);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Add conditional formatting
        addConditionalFormatting(sheet, 6, 9); // Utilization column
    }

    private void createConflictsReportSheet(XSSFWorkbook workbook, List<Schedule> schedules, ExportRequest request) {
        XSSFSheet sheet = workbook.createSheet("Schedule Conflicts");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date", "Time", "Conflict Type", "Classroom", "Teacher", "Course", "Severity", "Status"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderStyle(workbook));
        }

        // Find conflicts
        List<ScheduleConflict> conflicts = findScheduleConflicts(schedules);

        // Add conflicts
        int rowNum = 1;
        for (ScheduleConflict conflict : conflicts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(conflict.getDate().toString());
            row.createCell(1).setCellValue(conflict.getTimeRange());
            row.createCell(2).setCellValue(conflict.getType());
            row.createCell(3).setCellValue(conflict.getClassroom());
            row.createCell(4).setCellValue(conflict.getTeacher());
            row.createCell(5).setCellValue(conflict.getCourse());
            row.createCell(6).setCellValue(conflict.getSeverity());
            row.createCell(7).setCellValue(conflict.getStatus());
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // Helper methods for calculations

    private Map<Long, TeacherWorkload> calculateTeacherWorkloads(List<Teacher> teachers, List<Schedule> schedules) {
        Map<Long, TeacherWorkload> workloads = new HashMap<>();

        // Initialize
        for (Teacher teacher : teachers) {
            TeacherWorkload workload = new TeacherWorkload();
            workload.teacherId = teacher.getId();
            workload.courseCount = 0;
            workload.totalHours = 0;
            workloads.put(teacher.getId(), workload);
        }

        // Calculate from schedules
        for (Schedule schedule : schedules) {
            Long teacherId = schedule.getCourseOffering().getTeacher().getId();
            TeacherWorkload workload = workloads.get(teacherId);
            if (workload != null) {
                workload.courseCount++;
                workload.totalHours += schedule.getTimeSlot().getDurationHours();
            }
        }

        // Calculate utilization percentages
        for (Teacher teacher : teachers) {
            TeacherWorkload workload = workloads.get(teacher.getId());
            if (workload != null && teacher.getMaxWeeklyHours() != null) {
                workload.utilizationPercentage = (int) ((workload.totalHours / teacher.getMaxWeeklyHours().doubleValue()) * 100);
            }
        }

        return workloads;
    }

    private Map<Long, ClassroomUtilization> calculateClassroomUtilization(List<Classroom> classrooms, List<Schedule> schedules) {
        Map<Long, ClassroomUtilization> utilization = new HashMap<>();

        // Initialize
        for (Classroom classroom : classrooms) {
            ClassroomUtilization util = new ClassroomUtilization();
            util.classroomId = classroom.getId();
            util.scheduledHours = 0;
            util.availableHours = 40; // Assume 40 hours per week
            utilization.put(classroom.getId(), util);
        }

        // Calculate from schedules
        for (Schedule schedule : schedules) {
            Long classroomId = schedule.getClassroom().getId();
            ClassroomUtilization util = utilization.get(classroomId);
            if (util != null) {
                util.scheduledHours += schedule.getTimeSlot().getDurationHours();
            }
        }

        // Calculate utilization percentages
        for (ClassroomUtilization util : utilization.values()) {
            if (util.availableHours > 0) {
                util.utilizationPercentage = (int) ((util.scheduledHours / util.availableHours) * 100);
            }
        }

        return utilization;
    }

    private List<ScheduleConflict> findScheduleConflicts(List<Schedule> schedules) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        // Group by date and time slot
        Map<String, List<Schedule>> scheduleMap = new HashMap<>();
        for (Schedule schedule : schedules) {
            String key = schedule.getScheduleDate() + "_" + schedule.getTimeSlotId();
            scheduleMap.computeIfAbsent(key, k -> new ArrayList<>()).add(schedule);
        }

        // Find conflicts
        for (List<Schedule> sameTimeSchedules : scheduleMap.values()) {
            if (sameTimeSchedules.size() > 1) {
                // Check classroom conflicts
                Map<Long, List<Schedule>> classroomMap = new HashMap<>();
                for (Schedule schedule : sameTimeSchedules) {
                    classroomMap.computeIfAbsent(schedule.getClassroomId(), k -> new ArrayList<>()).add(schedule);
                }

                for (List<Schedule> classroomSchedules : classroomMap.values()) {
                    if (classroomSchedules.size() > 1) {
                        for (int i = 0; i < classroomSchedules.size() - 1; i++) {
                            for (int j = i + 1; j < classroomSchedules.size(); j++) {
                                ScheduleConflict conflict = new ScheduleConflict();
                                conflict.setDate(classroomSchedules.get(i).getScheduleDate());
                                conflict.setTimeRange(classroomSchedules.get(i).getTimeSlot().getTimeRange());
                                conflict.setType("Classroom Double Booking");
                                conflict.setClassroom(classroomSchedules.get(i).getClassroom().getRoomCode());
                                conflict.setTeacher(classroomSchedules.get(i).getCourseOffering().getTeacher().getFullName());
                                conflict.setCourse(classroomSchedules.get(i).getCourseOffering().getCourseCode());
                                conflict.setSeverity("High");
                                conflict.setStatus("Unresolved");
                                conflicts.add(conflict);
                            }
                        }
                    }
                }
            }
        }

        return conflicts;
    }

    private void addConditionalFormatting(XSSFSheet sheet, int columnIndex, int rowCount) {
        // This is a simplified version - in a real implementation, you would add proper conditional formatting
        // For now, we'll just set some basic formatting
        XSSFSheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        // Create conditional formatting rules
        // Note: This is a placeholder for actual conditional formatting implementation
        // In a real implementation, you would create specific formatting rules
    }

    // Helper classes for calculations

    private static class TeacherWorkload {
        Long teacherId;
        int courseCount;
        double totalHours;
        int utilizationPercentage;
    }

    private static class ClassroomUtilization {
        Long classroomId;
        int scheduledHours;
        int availableHours;
        int utilizationPercentage;
    }

    private static class ScheduleConflict {
        LocalDate date;
        String timeRange;
        String type;
        String classroom;
        String teacher;
        String course;
        String severity;
        String status;

        // Getters
        public LocalDate getDate() { return date; }
        public String getTimeRange() { return timeRange; }
        public String getType() { return type; }
        public String getClassroom() { return classroom; }
        public String getTeacher() { return teacher; }
        public String getCourse() { return course; }
        public String getSeverity() { return severity; }
        public String getStatus() { return status; }
    }

    // Export job tracking
    public static class ExportJob {
        private final String jobId;
        private final ExportRequest.ExportFormat format;
        private final ExportRequest request;
        private final LocalDateTime createdAt;
        private LocalDateTime completedAt;
        private Status status;
        private String filePath;
        private String fileName;
        private String errorMessage;
        private boolean batchExport;
        private List<ExportResponse> batchResults;

        public enum Status {
            PENDING, PROCESSING, COMPLETED, FAILED
        }

        public ExportJob(String jobId, ExportRequest.ExportFormat format, ExportRequest request) {
            this.jobId = jobId;
            this.format = format;
            this.request = request;
            this.createdAt = LocalDateTime.now();
            this.status = Status.PENDING;
        }

        // Getters and setters
        public String getJobId() { return jobId; }
        public ExportRequest.ExportFormat getFormat() { return format; }
        public ExportRequest getRequest() { return request; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public boolean isBatchExport() { return batchExport; }
        public void setBatchExport(boolean batchExport) { this.batchExport = batchExport; }
        public List<ExportResponse> getBatchResults() { return batchResults; }
        public void setBatchResults(List<ExportResponse> batchResults) { this.batchResults = batchResults; }
        public ExportRequest.ReportType getReportType() { return request.getReportType(); }
    }
}