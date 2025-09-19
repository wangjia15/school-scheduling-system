package com.school.scheduling.validation;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public abstract class BaseValidator {

    protected ValidationResult createValidationResult() {
        return new ValidationResult();
    }

    protected void validateNotNull(Object object, String fieldName, ValidationResult result) {
        if (object == null) {
            result.addError(fieldName + " cannot be null");
        }
    }

    protected void validateNotBlank(String value, String fieldName, ValidationResult result) {
        if (value == null || value.trim().isEmpty()) {
            result.addError(fieldName + " cannot be blank");
        }
    }

    protected void validateNotEmpty(List<?> list, String fieldName, ValidationResult result) {
        if (list == null || list.isEmpty()) {
            result.addError(fieldName + " cannot be empty");
        }
    }

    protected void validatePositive(Number number, String fieldName, ValidationResult result) {
        if (number == null || number.doubleValue() <= 0) {
            result.addError(fieldName + " must be positive");
        }
    }

    protected void validateNonNegative(Number number, String fieldName, ValidationResult result) {
        if (number == null || number.doubleValue() < 0) {
            result.addError(fieldName + " must be non-negative");
        }
    }

    protected void validateRange(Number number, String fieldName, Number min, Number max, ValidationResult result) {
        if (number == null) {
            result.addError(fieldName + " cannot be null");
            return;
        }

        double value = number.doubleValue();
        double minValue = min.doubleValue();
        double maxValue = max.doubleValue();

        if (value < minValue || value > maxValue) {
            result.addError(fieldName + " must be between " + min + " and " + max);
        }
    }

    protected void validateMaxLength(String value, String fieldName, int maxLength, ValidationResult result) {
        if (value != null && value.length() > maxLength) {
            result.addError(fieldName + " cannot exceed " + maxLength + " characters");
        }
    }

    protected void validateMinLength(String value, String fieldName, int minLength, ValidationResult result) {
        if (value != null && value.length() < minLength) {
            result.addError(fieldName + " must be at least " + minLength + " characters");
        }
    }

    protected void addWarningIfNull(Object object, String fieldName, ValidationResult result) {
        if (object == null) {
            result.addWarning(fieldName + " is null, this may cause issues");
        }
    }

    protected void addWarningIfEmpty(List<?> list, String fieldName, ValidationResult result) {
        if (list == null || list.isEmpty()) {
            result.addWarning(fieldName + " is empty, this may cause issues");
        }
    }

    protected void validateActiveStatus(Boolean active, String fieldName, ValidationResult result) {
        if (active == null) {
            result.addError(fieldName + " status cannot be null");
        }
    }

    protected void validateEmailFormat(String email, String fieldName, ValidationResult result) {
        if (email == null) {
            result.addError(fieldName + " cannot be null");
            return;
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            result.addError(fieldName + " must be a valid email address");
        }
    }

    protected void validateUniqueField(String value, String fieldName, boolean exists, ValidationResult result) {
        if (exists) {
            result.addError(fieldName + " '" + value + "' already exists");
        }
    }

    protected void validateEntityExists(Object entity, String entityType, String identifier, ValidationResult result) {
        if (entity == null) {
            result.addError(entityType + " with identifier '" + identifier + "' does not exist");
        }
    }

    protected void validateBusinessHours(int hour, String fieldName, ValidationResult result) {
        if (hour < 6 || hour > 22) {
            result.addError(fieldName + " must be within business hours (6:00 AM - 10:00 PM)");
        }
    }

    protected void validateWeekday(int dayOfWeek, String fieldName, ValidationResult result) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            result.addError(fieldName + " must be a valid weekday (1-7)");
        }
    }

    protected void validatePercentage(Number percentage, String fieldName, ValidationResult result) {
        if (percentage == null) {
            result.addError(fieldName + " cannot be null");
            return;
        }

        double value = percentage.doubleValue();
        if (value < 0 || value > 100) {
            result.addError(fieldName + " must be between 0 and 100");
        }
    }

    protected void validateRequiredField(Object field, String fieldName, ValidationResult result) {
        if (field == null) {
            result.addError(fieldName + " is required");
        } else if (field instanceof String && ((String) field).trim().isEmpty()) {
            result.addError(fieldName + " is required");
        } else if (field instanceof List && ((List<?>) field).isEmpty()) {
            result.addError(fieldName + " is required");
        }
    }

    protected void validateDateOrder(java.time.LocalDate startDate, java.time.LocalDate endDate,
                                   String startFieldName, String endFieldName, ValidationResult result) {
        if (startDate == null || endDate == null) {
            result.addError("Both " + startFieldName + " and " + endFieldName + " are required");
            return;
        }

        if (endDate.isBefore(startDate)) {
            result.addError(endFieldName + " must be after " + startFieldName);
        }
    }

    protected void validateTimeOrder(java.time.LocalTime startTime, java.time.LocalTime endTime,
                                   String startFieldName, String endFieldName, ValidationResult result) {
        if (startTime == null || endTime == null) {
            result.addError("Both " + startFieldName + " and " + endFieldName + " are required");
            return;
        }

        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            result.addError(endFieldName + " must be after " + startFieldName);
        }
    }

    protected void validateMinimumDuration(java.time.Duration duration, java.time.Duration minimum,
                                         String fieldName, ValidationResult result) {
        if (duration == null) {
            result.addError(fieldName + " cannot be null");
            return;
        }

        if (duration.compareTo(minimum) < 0) {
            result.addError(fieldName + " must be at least " + minimum.toMinutes() + " minutes");
        }
    }

    protected void validateMaximumDuration(java.time.Duration duration, java.time.Duration maximum,
                                         String fieldName, ValidationResult result) {
        if (duration == null) {
            result.addError(fieldName + " cannot be null");
            return;
        }

        if (duration.compareTo(maximum) > 0) {
            result.addError(fieldName + " cannot exceed " + maximum.toMinutes() + " minutes");
        }
    }
}