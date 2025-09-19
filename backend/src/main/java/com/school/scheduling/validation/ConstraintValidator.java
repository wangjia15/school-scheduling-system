package com.school.scheduling.validation;

import com.school.scheduling.domain.Course;
import com.school.scheduling.domain.SchedulingConstraint;
import com.school.scheduling.domain.Teacher;
import com.school.scheduling.domain.Classroom;
import com.school.scheduling.domain.Schedule;
import com.school.scheduling.domain.Semester;
import com.school.scheduling.domain.TimeSlot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ConstraintValidator extends BaseValidator {

    public ValidationResult validateSchedulingConstraints(List<SchedulingConstraint> constraints, List<Schedule> schedules) {
        ValidationResult result = createValidationResult();

        validateRequiredField(constraints, "Scheduling constraints", result);
        validateRequiredField(schedules, "Schedules", result);

        if (!result.isValid()) {
            return result;
        }

        validateTeacherConstraints(constraints, schedules, result);
        validateClassroomConstraints(constraints, schedules, result);
        validateCourseConstraints(constraints, schedules, result);
        validateTimeConstraints(constraints, schedules, result);
        validateBusinessRules(constraints, schedules, result);

        return result;
    }

    public ValidationResult validateTeacherWorkloadConstraints(Teacher teacher, List<Schedule> schedules, SchedulingConstraint constraints) {
        ValidationResult result = createValidationResult();

        validateRequiredField(teacher, "Teacher", result);
        validateRequiredField(schedules, "Schedules", result);
        validateRequiredField(constraints, "Constraints", result);

        if (!result.isValid()) {
            return result;
        }

        validateWeeklyHoursConstraint(teacher, schedules, constraints, result);
        validateDailyHoursConstraint(teacher, schedules, constraints, result);
        validateConsecutiveHoursConstraint(teacher, schedules, constraints, result);
        validateCourseCountConstraint(teacher, schedules, constraints, result);
        validatePreparationTimeConstraint(teacher, schedules, constraints, result);

        return result;
    }

    public ValidationResult validateClassroomUtilizationConstraints(Classroom classroom, List<Schedule> schedules, SchedulingConstraint constraints) {
        ValidationResult result = createValidationResult();

        validateRequiredField(classroom, "Classroom", result);
        validateRequiredField(schedules, "Schedules", result);
        validateRequiredField(constraints, "Constraints", result);

        if (!result.isValid()) {
            return result;
        }

        validateCapacityUtilizationConstraint(classroom, schedules, constraints, result);
        validateTimeUtilizationConstraint(classroom, schedules, constraints, result);
        validateMaintenanceWindowConstraint(classroom, schedules, constraints, result);
        validateEquipmentUsageConstraint(classroom, schedules, constraints, result);

        return result;
    }

    public ValidationResult validateCourseSchedulingConstraints(Course course, List<Schedule> schedules, SchedulingConstraint constraints) {
        ValidationResult result = createValidationResult();

        validateRequiredField(course, "Course", result);
        validateRequiredField(schedules, "Schedules", result);
        validateRequiredField(constraints, "Constraints", result);

        if (!result.isValid()) {
            return result;
        }

        validateMeetingFrequencyConstraint(course, schedules, constraints, result);
        validateTimeDistributionConstraint(course, schedules, constraints, result);
        validateRoomTypeConstraint(course, schedules, constraints, result);
        validateClassSizeConstraint(course, schedules, constraints, result);

        return result;
    }

    public ValidationResult validateSemesterConstraints(Semester semester, List<Schedule> schedules, SchedulingConstraint constraints) {
        ValidationResult result = createValidationResult();

        validateRequiredField(semester, "Semester", result);
        validateRequiredField(schedules, "Schedules", result);
        validateRequiredField(constraints, "Constraints", result);

        if (!result.isValid()) {
            return result;
        }

        validateSemesterDurationConstraint(semester, constraints, result);
        validateHolidayConstraint(semester, schedules, constraints, result);
        validateExamPeriodConstraint(semester, schedules, constraints, result);
        validateBreakPeriodConstraint(semester, schedules, constraints, result);

        return result;
    }

    public ValidationResult validateInstitutionalConstraints(List<Schedule> schedules, SchedulingConstraint constraints) {
        ValidationResult result = createValidationResult();

        validateRequiredField(schedules, "Schedules", result);
        validateRequiredField(constraints, "Constraints", result);

        if (!result.isValid()) {
            return result;
        }

        validateBuildingHoursConstraint(schedules, constraints, result);
        validateParkingConstraint(schedules, constraints, result);
        validateSecurityConstraint(schedules, constraints, result);
        validateAccessibilityConstraint(schedules, constraints, result);

        return result;
    }

    public ValidationResult validateConstraintDefinition(SchedulingConstraint constraint) {
        ValidationResult result = createValidationResult();

        validateRequiredField(constraint, "Scheduling constraint", result);
        if (!result.isValid()) {
            return result;
        }

        validateConstraintBasicInfo(constraint, result);
        validateConstraintParameters(constraint, result);
        validateConstraintFeasibility(constraint, result);

        return result;
    }

    public ValidationResult validateConstraintCompliance(Schedule schedule, List<SchedulingConstraint> constraints) {
        ValidationResult result = createValidationResult();

        validateRequiredField(schedule, "Schedule", result);
        validateRequiredField(constraints, "Constraints", result);

        if (!result.isValid()) {
            return result;
        }

        validateHardConstraints(schedule, constraints, result);
        validateSoftConstraints(schedule, constraints, result);
        validateConstraintPriority(schedule, constraints, result);

        return result;
    }

    public ValidationResult validateConstraintModification(SchedulingConstraint oldConstraint, SchedulingConstraint newConstraint, List<Schedule> affectedSchedules) {
        ValidationResult result = createValidationResult();

        validateRequiredField(oldConstraint, "Old constraint", result);
        validateRequiredField(newConstraint, "New constraint", result);
        validateRequiredField(affectedSchedules, "Affected schedules", result);

        if (!result.isValid()) {
            return result;
        }

        validateModificationImpact(oldConstraint, newConstraint, affectedSchedules, result);
        validateConstraintTypeChange(oldConstraint, newConstraint, result);
        validateParameterValueChange(oldConstraint, newConstraint, result);

        return result;
    }

    private void validateTeacherConstraints(List<SchedulingConstraint> constraints, List<Schedule> schedules, ValidationResult result) {
        List<SchedulingConstraint> teacherConstraints = constraints.stream()
            .filter(c -> c.getConstraintType() == SchedulingConstraint.ConstraintType.TEACHER_WORKLOAD)
            .collect(Collectors.toList());

        for (SchedulingConstraint constraint : teacherConstraints) {
            Map<String, Object> parameters = constraint.getParameters();
            if (parameters.containsKey("maxWeeklyHours")) {
                BigDecimal maxWeeklyHours = new BigDecimal(parameters.get("maxWeeklyHours").toString());
                validateTeacherWeeklyHoursLimits(schedules, maxWeeklyHours, result);
            }

            if (parameters.containsKey("maxDailyHours")) {
                BigDecimal maxDailyHours = new BigDecimal(parameters.get("maxDailyHours").toString());
                validateTeacherDailyHoursLimits(schedules, maxDailyHours, result);
            }

            if (parameters.containsKey("maxConsecutiveHours")) {
                Integer maxConsecutiveHours = Integer.parseInt(parameters.get("maxConsecutiveHours").toString());
                validateTeacherConsecutiveHoursLimits(schedules, maxConsecutiveHours, result);
            }
        }
    }

    private void validateClassroomConstraints(List<SchedulingConstraint> constraints, List<Schedule> schedules, ValidationResult result) {
        List<SchedulingConstraint> classroomConstraints = constraints.stream()
            .filter(c -> c.getConstraintType() == SchedulingConstraint.ConstraintType.CLASSROOM_UTILIZATION)
            .collect(Collectors.toList());

        for (SchedulingConstraint constraint : classroomConstraints) {
            Map<String, Object> parameters = constraint.getParameters();
            if (parameters.containsKey("maxUtilizationRate")) {
                Double maxUtilizationRate = Double.parseDouble(parameters.get("maxUtilizationRate").toString());
                validateClassroomUtilizationLimits(schedules, maxUtilizationRate, result);
            }

            if (parameters.containsKey("minBreakTime")) {
                Integer minBreakTime = Integer.parseInt(parameters.get("minBreakTime").toString());
                validateClassroomBreakTime(schedules, minBreakTime, result);
            }
        }
    }

    private void validateCourseConstraints(List<SchedulingConstraint> constraints, List<Schedule> schedules, ValidationResult result) {
        List<SchedulingConstraint> courseConstraints = constraints.stream()
            .filter(c -> c.getConstraintType() == SchedulingConstraint.ConstraintType.COURSE_SCHEDULING)
            .collect(Collectors.toList());

        for (SchedulingConstraint constraint : courseConstraints) {
            Map<String, Object> parameters = constraint.getParameters();
            if (parameters.containsKey("minMeetingFrequency")) {
                Integer minMeetingFrequency = Integer.parseInt(parameters.get("minMeetingFrequency").toString());
                validateCourseMeetingFrequency(schedules, minMeetingFrequency, result);
            }

            if (parameters.containsKey("maxClassSize")) {
                Integer maxClassSize = Integer.parseInt(parameters.get("maxClassSize").toString());
                validateCourseClassSize(schedules, maxClassSize, result);
            }
        }
    }

    private void validateTimeConstraints(List<SchedulingConstraint> constraints, List<Schedule> schedules, ValidationResult result) {
        List<SchedulingConstraint> timeConstraints = constraints.stream()
            .filter(c -> c.getConstraintType() == SchedulingConstraint.ConstraintType_TIME_AVAILABILITY)
            .collect(Collectors.toList());

        for (SchedulingConstraint constraint : timeConstraints) {
            Map<String, Object> parameters = constraint.getParameters();
            if (parameters.containsKey("allowedTimeSlots")) {
                validateTimeSlotAvailability(schedules, parameters, result);
            }

            if (parameters.containsKey("excludedTimeSlots")) {
                validateTimeSlotExclusions(schedules, parameters, result);
            }
        }
    }

    private void validateBusinessRules(List<SchedulingConstraint> constraints, List<Schedule> schedules, ValidationResult result) {
        List<SchedulingConstraint> businessRules = constraints.stream()
            .filter(c -> c.getConstraintType() == SchedulingConstraint.ConstraintType.BUSINESS_RULE)
            .collect(Collectors.toList());

        for (SchedulingConstraint rule : businessRules) {
            validateBusinessRuleCompliance(schedules, rule, result);
        }
    }

    private void validateWeeklyHoursConstraint(Teacher teacher, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("maxWeeklyHours")) {
            BigDecimal maxWeeklyHours = new BigDecimal(parameters.get("maxWeeklyHours").toString());
            BigDecimal actualHours = calculateTeacherWeeklyHours(teacher, schedules);

            if (actualHours.compareTo(maxWeeklyHours) > 0) {
                result.addError("Teacher " + teacher.getFullName() + " exceeds weekly hours limit. Actual: " +
                              actualHours + ", Max: " + maxWeeklyHours);
            }

            double utilizationRate = actualHours.divide(maxWeeklyHours, 4, java.math.RoundingMode.HALF_UP).doubleValue();
            if (utilizationRate > 0.9) {
                result.addWarning("Teacher " + teacher.getFullName() + " workload is very high (" +
                                String.format("%.1f%%", utilizationRate * 100) + ")");
            }
        }
    }

    private void validateDailyHoursConstraint(Teacher teacher, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("maxDailyHours")) {
            BigDecimal maxDailyHours = new BigDecimal(parameters.get("maxDailyHours").toString());
            Map<LocalDate, BigDecimal> dailyHours = calculateTeacherDailyHours(teacher, schedules);

            for (Map.Entry<LocalDate, BigDecimal> entry : dailyHours.entrySet()) {
                if (entry.getValue().compareTo(maxDailyHours) > 0) {
                    result.addError("Teacher " + teacher.getFullName() + " exceeds daily hours limit on " +
                                  entry.getKey() + ". Actual: " + entry.getValue() + ", Max: " + maxDailyHours);
                }
            }
        }
    }

    private void validateConsecutiveHoursConstraint(Teacher teacher, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("maxConsecutiveHours")) {
            Integer maxConsecutiveHours = Integer.parseInt(parameters.get("maxConsecutiveHours").toString());
            List<java.time.temporal.TemporalAmount> consecutiveBlocks = findConsecutiveTeachingBlocks(teacher, schedules);

            for (java.time.temporal.TemporalAmount block : consecutiveBlocks) {
                if (block instanceof java.time.Duration) {
                    java.time.Duration duration = (java.time.Duration) block;
                    if (duration.toHours() > maxConsecutiveHours) {
                        result.addError("Teacher " + teacher.getFullName() + " has consecutive teaching block of " +
                                      duration.toHours() + " hours, exceeding limit of " + maxConsecutiveHours);
                    }
                }
            }
        }
    }

    private void validateCourseCountConstraint(Teacher teacher, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("maxCoursesPerSemester")) {
            Integer maxCourses = Integer.parseInt(parameters.get("maxCoursesPerSemester").toString());
            long actualCourses = schedules.stream()
                .filter(s -> s.getCourseOffering() != null && s.getCourseOffering().getTeacher().equals(teacher))
                .map(s -> s.getCourseOffering().getCourse().getId())
                .distinct()
                .count();

            if (actualCourses > maxCourses) {
                result.addError("Teacher " + teacher.getFullName() + " exceeds course limit. Actual: " +
                              actualCourses + ", Max: " + maxCourses);
            }
        }
    }

    private void validatePreparationTimeConstraint(Teacher teacher, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("minPreparationTime")) {
            Integer minPrepTime = Integer.parseInt(parameters.get("minPreparationTime").toString());
            List<java.time.Duration> preparationGaps = findPreparationGaps(teacher, schedules);

            for (java.time.Duration gap : preparationGaps) {
                if (gap.toMinutes() < minPrepTime) {
                    result.addWarning("Teacher " + teacher.getFullName() + " has insufficient preparation time (" +
                                    gap.toMinutes() + " minutes, minimum: " + minPrepTime + ")");
                }
            }
        }
    }

    private void validateCapacityUtilizationConstraint(Classroom classroom, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("maxUtilizationRate")) {
            Double maxUtilizationRate = Double.parseDouble(parameters.get("maxUtilizationRate").toString());
            double actualUtilization = calculateClassroomUtilization(classroom, schedules);

            if (actualUtilization > maxUtilizationRate) {
                result.addError("Classroom " + classroom.getFullDisplayName() + " exceeds utilization rate. Actual: " +
                              String.format("%.1f%%", actualUtilization * 100) + ", Max: " +
                              String.format("%.1f%%", maxUtilizationRate * 100));
            }
        }
    }

    private void validateTimeUtilizationConstraint(Classroom classroom, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("maxDailyUtilization")) {
            Double maxDailyUtilization = Double.parseDouble(parameters.get("maxDailyUtilization").toString());
            Map<DayOfWeek, Double> dailyUtilization = calculateClassroomDailyUtilization(classroom, schedules);

            for (Map.Entry<DayOfWeek, Double> entry : dailyUtilization.entrySet()) {
                if (entry.getValue() > maxDailyUtilization) {
                    result.addError("Classroom " + classroom.getFullDisplayName() + " exceeds daily utilization on " +
                                  entry.getKey() + ". Actual: " + String.format("%.1f%%", entry.getValue() * 100) +
                                  ", Max: " + String.format("%.1f%%", maxDailyUtilization * 100));
                }
            }
        }
    }

    private void validateMaintenanceWindowConstraint(Classroom classroom, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("maintenanceWindows")) {
            validateMaintenanceWindowConflicts(classroom, schedules, parameters, result);
        }
    }

    private void validateEquipmentUsageConstraint(Classroom classroom, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("equipmentUsageLimits")) {
            validateEquipmentUsageLimits(classroom, schedules, parameters, result);
        }
    }

    private void validateMeetingFrequencyConstraint(Course course, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("minMeetingFrequency")) {
            Integer minFrequency = Integer.parseInt(parameters.get("minMeetingFrequency").toString());
            long actualFrequency = schedules.stream()
                .filter(s -> s.getCourseOffering() != null && s.getCourseOffering().getCourse().equals(course))
                .count();

            if (actualFrequency < minFrequency) {
                result.addError("Course " + course.getFullDisplayName() + " has insufficient meeting frequency. Actual: " +
                              actualFrequency + ", Min: " + minFrequency);
            }
        }
    }

    private void validateTimeDistributionConstraint(Course course, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("preferredTimeSlots")) {
            validatePreferredTimeSlots(course, schedules, parameters, result);
        }
    }

    private void validateRoomTypeConstraint(Course course, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("requiredRoomTypes")) {
            validateRoomTypeRequirements(course, schedules, parameters, result);
        }
    }

    private void validateClassSizeConstraint(Course course, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("maxClassSize")) {
            Integer maxClassSize = Integer.parseInt(parameters.get("maxClassSize").toString());
            validateClassSizeLimits(course, schedules, maxClassSize, result);
        }
    }

    private void validateSemesterDurationConstraint(Semester semester, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("minWeeks") || parameters.containsKey("maxWeeks")) {
            java.time.Period duration = java.time.Period.between(semester.getStartDate(), semester.getEndDate());
            int weeks = (int) java.time.temporal.ChronoUnit.WEEKS.between(semester.getStartDate(), semester.getEndDate());

            if (parameters.containsKey("minWeeks")) {
                Integer minWeeks = Integer.parseInt(parameters.get("minWeeks").toString());
                if (weeks < minWeeks) {
                    result.addError("Semester duration (" + weeks + " weeks) is less than minimum required (" + minWeeks + " weeks)");
                }
            }

            if (parameters.containsKey("maxWeeks")) {
                Integer maxWeeks = Integer.parseInt(parameters.get("maxWeeks").toString());
                if (weeks > maxWeeks) {
                    result.addError("Semester duration (" + weeks + " weeks) exceeds maximum allowed (" + maxWeeks + " weeks)");
                }
            }
        }
    }

    private void validateHolidayConstraint(Semester semester, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("holidays")) {
            validateHolidayScheduling(semester, schedules, parameters, result);
        }
    }

    private void validateExamPeriodConstraint(Semester semester, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("examPeriods")) {
            validateExamPeriodScheduling(semester, schedules, parameters, result);
        }
    }

    private void validateBreakPeriodConstraint(Semester semester, List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("breakPeriods")) {
            validateBreakPeriodScheduling(semester, schedules, parameters, result);
        }
    }

    private void validateBuildingHoursConstraint(List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("buildingHours")) {
            validateBuildingHoursCompliance(schedules, parameters, result);
        }
    }

    private void validateParkingConstraint(List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("parkingRestrictions")) {
            validateParkingRestrictions(schedules, parameters, result);
        }
    }

    private void validateSecurityConstraint(List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("securityHours")) {
            validateSecurityHours(schedules, parameters, result);
        }
    }

    private void validateAccessibilityConstraint(List<Schedule> schedules, SchedulingConstraint constraints, ValidationResult result) {
        Map<String, Object> parameters = constraints.getParameters();
        if (parameters.containsKey("accessibilityRequirements")) {
            validateAccessibilityRequirements(schedules, parameters, result);
        }
    }

    private void validateConstraintBasicInfo(SchedulingConstraint constraint, ValidationResult result) {
        validateRequiredField(constraint.getName(), "Constraint name", result);
        validateRequiredField(constraint.getConstraintType(), "Constraint type", result);
        validateRequiredField(constraint.getPriority(), "Constraint priority", result);

        validateNotBlank(constraint.getName(), "Constraint name", result);
        validateMaxLength(constraint.getName(), "Constraint name", 100, result);
        validateMaxLength(constraint.getDescription(), "Description", 500, result);

        validateRange(constraint.getPriority(), "Priority", 1, 10, result);
    }

    private void validateConstraintParameters(SchedulingConstraint constraint, ValidationResult result) {
        Map<String, Object> parameters = constraint.getParameters();
        if (parameters == null || parameters.isEmpty()) {
            result.addWarning("Constraint has no parameters defined");
            return;
        }

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key == null || key.trim().isEmpty()) {
                result.addError("Parameter key cannot be empty");
            }

            if (value == null) {
                result.addError("Parameter value for key '" + key + "' cannot be null");
            }

            validateParameterType(key, value, result);
        }
    }

    private void validateConstraintFeasibility(SchedulingConstraint constraint, ValidationResult result) {
        Map<String, Object> parameters = constraint.getParameters();
        SchedulingConstraint.ConstraintType type = constraint.getConstraintType();

        switch (type) {
            case TEACHER_WORKLOAD:
                validateTeacherWorkloadFeasibility(parameters, result);
                break;
            case CLASSROOM_UTILIZATION:
                validateClassroomUtilizationFeasibility(parameters, result);
                break;
            case COURSE_SCHEDULING:
                validateCourseSchedulingFeasibility(parameters, result);
                break;
            case TIME_AVAILABILITY:
                validateTimeAvailabilityFeasibility(parameters, result);
                break;
            case BUSINESS_RULE:
                validateBusinessRuleFeasibility(parameters, result);
                break;
        }
    }

    private void validateHardConstraints(Schedule schedule, List<SchedulingConstraint> constraints, ValidationResult result) {
        List<SchedulingConstraint> hardConstraints = constraints.stream()
            .filter(c -> c.isHardConstraint())
            .collect(Collectors.toList());

        for (SchedulingConstraint constraint : hardConstraints) {
            if (!isScheduleCompliantWithConstraint(schedule, constraint)) {
                result.addError("Schedule violates hard constraint: " + constraint.getName());
            }
        }
    }

    private void validateSoftConstraints(Schedule schedule, List<SchedulingConstraint> constraints, ValidationResult result) {
        List<SchedulingConstraint> softConstraints = constraints.stream()
            .filter(c -> !c.isHardConstraint())
            .collect(Collectors.toList());

        for (SchedulingConstraint constraint : softConstraints) {
            if (!isScheduleCompliantWithConstraint(schedule, constraint)) {
                result.addWarning("Schedule violates soft constraint: " + constraint.getName());
            }
        }
    }

    private void validateConstraintPriority(Schedule schedule, List<SchedulingConstraint> constraints, ValidationResult result) {
        List<SchedulingConstraint> violatedConstraints = constraints.stream()
            .filter(c -> !isScheduleCompliantWithConstraint(schedule, c))
            .sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority()))
            .collect(Collectors.toList());

        if (!violatedConstraints.isEmpty()) {
            SchedulingConstraint highestPriorityViolation = violatedConstraints.get(0);
            if (highestPriorityViolation.getPriority() >= 8) {
                result.addError("Schedule violates high-priority constraint: " + highestPriorityViolation.getName());
            }
        }
    }

    private void validateModificationImpact(SchedulingConstraint oldConstraint, SchedulingConstraint newConstraint, List<Schedule> affectedSchedules, ValidationResult result) {
        for (Schedule schedule : affectedSchedules) {
            if (isScheduleCompliantWithConstraint(schedule, oldConstraint) &&
                !isScheduleCompliantWithConstraint(schedule, newConstraint)) {
                result.addError("Constraint modification would invalidate schedule: " + schedule.getScheduleSummary());
            }
        }
    }

    private void validateConstraintTypeChange(SchedulingConstraint oldConstraint, SchedulingConstraint newConstraint, ValidationResult result) {
        if (!oldConstraint.getConstraintType().equals(newConstraint.getConstraintType())) {
            result.addWarning("Changing constraint type from " + oldConstraint.getConstraintType() +
                            " to " + newConstraint.getConstraintType());
        }
    }

    private void validateParameterValueChange(SchedulingConstraint oldConstraint, SchedulingConstraint newConstraint, ValidationResult result) {
        Map<String, Object> oldParams = oldConstraint.getParameters();
        Map<String, Object> newParams = newConstraint.getParameters();

        for (Map.Entry<String, Object> entry : newParams.entrySet()) {
            String key = entry.getKey();
            Object newValue = entry.getValue();
            Object oldValue = oldParams.get(key);

            if (oldValue != null && !oldValue.equals(newValue)) {
                result.addWarning("Parameter '" + key + "' changed from " + oldValue + " to " + newValue);
            }
        }
    }

    private void validateParameterType(String key, Object value, ValidationResult result) {
        try {
            if (value instanceof String) {
                if (((String) value).length() > 100) {
                    result.addWarning("String parameter '" + key + "' is very long");
                }
            } else if (value instanceof Number) {
                if (((Number) value).doubleValue() < 0) {
                    result.addWarning("Numeric parameter '" + key + "' is negative");
                }
            } else if (value instanceof Boolean) {
            } else {
                result.addWarning("Parameter '" + key + "' has unsupported type: " + value.getClass().getSimpleName());
            }
        } catch (Exception e) {
            result.addError("Parameter '" + key + "' validation failed: " + e.getMessage());
        }
    }

    private void validateTeacherWorkloadFeasibility(Map<String, Object> parameters, ValidationResult result) {
        if (parameters.containsKey("maxWeeklyHours")) {
            try {
                BigDecimal maxWeeklyHours = new BigDecimal(parameters.get("maxWeeklyHours").toString());
                if (maxWeeklyHours.compareTo(BigDecimal.ZERO) <= 0 || maxWeeklyHours.compareTo(new BigDecimal("80")) > 0) {
                    result.addError("Max weekly hours must be between 0 and 80");
                }
            } catch (Exception e) {
                result.addError("Invalid maxWeeklyHours parameter: " + e.getMessage());
            }
        }
    }

    private void validateClassroomUtilizationFeasibility(Map<String, Object> parameters, ValidationResult result) {
        if (parameters.containsKey("maxUtilizationRate")) {
            try {
                Double maxUtilizationRate = Double.parseDouble(parameters.get("maxUtilizationRate").toString());
                if (maxUtilizationRate < 0.0 || maxUtilizationRate > 1.0) {
                    result.addError("Max utilization rate must be between 0.0 and 1.0");
                }
            } catch (Exception e) {
                result.addError("Invalid maxUtilizationRate parameter: " + e.getMessage());
            }
        }
    }

    private void validateCourseSchedulingFeasibility(Map<String, Object> parameters, ValidationResult result) {
        if (parameters.containsKey("minMeetingFrequency")) {
            try {
                Integer minFrequency = Integer.parseInt(parameters.get("minMeetingFrequency").toString());
                if (minFrequency < 1 || minFrequency > 7) {
                    result.addError("Min meeting frequency must be between 1 and 7");
                }
            } catch (Exception e) {
                result.addError("Invalid minMeetingFrequency parameter: " + e.getMessage());
            }
        }
    }

    private void validateTimeAvailabilityFeasibility(Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateBusinessRuleFeasibility(Map<String, Object> parameters, ValidationResult result) {
    }

    private BigDecimal calculateTeacherWeeklyHours(Teacher teacher, List<Schedule> schedules) {
        return schedules.stream()
            .filter(s -> s.getCourseOffering() != null && s.getCourseOffering().getTeacher().equals(teacher))
            .map(s -> {
                LocalTime start = s.getTimeSlot().getStartTime();
                LocalTime end = s.getTimeSlot().getEndTime();
                return BigDecimal.valueOf(java.time.Duration.between(start, end).toMinutes() / 60.0);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<LocalDate, BigDecimal> calculateTeacherDailyHours(Teacher teacher, List<Schedule> schedules) {
        return schedules.stream()
            .filter(s -> s.getCourseOffering() != null && s.getCourseOffering().getTeacher().equals(teacher))
            .collect(Collectors.groupingBy(
                Schedule::getScheduleDate,
                Collectors.mapping(
                    s -> BigDecimal.valueOf(java.time.Duration.between(
                        s.getTimeSlot().getStartTime(), s.getTimeSlot().getEndTime()).toMinutes() / 60.0),
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));
    }

    private List<java.time.temporal.TemporalAmount> findConsecutiveTeachingBlocks(Teacher teacher, List<Schedule> schedules) {
        return List.of();
    }

    private List<java.time.Duration> findPreparationGaps(Teacher teacher, List<Schedule> schedules) {
        return List.of();
    }

    private double calculateClassroomUtilization(Classroom classroom, List<Schedule> schedules) {
        return 0.0;
    }

    private Map<DayOfWeek, Double> calculateClassroomDailyUtilization(Classroom classroom, List<Schedule> schedules) {
        return Map.of();
    }

    private void validateMaintenanceWindowConflicts(Classroom classroom, List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateEquipmentUsageLimits(Classroom classroom, List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateTeacherWeeklyHoursLimits(List<Schedule> schedules, BigDecimal maxWeeklyHours, ValidationResult result) {
    }

    private void validateTeacherDailyHoursLimits(List<Schedule> schedules, BigDecimal maxDailyHours, ValidationResult result) {
    }

    private void validateTeacherConsecutiveHoursLimits(List<Schedule> schedules, Integer maxConsecutiveHours, ValidationResult result) {
    }

    private void validateClassroomUtilizationLimits(List<Schedule> schedules, Double maxUtilizationRate, ValidationResult result) {
    }

    private void validateClassroomBreakTime(List<Schedule> schedules, Integer minBreakTime, ValidationResult result) {
    }

    private void validateCourseMeetingFrequency(List<Schedule> schedules, Integer minMeetingFrequency, ValidationResult result) {
    }

    private void validateCourseClassSize(List<Schedule> schedules, Integer maxClassSize, ValidationResult result) {
    }

    private void validateTimeSlotAvailability(List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateTimeSlotExclusions(List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateBusinessRuleCompliance(List<Schedule> schedules, SchedulingConstraint rule, ValidationResult result) {
    }

    private void validatePreferredTimeSlots(Course course, List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateRoomTypeRequirements(Course course, List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateClassSizeLimits(Course course, List<Schedule> schedules, Integer maxClassSize, ValidationResult result) {
    }

    private void validateHolidayScheduling(Semester semester, List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateExamPeriodScheduling(Semester semester, List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateBreakPeriodScheduling(Semester semester, List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateBuildingHoursCompliance(List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateParkingRestrictions(List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateSecurityHours(List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private void validateAccessibilityRequirements(List<Schedule> schedules, Map<String, Object> parameters, ValidationResult result) {
    }

    private boolean isScheduleCompliantWithConstraint(Schedule schedule, SchedulingConstraint constraint) {
        return true;
    }
}