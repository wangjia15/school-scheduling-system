package com.school.scheduling.mapper;

import com.school.scheduling.domain.Schedule;
import com.school.scheduling.domain.CourseOffering;
import com.school.scheduling.domain.Classroom;
import com.school.scheduling.domain.TimeSlot;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ScheduleMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO schedules (course_offering_id, classroom_id, time_slot_id, schedule_date, " +
            "is_recurring, recurrence_pattern, notes, created_at, updated_at) " +
            "VALUES (#{courseOfferingId}, #{classroomId}, #{timeSlotId}, #{scheduleDate}, " +
            "#{isRecurring}, #{recurrencePattern}, #{notes}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Schedule schedule);

    @Update("UPDATE schedules SET course_offering_id = #{courseOfferingId}, classroom_id = #{classroomId}, " +
            "time_slot_id = #{timeSlotId}, schedule_date = #{scheduleDate}, is_recurring = #{isRecurring}, " +
            "recurrence_pattern = #{recurrencePattern}, notes = #{notes}, updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int update(Schedule schedule);

    @Delete("UPDATE schedules SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Delete("DELETE FROM schedules WHERE id = #{id}")
    int hardDelete(@Param("id") Long id);

    @Select("SELECT * FROM schedules WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "scheduleResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "courseOfferingId", column = "course_offering_id"),
        @Result(property = "classroomId", column = "classroom_id"),
        @Result(property = "timeSlotId", column = "time_slot_id"),
        @Result(property = "scheduleDate", column = "schedule_date"),
        @Result(property = "isRecurring", column = "is_recurring"),
        @Result(property = "recurrencePattern", column = "recurrence_pattern"),
        @Result(property = "notes", column = "notes"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<Schedule> findById(@Param("id") Long id);

    @Select("SELECT * FROM schedules WHERE deleted_at IS NULL ORDER BY schedule_date, time_slot_id")
    @ResultMap("scheduleResultMap")
    List<Schedule> findAll();

    @Select("SELECT COUNT(*) FROM schedules WHERE deleted_at IS NULL")
    long countAll();

    // Date-based Queries
    @Select("SELECT * FROM schedules WHERE schedule_date = #{date} AND deleted_at IS NULL " +
            "ORDER BY time_slot_id")
    @ResultMap("scheduleResultMap")
    List<Schedule> findByDate(@Param("date") String date);

    @Select("SELECT * FROM schedules WHERE schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted_at IS NULL ORDER BY schedule_date, time_slot_id")
    @ResultMap("scheduleResultMap")
    List<Schedule> findByDateRange(@Param("startDate") String startDate,
                                   @Param("endDate") String endDate);

    @Select("SELECT * FROM schedules WHERE schedule_date >= #{date} AND deleted_at IS NULL " +
            "ORDER BY schedule_date, time_slot_id")
    @ResultMap("scheduleResultMap")
    List<Schedule> findUpcomingSchedules(@Param("date") String date);

    // Conflict Detection Queries
    @Select("SELECT * FROM schedules " +
            "WHERE classroom_id = #{classroomId} " +
            "AND time_slot_id = #{timeSlotId} " +
            "AND schedule_date = #{date} " +
            "AND deleted_at IS NULL " +
            "<if test='excludeScheduleId != null'>AND id != #{excludeScheduleId}</if>")
    @ResultMap("scheduleResultMap")
    List<Schedule> findClassroomConflicts(@Param("classroomId") Long classroomId,
                                          @Param("timeSlotId") Long timeSlotId,
                                          @Param("date") String date,
                                          @Param("excludeScheduleId") Long excludeScheduleId);

    @Select("SELECT s.* FROM schedules s " +
            "JOIN course_offerings co ON s.course_offering_id = co.id " +
            "WHERE co.teacher_id = #{teacherId} " +
            "AND s.time_slot_id = #{timeSlotId} " +
            "AND s.schedule_date = #{date} " +
            "AND s.deleted_at IS NULL AND co.deleted_at IS NULL " +
            "<if test='excludeScheduleId != null'>AND s.id != #{excludeScheduleId}</if>")
    @ResultMap("scheduleResultMap")
    List<Schedule> findTeacherConflicts(@Param("teacherId") Long teacherId,
                                        @Param("timeSlotId") Long timeSlotId,
                                        @Param("date") String date,
                                        @Param("excludeScheduleId") Long excludeScheduleId);

    @Select("SELECT s.* FROM schedules s " +
            "JOIN course_offerings co ON s.course_offering_id = co.id " +
            "JOIN enrollments e ON co.id = e.course_offering_id " +
            "WHERE e.student_id = #{studentId} " +
            "AND s.time_slot_id = #{timeSlotId} " +
            "AND s.schedule_date = #{date} " +
            "AND s.deleted_at IS NULL AND co.deleted_at IS NULL AND e.deleted_at IS NULL " +
            "AND e.status = 'ENROLLED' " +
            "<if test='excludeScheduleId != null'>AND s.id != #{excludeScheduleId}</if>")
    @ResultMap("scheduleResultMap")
    List<Schedule> findStudentConflicts(@Param("studentId") Long studentId,
                                       @Param("timeSlotId") Long timeSlotId,
                                       @Param("date") String date,
                                       @Param("excludeScheduleId") Long excludeScheduleId);

    // Course Offering and Teacher Schedules
    @Select("SELECT * FROM schedules WHERE course_offering_id = #{courseOfferingId} " +
            "AND deleted_at IS NULL ORDER BY schedule_date")
    @ResultMap("scheduleResultMap")
    List<Schedule> findByCourseOfferingId(@Param("courseOfferingId") Long courseOfferingId);

    @Select("SELECT s.* FROM schedules s " +
            "JOIN course_offerings co ON s.course_offering_id = co.id " +
            "WHERE co.teacher_id = #{teacherId} " +
            "AND s.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND s.deleted_at IS NULL AND co.deleted_at IS NULL " +
            "ORDER BY s.schedule_date, s.time_slot_id")
    @ResultMap("scheduleResultMap")
    List<Schedule> findByTeacherAndDateRange(@Param("teacherId") Long teacherId,
                                             @Param("startDate") String startDate,
                                             @Param("endDate") String endDate);

    @Select("SELECT s.* FROM schedules s " +
            "JOIN course_offerings co ON s.course_offering_id = co.id " +
            "JOIN semesters se ON co.semester_id = se.id " +
            "WHERE co.teacher_id = #{teacherId} " +
            "AND se.is_current = true " +
            "AND s.deleted_at IS NULL AND co.deleted_at IS NULL " +
            "ORDER BY s.schedule_date, s.time_slot_id")
    @ResultMap("scheduleResultMap")
    List<Schedule> findCurrentSemesterTeacherSchedules(@Param("teacherId") Long teacherId);

    @Select("SELECT s.* FROM schedules s " +
            "JOIN course_offerings co ON s.course_offering_id = co.id " +
            "WHERE co.semester_id = #{semesterId} " +
            "AND s.deleted_at IS NULL AND co.deleted_at IS NULL " +
            "ORDER BY s.schedule_date, s.time_slot_id")
    @ResultMap("scheduleResultMap")
    List<Schedule> findBySemesterId(@Param("semesterId") Long semesterId);

    @Select("SELECT * FROM schedule_conflicts WHERE id = #{id}")
    Optional<ScheduleConflict> findConflictById(@Param("id") Long id);

    // Classroom Usage
    @Select("SELECT * FROM schedules WHERE classroom_id = #{classroomId} " +
            "AND schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted_at IS NULL ORDER BY schedule_date, time_slot_id")
    @ResultMap("scheduleResultMap")
    List<Schedule> findByClassroomAndDateRange(@Param("classroomId") Long classroomId,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);

    // Recurring Schedule Management
    @Select("SELECT * FROM schedules WHERE is_recurring = true " +
            "AND recurrence_pattern = #{recurrencePattern} " +
            "AND deleted_at IS NULL ORDER BY schedule_date")
    @ResultMap("scheduleResultMap")
    List<Schedule> findRecurringSchedules(@Param("recurrencePattern") String recurrencePattern);

    @Select("SELECT * FROM schedules WHERE is_recurring = true " +
            "AND schedule_date >= #{startDate} " +
            "AND deleted_at IS NULL ORDER BY schedule_date")
    @ResultMap("scheduleResultMap")
    List<Schedule> findActiveRecurringSchedules(@Param("startDate") String startDate);

    // Complex Scheduling Queries
    @Select("SELECT s.* FROM schedules s " +
            "JOIN course_offerings co ON s.course_offering_id = co.id " +
            "JOIN courses c ON co.course_id = c.id " +
            "JOIN classrooms cr ON s.classroom_id = cr.id " +
            "JOIN time_slots ts ON s.time_slot_id = ts.id " +
            "WHERE s.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND s.deleted_at IS NULL " +
            "<if test='departmentId != null'>AND c.department_id = #{departmentId}</if> " +
            "<if test='buildingCode != null'>AND cr.building_code = #{buildingCode}</if> " +
            "<if test='roomType != null'>AND cr.room_type = #{roomType}</if> " +
            "<if test='dayOfWeek != null'>AND ts.day_of_week = #{dayOfWeek}</if> " +
            "ORDER BY s.schedule_date, ts.start_time")
    @ResultMap("scheduleResultMap")
    List<Schedule> findSchedulesByMultipleCriteria(@Param("startDate") String startDate,
                                                  @Param("endDate") String endDate,
                                                  @Param("departmentId") Long departmentId,
                                                  @Param("buildingCode") String buildingCode,
                                                  @Param("roomType") String roomType,
                                                  @Param("dayOfWeek") String dayOfWeek);

    // Advanced Conflict Detection
    @Select("SELECT s.* FROM schedules s " +
            "JOIN course_offerings co ON s.course_offering_id = co.id " +
            "WHERE s.schedule_date = #{date} " +
            "AND s.time_slot_id = #{timeSlotId} " +
            "AND s.deleted_at IS NULL AND co.deleted_at IS NULL " +
            "AND (s.classroom_id = #{classroomId} OR co.teacher_id = #{teacherId})")
    @ResultMap("scheduleResultMap")
    List<Schedule> findPotentialConflicts(@Param("date") String date,
                                          @Param("timeSlotId") Long timeSlotId,
                                          @Param("classroomId") Long classroomId,
                                          @Param("teacherId") Long teacherId);

    // Schedule Statistics and Analytics
    @Select("SELECT COUNT(*) as total_schedules, " +
            "COUNT(DISTINCT schedule_date) as unique_dates, " +
            "COUNT(DISTINCT classroom_id) as classrooms_used, " +
            "COUNT(DISTINCT course_offering_id) as courses_scheduled " +
            "FROM schedules " +
            "WHERE schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted_at IS NULL")
    ScheduleStatistics getScheduleStatistics(@Param("startDate") String startDate,
                                            @Param("endDate") String endDate);

    @Select("SELECT ts.day_of_week, COUNT(*) as schedule_count " +
            "FROM schedules s " +
            "JOIN time_slots ts ON s.time_slot_id = ts.id " +
            "WHERE s.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND s.deleted_at IS NULL " +
            "GROUP BY ts.day_of_week " +
            "ORDER BY FIELD(ts.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')")
    List<DayOfWeekStats> getScheduleStatsByDayOfWeek(@Param("startDate") String startDate,
                                                       @Param("endDate") String endDate);

    // Time Slot Analysis
    @Select("SELECT ts.*, COUNT(s.id) as usage_count " +
            "FROM time_slots ts " +
            "LEFT JOIN schedules s ON ts.id = s.time_slot_id " +
            "AND s.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND s.deleted_at IS NULL " +
            "WHERE ts.is_active = true " +
            "GROUP BY ts.id " +
            "ORDER BY ts.day_of_week, ts.start_time")
    List<TimeSlotUsage> getTimeSlotUsage(@Param("startDate") String startDate,
                                        @Param("endDate") String endDate);

    // Search Methods
    @Select("<script>" +
            "SELECT s.* FROM schedules s " +
            "JOIN course_offerings co ON s.course_offering_id = co.id " +
            "JOIN courses c ON co.course_id = c.id " +
            "JOIN classrooms cr ON s.classroom_id = cr.id " +
            "JOIN time_slots ts ON s.time_slot_id = ts.id " +
            "JOIN teachers t ON co.teacher_id = t.id " +
            "JOIN users u ON t.user_id = u.id " +
            "WHERE s.deleted_at IS NULL " +
            "<if test='searchText != null and !searchText.isEmpty()'>" +
            "AND (c.course_code LIKE CONCAT('%', #{searchText}, '%') " +
            "OR c.title LIKE CONCAT('%', #{searchText}, '%') " +
            "OR cr.building_code LIKE CONCAT('%', #{searchText}, '%') " +
            "OR cr.room_number LIKE CONCAT('%', #{searchText}, '%') " +
            "OR u.first_name LIKE CONCAT('%', #{searchText}, '%') " +
            "OR u.last_name LIKE CONCAT('%', #{searchText}, '%') " +
            "OR s.notes LIKE CONCAT('%', #{searchText}, '%')) " +
            "</if>" +
            "<if test='startDate != null'>AND s.schedule_date >= #{startDate}</if> " +
            "<if test='endDate != null'>AND s.schedule_date <= #{endDate}</if> " +
            "<if test='departmentId != null'>AND c.department_id = #{departmentId}</if> " +
            "<if test='teacherId != null'>AND co.teacher_id = #{teacherId}</if> " +
            "<if test='classroomId != null'>AND s.classroom_id = #{classroomId}</if> " +
            "<if test='buildingCode != null'>AND cr.building_code = #{buildingCode}</if> " +
            "<if test='dayOfWeek != null'>AND ts.day_of_week = #{dayOfWeek}</if> " +
            "ORDER BY s.schedule_date, ts.start_time" +
            "</script>")
    @ResultMap("scheduleResultMap")
    List<Schedule> searchSchedules(@Param("searchText") String searchText,
                                  @Param("startDate") String startDate,
                                  @Param("endDate") String endDate,
                                  @Param("departmentId") Long departmentId,
                                  @Param("teacherId") Long teacherId,
                                  @Param("classroomId") Long classroomId,
                                  @Param("buildingCode") String buildingCode,
                                  @Param("dayOfWeek") String dayOfWeek);

    // Batch Operations
    @Insert("<script>" +
            "INSERT INTO schedules (course_offering_id, classroom_id, time_slot_id, schedule_date, " +
            "is_recurring, recurrence_pattern, notes, created_at, updated_at) VALUES " +
            "<foreach collection='schedules' item='schedule' separator=','>" +
            "(#{schedule.courseOfferingId}, #{schedule.classroomId}, #{schedule.timeSlotId}, " +
            "#{schedule.scheduleDate}, #{schedule.isRecurring}, #{schedule.recurrencePattern}, " +
            "#{schedule.notes}, #{schedule.createdAt}, #{schedule.updatedAt})" +
            "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("schedules") List<Schedule> schedules);

    @Update("<script>" +
            "<foreach collection='schedules' item='schedule' separator=';'>" +
            "UPDATE schedules SET course_offering_id = #{schedule.courseOfferingId}, " +
            "classroom_id = #{schedule.classroomId}, time_slot_id = #{schedule.timeSlotId}, " +
            "schedule_date = #{schedule.scheduleDate}, is_recurring = #{schedule.isRecurring}, " +
            "recurrence_pattern = #{schedule.recurrencePattern}, notes = #{schedule.notes}, " +
            "updated_at = #{schedule.updatedAt} WHERE id = #{schedule.id}" +
            "</foreach>" +
            "</script>")
    int batchUpdate(@Param("schedules") List<Schedule> schedules);

    // Export support methods
    @Select("<script>" +
            "SELECT * FROM schedules WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_at IS NULL" +
            "</script>")
    @ResultMap("scheduleResultMap")
    List<Schedule> findByIds(@Param("ids") List<Long> ids);

    // Result classes for complex queries
    interface ScheduleStatistics {
        Integer getTotalSchedules();
        Integer getUniqueDates();
        Integer getClassroomsUsed();
        Integer getCoursesScheduled();
    }

    interface DayOfWeekStats {
        String getDayOfWeek();
        Integer getScheduleCount();
    }

    interface TimeSlotUsage {
        Long getId();
        String getDayOfWeek();
        java.time.LocalTime getStartTime();
        java.time.LocalTime getEndTime();
        String getSlotType();
        Integer getUsageCount();
    }
}