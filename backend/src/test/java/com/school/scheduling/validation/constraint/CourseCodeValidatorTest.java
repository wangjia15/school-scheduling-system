package com.school.scheduling.validation.constraint;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseCodeValidatorTest {

    private CourseCodeValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder nodeBuilder;

    @BeforeEach
    void setUp() {
        validator = new CourseCodeValidator();
        when(context.disableDefaultConstraintViolation()).thenReturn(context);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void isValid_WithValidCourseCode_ShouldReturnTrue() {
        assertTrue(validator.isValid("CS101", context));
        assertTrue(validator.isValid("MATH201", context));
        assertTrue(validator.isValid("PHYS301A", context));
        assertTrue(validator.isValid("ENG102", context));
        assertTrue(validator.isValid("BIO450", context));
    }

    @Test
    void isValid_WithNullCourseCode_ShouldReturnFalse() {
        assertFalse(validator.isValid(null, context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Course code must match pattern: DEPT123 or DEPT1234 or DEPT1234A");
    }

    @Test
    void isValid_WithEmptyCourseCode_ShouldReturnFalse() {
        assertFalse(validator.isValid("", context));
        assertFalse(validator.isValid("   ", context));
    }

    @Test
    void isValid_WithInvalidFormat_ShouldReturnFalse() {
        assertFalse(validator.isValid("INVALID", context));
        verify(context).buildConstraintViolationWithTemplate("Course code must match pattern: DEPT123 or DEPT1234 or DEPT1234A");

        assertFalse(validator.isValid("CS", context));
        verify(context).buildConstraintViolationWithTemplate("Course code must match pattern: DEPT123 or DEPT1234 or DEPT1234A");

        assertFalse(validator.isValid("CS101A5", context));
        verify(context).buildConstraintViolationWithTemplate("Course code must match pattern: DEPT123 or DEPT1234 or DEPT1234A");
    }

    @Test
    void isValid_WithInvalidDepartmentCode_ShouldReturnFalse() {
        assertFalse(validator.isValid("C101", context));
        verify(context).buildConstraintViolationWithTemplate("Department code must be 2-4 characters");

        assertFalse(validator.isValid("COMPUTER101", context));
        verify(context).buildConstraintViolationWithTemplate("Department code must be 2-4 characters");
    }

    @Test
    void isValid_WithMissingNumbers_ShouldReturnFalse() {
        assertFalse(validator.isValid("CS", context));
        verify(context).buildConstraintViolationWithTemplate("Course code must contain numbers");

        assertFalse(validator.isValid("CSC", context));
        verify(context).buildConstraintViolationWithTemplate("Course code must contain numbers");
    }

    @Test
    void isValid_WithValidFormats_ShouldReturnTrue() {
        assertTrue(validator.isValid("AB123", context));
        assertTrue(validator.isValid("ABC1234", context));
        assertTrue(validator.isValid("ABCD1234A", context));
        assertTrue(validator.isValid("CS101", context));
        assertTrue(validator.isValid("MATH201A", context));
    }

    @Test
    void isValid_WithLowerCaseLetters_ShouldReturnFalse() {
        assertFalse(validator.isValid("cs101", context));
        verify(context).buildConstraintViolationWithTemplate("Course code must match pattern: DEPT123 or DEPT1234 or DEPT1234A");
    }

    @Test
    void isValid_WithSpecialCharacters_ShouldReturnFalse() {
        assertFalse(validator.isValid("CS@101", context));
        verify(context).buildConstraintViolationWithTemplate("Course code must match pattern: DEPT123 or DEPT1234 or DEPT1234A");

        assertFalse(validator.isValid("CS-101", context));
        verify(context).buildConstraintViolationWithTemplate("Course code must match pattern: DEPT123 or DEPT1234 or DEPT1234A");
    }
}