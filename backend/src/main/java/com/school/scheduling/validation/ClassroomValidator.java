package com.school.scheduling.validation;

import com.school.scheduling.domain.Classroom;
import com.school.scheduling.domain.Course;
import com.school.scheduling.domain.Schedule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClassroomValidator extends BaseValidator {

    public ValidationResult validateClassroomForCreation(Classroom classroom) {
        ValidationResult result = createValidationResult();

        validateRequiredField(classroom, "Classroom", result);
        if (!result.isValid()) {
            return result;
        }

        validateBasicClassroomInfo(classroom, result);
        validateCapacityConstraints(classroom, result);
        validateEquipmentRequirements(classroom, result);
        validateAvailabilityStatus(classroom, result);

        return result;
    }

    public ValidationResult validateClassroomForUpdate(Classroom classroom) {
        ValidationResult result = createValidationResult();

        validateRequiredField(classroom, "Classroom", result);
        if (!result.isValid()) {
            return result;
        }

        validateBasicClassroomInfo(classroom, result);
        validateCapacityConstraints(classroom, result);
        validateEquipmentRequirements(classroom, result);
        validateUpdateImpact(classroom, result);

        return result;
    }

    public ValidationResult validateClassroomAvailability(Classroom classroom, List<Schedule> existingSchedules,
                                                         DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        ValidationResult result = createValidationResult();

        validateRequiredField(classroom, "Classroom", result);
        validateRequiredField(existingSchedules, "Existing schedules", result);

        if (!result.isValid()) {
            return result;
        }

        validateClassroomOperationalStatus(classroom, result);
        validateScheduleConflicts(classroom, existingSchedules, dayOfWeek, startTime, endTime, result);
        validateMaintenanceWindowConflicts(classroom, dayOfWeek, startTime, endTime, result);

        return result;
    }

    public ValidationResult validateClassroomCourseCompatibility(Classroom classroom, Course course) {
        ValidationResult result = createValidationResult();

        validateRequiredField(classroom, "Classroom", result);
        validateRequiredField(course, "Course", result);

        if (!result.isValid()) {
            return result;
        }

        validateCapacitySufficiency(classroom, course, result);
        validateEquipmentCompatibility(classroom, course, result);
        validateRoomTypeSuitability(classroom, course, result);
        validateLocationRequirements(classroom, course, result);

        return result;
    }

    public ValidationResult validateClassroomUtilization(Classroom classroom, List<Schedule> schedules) {
        ValidationResult result = createValidationResult();

        validateRequiredField(classroom, "Classroom", result);
        validateRequiredField(schedules, "Schedules", result);

        if (!result.isValid()) {
            return result;
        }

        validateUtilizationRates(classroom, schedules, result);
        validateScheduleDistribution(classroom, schedules, result);
        validateOverbookingRisks(classroom, schedules, result);

        return result;
    }

    public ValidationResult validateClassroomDeletion(Classroom classroom, List<Schedule> activeSchedules) {
        ValidationResult result = createValidationResult();

        validateRequiredField(classroom, "Classroom", result);
        if (!result.isValid()) {
            return result;
        }

        validateDeletionImpact(classroom, activeSchedules, result);
        validateDependencyResolution(classroom, result);

        return result;
    }

    public ValidationResult validateClassroomEquipmentUpgrade(Classroom classroom, List<String> newEquipment) {
        ValidationResult result = createValidationResult();

        validateRequiredField(classroom, "Classroom", result);
        validateRequiredField(newEquipment, "New equipment", result);

        if (!result.isValid()) {
            return result;
        }

        validateEquipmentCompatibility(newEquipment, result);
        validateUpgradeFeasibility(classroom, newEquipment, result);
        validateMaintenanceRequirements(newEquipment, result);

        return result;
    }

    public ValidationResult validateClassroomCapacityChange(Classroom classroom, Integer newCapacity) {
        ValidationResult result = createValidationResult();

        validateRequiredField(classroom, "Classroom", result);
        validateRequiredField(newCapacity, "New capacity", result);

        if (!result.isValid()) {
            return result;
        }

        validateCapacityRange(newCapacity, result);
        validateCapacityChangeImpact(classroom, newCapacity, result);
        validateRegulatoryCompliance(classroom, newCapacity, result);

        return result;
    }

    public ValidationResult validateClassroomMaintenance(Classroom classroom, java.time.LocalDate maintenanceDate,
                                                         LocalTime startTime, LocalTime endTime) {
        ValidationResult result = createValidationResult();

        validateRequiredField(classroom, "Classroom", result);
        validateRequiredField(maintenanceDate, "Maintenance date", result);
        validateRequiredField(startTime, "Start time", result);
        validateRequiredField(endTime, "End time", result);

        if (!result.isValid()) {
            return result;
        }

        validateMaintenanceTiming(maintenanceDate, startTime, endTime, result);
        validateMaintenanceConflict(classroom, maintenanceDate, startTime, endTime, result);
        validateEmergencyMaintenanceRules(maintenanceDate, startTime, endTime, result);

        return result;
    }

    private void validateBasicClassroomInfo(Classroom classroom, ValidationResult result) {
        validateRequiredField(classroom.getBuildingCode(), "Building code", result);
        validateRequiredField(classroom.getRoomNumber(), "Room number", result);
        validateRequiredField(classroom.getName(), "Classroom name", result);
        validateRequiredField(classroom.getRoomType(), "Room type", result);

        validateNotBlank(classroom.getBuildingCode(), "Building code", result);
        validateNotBlank(classroom.getRoomNumber(), "Room number", result);
        validateNotBlank(classroom.getName(), "Classroom name", result);

        validateMaxLength(classroom.getBuildingCode(), "Building code", 20, result);
        validateMaxLength(classroom.getRoomNumber(), "Room number", 20, result);
        validateMaxLength(classroom.getName(), "Classroom name", 100, result);
        validateMaxLength(classroom.getSpecialEquipment(), "Special equipment", 1000, result);

        validateBuildingCodeFormat(classroom.getBuildingCode(), result);
        validateRoomNumberFormat(classroom.getRoomNumber(), result);
    }

    private void validateCapacityConstraints(Classroom classroom, ValidationResult result) {
        validatePositive(classroom.getCapacity(), "Capacity", result);
        validateRange(classroom.getCapacity(), "Capacity", 1, 500, result);

        if (classroom.getCapacity() < 10) {
            result.addWarning("Classroom capacity is very small (" + classroom.getCapacity() + ")");
        }

        if (classroom.getCapacity() > 200) {
            result.addWarning("Classroom capacity is very large (" + classroom.getCapacity() + ")");
        }

        if (classroom.isLargeClassroom() && !classroom.isLectureHall()) {
            result.addWarning("Large capacity classroom should be a lecture hall");
        }

        if (classroom.isSmallClassroom() && classroom.isLectureHall()) {
            result.addWarning("Lecture hall with small capacity - consider room type");
        }
    }

    private void validateEquipmentRequirements(Classroom classroom, ValidationResult result) {
        validateRequiredField(classroom.hasProjector(), "Projector availability", result);
        validateRequiredField(classroom.hasComputer(), "Computer availability", result);
        validateRequiredField(classroom.hasWhiteboard(), "Whiteboard availability", result);

        validateEquipmentCompatibility(classroom.getEquipmentList(), result);

        if (classroom.isComputerLab() && !classroom.hasComputer()) {
            result.addError("Computer lab must have computers");
        }

        if (classroom.isLaboratory() && !classroom.hasComputer() && !classroom.hasSpecialEquipment()) {
            result.addWarning("Laboratory classroom lacks specialized equipment");
        }

        if (classroom.isLectureHall() && !classroom.hasProjector()) {
            result.addWarning("Lecture hall without projector - consider adding one");
        }

        if (!classroom.hasWhiteboard()) {
            result.addWarning("Classroom without whiteboard - may limit teaching options");
        }
    }

    private void validateAvailabilityStatus(Classroom classroom, ValidationResult result) {
        validateRequiredField(classroom.isAvailable(), "Availability status", result);

        if (!classroom.isAvailable()) {
            result.addWarning("Classroom is marked as unavailable - verify this is intended");
        }

        if (!classroom.isAvailableForScheduling()) {
            result.addWarning("Classroom not available for scheduling - check availability and active status");
        }
    }

    private void validateUpdateImpact(Classroom classroom, ValidationResult result) {
        if (!classroom.isAvailable()) {
            result.addWarning("Updating unavailable classroom - verify this is intended");
        }

        if (classroom.getSpecialEquipment() != null && classroom.getSpecialEquipment().length() > 500) {
            result.addWarning("Special equipment description is very long");
        }

        if (classroom.getEquipmentList().size() > 10) {
            result.addWarning("Classroom has extensive equipment list - verify all equipment is functional");
        }
    }

    private void validateClassroomOperationalStatus(Classroom classroom, ValidationResult result) {
        if (!classroom.isAvailable()) {
            result.addError("Classroom is not available for scheduling");
        }

        if (!classroom.isActive()) {
            result.addError("Classroom is not active");
        }
    }

    private void validateScheduleConflicts(Classroom classroom, List<Schedule> existingSchedules,
                                         DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                                         ValidationResult result) {
        for (Schedule schedule : existingSchedules) {
            if (schedule.getTimeSlot() != null &&
                schedule.getTimeSlot().getDayOfWeek() == dayOfWeek &&
                schedule.getClassroom().equals(classroom)) {

                LocalTime existingStart = schedule.getTimeSlot().getStartTime();
                LocalTime existingEnd = schedule.getTimeSlot().getEndTime();

                if (hasTimeOverlap(startTime, endTime, existingStart, existingEnd)) {
                    result.addError("Classroom has a scheduling conflict with " +
                                  schedule.getScheduleSummary() +
                                  " (" + existingStart + " - " + existingEnd + ")");
                }

                if (hasAdjacentScheduleConflict(startTime, endTime, existingStart, existingEnd)) {
                    result.addWarning("Classroom has adjacent schedule with " +
                                    schedule.getScheduleSummary() +
                                    " - allow time for setup/cleanup");
                }
            }
        }
    }

    private void validateMaintenanceWindowConflicts(Classroom classroom, DayOfWeek dayOfWeek,
                                                    LocalTime startTime, LocalTime endTime,
                                                    ValidationResult result) {
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            result.addWarning("Scheduling on weekend - verify maintenance schedule");
        }

        if (startTime.isBefore(LocalTime.of(7, 0)) || endTime.isAfter(LocalTime.of(22, 0))) {
            result.addWarning("Scheduling outside normal building hours (7:00 AM - 10:00 PM)");
        }

        if (java.time.Duration.between(startTime, endTime).toMinutes() > 240) {
            result.addWarning("Long scheduling duration (" +
                            java.time.Duration.between(startTime, endTime).toMinutes() + " minutes) - verify maintenance windows");
        }
    }

    private void validateCapacitySufficiency(Classroom classroom, Course course, ValidationResult result) {
        if (course.getMaxStudents() != null && classroom.getCapacity() < course.getMaxStudents()) {
            result.addError("Classroom capacity (" + classroom.getCapacity() + ") is insufficient for course maximum students (" + course.getMaxStudents() + ")");
        }

        if (course.getMinStudents() != null && classroom.getCapacity() < course.getMinStudents()) {
            result.addError("Classroom capacity (" + classroom.getCapacity() + ") is below course minimum students (" + course.getMinStudents() + ")");
        }

        if (course.getMaxStudents() != null) {
            double utilizationRate = (double) course.getMaxStudents() / classroom.getCapacity();
            if (utilizationRate < 0.3) {
                result.addWarning("Low expected utilization rate (" + String.format("%.1f%%", utilizationRate * 100) + ")");
            }
            if (utilizationRate > 0.9) {
                result.addWarning("High expected utilization rate (" + String.format("%.1f%%", utilizationRate * 100) + ")");
            }
        }
    }

    private void validateEquipmentCompatibility(Classroom classroom, Course course, ValidationResult result) {
        List<String> requiredEquipment = getCourseRequiredEquipment(course);
        List<String> availableEquipment = classroom.getEquipmentList();

        for (String required : requiredEquipment) {
            if (!availableEquipment.contains(required)) {
                result.addError("Classroom lacks required equipment: " + required);
            }
        }

        if (course.requiresLab() && !classroom.isSuitableForLabWork()) {
            result.addError("Course requires lab but classroom is not suitable for lab work");
        }

        if (course.requiresComputerLab() && !classroom.isComputerLab()) {
            result.addError("Course requires computer lab but classroom is not a computer lab");
        }

        if (course.requiresScienceLab() && !classroom.isLaboratory()) {
            result.addError("Course requires science lab but classroom is not a laboratory");
        }
    }

    private void validateRoomTypeSuitability(Classroom classroom, Course course, ValidationResult result) {
        if (course.getMaxStudents() != null && course.getMaxStudents() > 50 && !classroom.isLectureHall()) {
            result.addWarning("Large class (" + course.getMaxStudents() + " students) in non-lecture hall");
        }

        if (course.hasLabComponent() && classroom.isLectureHall()) {
            result.addWarning("Lab course in lecture hall - verify equipment compatibility");
        }

        if (course.isUndergraduate() && classroom.isConferenceRoom()) {
            result.addWarning("Undergraduate course in conference room - verify space suitability");
        }

        if (course.isGraduate() && classroom.getCapacity() > 100) {
            result.addWarning("Graduate course in large classroom - consider more intimate setting");
        }
    }

    private void validateLocationRequirements(Classroom classroom, Course course, ValidationResult result) {
        String buildingCode = classroom.getBuildingCode().toUpperCase();
        String courseDept = course.getCourseCode().substring(0, Math.min(4, course.getCourseCode().length()));

        if (buildingCode.startsWith("SCI") && courseDept.matches("CS|MATH|STAT")) {
            result.addWarning("Computer science/math course in science building - verify location");
        }

        if (buildingCode.startsWith("ENG") && !courseDept.matches("ENGR|MECH|ELEC")) {
            result.addWarning("Non-engineering course in engineering building - verify location");
        }

        if (classroom.getRoomNumber().toLowerCase().contains("lab") && !course.hasLabComponent()) {
            result.addWarning("Non-lab course in lab room - verify suitability");
        }
    }

    private void validateUtilizationRates(Classroom classroom, List<Schedule> schedules, ValidationResult result) {
        if (schedules.isEmpty()) {
            result.addWarning("Classroom has no scheduled classes - underutilized");
            return;
        }

        int totalWeeklyHours = calculateTotalWeeklyHours(schedules);
        int capacity = classroom.getCapacity();
        double utilizationRate = calculateUtilizationRate(schedules, capacity);

        if (utilizationRate < 0.3) {
            result.addWarning("Low classroom utilization rate: " + String.format("%.1f%%", utilizationRate * 100));
        } else if (utilizationRate > 0.9) {
            result.addWarning("High classroom utilization rate: " + String.format("%.1f%%", utilizationRate * 100));
        }

        if (totalWeeklyHours < 10) {
            result.addWarning("Low weekly usage: " + totalWeeklyHours + " hours");
        } else if (totalWeeklyHours > 40) {
            result.addWarning("High weekly usage: " + totalWeeklyHours + " hours");
        }
    }

    private void validateScheduleDistribution(Classroom classroom, List<Schedule> schedules, ValidationResult result) {
        Set<DayOfWeek> scheduledDays = schedules.stream()
            .filter(schedule -> schedule.getTimeSlot() != null)
            .map(schedule -> schedule.getTimeSlot().getDayOfWeek())
            .collect(Collectors.toSet());

        if (scheduledDays.size() == 1) {
            result.addWarning("Classroom only used on " + scheduledDays.iterator().next() + " - consider distribution");
        }

        if (scheduledDays.size() > 5) {
            result.addWarning("Classroom used on " + scheduledDays.size() + " days - very high utilization");
        }

        long morningClasses = schedules.stream()
            .filter(schedule -> schedule.getTimeSlot() != null)
            .filter(schedule -> schedule.getTimeSlot().getStartTime().isBefore(LocalTime.of(12, 0)))
            .count();

        long afternoonClasses = schedules.stream()
            .filter(schedule -> schedule.getTimeSlot() != null)
            .filter(schedule -> !schedule.getTimeSlot().getStartTime().isBefore(LocalTime.of(12, 0)) &&
                               schedule.getTimeSlot().getStartTime().isBefore(LocalTime.of(17, 0)))
            .count();

        long eveningClasses = schedules.stream()
            .filter(schedule -> schedule.getTimeSlot() != null)
            .filter(schedule -> !schedule.getTimeSlot().getStartTime().isBefore(LocalTime.of(17, 0)))
            .count();

        if (eveningClasses > afternoonClasses) {
            result.addWarning("More evening classes than afternoon classes");
        }

        if (morningClasses == 0) {
            result.addWarning("No morning classes scheduled");
        }
    }

    private void validateOverbookingRisks(Classroom classroom, List<Schedule> schedules, ValidationResult result) {
        long backToBackCount = schedules.stream()
            .filter(schedule -> schedule.getTimeSlot() != null)
            .filter(schedule -> hasBackToBackSchedule(schedule, schedules))
            .count();

        if (backToBackCount > 3) {
            result.addWarning("High number of back-to-back schedules (" + backToBackCount + ") - allow transition time");
        }

        long quickTurnaroundCount = schedules.stream()
            .filter(schedule -> schedule.getTimeSlot() != null)
            .filter(schedule -> hasQuickTurnaround(schedule, schedules))
            .count();

        if (quickTurnaroundCount > 2) {
            result.addWarning("Multiple quick turnarounds (" + quickTurnaroundCount + ") - may not allow sufficient cleanup time");
        }
    }

    private void validateDeletionImpact(Classroom classroom, List<Schedule> activeSchedules, ValidationResult result) {
        if (!activeSchedules.isEmpty()) {
            result.addError("Cannot delete classroom with active schedules (" + activeSchedules.size() + " found)");
        }

        if (classroom.getCapacity() > 50) {
            result.addWarning("Deleting large capacity classroom - verify alternatives are available");
        }

        if (classroom.isComputerLab()) {
            result.addWarning("Deleting computer lab - verify sufficient alternatives exist");
        }

        if (classroom.isLaboratory()) {
            result.addWarning("Deleting laboratory - verify sufficient alternatives exist");
        }
    }

    private void validateDependencyResolution(Classroom classroom, ValidationResult result) {
        if (classroom.isComputerLab()) {
            result.addWarning("Computer lab deletion may affect course scheduling");
        }

        if (classroom.getCapacity() > 100) {
            result.addWarning("Large classroom deletion may affect large course scheduling");
        }
    }

    private void validateEquipmentCompatibility(List<String> newEquipment, ValidationResult result) {
        Set<String> duplicates = newEquipment.stream()
            .filter(item -> newEquipment.stream().filter(item::equals).count() > 1)
            .collect(Collectors.toSet());

        if (!duplicates.isEmpty()) {
            result.addError("Duplicate equipment items: " + duplicates);
        }

        for (String equipment : newEquipment) {
            if (equipment == null || equipment.trim().isEmpty()) {
                result.addError("Equipment item cannot be empty");
            } else if (equipment.length() > 50) {
                result.addError("Equipment item too long: " + equipment);
            } else if (!equipment.matches("^[A-Z0-9\\-_\\s]+$")) {
                result.addError("Invalid equipment format: " + equipment);
            }
        }
    }

    private void validateUpgradeFeasibility(Classroom classroom, List<String> newEquipment, ValidationResult result) {
        List<String> currentEquipment = classroom.getEquipmentList();

        if (newEquipment.contains("COMPUTER") && !classroom.isComputerLab()) {
            result.addWarning("Adding computers to non-computer lab - verify room suitability");
        }

        if (newEquipment.contains("PROJECTOR") && classroom.getCapacity() > 100 && !classroom.hasProjector()) {
            result.addWarning("Adding projector to large classroom - verify visibility");
        }

        if (newEquipment.size() > currentEquipment.size() + 3) {
            result.addWarning("Adding many new equipment items - verify installation feasibility");
        }
    }

    private void validateMaintenanceRequirements(List<String> newEquipment, ValidationResult result) {
        if (newEquipment.contains("COMPUTER")) {
            result.addWarning("Computers require regular maintenance and IT support");
        }

        if (newEquipment.contains("PROJECTOR")) {
            result.addWarning("Projectors require bulb replacement and calibration");
        }

        if (newEquipment.stream().anyMatch(item -> item.contains("LAB") || item.contains("SCIENCE"))) {
            result.addWarning("Laboratory equipment requires specialized maintenance");
        }
    }

    private void validateCapacityRange(Integer newCapacity, ValidationResult result) {
        validateRange(newCapacity, "New capacity", 1, 500, result);
    }

    private void validateCapacityChangeImpact(Classroom classroom, Integer newCapacity, ValidationResult result) {
        int currentCapacity = classroom.getCapacity();
        int difference = newCapacity - currentCapacity;

        if (Math.abs(difference) > 50) {
            result.addWarning("Large capacity change (" + difference + " seats) - verify feasibility");
        }

        if (difference > 0) {
            result.addWarning("Increasing capacity - verify egress and safety compliance");
        } else if (difference < 0) {
            result.addWarning("Decreasing capacity - verify impact on existing courses");
        }

        if (newCapacity > 100 && !classroom.isLectureHall()) {
            result.addWarning("Large capacity classroom should be a lecture hall");
        }
    }

    private void validateRegulatoryCompliance(Classroom classroom, Integer newCapacity, ValidationResult result) {
        if (newCapacity > 200) {
            result.addWarning("Capacity exceeds 200 - verify building code compliance");
        }

        if (newCapacity > 100 && !classroom.hasProjector()) {
            result.addWarning("Large classroom without projector - may not meet accessibility requirements");
        }

        if (newCapacity > 50 && !classroom.hasWhiteboard()) {
            result.addWarning("Medium/large classroom without whiteboard - may not meet teaching requirements");
        }
    }

    private void validateMaintenanceTiming(java.time.LocalDate maintenanceDate, LocalTime startTime, LocalTime endTime, ValidationResult result) {
        validateTimeOrder(startTime, endTime, "Start time", "End time", result);

        java.time.Duration duration = java.time.Duration.between(startTime, endTime);
        if (duration.toMinutes() < 30) {
            result.addError("Maintenance duration too short (minimum 30 minutes)");
        }
        if (duration.toHours() > 8) {
            result.addError("Maintenance duration too long (maximum 8 hours)");
        }

        if (maintenanceDate.isBefore(java.time.LocalDate.now())) {
            result.addError("Maintenance date cannot be in the past");
        }

        if (maintenanceDate.getDayOfWeek() == DayOfWeek.SATURDAY || maintenanceDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            result.addWarning("Weekend maintenance - verify staff availability");
        }
    }

    private void validateMaintenanceConflict(Classroom classroom, java.time.LocalDate maintenanceDate,
                                           LocalTime startTime, LocalTime endTime, ValidationResult result) {
        if (maintenanceDate.equals(java.time.LocalDate.now())) {
            result.addWarning("Same-day maintenance - may disrupt scheduled classes");
        }

        if (startTime.isBefore(LocalTime.of(8, 0)) || endTime.isAfter(LocalTime.of(18, 0))) {
            result.addWarning("Maintenance outside normal hours - verify staff availability");
        }
    }

    private void validateEmergencyMaintenanceRules(java.time.LocalDate maintenanceDate, LocalTime startTime, LocalTime endTime, ValidationResult result) {
        java.time.Duration duration = java.time.Duration.between(startTime, endTime);
        if (duration.toMinutes() < 60) {
            result.addWarning("Short maintenance window - may not be sufficient for proper work");
        }

        if (maintenanceDate.plusDays(1).isBefore(java.time.LocalDate.now().plusWeeks(1))) {
            result.addWarning("Short notice maintenance - may require emergency procedures");
        }
    }

    private List<String> getCourseRequiredEquipment(Course course) {
        List<String> required = new java.util.ArrayList<>();

        if (course.requiresComputerLab()) {
            required.add("COMPUTER");
        }

        if (course.requiresScienceLab()) {
            required.add("LAB_EQUIPMENT");
        }

        if (course.getTheoryHours().compareTo(BigDecimal.ZERO) > 0) {
            required.add("WHITEBOARD");
        }

        if (course.getMaxStudents() > 30) {
            required.add("PROJECTOR");
        }

        return required;
    }

    private boolean hasTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return !end1.isBefore(start2) && !start1.isAfter(end2);
    }

    private boolean hasAdjacentScheduleConflict(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        java.time.Duration gap1 = java.time.Duration.between(end1, start2);
        java.time.Duration gap2 = java.time.Duration.between(end2, start1);
        return gap1.abs().toMinutes() < 15 || gap2.abs().toMinutes() < 15;
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

    private boolean hasQuickTurnaround(Schedule schedule, List<Schedule> allSchedules) {
        if (schedule.getTimeSlot() == null) return false;

        return allSchedules.stream()
            .filter(s -> !s.equals(schedule) && s.getTimeSlot() != null)
            .filter(s -> s.getTimeSlot().getDayOfWeek() == schedule.getTimeSlot().getDayOfWeek())
            .anyMatch(s -> {
                LocalTime end1 = schedule.getTimeSlot().getEndTime();
                LocalTime start2 = s.getTimeSlot().getStartTime();
                LocalTime end2 = s.getTimeSlot().getEndTime();
                LocalTime start1 = schedule.getTimeSlot().getStartTime();

                return Math.abs(java.time.Duration.between(end1, start2).toMinutes()) < 15 ||
                       Math.abs(java.time.Duration.between(end2, start1).toMinutes()) < 15;
            });
    }

    private int calculateTotalWeeklyHours(List<Schedule> schedules) {
        return schedules.stream()
            .filter(schedule -> schedule.getTimeSlot() != null)
            .mapToInt(schedule -> {
                LocalTime start = schedule.getTimeSlot().getStartTime();
                LocalTime end = schedule.getTimeSlot().getEndTime();
                return (int) java.time.Duration.between(start, end).toMinutes() / 60;
            })
            .sum();
    }

    private double calculateUtilizationRate(List<Schedule> schedules, int capacity) {
        if (capacity == 0) return 0.0;

        int totalStudentHours = schedules.stream()
            .filter(schedule -> schedule.getCourseOffering() != null)
            .mapToInt(schedule -> {
                int enrollment = schedule.getCourseOffering().getCurrentEnrollment();
                long durationMinutes = java.time.Duration.between(
                    schedule.getTimeSlot().getStartTime(),
                    schedule.getTimeSlot().getEndTime()
                ).toMinutes();
                return enrollment * (int) durationMinutes;
            })
            .sum();

        int totalCapacityMinutes = capacity * 5 * 8 * 60;

        return (double) totalStudentHours / totalCapacityMinutes;
    }

    private void validateBuildingCodeFormat(String buildingCode, ValidationResult result) {
        if (!buildingCode.matches("^[A-Z]{2,4}$")) {
            result.addError("Building code must be 2-4 uppercase letters");
        }
    }

    private void validateRoomNumberFormat(String roomNumber, ValidationResult result) {
        if (!roomNumber.matches("^[0-9A-Z]{1,10}$")) {
            result.addError("Room number must be alphanumeric");
        }
    }
}