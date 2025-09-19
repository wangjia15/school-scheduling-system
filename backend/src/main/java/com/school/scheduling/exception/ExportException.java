package com.school.scheduling.exception;

/**
 * Custom exception for export-related errors
 */
public class ExportException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public ExportException(String message) {
        super(message);
        this.errorCode = "EXPORT_ERROR";
        this.httpStatus = 500;
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "EXPORT_ERROR";
        this.httpStatus = 500;
    }

    public ExportException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public ExportException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    // Common export error codes
    public static final String ERROR_INVALID_EXPORT_FORMAT = "INVALID_EXPORT_FORMAT";
    public static final String ERROR_EXPORT_JOB_NOT_FOUND = "EXPORT_JOB_NOT_FOUND";
    public static final String ERROR_EXPORT_JOB_FAILED = "EXPORT_JOB_FAILED";
    public static final String ERROR_FILE_GENERATION_FAILED = "FILE_GENERATION_FAILED";
    public static final String ERROR_FILE_TOO_LARGE = "FILE_TOO_LARGE";
    public static final String ERROR_EMAIL_SEND_FAILED = "EMAIL_SEND_FAILED";
    public static final String ERROR_TEMPLATE_NOT_FOUND = "TEMPLATE_NOT_FOUND";
    public static final String ERROR_INVALID_DATE_RANGE = "INVALID_DATE_RANGE";
    public static final String ERROR_INSUFFICIENT_PERMISSIONS = "INSUFFICIENT_PERMISSIONS";
    public static final String ERROR_EXPORT_LIMIT_EXCEEDED = "EXPORT_LIMIT_EXCEEDED";
}