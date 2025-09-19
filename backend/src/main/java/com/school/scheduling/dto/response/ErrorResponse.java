package com.school.scheduling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private HttpStatus status;
    private int statusCode;
    private String message;
    private String error;
    private String path;
    private LocalDateTime timestamp;
    private List<ValidationError> validationErrors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private String rejectedValue;
        private String object;
    }

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(
                status,
                status.value(),
                message,
                status.getReasonPhrase(),
                path,
                LocalDateTime.now(),
                new ArrayList<>()
        );
    }

    public static ErrorResponse of(HttpStatus status, String message, String error, String path) {
        return new ErrorResponse(
                status,
                status.value(),
                message,
                error,
                path,
                LocalDateTime.now(),
                new ArrayList<>()
        );
    }

    public static ErrorResponse validationError(String message, String path, List<ValidationError> validationErrors) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                message,
                "Validation Failed",
                path,
                LocalDateTime.now(),
                validationErrors
        );
    }

    public static ErrorResponse notFound(String message, String path) {
        return of(HttpStatus.NOT_FOUND, message, path);
    }

    public static ErrorResponse badRequest(String message, String path) {
        return of(HttpStatus.BAD_REQUEST, message, path);
    }

    public static ErrorResponse unauthorized(String message, String path) {
        return of(HttpStatus.UNAUTHORIZED, message, path);
    }

    public static ErrorResponse forbidden(String message, String path) {
        return of(HttpStatus.FORBIDDEN, message, path);
    }

    public static ErrorResponse internalServerError(String message, String path) {
        return of(HttpStatus.INTERNAL_SERVER_ERROR, message, path);
    }

    public ErrorResponse addValidationError(String field, String message, String rejectedValue, String object) {
        if (this.validationErrors == null) {
            this.validationErrors = new ArrayList<>();
        }
        this.validationErrors.add(new ValidationError(field, message, rejectedValue, object));
        return this;
    }
}