package com.school.scheduling.mapper;

import com.school.scheduling.domain.Department;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DepartmentMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO departments (name, code, description, head_id, created_at, updated_at) " +
            "VALUES (#{name}, #{code}, #{description}, #{headId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Department department);

    @Update("UPDATE departments SET name = #{name}, code = #{code}, description = #{description}, " +
            "head_id = #{headId}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Department department);

    @Delete("UPDATE departments SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Select("SELECT * FROM departments WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "departmentResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "code", column = "code"),
        @Result(property = "description", column = "description"),
        @Result(property = "headId", column = "head_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<Department> findById(@Param("id") Long id);

    @Select("SELECT * FROM departments WHERE code = #{code} AND deleted_at IS NULL")
    @ResultMap("departmentResultMap")
    Optional<Department> findByCode(@Param("code") String code);

    @Select("SELECT * FROM departments WHERE head_id = #{headId} AND deleted_at IS NULL")
    @ResultMap("departmentResultMap")
    Optional<Department> findByHeadId(@Param("headId") Long headId);

    @Select("SELECT * FROM departments WHERE deleted_at IS NULL ORDER BY name")
    @ResultMap("departmentResultMap")
    List<Department> findAll();

    @Select("<script>" +
            "SELECT * FROM departments WHERE deleted_at IS NULL " +
            "<if test='searchText != null and !searchText.isEmpty()'>" +
            "AND (name LIKE CONCAT('%', #{searchText}, '%') OR code LIKE CONCAT('%', #{searchText}, '%')) " +
            "</if>" +
            "ORDER BY name" +
            "</script>")
    @ResultMap("departmentResultMap")
    List<Department> searchDepartments(@Param("searchText") String searchText);

    @Update("UPDATE departments SET head_id = #{headId}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateDepartmentHead(@Param("id") Long id, @Param("headId") Long headId, @Param("updatedAt") LocalDateTime updatedAt);
}