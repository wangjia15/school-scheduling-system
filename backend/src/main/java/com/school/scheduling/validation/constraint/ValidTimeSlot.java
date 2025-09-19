package com.school.scheduling.validation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TimeSlotValidator.class)
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimeSlot {
    String message() default "Invalid time slot configuration";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}