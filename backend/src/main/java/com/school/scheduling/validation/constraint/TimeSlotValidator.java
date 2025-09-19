package com.school.scheduling.validation.constraint;

import com.school.scheduling.domain.TimeSlot;
import com.school.scheduling.validation.constraint.ValidTimeSlot;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Duration;

public class TimeSlotValidator implements ConstraintValidator<ValidTimeSlot, TimeSlot> {

    @Override
    public boolean isValid(TimeSlot timeSlot, ConstraintValidatorContext context) {
        if (timeSlot == null) {
            return false;
        }

        boolean isValid = true;

        if (timeSlot.getDayOfWeek() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Day of week is required")
                   .addPropertyNode("dayOfWeek")
                   .addConstraintViolation();
            isValid = false;
        }

        if (timeSlot.getStartTime() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Start time is required")
                   .addPropertyNode("startTime")
                   .addConstraintViolation();
            isValid = false;
        }

        if (timeSlot.getEndTime() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("End time is required")
                   .addPropertyNode("endTime")
                   .addConstraintViolation();
            isValid = false;
        }

        if (timeSlot.getStartTime() != null && timeSlot.getEndTime() != null) {
            if (!timeSlot.getStartTime().isBefore(timeSlot.getEndTime())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Start time must be before end time")
                       .addPropertyNode("startTime")
                       .addConstraintViolation();
                isValid = false;
            }

            Duration duration = Duration.between(timeSlot.getStartTime(), timeSlot.getEndTime());
            if (duration.toMinutes() < 15) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Time slot must be at least 15 minutes")
                       .addPropertyNode("startTime")
                       .addConstraintViolation();
                isValid = false;
            }

            if (duration.toHours() > 6) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Time slot cannot exceed 6 hours")
                       .addPropertyNode("startTime")
                       .addConstraintViolation();
                isValid = false;
            }
        }

        if (timeSlot.getDayOfWeek() != null) {
            if (timeSlot.getDayOfWeek() == DayOfWeek.SATURDAY || timeSlot.getDayOfWeek() == DayOfWeek.SUNDAY) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Weekend scheduling requires special approval")
                       .addPropertyNode("dayOfWeek")
                       .addConstraintViolation();
            }

            if (timeSlot.getStartTime() != null) {
                int hour = timeSlot.getStartTime().getHour();
                if (hour < 6 || hour > 22) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Start time must be within business hours (6 AM - 10 PM)")
                           .addPropertyNode("startTime")
                           .addConstraintViolation();
                }
            }
        }

        return isValid;
    }
}