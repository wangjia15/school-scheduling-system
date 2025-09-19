package com.school.scheduling.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for export operations
 */
public class ExportRequest {

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    @NotNull(message = "Export format is required")
    private ExportFormat format;

    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 500, message = "Template name cannot exceed 500 characters")
    private String template;

    private boolean includeCharts = false;

    private boolean includeAnalytics = true;

    private boolean emailReport = false;

    @Size(max = 1000, message = "Email list cannot exceed 1000 characters")
    private String emailRecipients;

    private List<Long> entityIds;

    private String customTitle;

    private String customDescription;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 1000, message = "Page size cannot exceed 1000")
    private int pageSize = 50;

    private boolean landscape = false;

    private String paperSize = "A4";

    private boolean includeHeaderFooter = true;

    private String watermark;

    public enum ReportType {
        SCHEDULE_OVERVIEW,
        TEACHER_WORKLOAD,
        CLASSROOM_UTILIZATION,
        CONFLICTS_REPORT,
        STUDENT_SCHEDULES,
        COURSE_CATALOG,
        ROOM_ALLOCATION,
        DEPARTMENT_SUMMARY,
        COMPREHENSIVE_REPORT
    }

    public enum ExportFormat {
        PDF,
        EXCEL,
        CSV,
        JSON
    }

    // Constructors
    public ExportRequest() {}

    public ExportRequest(ReportType reportType, ExportFormat format) {
        this.reportType = reportType;
        this.format = format;
    }

    // Getters and Setters
    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }

    public ExportFormat getFormat() { return format; }
    public void setFormat(ExportFormat format) { this.format = format; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public boolean isIncludeCharts() { return includeCharts; }
    public void setIncludeCharts(boolean includeCharts) { this.includeCharts = includeCharts; }

    public boolean isIncludeAnalytics() { return includeAnalytics; }
    public void setIncludeAnalytics(boolean includeAnalytics) { this.includeAnalytics = includeAnalytics; }

    public boolean isEmailReport() { return emailReport; }
    public void setEmailReport(boolean emailReport) { this.emailReport = emailReport; }

    public String getEmailRecipients() { return emailRecipients; }
    public void setEmailRecipients(String emailRecipients) { this.emailRecipients = emailRecipients; }

    public List<Long> getEntityIds() { return entityIds; }
    public void setEntityIds(List<Long> entityIds) { this.entityIds = entityIds; }

    public String getCustomTitle() { return customTitle; }
    public void setCustomTitle(String customTitle) { this.customTitle = customTitle; }

    public String getCustomDescription() { return customDescription; }
    public void setCustomDescription(String customDescription) { this.customDescription = customDescription; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public boolean isLandscape() { return landscape; }
    public void setLandscape(boolean landscape) { this.landscape = landscape; }

    public String getPaperSize() { return paperSize; }
    public void setPaperSize(String paperSize) { this.paperSize = paperSize; }

    public boolean isIncludeHeaderFooter() { return includeHeaderFooter; }
    public void setIncludeHeaderFooter(boolean includeHeaderFooter) { this.includeHeaderFooter = includeHeaderFooter; }

    public String getWatermark() { return watermark; }
    public void setWatermark(String watermark) { this.watermark = watermark; }

    @Override
    public String toString() {
        return "ExportRequest{" +
                "reportType=" + reportType +
                ", format=" + format +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", template='" + template + '\'' +
                ", includeCharts=" + includeCharts +
                ", includeAnalytics=" + includeAnalytics +
                ", emailReport=" + emailReport +
                ", entityIds=" + entityIds +
                '}';
    }
}