package com.school.scheduling.mapper;

import com.school.scheduling.domain.TeacherAvailability;
import com.school.scheduling.domain.Teacher;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface TeacherAvailabilityMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO teacher_availability (teacher_id, day_of_week, start_time, end_time, " +
            "availability_type, max_classes, break_duration, requires_break_between_classes, " +
            "is_recurring, notes, created_at, updated_at) " +
            "VALUES (#{teacherId}, #{dayOfWeek}, #{startTime}, #{endTime}, #{availabilityType}, " +
            "#{maxClasses}, #{breakDuration}, #{requiresBreakBetweenClasses}, #{isRecurring}, " +
            "#{notes}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TeacherAvailability availability);

    @Update("UPDATE teacher_availability SET teacher_id = #{teacherId}, day_of_week = #{dayOfWeek}, " +
            "start_time = #{startTime}, end_time = #{endTime}, availability_type = #{availabilityType}, " +
            "max_classes = #{maxClasses}, break_duration = #{breakDuration}, " +
            "requires_break_between_classes = #{requiresBreakBetweenClasses}, is_recurring = #{isRecurring}, " +
            "notes = #{notes}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(TeacherAvailability availability);

    @Delete("UPDATE teacher_availability SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") java.time.LocalDateTime deletedAt);

    @Delete("DELETE FROM teacher_availability WHERE id = #{id}")
    int hardDelete(@Param("id") Long id);

    @Select("SELECT * FROM teacher_availability WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "teacherAvailabilityResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "teacherId", column = "teacher_id"),
        @Result(property = "dayOfWeek", column = "day_of_week", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "startTime", column = "start_time"),
        @Result(property = "endTime", column = "end_time"),
        @Result(property = "availabilityType", column = "availability_type", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "maxClasses", column = "max_classes"),
        @Result(property = "breakDuration", column = "break_duration"),
        @Result(property = "requiresBreakBetweenClasses", column = "requires_break_between_classes"),
        @Result(property = "isRecurring", column = "is_recurring"),
        @Result(property = "notes", column = "notes"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<TeacherAvailability> findById(@Param("id") Long id);

    @Select("SELECT * FROM teacher_availability WHERE teacher_id = #{teacherId} AND deleted_at IS NULL ORDER BY day_of_week, start_time")
    @ResultMap("teacherAvailabilityResultMap")
    List<TeacherAvailability> findByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT * FROM teacher_availability WHERE teacher_id = #{teacherId} AND day_of_week = #{dayOfWeek} AND deleted_at IS NULL ORDER BY start_time")
    @ResultMap("teacherAvailabilityResultMap")
    List<TeacherAvailability> findByTeacherAndDay(@Param("teacherId") Long teacherId, @Param("dayOfWeek") String dayOfWeek);

    @Select("SELECT * FROM teacher_availability WHERE deleted_at IS NULL ORDER BY teacher_id, day_of_week, start_time")
    @ResultMap("teacherAvailabilityResultMap")
    List<TeacherAvailability> findAll();

    @Select("SELECT COUNT(*) FROM teacher_availability WHERE teacher_id = #{teacherId} AND deleted_at IS NULL")
    long countByTeacherId(@Param("teacherId") Long teacherId);

    // Availability Checking Methods
    @Select("SELECT * FROM teacher_availability " +
            "WHERE teacher_id = #{teacherId} " +
            "AND day_of_week = #{dayOfWeek} " +
            "AND start_time <= #{time} AND end_time >= #{time} " +
            "AND availability_type IN ('PREFERRED', 'AVAILABLE') " +
            "AND deleted_at IS NULL")
    @ResultMap("teacherAvailabilityResultMap")
    List<TeacherAvailability> findAvailableAtTime(@Param("teacherId") Long teacherId,
                                                  @Param("dayOfWeek") String dayOfWeek,
                                                  @Param("time") LocalTime time);

    @Select("SELECT * FROM teacher_availability " +
            "WHERE teacher_id = #{teacherId} " +
            "AND day_of_week = #{dayOfWeek} " +
            "AND start_time <= #{startTime} AND end_time >= #{endTime} " +
            "AND availability_type IN ('PREFERRED', 'AVAILABLE') " +
            "AND deleted_at IS NULL")
    @ResultMap("teacherAvailabilityResultMap")
    List<TeacherAvailability> findAvailableForTimeRange(@Param("teacherId") Long teacherId,
                                                       @Param("dayOfWeek") String dayOfWeek,
                                                       @Param("startTime") LocalTime startTime,
                                                       @Param("endTime") LocalTime endTime);

    @Select("SELECT * FROM teacher_availability " +
            "WHERE teacher_id = #{teacherId} " +
            "AND day_of_week = #{dayOfWeek} " +
            "AND ((start_time <= #{startTime} AND end_time > #{startTime}) OR " +
            "(start_time < #{endTime} AND end_time >= #{endTime}) OR " +
            "(start_time >= #{startTime} AND end_time <= #{endTime})) " +
            "AND deleted_at IS NULL")
    @ResultMap("teacherAvailabilityResultMap")
    List<TeacherAvailability> findOverlappingAvailability(@Param("teacherId") Long teacherId,
                                                         @Param("dayOfWeek") String dayOfWeek,
                                                         @Param("startTime") LocalTime startTime,
                                                         @Param("endTime") LocalTime endTime);

    // Advanced Scheduling Queries
    @Select("SELECT DISTINCT t.* FROM teachers t " +
            "JOIN teacher_availability ta ON t.id = ta.teacher_id " +
            "WHERE ta.day_of_week = #{dayOfWeek} " +
            "AND ta.start_time <= #{startTime} AND ta.end_time >= #{endTime} " +
            "AND ta.availability_type IN ('PREFERRED', 'AVAILABLE') " +
            "AND t.max_weekly_hours >= #{requiredHours} " +
            "AND t.max_courses_per_semester > (SELECT COUNT(DISTINCT co.id) " +
            "FROM course_offerings co JOIN semesters s ON co.semester_id = s.id " +
            "WHERE co.teacher_id = t.id AND s.is_current = true) " +
            "AND t.deleted_at IS NULL AND ta.deleted_at IS NULL")
    @ResultMap("teacherResultMap")
    List<Teacher> findAvailableTeachersForTimeSlot(@Param("dayOfWeek") String dayOfWeek,
                                                   @Param("startTime") LocalTime startTime,
                                                   @Param("endTime") LocalTime endTime,
                                                   @Param("requiredHours") java.math.BigDecimal requiredHours);

    @Select("SELECT ta.* FROM teacher_availability ta " +
            "WHERE ta.teacher_id = #{teacherId} " +
            "AND ta.day_of_week = #{dayOfWeek} " +
            "AND ta.is_recurring = true " +
            "AND ta.availability_type IN ('PREFERRED', 'AVAILABLE') " +
            "AND ta.deleted_at IS NULL " +
            "ORDER BY ta.start_time")
    @ResultMap("teacherAvailabilityResultMap")
    List<TeacherAvailability> findRecurringAvailability(@Param("teacherId") Long teacherId,
                                                       @Param("dayOfWeek") String dayOfWeek);

    // Exception Handling (Non-Recurring Availability)
    @Select("SELECT * FROM teacher_availability " +
            "WHERE teacher_id = #{teacherId} " +
            "AND is_recurring = false " +
            "AND ((specific_date IS NOT NULL AND specific_date = #{date}) OR " +
            "(start_date <= #{date} AND end_date >= #{date})) " +
            "AND deleted_at IS NULL")
    @ResultMap("teacherAvailabilityResultMap")
    List<TeacherAvailability> findExceptionsForDate(@Param("teacherId") Long teacherId,
                                                   @Param("date") LocalDate date);

    // Batch Operations
    @Insert("<script>" +
            "INSERT INTO teacher_availability (teacher_id, day_of_week, start_time, end_time, " +
            "availability_type, max_classes, break_duration, requires_break_between_classes, " +
            "is_recurring, notes, created_at, updated_at) VALUES " +
            "<foreach collection='availabilities' item='availability' separator=','>" +
            "(#{availability.teacherId}, #{availability.dayOfWeek}, #{availability.startTime}, #{availability.endTime}, " +
            "#{availability.availabilityType}, #{availability.maxClasses}, #{availability.breakDuration}, " +
            "#{availability.requiresBreakBetweenClasses}, #{availability.isRecurring}, #{availability.notes}, " +
            "#{availability.createdAt}, #{availability.updatedAt})" +
            "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("availabilities") List<TeacherAvailability> availabilities);

    @Update("<script>" +
            "<foreach collection='availabilities' item='availability' separator=';'>" +
            "UPDATE teacher_availability SET teacher_id = #{availability.teacherId}, day_of_week = #{availability.dayOfWeek}, " +
            "start_time = #{availability.startTime}, end_time = #{availability.endTime}, " +
            "availability_type = #{availability.availabilityType}, max_classes = #{availability.maxClasses}, " +
            "break_duration = #{availability.breakDuration}, requires_break_between_classes = #{availability.requiresBreakBetweenClasses}, " +
            "is_recurring = #{availability.isRecurring}, notes = #{availability.notes}, " +
            "updated_at = #{availability.updatedAt} WHERE id = #{availability.id}" +
            "</foreach>" +
            "</script>")
    int batchUpdate(@Param("availabilities") List<TeacherAvailability> availabilities);

    // Statistics and Reporting
    @Select("SELECT t.id, t.employee_id, u.first_name, u.last_name, " +
            "COUNT(ta.id) as total_availability_slots, " +
            "SUM(CASE WHEN ta.availability_type = 'PREFERRED' THEN 1 ELSE 0 END) as preferred_slots, " +
            "AVG(ta.max_classes) as avg_max_classes " +
            "FROM teachers t " +
            "LEFT JOIN users u ON t.user_id = u.id " +
            "LEFT JOIN teacher_availability ta ON t.id = ta.teacher_id " +
            "WHERE t.deleted_at IS NULL AND ta.deleted_at IS NULL " +
            "GROUP BY t.id, t.employee_id, u.first_name, u.last_name " +
            "ORDER BY u.last_name, u.first_name")
    List<TeacherAvailabilityStats> getTeacherAvailabilityStatistics();

    // Interface for statistics result
    interface TeacherAvailabilityStats {
        Long getId();
        String getEmployeeId();
        String getFirstName();
        String getLastName();
        Long getTotalAvailabilitySlots();
        Long getPreferredSlots();
        java.math.BigDecimal getAvgMaxClasses();
    }
}