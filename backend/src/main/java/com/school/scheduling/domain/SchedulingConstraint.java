package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "scheduling_constraints")
public class SchedulingConstraint extends BaseEntity {

    @NotBlank(message = "Constraint name is required")
    @Size(max = 100, message = "Constraint name must be less than 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Constraint type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "constraint_type", nullable = false, length = 50)
    private ConstraintType constraintType;

    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 20)
    private EntityType entityType;

    @NotNull(message = "Constraint data is required")
    @Column(name = "constraint_data", nullable = false, columnDefinition = "JSON")
    private String constraintData;

    @NotNull(message = "Active status is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull(message = "Priority is required")
    @Column(name = "priority", nullable = false)
    private Integer priority = 1;

    public enum ConstraintType {
        TEACHER_AVAILABILITY, CLASSROOM_AVAILABILITY, STUDENT_CONFLICT,
        DEPARTMENT_RESTRICTION, CAPACITY_LIMIT, TIME_RESTRICTION, EQUIPMENT_REQUIREMENT
    }

    public enum EntityType {
        TEACHER, CLASSROOM, STUDENT, DEPARTMENT, COURSE
    }

    public boolean isHighPriority() {
        return priority != null && priority == 1;
    }

    public boolean isMediumPriority() {
        return priority != null && priority == 2;
    }

    public boolean isLowPriority() {
        return priority != null && priority == 3;
    }

    public boolean isActiveConstraint() {
        return isActive && isActive();
    }

    public boolean isTeacherConstraint() {
        return constraintType == ConstraintType.TEACHER_AVAILABILITY && entityType == EntityType.TEACHER;
    }

    public boolean isClassroomConstraint() {
        return constraintType == ConstraintType.CLASSROOM_AVAILABILITY && entityType == EntityType.CLASSROOM;
    }

    public boolean isStudentConstraint() {
        return constraintType == ConstraintType.STUDENT_CONFLICT && entityType == EntityType.STUDENT;
    }

    public boolean isDepartmentConstraint() {
        return constraintType == ConstraintType.DEPARTMENT_RESTRICTION && entityType == EntityType.DEPARTMENT;
    }

    public boolean isCapacityConstraint() {
        return constraintType == ConstraintType.CAPACITY_LIMIT;
    }

    public boolean isTimeConstraint() {
        return constraintType == ConstraintType.TIME_RESTRICTION;
    }

    public boolean isEquipmentConstraint() {
        return constraintType == ConstraintType.EQUIPMENT_REQUIREMENT;
    }

    public boolean appliesToEntity(Long entityId, EntityType type) {
        return this.entityId != null &&
               this.entityId.equals(entityId) &&
               this.entityType == type;
    }

    public boolean isMandatory() {
        return isHighPriority();
    }

    public boolean isOptional() {
        return !isMandatory();
    }

    public String getPriorityDisplay() {
        switch (priority) {
            case 1: return "HIGH";
            case 2: return "MEDIUM";
            case 3: return "LOW";
            default: return "UNKNOWN";
        }
    }

    public String getConstraintSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(name).append(" (").append(constraintType).append(")");

        if (entityType != null) {
            summary.append(" - ").append(entityType);
        }

        summary.append(" - Priority: ").append(getPriorityDisplay());

        return summary.toString();
    }

    public boolean isValidConstraint() {
        return name != null && !name.trim().isEmpty() &&
               constraintType != null &&
               constraintData != null && !constraintData.trim().isEmpty() &&
               priority != null && priority >= 1 && priority <= 3;
    }

    public boolean isApplicableToScheduling() {
        return isActiveConstraint() && isValidConstraint();
    }

    public String getValidationMessage() {
        if (!isValidConstraint()) {
            return "Invalid constraint: missing required fields";
        }
        return "Valid constraint";
    }

    public boolean hasEntityReference() {
        return entityId != null && entityType != null;
    }

    public boolean isGlobalConstraint() {
        return !hasEntityReference();
    }

    public boolean isSpecificConstraint() {
        return hasEntityReference();
    }
}