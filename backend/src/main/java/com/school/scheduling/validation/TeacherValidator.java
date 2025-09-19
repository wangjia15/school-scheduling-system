package com.school.scheduling.validation;

import com.school.scheduling.domain.Course;
import com.school.scheduling.domain.Schedule;
import com.school.scheduling.domain.Teacher;
import com.school.scheduling.domain.TeacherSpecialization;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TeacherValidator extends BaseValidator {

    public ValidationResult validateTeacherForCreation(Teacher teacher) {
        ValidationResult result = createValidationResult();

        validateRequiredField(teacher, "Teacher", result);
        if (!result.isValid()) {
            return result;
        }

        validateBasicTeacherInfo(teacher, result);
        validateWorkloadLimits(teacher, result);
        validateContactInfo(teacher, result);

        return result;
    }

    public ValidationResult validateTeacherForUpdate(Teacher teacher) {
        ValidationResult result = createValidationResult();

        validateRequiredField(teacher, "Teacher", result);
        if (!result.isValid()) {
            return result;
        }

        validateBasicTeacherInfo(teacher, result);
        validateWorkloadLimits(teacher, result);
        validateContactInfo(teacher, result);

        return result;
    }

    public ValidationResult validateTeacherAvailability(Teacher teacher, List<Schedule> existingSchedules,
                                                        DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        ValidationResult result = createValidationResult();

        validateRequiredField(teacher, "Teacher", result);
        validateRequiredField(existingSchedules, "Existing schedules", result);

        if (!result.isValid()) {
            return result;
        }

        validateTimeConflicts(teacher, existingSchedules, dayOfWeek, startTime, endTime, result);
        validateWorkloadCapacity(teacher, existingSchedules, startTime, endTime, result);

        return result;
    }

    public ValidationResult validateTeacherCourseAssignment(Teacher teacher, Course course) {
        ValidationResult result = createValidationResult();

        validateRequiredField(teacher, "Teacher", result);
        validateRequiredField(course, "Course", result);

        if (!result.isValid()) {
            return result;
        }

        validateSubjectSpecialization(teacher, course, result);
        validateTeacherWorkloadForCourse(teacher, course, result);
        validateTeacherQualifications(teacher, course, result);

        return result;
    }

    public ValidationResult validateTeacherSpecialization(TeacherSpecialization specialization) {
        ValidationResult result = createValidationResult();

        validateRequiredField(specialization, "Teacher specialization", result);
        if (!result.isValid()) {
            return result;
        }

        validateRequiredField(specialization.getSubjectCode(), "Subject code", result);
        validateRequiredField(specialization.getProficiencyLevel(), "Proficiency level", result);

        validateNotBlank(specialization.getSubjectCode(), "Subject code", result);
        validateMaxLength(specialization.getSubjectCode(), "Subject code", 20, result);

        if (specialization.getProficiencyLevel() == null) {
            result.addError("Proficiency level is required");
        }

        return result;
    }

    public ValidationResult validateTeacherWorkload(Teacher teacher, List<Schedule> currentSchedules) {
        ValidationResult result = createValidationResult();

        validateRequiredField(teacher, "Teacher", result);
        validateRequiredField(currentSchedules, "Current schedules", result);

        if (!result.isValid()) {
            return result;
        }

        validateWeeklyHoursLimit(teacher, currentSchedules, result);
        validateCourseCountLimit(teacher, currentSchedules, result);
        validateScheduleDistribution(teacher, currentSchedules, result);

        return result;
    }

    private void validateBasicTeacherInfo(Teacher teacher, ValidationResult result) {
        validateRequiredField(teacher.getUser(), "User", result);
        validateRequiredField(teacher.getDepartment(), "Department", result);
        validateRequiredField(teacher.getTitle(), "Title", result);
        validateRequiredField(teacher.getEmployeeId(), "Employee ID", result);

        validateNotBlank(teacher.getEmployeeId(), "Employee ID", result);
        validateMaxLength(teacher.getEmployeeId(), "Employee ID", 20, result);

        validateMaxLength(teacher.getOfficeLocation(), "Office location", 100, result);
        validateMaxLength(teacher.getPhone(), "Phone number", 20, result);

        if (teacher.getUser() != null) {
            validateEmailFormat(teacher.getUser().getEmail(), "Email", result);
        }
    }

    private void validateWorkloadLimits(Teacher teacher, ValidationResult result) {
        validatePositive(teacher.getMaxWeeklyHours(), "Max weekly hours", result);
        validatePositive(teacher.getMaxCoursesPerSemester(), "Max courses per semester", result);

        validateRange(teacher.getMaxWeeklyHours(), "Max weekly hours",
                    new BigDecimal("1.0"), new BigDecimal("60.0"), result);
        validateRange(teacher.getMaxCoursesPerSemester(), "Max courses per semester",
                    1, 10, result);

        if (teacher.getMaxWeeklyHours().compareTo(new BigDecimal("10.0")) < 0) {
            result.addWarning("Max weekly hours is very low (" + teacher.getMaxWeeklyHours() + ")");
        }

        if (teacher.getMaxWeeklyHours().compareTo(new BigDecimal("50.0")) > 0) {
            result.addWarning("Max weekly hours is very high (" + teacher.getMaxWeeklyHours() + ")");
        }
    }

    private void validateContactInfo(Teacher teacher, ValidationResult result) {
        if (teacher.getPhone() != null && !teacher.getPhone().trim().isEmpty()) {
            String phoneRegex = "^[+]?[0-9\\-\\s\\(\\)]{10,20}$";
            if (!teacher.getPhone().matches(phoneRegex)) {
                result.addError("Phone number format is invalid");
            }
        }
    }

    private void validateTimeConflicts(Teacher teacher, List<Schedule> existingSchedules,
                                     DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                                     ValidationResult result) {
        for (Schedule schedule : existingSchedules) {
            if (schedule.getTimeSlot() != null && schedule.getTimeSlot().getDayOfWeek() == dayOfWeek) {
                LocalTime existingStart = schedule.getTimeSlot().getStartTime();
                LocalTime existingEnd = schedule.getTimeSlot().getEndTime();

                if (hasTimeOverlap(startTime, endTime, existingStart, existingEnd)) {
                    result.addError("Teacher has a scheduling conflict with " +
                                  schedule.getScheduleSummary() +
                                  " (" + existingStart + " - " + existingEnd + ")");
                }
            }
        }
    }

    private void validateWorkloadCapacity(Teacher teacher, List<Schedule> existingSchedules,
                                        LocalTime startTime, LocalTime endTime, ValidationResult result) {
        BigDecimal durationHours = BigDecimal.valueOf(
            java.time.Duration.between(startTime, endTime).toMinutes() / 60.0
        );

        BigDecimal currentWeeklyHours = calculateCurrentWeeklyHours(existingSchedules);
        BigDecimal newTotalHours = currentWeeklyHours.add(durationHours);

        if (newTotalHours.compareTo(teacher.getMaxWeeklyHours()) > 0) {
            result.addError("Teacher would exceed maximum weekly hours. Current: " + currentWeeklyHours +
                          ", New: " + newTotalHours + ", Max: " + teacher.getMaxWeeklyHours());
        }

        double utilizationRate = newTotalHours.divide(teacher.getMaxWeeklyHours(), 4, java.math.RoundingMode.HALF_UP)
                                            .doubleValue();
        if (utilizationRate > 0.9) {
            result.addWarning("Teacher workload would be very high (" +
                            String.format("%.1f%%", utilizationRate * 100) + ")");
        }
    }

    private void validateSubjectSpecialization(Teacher teacher, Course course, ValidationResult result) {
        if (teacher.getSpecializations().isEmpty()) {
            result.addError("Teacher has no specializations defined");
            return;
        }

        boolean hasMatchingSpecialization = teacher.getSpecializations().stream()
            .anyMatch(spec -> course.getCourseCode().toUpperCase().contains(spec.getSubjectCode().toUpperCase()) ||
                             spec.getSubjectCode().toUpperCase().contains(course.getCourseCode().substring(0, 3).toUpperCase()));

        if (!hasMatchingSpecialization) {
            result.addError("Teacher does not have required specialization for course " + course.getCourseCode());
            result.addWarning("Consider adding specialization or checking if teacher is qualified");
        }
    }

    private void validateTeacherWorkloadForCourse(Teacher teacher, Course course, ValidationResult result) {
        if (course.getContactHoursPerWeek() == null) {
            result.addError("Course contact hours per week is not defined");
            return;
        }

        if (course.getContactHoursPerWeek().compareTo(teacher.getMaxWeeklyHours()) > 0) {
            result.addError("Course contact hours (" + course.getContactHoursPerWeek() +
                          ") exceed teacher's maximum weekly hours (" + teacher.getMaxWeeklyHours() + ")");
        }

        if (teacher.getMaxCoursesPerSemester() <= 0) {
            result.addWarning("Teacher has no available course slots for this semester");
        }
    }

    private void validateTeacherQualifications(Teacher teacher, Course course, ValidationResult result) {
        if (course.isGraduate() && teacher.getTitle() == Teacher.TeacherTitle.INSTRUCTOR) {
            result.addWarning("Instructor teaching graduate course - verify qualifications");
        }

        if (course.isPhD() && (teacher.getTitle() == Teacher.TeacherTitle.INSTRUCTOR ||
                              teacher.getTitle() == Teacher.TeacherTitle.ADJUNCT)) {
            result.addError("Instructor or Adjunct cannot teach PhD courses");
        }

        if (course.getDifficultyLevel() >= 4 && teacher.getTitle() == Teacher.TeacherTitle.ADJUNCT) {
            result.addWarning("Adjunct teaching advanced course - verify qualifications");
        }
    }

    private void validateWeeklyHoursLimit(Teacher teacher, List<Schedule> currentSchedules, ValidationResult result) {
        BigDecimal currentHours = calculateCurrentWeeklyHours(currentSchedules);

        if (currentHours.compareTo(teacher.getMaxWeeklyHours()) > 0) {
            result.addError("Teacher exceeds maximum weekly hours. Current: " + currentHours +
                          ", Max: " + teacher.getMaxWeeklyHours());
        }

        double utilizationRate = currentHours.divide(teacher.getMaxWeeklyHours(), 4, java.math.RoundingMode.HALF_UP)
                                           .doubleValue();
        if (utilizationRate > 0.8) {
            result.addWarning("Teacher workload is high (" +
                            String.format("%.1f%%", utilizationRate * 100) + ")");
        }
    }

    private void validateCourseCountLimit(Teacher teacher, List<Schedule> currentSchedules, ValidationResult result) {
        Set<Long> uniqueCourseIds = currentSchedules.stream()
            .filter(schedule -> schedule.getCourseOffering() != null)
            .map(schedule -> schedule.getCourseOffering().getCourse().getId())
            .collect(Collectors.toSet());

        int currentCourseCount = uniqueCourseIds.size();

        if (currentCourseCount > teacher.getMaxCoursesPerSemester()) {
            result.addError("Teacher exceeds maximum courses per semester. Current: " + currentCourseCount +
                          ", Max: " + teacher.getMaxCoursesPerSemester());
        }

        if (currentCourseCount >= teacher.getMaxCoursesPerSemester()) {
            result.addWarning("Teacher has reached maximum course limit for this semester");
        }
    }

    private void validateScheduleDistribution(Teacher teacher, List<Schedule> currentSchedules, ValidationResult result) {
        if (currentSchedules.isEmpty()) {
            return;
        }

        Set<DayOfWeek> teachingDays = currentSchedules.stream()
            .filter(schedule -> schedule.getTimeSlot() != null)
            .map(schedule -> schedule.getTimeSlot().getDayOfWeek())
            .collect(Collectors.toSet());

        if (teachingDays.size() > 5) {
            result.addWarning("Teacher scheduled on " + teachingDays.size() + " days - consider distribution");
        }

        long backToBackCount = currentSchedules.stream()
            .filter(schedule -> schedule.getTimeSlot() != null)
            .filter(schedule -> hasBackToBackSchedule(schedule, currentSchedules))
            .count();

        if (backToBackCount > 3) {
            result.addWarning("Teacher has " + backToBackCount + " back-to-back schedules - consider breaks");
        }
    }

    private BigDecimal calculateCurrentWeeklyHours(List<Schedule> schedules) {
        return schedules.stream()
            .filter(schedule -> schedule.getTimeSlot() != null)
            .map(schedule -> {
                LocalTime start = schedule.getTimeSlot().getStartTime();
                LocalTime end = schedule.getTimeSlot().getEndTime();
                double hours = java.time.Duration.between(start, end).toMinutes() / 60.0;
                return BigDecimal.valueOf(hours);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean hasTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return !end1.isBefore(start2) && !start1.isAfter(end2);
    }

    private boolean hasBackToBackSchedule(Schedule schedule, List<Schedule> allSchedules) {
        if (schedule.getTimeSlot() == null) return false;

        return allSchedules.stream()
            .filter(s -> !s.equals(schedule) && s.getTimeSlot() != null)
            .filter(s -> s.getTimeSlot().getDayOfWeek() == schedule.getTimeSlot().getDayOfWeek())
            .anyMatch(s -> {
                LocalTime end1 = schedule.getTimeSlot().getEndTime();
                LocalTime start2 = s.getTimeSlot().getStartTime();
                LocalTime end2 = s.getTimeSlot().getEndTime();
                LocalTime start1 = schedule.getTimeSlot().getStartTime();

                return end1.equals(start2) || start1.equals(end2);
            });
    }
}