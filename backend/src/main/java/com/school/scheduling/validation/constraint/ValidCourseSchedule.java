package com.school.scheduling.validation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CourseScheduleValidator.class)
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCourseSchedule {
    String message() default "Invalid course schedule configuration";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}