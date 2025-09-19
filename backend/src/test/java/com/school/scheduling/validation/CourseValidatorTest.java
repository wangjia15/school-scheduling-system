package com.school.scheduling.validation;

import com.school.scheduling.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseValidatorTest {

    private CourseValidator courseValidator;

    @Mock
    private Course course;

    @Mock
    private Department department;

    @Mock
    private Student student;

    @Mock
    private CoursePrerequisite prerequisite;

    @Mock
    private CourseOffering offering;

    @Mock
    private Teacher teacher;

    @BeforeEach
    void setUp() {
        courseValidator = new CourseValidator();
    }

    @Test
    void validateCourseForCreation_WithValidCourse_ShouldReturnValidResult() {
        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getTitle()).thenReturn("Introduction to Computer Science");
        when(course.getDepartment()).thenReturn(department);
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.getCredits()).thenReturn(3);
        when(course.getContactHoursPerWeek()).thenReturn(new BigDecimal("3.0"));
        when(course.getTheoryHours()).thenReturn(new BigDecimal("3.0"));
        when(course.getLabHours()).thenReturn(new BigDecimal("0.0"));
        when(course.getMinStudents()).thenReturn(5);
        when(course.getMaxStudents()).thenReturn(30);
        when(course.isActive()).thenReturn(true);

        ValidationResult result = courseValidator.validateCourseForCreation(course);

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
    }

    @Test
    void validateCourseForCreation_WithNullCourse_ShouldReturnInvalidResult() {
        ValidationResult result = courseValidator.validateCourseForCreation(null);

        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Course is required"));
    }

    @Test
    void validateCourseForCreation_WithInvalidCourseCode_ShouldReturnInvalidResult() {
        when(course.getCourseCode()).thenReturn("INVALID");
        when(course.getTitle()).thenReturn("Introduction to Computer Science");
        when(course.getDepartment()).thenReturn(department);
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.getCredits()).thenReturn(3);
        when(course.getContactHoursPerWeek()).thenReturn(new BigDecimal("3.0"));
        when(course.getTheoryHours()).thenReturn(new BigDecimal("3.0"));
        when(course.getLabHours()).thenReturn(new BigDecimal("0.0"));
        when(course.getMinStudents()).thenReturn(5);
        when(course.getMaxStudents()).thenReturn(30);
        when(course.isActive()).thenReturn(true);

        ValidationResult result = courseValidator.validateCourseForCreation(course);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Course code format is invalid")));
    }

    @Test
    void validateCourseForCreation_WithInvalidHoursDistribution_ShouldReturnInvalidResult() {
        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getTitle()).thenReturn("Introduction to Computer Science");
        when(course.getDepartment()).thenReturn(department);
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.getCredits()).thenReturn(3);
        when(course.getContactHoursPerWeek()).thenReturn(new BigDecimal("4.0"));
        when(course.getTheoryHours()).thenReturn(new BigDecimal("2.0"));
        when(course.getLabHours()).thenReturn(new BigDecimal("1.0"));
        when(course.getMinStudents()).thenReturn(5);
        when(course.getMaxStudents()).thenReturn(30);
        when(course.isActive()).thenReturn(true);

        ValidationResult result = courseValidator.validateCourseForCreation(course);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("must equal Contact hours per week")));
    }

    @Test
    void validateCourseForCreation_WithInvalidEnrollmentRange_ShouldReturnInvalidResult() {
        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getTitle()).thenReturn("Introduction to Computer Science");
        when(course.getDepartment()).thenReturn(department);
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.getCredits()).thenReturn(3);
        when(course.getContactHoursPerWeek()).thenReturn(new BigDecimal("3.0"));
        when(course.getTheoryHours()).thenReturn(new BigDecimal("3.0"));
        when(course.getLabHours()).thenReturn(new BigDecimal("0.0"));
        when(course.getMinStudents()).thenReturn(40);
        when(course.getMaxStudents()).thenReturn(30);
        when(course.isActive()).thenReturn(true);

        ValidationResult result = courseValidator.validateCourseForCreation(course);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Minimum students cannot exceed maximum students")));
    }

    @Test
    void validateCourseForCreation_WithVeryLargeCapacity_ShouldReturnWarning() {
        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getTitle()).thenReturn("Introduction to Computer Science");
        when(course.getDepartment()).thenReturn(department);
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.getCredits()).thenReturn(3);
        when(course.getContactHoursPerWeek()).thenReturn(new BigDecimal("3.0"));
        when(course.getTheoryHours()).thenReturn(new BigDecimal("3.0"));
        when(course.getLabHours()).thenReturn(new BigDecimal("0.0"));
        when(course.getMinStudents()).thenReturn(5);
        when(course.getMaxStudents()).thenReturn(300);
        when(course.isActive()).thenReturn(true);

        ValidationResult result = courseValidator.validateCourseForCreation(course);

        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("very high")));
    }

    @Test
    void validateCoursePrerequisites_WithValidPrerequisites_ShouldReturnValidResult() {
        Course prerequisiteCourse = new Course();
        prerequisiteCourse.setCourseCode("CS100");
        prerequisiteCourse.setLevel(Course.CourseLevel.UNDERGRADUATE);

        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.hasPrerequisites()).thenReturn(true);
        when(course.getPrerequisites()).thenReturn(Collections.singletonList(prerequisiteCourse));
        when(prerequisite.getPrerequisiteCourse()).thenReturn(prerequisiteCourse);

        List<Course> allCourses = Arrays.asList(course, prerequisiteCourse);

        ValidationResult result = courseValidator.validateCoursePrerequisites(course, allCourses);

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }

    @Test
    void validateCoursePrerequisites_WithCircularDependency_ShouldReturnInvalidResult() {
        Course prerequisiteCourse = new Course();
        prerequisiteCourse.setCourseCode("CS101");
        prerequisiteCourse.setLevel(Course.CourseLevel.UNDERGRADUATE);

        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.hasPrerequisites()).thenReturn(true);
        when(course.getPrerequisites()).thenReturn(Collections.singletonList(prerequisiteCourse));
        when(prerequisite.getPrerequisiteCourse()).thenReturn(course);

        List<Course> allCourses = Arrays.asList(course, prerequisiteCourse);

        ValidationResult result = courseValidator.validateCoursePrerequisites(course, allCourses);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("circular prerequisite dependencies")));
    }

    @Test
    void validateCoursePrerequisites_WithGraduatePrerequisiteForUndergraduate_ShouldReturnInvalidResult() {
        Course prerequisiteCourse = new Course();
        prerequisiteCourse.setCourseCode("CS500");
        prerequisiteCourse.setLevel(Course.CourseLevel.GRADUATE);

        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.hasPrerequisites()).thenReturn(true);
        when(course.getPrerequisites()).thenReturn(Collections.singletonList(prerequisiteCourse));
        when(prerequisite.getPrerequisiteCourse()).thenReturn(prerequisiteCourse);

        List<Course> allCourses = Arrays.asList(course, prerequisiteCourse);

        ValidationResult result = courseValidator.validateCoursePrerequisites(course, allCourses);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Undergraduate course cannot have graduate prerequisite")));
    }

    @Test
    void validateStudentEligibility_WithEligibleStudent_ShouldReturnValidResult() {
        Course completedCourse = new Course();
        completedCourse.setCourseCode("CS100");

        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.hasPrerequisites()).thenReturn(true);
        when(course.getPrerequisites()).thenReturn(Collections.singletonList(prerequisiteCourse));
        when(prerequisite.getPrerequisiteCourse()).thenReturn(completedCourse);
        when(course.getMaxStudents()).thenReturn(30);

        when(student.isUndergraduate()).thenReturn(true);
        when(student.isGraduate()).thenReturn(false);
        when(student.isPhD()).thenReturn(false);

        List<Course> completedCourses = Collections.singletonList(completedCourse);

        ValidationResult result = courseValidator.validateStudentEligibility(student, course, completedCourses);

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }

    @Test
    void validateStudentEligibility_WithUnsatisfiedPrerequisites_ShouldReturnInvalidResult() {
        Course prerequisiteCourse = new Course();
        prerequisiteCourse.setCourseCode("CS100");

        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.hasPrerequisites()).thenReturn(true);
        when(course.getPrerequisites()).thenReturn(Collections.singletonList(prerequisiteCourse));
        when(prerequisite.getPrerequisiteCourse()).thenReturn(prerequisiteCourse);

        when(student.isUndergraduate()).thenReturn(true);

        List<Course> completedCourses = Collections.emptyList();

        ValidationResult result = courseValidator.validateStudentEligibility(student, course, completedCourses);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("has not completed prerequisite")));
    }

    @Test
    void validateStudentEligibility_WithPhdStudentForUndergraduateCourse_ShouldReturnInvalidResult() {
        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.hasPrerequisites()).thenReturn(false);
        when(course.getMaxStudents()).thenReturn(30);

        when(student.isUndergraduate()).thenReturn(false);
        when(student.isGraduate()).thenReturn(false);
        when(student.isPhD()).thenReturn(true);

        List<Course> completedCourses = Collections.emptyList();

        ValidationResult result = courseValidator.validateStudentEligibility(student, course, completedCourses);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Only PhD students can enroll in PhD courses")));
    }

    @Test
    void validateCourseOffering_WithValidOffering_ShouldReturnValidResult() {
        Semester semester = new Semester();
        semester.setStartDate(java.time.LocalDate.now());
        semester.setEndDate(java.time.LocalDate.now().plusMonths(4));

        when(offering.getCourse()).thenReturn(course);
        when(offering.getSemester()).thenReturn(semester);
        when(offering.getTeacher()).thenReturn(teacher);
        when(offering.getSection()).thenReturn(1);
        when(offering.getMaxEnrollment()).thenReturn(30);
        when(offering.getCurrentEnrollment()).thenReturn(15);
        when(offering.getStartDate()).thenReturn(java.time.LocalDate.now().plusWeeks(1));
        when(offering.getEndDate()).thenReturn(java.time.LocalDate.now().plusMonths(4));

        List<CourseOffering> existingOfferings = Collections.emptyList();

        ValidationResult result = courseValidator.validateCourseOffering(offering, existingOfferings);

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }

    @Test
    void validateCourseOffering_WithExceededEnrollment_ShouldReturnInvalidResult() {
        Semester semester = new Semester();
        semester.setStartDate(java.time.LocalDate.now());
        semester.setEndDate(java.time.LocalDate.now().plusMonths(4));

        when(offering.getCourse()).thenReturn(course);
        when(offering.getSemester()).thenReturn(semester);
        when(offering.getTeacher()).thenReturn(teacher);
        when(offering.getSection()).thenReturn(1);
        when(offering.getMaxEnrollment()).thenReturn(30);
        when(offering.getCurrentEnrollment()).thenReturn(35);

        List<CourseOffering> existingOfferings = Collections.emptyList();

        ValidationResult result = courseValidator.validateCourseOffering(offering, existingOfferings);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Current enrollment exceeds maximum enrollment")));
    }

    @Test
    void validateCourseOffering_WithDuplicateOffering_ShouldReturnInvalidResult() {
        Semester semester = new Semester();
        semester.setStartDate(java.time.LocalDate.now());
        semester.setEndDate(java.time.LocalDate.now().plusMonths(4));

        when(offering.getCourse()).thenReturn(course);
        when(offering.getSemester()).thenReturn(semester);
        when(offering.getTeacher()).thenReturn(teacher);
        when(offering.getSection()).thenReturn(1);
        when(offering.getMaxEnrollment()).thenReturn(30);
        when(offering.getCurrentEnrollment()).thenReturn(15);

        CourseOffering existingOffering = new CourseOffering();
        existingOffering.setCourse(course);
        existingOffering.setSemester(semester);
        existingOffering.setSection(1);

        List<CourseOffering> existingOfferings = Collections.singletonList(existingOffering);

        ValidationResult result = courseValidator.validateCourseOffering(offering, existingOfferings);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Course offering already exists")));
    }

    @Test
    void validateCourseDeletion_WithExistingOfferings_ShouldReturnInvalidResult() {
        CourseOffering existingOffering = new CourseOffering();
        existingOffering.setCourse(course);

        List<CourseOffering> offerings = Collections.singletonList(existingOffering);
        List<CoursePrerequisite> prerequisites = Collections.emptyList();

        ValidationResult result = courseValidator.validateCourseDeletion(course, offerings, prerequisites);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Cannot delete course with existing offerings")));
    }

    @Test
    void validateCourseDeletion_WithPrerequisiteDependencies_ShouldReturnInvalidResult() {
        List<CourseOffering> offerings = Collections.emptyList();
        List<CoursePrerequisite> prerequisites = Collections.singletonList(prerequisiteCourse);

        when(course.isPrerequisiteForOtherCourses()).thenReturn(true);

        ValidationResult result = courseValidator.validateCourseDeletion(course, offerings, prerequisites);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Cannot delete course that is prerequisite for other courses")));
    }

    @Test
    void validateCourseCodeFormat_WithValidCode_ShouldReturnValidResult() {
        ValidationResult result = courseValidator.validateCourseCodeFormat("CS101");

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }

    @Test
    void validateCourseCodeFormat_WithInvalidCode_ShouldReturnInvalidResult() {
        ValidationResult result = courseValidator.validateCourseCodeFormat("INVALID");

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Course code format is invalid")));
    }

    @Test
    void validateCourseForUpdate_WithValidCourse_ShouldReturnValidResult() {
        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getTitle()).thenReturn("Introduction to Computer Science");
        when(course.getDepartment()).thenReturn(department);
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.getCredits()).thenReturn(3);
        when(course.getContactHoursPerWeek()).thenReturn(new BigDecimal("3.0"));
        when(course.getTheoryHours()).thenReturn(new BigDecimal("3.0"));
        when(course.getLabHours()).thenReturn(new BigDecimal("0.0"));
        when(course.getMinStudents()).thenReturn(5);
        when(course.getMaxStudents()).thenReturn(30);
        when(course.isActive()).thenReturn(true);

        ValidationResult result = courseValidator.validateCourseForUpdate(course);

        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }

    @Test
    void validateStudentEligibility_WithInactiveCourse_ShouldReturnInvalidResult() {
        Course completedCourse = new Course();
        completedCourse.setCourseCode("CS100");

        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getLevel()).thenReturn(Course.CourseLevel.UNDERGRADUATE);
        when(course.hasPrerequisites()).thenReturn(false);
        when(course.isActive()).thenReturn(false);

        when(student.isUndergraduate()).thenReturn(true);

        List<Course> completedCourses = Collections.singletonList(completedCourse);

        ValidationResult result = courseValidator.validateStudentEligibility(student, course, completedCourses);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("not available for registration")));
    }
}