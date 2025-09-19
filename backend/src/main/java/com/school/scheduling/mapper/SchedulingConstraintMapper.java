package com.school.scheduling.mapper;

import com.school.scheduling.domain.SchedulingConstraint;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface SchedulingConstraintMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO scheduling_constraints (name, description, constraint_type, " +
            "entity_id, entity_type, constraint_data, is_active, priority, " +
            "created_at, updated_at) " +
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

    @Select("SELECT * FROM scheduling_constraints WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "schedulingConstraintResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "constraintType", column = "constraint_type", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "entityId", column = "entity_id"),
        @Result(property = "entityType", column = "entity_type", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "constraintData", column = "constraint_data"),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "priority", column = "priority"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<SchedulingConstraint> findById(@Param("id") Long id);

    @Select("SELECT * FROM scheduling_constraints WHERE name = #{name} AND deleted_at IS NULL")
    @ResultMap("schedulingConstraintResultMap")
    Optional<SchedulingConstraint> findByName(@Param("name") String name);

    @Select("SELECT * FROM scheduling_constraints WHERE constraint_type = #{constraintType} " +
            "AND deleted_at IS NULL ORDER BY priority, name")
    @ResultMap("schedulingConstraintResultMap")
    List<SchedulingConstraint> findByConstraintType(@Param("constraintType") String constraintType);

    @Select("SELECT * FROM scheduling_constraints WHERE entity_id = #{entityId} " +
            "AND entity_type = #{entityType} AND deleted_at IS NULL " +
            "ORDER BY priority, name")
    @ResultMap("schedulingConstraintResultMap")
    List<SchedulingConstraint> findByEntity(@Param("entityId") Long entityId, @Param("entityType") String entityType);

    @Select("SELECT * FROM scheduling_constraints WHERE is_active = true AND deleted_at IS NULL " +
            "ORDER BY priority, name")
    @ResultMap("schedulingConstraintResultMap")
    List<SchedulingConstraint> findActiveConstraints();

    @Select("SELECT * FROM scheduling_constraints WHERE priority = #{priority} " +
            "AND deleted_at IS NULL ORDER BY name")
    @ResultMap("schedulingConstraintResultMap")
    List<SchedulingConstraint> findByPriority(@Param("priority") Integer priority);

    @Select("<script>" +
            "SELECT * FROM scheduling_constraints WHERE deleted_at IS NULL " +
            "<if test='constraintType != null'>AND constraint_type = #{constraintType}</if> " +
            "<if test='entityType != null'>AND entity_type = #{entityType}</if> " +
            "<if test='entityId != null'>AND entity_id = #{entityId}</if> " +
            "<if test='isActive != null'>AND is_active = #{isActive}</if> " +
            "<if test='priority != null'>AND priority = #{priority}</if> " +
            "<if test='nameSearch != null and !nameSearch.isEmpty()'>" +
            "AND (name LIKE CONCAT('%', #{nameSearch}, '%') OR description LIKE CONCAT('%', #{nameSearch}, '%'))" +
            "</if> " +
            "ORDER BY priority, name" +
            "</script>")
    @ResultMap("schedulingConstraintResultMap")
    List<SchedulingConstraint> findByCriteria(@Param("constraintType") String constraintType,
                                            @Param("entityType") String entityType,
                                            @Param("entityId") Long entityId,
                                            @Param("isActive") Boolean isActive,
                                            @Param("priority") Integer priority,
                                            @Param("nameSearch") String nameSearch);

    @Select("SELECT COUNT(*) FROM scheduling_constraints WHERE constraint_type = #{constraintType} " +
            "AND deleted_at IS NULL")
    int countByConstraintType(@Param("constraintType") String constraintType);

    @Select("SELECT COUNT(*) FROM scheduling_constraints WHERE entity_id = #{entityId} " +
            "AND entity_type = #{entityType} AND deleted_at IS NULL")
    int countByEntity(@Param("entityId") Long entityId, @Param("entityType") String entityType);

    @Select("SELECT COUNT(*) FROM scheduling_constraints WHERE is_active = true AND deleted_at IS NULL")
    int countActiveConstraints();

    @Select("SELECT constraint_type, COUNT(*) as count " +
            "FROM scheduling_constraints WHERE deleted_at IS NULL " +
            "GROUP BY constraint_type ORDER BY count DESC")
    List<TypeCount> getConstraintTypeDistribution();

    @Select("SELECT entity_type, COUNT(*) as count " +
            "FROM scheduling_constraints WHERE deleted_at IS NULL " +
            "GROUP BY entity_type ORDER BY count DESC")
    List<EntityCount> getEntityTypeDistribution();

    @Update("UPDATE scheduling_constraints SET is_active = #{isActive}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateActiveStatus(@Param("id") Long id,
                          @Param("isActive") Boolean isActive,
                          @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE scheduling_constraints SET priority = #{priority}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updatePriority(@Param("id") Long id,
                      @Param("priority") Integer priority,
                      @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE scheduling_constraints SET constraint_data = #{constraintData}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateConstraintData(@Param("id") Long id,
                             @Param("constraintData") String constraintData,
                             @Param("updatedAt") LocalDateTime updatedAt);

    // Result classes for distribution queries
    class TypeCount {
        private String constraintType;
        private Integer count;

        // Getters and setters
        public String getConstraintType() { return constraintType; }
        public void setConstraintType(String constraintType) { this.constraintType = constraintType; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    class EntityCount {
        private String entityType;
        private Integer count;

        // Getters and setters
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}