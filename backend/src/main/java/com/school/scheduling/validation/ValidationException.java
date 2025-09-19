package com.school.scheduling.validation;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<String> validationErrors;

    public ValidationException(String message) {
        super(message);
        this.validationErrors = List.of(message);
    }

    public ValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors != null ? validationErrors : List.of();
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.validationErrors = List.of(message);
    }

    public ValidationException(ValidationResult validationResult) {
        super("Validation failed: " + validationResult.getErrorSummary());
        this.validationErrors = validationResult.getErrors();
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public boolean hasMultipleErrors() {
        return validationErrors.size() > 1;
    }

    public String getDetailedMessage() {
        if (!hasMultipleErrors()) {
            return getMessage();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Validation failed with ").append(validationErrors.size()).append(" errors:\n");
        for (int i = 0; i < validationErrors.size(); i++) {
            sb.append(i + 1).append(". ").append(validationErrors.get(i)).append("\n");
        }
        return sb.toString();
    }
}