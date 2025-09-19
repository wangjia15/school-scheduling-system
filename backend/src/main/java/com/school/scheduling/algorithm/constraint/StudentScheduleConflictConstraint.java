package com.school.scheduling.algorithm.constraint;

import com.school.scheduling.domain.constraint.*;
import com.school.scheduling.domain.Student;
import com.school.scheduling.domain.Course;
import com.school.scheduling.domain.CoursePrerequisite;

import java.time.DayOfWeek;
import java.util.*;

/**
 * Constraint that prevents student schedule conflicts and ensures prerequisites are met.
 */
public class StudentScheduleConflictConstraint extends SchedulingConstraint {

    private final Map<Long, StudentInfo> studentInfo;
    private final Map<String, CoursePrerequisites> coursePrerequisites;
    private final int maxConsecutiveHours;
    private final int minBreakBetweenClasses;

    public StudentScheduleConflictConstraint(Map<Long, StudentInfo> studentInfo,
                                           Map<String, CoursePrerequisites> coursePrerequisites,
                                           int maxConsecutiveHours, int minBreakBetweenClasses) {
        super("Student Schedule Conflict Constraint", ConstraintType.STUDENT_CONFLICT, ConstraintPriority.HARD,
              "Prevents student schedule conflicts and ensures prerequisites are met");
        this.studentInfo = new HashMap<>(studentInfo);
        this.coursePrerequisites = new HashMap<>(coursePrerequisites);
        this.maxConsecutiveHours = maxConsecutiveHours;
        this.minBreakBetweenClasses = minBreakBetweenClasses;
    }

    @Override
    public ConstraintResult validate(SchedulingAssignment assignment) {
        // Extract student schedules from the assignment
        Map<Long, List<StudentSchedule>> studentSchedules = extractStudentSchedules(assignment);

        for (Map.Entry<Long, List<StudentSchedule>> entry : studentSchedules.entrySet()) {
            Long studentId = entry.getKey();
            List<StudentSchedule> schedules = entry.getValue();

            // Check for time conflicts
            ConstraintResult conflictResult = checkTimeConflicts(studentId, schedules);
            if (!conflictResult.isSatisfied()) {
                return conflictResult;
            }

            // Check prerequisites
            ConstraintResult prerequisiteResult = checkPrerequisites(studentId, schedules);
            if (!prerequisiteResult.isSatisfied()) {
                return prerequisiteResult;
            }

            // Check consecutive classes
            ConstraintResult consecutiveResult = checkConsecutiveClasses(studentId, schedules);
            if (!consecutiveResult.isSatisfied()) {
                return consecutiveResult;
            }

            // Check travel time between classes
            ConstraintResult travelResult = checkTravelTime(studentId, schedules);
            if (!travelResult.isSatisfied()) {
                return travelResult;
            }

            // Check credit load
            ConstraintResult creditResult = checkCreditLoad(studentId, schedules);
            if (!creditResult.isSatisfied()) {
                return creditResult;
            }
        }

        return ConstraintResult.satisfied();
    }

    @Override
    public Set<SchedulingVariable> getScope() {
        // This constraint affects all student enrollment variables
        Set<SchedulingVariable> scope = new HashSet<>();
        // In a real implementation, we'd return the specific student variables
        return scope;
    }

    @Override
    public List<SchedulingValue> getValidValues(SchedulingAssignment assignment,
                                               Map<SchedulingVariable, List<SchedulingValue>> domains) {
        // Return course offerings that don't conflict with student's schedule
        List<SchedulingValue> validValues = new ArrayList<>();

        for (Map.Entry<SchedulingVariable, List<SchedulingValue>> entry : domains.entrySet()) {
            SchedulingVariable variable = entry.getKey();
            if (variable.getType() == VariableType.STUDENT_ENROLLMENT) {
                for (SchedulingValue value : entry.getValue()) {
                    if (isCourseAvailableForStudent(assignment, variable, value)) {
                        validValues.add(value);
                    }
                }
            }
        }

        return validValues;
    }

    private ConstraintResult checkTimeConflicts(Long studentId, List<StudentSchedule> schedules) {
        // Group by day of week
        Map<DayOfWeek, List<StudentSchedule>> dailySchedules = new HashMap<>();
        for (StudentSchedule schedule : schedules) {
            dailySchedules.computeIfAbsent(schedule.getDayOfWeek(), k -> new ArrayList<>())
                        .add(schedule);
        }

        // Check conflicts for each day
        for (Map.Entry<DayOfWeek, List<StudentSchedule>> entry : dailySchedules.entrySet()) {
            DayOfWeek day = entry.getKey();
            List<StudentSchedule> daySchedules = entry.getValue();

            // Sort by start time
            daySchedules.sort(Comparator.comparing(StudentSchedule::getStartTime));

            // Check for overlaps
            for (int i = 0; i < daySchedules.size() - 1; i++) {
                StudentSchedule current = daySchedules.get(i);
                StudentSchedule next = daySchedules.get(i + 1);

                if (current.getEndTime().isAfter(next.getStartTime())) {
                    return ConstraintResult.violated(
                        String.format("Student %d has time conflict on %s: %s (%s) overlaps with %s (%s)",
                            studentId, day,
                            current.getCourseCode(), current.getTimeRange(),
                            next.getCourseCode(), next.getTimeRange()),
                        1.0,
                        Arrays.asList("Student_" + studentId, current.getCourseCode(), next.getCourseCode())
                    );
                }
            }
        }

        return ConstraintResult.satisfied();
    }

    private ConstraintResult checkPrerequisites(Long studentId, List<StudentSchedule> schedules) {
        StudentInfo student = studentInfo.get(studentId);
        if (student == null) {
            return ConstraintResult.violated("Student information not found for ID: " + studentId);
        }

        for (StudentSchedule schedule : schedules) {
            CoursePrerequisites prerequisites = coursePrerequisites.get(schedule.getCourseCode());
            if (prerequisites != null) {
                for (String requiredCourse : prerequisites.getRequiredCourses()) {
                    if (!student.hasCompletedCourse(requiredCourse)) {
                        return ConstraintResult.violated(
                            String.format("Student %d missing prerequisite %s for course %s",
                                studentId, requiredCourse, schedule.getCourseCode()),
                            1.0,
                            Arrays.asList("Student_" + studentId, schedule.getCourseCode())
                        );
                    }
                }

                // Check minimum grade requirements
                for (Map.Entry<String, String> gradeReq : prerequisites.getMinimumGradeRequirements().entrySet()) {
                    String requiredCourse = gradeReq.getKey();
                    String minGrade = gradeReq.getValue();

                    if (!student.hasCompletedCourseWithMinimumGrade(requiredCourse, minGrade)) {
                        return ConstraintResult.violated(
                            String.format("Student %d did not meet minimum grade requirement %s for %s (prerequisite for %s)",
                                studentId, minGrade, requiredCourse, schedule.getCourseCode()),
                            1.0,
                            Arrays.asList("Student_" + studentId, schedule.getCourseCode())
                        );
                    }
                }
            }
        }

        return ConstraintResult.satisfied();
    }

    private ConstraintResult checkConsecutiveClasses(Long studentId, List<StudentSchedule> schedules) {
        // Group by day of week
        Map<DayOfWeek, List<StudentSchedule>> dailySchedules = new HashMap<>();
        for (StudentSchedule schedule : schedules) {
            dailySchedules.computeIfAbsent(schedule.getDayOfWeek(), k -> new ArrayList<>())
                        .add(schedule);
        }

        for (Map.Entry<DayOfWeek, List<StudentSchedule>> entry : dailySchedules.entrySet()) {
            DayOfWeek day = entry.getKey();
            List<StudentSchedule> daySchedules = entry.getValue();

            // Sort by start time
            daySchedules.sort(Comparator.comparing(StudentSchedule::getStartTime));

            // Check consecutive classes
            for (int i = 0; i < daySchedules.size() - 1; i++) {
                StudentSchedule current = daySchedules.get(i);
                StudentSchedule next = daySchedules.get(i + 1);

                if (current.getDayOfWeek() == next.getDayOfWeek() &&
                    current.getEndTime().equals(next.getStartTime())) {

                    // Count consecutive hours
                    double consecutiveHours = current.getDurationHours() + next.getDurationHours();
                    int j = i + 2;

                    while (j < daySchedules.size() &&
                           daySchedules.get(j).getStartTime().equals(daySchedules.get(j-1).getEndTime())) {
                        consecutiveHours += daySchedules.get(j).getDurationHours();
                        j++;
                    }

                    if (consecutiveHours > maxConsecutiveHours) {
                        return ConstraintResult.violated(
                            String.format("Student %d has %.1f consecutive hours on %s (max: %d)",
                                studentId, consecutiveHours, day, maxConsecutiveHours),
                            0.8, // Soft constraint
                            Arrays.asList("Student_" + studentId)
                        );
                    }
                }
            }
        }

        return ConstraintResult.satisfied();
    }

    private ConstraintResult checkTravelTime(Long studentId, List<StudentSchedule> schedules) {
        // Group by day of week
        Map<DayOfWeek, List<StudentSchedule>> dailySchedules = new HashMap<>();
        for (StudentSchedule schedule : schedules) {
            dailySchedules.computeIfAbsent(schedule.getDayOfWeek(), k -> new ArrayList<>())
                        .add(schedule);
        }

        for (Map.Entry<DayOfWeek, List<StudentSchedule>> entry : dailySchedules.entrySet()) {
            DayOfWeek day = entry.getKey();
            List<StudentSchedule> daySchedules = entry.getValue();

            // Sort by start time
            daySchedules.sort(Comparator.comparing(StudentSchedule::getStartTime));

            for (int i = 0; i < daySchedules.size() - 1; i++) {
                StudentSchedule current = daySchedules.get(i);
                StudentSchedule next = daySchedules.get(i + 1);

                if (current.getDayOfWeek() == next.getDayOfWeek()) {
                    long breakMinutes = java.time.Duration.between(current.getEndTime(), next.getStartTime()).toMinutes();

                    // Check if classes are in different buildings
                    if (!current.getBuilding().equals(next.getBuilding())) {
                        if (breakMinutes < 15) { // Assume 15 minutes minimum for building change
                            return ConstraintResult.violated(
                                String.format("Insufficient travel time for student %d between %s (%s, %s) and %s (%s, %s) on %s: only %d minutes",
                                    studentId,
                                    current.getCourseCode(), current.getBuilding(), current.getRoomNumber(),
                                    next.getCourseCode(), next.getBuilding(), next.getRoomNumber(),
                                    day, breakMinutes),
                                0.7, // Soft constraint
                                Arrays.asList("Student_" + studentId)
                            );
                        }
                    } else if (breakMinutes < minBreakBetweenClasses) {
                        return ConstraintResult.violated(
                            String.format("Insufficient break time for student %d between classes on %s: %d minutes (minimum: %d)",
                                studentId, day, breakMinutes, minBreakBetweenClasses),
                            0.6, // Soft constraint
                            Arrays.asList("Student_" + studentId)
                        );
                    }
                }
            }
        }

        return ConstraintResult.satisfied();
    }

    private ConstraintResult checkCreditLoad(Long studentId, List<StudentSchedule> schedules) {
        StudentInfo student = studentInfo.get(studentId);
        if (student == null) {
            return ConstraintResult.satisfied();
        }

        // Calculate total credits
        double totalCredits = schedules.stream()
                .mapToDouble(s -> s.getCredits())
                .sum();

        if (totalCredits > student.getMaxCreditsPerSemester()) {
            return ConstraintResult.violated(
                String.format("Student %d exceeds maximum credit load: %.1f > %.1f",
                    studentId, totalCredits, student.getMaxCreditsPerSemester()),
                0.9, // Soft constraint
                Arrays.asList("Student_" + studentId)
            );
        }

        // Check for underload (warning)
        if (totalCredits < student.getMinCreditsPerSemester()) {
            return ConstraintResult.violated(
                String.format("Student %d is below minimum credit load: %.1f < %.1f",
                    studentId, totalCredits, student.getMinCreditsPerSemester()),
                0.3, // Very soft constraint
                Arrays.asList("Student_" + studentId)
            );
        }

        return ConstraintResult.satisfied();
    }

    private Map<Long, List<StudentSchedule>> extractStudentSchedules(SchedulingAssignment assignment) {
        Map<Long, List<StudentSchedule>> studentSchedules = new HashMap<>();

        // Extract student schedules from assignment
        for (Map.Entry<SchedulingVariable, SchedulingValue> entry : assignment.getAssignments().entrySet()) {
            SchedulingVariable variable = entry.getKey();
            SchedulingValue value = entry.getValue();

            if (variable.getType() == VariableType.STUDENT_ENROLLMENT) {
                // This would typically come from enrollment data
                // For now, we'll create a placeholder implementation
                Long studentId = Long.parseLong(variable.getEntityId());
                StudentSchedule schedule = createStudentScheduleFromAssignment(variable, value);
                studentSchedules.computeIfAbsent(studentId, k -> new ArrayList<>())
                               .add(schedule);
            }
        }

        return studentSchedules;
    }

    private StudentSchedule createStudentScheduleFromAssignment(SchedulingVariable variable, SchedulingValue value) {
        // This is a simplified version - in real implementation, we'd extract actual domain objects
        return new StudentSchedule(
            "course_" + value.getValue(),
            "COURSE101",
            "Main Building",
            "Room 101",
            DayOfWeek.MONDAY,
            java.time.LocalTime.of(9, 0),
            java.time.LocalTime.of(10, 30),
            3.0 // Credits
        );
    }

    private boolean isCourseAvailableForStudent(SchedulingAssignment assignment,
                                               SchedulingVariable variable,
                                               SchedulingValue value) {
        // Check if the course doesn't conflict with student's existing schedule
        // This is a simplified version - real implementation would check actual conflicts
        return true;
    }

    // Helper classes
    public static class StudentInfo {
        private final Long studentId;
        private final Set<String> completedCourses;
        private final Map<String, String> courseGrades;
        private final double maxCreditsPerSemester;
        private final double minCreditsPerSemester;

        public StudentInfo(Long studentId, Set<String> completedCourses, Map<String, String> courseGrades,
                         double maxCreditsPerSemester, double minCreditsPerSemester) {
            this.studentId = studentId;
            this.completedCourses = new HashSet<>(completedCourses);
            this.courseGrades = new HashMap<>(courseGrades);
            this.maxCreditsPerSemester = maxCreditsPerSemester;
            this.minCreditsPerSemester = minCreditsPerSemester;
        }

        public boolean hasCompletedCourse(String courseCode) {
            return completedCourses.contains(courseCode);
        }

        public boolean hasCompletedCourseWithMinimumGrade(String courseCode, String minGrade) {
            String actualGrade = courseGrades.get(courseCode);
            if (actualGrade == null) return false;
            return compareGrades(actualGrade, minGrade) >= 0;
        }

        private int compareGrades(String grade1, String grade2) {
            // Simple grade comparison - in real implementation, this would be more sophisticated
            Map<String, Integer> gradeValues = Map.of(
                "A", 4, "B", 3, "C", 2, "D", 1, "F", 0
            );
            return Integer.compare(
                gradeValues.getOrDefault(grade1, 0),
                gradeValues.getOrDefault(grade2, 0)
            );
        }

        // Getters
        public double getMaxCreditsPerSemester() { return maxCreditsPerSemester; }
        public double getMinCreditsPerSemester() { return minCreditsPerSemester; }
    }

    public static class CoursePrerequisites {
        private final String courseCode;
        private final Set<String> requiredCourses;
        private final Map<String, String> minimumGradeRequirements;

        public CoursePrerequisites(String courseCode, Set<String> requiredCourses,
                                  Map<String, String> minimumGradeRequirements) {
            this.courseCode = courseCode;
            this.requiredCourses = new HashSet<>(requiredCourses);
            this.minimumGradeRequirements = new HashMap<>(minimumGradeRequirements);
        }

        // Getters
        public String getCourseCode() { return courseCode; }
        public Set<String> getRequiredCourses() { return new HashSet<>(requiredCourses); }
        public Map<String, String> getMinimumGradeRequirements() { return new HashMap<>(minimumGradeRequirements); }
    }

    public static class StudentSchedule {
        private final String courseOfferingId;
        private final String courseCode;
        private final String building;
        private final String roomNumber;
        private final DayOfWeek dayOfWeek;
        private final java.time.LocalTime startTime;
        private final java.time.LocalTime endTime;
        private final double credits;

        public StudentSchedule(String courseOfferingId, String courseCode, String building, String roomNumber,
                              DayOfWeek dayOfWeek, java.time.LocalTime startTime,
                              java.time.LocalTime endTime, double credits) {
            this.courseOfferingId = courseOfferingId;
            this.courseCode = courseCode;
            this.building = building;
            this.roomNumber = roomNumber;
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
            this.credits = credits;
        }

        // Getters
        public String getCourseOfferingId() { return courseOfferingId; }
        public String getCourseCode() { return courseCode; }
        public String getBuilding() { return building; }
        public String getRoomNumber() { return roomNumber; }
        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public java.time.LocalTime getStartTime() { return startTime; }
        public java.time.LocalTime getEndTime() { return endTime; }
        public double getCredits() { return credits; }
        public double getDurationHours() {
            return java.time.Duration.between(startTime, endTime).toMinutes() / 60.0;
        }
        public String getTimeRange() {
            return startTime + "-" + endTime;
        }
    }
}