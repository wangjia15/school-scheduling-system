package com.school.scheduling.validation;

import com.school.scheduling.domain.Course;
import com.school.scheduling.domain.Schedule;
import com.school.scheduling.domain.Semester;
import com.school.scheduling.domain.Teacher;
import com.school.scheduling.domain.TimeSlot;
import com.school.scheduling.domain.Classroom;
import com.school.scheduling.domain.CourseOffering;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ScheduleValidator extends BaseValidator {

    public ValidationResult validateScheduleForCreation(Schedule schedule) {
        ValidationResult result = createValidationResult();

        validateRequiredField(schedule, "Schedule", result);
        if (!result.isValid()) {
            return result;
        }

        validateBasicScheduleInfo(schedule, result);
        validateScheduleTiming(schedule, result);
        validateScheduleSemester(schedule, result);

        return result;
    }

    public ValidationResult validateScheduleForUpdate(Schedule schedule) {
        ValidationResult result = createValidationResult();

        validateRequiredField(schedule, "Schedule", result);
        if (!result.isValid()) {
            return result;
        }

        validateBasicScheduleInfo(schedule, result);
        validateScheduleTiming(schedule, result);
        validateScheduleSemester(schedule, result);
        validateUpdateRestrictions(schedule, result);

        return result;
    }

    public ValidationResult validateScheduleConflicts(Schedule schedule, List<Schedule> existingSchedules) {
        ValidationResult result = createValidationResult();

        validateRequiredField(schedule, "Schedule", result);
        validateRequiredField(existingSchedules, "Existing schedules", result);

        if (!result.isValid()) {
            return result;
        }

        validateClassroomConflicts(schedule, existingSchedules, result);
        validateTeacherConflicts(schedule, existingSchedules, result);
        validateCourseConflicts(schedule, existingSchedules, result);
        validateTimeSlotConflicts(schedule, existingSchedules, result);

        return result;
    }

    public ValidationResult validateScheduleDeletion(Schedule schedule, List<Schedule> dependentSchedules) {
        ValidationResult result = createValidationResult();

        validateRequiredField(schedule, "Schedule", result);
        if (!result.isValid()) {
            return result;
        }

        validateDeletionImpact(schedule, dependentSchedules, result);
        validateDeletionTiming(schedule, result);

        return result;
    }

    public ValidationResult validateScheduleSeries(Schedule baseSchedule, List<Schedule> seriesSchedules) {
        ValidationResult result = createValidationResult();

        validateRequiredField(baseSchedule, "Base schedule", result);
        validateRequiredField(seriesSchedules, "Series schedules", result);

        if (!result.isValid()) {
            return result;
        }

        validateSeriesStructure(baseSchedule, seriesSchedules, result);
        validateSeriesConsistency(baseSchedule, seriesSchedules, result);
        validateSeriesConflicts(baseSchedule, seriesSchedules, result);

        return result;
    }

    public ValidationResult validateScheduleModification(Schedule schedule, Schedule existingSchedule) {
        ValidationResult result = createValidationResult();

        validateRequiredField(schedule, "New schedule", result);
        validateRequiredField(existingSchedule, "Existing schedule", result);

        if (!result.isValid()) {
            return result;
        }

        validateModificationPermission(schedule, existingSchedule, result);
        validateModificationImpact(schedule, existingSchedule, result);
        validateModificationConsistency(schedule, existingSchedule, result);

        return result;
    }

    public ValidationResult validateScheduleCancellation(Schedule schedule) {
        ValidationResult result = createValidationResult();

        validateRequiredField(schedule, "Schedule", result);
        if (!result.isValid()) {
            return result;
        }

        validateCancellationPermission(schedule, result);
        validateCancellationImpact(schedule, result);
        validateCancellationTiming(schedule, result);

        return result;
    }

    public ValidationResult validateScheduleReassignment(Schedule schedule, Teacher newTeacher, Classroom newClassroom) {
        ValidationResult result = createValidationResult();

        validateRequiredField(schedule, "Schedule", result);
        validateRequiredField(newTeacher, "New teacher", result);
        validateRequiredField(newClassroom, "New classroom", result);

        if (!result.isValid()) {
            return result;
        }

        validateTeacherReassignment(schedule, newTeacher, result);
        validateClassroomReassignment(schedule, newClassroom, result);
        validateReassignmentCompatibility(newTeacher, newClassroom, result);

        return result;
    }

    public ValidationResult validateScheduleRescheduling(Schedule schedule, LocalDate newDate, TimeSlot newTimeSlot) {
        ValidationResult result = createValidationResult();

        validateRequiredField(schedule, "Schedule", result);
        validateRequiredField(newDate, "New date", result);
        validateRequiredField(newTimeSlot, "New time slot", result);

        if (!result.isValid()) {
            return result;
        }

        validateDateRescheduling(schedule, newDate, result);
        validateTimeSlotRescheduling(schedule, newTimeSlot, result);
        validateReschedulingConflicts(schedule, newDate, newTimeSlot, result);

        return result;
    }

    public ValidationResult validateScheduleSeriesGeneration(Schedule templateSchedule, LocalDate startDate, LocalDate endDate, String pattern) {
        ValidationResult result = createValidationResult();

        validateRequiredField(templateSchedule, "Template schedule", result);
        validateRequiredField(startDate, "Start date", result);
        validateRequiredField(endDate, "End date", result);
        validateRequiredField(pattern, "Pattern", result);

        if (!result.isValid()) {
            return result;
        }

        validateSeriesGenerationParameters(startDate, endDate, pattern, result);
        validateSeriesGenerationFeasibility(templateSchedule, startDate, endDate, pattern, result);
        validateSeriesGenerationConflicts(templateSchedule, startDate, endDate, pattern, result);

        return result;
    }

    private void validateBasicScheduleInfo(Schedule schedule, ValidationResult result) {
        validateRequiredField(schedule.getCourseOffering(), "Course offering", result);
        validateRequiredField(schedule.getClassroom(), "Classroom", result);
        validateRequiredField(schedule.getTimeSlot(), "Time slot", result);
        validateRequiredField(schedule.getScheduleDate(), "Schedule date", result);
        validateRequiredField(schedule.isRecurring(), "Recurring flag", result);

        validateMaxLength(schedule.getNotes(), "Notes", 1000, result);
        validateMaxLength(schedule.getRecurrencePattern(), "Recurrence pattern", 50, result);

        if (schedule.isRecurring() && schedule.getRecurrencePattern() == null) {
            result.addError("Recurrence pattern is required for recurring schedules");
        }

        if (!schedule.isRecurring() && schedule.getRecurrencePattern() != null) {
            result.addWarning("Recurrence pattern specified for non-recurring schedule");
        }
    }

    private void validateScheduleTiming(Schedule schedule, ValidationResult result) {
        TimeSlot timeSlot = schedule.getTimeSlot();
        if (timeSlot != null) {
            validateTimeOrder(timeSlot.getStartTime(), timeSlot.getEndTime(), "Start time", "End time", result);

            java.time.Duration duration = java.time.Duration.between(timeSlot.getStartTime(), timeSlot.getEndTime());
            if (duration.toMinutes() < 30) {
                result.addError("Schedule duration too short (minimum 30 minutes)");
            }
            if (duration.toHours() > 4) {
                result.addWarning("Schedule duration very long (" + duration.toHours() + " hours)");
            }

            validateBusinessHours(timeSlot.getStartTime().getHour(), "Start hour", result);
            validateBusinessHours(timeSlot.getEndTime().getHour(), "End hour", result);

            validateWeekday(timeSlot.getDayOfWeek().getValue(), "Day of week", result);
        }

        if (schedule.getScheduleDate() != null) {
            if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
                result.addWarning("Schedule date is in the past");
            }

            if (schedule.getScheduleDate().isAfter(LocalDate.now().plusYears(1))) {
                result.addWarning("Schedule date is more than 1 year in the future");
            }
        }
    }

    private void validateScheduleSemester(Schedule schedule, ValidationResult result) {
        if (schedule.getCourseOffering() != null && schedule.getCourseOffering().getSemester() != null) {
            Semester semester = schedule.getCourseOffering().getSemester();

            if (schedule.getScheduleDate() != null) {
                if (!semester.containsDate(schedule.getScheduleDate())) {
                    result.addError("Schedule date " + schedule.getScheduleDate() + " is outside semester dates " +
                                  semester.getStartDate() + " to " + semester.getEndDate());
                }
            }
        }
    }

    private void validateUpdateRestrictions(Schedule schedule, ValidationResult result) {
        if (!schedule.canBeModified()) {
            result.addError("Schedule cannot be modified - it may be in progress or in the past");
        }

        if (schedule.isCurrentlyInProgress()) {
            result.addError("Cannot modify schedule that is currently in progress");
        }

        if (schedule.isPast()) {
            result.addError("Cannot modify schedule that has already occurred");
        }
    }

    private void validateClassroomConflicts(Schedule schedule, List<Schedule> existingSchedules, ValidationResult result) {
        for (Schedule existing : existingSchedules) {
            if (!existing.equals(schedule) && schedule.hasClassroomConflict(existing)) {
                result.addError("Classroom conflict with " + existing.getScheduleSummary() +
                              " on " + existing.getScheduleDate());
            }
        }
    }

    private void validateTeacherConflicts(Schedule schedule, List<Schedule> existingSchedules, ValidationResult result) {
        for (Schedule existing : existingSchedules) {
            if (!existing.equals(schedule) && schedule.hasTeacherConflict(existing)) {
                result.addError("Teacher conflict with " + existing.getScheduleSummary() +
                              " on " + existing.getScheduleDate());
            }
        }
    }

    private void validateCourseConflicts(Schedule schedule, List<Schedule> existingSchedules, ValidationResult result) {
        CourseOffering thisOffering = schedule.getCourseOffering();
        if (thisOffering == null) return;

        for (Schedule existing : existingSchedules) {
            if (!existing.equals(schedule) && existing.getCourseOffering() != null) {
                CourseOffering existingOffering = existing.getCourseOffering();

                if (thisOffering.getCourse().equals(existingOffering.getCourse()) &&
                    schedule.getTimeSlot().conflictsWith(existing.getTimeSlot()) &&
                    schedule.getScheduleDate().equals(existing.getScheduleDate())) {
                    result.addError("Course conflict - same course scheduled at the same time: " +
                                  existing.getScheduleSummary());
                }

                if (thisOffering.getTeacher().equals(existingOffering.getTeacher()) &&
                    schedule.getTimeSlot().conflictsWith(existing.getTimeSlot()) &&
                    schedule.getScheduleDate().equals(existing.getScheduleDate())) {
                    result.addError("Teacher conflict - same teacher scheduled for different courses: " +
                                  existing.getScheduleSummary());
                }
            }
        }
    }

    private void validateTimeSlotConflicts(Schedule schedule, List<Schedule> existingSchedules, ValidationResult result) {
        TimeSlot timeSlot = schedule.getTimeSlot();
        if (timeSlot == null) return;

        for (Schedule existing : existingSchedules) {
            if (!existing.equals(schedule) && existing.getTimeSlot() != null &&
                schedule.getScheduleDate().equals(existing.getScheduleDate())) {

                if (timeSlot.conflictsWith(existing.getTimeSlot())) {
                    result.addError("Time slot conflict with " + existing.getScheduleSummary());
                }

                if (hasAdjacentScheduleConflict(timeSlot, existing.getTimeSlot())) {
                    result.addWarning("Adjacent schedule with " + existing.getScheduleSummary() +
                                    " - allow transition time");
                }
            }
        }
    }

    private void validateDeletionImpact(Schedule schedule, List<Schedule> dependentSchedules, ValidationResult result) {
        if (!dependentSchedules.isEmpty()) {
            result.addError("Cannot delete schedule with dependent schedules (" + dependentSchedules.size() + " found)");
        }

        if (schedule.isCurrentlyInProgress()) {
            result.addError("Cannot delete schedule that is currently in progress");
        }

        if (schedule.isToday() && schedule.isUpcoming()) {
            result.addWarning("Deleting today's schedule - verify notification to students");
        }
    }

    private void validateDeletionTiming(Schedule schedule, ValidationResult result) {
        if (schedule.isCurrentlyInProgress()) {
            result.addError("Cannot delete schedule that is currently in progress");
        }

        if (schedule.isToday() && schedule.getScheduleDateTime() != null &&
            schedule.getScheduleDateTime().isBefore(java.time.LocalDateTime.now().plusHours(1))) {
            result.addError("Cannot delete schedule less than 1 hour before start time");
        }
    }

    private void validateSeriesStructure(Schedule baseSchedule, List<Schedule> seriesSchedules, ValidationResult result) {
        if (!baseSchedule.isRecurring()) {
            result.addError("Base schedule must be recurring for series validation");
        }

        if (seriesSchedules.isEmpty()) {
            result.addError("Series schedules cannot be empty");
        }

        if (seriesSchedules.size() < 2) {
            result.addWarning("Series should have at least 2 schedules");
        }

        Set<Long> uniqueIds = seriesSchedules.stream()
            .map(Schedule::getId)
            .collect(Collectors.toSet());

        if (uniqueIds.size() != seriesSchedules.size()) {
            result.addError("Duplicate schedules found in series");
        }
    }

    private void validateSeriesConsistency(Schedule baseSchedule, List<Schedule> seriesSchedules, ValidationResult result) {
        CourseOffering baseOffering = baseSchedule.getCourseOffering();
        Classroom baseClassroom = baseSchedule.getClassroom();

        for (Schedule seriesSchedule : seriesSchedules) {
            if (!seriesSchedule.getCourseOffering().equals(baseOffering)) {
                result.addError("Inconsistent course offering in series schedule " + seriesSchedule.getId());
            }

            if (!seriesSchedule.getClassroom().equals(baseClassroom)) {
                result.addError("Inconsistent classroom in series schedule " + seriesSchedule.getId());
            }

            if (!seriesSchedule.getTimeSlot().getDayOfWeek().equals(baseSchedule.getTimeSlot().getDayOfWeek())) {
                result.addError("Inconsistent day of week in series schedule " + seriesSchedule.getId());
            }

            if (!java.time.Duration.between(
                seriesSchedule.getTimeSlot().getStartTime(),
                baseSchedule.getTimeSlot().getStartTime()
            ).equals(java.time.Duration.ZERO)) {
                result.addError("Inconsistent start time in series schedule " + seriesSchedule.getId());
            }
        }
    }

    private void validateSeriesConflicts(Schedule baseSchedule, List<Schedule> seriesSchedules, ValidationResult result) {
        for (int i = 0; i < seriesSchedules.size(); i++) {
            for (int j = i + 1; j < seriesSchedules.size(); j++) {
                Schedule schedule1 = seriesSchedules.get(i);
                Schedule schedule2 = seriesSchedules.get(j);

                if (schedule1.hasTeacherConflict(schedule2)) {
                    result.addError("Teacher conflict within series between schedules " + i + " and " + j);
                }

                if (schedule1.hasClassroomConflict(schedule2)) {
                    result.addError("Classroom conflict within series between schedules " + i + " and " + j);
                }
            }
        }
    }

    private void validateModificationPermission(Schedule schedule, Schedule existingSchedule, ValidationResult result) {
        if (!existingSchedule.canBeModified()) {
            result.addError("Existing schedule cannot be modified");
        }

        if (existingSchedule.isCurrentlyInProgress()) {
            result.addError("Cannot modify schedule that is currently in progress");
        }

        if (existingSchedule.isPast()) {
            result.addError("Cannot modify schedule that has already occurred");
        }
    }

    private void validateModificationImpact(Schedule schedule, Schedule existingSchedule, ValidationResult result) {
        if (!schedule.getCourseOffering().equals(existingSchedule.getCourseOffering())) {
            result.addError("Cannot change course offering for existing schedule");
        }

        if (schedule.getScheduleDate().equals(existingSchedule.getScheduleDate()) &&
            schedule.getTimeSlot().equals(existingSchedule.getTimeSlot()) &&
            schedule.getClassroom().equals(existingSchedule.getClassroom())) {
            result.addWarning("No meaningful changes detected in schedule modification");
        }
    }

    private void validateModificationConsistency(Schedule schedule, Schedule existingSchedule, ValidationResult result) {
        if (schedule.isRecurring() != existingSchedule.isRecurring()) {
            result.addError("Cannot change recurring status of existing schedule");
        }

        if (schedule.isRecurring() && existingSchedule.isRecurring() &&
            !schedule.getRecurrencePattern().equals(existingSchedule.getRecurrencePattern())) {
            result.addWarning("Changing recurrence pattern - verify series consistency");
        }
    }

    private void validateCancellationPermission(Schedule schedule, ValidationResult result) {
        if (!schedule.canBeCancelled()) {
            result.addError("Schedule cannot be cancelled");
        }

        if (schedule.isCurrentlyInProgress()) {
            result.addError("Cannot cancel schedule that is currently in progress");
        }

        if (schedule.isPast()) {
            result.addError("Cannot cancel schedule that has already occurred");
        }
    }

    private void validateCancellationImpact(Schedule schedule, ValidationResult result) {
        CourseOffering offering = schedule.getCourseOffering();
        if (offering != null && offering.getCurrentEnrollment() > 0) {
            result.addWarning("Cancelling schedule with enrolled students (" + offering.getCurrentEnrollment() + ")");
        }

        if (schedule.isExamSchedule()) {
            result.addWarning("Cancelling exam schedule - ensure alternative arrangements are made");
        }
    }

    private void validateCancellationTiming(Schedule schedule, ValidationResult result) {
        if (schedule.isToday() && schedule.getScheduleDateTime() != null) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime scheduleTime = schedule.getScheduleDateTime();

            if (scheduleTime.isBefore(now.plusHours(2))) {
                result.addError("Cannot cancel schedule less than 2 hours before start time");
            }

            if (scheduleTime.isAfter(now) && scheduleTime.isBefore(now.plusHours(24))) {
                result.addWarning("Cancelling schedule within 24 hours - emergency procedures may apply");
            }
        }
    }

    private void validateTeacherReassignment(Schedule schedule, Teacher newTeacher, ValidationResult result) {
        if (!schedule.canBeModified()) {
            result.addError("Cannot reassign teacher for schedule that cannot be modified");
        }

        CourseOffering offering = schedule.getCourseOffering();
        if (offering != null && offering.getCourse() != null) {
            TeacherValidator teacherValidator = new TeacherValidator();
            ValidationResult teacherResult = teacherValidator.validateTeacherCourseAssignment(newTeacher, offering.getCourse());
            result.merge(teacherResult);
        }
    }

    private void validateClassroomReassignment(Schedule schedule, Classroom newClassroom, ValidationResult result) {
        if (!schedule.canBeModified()) {
            result.addError("Cannot reassign classroom for schedule that cannot be modified");
        }

        CourseOffering offering = schedule.getCourseOffering();
        if (offering != null && offering.getCourse() != null) {
            ClassroomValidator classroomValidator = new ClassroomValidator();
            ValidationResult classroomResult = classroomValidator.validateClassroomCourseCompatibility(newClassroom, offering.getCourse());
            result.merge(classroomResult);
        }
    }

    private void validateReassignmentCompatibility(Teacher newTeacher, Classroom newClassroom, ValidationResult result) {
        if (newTeacher != null && newClassroom != null) {
            if (newTeacher.getTitle() == Teacher.TeacherTitle.PROFESSOR && newClassroom.isSmallClassroom()) {
                result.addWarning("Professor assigned to small classroom - verify suitability");
            }

            if (newTeacher.getTitle() == Teacher.TeacherTitle.INSTRUCTOR && newClassroom.isLargeClassroom()) {
                result.addWarning("Instructor assigned to large classroom - verify suitability");
            }
        }
    }

    private void validateDateRescheduling(Schedule schedule, LocalDate newDate, ValidationResult result) {
        if (!schedule.canBeModified()) {
            result.addError("Cannot reschedule schedule that cannot be modified");
        }

        if (newDate.isBefore(LocalDate.now())) {
            result.addError("Cannot reschedule to a past date");
        }

        if (newDate.isAfter(LocalDate.now().plusMonths(6))) {
            result.addWarning("Rescheduling to distant future (" + newDate + ")");
        }

        if (schedule.getCourseOffering() != null && schedule.getCourseOffering().getSemester() != null) {
            Semester semester = schedule.getCourseOffering().getSemester();
            if (!semester.containsDate(newDate)) {
                result.addError("New date " + newDate + " is outside semester dates " +
                              semester.getStartDate() + " to " + semester.getEndDate());
            }
        }
    }

    private void validateTimeSlotRescheduling(Schedule schedule, TimeSlot newTimeSlot, ValidationResult result) {
        if (!schedule.canBeModified()) {
            result.addError("Cannot reschedule time slot for schedule that cannot be modified");
        }

        validateTimeOrder(newTimeSlot.getStartTime(), newTimeSlot.getEndTime(), "New start time", "New end time", result);

        java.time.Duration newDuration = java.time.Duration.between(newTimeSlot.getStartTime(), newTimeSlot.getEndTime());
        if (schedule.getTimeSlot() != null) {
            java.time.Duration oldDuration = java.time.Duration.between(
                schedule.getTimeSlot().getStartTime(), schedule.getTimeSlot().getEndTime());

            if (!newDuration.equals(oldDuration)) {
                result.addWarning("Changing duration from " + oldDuration.toMinutes() + " to " + newDuration.toMinutes() + " minutes");
            }
        }

        if (newDuration.toMinutes() < 30) {
            result.addError("Rescheduled duration too short (minimum 30 minutes)");
        }
    }

    private void validateReschedulingConflicts(Schedule schedule, LocalDate newDate, TimeSlot newTimeSlot, ValidationResult result) {
        if (schedule.getTimeSlot() != null && schedule.getTimeSlot().equals(newTimeSlot) &&
            schedule.getScheduleDate().equals(newDate)) {
            result.addWarning("Rescheduling to same time and date - no changes made");
        }

        if (schedule.getTimeSlot() != null && !schedule.getTimeSlot().getDayOfWeek().equals(newTimeSlot.getDayOfWeek())) {
            result.addWarning("Changing day of week - verify recurring series consistency");
        }
    }

    private void validateSeriesGenerationParameters(LocalDate startDate, LocalDate endDate, String pattern, ValidationResult result) {
        validateDateOrder(startDate, endDate, "Start date", "End date", result);

        if (startDate.isBefore(LocalDate.now())) {
            result.addError("Series start date cannot be in the past");
        }

        if (endDate.isAfter(LocalDate.now().plusYears(1))) {
            result.addWarning("Series extends more than 1 year into the future");
        }

        java.time.Period period = java.time.Period.between(startDate, endDate);
        if (period.getMonths() > 6) {
            result.addWarning("Series spans more than 6 months - verify feasibility");
        }

        if (!"WEEKLY".equals(pattern) && !"BIWEEKLY".equals(pattern)) {
            result.addError("Unsupported recurrence pattern: " + pattern);
        }
    }

    private void validateSeriesGenerationFeasibility(Schedule templateSchedule, LocalDate startDate, LocalDate endDate, String pattern, ValidationResult result) {
        int expectedOccurrences = calculateExpectedOccurrences(startDate, endDate, pattern);
        if (expectedOccurrences > 50) {
            result.addWarning("Series would generate " + expectedOccurrences + " occurrences - consider breaking into smaller series");
        }

        if (expectedOccurrences < 2) {
            result.addError("Series would generate only " + expectedOccurrences + " occurrence(s)");
        }

        if (templateSchedule.getTimeSlot() != null) {
            DayOfWeek dayOfWeek = templateSchedule.getTimeSlot().getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                result.addWarning("Series includes weekend scheduling - verify this is intended");
            }
        }
    }

    private void validateSeriesGenerationConflicts(Schedule templateSchedule, LocalDate startDate, LocalDate endDate, String pattern, ValidationResult result) {
        if (templateSchedule.getTimeSlot() != null) {
            DayOfWeek dayOfWeek = templateSchedule.getTimeSlot().getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                result.addWarning("Weekend scheduling in series - verify staff availability");
            }
        }

        if (templateSchedule.getScheduleDate() != null) {
            java.time.DayOfWeek originalDay = templateSchedule.getScheduleDate().getDayOfWeek();
            if (templateSchedule.getTimeSlot() != null &&
                !originalDay.equals(templateSchedule.getTimeSlot().getDayOfWeek())) {
                result.addWarning("Template schedule day mismatch with time slot day");
            }
        }
    }

    private int calculateExpectedOccurrences(LocalDate startDate, LocalDate endDate, String pattern) {
        if ("WEEKLY".equals(pattern)) {
            return (int) java.time.temporal.ChronoUnit.WEEKS.between(startDate, endDate) + 1;
        } else if ("BIWEEKLY".equals(pattern)) {
            return (int) (java.time.temporal.ChronoUnit.WEEKS.between(startDate, endDate) / 2) + 1;
        }
        return 0;
    }

    private boolean hasAdjacentScheduleConflict(TimeSlot timeSlot1, TimeSlot timeSlot2) {
        if (timeSlot1 == null || timeSlot2 == null) return false;
        if (!timeSlot1.getDayOfWeek().equals(timeSlot2.getDayOfWeek())) return false;

        java.time.Duration gap1 = java.time.Duration.between(timeSlot1.getEndTime(), timeSlot2.getStartTime());
        java.time.Duration gap2 = java.time.Duration.between(timeSlot2.getEndTime(), timeSlot1.getStartTime());

        return gap1.abs().toMinutes() < 15 || gap2.abs().toMinutes() < 15;
    }
}