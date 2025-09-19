package com.school.scheduling.validation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RoomCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRoomCode {
    String message() default "Invalid room code format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}