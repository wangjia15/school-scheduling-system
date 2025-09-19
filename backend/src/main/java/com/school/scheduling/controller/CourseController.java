package com.school.scheduling.controller;

import com.school.scheduling.dto.PageRequest;
import com.school.scheduling.dto.response.ApiResponse;
import com.school.scheduling.dto.response.PaginatedResponse;
import com.school.scheduling.dto.response.CourseResponse;
import com.school.scheduling.dto.request.CourseRequest;
import com.school.scheduling.exception.ResourceNotFoundException;
import com.school.scheduling.exception.ResourceAlreadyExistsException;
import com.school.scheduling.exception.BadRequestException;
import com.school.scheduling.mapper.CourseMapper;
import com.school.scheduling.validation.CourseValidator;
import com.school.scheduling.validation.ValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Courses", description = "Course management endpoints")
public class CourseController {

    private final CourseMapper courseMapper;
    private final CourseValidator courseValidator;

    public CourseController(CourseMapper courseMapper, CourseValidator courseValidator) {
        this.courseMapper = courseMapper;
        this.courseValidator = courseValidator;
    }

    @GetMapping
    @Operation(summary = "Get all courses", description = "Retrieve a paginated list of all courses")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved courses",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<PaginatedResponse<CourseResponse>>> getAllCourses(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String direction,
            @Parameter(description = "Search term") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by department") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Filter by level") @RequestParam(required = false) String level,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active) {

        try {
            PageRequest pageRequest = new PageRequest(page, size, sort, direction);
            List<CourseResponse> courses = courseMapper.findAllWithFilters(pageRequest, search, departmentId, level, active)
                    .stream()
                    .map(CourseResponse::fromEntity)
                    .toList();

            long total = courseMapper.countWithFilters(search, departmentId, level, active);
            PaginatedResponse<CourseResponse> response = PaginatedResponse.of(courses, page, size, total);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve courses", e);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID", description = "Retrieve a specific course by its ID")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved course",
                    content = @Content(schema = @Schema(implementation = CourseResponse.class))),
            @SwaggerApiResponse(responseCode = "404", description = "Course not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(
            @Parameter(description = "Course ID") @PathVariable Long id) {

        try {
            com.school.scheduling.domain.Course course = courseMapper.findById(id);
            if (course == null) {
                throw new ResourceNotFoundException("Course not found with id: " + id);
            }

            CourseResponse response = CourseResponse.fromEntity(course);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve course with id: " + id, e);
        }
    }

    @GetMapping("/code/{courseCode}")
    @Operation(summary = "Get course by code", description = "Retrieve a specific course by its course code")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved course",
                    content = @Content(schema = @Schema(implementation = CourseResponse.class))),
            @SwaggerApiResponse(responseCode = "404", description = "Course not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseByCode(
            @Parameter(description = "Course code") @PathVariable String courseCode) {

        try {
            com.school.scheduling.domain.Course course = courseMapper.findByCourseCode(courseCode);
            if (course == null) {
                throw new ResourceNotFoundException("Course not found with code: " + courseCode);
            }

            CourseResponse response = CourseResponse.fromEntity(course);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve course with code: " + courseCode, e);
        }
    }

    @PostMapping
    @Operation(summary = "Create a new course", description = "Create a new course record")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "201", description = "Course created successfully",
                    content = @Content(schema = @Schema(implementation = CourseResponse.class))),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid input"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CourseRequest courseRequest) {

        try {
            ValidationResult validationResult = courseValidator.validateForCreation(courseRequest);
            if (!validationResult.isValid()) {
                throw new com.school.scheduling.validation.ValidationException("Course validation failed", validationResult);
            }

            // Check if course with course code already exists
            if (courseMapper.findByCourseCode(courseRequest.getCourseCode()) != null) {
                throw new ResourceAlreadyExistsException("Course", "courseCode", courseRequest.getCourseCode());
            }

            com.school.scheduling.domain.Course course = new com.school.scheduling.domain.Course();
            course.setCourseCode(courseRequest.getCourseCode());
            course.setTitle(courseRequest.getTitle());
            course.setDescription(courseRequest.getDescription());
            course.setDepartment(new com.school.scheduling.domain.Department(courseRequest.getDepartmentId()));
            course.setCredits(courseRequest.getCredits());
            course.setContactHoursPerWeek(courseRequest.getContactHoursPerWeek());
            course.setTheoryHours(courseRequest.getTheoryHours());
            course.setLabHours(courseRequest.getLabHours());
            course.setLevel(courseRequest.getLevel());
            course.setMaxStudents(courseRequest.getMaxStudents());
            course.setMinStudents(courseRequest.getMinStudents());
            course.setRequiresLab(courseRequest.getRequiresLab());

            courseMapper.insert(course);

            // Add prerequisites if provided
            if (courseRequest.getPrerequisites() != null) {
                courseRequest.getPrerequisites().forEach(prereqRequest -> {
                    com.school.scheduling.domain.CoursePrerequisite prerequisite = new com.school.scheduling.domain.CoursePrerequisite();
                    prerequisite.setCourse(course);
                    prerequisite.setPrerequisiteCourse(new com.school.scheduling.domain.Course(prereqRequest.getPrerequisiteCourseId()));
                    prerequisite.setIsMandatory(prereqRequest.getIsMandatory());
                    prerequisite.setMinimumGrade(prereqRequest.getMinimumGrade());
                    prerequisite.setNotes(prereqRequest.getNotes());
                    course.addPrerequisite(prerequisite);
                });
            }

            CourseResponse response = CourseResponse.fromEntity(courseMapper.findById(course.getId()));
            return new ResponseEntity<>(ApiResponse.created(response), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create course", e);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course", description = "Update an existing course record")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Course updated successfully",
                    content = @Content(schema = @Schema(implementation = CourseResponse.class))),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid input"),
            @SwaggerApiResponse(responseCode = "404", description = "Course not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @Parameter(description = "Course ID") @PathVariable Long id,
            @Valid @RequestBody CourseRequest courseRequest) {

        try {
            com.school.scheduling.domain.Course existingCourse = courseMapper.findById(id);
            if (existingCourse == null) {
                throw new ResourceNotFoundException("Course not found with id: " + id);
            }

            ValidationResult validationResult = courseValidator.validateForUpdate(existingCourse, courseRequest);
            if (!validationResult.isValid()) {
                throw new com.school.scheduling.validation.ValidationException("Course validation failed", validationResult);
            }

            // Update fields
            existingCourse.setCourseCode(courseRequest.getCourseCode());
            existingCourse.setTitle(courseRequest.getTitle());
            existingCourse.setDescription(courseRequest.getDescription());
            existingCourse.setDepartment(new com.school.scheduling.domain.Department(courseRequest.getDepartmentId()));
            existingCourse.setCredits(courseRequest.getCredits());
            existingCourse.setContactHoursPerWeek(courseRequest.getContactHoursPerWeek());
            existingCourse.setTheoryHours(courseRequest.getTheoryHours());
            existingCourse.setLabHours(courseRequest.getLabHours());
            existingCourse.setLevel(courseRequest.getLevel());
            existingCourse.setMaxStudents(courseRequest.getMaxStudents());
            existingCourse.setMinStudents(courseRequest.getMinStudents());
            existingCourse.setRequiresLab(courseRequest.getRequiresLab());

            courseMapper.update(existingCourse);

            // Update prerequisites if provided
            if (courseRequest.getPrerequisites() != null) {
                // Remove existing prerequisites
                existingCourse.getPrerequisites().clear();

                // Add new prerequisites
                courseRequest.getPrerequisites().forEach(prereqRequest -> {
                    com.school.scheduling.domain.CoursePrerequisite prerequisite = new com.school.scheduling.domain.CoursePrerequisite();
                    prerequisite.setCourse(existingCourse);
                    prerequisite.setPrerequisiteCourse(new com.school.scheduling.domain.Course(prereqRequest.getPrerequisiteCourseId()));
                    prerequisite.setIsMandatory(prereqRequest.getIsMandatory());
                    prerequisite.setMinimumGrade(prereqRequest.getMinimumGrade());
                    prerequisite.setNotes(prereqRequest.getNotes());
                    existingCourse.addPrerequisite(prerequisite);
                });
            }

            CourseResponse response = CourseResponse.fromEntity(courseMapper.findById(id));
            return ResponseEntity.ok(ApiResponse.updated(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update course with id: " + id, e);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a course", description = "Delete a course record")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Course deleted successfully"),
            @SwaggerApiResponse(responseCode = "404", description = "Course not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @Parameter(description = "Course ID") @PathVariable Long id) {

        try {
            com.school.scheduling.domain.Course course = courseMapper.findById(id);
            if (course == null) {
                throw new ResourceNotFoundException("Course not found with id: " + id);
            }

            // Check if course has active offerings or is a prerequisite for other courses
            if (courseMapper.hasActiveOfferings(id)) {
                throw new BadRequestException("Cannot delete course with active course offerings");
            }

            if (courseMapper.isPrerequisiteForOtherCourses(id)) {
                throw new BadRequestException("Cannot delete course that is a prerequisite for other courses");
            }

            courseMapper.deleteById(id);
            return ResponseEntity.ok(ApiResponse.deleted("Course deleted successfully"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete course with id: " + id, e);
        }
    }

    @GetMapping("/by-department/{departmentId}")
    @Operation(summary = "Get courses by department", description = "Retrieve all courses in a specific department")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved courses",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursesByDepartment(
            @Parameter(description = "Department ID") @PathVariable Long departmentId) {

        try {
            List<CourseResponse> courses = courseMapper.findByDepartmentId(departmentId)
                    .stream()
                    .map(CourseResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(courses));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve courses for department: " + departmentId, e);
        }
    }

    @GetMapping("/by-level/{level}")
    @Operation(summary = "Get courses by level", description = "Retrieve all courses at a specific level")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved courses",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursesByLevel(
            @Parameter(description = "Course level") @PathVariable String level) {

        try {
            com.school.scheduling.domain.Course.CourseLevel courseLevel = com.school.scheduling.domain.Course.CourseLevel.valueOf(level.toUpperCase());
            List<CourseResponse> courses = courseMapper.findByLevel(courseLevel)
                    .stream()
                    .map(CourseResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(courses));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid course level: " + level);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve courses for level: " + level, e);
        }
    }

    @GetMapping("/prerequisites/{courseId}")
    @Operation(summary = "Get course prerequisites", description = "Retrieve all prerequisites for a specific course")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved prerequisites",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "404", description = "Course not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCoursePrerequisites(
            @Parameter(description = "Course ID") @PathVariable Long courseId) {

        try {
            com.school.scheduling.domain.Course course = courseMapper.findById(courseId);
            if (course == null) {
                throw new ResourceNotFoundException("Course not found with id: " + courseId);
            }

            List<CourseResponse> prerequisites = courseMapper.findPrerequisitesByCourseId(courseId)
                    .stream()
                    .map(CourseResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(prerequisites));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve prerequisites for course: " + courseId, e);
        }
    }

    @GetMapping("/lab-courses")
    @Operation(summary = "Get lab courses", description = "Retrieve all courses that require laboratory facilities")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved lab courses",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getLabCourses() {

        try {
            List<CourseResponse> courses = courseMapper.findLabCourses()
                    .stream()
                    .map(CourseResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(courses));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve lab courses", e);
        }
    }
}