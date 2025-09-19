package com.school.scheduling.validation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TeacherWorkloadValidator.class)
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTeacherWorkload {
    String message() default "Invalid teacher workload configuration";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}