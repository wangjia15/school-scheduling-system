package com.school.scheduling.validation.constraint;

import com.school.scheduling.validation.constraint.ValidRoomCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RoomCodeValidator implements ConstraintValidator<ValidRoomCode, String> {

    @Override
    public boolean isValid(String roomCode, ConstraintValidatorContext context) {
        if (roomCode == null || roomCode.trim().isEmpty()) {
            return false;
        }

        String trimmed = roomCode.trim();

        if (!trimmed.matches("^[A-Z]{2,4}-[0-9A-Z]{1,10}$")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Room code must be in format: BUILDING-ROOM (e.g., SCI-101)")
                   .addConstraintViolation();
            return false;
        }

        String[] parts = trimmed.split("-");
        if (parts.length != 2) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Room code must contain exactly one hyphen")
                   .addConstraintViolation();
            return false;
        }

        String buildingCode = parts[0];
        String roomNumber = parts[1];

        if (buildingCode.length() < 2 || buildingCode.length() > 4) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Building code must be 2-4 characters")
                   .addConstraintViolation();
            return false;
        }

        if (roomNumber.isEmpty() || roomNumber.length() > 10) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Room number must be 1-10 characters")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}