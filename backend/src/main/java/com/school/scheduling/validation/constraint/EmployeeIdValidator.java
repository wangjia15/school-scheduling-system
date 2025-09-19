package com.school.scheduling.validation.constraint;

import com.school.scheduling.validation.constraint.ValidEmployeeId;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmployeeIdValidator implements ConstraintValidator<ValidEmployeeId, String> {

    @Override
    public boolean isValid(String employeeId, ConstraintValidatorContext context) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return false;
        }

        String trimmed = employeeId.trim();

        if (trimmed.length() < 3 || trimmed.length() > 20) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Employee ID must be between 3 and 20 characters")
                   .addConstraintViolation();
            return false;
        }

        if (!trimmed.matches("^[A-Z0-9\\-]+$")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Employee ID can only contain uppercase letters, numbers, and hyphens")
                   .addConstraintViolation();
            return false;
        }

        if (trimmed.startsWith("-") || trimmed.endsWith("-")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Employee ID cannot start or end with a hyphen")
                   .addConstraintViolation();
            return false;
        }

        if (trimmed.contains("--")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Employee ID cannot contain consecutive hyphens")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}