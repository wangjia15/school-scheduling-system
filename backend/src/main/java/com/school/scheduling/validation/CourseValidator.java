package com.school.scheduling.validation;

import com.school.scheduling.domain.Course;
import com.school.scheduling.domain.CoursePrerequisite;
import com.school.scheduling.domain.CourseOffering;
import com.school.scheduling.domain.Student;
import com.school.scheduling.domain.Teacher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CourseValidator extends BaseValidator {

    public ValidationResult validateCourseForCreation(Course course) {
        ValidationResult result = createValidationResult();

        validateRequiredField(course, "Course", result);
        if (!result.isValid()) {
            return result;
        }

        validateBasicCourseInfo(course, result);
        validateAcademicStructure(course, result);
        validateEnrollmentLimits(course, result);
        validateTimeRequirements(course, result);

        return result;
    }

    public ValidationResult validateCourseForUpdate(Course course) {
        ValidationResult result = createValidationResult();

        validateRequiredField(course, "Course", result);
        if (!result.isValid()) {
            return result;
        }

        validateBasicCourseInfo(course, result);
        validateAcademicStructure(course, result);
        validateEnrollmentLimits(course, result);
        validateTimeRequirements(course, result);
        validateUpdateImpact(course, result);

        return result;
    }

    public ValidationResult validateCoursePrerequisites(Course course, List<Course> allCourses) {
        ValidationResult result = createValidationResult();

        validateRequiredField(course, "Course", result);
        validateRequiredField(allCourses, "All courses", result);

        if (!result.isValid()) {
            return result;
        }

        validatePrerequisiteStructure(course, result);
        validatePrerequisiteCycles(course, allCourses, result);
        validatePrerequisiteLevelConsistency(course, result);

        return result;
    }

    public ValidationResult validateStudentEligibility(Student student, Course course, List<Course> completedCourses) {
        ValidationResult result = createValidationResult();

        validateRequiredField(student, "Student", result);
        validateRequiredField(course, "Course", result);
        validateRequiredField(completedCourses, "Completed courses", result);

        if (!result.isValid()) {
            return result;
        }

        validatePrerequisitesSatisfied(student, course, completedCourses, result);
        validateAcademicLevel(student, course, result);
        validateStudentCapacity(student, course, result);
        validateCourseAvailability(course, result);

        return result;
    }

    public ValidationResult validateCourseOffering(CourseOffering offering, List<CourseOffering> existingOfferings) {
        ValidationResult result = createValidationResult();

        validateRequiredField(offering, "Course offering", result);
        if (!result.isValid()) {
            return result;
        }

        validateOfferingStructure(offering, result);
        validateOfferingUniqueness(offering, existingOfferings, result);
        validateTeacherAssignment(offering, result);
        validateTimingConstraints(offering, result);

        return result;
    }

    public ValidationResult validateCourseDeletion(Course course, List<CourseOffering> offerings, List<CoursePrerequisite> prerequisites) {
        ValidationResult result = createValidationResult();

        validateRequiredField(course, "Course", result);
        if (!result.isValid()) {
            return result;
        }

        validateDeletionImpact(course, offerings, prerequisites, result);
        validateDependencyResolution(course, result);

        return result;
    }

    public ValidationResult validateCourseCodeFormat(String courseCode) {
        ValidationResult result = createValidationResult();

        validateRequiredField(courseCode, "Course code", result);
        validateNotBlank(courseCode, "Course code", result);
        validateMaxLength(courseCode, "Course code", 20, result);

        if (!result.isValid()) {
            return result;
        }

        String courseCodePattern = "^[A-Z]{2,4}[0-9]{3,4}[A-Z]?$";
        if (!courseCode.matches(courseCodePattern)) {
            result.addError("Course code format is invalid. Expected format: DEPT123 or DEPT1234 or DEPT1234A");
        }

        String deptCode = courseCode.replaceAll("[0-9A-Z]*$", "");
        if (deptCode.length() < 2 || deptCode.length() > 4) {
            result.addError("Department code must be 2-4 characters");
        }

        return result;
    }

    public ValidationResult validateCourseScheduleCompatibility(Course course, Teacher teacher) {
        ValidationResult result = createValidationResult();

        validateRequiredField(course, "Course", result);
        validateRequiredField(teacher, "Teacher", result);

        if (!result.isValid()) {
            return result;
        }

        validateTeacherQualifications(course, teacher, result);
        validateWorkloadCompatibility(course, teacher, result);
        validateScheduleConstraints(course, teacher, result);

        return result;
    }

    private void validateBasicCourseInfo(Course course, ValidationResult result) {
        validateRequiredField(course.getCourseCode(), "Course code", result);
        validateRequiredField(course.getTitle(), "Course title", result);
        validateRequiredField(course.getDepartment(), "Department", result);
        validateRequiredField(course.getLevel(), "Course level", result);
        validateRequiredField(course.getCredits(), "Credits", result);

        validateNotBlank(course.getCourseCode(), "Course code", result);
        validateNotBlank(course.getTitle(), "Course title", result);
        validateMaxLength(course.getTitle(), "Course title", 200, result);
        validateMaxLength(course.getDescription(), "Description", 2000, result);

        validateCourseCodeFormat(course.getCourseCode());
        result.merge(validateCourseCodeFormat(course.getCourseCode()));

        validateRange(course.getCredits(), "Credits", 1, 6, result);
    }

    private void validateAcademicStructure(Course course, ValidationResult result) {
        validateRange(course.getContactHoursPerWeek(), "Contact hours per week",
                    new BigDecimal("0.5"), new BigDecimal("20.0"), result);

        validateRange(course.getTheoryHours(), "Theory hours",
                    new BigDecimal("0.0"), new BigDecimal("20.0"), result);
        validateRange(course.getLabHours(), "Lab hours",
                    new BigDecimal("0.0"), new BigDecimal("20.0"), result);

        if (!course.hasValidHoursDistribution()) {
            result.addError("Theory hours + Lab hours must equal Contact hours per week");
        }

        if (course.getContactHoursPerWeek().compareTo(BigDecimal.ZERO) <= 0) {
            result.addError("Contact hours per week must be greater than 0");
        }

        if (course.getTheoryHours().compareTo(BigDecimal.ZERO) < 0 ||
            course.getLabHours().compareTo(BigDecimal.ZERO) < 0) {
            result.addError("Theory and Lab hours cannot be negative");
        }

        if (course.isLabCourse() && !course.requiresLab) {
            result.addWarning("Course has lab hours but is not marked as requiring lab");
        }

        if (course.requiresLab && course.getLabHours().compareTo(BigDecimal.ZERO) <= 0) {
            result.addWarning("Course requires lab but has no lab hours defined");
        }
    }

    private void validateEnrollmentLimits(Course course, ValidationResult result) {
        validateRange(course.getMinStudents(), "Minimum students", 1, 500, result);
        validateRange(course.getMaxStudents(), "Maximum students", 1, 500, result);

        if (!course.isValidEnrollmentRange()) {
            result.addError("Minimum students cannot exceed maximum students");
        }

        if (course.getMinStudents() < 5) {
            result.addWarning("Minimum students is very low (" + course.getMinStudents() + ")");
        }

        if (course.getMaxStudents() > 100) {
            result.addWarning("Maximum students is very high (" + course.getMaxStudents() + ")");
        }

        if (course.getMaxStudents() - course.getMinStudents() < 5) {
            result.addWarning("Enrollment range is very tight (" + course.getMinStudents() + " - " + course.getMaxStudents() + ")");
        }
    }

    private void validateTimeRequirements(Course course, ValidationResult result) {
        if (!course.isValidCourseStructure()) {
            result.addError("Course structure is invalid - check enrollment ranges and hours distribution");
        }

        if (course.getContactHoursPerWeek().compareTo(BigDecimal.valueOf(course.getCredits() * 3)) > 0) {
            result.addWarning("Contact hours (" + course.getContactHoursPerWeek() +
                            ") seem high for " + course.getCredits() + " credits");
        }

        if (course.getContactHoursPerWeek().compareTo(BigDecimal.valueOf(course.getCredits())) < 0) {
            result.addWarning("Contact hours (" + course.getContactHoursPerWeek() +
                            ") seem low for " + course.getCredits() + " credits");
        }
    }

    private void validateUpdateImpact(Course course, ValidationResult result) {
        if (!course.isActive()) {
            result.addWarning("Updating inactive course - verify this is intended");
        }

        if (course.hasPrerequisites() || course.isPrerequisiteForOtherCourses()) {
            result.addWarning("Course has prerequisite relationships - verify changes won't break dependencies");
        }

        if (course.getDifficultyLevel() >= 4) {
            result.addWarning("Updating advanced course - verify prerequisite requirements");
        }
    }

    private void validatePrerequisiteStructure(Course course, ValidationResult result) {
        if (course.hasPrerequisites()) {
            for (CoursePrerequisite prereq : course.getPrerequisites()) {
                if (prereq.getPrerequisiteCourse() == null) {
                    result.addError("Prerequisite course is missing");
                }
                if (prereq.getPrerequisiteCourse().equals(course)) {
                    result.addError("Course cannot be its own prerequisite");
                }
            }
        }
    }

    private void validatePrerequisiteCycles(Course course, List<Course> allCourses, ValidationResult result) {
        if (hasCircularPrerequisite(course, new HashSet<>(), allCourses)) {
            result.addError("Course has circular prerequisite dependencies");
        }
    }

    private void validatePrerequisiteLevelConsistency(Course course, ValidationResult result) {
        if (course.hasPrerequisites()) {
            for (CoursePrerequisite prereq : course.getPrerequisites()) {
                Course prereqCourse = prereq.getPrerequisiteCourse();
                if (prereqCourse != null) {
                    if (course.isUndergraduate() && prereqCourse.isGraduate()) {
                        result.addError("Undergraduate course cannot have graduate prerequisite");
                    }
                    if (course.isGraduate() && prereqCourse.isPhD()) {
                        result.addError("Graduate course cannot have PhD prerequisite");
                    }
                    if (course.getDifficultyLevel() <= prereqCourse.getDifficultyLevel()) {
                        result.addWarning("Prerequisite course has same or higher difficulty level");
                    }
                }
            }
        }
    }

    private void validatePrerequisitesSatisfied(Student student, Course course, List<Course> completedCourses, ValidationResult result) {
        if (course.hasPrerequisites()) {
            for (CoursePrerequisite prereq : course.getPrerequisites()) {
                Course prereqCourse = prereq.getPrerequisiteCourse();
                if (prereqCourse != null) {
                    boolean isCompleted = completedCourses.stream()
                        .anyMatch(completed -> completed.equals(prereqCourse));

                    if (!isCompleted) {
                        result.addError("Student has not completed prerequisite: " + prereqCourse.getFullDisplayName());
                    }
                }
            }
        }
    }

    private void validateAcademicLevel(Student student, Course course, ValidationResult result) {
        if (course.isPhD() && !student.isPhD()) {
            result.addError("Only PhD students can enroll in PhD courses");
        }

        if (course.isGraduate() && !student.isGraduate() && !student.isPhD()) {
            result.addError("Only graduate or PhD students can enroll in graduate courses");
        }

        if (course.requiresInstructorApproval()) {
            result.addWarning("Course requires instructor approval");
        }
    }

    private void validateStudentCapacity(Student student, Course course, ValidationResult result) {
        if (course.getMaxStudents() > 0) {
            int currentEnrollment = 0;
            double utilizationRate = (double) currentEnrollment / course.getMaxStudents();

            if (utilizationRate >= 1.0) {
                result.addError("Course is already at maximum capacity");
            } else if (utilizationRate >= 0.9) {
                result.addWarning("Course is near maximum capacity");
            }
        }
    }

    private void validateCourseAvailability(Course course, ValidationResult result) {
        if (!course.isActiveForRegistration()) {
            result.addError("Course is not available for registration");
        }

        if (!course.canBeOfferedThisSemester()) {
            result.addError("Course cannot be offered this semester");
        }
    }

    private void validateOfferingStructure(CourseOffering offering, ValidationResult result) {
        validateRequiredField(offering.getCourse(), "Course", result);
        validateRequiredField(offering.getSemester(), "Semester", result);
        validateRequiredField(offering.getTeacher(), "Teacher", result);

        if (offering.getSection() != null) {
            validateRange(offering.getSection(), "Section", 1, 99, result);
        }

        validateRange(offering.getMaxEnrollment(), "Maximum enrollment", 1, 500, result);
        validateRange(offering.getCurrentEnrollment(), "Current enrollment", 0, 500, result);

        if (offering.getCurrentEnrollment() > offering.getMaxEnrollment()) {
            result.addError("Current enrollment exceeds maximum enrollment");
        }
    }

    private void validateOfferingUniqueness(CourseOffering offering, List<CourseOffering> existingOfferings, ValidationResult result) {
        for (CourseOffering existing : existingOfferings) {
            if (!existing.equals(offering) &&
                existing.getCourse().equals(offering.getCourse()) &&
                existing.getSemester().equals(offering.getSemester()) &&
                existing.getSection() == offering.getSection()) {
                result.addError("Course offering already exists for this course, semester, and section");
                break;
            }
        }
    }

    private void validateTeacherAssignment(CourseOffering offering, ValidationResult result) {
        if (offering.getTeacher() != null && offering.getCourse() != null) {
            TeacherValidator teacherValidator = new TeacherValidator();
            ValidationResult teacherResult = teacherValidator.validateTeacherCourseAssignment(
                offering.getTeacher(), offering.getCourse());
            result.merge(teacherResult);
        }
    }

    private void validateTimingConstraints(CourseOffering offering, ValidationResult result) {
        if (offering.getStartDate() != null && offering.getEndDate() != null) {
            validateDateOrder(offering.getStartDate(), offering.getEndDate(), "Start date", "End date", result);
        }

        if (offering.getMeetingDays() != null && offering.getMeetingDays().isEmpty()) {
            result.addWarning("No meeting days specified");
        }

        if (offering.getStartTime() != null && offering.getEndTime() != null) {
            validateTimeOrder(offering.getStartTime(), offering.getEndTime(), "Start time", "End time", result);
        }
    }

    private void validateDeletionImpact(Course course, List<CourseOffering> offerings, List<CoursePrerequisite> prerequisites, ValidationResult result) {
        if (!offerings.isEmpty()) {
            result.addError("Cannot delete course with existing offerings (" + offerings.size() + " found)");
        }

        if (!prerequisites.isEmpty()) {
            result.addError("Cannot delete course that is a prerequisite for other courses (" + prerequisites.size() + " dependencies)");
        }
    }

    private void validateDependencyResolution(Course course, ValidationResult result) {
        if (course.isPrerequisiteForOtherCourses()) {
            result.addError("Cannot delete course that is prerequisite for other courses");
        }
    }

    private void validateTeacherQualifications(Course course, Teacher teacher, ValidationResult result) {
        if (course.isGraduate() && teacher.getTitle() == Teacher.TeacherTitle.INSTRUCTOR) {
            result.addWarning("Instructor assigned to graduate course");
        }

        if (course.isPhD() && (teacher.getTitle() == Teacher.TeacherTitle.INSTRUCTOR ||
                              teacher.getTitle() == Teacher.TeacherTitle.ADJUNCT)) {
            result.addError("Instructor or Adjunct cannot teach PhD courses");
        }

        if (!teacher.canTeachSubject(course.getCourseCode().substring(0, 3))) {
            result.addWarning("Teacher may not be qualified for this course subject");
        }
    }

    private void validateWorkloadCompatibility(Course course, Teacher teacher, ValidationResult result) {
        BigDecimal courseHours = course.getContactHoursPerWeek();
        if (courseHours.compareTo(teacher.getMaxWeeklyHours()) > 0) {
            result.addError("Course requires " + courseHours + " hours per week, but teacher's maximum is " + teacher.getMaxWeeklyHours());
        }

        if (courseHours.compareTo(teacher.getMaxWeeklyHours().multiply(new BigDecimal("0.5"))) > 0) {
            result.addWarning("Course requires more than 50% of teacher's weekly hours");
        }
    }

    private void validateScheduleConstraints(Course course, Teacher teacher, ValidationResult result) {
        if (course.hasLabComponent() && course.getLabHours().compareTo(BigDecimal.valueOf(2)) > 0) {
            result.addWarning("Course with extensive lab requirements - verify teacher availability");
        }

        if (course.getDifficultyLevel() >= 4 && course.getContactHoursPerWeek().compareTo(BigDecimal.valueOf(6)) > 0) {
            result.addWarning("Advanced course with high contact hours - verify teacher workload");
        }
    }

    private boolean hasCircularPrerequisite(Course course, Set<Long> visited, List<Course> allCourses) {
        if (visited.contains(course.getId())) {
            return true;
        }
        visited.add(course.getId());

        for (CoursePrerequisite prereq : course.getPrerequisites()) {
            Course prereqCourse = findCourseById(prereq.getPrerequisiteCourse().getId(), allCourses);
            if (prereqCourse != null && hasCircularPrerequisite(prereqCourse, visited, allCourses)) {
                return true;
            }
        }

        return false;
    }

    private Course findCourseById(Long id, List<Course> courses) {
        return courses.stream()
            .filter(course -> course.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
}