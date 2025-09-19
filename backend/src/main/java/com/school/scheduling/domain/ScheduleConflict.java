package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "schedule_conflicts")
public class ScheduleConflict extends BaseEntity {

    @NotNull(message = "Conflict type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "conflict_type", nullable = false, length = 50)
    private ConflictType conflictType;

    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private Severity severity = Severity.HIGH;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "schedule_id_1")
    private Schedule schedule1;

    @ManyToOne
    @JoinColumn(name = "schedule_id_2")
    private Schedule schedule2;

    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 30)
    private EntityType entityType;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @NotNull(message = "Resolution status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_status", nullable = false, length = 20)
    private ResolutionStatus resolutionStatus = ResolutionStatus.PENDING;

    @Size(max = 1000, message = "Resolution notes must be less than 1000 characters")
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    public enum ConflictType {
        TEACHER_DOUBLE_BOOKING, CLASSROOM_DOUBLE_BOOKING, STUDENT_SCHEDULE_CONFLICT,
        CAPACITY_EXCEEDED, PREREQUISITE_NOT_MET, EQUIPMENT_MISMATCH,
        TIME_SLOT_CONFLICT, DEPARTMENT_POLICY_VIOLATION, TEACHER_WORKLOAD_EXCEEDED
    }

    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }

    public enum ResolutionStatus {
        PENDING, RESOLVED, IGNORED, DEFERRED
    }

    public enum EntityType {
        TEACHER, CLASSROOM, STUDENT, COURSE_OFFERING, DEPARTMENT
    }

    public boolean isCritical() {
        return severity == Severity.CRITICAL;
    }

    public boolean isHighSeverity() {
        return severity == Severity.HIGH;
    }

    public boolean isMediumSeverity() {
        return severity == Severity.MEDIUM;
    }

    public boolean isLowSeverity() {
        return severity == Severity.LOW;
    }

    public boolean isPending() {
        return resolutionStatus == ResolutionStatus.PENDING;
    }

    public boolean isResolved() {
        return resolutionStatus == ResolutionStatus.RESOLVED;
    }

    public boolean isIgnored() {
        return resolutionStatus == ResolutionStatus.IGNORED;
    }

    public boolean isDeferred() {
        return resolutionStatus == ResolutionStatus.DEFERRED;
    }

    public boolean isTeacherConflict() {
        return conflictType == ConflictType.TEACHER_DOUBLE_BOOKING ||
               conflictType == ConflictType.TEACHER_WORKLOAD_EXCEEDED;
    }

    public boolean isClassroomConflict() {
        return conflictType == ConflictType.CLASSROOM_DOUBLE_BOOKING ||
               conflictType == ConflictType.CAPACITY_EXCEEDED;
    }

    public boolean isStudentConflict() {
        return conflictType == ConflictType.STUDENT_SCHEDULE_CONFLICT ||
               conflictType == ConflictType.PREREQUISITE_NOT_MET;
    }

    public boolean isEquipmentConflict() {
        return conflictType == ConflictType.EQUIPMENT_MISMATCH;
    }

    public boolean isTimeConflict() {
        return conflictType == ConflictType.TIME_SLOT_CONFLICT;
    }

    public boolean isPolicyConflict() {
        return conflictType == ConflictType.DEPARTMENT_POLICY_VIOLATION;
    }

    public boolean requiresImmediateAttention() {
        return isCritical() || isHighSeverity();
    }

    public boolean canBeIgnored() {
        return isLowSeverity() || isMediumSeverity();
    }

    public boolean canBeDeferred() {
        return !isCritical();
    }

    public boolean isRecentlyDetected() {
        if (detectedAt == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return !detectedAt.isBefore(now.minusHours(24));
    }

    public boolean isOldConflict() {
        if (detectedAt == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return detectedAt.isBefore(now.minusDays(7));
    }

    public boolean isOverdue() {
        if (detectedAt == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return detectedAt.isBefore(now.minusDays(3)) && isPending();
    }

    public long getHoursSinceDetection() {
        if (detectedAt == null) return 0;
        return java.time.temporal.ChronoUnit.HOURS.between(detectedAt, LocalDateTime.now());
    }

    public long getDaysSinceDetection() {
        if (detectedAt == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(detectedAt.toLocalDate(), LocalDateTime.now().toLocalDate());
    }

    public boolean hasResolution() {
        return isResolved() && resolutionNotes != null && !resolutionNotes.trim().isEmpty();
    }

    public String getSeverityDisplay() {
        return severity.toString().charAt(0) + severity.toString().substring(1).toLowerCase();
    }

    public String getStatusDisplay() {
        return resolutionStatus.toString().charAt(0) + resolutionStatus.toString().substring(1).toLowerCase();
    }

    public String getConflictSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(conflictType.toString().replace('_', ' '));
        summary.append(" (").append(getSeverityDisplay()).append(")");

        if (entityType != null) {
            summary.append(" - ").append(entityType.toString().replace('_', ' '));
        }

        summary.append(" - ").append(getStatusDisplay());

        return summary.toString();
    }

    public boolean isValidConflict() {
        return conflictType != null &&
               severity != null &&
               description != null && !description.trim().isEmpty() &&
               resolutionStatus != null &&
               detectedAt != null;
    }

    public boolean requiresManualResolution() {
        return isCritical() || isHighSeverity() || isTeacherConflict() || isClassroomConflict();
    }

    public boolean canBeAutoResolved() {
        return isLowSeverity() && !isTeacherConflict() && !isClassroomConflict();
    }

    public void markAsResolved(String resolutionNotes) {
        this.resolutionStatus = ResolutionStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = resolutionNotes;
    }

    public void markAsIgnored(String reason) {
        this.resolutionStatus = ResolutionStatus.IGNORED;
        this.resolutionNotes = reason;
    }

    public void markAsDeferred(String reason) {
        this.resolutionStatus = ResolutionStatus.DEFERRED;
        this.resolutionNotes = reason;
    }

    public void reopen() {
        this.resolutionStatus = ResolutionStatus.PENDING;
        this.resolvedAt = null;
        this.resolutionNotes = null;
    }

    public boolean isSameConflictAs(ScheduleConflict other) {
        if (other == null) return false;

        return this.conflictType == other.conflictType &&
               this.entityType == other.entityType &&
               ((this.entityId != null && this.entityId.equals(other.entityId)) ||
                (this.entityId == null && other.entityId == null)) &&
               ((this.schedule1 != null && this.schedule1.equals(other.schedule1)) ||
                (this.schedule1 == null && other.schedule1 == null)) &&
               ((this.schedule2 != null && this.schedule2.equals(other.schedule2)) ||
                (this.schedule2 == null && other.schedule2 == null));
    }

    public boolean isDuplicateOf(ScheduleConflict other) {
        if (other == null) return false;

        // Consider it a duplicate if it's the same type and affects the same entities
        // and was detected recently (within 1 hour)
        return this.isSameConflictAs(other) &&
               Math.abs(this.getHoursSinceDetection() - other.getHoursSinceDetection()) <= 1;
    }

    public String getDetailedDescription() {
        StringBuilder details = new StringBuilder();
        details.append(description);

        if (schedule1 != null) {
            details.append("\\nSchedule 1: ").append(schedule1.getScheduleSummary());
        }

        if (schedule2 != null) {
            details.append("\\nSchedule 2: ").append(schedule2.getScheduleSummary());
        }

        if (entityType != null) {
            details.append("\\nEntity Type: ").append(entityType);
        }

        if (entityId != null) {
            details.append("\\nEntity ID: ").append(entityId);
        }

        details.append("\\nDetected: ").append(detectedAt);

        if (resolvedAt != null) {
            details.append("\\nResolved: ").append(resolvedAt);
        }

        return details.toString();
    }

    public boolean isSystemGenerated() {
        return conflictType == ConflictType.TEACHER_DOUBLE_BOOKING ||
               conflictType == ConflictType.CLASSROOM_DOUBLE_BOOKING ||
               conflictType == ConflictType.TIME_SLOT_CONFLICT;
    }

    public boolean isUserReported() {
        return !isSystemGenerated();
    }

    public boolean affectsScheduling() {
        return !isResolved() && !isIgnored() && isActive();
    }

    public String getResolutionTime() {
        if (resolvedAt == null || detectedAt == null) return "Not resolved";

        long minutes = java.time.temporal.ChronoUnit.MINUTES.between(detectedAt, resolvedAt);
        if (minutes < 60) {
            return minutes + " minutes";
        } else {
            long hours = minutes / 60;
            return hours + " hours";
        }
    }

    public boolean wasResolvedQuickly() {
        if (!isResolved()) return false;
        return getHoursSinceDetection() <= 24;
    }

    public boolean tookLongToResolve() {
        if (!isResolved()) return false;
        return getHoursSinceDetection() > 72;
    }
}