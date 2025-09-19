package com.school.scheduling.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private HttpStatus status;
    private int statusCode;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;
    private String error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                HttpStatus.OK,
                HttpStatus.OK.value(),
                "Success",
                data,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(
                HttpStatus.OK,
                HttpStatus.OK.value(),
                message,
                data,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(
                HttpStatus.CREATED,
                HttpStatus.CREATED.value(),
                "Resource created successfully",
                data,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(
                HttpStatus.CREATED,
                HttpStatus.CREATED.value(),
                message,
                data,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static <T> ApiResponse<T> updated(T data) {
        return new ApiResponse<>(
                HttpStatus.OK,
                HttpStatus.OK.value(),
                "Resource updated successfully",
                data,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static <T> ApiResponse<T> updated(T data, String message) {
        return new ApiResponse<>(
                HttpStatus.OK,
                HttpStatus.OK.value(),
                message,
                data,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static <T> ApiResponse<T> deleted() {
        return new ApiResponse<>(
                HttpStatus.OK,
                HttpStatus.OK.value(),
                "Resource deleted successfully",
                null,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static <T> ApiResponse<T> deleted(String message) {
        return new ApiResponse<>(
                HttpStatus.OK,
                HttpStatus.OK.value(),
                message,
                null,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return new ApiResponse<>(
                status,
                status.value(),
                message,
                null,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message, String error) {
        return new ApiResponse<>(
                status,
                status.value(),
                message,
                null,
                LocalDateTime.now(),
                null,
                error
        );
    }

    public ApiResponse<T> withPath(String path) {
        this.setPath(path);
        return this;
    }
}