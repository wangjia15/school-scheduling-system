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
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    @NotBlank(message = "Table name is required")
    @Size(max = 50, message = "Table name must be less than 50 characters")
    @Column(name = "table_name", nullable = false, length = 50)
    private String tableName;

    @NotNull(message = "Record ID is required")
    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @NotNull(message = "Action is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 10)
    private AuditAction action;

    @Column(name = "old_values", columnDefinition = "JSON")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "JSON")
    private String newValues;

    @NotNull(message = "User is required")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "action_timestamp", nullable = false)
    private LocalDateTime actionTimestamp = LocalDateTime.now();

    @Size(max = 45, message = "IP address must be less than 45 characters")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Size(max = 500, message = "User agent must be less than 500 characters")
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    public enum AuditAction {
        INSERT, UPDATE, DELETE
    }

    public boolean isInsertAction() {
        return action == AuditAction.INSERT;
    }

    public boolean isUpdateAction() {
        return action == AuditAction.UPDATE;
    }

    public boolean isDeleteAction() {
        return action == AuditAction.DELETE;
    }

    public boolean hasOldData() {
        return oldValues != null && !oldValues.trim().isEmpty();
    }

    public boolean hasNewData() {
        return newValues != null && !newValues.trim().isEmpty();
    }

    public boolean hasFullData() {
        return hasOldData() && hasNewData();
    }

    public boolean isPartialData() {
        return hasOldData() ^ hasNewData();
    }

    public boolean isRecent() {
        if (actionTimestamp == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return !actionTimestamp.isBefore(now.minusHours(24));
    }

    public boolean isOld() {
        if (actionTimestamp == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return actionTimestamp.isBefore(now.minusDays(30));
    }

    public long getHoursSinceAction() {
        if (actionTimestamp == null) return 0;
        return java.time.temporal.ChronoUnit.HOURS.between(actionTimestamp, LocalDateTime.now());
    }

    public long getDaysSinceAction() {
        if (actionTimestamp == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(actionTimestamp.toLocalDate(), LocalDateTime.now().toLocalDate());
    }

    public String getUserDisplayName() {
        return user != null ? user.getFullName() : "Unknown User";
    }

    public String getUserEmail() {
        return user != null ? user.getEmail() : "Unknown Email";
    }

    public String getUserRole() {
        return user != null ? user.getRole().toString() : "Unknown Role";
    }

    public String getActionDisplay() {
        return action.toString().charAt(0) + action.toString().substring(1).toLowerCase();
    }

    public String getTimestampDisplay() {
        if (actionTimestamp == null) return "Unknown";
        return actionTimestamp.toString();
    }

    public String getTableDisplay() {
        return tableName != null ? tableName.replace('_', ' ') : "Unknown Table";
    }

    public String getAuditSummary() {
        return String.format("%s on %s (ID: %d) by %s at %s",
                getActionDisplay(),
                getTableDisplay(),
                recordId,
                getUserDisplayName(),
                getTimestampDisplay());
    }

    public boolean isValidAuditLog() {
        return tableName != null && !tableName.trim().isEmpty() &&
               recordId != null &&
               action != null &&
               user != null &&
               actionTimestamp != null;
    }

    public boolean isSystemAction() {
        return user != null && user.isAdmin();
    }

    public boolean isUserAction() {
        return !isSystemAction();
    }

    public boolean isSensitiveAction() {
        return "users".equals(tableName) ||
               "teachers".equals(tableName) ||
               "students".equals(tableName) ||
               "departments".equals(tableName);
    }

    public boolean isSchedulingAction() {
        return "schedules".equals(tableName) ||
               "course_offerings".equals(tableName) ||
               "enrollments".equals(tableName);
    }

    public boolean isConfigurationAction() {
        return "classrooms".equals(tableName) ||
               "courses".equals(tableName) ||
               "time_slots".equals(tableName) ||
               "semesters".equals(tableName);
    }

    public boolean hasIPAddress() {
        return ipAddress != null && !ipAddress.trim().isEmpty();
    }

    public boolean hasUserAgent() {
        return userAgent != null && !userAgent.trim().isEmpty();
    }

    public boolean isFromMobileDevice() {
        if (!hasUserAgent()) return false;
        return userAgent.toLowerCase().contains("mobile") ||
               userAgent.toLowerCase().contains("android") ||
               userAgent.toLowerCase().contains("iphone");
    }

    public boolean isFromWebBrowser() {
        if (!hasUserAgent()) return false;
        return userAgent.toLowerCase().contains("mozilla") ||
               userAgent.toLowerCase().contains("chrome") ||
               userAgent.toLowerCase().contains("safari") ||
               userAgent.toLowerCase().contains("firefox");
    }

    public boolean isFromAPI() {
        if (!hasUserAgent()) return false;
        return userAgent.toLowerCase().contains("api") ||
               userAgent.toLowerCase().contains("curl") ||
               userAgent.toLowerCase().contains("postman");
    }

    public String getDeviceType() {
        if (isFromMobileDevice()) return "MOBILE";
        if (isFromAPI()) return "API";
        if (isFromWebBrowser()) return "WEB";
        return "UNKNOWN";
    }

    public String getSourceLocation() {
        if (hasIPAddress()) {
            return ipAddress + " (" + getDeviceType() + ")";
        }
        return "Unknown Location";
    }

    public boolean isDataModificationAction() {
        return isUpdateAction() || isDeleteAction();
    }

    public boolean isDataCreationAction() {
        return isInsertAction();
    }

    public boolean wasDataChanged() {
        return isDataModificationAction() && hasFullData();
    }

    public boolean wasDataCreated() {
        return isDataCreationAction() && hasNewData();
    }

    public boolean wasDataDeleted() {
        return isDeleteAction() && hasOldData();
    }

    public String getDataChangeSummary() {
        if (wasDataCreated()) {
            return "Created new record with data: " + newValues;
        } else if (wasDataDeleted()) {
            return "Deleted record with data: " + oldValues;
        } else if (wasDataChanged()) {
            return "Modified record - Old: " + oldValues + ", New: " + newValues;
        } else {
            return "No data change recorded";
        }
    }

    public boolean isCompliantWithPrivacy() {
        // Check if sensitive data is properly masked in audit logs
        if ("users".equals(tableName)) {
            return oldValues == null || !oldValues.contains("password_hash") &&
                   newValues == null || !newValues.contains("password_hash");
        }
        return true;
    }

    public boolean requiresAdditionalAuthorization() {
        return isSensitiveAction() && !isSystemAction();
    }

    public boolean shouldTriggerAlert() {
        return isSensitiveAction() && !isSystemAction() && isRecent();
    }

    public String getRiskLevel() {
        if (isSensitiveAction() && !isSystemAction()) {
            return "HIGH";
        } else if (isSensitiveAction()) {
            return "MEDIUM";
        } else if (isSchedulingAction()) {
            return "LOW";
        } else {
            return "MINIMAL";
        }
    }

    public boolean isHighRiskAction() {
        return "HIGH".equals(getRiskLevel());
    }

    public boolean isMediumRiskAction() {
        return "MEDIUM".equals(getRiskLevel());
    }

    public boolean isLowRiskAction() {
        return "LOW".equals(getRiskLevel());
    }

    public String getDetailedAuditInfo() {
        return String.format("%s | %s | %s | %s | %s | Risk: %s",
                getAuditSummary(),
                getSourceLocation(),
                getActionDisplay(),
                getTableDisplay(),
                getUserRole(),
                getRiskLevel());
    }

    public boolean canBeArchived() {
        return isOld() && !isSensitiveAction();
    }

    public boolean shouldBeRetained() {
        return isSensitiveAction() || isSchedulingAction() || !isOld();
    }

    public String getRetentionPolicy() {
        if (isSensitiveAction()) {
            return "RETAIN_PERMANENTLY";
        } else if (isSchedulingAction()) {
            return "RETAIN_7_YEARS";
        } else {
            return "RETAIN_1_YEAR";
        }
    }
}