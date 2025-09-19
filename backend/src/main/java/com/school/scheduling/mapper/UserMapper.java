package com.school.scheduling.mapper;

import com.school.scheduling.domain.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO users (username, email, password_hash, first_name, last_name, " +
            "role, is_active, last_login_at, created_at, updated_at) " +
            "VALUES (#{username}, #{email}, #{passwordHash}, #{firstName}, #{lastName}, " +
            "#{role}, #{isActive}, #{lastLoginAt}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET username = #{username}, email = #{email}, " +
            "password_hash = #{passwordHash}, first_name = #{firstName}, last_name = #{lastName}, " +
            "role = #{role}, is_active = #{isActive}, last_login_at = #{lastLoginAt}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(User user);

    @Delete("UPDATE users SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Select("SELECT * FROM users WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "userResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "email", column = "email"),
        @Result(property = "passwordHash", column = "password_hash"),
        @Result(property = "firstName", column = "first_name"),
        @Result(property = "lastName", column = "last_name"),
        @Result(property = "role", column = "role", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "lastLoginAt", column = "last_login_at"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<User> findById(@Param("id") Long id);

    @Select("SELECT * FROM users WHERE username = #{username} AND deleted_at IS NULL")
    @ResultMap("userResultMap")
    Optional<User> findByUsername(@Param("username") String username);

    @Select("SELECT * FROM users WHERE email = #{email} AND deleted_at IS NULL")
    @ResultMap("userResultMap")
    Optional<User> findByEmail(@Param("email") String email);

    @Select("SELECT * FROM users WHERE role = #{role} AND deleted_at IS NULL ORDER BY last_name, first_name")
    @ResultMap("userResultMap")
    List<User> findByRole(@Param("role") String role);

    @Select("SELECT * FROM users WHERE is_active = true AND deleted_at IS NULL ORDER BY last_name, first_name")
    @ResultMap("userResultMap")
    List<User> findActiveUsers();

    @Update("UPDATE users SET last_login_at = #{lastLoginAt} WHERE id = #{id}")
    int updateLastLogin(@Param("id") Long id, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    @Update("UPDATE users SET is_active = #{isActive}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive, @Param("updatedAt") LocalDateTime updatedAt);

    @Select("SELECT COUNT(*) FROM users WHERE role = #{role} AND is_active = true AND deleted_at IS NULL")
    int countActiveByRole(@Param("role") String role);

    @Select("<script>" +
            "SELECT * FROM users WHERE deleted_at IS NULL " +
            "<if test='role != null'>AND role = #{role}</if> " +
            "<if test='isActive != null'>AND is_active = #{isActive}</if> " +
            "ORDER BY last_name, first_name" +
            "</script>")
    @ResultMap("userResultMap")
    List<User> findByCriteria(@Param("role") String role, @Param("isActive") Boolean isActive);
}