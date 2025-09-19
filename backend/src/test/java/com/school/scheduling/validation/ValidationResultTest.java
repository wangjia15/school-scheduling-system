package com.school.scheduling.validation;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationResultTest {

    @Test
    void validResult_ShouldBeValid() {
        ValidationResult result = ValidationResult.valid();

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertEquals("No validation errors", result.getErrorSummary());
        assertEquals("No validation warnings", result.getWarningSummary());
    }

    @Test
    void invalidResult_WithSingleError_ShouldBeInvalid() {
        ValidationResult result = ValidationResult.invalid("Test error");

        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertEquals(1, result.getErrors().size());
        assertEquals("Test error", result.getErrors().get(0));
        assertEquals("Test error", result.getErrorSummary());
    }

    @Test
    void invalidResult_WithMultipleErrors_ShouldBeInvalid() {
        List<String> errors = Arrays.asList("Error 1", "Error 2", "Error 3");
        ValidationResult result = ValidationResult.withErrors(errors);

        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertEquals(3, result.getErrors().size());
        assertTrue(result.getErrors().containsAll(errors));
        assertEquals("Error 1; Error 2; Error 3", result.getErrorSummary());
    }

    @Test
    void addError_ShouldMakeResultInvalid() {
        ValidationResult result = ValidationResult.valid();

        assertTrue(result.isValid());

        result.addError("Test error");

        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertEquals("Test error", result.getErrors().get(0));
    }

    @Test
    void addWarning_ShouldNotAffectValidity() {
        ValidationResult result = ValidationResult.valid();

        assertTrue(result.isValid());

        result.addWarning("Test warning");

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertEquals("Test warning", result.getWarnings().get(0));
    }

    @Test
    void merge_WithValidResults_ShouldRemainValid() {
        ValidationResult result1 = ValidationResult.valid();
        ValidationResult result2 = ValidationResult.valid();

        result1.merge(result2);

        assertTrue(result1.isValid());
        assertFalse(result1.hasErrors());
        assertFalse(result1.hasWarnings());
    }

    @Test
    void merge_WithInvalidResult_ShouldBecomeInvalid() {
        ValidationResult result1 = ValidationResult.valid();
        ValidationResult result2 = ValidationResult.invalid("Error from result2");

        result1.merge(result2);

        assertFalse(result1.isValid());
        assertTrue(result1.hasErrors());
        assertEquals("Error from result2", result1.getErrors().get(0));
    }

    @Test
    void merge_WithWarnings_ShouldCombineWarnings() {
        ValidationResult result1 = ValidationResult.valid();
        ValidationResult result2 = ValidationResult.valid();

        result1.addWarning("Warning 1");
        result2.addWarning("Warning 2");

        result1.merge(result2);

        assertTrue(result1.isValid());
        assertFalse(result1.hasErrors());
        assertTrue(result1.hasWarnings());
        assertEquals(2, result1.getWarnings().size());
        assertTrue(result1.getWarnings().contains("Warning 1"));
        assertTrue(result1.getWarnings().contains("Warning 2"));
    }

    @Test
    void merge_WithBothErrorsAndWarnings_ShouldCombineAll() {
        ValidationResult result1 = ValidationResult.valid();
        ValidationResult result2 = ValidationResult.valid();

        result1.addError("Error 1");
        result1.addWarning("Warning 1");
        result2.addError("Error 2");
        result2.addWarning("Warning 2");

        result1.merge(result2);

        assertFalse(result1.isValid());
        assertTrue(result1.hasErrors());
        assertTrue(result1.hasWarnings());
        assertEquals(2, result1.getErrors().size());
        assertEquals(2, result1.getWarnings().size());
    }

    @Test
    void throwIfInvalid_WithValidResult_ShouldNotThrow() {
        ValidationResult result = ValidationResult.valid();

        assertDoesNotThrow(result::throwIfInvalid);
    }

    @Test
    void throwIfInvalid_WithInvalidResult_ShouldThrowValidationException() {
        ValidationResult result = ValidationResult.invalid("Test error");

        ValidationException exception = assertThrows(ValidationException.class, result::throwIfInvalid);
        assertEquals("Validation failed: Test error", exception.getMessage());
        assertEquals(1, exception.getValidationErrors().size());
        assertEquals("Test error", exception.getValidationErrors().get(0));
    }

    @Test
    void toString_ShouldIncludeAllFields() {
        ValidationResult result = ValidationResult.valid();
        result.addError("Test error");
        result.addWarning("Test warning");

        String toString = result.toString();

        assertTrue(toString.contains("valid=false"));
        assertTrue(toString.contains("Test error"));
        assertTrue(toString.contains("Test warning"));
    }

    @Test
    void getErrorSummary_WithNoErrors_ShouldReturnDefaultMessage() {
        ValidationResult result = ValidationResult.valid();

        assertEquals("No validation errors", result.getErrorSummary());
    }

    @Test
    void getWarningSummary_WithNoWarnings_ShouldReturnDefaultMessage() {
        ValidationResult result = ValidationResult.valid();

        assertEquals("No validation warnings", result.getWarningSummary());
    }
}