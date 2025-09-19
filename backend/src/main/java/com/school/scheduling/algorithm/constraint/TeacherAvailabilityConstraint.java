package com.school.scheduling.algorithm.constraint;

import com.school.scheduling.domain.constraint.*;
import com.school.scheduling.domain.Teacher;
import com.school.scheduling.domain.CourseOffering;
import com.school.scheduling.domain.TimeSlot;
import com.school.scheduling.domain.Classroom;

import java.time.DayOfWeek;
import java.util.*;

/**
 * Constraint that ensures teachers are available during scheduled times and don't exceed workload limits.
 */
public class TeacherAvailabilityConstraint extends SchedulingConstraint {

    private final Map<Long, TeacherAvailability> teacherAvailabilities;
    private final int maxConsecutiveHours;
    private final int minBreakBetweenClasses;

    public TeacherAvailabilityConstraint(Map<Long, TeacherAvailability> teacherAvailabilities,
                                       int maxConsecutiveHours, int minBreakBetweenClasses) {
        super("Teacher Availability Constraint", ConstraintType.TEACHER_AVAILABILITY, ConstraintPriority.HARD,
              "Ensures teachers are available and not over-scheduled");
        this.teacherAvailabilities = new HashMap<>(teacherAvailabilities);
        this.maxConsecutiveHours = maxConsecutiveHours;
        this.minBreakBetweenClasses = minBreakBetweenClasses;
    }

    @Override
    public ConstraintResult validate(SchedulingAssignment assignment) {
        // Find all teacher assignments in the current assignment
        Map<Long, List<ScheduledClass>> teacherSchedules = extractTeacherSchedules(assignment);

        for (Map.Entry<Long, List<ScheduledClass>> entry : teacherSchedules.entrySet()) {
            Long teacherId = entry.getKey();
            List<ScheduledClass> teacherClasses = entry.getValue();

            // Sort by time
            teacherClasses.sort(Comparator.comparing(ScheduledClass::getStartTime));

            // Check availability
            ConstraintResult availabilityResult = checkTeacherAvailability(teacherId, teacherClasses);
            if (!availabilityResult.isSatisfied()) {
                return availabilityResult;
            }

            // Check workload limits
            ConstraintResult workloadResult = checkTeacherWorkload(teacherId, teacherClasses);
            if (!workloadResult.isSatisfied()) {
                return workloadResult;
            }

            // Check consecutive classes
            ConstraintResult consecutiveResult = checkConsecutiveClasses(teacherClasses);
            if (!consecutiveResult.isSatisfied()) {
                return consecutiveResult;
            }

            // Check break times
            ConstraintResult breakResult = checkBreakTimes(teacherClasses);
            if (!breakResult.isSatisfied()) {
                return breakResult;
            }
        }

        return ConstraintResult.satisfied();
    }

    @Override
    public Set<SchedulingVariable> getScope() {
        // This constraint affects all teacher assignment variables
        Set<SchedulingVariable> scope = new HashSet<>();
        // In a real implementation, we'd return the specific teacher variables
        return scope;
    }

    @Override
    public List<SchedulingValue> getValidValues(SchedulingAssignment assignment,
                                               Map<SchedulingVariable, List<SchedulingValue>> domains) {
        // Return time slots where the teacher is available
        List<SchedulingValue> validValues = new ArrayList<>();

        for (Map.Entry<SchedulingVariable, List<SchedulingValue>> entry : domains.entrySet()) {
            SchedulingVariable variable = entry.getKey();
            if (variable.getType() == VariableType.TEACHER_ASSIGNMENT) {
                for (SchedulingValue value : entry.getValue()) {
                    if (isTeacherAvailableForValue(assignment, variable, value)) {
                        validValues.add(value);
                    }
                }
            }
        }

        return validValues;
    }

    private ConstraintResult checkTeacherAvailability(Long teacherId, List<ScheduledClass> classes) {
        TeacherAvailability availability = teacherAvailabilities.get(teacherId);
        if (availability == null) {
            return ConstraintResult.violated("Teacher availability information not found for ID: " + teacherId);
        }

        for (ScheduledClass scheduledClass : classes) {
            if (!availability.isAvailable(scheduledClass.getDayOfWeek(),
                                        scheduledClass.getStartTime(),
                                        scheduledClass.getEndTime())) {
                return ConstraintResult.violated(
                    String.format("Teacher %d is not available on %s from %s to %s",
                        teacherId, scheduledClass.getDayOfWeek(),
                        scheduledClass.getStartTime(), scheduledClass.getEndTime()),
                    1.0,
                    Arrays.asList("Teacher_" + teacherId, scheduledClass.getCourseOfferingId())
                );
            }
        }

        return ConstraintResult.satisfied();
    }

    private ConstraintResult checkTeacherWorkload(Long teacherId, List<ScheduledClass> classes) {
        TeacherAvailability availability = teacherAvailabilities.get(teacherId);
        if (availability == null) {
            return ConstraintResult.satisfied(); // Already checked above
        }

        // Calculate weekly hours
        double totalWeeklyHours = classes.stream()
                .mapToDouble(ScheduledClass::getDurationHours)
                .sum();

        if (totalWeeklyHours > availability.getMaxWeeklyHours()) {
            return ConstraintResult.violated(
                String.format("Teacher %d exceeds maximum weekly hours: %.1f > %.1f",
                    teacherId, totalWeeklyHours, availability.getMaxWeeklyHours()),
                1.0,
                Arrays.asList("Teacher_" + teacherId)
            );
        }

        // Check courses per semester
        Set<String> uniqueCourses = new HashSet<>();
        for (ScheduledClass scheduledClass : classes) {
            uniqueCourses.add(scheduledClass.getCourseOfferingId());
        }

        if (uniqueCourses.size() > availability.getMaxCoursesPerSemester()) {
            return ConstraintResult.violated(
                String.format("Teacher %d exceeds maximum courses per semester: %d > %d",
                    teacherId, uniqueCourses.size(), availability.getMaxCoursesPerSemester()),
                1.0,
                Arrays.asList("Teacher_" + teacherId)
            );
        }

        return ConstraintResult.satisfied();
    }

    private ConstraintResult checkConsecutiveClasses(List<ScheduledClass> classes) {
        for (int i = 0; i < classes.size() - 1; i++) {
            ScheduledClass current = classes.get(i);
            ScheduledClass next = classes.get(i + 1);

            // Check if classes are on the same day and consecutive
            if (current.getDayOfWeek() == next.getDayOfWeek() &&
                current.getEndTime().equals(next.getStartTime())) {

                // Count consecutive classes
                int consecutiveCount = 2;
                double consecutiveHours = current.getDurationHours() + next.getDurationHours();

                int j = i + 2;
                while (j < classes.size() &&
                       classes.get(j).getDayOfWeek() == current.getDayOfWeek() &&
                       classes.get(j).getStartTime().equals(classes.get(j-1).getEndTime())) {
                    consecutiveCount++;
                    consecutiveHours += classes.get(j).getDurationHours();
                    j++;
                }

                if (consecutiveHours > maxConsecutiveHours) {
                    return ConstraintResult.violated(
                        String.format("Teacher has %.1f consecutive hours (max: %d) starting at %s on %s",
                            consecutiveHours, maxConsecutiveHours,
                            current.getStartTime(), current.getDayOfWeek()),
                        1.0,
                        Arrays.asList("Teacher_" + current.getTeacherId())
                    );
                }
            }
        }

        return ConstraintResult.satisfied();
    }

    private ConstraintResult checkBreakTimes(List<ScheduledClass> classes) {
        for (int i = 0; i < classes.size() - 1; i++) {
            ScheduledClass current = classes.get(i);
            ScheduledClass next = classes.get(i + 1);

            // Check if classes are on the same day
            if (current.getDayOfWeek() == next.getDayOfWeek()) {
                long breakMinutes = java.time.Duration.between(current.getEndTime(), next.getStartTime()).toMinutes();

                if (breakMinutes > 0 && breakMinutes < minBreakBetweenClasses) {
                    return ConstraintResult.violated(
                        String.format("Insufficient break time (%d minutes) between classes for teacher. Minimum required: %d minutes",
                            breakMinutes, minBreakBetweenClasses),
                        0.8, // Soft constraint
                        Arrays.asList("Teacher_" + current.getTeacherId())
                    );
                }
            }
        }

        return ConstraintResult.satisfied();
    }

    private Map<Long, List<ScheduledClass>> extractTeacherSchedules(SchedulingAssignment assignment) {
        Map<Long, List<ScheduledClass>> teacherSchedules = new HashMap<>();

        // Extract scheduled classes from assignment
        for (Map.Entry<SchedulingVariable, SchedulingValue> entry : assignment.getAssignments().entrySet()) {
            SchedulingVariable variable = entry.getKey();
            SchedulingValue value = entry.getValue();

            if (variable.getType() == VariableType.COURSE_SCHEDULING && value.isTimeSlot()) {
                // In a real implementation, we'd extract the actual course offering and time slot details
                // For now, we'll create a placeholder implementation
                ScheduledClass scheduledClass = createScheduledClassFromAssignment(variable, value);
                teacherSchedules.computeIfAbsent(scheduledClass.getTeacherId(), k -> new ArrayList<>())
                               .add(scheduledClass);
            }
        }

        return teacherSchedules;
    }

    private ScheduledClass createScheduledClassFromAssignment(SchedulingVariable variable, SchedulingValue value) {
        // This is a simplified version - in real implementation, we'd extract actual domain objects
        return new ScheduledClass(
            "course_" + variable.getEntityId(),
            1L, // Placeholder teacher ID
            "classroom_" + value.getValue(),
            DayOfWeek.MONDAY, // Placeholder
            java.time.LocalTime.of(9, 0), // Placeholder
            java.time.LocalTime.of(10, 30), // Placeholder
            1.5 // Duration
        );
    }

    private boolean isTeacherAvailableForValue(SchedulingAssignment assignment,
                                                SchedulingVariable variable,
                                                SchedulingValue value) {
        // Check if the teacher is available for this time slot
        // This is a simplified version - real implementation would check actual availability
        return true;
    }

    // Helper classes
    public static class TeacherAvailability {
        private final Long teacherId;
        private final Map<DayOfWeek, List<TimeRange>> availability;
        private final double maxWeeklyHours;
        private final int maxCoursesPerSemester;

        public TeacherAvailability(Long teacherId, Map<DayOfWeek, List<TimeRange>> availability,
                                 double maxWeeklyHours, int maxCoursesPerSemester) {
            this.teacherId = teacherId;
            this.availability = new HashMap<>(availability);
            this.maxWeeklyHours = maxWeeklyHours;
            this.maxCoursesPerSemester = maxCoursesPerSemester;
        }

        public boolean isAvailable(DayOfWeek day, java.time.LocalTime startTime, java.time.LocalTime endTime) {
            List<TimeRange> dayAvailability = availability.get(day);
            if (dayAvailability == null) return false;

            for (TimeRange range : dayAvailability) {
                if (range.contains(startTime, endTime)) {
                    return true;
                }
            }
            return false;
        }

        public double getMaxWeeklyHours() {
            return maxWeeklyHours;
        }

        public int getMaxCoursesPerSemester() {
            return maxCoursesPerSemester;
        }
    }

    public static class TimeRange {
        private final java.time.LocalTime startTime;
        private final java.time.LocalTime endTime;

        public TimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public boolean contains(java.time.LocalTime start, java.time.LocalTime end) {
            return !start.isBefore(startTime) && !end.isAfter(endTime);
        }
    }

    public static class ScheduledClass {
        private final String courseOfferingId;
        private final Long teacherId;
        private final String classroomId;
        private final DayOfWeek dayOfWeek;
        private final java.time.LocalTime startTime;
        private final java.time.LocalTime endTime;
        private final double durationHours;

        public ScheduledClass(String courseOfferingId, Long teacherId, String classroomId,
                             DayOfWeek dayOfWeek, java.time.LocalTime startTime,
                             java.time.LocalTime endTime, double durationHours) {
            this.courseOfferingId = courseOfferingId;
            this.teacherId = teacherId;
            this.classroomId = classroomId;
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationHours = durationHours;
        }

        // Getters
        public String getCourseOfferingId() { return courseOfferingId; }
        public Long getTeacherId() { return teacherId; }
        public String getClassroomId() { return classroomId; }
        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public java.time.LocalTime getStartTime() { return startTime; }
        public java.time.LocalTime getEndTime() { return endTime; }
        public double getDurationHours() { return durationHours; }
    }
}