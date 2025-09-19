package com.school.scheduling.mapper;

import com.school.scheduling.domain.ScheduleConflict;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ConflictMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO schedule_conflicts (conflict_type, severity, description, " +
            "schedule_id_1, schedule_id_2, entity_id, entity_type, detected_at, " +
            "resolution_status, resolution_notes, created_at, updated_at) " +
            "VALUES (#{conflictType}, #{severity}, #{description}, #{scheduleId1}, #{scheduleId2}, " +
            "#{entityId}, #{entityType}, #{detectedAt}, #{resolutionStatus}, #{resolutionNotes}, " +
            "#{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ScheduleConflict conflict);

    @Update("UPDATE schedule_conflicts SET conflict_type = #{conflictType}, " +
            "severity = #{severity}, description = #{description}, schedule_id_1 = #{scheduleId1}, " +
            "schedule_id_2 = #{scheduleId2}, entity_id = #{entityId}, entity_type = #{entityType}, " +
            "resolution_status = #{resolutionStatus}, resolution_notes = #{resolutionNotes}, " +
            "resolved_at = #{resolvedAt}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(ScheduleConflict conflict);

    @Delete("UPDATE schedule_conflicts SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Delete("DELETE FROM schedule_conflicts WHERE id = #{id}")
    int hardDelete(@Param("id") Long id);

    @Select("SELECT * FROM schedule_conflicts WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "conflictResultMap", value = {
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

    @Select("SELECT * FROM schedule_conflicts WHERE deleted_at IS NULL ORDER BY detected_at DESC")
    @ResultMap("conflictResultMap")
    List<ScheduleConflict> findAll();

    @Select("SELECT COUNT(*) FROM schedule_conflicts WHERE deleted_at IS NULL")
    long countAll();

    // Conflict Type Queries
    @Select("SELECT * FROM schedule_conflicts " +
            "WHERE conflict_type = #{conflictType} AND deleted_at IS NULL " +
            "ORDER BY detected_at DESC")
    @ResultMap("conflictResultMap")
    List<ScheduleConflict> findByConflictType(@Param("conflictType") String conflictType);

    @Select("SELECT * FROM schedule_conflicts " +
            "WHERE severity = #{severity} AND deleted_at IS NULL " +
            "ORDER BY detected_at DESC")
    @ResultMap("conflictResultMap")
    List<ScheduleConflict> findBySeverity(@Param("severity") String severity);

    @Select("SELECT * FROM schedule_conflicts " +
            "WHERE resolution_status = #{resolutionStatus} AND deleted_at IS NULL " +
            "ORDER BY detected_at DESC")
    @ResultMap("conflictResultMap")
    List<ScheduleConflict> findByResolutionStatus(@Param("resolutionStatus") String resolutionStatus);

    // Entity-based Queries
    @Select("SELECT * FROM schedule_conflicts " +
            "WHERE entity_id = #{entityId} AND entity_type = #{entityType} " +
            "AND deleted_at IS NULL ORDER BY detected_at DESC")
    @ResultMap("conflictConflicts")
    List<ScheduleConflict> findByEntity(@Param("entityId") Long entityId,
                                       @Param("entityType") String entityType);

    @Select("SELECT * FROM schedule_conflicts " +
            "WHERE entity_type = #{entityType} AND deleted_at IS NULL " +
            "ORDER BY detected_at DESC")
    @ResultMap("conflictResultMap")
    List<ScheduleConflict> findByEntityType(@Param("entityType") String entityType);

    // Schedule-based Queries
    @Select("SELECT * FROM schedule_conflicts " +
            "WHERE (schedule_id_1 = #{scheduleId} OR schedule_id_2 = #{scheduleId}) " +
            "AND deleted_at IS NULL ORDER BY detected_at DESC")
    @ResultMap("conflictResultMap")
    List<ScheduleConflict> findByScheduleId(@Param("scheduleId") Long scheduleId);

    // Time-based Queries
    @Select("SELECT * FROM schedule_conflicts " +
            "WHERE detected_at BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted_at IS NULL ORDER BY detected_at DESC")
    @ResultMap("conflictResultMap")
    List<ScheduleConflict> findByDetectionDateRange(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Select("SELECT * FROM schedule_conflicts " +
            "WHERE detected_at >= #{startDate} AND deleted_at IS NULL " +
            "ORDER BY detected_at DESC")
    @ResultMap("conflictResultMap")
    List<ScheduleConflict> findRecentConflicts(@Param("startDate") LocalDateTime startDate);

    // Unresolved Conflicts
    @Select("SELECT * FROM schedule_conflicts " +
            "WHERE resolution_status = 'PENDING' AND deleted_at IS NULL " +
            "ORDER BY severity, detected_at DESC")
    @ResultMap("conflictResultMap")
    List<ScheduleConflict> findPendingConflicts();

    @Select("SELECT * FROM schedule_conflicts " +
            "WHERE resolution_status IN ('PENDING', 'DEFERRED') " +
            "AND severity = #{severity} AND deleted_at IS NULL " +
            "ORDER BY detected_at DESC")
    @ResultMap("conflictResultMap")
    List<ScheduleConflict> findPendingBySeverity(@Param("severity") String severity);

    // Resolution Management
    @Update("UPDATE schedule_conflicts SET resolution_status = #{resolutionStatus}, " +
            "resolution_notes = #{resolutionNotes}, resolved_at = #{resolvedAt}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateResolution(@Param("id") Long id,
                         @Param("resolutionStatus") String resolutionStatus,
                         @Param("resolutionNotes") String resolutionNotes,
                         @Param("resolvedAt") LocalDateTime resolvedAt,
                         @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE schedule_conflicts SET resolution_status = 'RESOLVED', " +
            "resolution_notes = CONCAT(COALESCE(resolution_notes, ''), '\\nResolved: ', #{resolutionNotes}), " +
            "resolved_at = #{resolvedAt}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int markAsResolved(@Param("id") Long id,
                       @Param("resolutionNotes") String resolutionNotes,
                       @Param("resolvedAt") LocalDateTime resolvedAt,
                       @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE schedule_conflicts SET resolution_status = 'IGNORED', " +
            "resolution_notes = CONCAT(COALESCE(resolution_notes, ''), '\\nIgnored: ', #{reason}), " +
            "resolved_at = #{resolvedAt}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int markAsIgnored(@Param("id") Long id,
                     @Param("reason") String reason,
                     @Param("resolvedAt") LocalDateTime resolvedAt,
                     @Param("updatedAt") LocalDateTime updatedAt);

    // Search Methods
    @Select("<script>" +
            "SELECT * FROM schedule_conflicts WHERE deleted_at IS NULL " +
            "<if test='searchText != null and !searchText.isEmpty()'>" +
            "AND description LIKE CONCAT('%', #{searchText}, '%') " +
            "</if>" +
            "<if test='conflictType != null'>AND conflict_type = #{conflictType}</if> " +
            "<if test='severity != null'>AND severity = #{severity}</if> " +
            "<if test='resolutionStatus != null'>AND resolution_status = #{resolutionStatus}</if> " +
            "<if test='entityType != null'>AND entity_type = #{entityType}</if> " +
            "<if test='startDate != null'>AND detected_at >= #{startDate}</if> " +
            "<if test='endDate != null'>AND detected_at <= #{endDate}</if> " +
            "ORDER BY " +
            "<choose>" +
            "    <when test='sortBy == \"severity\"'>severity DESC, detected_at DESC</when>" +
            "    <when test='sortBy == \"date\"'>detected_at DESC</when>" +
            "    <when test='sortBy == \"type\"'>conflict_type, detected_at DESC</when>" +
            "    <otherwise>detected_at DESC</otherwise>" +
            "</choose>" +
            "</script>")
    @ResultMap("conflictResultMap")
    List<ScheduleConflict> searchConflicts(@Param("searchText") String searchText,
                                          @Param("conflictType") String conflictType,
                                          @Param("severity") String severity,
                                          @Param("resolutionStatus") String resolutionStatus,
                                          @Param("entityType") String entityType,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("sortBy") String sortBy);

    // Conflict Statistics and Analytics
    @Select("SELECT conflict_type, COUNT(*) as conflict_count, " +
            "SUM(CASE WHEN resolution_status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved_count, " +
            "SUM(CASE WHEN resolution_status = 'PENDING' THEN 1 ELSE 0 END) as pending_count, " +
            "AVG(TIMESTAMPDIFF(HOUR, detected_at, COALESCE(resolved_at, NOW()))) as avg_resolution_hours " +
            "FROM schedule_conflicts " +
            "WHERE deleted_at IS NULL " +
            "GROUP BY conflict_type " +
            "ORDER BY conflict_type")
    List<ConflictTypeStats> getConflictStatsByType();

    @Select("SELECT severity, COUNT(*) as conflict_count, " +
            "SUM(CASE WHEN resolution_status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved_count " +
            "FROM schedule_conflicts " +
            "WHERE deleted_at IS NULL " +
            "GROUP BY severity " +
            "ORDER BY FIELD(severity, 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW')")
    List<SeverityStats> getConflictStatsBySeverity();

    @Select("SELECT resolution_status, COUNT(*) as conflict_count, " +
            "AVG(TIMESTAMPDIFF(HOUR, detected_at, COALESCE(resolved_at, NOW()))) as avg_resolution_time " +
            "FROM schedule_conflicts " +
            "WHERE deleted_at IS NULL " +
            "GROUP BY resolution_status " +
            "ORDER BY conflict_count DESC")
    List<ResolutionStats> getConflictStatsByResolutionStatus();

    @Select("SELECT entity_type, COUNT(*) as conflict_count, " +
            "SUM(CASE WHEN resolution_status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved_count " +
            "FROM schedule_conflicts " +
            "WHERE deleted_at IS NULL " +
            "GROUP BY entity_type " +
            "ORDER BY conflict_count DESC")
    List<EntityConflictStats> getConflictStatsByEntityType();

    // Advanced Conflict Detection Queries
    @Select("SELECT COUNT(*) FROM schedule_conflicts " +
            "WHERE resolution_status = 'PENDING' " +
            "AND severity = 'CRITICAL' " +
            "AND deleted_at IS NULL")
    int countCriticalPendingConflicts();

    @Select("SELECT COUNT(*) FROM schedule_conflicts " +
            "WHERE detected_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
            "AND deleted_at IS NULL")
    int countConflictsLast24Hours();

    @Select("SELECT COUNT(*) FROM schedule_conflicts " +
            "WHERE resolution_status = 'PENDING' " +
            "AND detected_at <= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "AND deleted_at IS NULL")
    int countOverdueConflicts();

    // Conflict Trend Analysis
    @Select("SELECT DATE(detected_at) as conflict_date, " +
            "COUNT(*) as conflict_count, " +
            "SUM(CASE WHEN resolution_status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved_count " +
            "FROM schedule_conflicts " +
            "WHERE detected_at BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted_at IS NULL " +
            "GROUP BY DATE(detected_at) " +
            "ORDER BY conflict_date")
    List<ConflictTrend> getConflictTrend(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    // Top Conflict Sources
    @Select("SELECT entity_id, entity_type, COUNT(*) as conflict_count, " +
            "MAX(detected_at) as last_conflict_date " +
            "FROM schedule_conflicts " +
            "WHERE deleted_at IS NULL " +
            "GROUP BY entity_id, entity_type " +
            "HAVING COUNT(*) >= #{minConflicts} " +
            "ORDER BY conflict_count DESC " +
            "LIMIT #{limit}")
    List<ConflictSource> getTopConflictSources(@Param("minConflicts") Integer minConflicts,
                                             @Param("limit") Integer limit);

    // Batch Operations
    @Insert("<script>" +
            "INSERT INTO schedule_conflicts (conflict_type, severity, description, " +
            "schedule_id_1, schedule_id_2, entity_id, entity_type, detected_at, " +
            "resolution_status, resolution_notes, created_at, updated_at) VALUES " +
            "<foreach collection='conflicts' item='conflict' separator=','>" +
            "(#{conflict.conflictType}, #{conflict.severity}, #{conflict.description}, " +
            "#{conflict.scheduleId1}, #{conflict.scheduleId2}, #{conflict.entityId}, " +
            "#{conflict.entityType}, #{conflict.detectedAt}, #{conflict.resolutionStatus}, " +
            "#{conflict.resolutionNotes}, #{conflict.createdAt}, #{conflict.updatedAt})" +
            "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("conflicts") List<ScheduleConflict> conflicts);

    @Update("<script>" +
            "<foreach collection='conflicts' item='conflict' separator=';'>" +
            "UPDATE schedule_conflicts SET conflict_type = #{conflict.conflictType}, " +
            "severity = #{conflict.severity}, description = #{conflict.description}, " +
            "schedule_id_1 = #{conflict.scheduleId1}, schedule_id_2 = #{conflict.scheduleId2}, " +
            "entity_id = #{conflict.entityId}, entity_type = #{conflict.entityType}, " +
            "resolution_status = #{conflict.resolutionStatus}, resolution_notes = #{conflict.resolutionNotes}, " +
            "resolved_at = #{conflict.resolvedAt}, updated_at = #{conflict.updatedAt} " +
            "WHERE id = #{conflict.id}" +
            "</foreach>" +
            "</script>")
    int batchUpdate(@Param("conflicts") List<ScheduleConflict> conflicts);

    @Update("<script>" +
            "UPDATE schedule_conflicts SET resolution_status = #{resolutionStatus}, " +
            "resolution_notes = CONCAT(COALESCE(resolution_notes, ''), '\\nBatch updated: ', #{notes}), " +
            "resolved_at = #{resolvedAt}, updated_at = #{updatedAt} " +
            "WHERE id IN " +
            "<foreach collection='conflictIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateResolutionStatus(@Param("conflictIds") List<Long> conflictIds,
                                   @Param("resolutionStatus") String resolutionStatus,
                                   @Param("notes") String notes,
                                   @Param("resolvedAt") LocalDateTime resolvedAt,
                                   @Param("updatedAt") LocalDateTime updatedAt);

    // Auto-resolution Operations
    @Update("UPDATE schedule_conflicts SET resolution_status = 'RESOLVED', " +
            "resolution_notes = CONCAT(COALESCE(resolution_notes, ''), '\\nAuto-resolved: Schedule deleted'), " +
            "resolved_at = #{now}, updated_at = #{now} " +
            "WHERE resolution_status = 'PENDING' " +
            "AND (schedule_id_1 = #{scheduleId} OR schedule_id_2 = #{scheduleId}) " +
            "AND deleted_at IS NULL")
    int autoResolveConflictsForDeletedSchedule(@Param("scheduleId") Long scheduleId,
                                              @Param("now") LocalDateTime now);

    // Result classes for complex queries
    interface ConflictTypeStats {
        String getConflictType();
        Integer getConflictCount();
        Integer getResolvedCount();
        Integer getPendingCount();
        Double getAvgResolutionHours();
    }

    interface SeverityStats {
        String getSeverity();
        Integer getConflictCount();
        Integer getResolvedCount();
    }

    interface ResolutionStats {
        String getResolutionStatus();
        Integer getConflictCount();
        Double getAvgResolutionTime();
    }

    interface EntityConflictStats {
        String getEntityType();
        Integer getConflictCount();
        Integer getResolvedCount();
    }

    interface ConflictTrend {
        String getConflictDate();
        Integer getConflictCount();
        Integer getResolvedCount();
    }

    interface ConflictSource {
        Long getEntityId();
        String getEntityType();
        Integer getConflictCount();
        LocalDateTime getLastConflictDate();
    }
}