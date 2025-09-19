package com.school.scheduling.controller;

import com.school.scheduling.dto.PageRequest;
import com.school.scheduling.dto.response.ApiResponse;
import com.school.scheduling.dto.response.PaginatedResponse;
import com.school.scheduling.dto.response.TeacherResponse;
import com.school.scheduling.dto.request.TeacherRequest;
import com.school.scheduling.dto.request.TeacherUpdateRequest;
import com.school.scheduling.exception.ResourceNotFoundException;
import com.school.scheduling.exception.ResourceAlreadyExistsException;
import com.school.scheduling.exception.BadRequestException;
import com.school.scheduling.mapper.TeacherMapper;
import com.school.scheduling.validation.TeacherValidator;
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
@RequestMapping("/api/v1/teachers")
@Tag(name = "Teachers", description = "Teacher management endpoints")
public class TeacherController {

    private final TeacherMapper teacherMapper;
    private final TeacherValidator teacherValidator;

    public TeacherController(TeacherMapper teacherMapper, TeacherValidator teacherValidator) {
        this.teacherMapper = teacherMapper;
        this.teacherValidator = teacherValidator;
    }

    @GetMapping
    @Operation(summary = "Get all teachers", description = "Retrieve a paginated list of all teachers")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved teachers",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<PaginatedResponse<TeacherResponse>>> getAllTeachers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String direction,
            @Parameter(description = "Search term") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by department") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Filter by title") @RequestParam(required = false) String title) {

        try {
            PageRequest pageRequest = new PageRequest(page, size, sort, direction);
            List<TeacherResponse> teachers = teacherMapper.findAllWithFilters(pageRequest, search, departmentId, title)
                    .stream()
                    .map(TeacherResponse::fromEntity)
                    .toList();

            long total = teacherMapper.countWithFilters(search, departmentId, title);
            PaginatedResponse<TeacherResponse> response = PaginatedResponse.of(teachers, page, size, total);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve teachers", e);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get teacher by ID", description = "Retrieve a specific teacher by their ID")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved teacher",
                    content = @Content(schema = @Schema(implementation = TeacherResponse.class))),
            @SwaggerApiResponse(responseCode = "404", description = "Teacher not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherById(
            @Parameter(description = "Teacher ID") @PathVariable Long id) {

        try {
            com.school.scheduling.domain.Teacher teacher = teacherMapper.findById(id);
            if (teacher == null) {
                throw new ResourceNotFoundException("Teacher not found with id: " + id);
            }

            TeacherResponse response = TeacherResponse.fromEntity(teacher);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve teacher with id: " + id, e);
        }
    }

    @PostMapping
    @Operation(summary = "Create a new teacher", description = "Create a new teacher record")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "201", description = "Teacher created successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponse.class))),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid input"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(
            @Valid @RequestBody TeacherRequest teacherRequest) {

        try {
            ValidationResult validationResult = teacherValidator.validateForCreation(teacherRequest);
            if (!validationResult.isValid()) {
                throw new com.school.scheduling.validation.ValidationException("Teacher validation failed", validationResult);
            }

            // Check if teacher with employee ID already exists
            if (teacherMapper.findByEmployeeId(teacherRequest.getEmployeeId()) != null) {
                throw new ResourceAlreadyExistsException("Teacher", "employeeId", teacherRequest.getEmployeeId());
            }

            // Check if user is already assigned to another teacher
            if (teacherMapper.findByUserId(teacherRequest.getUserId()) != null) {
                throw new ResourceAlreadyExistsException("User", "userId", teacherRequest.getUserId());
            }

            com.school.scheduling.domain.Teacher teacher = new com.school.scheduling.domain.Teacher();
            teacher.setEmployeeId(teacherRequest.getEmployeeId());
            teacher.setDepartment(new com.school.scheduling.domain.Department(teacherRequest.getDepartmentId()));
            teacher.setTitle(teacherRequest.getTitle());
            teacher.setMaxWeeklyHours(teacherRequest.getMaxWeeklyHours());
            teacher.setMaxCoursesPerSemester(teacherRequest.getMaxCoursesPerSemester());
            teacher.setOfficeLocation(teacherRequest.getOfficeLocation());
            teacher.setPhone(teacherRequest.getPhone());

            // Set user
            com.school.scheduling.domain.User user = new com.school.scheduling.domain.User();
            user.setId(teacherRequest.getUserId());
            teacher.setUser(user);

            teacherMapper.insert(teacher);

            // Add specializations if provided
            if (teacherRequest.getSpecializations() != null) {
                teacherRequest.getSpecializations().forEach(specRequest -> {
                    com.school.scheduling.domain.TeacherSpecialization spec = new com.school.scheduling.domain.TeacherSpecialization();
                    spec.setTeacher(teacher);
                    spec.setSubjectCode(specRequest.getSubjectCode());
                    spec.setSubjectName(specRequest.getSubjectName());
                    spec.setProficiencyLevel(specRequest.getProficiencyLevel());
                    spec.setYearsOfExperience(specRequest.getYearsOfExperience());
                    spec.setCertified(specRequest.getCertified());
                    spec.setCertificationDetails(specRequest.getCertificationDetails());
                    teacher.addSpecialization(spec);
                });
            }

            TeacherResponse response = TeacherResponse.fromEntity(teacherMapper.findById(teacher.getId()));
            return new ResponseEntity<>(ApiResponse.created(response), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create teacher", e);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a teacher", description = "Update an existing teacher record")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Teacher updated successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponse.class))),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid input"),
            @SwaggerApiResponse(responseCode = "404", description = "Teacher not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(
            @Parameter(description = "Teacher ID") @PathVariable Long id,
            @Valid @RequestBody TeacherUpdateRequest teacherUpdateRequest) {

        try {
            com.school.scheduling.domain.Teacher existingTeacher = teacherMapper.findById(id);
            if (existingTeacher == null) {
                throw new ResourceNotFoundException("Teacher not found with id: " + id);
            }

            ValidationResult validationResult = teacherValidator.validateForUpdate(existingTeacher, teacherUpdateRequest);
            if (!validationResult.isValid()) {
                throw new com.school.scheduling.validation.ValidationException("Teacher validation failed", validationResult);
            }

            // Update fields if provided
            if (teacherUpdateRequest.getEmployeeId() != null) {
                existingTeacher.setEmployeeId(teacherUpdateRequest.getEmployeeId());
            }
            if (teacherUpdateRequest.getDepartmentId() != null) {
                existingTeacher.setDepartment(new com.school.scheduling.domain.Department(teacherUpdateRequest.getDepartmentId()));
            }
            if (teacherUpdateRequest.getTitle() != null) {
                existingTeacher.setTitle(teacherUpdateRequest.getTitle());
            }
            if (teacherUpdateRequest.getMaxWeeklyHours() != null) {
                existingTeacher.setMaxWeeklyHours(teacherUpdateRequest.getMaxWeeklyHours());
            }
            if (teacherUpdateRequest.getMaxCoursesPerSemester() != null) {
                existingTeacher.setMaxCoursesPerSemester(teacherUpdateRequest.getMaxCoursesPerSemester());
            }
            if (teacherUpdateRequest.getOfficeLocation() != null) {
                existingTeacher.setOfficeLocation(teacherUpdateRequest.getOfficeLocation());
            }
            if (teacherUpdateRequest.getPhone() != null) {
                existingTeacher.setPhone(teacherUpdateRequest.getPhone());
            }

            teacherMapper.update(existingTeacher);

            // Update specializations if provided
            if (teacherUpdateRequest.getSpecializations() != null) {
                // Remove existing specializations
                existingTeacher.getSpecializations().clear();

                // Add new specializations
                teacherUpdateRequest.getSpecializations().forEach(specRequest -> {
                    com.school.scheduling.domain.TeacherSpecialization spec = new com.school.scheduling.domain.TeacherSpecialization();
                    spec.setTeacher(existingTeacher);
                    spec.setSubjectCode(specRequest.getSubjectCode());
                    spec.setSubjectName(specRequest.getSubjectName());
                    spec.setProficiencyLevel(specRequest.getProficiencyLevel());
                    spec.setYearsOfExperience(specRequest.getYearsOfExperience());
                    spec.setCertified(specRequest.getCertified());
                    spec.setCertificationDetails(specRequest.getCertificationDetails());
                    existingTeacher.addSpecialization(spec);
                });
            }

            TeacherResponse response = TeacherResponse.fromEntity(teacherMapper.findById(id));
            return ResponseEntity.ok(ApiResponse.updated(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update teacher with id: " + id, e);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a teacher", description = "Delete a teacher record")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Teacher deleted successfully"),
            @SwaggerApiResponse(responseCode = "404", description = "Teacher not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(
            @Parameter(description = "Teacher ID") @PathVariable Long id) {

        try {
            com.school.scheduling.domain.Teacher teacher = teacherMapper.findById(id);
            if (teacher == null) {
                throw new ResourceNotFoundException("Teacher not found with id: " + id);
            }

            // Check if teacher has active schedules
            if (teacherMapper.hasActiveSchedules(id)) {
                throw new BadRequestException("Cannot delete teacher with active schedules");
            }

            teacherMapper.deleteById(id);
            return ResponseEntity.ok(ApiResponse.deleted("Teacher deleted successfully"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete teacher with id: " + id, e);
        }
    }

    @GetMapping("/by-employee/{employeeId}")
    @Operation(summary = "Get teacher by employee ID", description = "Retrieve a teacher by their employee ID")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved teacher",
                    content = @Content(schema = @Schema(implementation = TeacherResponse.class))),
            @SwaggerApiResponse(responseCode = "404", description = "Teacher not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherByEmployeeId(
            @Parameter(description = "Employee ID") @PathVariable String employeeId) {

        try {
            com.school.scheduling.domain.Teacher teacher = teacherMapper.findByEmployeeId(employeeId);
            if (teacher == null) {
                throw new ResourceNotFoundException("Teacher not found with employee id: " + employeeId);
            }

            TeacherResponse response = TeacherResponse.fromEntity(teacher);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve teacher with employee id: " + employeeId, e);
        }
    }

    @GetMapping("/by-department/{departmentId}")
    @Operation(summary = "Get teachers by department", description = "Retrieve all teachers in a specific department")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved teachers",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getTeachersByDepartment(
            @Parameter(description = "Department ID") @PathVariable Long departmentId) {

        try {
            List<TeacherResponse> teachers = teacherMapper.findByDepartmentId(departmentId)
                    .stream()
                    .map(TeacherResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(teachers));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve teachers for department: " + departmentId, e);
        }
    }

    @GetMapping("/specializations/{subjectCode}")
    @Operation(summary = "Get teachers by specialization", description = "Retrieve all teachers with a specific subject specialization")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved teachers",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getTeachersBySpecialization(
            @Parameter(description = "Subject code") @PathVariable String subjectCode) {

        try {
            List<TeacherResponse> teachers = teacherMapper.findBySpecialization(subjectCode)
                    .stream()
                    .map(TeacherResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(teachers));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve teachers for specialization: " + subjectCode, e);
        }
    }
}