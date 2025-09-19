package com.school.scheduling.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for export operations
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExportResponse {

    private boolean success;
    private String message;
    private String jobId;
    private String fileName;
    private String filePath;
    private String downloadUrl;
    private Long fileSize;
    private Integer successfulCount;
    private Integer failedCount;
    private Integer totalCount;
    private String status;
    private Integer progress;

    // Private constructor for builder pattern
    private ExportResponse() {}

    public static ExportResponse accepted(String jobId, String message) {
        ExportResponse response = new ExportResponse();
        response.success = true;
        response.message = message;
        response.jobId = jobId;
        response.status = "ACCEPTED";
        return response;
    }

    public static ExportResponse success(String message) {
        ExportResponse response = new ExportResponse();
        response.success = true;
        response.message = message;
        response.status = "COMPLETED";
        return response;
    }

    public static ExportResponse success(String message, String fileName, String filePath) {
        ExportResponse response = new ExportResponse();
        response.success = true;
        response.message = message;
        response.fileName = fileName;
        response.filePath = filePath;
        response.downloadUrl = "/api/v1/exports/download/" + fileName;
        response.status = "COMPLETED";
        return response;
    }

    public static ExportResponse batchSuccess(String message, int successful, int failed, int total) {
        ExportResponse response = new ExportResponse();
        response.success = true;
        response.message = message;
        response.successfulCount = successful;
        response.failedCount = failed;
        response.totalCount = total;
        response.status = "COMPLETED";
        return response;
    }

    public static ExportResponse failure(String message) {
        ExportResponse response = new ExportResponse();
        response.success = false;
        response.message = message;
        response.status = "FAILED";
        return response;
    }

    public static ExportResponse progress(String jobId, String message, int progress) {
        ExportResponse response = new ExportResponse();
        response.success = true;
        response.message = message;
        response.jobId = jobId;
        response.progress = progress;
        response.status = "IN_PROGRESS";
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getSuccessfulCount() { return successfulCount; }
    public void setSuccessfulCount(Integer successfulCount) { this.successfulCount = successfulCount; }

    public Integer getFailedCount() { return failedCount; }
    public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    @Override
    public String toString() {
        return "ExportResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", jobId='" + jobId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", status='" + status + '\'' +
                ", progress=" + progress +
                '}';
    }
}