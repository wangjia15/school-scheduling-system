package com.school.scheduling.validation;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class ValidationResult {
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private boolean valid = true;

    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public void merge(ValidationResult other) {
        if (other == null) return;

        this.errors.addAll(other.errors);
        this.warnings.addAll(other.warnings);
        this.valid = this.valid && other.valid;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public String getErrorSummary() {
        if (!hasErrors()) return "No validation errors";
        return String.join("; ", errors);
    }

    public String getWarningSummary() {
        if (!hasWarnings()) return "No validation warnings";
        return String.join("; ", warnings);
    }

    public static ValidationResult valid() {
        return new ValidationResult();
    }

    public static ValidationResult invalid(String error) {
        ValidationResult result = new ValidationResult();
        result.addError(error);
        return result;
    }

    public static ValidationResult withErrors(List<String> errors) {
        ValidationResult result = new ValidationResult();
        errors.forEach(result::addError);
        return result;
    }

    public void throwIfInvalid() {
        if (!isValid()) {
            throw new ValidationException("Validation failed: " + getErrorSummary());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationResult{");
        sb.append("valid=").append(valid);
        sb.append(", errors=").append(errors);
        sb.append(", warnings=").append(warnings);
        sb.append('}');
        return sb.toString();
    }
}