package com.school.scheduling.mapper;

import com.school.scheduling.domain.CourseOffering;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface CourseOfferingMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO course_offerings (course_id, semester_id, section_number, teacher_id, " +
            "max_enrollment, current_enrollment, schedule_type, is_open, syllabus_url, " +
            "created_at, updated_at) " +
            "VALUES (#{courseId}, #{semesterId}, #{sectionNumber}, #{teacherId}, " +
            "#{maxEnrollment}, #{currentEnrollment}, #{scheduleType}, #{isOpen}, #{syllabusUrl}, " +
            "#{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CourseOffering courseOffering);

    @Update("UPDATE course_offerings SET course_id = #{courseId}, semester_id = #{semesterId}, " +
            "section_number = #{sectionNumber}, teacher_id = #{teacherId}, max_enrollment = #{maxEnrollment}, " +
            "current_enrollment = #{currentEnrollment}, schedule_type = #{scheduleType}, is_open = #{isOpen}, " +
            "syllabus_url = #{syllabusUrl}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(CourseOffering courseOffering);

    @Delete("UPDATE course_offerings SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Select("SELECT * FROM course_offerings WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "courseOfferingResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "courseId", column = "course_id"),
        @Result(property = "semesterId", column = "semester_id"),
        @Result(property = "sectionNumber", column = "section_number"),
        @Result(property = "teacherId", column = "teacher_id"),
        @Result(property = "maxEnrollment", column = "max_enrollment"),
        @Result(property = "currentEnrollment", column = "current_enrollment"),
        @Result(property = "scheduleType", column = "schedule_type", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "isOpen", column = "is_open"),
        @Result(property = "syllabusUrl", column = "syllabus_url"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<CourseOffering> findById(@Param("id") Long id);

    @Select("SELECT * FROM course_offerings WHERE course_id = #{courseId} AND semester_id = #{semesterId} " +
            "AND section_number = #{sectionNumber} AND deleted_at IS NULL")
    @ResultMap("courseOfferingResultMap")
    Optional<CourseOffering> findByCourseSemesterAndSection(@Param("courseId") Long courseId,
                                                            @Param("semesterId") Long semesterId,
                                                            @Param("sectionNumber") String sectionNumber);

    @Select("SELECT * FROM course_offerings WHERE course_id = #{courseId} AND deleted_at IS NULL ORDER BY semester_id DESC, section_number")
    @ResultMap("courseOfferingResultMap")
    List<CourseOffering> findByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT * FROM course_offerings WHERE semester_id = #{semesterId} AND deleted_at IS NULL ORDER BY course_id")
    @ResultMap("courseOfferingResultMap")
    List<CourseOffering> findBySemesterId(@Param("semesterId") Long semesterId);

    @Select("SELECT * FROM course_offerings WHERE teacher_id = #{teacherId} AND deleted_at IS NULL ORDER BY semester_id DESC, course_id")
    @ResultMap("courseOfferingResultMap")
    List<CourseOffering> findByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT * FROM course_offerings WHERE is_open = true AND deleted_at IS NULL ORDER BY semester_id DESC, course_id")
    @ResultMap("courseOfferingResultMap")
    List<CourseOffering> findOpenOfferings();

    @Select("SELECT * FROM course_offerings WHERE semester_id = #{semesterId} AND is_open = true " +
            "AND deleted_at IS NULL ORDER BY course_id")
    @ResultMap("courseOfferingResultMap")
    List<CourseOffering> findOpenOfferingsBySemester(@Param("semesterId") Long semesterId);

    @Select("SELECT * FROM course_offerings WHERE current_enrollment >= max_enrollment AND deleted_at IS NULL")
    @ResultMap("courseOfferingResultMap")
    List<CourseOffering> findFullOfferings();

    @Select("<script>" +
            "SELECT * FROM course_offerings WHERE deleted_at IS NULL " +
            "<if test='courseId != null'>AND course_id = #{courseId}</if> " +
            "<if test='semesterId != null'>AND semester_id = #{semesterId}</if> " +
            "<if test='teacherId != null'>AND teacher_id = #{teacherId}</if> " +
            "<if test='isOpen != null'>AND is_open = #{isOpen}</if> " +
            "<if test='scheduleType != null'>AND schedule_type = #{scheduleType}</if> " +
            "<if test='hasAvailableSeats != null and hasAvailableSeats'>AND current_enrollment < max_enrollment</if> " +
            "ORDER BY semester_id DESC, course_id" +
            "</script>")
    @ResultMap("courseOfferingResultMap")
    List<CourseOffering> findByCriteria(@Param("courseId") Long courseId,
                                       @Param("semesterId") Long semesterId,
                                       @Param("teacherId") Long teacherId,
                                       @Param("isOpen") Boolean isOpen,
                                       @Param("scheduleType") String scheduleType,
                                       @Param("hasAvailableSeats") Boolean hasAvailableSeats);

    @Update("UPDATE course_offerings SET current_enrollment = #{currentEnrollment}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateCurrentEnrollment(@Param("id") Long id, @Param("currentEnrollment") Integer currentEnrollment, @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE course_offerings SET is_open = #{isOpen}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateOpenStatus(@Param("id") Long id, @Param("isOpen") Boolean isOpen, @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE course_offerings SET teacher_id = #{teacherId}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateTeacher(@Param("id") Long id, @Param("teacherId") Long teacherId, @Param("updatedAt") LocalDateTime updatedAt);

    @Select("SELECT COUNT(*) FROM course_offerings WHERE semester_id = #{semesterId} AND teacher_id = #{teacherId} " +
            "AND deleted_at IS NULL")
    int countByTeacherAndSemester(@Param("teacherId") Long teacherId, @Param("semesterId") Long semesterId);

    @Select("SELECT SUM(current_enrollment) FROM course_offerings WHERE semester_id = #{semesterId} " +
            "AND deleted_at IS NULL")
    Integer getTotalEnrollmentBySemester(@Param("semesterId") Long semesterId);

    @Select("<script>" +
            "SELECT * FROM course_offerings WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_at IS NULL" +
            "</script>")
    @ResultMap("courseOfferingResultMap")
    List<CourseOffering> findByIds(@Param("ids") List<Long> ids);
}