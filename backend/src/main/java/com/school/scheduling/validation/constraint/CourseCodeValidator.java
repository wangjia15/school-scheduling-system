package com.school.scheduling.validation.constraint;

import com.school.scheduling.validation.constraint.ValidCourseCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CourseCodeValidator implements ConstraintValidator<ValidCourseCode, String> {

    @Override
    public boolean isValid(String courseCode, ConstraintValidatorContext context) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            return false;
        }

        String pattern = "^[A-Z]{2,4}[0-9]{3,4}[A-Z]?$";
        if (!courseCode.matches(pattern)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Course code must match pattern: DEPT123 or DEPT1234 or DEPT1234A")
                   .addConstraintViolation();
            return false;
        }

        String deptCode = courseCode.replaceAll("[0-9A-Z]*$", "");
        if (deptCode.length() < 2 || deptCode.length() > 4) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Department code must be 2-4 characters")
                   .addConstraintViolation();
            return false;
        }

        String numberPart = courseCode.substring(deptCode.length()).replaceAll("[^0-9]", "");
        if (numberPart.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Course code must contain numbers")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}