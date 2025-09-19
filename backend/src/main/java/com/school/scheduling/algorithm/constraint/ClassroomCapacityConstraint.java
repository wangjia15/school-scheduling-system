package com.school.scheduling.algorithm.constraint;

import com.school.scheduling.domain.constraint.*;
import com.school.scheduling.domain.Classroom;
import com.school.scheduling.domain.CourseOffering;

import java.util.*;

/**
 * Constraint that ensures classroom capacity is not exceeded and room types match course requirements.
 */
public class ClassroomCapacityConstraint extends SchedulingConstraint {

    private final Map<String, ClassroomInfo> classroomInfo;
    private final Map<String, CourseRequirements> courseRequirements;
    private final boolean allowOversubscription;
    private final double maxOversubscriptionRatio;

    public ClassroomCapacityConstraint(Map<String, ClassroomInfo> classroomInfo,
                                     Map<String, CourseRequirements> courseRequirements,
                                     boolean allowOversubscription, double maxOversubscriptionRatio) {
        super("Classroom Capacity Constraint", ConstraintType.CLASSROOM_CAPACITY, ConstraintPriority.HARD,
              "Ensures classroom capacity is not exceeded and room types match requirements");
        this.classroomInfo = new HashMap<>(classroomInfo);
        this.courseRequirements = new HashMap<>(courseRequirements);
        this.allowOversubscription = allowOversubscription;
        this.maxOversubscriptionRatio = maxOversubscriptionRatio;
    }

    @Override
    public ConstraintResult validate(SchedulingAssignment assignment) {
        // Check all classroom assignments in the current assignment
        Map<String, List<ScheduledClass>> classroomSchedules = extractClassroomSchedules(assignment);

        for (Map.Entry<String, List<ScheduledClass>> entry : classroomSchedules.entrySet()) {
            String classroomId = entry.getKey();
            List<ScheduledClass> scheduledClasses = entry.getValue();

            // Check time conflicts for the same classroom
            ConstraintResult conflictResult = checkClassroomConflicts(scheduledClasses);
            if (!conflictResult.isSatisfied()) {
                return conflictResult;
            }

            // Check capacity for each class
            for (ScheduledClass scheduledClass : scheduledClasses) {
                ConstraintResult capacityResult = checkClassCapacity(classroomId, scheduledClass);
                if (!capacityResult.isSatisfied()) {
                    return capacityResult;
                }

                // Check room type requirements
                ConstraintResult roomTypeResult = checkRoomTypeRequirements(classroomId, scheduledClass);
                if (!roomTypeResult.isSatisfied()) {
                    return roomTypeResult;
                }

                // Check equipment requirements
                ConstraintResult equipmentResult = checkEquipmentRequirements(classroomId, scheduledClass);
                if (!equipmentResult.isSatisfied()) {
                    return equipmentResult;
                }
            }
        }

        return ConstraintResult.satisfied();
    }

    @Override
    public Set<SchedulingVariable> getScope() {
        // This constraint affects all classroom assignment variables
        Set<SchedulingVariable> scope = new HashSet<>();
        // In a real implementation, we'd return the specific classroom variables
        return scope;
    }

    @Override
    public List<SchedulingValue> getValidValues(SchedulingAssignment assignment,
                                               Map<SchedulingVariable, List<SchedulingValue>> domains) {
        // Return classrooms that meet the requirements
        List<SchedulingValue> validValues = new ArrayList<>();

        for (Map.Entry<SchedulingVariable, List<SchedulingValue>> entry : domains.entrySet()) {
            SchedulingVariable variable = entry.getKey();
            if (variable.getType() == VariableType.CLASSROOM_ASSIGNMENT) {
                for (SchedulingValue value : entry.getValue()) {
                    if (isClassroomSuitableForAssignment(assignment, variable, value)) {
                        validValues.add(value);
                    }
                }
            }
        }

        return validValues;
    }

    private ConstraintResult checkClassroomConflicts(List<ScheduledClass> scheduledClasses) {
        // Sort by start time
        scheduledClasses.sort(Comparator.comparing(ScheduledClass::getStartTime));

        for (int i = 0; i < scheduledClasses.size() - 1; i++) {
            ScheduledClass current = scheduledClasses.get(i);
            ScheduledClass next = scheduledClasses.get(i + 1);

            // Check for time conflicts on the same day
            if (current.getDayOfWeek() == next.getDayOfWeek() &&
                current.getEndTime().isAfter(next.getStartTime())) {
                return ConstraintResult.violated(
                    String.format("Classroom conflict: %s (%s) overlaps with %s (%s) on %s",
                        current.getCourseOfferingId(), current.getTimeRange(),
                        next.getCourseOfferingId(), next.getTimeRange(),
                        current.getDayOfWeek()),
                    1.0,
                    Arrays.asList("Classroom_" + current.getClassroomId(),
                                 current.getCourseOfferingId(), next.getCourseOfferingId())
                );
            }
        }

        return ConstraintResult.satisfied();
    }

    private ConstraintResult checkClassCapacity(String classroomId, ScheduledClass scheduledClass) {
        ClassroomInfo classroom = classroomInfo.get(classroomId);
        CourseRequirements course = courseRequirements.get(scheduledClass.getCourseOfferingId());

        if (classroom == null || course == null) {
            return ConstraintResult.violated("Missing classroom or course information");
        }

        int requiredCapacity = course.getExpectedEnrollment();
        int actualCapacity = classroom.getCapacity();

        if (requiredCapacity > actualCapacity) {
            if (allowOversubscription && requiredCapacity <= actualCapacity * maxOversubscriptionRatio) {
                return ConstraintResult.violated(
                    String.format("Course %s exceeds classroom capacity: %d > %d (oversubscribed)",
                        course.getCourseCode(), requiredCapacity, actualCapacity),
                    0.5, // Soft constraint when oversubscription is allowed
                    Arrays.asList("Classroom_" + classroomId, course.getCourseCode())
                );
            } else {
                return ConstraintResult.violated(
                    String.format("Course %s exceeds classroom capacity: %d > %d",
                        course.getCourseCode(), requiredCapacity, actualCapacity),
                    1.0,
                    Arrays.asList("Classroom_" + classroomId, course.getCourseCode())
                );
            }
        }

        // Check for underutilization (soft constraint)
        double utilizationRatio = (double) requiredCapacity / actualCapacity;
        if (utilizationRatio < 0.3) {
            return ConstraintResult.violated(
                String.format("Low classroom utilization: %s uses only %.1f%% of capacity",
                    course.getCourseCode(), utilizationRatio * 100),
                0.2, // Very soft constraint
                Arrays.asList("Classroom_" + classroomId, course.getCourseCode())
            );
        }

        return ConstraintResult.satisfied();
    }

    private ConstraintResult checkRoomTypeRequirements(String classroomId, ScheduledClass scheduledClass) {
        ClassroomInfo classroom = classroomInfo.get(classroomId);
        CourseRequirements course = courseRequirements.get(scheduledClass.getCourseOfferingId());

        if (classroom == null || course == null) {
            return ConstraintResult.satisfied();
        }

        // Check if room type matches course requirements
        if (course.getRequiredRoomType() != null &&
            !classroom.getRoomType().equals(course.getRequiredRoomType())) {
            return ConstraintResult.violated(
                String.format("Room type mismatch: %s requires %s but %s is %s",
                    course.getCourseCode(), course.getRequiredRoomType(),
                    classroomId, classroom.getRoomType()),
                1.0,
                Arrays.asList("Classroom_" + classroomId, course.getCourseCode())
            );
        }

        // Check for special room requirements
        if (course.requiresLab() && !classroom.isLab()) {
            return ConstraintResult.violated(
                String.format("Course %s requires lab but %s is not a lab room",
                    course.getCourseCode(), classroomId),
                1.0,
                Arrays.asList("Classroom_" + classroomId, course.getCourseCode())
            );
        }

        if (course.requiresComputerLab() && !classroom.hasComputers()) {
            return ConstraintResult.violated(
                String.format("Course %s requires computer lab but %s has no computers",
                    course.getCourseCode(), classroomId),
                1.0,
                Arrays.asList("Classroom_" + classroomId, course.getCourseCode())
            );
        }

        return ConstraintResult.satisfied();
    }

    private ConstraintResult checkEquipmentRequirements(String classroomId, ScheduledClass scheduledClass) {
        ClassroomInfo classroom = classroomInfo.get(classroomId);
        CourseRequirements course = courseRequirements.get(scheduledClass.getCourseOfferingId());

        if (classroom == null || course == null) {
            return ConstraintResult.satisfied();
        }

        // Check required equipment
        for (String requiredEquipment : course.getRequiredEquipment()) {
            if (!classroom.hasEquipment(requiredEquipment)) {
                return ConstraintResult.violated(
                    String.format("Missing equipment: %s requires %s but %s doesn't have it",
                        course.getCourseCode(), requiredEquipment, classroomId),
                    0.8,
                    Arrays.asList("Classroom_" + classroomId, course.getCourseCode())
                );
            }
        }

        return ConstraintResult.satisfied();
    }

    private Map<String, List<ScheduledClass>> extractClassroomSchedules(SchedulingAssignment assignment) {
        Map<String, List<ScheduledClass>> classroomSchedules = new HashMap<>();

        // Extract scheduled classes from assignment
        for (Map.Entry<SchedulingVariable, SchedulingValue> entry : assignment.getAssignments().entrySet()) {
            SchedulingVariable variable = entry.getKey();
            SchedulingValue value = entry.getValue();

            if (variable.getType() == VariableType.COURSE_SCHEDULING && value.isClassroom()) {
                ScheduledClass scheduledClass = createScheduledClassFromAssignment(variable, value);
                classroomSchedules.computeIfAbsent(scheduledClass.getClassroomId(), k -> new ArrayList<>())
                               .add(scheduledClass);
            }
        }

        return classroomSchedules;
    }

    private ScheduledClass createScheduledClassFromAssignment(SchedulingVariable variable, SchedulingValue value) {
        // This is a simplified version - in real implementation, we'd extract actual domain objects
        return new ScheduledClass(
            "course_" + variable.getEntityId(),
            "classroom_" + value.getValue(),
            DayOfWeek.MONDAY, // Placeholder
            java.time.LocalTime.of(9, 0), // Placeholder
            java.time.LocalTime.of(10, 30) // Placeholder
        );
    }

    private boolean isClassroomSuitableForAssignment(SchedulingAssignment assignment,
                                                    SchedulingVariable variable,
                                                    SchedulingValue value) {
        // Check if the classroom meets the requirements for this assignment
        // This is a simplified version - real implementation would check actual requirements
        return true;
    }

    // Helper classes
    public static class ClassroomInfo {
        private final String classroomId;
        private final String roomType;
        private final int capacity;
        private final boolean isLab;
        private final boolean hasComputers;
        private final Set<String> equipment;

        public ClassroomInfo(String classroomId, String roomType, int capacity, boolean isLab,
                           boolean hasComputers, Set<String> equipment) {
            this.classroomId = classroomId;
            this.roomType = roomType;
            this.capacity = capacity;
            this.isLab = isLab;
            this.hasComputers = hasComputers;
            this.equipment = new HashSet<>(equipment);
        }

        // Getters
        public String getClassroomId() { return classroomId; }
        public String getRoomType() { return roomType; }
        public int getCapacity() { return capacity; }
        public boolean isLab() { return isLab; }
        public boolean hasComputers() { return hasComputers; }
        public Set<String> getEquipment() { return new HashSet<>(equipment); }

        public boolean hasEquipment(String equipmentName) {
            return equipment.contains(equipmentName);
        }
    }

    public static class CourseRequirements {
        private final String courseCode;
        private final int expectedEnrollment;
        private final String requiredRoomType;
        private final boolean requiresLab;
        private final boolean requiresComputerLab;
        private final Set<String> requiredEquipment;

        public CourseRequirements(String courseCode, int expectedEnrollment, String requiredRoomType,
                                boolean requiresLab, boolean requiresComputerLab, Set<String> requiredEquipment) {
            this.courseCode = courseCode;
            this.expectedEnrollment = expectedEnrollment;
            this.requiredRoomType = requiredRoomType;
            this.requiresLab = requiresLab;
            this.requiresComputerLab = requiresComputerLab;
            this.requiredEquipment = new HashSet<>(requiredEquipment);
        }

        // Getters
        public String getCourseCode() { return courseCode; }
        public int getExpectedEnrollment() { return expectedEnrollment; }
        public String getRequiredRoomType() { return requiredRoomType; }
        public boolean requiresLab() { return requiresLab; }
        public boolean requiresComputerLab() { return requiresComputerLab; }
        public Set<String> getRequiredEquipment() { return new HashSet<>(requiredEquipment); }
    }

    public static class ScheduledClass {
        private final String courseOfferingId;
        private final String classroomId;
        private final DayOfWeek dayOfWeek;
        private final java.time.LocalTime startTime;
        private final java.time.LocalTime endTime;

        public ScheduledClass(String courseOfferingId, String classroomId,
                             DayOfWeek dayOfWeek, java.time.LocalTime startTime,
                             java.time.LocalTime endTime) {
            this.courseOfferingId = courseOfferingId;
            this.classroomId = classroomId;
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        // Getters
        public String getCourseOfferingId() { return courseOfferingId; }
        public String getClassroomId() { return classroomId; }
        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public java.time.LocalTime getStartTime() { return startTime; }
        public java.time.LocalTime getEndTime() { return endTime; }

        public String getTimeRange() {
            return startTime + "-" + endTime;
        }
    }
}