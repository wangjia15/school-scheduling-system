package com.school.scheduling.mapper;

import com.school.scheduling.domain.ScheduleConflict;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ScheduleConflictMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO schedule_conflicts (conflict_type, severity, description, " +
            "schedule_id_1, schedule_id_2, entity_id, entity_type, detected_at, " +
            "resolution_status, resolution_notes, created_at, updated_at) " +
            "VALUES (#{conflictType}, #{severity}, #{description}, #{scheduleId1}, #{scheduleId2}, " +
            "#{entityId}, #{entityType}, #{detectedAt}, #{resolutionStatus}, #{resolutionNotes}, " +
            "#{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ScheduleConflict conflict);

    @Update("UPDATE schedule_conflicts SET conflict_type = #{conflictType}, severity = #{severity}, " +
            "description = #{description}, schedule_id_1 = #{scheduleId1}, schedule_id_2 = #{scheduleId2}, " +
            "entity_id = #{entityId}, entity_type = #{entityType}, resolution_status = #{resolutionStatus}, " +
            "resolution_notes = #{resolutionNotes}, resolved_at = #{resolvedAt}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(ScheduleConflict conflict);

    @Delete("UPDATE schedule_conflicts SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Select("SELECT * FROM schedule_conflicts WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "scheduleConflictResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "conflictType", column = "conflict_type", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "severity", column = "severity", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "description", column = "description"),
        @Result(property = "scheduleId1", column = "schedule_id_1"),
        @Result(property = "scheduleId2", column = "schedule_id_2"),
        @Result(property = "entityId", column = "entity_id"),
        @Result(property = "entityType", column = "entity_type", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "detectedAt", column = "detected_at"),
        @Result(property = "resolvedAt", column = "resolved_at"),
        @Result(property = "resolutionStatus", column = "resolution_status", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "resolutionNotes", column = "resolution_notes"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<ScheduleConflict> findById(@Param("id") Long id);

    @Select("SELECT * FROM schedule_conflicts WHERE conflict_type = #{conflictType} " +
            "AND deleted_at IS NULL ORDER BY detected_at DESC")
    @ResultMap("scheduleConflictResultMap")
    List<ScheduleConflict> findByConflictType(@Param("conflictType") String conflictType);

    @Select("SELECT * FROM schedule_conflicts WHERE severity = #{severity} " +
            "AND deleted_at IS NULL ORDER BY detected_at DESC")
    @ResultMap("scheduleConflictResultMap")
    List<ScheduleConflict> findBySeverity(@Param("severity") String severity);

    @Select("SELECT * FROM schedule_conflicts WHERE resolution_status = #{resolutionStatus} " +
            "AND deleted_at IS NULL ORDER BY detected_at DESC")
    @ResultMap("scheduleConflictResultMap")
    List<ScheduleConflict> findByResolutionStatus(@Param("resolutionStatus") String resolutionStatus);

    @Select("SELECT * FROM schedule_conflicts WHERE entity_id = #{entityId} " +
            "AND entity_type = #{entityType} AND deleted_at IS NULL " +
            "ORDER BY detected_at DESC")
    @ResultMap("scheduleConflictResultMap")
    List<ScheduleConflict> findByEntity(@Param("entityId") Long entityId, @Param("entityType") String entityType);

    @Select("SELECT * FROM schedule_conflicts WHERE schedule_id_1 = #{scheduleId} " +
            "OR schedule_id_2 = #{scheduleId} AND deleted_at IS NULL " +
            "ORDER BY detected_at DESC")
    @ResultMap("scheduleConflictResultMap")
    List<ScheduleConflict> findByScheduleId(@Param("scheduleId") Long scheduleId);

    @Select("SELECT * FROM schedule_conflicts WHERE detected_at >= #{startTime} " +
            "AND detected_at <= #{endTime} AND deleted_at IS NULL " +
            "ORDER BY detected_at DESC")
    @ResultMap("scheduleConflictResultMap")
    List<ScheduleConflict> findByDetectionTimeRange(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM schedule_conflicts WHERE resolved_at IS NOT NULL " +
            "AND deleted_at IS NULL ORDER BY resolved_at DESC")
    @ResultMap("scheduleConflictResultMap")
    List<ScheduleConflict> findResolvedConflicts();

    @Select("SELECT * FROM schedule_conflicts WHERE resolved_at IS NULL " +
            "AND resolution_status = 'PENDING' AND deleted_at IS NULL " +
            "ORDER BY detected_at DESC")
    @ResultMap("scheduleConflictResultMap")
    List<ScheduleConflict> findPendingConflicts();

    @Select("<script>" +
            "SELECT * FROM schedule_conflicts WHERE deleted_at IS NULL " +
            "<if test='conflictType != null'>AND conflict_type = #{conflictType}</if> " +
            "<if test='severity != null'>AND severity = #{severity}</if> " +
            "<if test='resolutionStatus != null'>AND resolution_status = #{resolutionStatus}</if> " +
            "<if test='entityType != null'>AND entity_type = #{entityType}</if> " +
            "<if test='entityId != null'>AND entity_id = #{entityId}</if> " +
            "<if test='scheduleId != null'>AND (schedule_id_1 = #{scheduleId} OR schedule_id_2 = #{scheduleId})</if> " +
            "<if test='isResolved != null'>" +
            "<choose>" +
            "<when test='isResolved'>AND resolved_at IS NOT NULL</when>" +
            "<otherwise>AND resolved_at IS NULL</otherwise>" +
            "</choose></if> " +
            "ORDER BY detected_at DESC" +
            "</script>")
    @ResultMap("scheduleConflictResultMap")
    List<ScheduleConflict> findByCriteria(@Param("conflictType") String conflictType,
                                         @Param("severity") String severity,
                                         @Param("resolutionStatus") String resolutionStatus,
                                         @Param("entityType") String entityType,
                                         @Param("entityId") Long entityId,
                                         @Param("scheduleId") Long scheduleId,
                                         @Param("isResolved") Boolean isResolved);

    @Select("SELECT COUNT(*) FROM schedule_conflicts WHERE conflict_type = #{conflictType} " +
            "AND deleted_at IS NULL")
    int countByConflictType(@Param("conflictType") String conflictType);

    @Select("SELECT COUNT(*) FROM schedule_conflicts WHERE severity = #{severity} " +
            "AND deleted_at IS NULL")
    int countBySeverity(@Param("severity") String severity);

    @Select("SELECT COUNT(*) FROM schedule_conflicts WHERE resolution_status = #{resolutionStatus} " +
            "AND deleted_at IS NULL")
    int countByResolutionStatus(@Param("resolutionStatus") String resolutionStatus);

    @Select("SELECT COUNT(*) FROM schedule_conflicts WHERE resolved_at IS NOT NULL " +
            "AND deleted_at IS NULL")
    int countResolvedConflicts();

    @Select("SELECT COUNT(*) FROM schedule_conflicts WHERE resolved_at IS NULL " +
            "AND deleted_at IS NULL")
    int countUnresolvedConflicts();

    @Select("SELECT conflict_type, COUNT(*) as count " +
            "FROM schedule_conflicts WHERE deleted_at IS NULL " +
            "GROUP BY conflict_type ORDER BY count DESC")
    List<ConflictTypeCount> getConflictTypeDistribution();

    @Select("SELECT severity, COUNT(*) as count " +
            "FROM schedule_conflicts WHERE deleted_at IS NULL " +
            "GROUP BY severity ORDER BY count DESC")
    List<SeverityCount> getSeverityDistribution();

    @Update("UPDATE schedule_conflicts SET resolution_status = #{resolutionStatus}, " +
            "resolved_at = #{resolvedAt}, resolution_notes = #{resolutionNotes}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateResolutionStatus(@Param("id") Long id,
                             @Param("resolutionStatus") String resolutionStatus,
                             @Param("resolvedAt") LocalDateTime resolvedAt,
                             @Param("resolutionNotes") String resolutionNotes,
                             @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE schedule_conflicts SET severity = #{severity}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateSeverity(@Param("id") Long id,
                      @Param("severity") String severity,
                      @Param("updatedAt") LocalDateTime updatedAt);

    @Select("SELECT AVG(TIMESTAMPDIFF(HOUR, detected_at, resolved_at)) " +
            "FROM schedule_conflicts WHERE resolved_at IS NOT NULL AND deleted_at IS NULL")
    Double getAverageResolutionTimeHours();

    // Result classes for distribution queries
    class ConflictTypeCount {
        private String conflictType;
        private Integer count;

        // Getters and setters
        public String getConflictType() { return conflictType; }
        public void setConflictType(String conflictType) { this.conflictType = conflictType; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    class SeverityCount {
        private String severity;
        private Integer count;

        // Getters and setters
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}