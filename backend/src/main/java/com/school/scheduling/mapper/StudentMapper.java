package com.school.scheduling.mapper;

import com.school.scheduling.domain.Student;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface StudentMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO students (user_id, student_id, department_id, enrollment_year, " +
            "graduation_year, current_semester, gpa, status, created_at, updated_at) " +
            "VALUES (#{userId}, #{studentId}, #{departmentId}, #{enrollmentYear}, " +
            "#{graduationYear}, #{currentSemester}, #{gpa}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Student student);

    @Update("UPDATE students SET user_id = #{userId}, student_id = #{studentId}, " +
            "department_id = #{departmentId}, enrollment_year = #{enrollmentYear}, " +
            "graduation_year = #{graduationYear}, current_semester = #{currentSemester}, " +
            "gpa = #{gpa}, status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Student student);

    @Delete("UPDATE students SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Select("SELECT * FROM students WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "studentResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "departmentId", column = "department_id"),
        @Result(property = "enrollmentYear", column = "enrollment_year"),
        @Result(property = "graduationYear", column = "graduation_year"),
        @Result(property = "currentSemester", column = "current_semester"),
        @Result(property = "gpa", column = "gpa"),
        @Result(property = "status", column = "status", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<Student> findById(@Param("id") Long id);

    @Select("SELECT * FROM students WHERE student_id = #{studentId} AND deleted_at IS NULL")
    @ResultMap("studentResultMap")
    Optional<Student> findByStudentId(@Param("studentId") String studentId);

    @Select("SELECT * FROM students WHERE user_id = #{userId} AND deleted_at IS NULL")
    @ResultMap("studentResultMap")
    Optional<Student> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM students WHERE department_id = #{departmentId} AND deleted_at IS NULL ORDER BY student_id")
    @ResultMap("studentResultMap")
    List<Student> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Select("SELECT * FROM students WHERE status = #{status} AND deleted_at IS NULL ORDER BY student_id")
    @ResultMap("studentResultMap")
    List<Student> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM students WHERE enrollment_year = #{enrollmentYear} AND deleted_at IS NULL ORDER BY student_id")
    @ResultMap("studentResultMap")
    List<Student> findByEnrollmentYear(@Param("enrollmentYear") Integer enrollmentYear);

    @Select("SELECT * FROM students WHERE status = 'ACTIVE' AND deleted_at IS NULL ORDER BY student_id")
    @ResultMap("studentResultMap")
    List<Student> findActiveStudents();

    @Select("<script>" +
            "SELECT * FROM students WHERE deleted_at IS NULL " +
            "<if test='departmentId != null'>AND department_id = #{departmentId}</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "<if test='enrollmentYear != null'>AND enrollment_year = #{enrollmentYear}</if> " +
            "<if test='currentSemester != null'>AND current_semester = #{currentSemester}</if> " +
            "<if test='minGpa != null'>AND gpa >= #{minGpa}</if> " +
            "ORDER BY student_id" +
            "</script>")
    @ResultMap("studentResultMap")
    List<Student> findByCriteria(@Param("departmentId") Long departmentId,
                                 @Param("status") String status,
                                 @Param("enrollmentYear") Integer enrollmentYear,
                                 @Param("currentSemester") Integer currentSemester,
                                 @Param("minGpa") Double minGpa);

    @Update("UPDATE students SET gpa = #{gpa}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateGpa(@Param("id") Long id, @Param("gpa") Double gpa, @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE students SET status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE students SET current_semester = #{currentSemester}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateCurrentSemester(@Param("id") Long id, @Param("currentSemester") Integer currentSemester, @Param("updatedAt") LocalDateTime updatedAt);
}