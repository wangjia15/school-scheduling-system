package com.school.scheduling.validation.constraint;

import com.school.scheduling.domain.Schedule;
import com.school.scheduling.validation.constraint.ValidCourseSchedule;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

public class CourseScheduleValidator implements ConstraintValidator<ValidCourseSchedule, Schedule> {

    @Override
    public boolean isValid(Schedule schedule, ConstraintValidatorContext context) {
        if (schedule == null) {
            return false;
        }

        boolean isValid = true;

        if (schedule.getCourseOffering() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Course offering is required")
                   .addPropertyNode("courseOffering")
                   .addConstraintViolation();
            isValid = false;
        }

        if (schedule.getClassroom() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Classroom is required")
                   .addPropertyNode("classroom")
                   .addConstraintViolation();
            isValid = false;
        }

        if (schedule.getTimeSlot() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Time slot is required")
                   .addPropertyNode("timeSlot")
                   .addConstraintViolation();
            isValid = false;
        }

        if (schedule.getScheduleDate() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Schedule date is required")
                   .addPropertyNode("scheduleDate")
                   .addConstraintViolation();
            isValid = false;
        }

        if (schedule.getScheduleDate() != null) {
            LocalDate today = LocalDate.now();
            if (schedule.getScheduleDate().isBefore(today)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Schedule date cannot be in the past")
                       .addPropertyNode("scheduleDate")
                       .addConstraintViolation();
                isValid = false;
            }

            if (schedule.getScheduleDate().isAfter(today.plusYears(2))) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Schedule date cannot be more than 2 years in the future")
                       .addPropertyNode("scheduleDate")
                       .addConstraintViolation();
                isValid = false;
            }
        }

        if (schedule.getTimeSlot() != null && schedule.getTimeSlot().getDayOfWeek() != null && schedule.getScheduleDate() != null) {
            DayOfWeek scheduledDay = schedule.getTimeSlot().getDayOfWeek();
            DayOfWeek actualDay = schedule.getScheduleDate().getDayOfWeek();

            if (!scheduledDay.equals(actualDay)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Time slot day must match schedule date")
                       .addPropertyNode("timeSlot")
                       .addConstraintViolation();
                isValid = false;
            }
        }

        if (schedule.getTimeSlot() != null && schedule.getTimeSlot().getStartTime() != null && schedule.getTimeSlot().getEndTime() != null) {
            Duration duration = Duration.between(schedule.getTimeSlot().getStartTime(), schedule.getTimeSlot().getEndTime());
            if (duration.toMinutes() < 30) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Schedule duration must be at least 30 minutes")
                       .addPropertyNode("timeSlot")
                       .addConstraintViolation();
                isValid = false;
            }

            if (duration.toHours() > 4) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Schedule duration cannot exceed 4 hours")
                       .addPropertyNode("timeSlot")
                       .addConstraintViolation();
                isValid = false;
            }
        }

        if (schedule.isRecurring() && schedule.getRecurrencePattern() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Recurrence pattern is required for recurring schedules")
                   .addPropertyNode("recurrencePattern")
                   .addConstraintViolation();
            isValid = false;
        }

        if (!schedule.isRecurring() && schedule.getRecurrencePattern() != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Recurrence pattern should not be specified for non-recurring schedules")
                   .addPropertyNode("recurrencePattern")
                   .addConstraintViolation();
            isValid = false;
        }

        if (schedule.getRecurrencePattern() != null) {
            if (!schedule.getRecurrencePattern().matches("^(WEEKLY|BIWEEKLY|CUSTOM)$")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Invalid recurrence pattern. Use WEEKLY, BIWEEKLY, or CUSTOM")
                       .addPropertyNode("recurrencePattern")
                       .addConstraintViolation();
                isValid = false;
            }
        }

        if (schedule.getNotes() != null && schedule.getNotes().length() > 1000) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Notes cannot exceed 1000 characters")
                   .addPropertyNode("notes")
                   .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}