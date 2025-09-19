package com.school.scheduling.validation.constraint;

import com.school.scheduling.domain.Teacher;
import com.school.scheduling.validation.constraint.ValidTeacherWorkload;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class TeacherWorkloadValidator implements ConstraintValidator<ValidTeacherWorkload, Teacher> {

    @Override
    public boolean isValid(Teacher teacher, ConstraintValidatorContext context) {
        if (teacher == null) {
            return false;
        }

        boolean isValid = true;

        if (teacher.getMaxWeeklyHours() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Maximum weekly hours is required")
                   .addPropertyNode("maxWeeklyHours")
                   .addConstraintViolation();
            isValid = false;
        } else {
            if (teacher.getMaxWeeklyHours().compareTo(BigDecimal.ZERO) <= 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Maximum weekly hours must be positive")
                       .addPropertyNode("maxWeeklyHours")
                       .addConstraintViolation();
                isValid = false;
            }

            if (teacher.getMaxWeeklyHours().compareTo(new BigDecimal("60")) > 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Maximum weekly hours cannot exceed 60")
                       .addPropertyNode("maxWeeklyHours")
                       .addConstraintViolation();
                isValid = false;
            }

            if (teacher.getMaxWeeklyHours().compareTo(new BigDecimal("10")) < 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Maximum weekly hours is very low (minimum 10 recommended)")
                       .addPropertyNode("maxWeeklyHours")
                       .addConstraintViolation();
            }
        }

        if (teacher.getMaxCoursesPerSemester() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Maximum courses per semester is required")
                   .addPropertyNode("maxCoursesPerSemester")
                   .addConstraintViolation();
            isValid = false;
        } else {
            if (teacher.getMaxCoursesPerSemester() < 1) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Maximum courses per semester must be at least 1")
                       .addPropertyNode("maxCoursesPerSemester")
                       .addConstraintViolation();
                isValid = false;
            }

            if (teacher.getMaxCoursesPerSemester() > 10) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Maximum courses per semester cannot exceed 10")
                       .addPropertyNode("maxCoursesPerSemester")
                       .addConstraintViolation();
                isValid = false;
            }
        }

        if (teacher.getMaxWeeklyHours() != null && teacher.getMaxCoursesPerSemester() != null) {
            BigDecimal minHoursPerCourse = teacher.getMaxWeeklyHours().divide(
                new BigDecimal(teacher.getMaxCoursesPerSemester()), 2, java.math.RoundingMode.HALF_UP);

            if (minHoursPerCourse.compareTo(new BigDecimal("2")) < 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("Workload distribution may be inadequate: %.2f hours per course on average", minHoursPerCourse))
                       .addPropertyNode("maxWeeklyHours")
                       .addConstraintViolation();
            }

            if (minHoursPerCourse.compareTo(new BigDecimal("8")) > 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("Workload distribution may be excessive: %.2f hours per course on average", minHoursPerCourse))
                       .addPropertyNode("maxWeeklyHours")
                       .addConstraintViolation();
            }
        }

        if (teacher.getTitle() != null) {
            switch (teacher.getTitle()) {
                case PROFESSOR:
                    if (teacher.getMaxCoursesPerSemester() < 3) {
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate("Professors typically teach at least 3 courses per semester")
                               .addPropertyNode("maxCoursesPerSemester")
                               .addConstraintViolation();
                    }
                    break;
                case INSTRUCTOR:
                case ADJUNCT:
                    if (teacher.getMaxCoursesPerSemester() > 5) {
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate("Instructors and adjuncts typically teach no more than 5 courses per semester")
                               .addPropertyNode("maxCoursesPerSemester")
                               .addConstraintViolation();
                    }
                    break;
            }
        }

        if (teacher.getSpecializations() != null && teacher.getSpecializations().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Teacher should have at least one specialization")
                   .addPropertyNode("specializations")
                   .addConstraintViolation();
        }

        return isValid;
    }
}