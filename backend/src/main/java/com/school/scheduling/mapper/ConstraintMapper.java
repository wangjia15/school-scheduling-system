package com.school.scheduling.mapper;

import com.school.scheduling.domain.SchedulingConstraint;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ConstraintMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO scheduling_constraints (name, description, constraint_type, " +
            "entity_id, entity_type, constraint_data, is_active, priority, created_at, updated_at) " +
            "VALUES (#{name}, #{description}, #{constraintType}, #{entityId}, #{entityType}, " +
            "#{constraintData}, #{isActive}, #{priority}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SchedulingConstraint constraint);

    @Update("UPDATE scheduling_constraints SET name = #{name}, description = #{description}, " +
            "constraint_type = #{constraintType}, entity_id = #{entityId}, entity_type = #{entityType}, " +
            "constraint_data = #{constraintData}, is_active = #{isActive}, priority = #{priority}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(SchedulingConstraint constraint);

    @Delete("UPDATE scheduling_constraints SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Delete("DELETE FROM scheduling_constraints WHERE id = #{id}")
    int hardDelete(@Param("id") Long id);

    @Select("SELECT * FROM scheduling_constraints WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "constraintResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "constraintType", column = "constraint_type", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "entityId", column = "entity_id"),
        @Result(property = "entityType", column = "entity_type", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "constraintData", column = "constraint_data", typeHandler = org.apache.ibatis.type.JsonTypeHandler.class),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "priority", column = "priority"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<SchedulingConstraint> findById(@Param("id") Long id);

    @Select("SELECT * FROM scheduling_constraints WHERE deleted_at IS NULL ORDER BY priority, name")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findAll();

    @Select("SELECT COUNT(*) FROM scheduling_constraints WHERE deleted_at IS NULL")
    long countAll();

    // Constraint Type Queries
    @Select("SELECT * FROM scheduling_constraints " +
            "WHERE constraint_type = #{constraintType} AND deleted_at IS NULL " +
            "ORDER BY priority, name")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findByConstraintType(@Param("constraintType") String constraintType);

    @Select("SELECT * FROM scheduling_constraints " +
            "WHERE constraint_type = #{constraintType} AND entity_id = #{entityId} " +
            "AND entity_type = #{entityType} AND deleted_at IS NULL " +
            "ORDER BY priority, name")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findByTypeAndEntity(@Param("constraintType") String constraintType,
                                                  @Param("entityId") Long entityId,
                                                  @Param("entityType") String entityType);

    @Select("SELECT * FROM scheduling_constraints " +
            "WHERE entity_id = #{entityId} AND entity_type = #{entityType} " +
            "AND deleted_at IS NULL ORDER BY priority, name")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findByEntity(@Param("entityId") Long entityId,
                                          @Param("entityType") String entityType);

    // Active Constraints
    @Select("SELECT * FROM scheduling_constraints " +
            "WHERE is_active = true AND deleted_at IS NULL ORDER BY priority, name")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findActiveConstraints();

    @Select("SELECT * FROM scheduling_constraints " +
            "WHERE constraint_type = #{constraintType} AND is_active = true " +
            "AND deleted_at IS NULL ORDER BY priority, name")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findActiveByConstraintType(@Param("constraintType") String constraintType);

    @Select("SELECT * FROM scheduling_constraints " +
            "WHERE entity_id = #{entityId} AND entity_type = #{entityType} " +
            "AND is_active = true AND deleted_at IS NULL ORDER BY priority, name")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findActiveByEntity(@Param("entityId") Long entityId,
                                                @Param("entityType") String entityType);

    // Priority-based Queries
    @Select("SELECT * FROM scheduling_constraints " +
            "WHERE priority = #{priority} AND deleted_at IS NULL " +
            "ORDER BY name")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findByPriority(@Param("priority") Integer priority);

    @Select("SELECT * FROM scheduling_constraints " +
            "WHERE priority <= #{maxPriority} AND is_active = true " +
            "AND deleted_at IS NULL ORDER BY priority, name")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findByPriorityRange(@Param("maxPriority") Integer maxPriority);

    // Search Methods
    @Select("<script>" +
            "SELECT * FROM scheduling_constraints WHERE deleted_at IS NULL " +
            "<if test='searchText != null and !searchText.isEmpty()'>" +
            "AND (name LIKE CONCAT('%', #{searchText}, '%') " +
            "OR description LIKE CONCAT('%', #{searchText}, '%')) " +
            "</if>" +
            "<if test='constraintType != null'>AND constraint_type = #{constraintType}</if> " +
            "<if test='entityType != null'>AND entity_type = #{entityType}</if> " +
            "<if test='isActive != null'>AND is_active = #{isActive}</if> " +
            "<if test='priority != null'>AND priority = #{priority}</if> " +
            "ORDER BY priority, name" +
            "</script>")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> searchConstraints(@Param("searchText") String searchText,
                                                @Param("constraintType") String constraintType,
                                                @Param("entityType") String entityType,
                                                @Param("isActive") Boolean isActive,
                                                @Param("priority") Integer priority);

    // Advanced Constraint Validation Queries
    @Select("SELECT c.* FROM scheduling_constraints c " +
            "WHERE c.is_active = true " +
            "AND c.deleted_at IS NULL " +
            "AND (" +
            "    (c.constraint_type = 'TEACHER_AVAILABILITY' AND c.entity_id = #{teacherId}) " +
            "    OR (c.constraint_type = 'CLASSROOM_AVAILABILITY' AND c.entity_id = #{classroomId}) " +
            "    OR (c.constraint_type = 'CAPACITY_LIMIT' AND c.entity_type = 'CLASSROOM') " +
            ") " +
            "ORDER BY c.priority")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findRelevantConstraintsForScheduling(@Param("teacherId") Long teacherId,
                                                                  @Param("classroomId") Long classroomId);

    @Select("SELECT c.* FROM scheduling_constraints c " +
            "WHERE c.is_active = true " +
            "AND c.constraint_type = 'TEACHER_AVAILABILITY' " +
            "AND c.entity_id = #{teacherId} " +
            "AND JSON_CONTAINS(c.constraint_data, JSON_OBJECT('day_of_week', #{dayOfWeek})) " +
            "AND c.deleted_at IS NULL")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findTeacherAvailabilityConstraints(@Param("teacherId") Long teacherId,
                                                               @Param("dayOfWeek") String dayOfWeek);

    @Select("SELECT c.* FROM scheduling_constraints c " +
            "WHERE c.is_active = true " +
            "AND c.constraint_type = 'CAPACITY_LIMIT' " +
            "AND c.entity_type = 'COURSE_OFFERING' " +
            "AND c.entity_id = #{courseOfferingId} " +
            "AND c.deleted_at IS NULL")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findCapacityConstraintsForOffering(@Param("courseOfferingId") Long courseOfferingId);

    // Constraint Statistics and Analysis
    @Select("SELECT constraint_type, COUNT(*) as constraint_count, " +
            "SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) as active_count " +
            "FROM scheduling_constraints " +
            "WHERE deleted_at IS NULL " +
            "GROUP BY constraint_type " +
            "ORDER BY constraint_type")
    List<ConstraintTypeStats> getConstraintStatsByType();

    @Select("SELECT entity_type, COUNT(*) as constraint_count, " +
            "AVG(priority) as avg_priority " +
            "FROM scheduling_constraints " +
            "WHERE deleted_at IS NULL " +
            "GROUP BY entity_type " +
            "ORDER BY entity_type")
    List<EntityConstraintStats> getConstraintStatsByEntityType();

    @Select("SELECT priority, COUNT(*) as constraint_count " +
            "FROM scheduling_constraints " +
            "WHERE deleted_at IS NULL " +
            "GROUP BY priority " +
            "ORDER BY priority")
    List<PriorityStats> getConstraintStatsByPriority();

    // Complex JSON-based Queries
    @Select("SELECT c.* FROM scheduling_constraints c " +
            "WHERE c.is_active = true " +
            "AND c.constraint_type = 'TEACHER_AVAILABILITY' " +
            "AND JSON_CONTAINS(c.constraint_data, JSON_OBJECT('day_of_week', #{dayOfWeek})) " +
            "AND JSON_CONTAINS(c.constraint_data, JSON_OBJECT('start_time', #{startTime})) " +
            "AND c.deleted_at IS NULL")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findTeacherTimeConstraints(@Param("dayOfWeek") String dayOfWeek,
                                                        @Param("startTime") String startTime);

    @Select("SELECT c.* FROM scheduling_constraints c " +
            "WHERE c.is_active = true " +
            "AND c.constraint_type = 'CLASSROOM_AVAILABILITY' " +
            "AND JSON_CONTAINS(c.constraint_data, JSON_ARRAY(#{equipment})) " +
            "AND c.deleted_at IS NULL")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findClassroomEquipmentConstraints(@Param("equipment") String equipment);

    // Constraint Validation for Schedule Creation
    @Select("SELECT c.* FROM scheduling_constraints c " +
            "WHERE c.is_active = true " +
            "AND c.deleted_at IS NULL " +
            "AND (" +
            "    (c.constraint_type = 'TEACHER_AVAILABILITY' AND c.entity_id = #{teacherId}) " +
            "    OR (c.constraint_type = 'CLASSROOM_AVAILABILITY' AND c.entity_id = #{classroomId}) " +
            "    OR (c.constraint_type = 'DEPARTMENT_RESTRICTION' AND c.entity_type = 'DEPARTMENT') " +
            "    OR (c.constraint_type = 'CAPACITY_LIMIT') " +
            ") " +
            "ORDER BY c.priority")
    @ResultMap("constraintResultMap")
    List<SchedulingConstraint> findConstraintsToValidate(@Param("teacherId") Long teacherId,
                                                         @Param("classroomId") Long classroomId);

    // Batch Operations
    @Insert("<script>" +
            "INSERT INTO scheduling_constraints (name, description, constraint_type, " +
            "entity_id, entity_type, constraint_data, is_active, priority, created_at, updated_at) VALUES " +
            "<foreach collection='constraints' item='constraint' separator=','>" +
            "(#{constraint.name}, #{constraint.description}, #{constraint.constraintType}, " +
            "#{constraint.entityId}, #{constraint.entityType}, #{constraint.constraintData}, " +
            "#{constraint.isActive}, #{constraint.priority}, #{constraint.createdAt}, #{constraint.updatedAt})" +
            "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("constraints") List<SchedulingConstraint> constraints);

    @Update("<script>" +
            "<foreach collection='constraints' item='constraint' separator=';'>" +
            "UPDATE scheduling_constraints SET name = #{constraint.name}, " +
            "description = #{constraint.description}, constraint_type = #{constraint.constraintType}, " +
            "entity_id = #{constraint.entityId}, entity_type = #{constraint.entityType}, " +
            "constraint_data = #{constraint.constraintData}, is_active = #{constraint.isActive}, " +
            "priority = #{constraint.priority}, updated_at = #{constraint.updatedAt} " +
            "WHERE id = #{constraint.id}" +
            "</foreach>" +
            "</script>")
    int batchUpdate(@Param("constraints") List<SchedulingConstraint> constraints);

    @Update("<script>" +
            "UPDATE scheduling_constraints SET is_active = #{isActive}, " +
            "updated_at = #{updatedAt} WHERE id IN " +
            "<foreach collection='constraintIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateActivation(@Param("constraintIds") List<Long> constraintIds,
                             @Param("isActive") Boolean isActive,
                             @Param("updatedAt") LocalDateTime updatedAt);

    // Constraint Management Operations
    @Update("UPDATE scheduling_constraints SET priority = #{newPriority} " +
            "WHERE id = #{constraintId}")
    int updateConstraintPriority(@Param("constraintId") Long constraintId,
                                @Param("newPriority") Integer newPriority);

    @Select("SELECT COUNT(*) FROM scheduling_constraints " +
            "WHERE name = #{name} AND deleted_at IS NULL " +
            "<if test='excludeId != null'>AND id != #{excludeId}</if>")
    int countByName(@Param("name") String name,
                   @Param("excludeId") Long excludeId);

    // Result classes for complex queries
    interface ConstraintTypeStats {
        String getConstraintType();
        Integer getConstraintCount();
        Integer getActiveCount();
    }

    interface EntityConstraintStats {
        String getEntityType();
        Integer getConstraintCount();
        Double getAvgPriority();
    }

    interface PriorityStats {
        Integer getPriority();
        Integer getConstraintCount();
    }
}