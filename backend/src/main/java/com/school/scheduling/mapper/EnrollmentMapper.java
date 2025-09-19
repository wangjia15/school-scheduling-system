package com.school.scheduling.mapper;

import com.school.scheduling.domain.Enrollment;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface EnrollmentMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO enrollments (student_id, course_offering_id, enrollment_date, " +
            "status, grade, grade_letter, is_attending, created_at, updated_at) " +
            "VALUES (#{studentId}, #{courseOfferingId}, #{enrollmentDate}, #{status}, " +
            "#{grade}, #{gradeLetter}, #{isAttending}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Enrollment enrollment);

    @Update("UPDATE enrollments SET student_id = #{studentId}, course_offering_id = #{courseOfferingId}, " +
            "enrollment_date = #{enrollmentDate}, status = #{status}, grade = #{grade}, " +
            "grade_letter = #{gradeLetter}, is_attending = #{isAttending}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Enrollment enrollment);

    @Delete("UPDATE enrollments SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Select("SELECT * FROM enrollments WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "enrollmentResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "courseOfferingId", column = "course_offering_id"),
        @Result(property = "enrollmentDate", column = "enrollment_date"),
        @Result(property = "status", column = "status", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "grade", column = "grade"),
        @Result(property = "gradeLetter", column = "grade_letter"),
        @Result(property = "isAttending", column = "is_attending"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<Enrollment> findById(@Param("id") Long id);

    @Select("SELECT * FROM enrollments WHERE student_id = #{studentId} " +
            "AND course_offering_id = #{courseOfferingId} AND deleted_at IS NULL")
    @ResultMap("enrollmentResultMap")
    Optional<Enrollment> findByStudentAndCourseOffering(@Param("studentId") Long studentId,
                                                         @Param("courseOfferingId") Long courseOfferingId);

    @Select("SELECT * FROM enrollments WHERE student_id = #{studentId} AND deleted_at IS NULL " +
            "ORDER BY enrollment_date DESC")
    @ResultMap("enrollmentResultMap")
    List<Enrollment> findByStudentId(@Param("studentId") Long studentId);

    @Select("SELECT * FROM enrollments WHERE course_offering_id = #{courseOfferingId} " +
            "AND deleted_at IS NULL ORDER BY enrollment_date")
    @ResultMap("enrollmentResultMap")
    List<Enrollment> findByCourseOfferingId(@Param("courseOfferingId") Long courseOfferingId);

    @Select("SELECT * FROM enrollments WHERE status = #{status} AND deleted_at IS NULL " +
            "ORDER BY enrollment_date DESC")
    @ResultMap("enrollmentResultMap")
    List<Enrollment> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM enrollments WHERE is_attending = true AND deleted_at IS NULL " +
            "ORDER BY enrollment_date DESC")
    @ResultMap("enrollmentResultMap")
    List<Enrollment> findAttendingEnrollments();

    @Select("SELECT * FROM enrollments WHERE grade IS NOT NULL AND deleted_at IS NULL " +
            "ORDER BY grade DESC")
    @ResultMap("enrollmentResultMap")
    List<Enrollment> findGradedEnrollments();

    @Select("SELECT * FROM enrollments WHERE grade >= #{minimumGrade} AND deleted_at IS NULL " +
            "ORDER BY grade DESC")
    @ResultMap("enrollmentResultMap")
    List<Enrollment> findByMinimumGrade(@Param("minimumGrade") BigDecimal minimumGrade);

    @Select("SELECT * FROM enrollments WHERE enrollment_date >= #{startDate} " +
            "AND enrollment_date <= #{endDate} AND deleted_at IS NULL " +
            "ORDER BY enrollment_date")
    @ResultMap("enrollmentResultMap")
    List<Enrollment> findByEnrollmentDateRange(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Select("<script>" +
            "SELECT * FROM enrollments WHERE deleted_at IS NULL " +
            "<if test='studentId != null'>AND student_id = #{studentId}</if> " +
            "<if test='courseOfferingId != null'>AND course_offering_id = #{courseOfferingId}</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "<if test='isAttending != null'>AND is_attending = #{isAttending}</if> " +
            "<if test='hasGrade != null'>" +
            "<choose>" +
            "<when test='hasGrade'>AND grade IS NOT NULL</when>" +
            "<otherwise>AND grade IS NULL</otherwise>" +
            "</choose></if> " +
            "<if test='minGrade != null'>AND grade >= #{minGrade}</if> " +
            "<if test='maxGrade != null'>AND grade <= #{maxGrade}</if> " +
            "ORDER BY enrollment_date DESC" +
            "</script>")
    @ResultMap("enrollmentResultMap")
    List<Enrollment> findByCriteria(@Param("studentId") Long studentId,
                                   @Param("courseOfferingId") Long courseOfferingId,
                                   @Param("status") String status,
                                   @Param("isAttending") Boolean isAttending,
                                   @Param("hasGrade") Boolean hasGrade,
                                   @Param("minGrade") BigDecimal minGrade,
                                   @Param("maxGrade") BigDecimal maxGrade);

    @Select("SELECT COUNT(*) FROM enrollments WHERE student_id = #{studentId} AND deleted_at IS NULL")
    int countByStudentId(@Param("studentId") Long studentId);

    @Select("SELECT COUNT(*) FROM enrollments WHERE course_offering_id = #{courseOfferingId} " +
            "AND deleted_at IS NULL")
    int countByCourseOfferingId(@Param("courseOfferingId") Long courseOfferingId);

    @Select("SELECT COUNT(*) FROM enrollments WHERE status = #{status} AND deleted_at IS NULL")
    int countByStatus(@Param("status") String status);

    @Select("SELECT COUNT(*) FROM enrollments WHERE is_attending = true AND deleted_at IS NULL")
    int countAttendingEnrollments();

    @Select("SELECT COUNT(*) FROM enrollments WHERE grade >= #{passingGrade} AND deleted_at IS NULL")
    int countPassingGrades(@Param("passingGrade") BigDecimal passingGrade);

    @Update("UPDATE enrollments SET status = #{status}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id,
                   @Param("status") String status,
                   @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE enrollments SET grade = #{grade}, grade_letter = #{gradeLetter}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateGrade(@Param("id") Long id,
                   @Param("grade") BigDecimal grade,
                   @Param("gradeLetter") String gradeLetter,
                   @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE enrollments SET is_attending = #{isAttending}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateAttendanceStatus(@Param("id") Long id,
                             @Param("isAttending") Boolean isAttending,
                             @Param("updatedAt") LocalDateTime updatedAt);

    @Select("SELECT AVG(grade) FROM enrollments WHERE student_id = #{studentId} " +
            "AND grade IS NOT NULL AND deleted_at IS NULL")
    Double getAverageGradeByStudent(@Param("studentId") Long studentId);

    @Select("SELECT AVG(grade) FROM enrollments WHERE course_offering_id = #{courseOfferingId} " +
            "AND grade IS NOT NULL AND deleted_at IS NULL")
    Double getAverageGradeByCourseOffering(@Param("courseOfferingId") Long courseOfferingId);

    @Select("SELECT status, COUNT(*) as count FROM enrollments " +
            "WHERE deleted_at IS NULL GROUP BY status ORDER BY count DESC")
    List<StatusCount> getStatusDistribution();

    @Select("SELECT COUNT(*) FROM enrollments WHERE status = 'COMPLETED' " +
            "AND grade >= 60 AND deleted_at IS NULL")
    int countSuccessfulCompletions();

    @Select("SELECT COUNT(*) FROM enrollments WHERE status = 'COMPLETED' " +
            "AND grade < 60 AND deleted_at IS NULL")
    int countFailedCompletions();

    // Result classes for distribution queries
    class StatusCount {
        private String status;
        private Integer count;

        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}