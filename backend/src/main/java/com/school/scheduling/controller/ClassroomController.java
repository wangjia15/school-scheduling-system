package com.school.scheduling.controller;

import com.school.scheduling.dto.PageRequest;
import com.school.scheduling.dto.response.ApiResponse;
import com.school.scheduling.dto.response.PaginatedResponse;
import com.school.scheduling.dto.response.ClassroomResponse;
import com.school.scheduling.dto.request.ClassroomRequest;
import com.school.scheduling.exception.ResourceNotFoundException;
import com.school.scheduling.exception.ResourceAlreadyExistsException;
import com.school.scheduling.exception.BadRequestException;
import com.school.scheduling.mapper.ClassroomMapper;
import com.school.scheduling.validation.ClassroomValidator;
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
@RequestMapping("/api/v1/classrooms")
@Tag(name = "Classrooms", description = "Classroom management endpoints")
public class ClassroomController {

    private final ClassroomMapper classroomMapper;
    private final ClassroomValidator classroomValidator;

    public ClassroomController(ClassroomMapper classroomMapper, ClassroomValidator classroomValidator) {
        this.classroomMapper = classroomMapper;
        this.classroomValidator = classroomValidator;
    }

    @GetMapping
    @Operation(summary = "Get all classrooms", description = "Retrieve a paginated list of all classrooms")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved classrooms",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<PaginatedResponse<ClassroomResponse>>> getAllClassrooms(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String direction,
            @Parameter(description = "Search term") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by building") @RequestParam(required = false) String building,
            @Parameter(description = "Filter by room type") @RequestParam(required = false) String roomType,
            @Parameter(description = "Filter by minimum capacity") @RequestParam(required = false) Integer minCapacity,
            @Parameter(description = "Filter by availability") @RequestParam(required = false) Boolean available) {

        try {
            PageRequest pageRequest = new PageRequest(page, size, sort, direction);
            List<ClassroomResponse> classrooms = classroomMapper.findAllWithFilters(pageRequest, search, building, roomType, minCapacity, available)
                    .stream()
                    .map(ClassroomResponse::fromEntity)
                    .toList();

            long total = classroomMapper.countWithFilters(search, building, roomType, minCapacity, available);
            PaginatedResponse<ClassroomResponse> response = PaginatedResponse.of(classrooms, page, size, total);

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve classrooms", e);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get classroom by ID", description = "Retrieve a specific classroom by its ID")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved classroom",
                    content = @Content(schema = @Schema(implementation = ClassroomResponse.class))),
            @SwaggerApiResponse(responseCode = "404", description = "Classroom not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<ClassroomResponse>> getClassroomById(
            @Parameter(description = "Classroom ID") @PathVariable Long id) {

        try {
            com.school.scheduling.domain.Classroom classroom = classroomMapper.findById(id);
            if (classroom == null) {
                throw new ResourceNotFoundException("Classroom not found with id: " + id);
            }

            ClassroomResponse response = ClassroomResponse.fromEntity(classroom);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve classroom with id: " + id, e);
        }
    }

    @GetMapping("/by-building/{buildingCode}")
    @Operation(summary = "Get classrooms by building", description = "Retrieve all classrooms in a specific building")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved classrooms",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ClassroomResponse>>> getClassroomsByBuilding(
            @Parameter(description = "Building code") @PathVariable String buildingCode) {

        try {
            List<ClassroomResponse> classrooms = classroomMapper.findByBuildingCode(buildingCode)
                    .stream()
                    .map(ClassroomResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(classrooms));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve classrooms for building: " + buildingCode, e);
        }
    }

    @GetMapping("/by-capacity")
    @Operation(summary = "Get classrooms by capacity", description = "Retrieve classrooms that can accommodate at least the specified capacity")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved classrooms",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ClassroomResponse>>> getClassroomsByCapacity(
            @Parameter(description = "Minimum capacity required") @RequestParam Integer minCapacity) {

        try {
            List<ClassroomResponse> classrooms = classroomMapper.findByMinimumCapacity(minCapacity)
                    .stream()
                    .map(ClassroomResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(classrooms));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve classrooms with capacity >= " + minCapacity, e);
        }
    }

    @GetMapping("/by-type/{roomType}")
    @Operation(summary = "Get classrooms by type", description = "Retrieve all classrooms of a specific type")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved classrooms",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ClassroomResponse>>> getClassroomsByType(
            @Parameter(description = "Room type") @PathVariable String roomType) {

        try {
            com.school.scheduling.domain.Classroom.RoomType type = com.school.scheduling.domain.Classroom.RoomType.valueOf(roomType.toUpperCase());
            List<ClassroomResponse> classrooms = classroomMapper.findByRoomType(type)
                    .stream()
                    .map(ClassroomResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(classrooms));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid room type: " + roomType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve classrooms for type: " + roomType, e);
        }
    }

    @GetMapping("/available")
    @Operation(summary = "Get available classrooms", description = "Retrieve all currently available classrooms")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved available classrooms",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ClassroomResponse>>> getAvailableClassrooms() {

        try {
            List<ClassroomResponse> classrooms = classroomMapper.findAvailableClassrooms()
                    .stream()
                    .map(ClassroomResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(classrooms));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve available classrooms", e);
        }
    }

    @GetMapping("/lab-rooms")
    @Operation(summary = "Get laboratory rooms", description = "Retrieve all laboratory rooms")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved lab rooms",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ClassroomResponse>>> getLabRooms() {

        try {
            List<ClassroomResponse> classrooms = classroomMapper.findLaboratoryRooms()
                    .stream()
                    .map(ClassroomResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(classrooms));
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve laboratory rooms", e);
        }
    }

    @PostMapping
    @Operation(summary = "Create a new classroom", description = "Create a new classroom record")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "201", description = "Classroom created successfully",
                    content = @Content(schema = @Schema(implementation = ClassroomResponse.class))),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid input"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClassroomResponse>> createClassroom(
            @Valid @RequestBody ClassroomRequest classroomRequest) {

        try {
            ValidationResult validationResult = classroomValidator.validateForCreation(classroomRequest);
            if (!validationResult.isValid()) {
                throw new com.school.scheduling.validation.ValidationException("Classroom validation failed", validationResult);
            }

            // Check if classroom with same building and room number already exists
            if (classroomMapper.findByBuildingAndRoomNumber(classroomRequest.getBuildingCode(), classroomRequest.getRoomNumber()) != null) {
                throw new ResourceAlreadyExistsException("Classroom", "buildingCode-roomNumber",
                        classroomRequest.getBuildingCode() + "-" + classroomRequest.getRoomNumber());
            }

            com.school.scheduling.domain.Classroom classroom = new com.school.scheduling.domain.Classroom();
            classroom.setBuildingCode(classroomRequest.getBuildingCode());
            classroom.setRoomNumber(classroomRequest.getRoomNumber());
            classroom.setName(classroomRequest.getName());
            classroom.setCapacity(classroomRequest.getCapacity());
            classroom.setRoomType(classroomRequest.getRoomType());
            classroom.setHasProjector(classroomRequest.getHasProjector());
            classroom.setHasComputer(classroomRequest.getHasComputer());
            classroom.setHasWhiteboard(classroomRequest.getHasWhiteboard());
            classroom.setSpecialEquipment(classroomRequest.getSpecialEquipment());
            classroom.setIsAvailable(classroomRequest.getIsAvailable());

            classroomMapper.insert(classroom);

            ClassroomResponse response = ClassroomResponse.fromEntity(classroomMapper.findById(classroom.getId()));
            return new ResponseEntity<>(ApiResponse.created(response), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create classroom", e);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a classroom", description = "Update an existing classroom record")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Classroom updated successfully",
                    content = @Content(schema = @Schema(implementation = ClassroomResponse.class))),
            @SwaggerApiResponse(responseCode = "400", description = "Invalid input"),
            @SwaggerApiResponse(responseCode = "404", description = "Classroom not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClassroomResponse>> updateClassroom(
            @Parameter(description = "Classroom ID") @PathVariable Long id,
            @Valid @RequestBody ClassroomRequest classroomRequest) {

        try {
            com.school.scheduling.domain.Classroom existingClassroom = classroomMapper.findById(id);
            if (existingClassroom == null) {
                throw new ResourceNotFoundException("Classroom not found with id: " + id);
            }

            ValidationResult validationResult = classroomValidator.validateForUpdate(existingClassroom, classroomRequest);
            if (!validationResult.isValid()) {
                throw new com.school.scheduling.validation.ValidationException("Classroom validation failed", validationResult);
            }

            // Check if another classroom has the same building and room number
            com.school.scheduling.domain.Classroom duplicate = classroomMapper.findByBuildingAndRoomNumber(
                    classroomRequest.getBuildingCode(), classroomRequest.getRoomNumber());
            if (duplicate != null && !duplicate.getId().equals(id)) {
                throw new ResourceAlreadyExistsException("Classroom", "buildingCode-roomNumber",
                        classroomRequest.getBuildingCode() + "-" + classroomRequest.getRoomNumber());
            }

            // Update fields
            existingClassroom.setBuildingCode(classroomRequest.getBuildingCode());
            existingClassroom.setRoomNumber(classroomRequest.getRoomNumber());
            existingClassroom.setName(classroomRequest.getName());
            existingClassroom.setCapacity(classroomRequest.getCapacity());
            existingClassroom.setRoomType(classroomRequest.getRoomType());
            existingClassroom.setHasProjector(classroomRequest.getHasProjector());
            existingClassroom.setHasComputer(classroomRequest.getHasComputer());
            existingClassroom.setHasWhiteboard(classroomRequest.getHasWhiteboard());
            existingClassroom.setSpecialEquipment(classroomRequest.getSpecialEquipment());
            existingClassroom.setIsAvailable(classroomRequest.getIsAvailable());

            classroomMapper.update(existingClassroom);

            ClassroomResponse response = ClassroomResponse.fromEntity(classroomMapper.findById(id));
            return ResponseEntity.ok(ApiResponse.updated(response));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update classroom with id: " + id, e);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a classroom", description = "Delete a classroom record")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Classroom deleted successfully"),
            @SwaggerApiResponse(responseCode = "404", description = "Classroom not found"),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteClassroom(
            @Parameter(description = "Classroom ID") @PathVariable Long id) {

        try {
            com.school.scheduling.domain.Classroom classroom = classroomMapper.findById(id);
            if (classroom == null) {
                throw new ResourceNotFoundException("Classroom not found with id: " + id);
            }

            // Check if classroom has active schedules
            if (classroomMapper.hasActiveSchedules(id)) {
                throw new BadRequestException("Cannot delete classroom with active schedules");
            }

            classroomMapper.deleteById(id);
            return ResponseEntity.ok(ApiResponse.deleted("Classroom deleted successfully"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete classroom with id: " + id, e);
        }
    }

    @GetMapping("/suitable-for-course")
    @Operation(summary = "Get suitable classrooms for course", description = "Find classrooms suitable for a specific course")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Successfully retrieved suitable classrooms",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @SwaggerApiResponse(responseCode = "401", description = "Unauthorized"),
            @SwaggerApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ClassroomResponse>>> getSuitableClassroomsForCourse(
            @Parameter(description = "Course ID") @RequestParam Long courseId,
            @Parameter(description = "Required capacity") @RequestParam Integer requiredCapacity) {

        try {
            List<ClassroomResponse> classrooms = classroomMapper.findSuitableClassroomsForCourse(courseId, requiredCapacity)
                    .stream()
                    .map(ClassroomResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(classrooms));
        } catch (Exception e) {
            throw new RuntimeException("Failed to find suitable classrooms for course: " + courseId, e);
        }
    }
}