package com.school.scheduling.mapper;

import com.school.scheduling.domain.TimeSlot;
import org.apache.ibatis.annotations.*;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface TimeSlotMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO time_slots (day_of_week, start_time, end_time, slot_type, " +
            "is_active, created_at, updated_at) " +
            "VALUES (#{dayOfWeek}, #{startTime}, #{endTime}, #{slotType}, " +
            "#{isActive}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TimeSlot timeSlot);

    @Update("UPDATE time_slots SET day_of_week = #{dayOfWeek}, start_time = #{startTime}, " +
            "end_time = #{endTime}, slot_type = #{slotType}, is_active = #{isActive}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(TimeSlot timeSlot);

    @Delete("UPDATE time_slots SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Select("SELECT * FROM time_slots WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "timeSlotResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "dayOfWeek", column = "day_of_week", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "startTime", column = "start_time"),
        @Result(property = "endTime", column = "end_time"),
        @Result(property = "slotType", column = "slot_type", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<TimeSlot> findById(@Param("id") Long id);

    @Select("SELECT * FROM time_slots WHERE day_of_week = #{dayOfWeek} " +
            "AND start_time = #{startTime} AND end_time = #{endTime} " +
            "AND deleted_at IS NULL")
    @ResultMap("timeSlotResultMap")
    Optional<TimeSlot> findByDayAndTime(@Param("dayOfWeek") String dayOfWeek,
                                        @Param("startTime") LocalTime startTime,
                                        @Param("endTime") LocalTime endTime);

    @Select("SELECT * FROM time_slots WHERE day_of_week = #{dayOfWeek} AND deleted_at IS NULL " +
            "ORDER BY start_time")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findByDayOfWeek(@Param("dayOfWeek") String dayOfWeek);

    @Select("SELECT * FROM time_slots WHERE slot_type = #{slotType} AND deleted_at IS NULL " +
            "ORDER BY day_of_week, start_time")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findBySlotType(@Param("slotType") String slotType);

    @Select("SELECT * FROM time_slots WHERE is_active = true AND deleted_at IS NULL " +
            "ORDER BY day_of_week, start_time")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findActiveTimeSlots();

    @Select("SELECT * FROM time_slots WHERE is_active = false AND deleted_at IS NULL " +
            "ORDER BY day_of_week, start_time")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findInactiveTimeSlots();

    @Select("SELECT * FROM time_slots WHERE start_time >= #{startTime} " +
            "AND end_time <= #{endTime} AND deleted_at IS NULL " +
            "ORDER BY day_of_week, start_time")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findByTimeRange(@Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime);

    @Select("SELECT * FROM time_slots WHERE start_time >= #{startTime} " +
            "AND deleted_at IS NULL ORDER BY start_time")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findStartingAfter(@Param("startTime") LocalTime startTime);

    @Select("SELECT * FROM time_slots WHERE end_time <= #{endTime} " +
            "AND deleted_at IS NULL ORDER BY end_time")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findEndingBefore(@Param("endTime") LocalTime endTime);

    @Select("<script>" +
            "SELECT * FROM time_slots WHERE deleted_at IS NULL " +
            "<if test='dayOfWeek != null'>AND day_of_week = #{dayOfWeek}</if> " +
            "<if test='slotType != null'>AND slot_type = #{slotType}</if> " +
            "<if test='isActive != null'>AND is_active = #{isActive}</if> " +
            "<if test='startTime != null'>AND start_time >= #{startTime}</if> " +
            "<if test='endTime != null'>AND end_time <= #{endTime}</if> " +
            "<if test='durationMinutes != null'>" +
            "AND TIME_TO_SEC(end_time) - TIME_TO_SEC(start_time) = #{durationMinutes} * 60" +
            "</if> " +
            "ORDER BY day_of_week, start_time" +
            "</script>")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findByCriteria(@Param("dayOfWeek") String dayOfWeek,
                                  @Param("slotType") String slotType,
                                  @Param("isActive") Boolean isActive,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime,
                                  @Param("durationMinutes") Integer durationMinutes);

    @Select("SELECT * FROM time_slots WHERE deleted_at IS NULL ORDER BY day_of_week, start_time")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findAll();

    @Select("SELECT * FROM time_slots WHERE deleted_at IS NULL AND is_active = true ORDER BY day_of_week, start_time")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findAllActive();

    @Select("<script>" +
            "SELECT * FROM time_slots WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_at IS NULL" +
            "</script>")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findByIds(@Param("ids") List<Long> ids);

    @Select("SELECT COUNT(*) FROM time_slots WHERE day_of_week = #{dayOfWeek} AND deleted_at IS NULL")
    int countByDayOfWeek(@Param("dayOfWeek") String dayOfWeek);

    @Select("SELECT COUNT(*) FROM time_slots WHERE slot_type = #{slotType} AND deleted_at IS NULL")
    int countBySlotType(@Param("slotType") String slotType);

    @Select("SELECT COUNT(*) FROM time_slots WHERE is_active = true AND deleted_at IS NULL")
    int countActiveTimeSlots();

    @Select("SELECT COUNT(*) FROM time_slots WHERE deleted_at IS NULL")
    int countAllTimeSlots();

    @Update("UPDATE time_slots SET is_active = #{isActive}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateActiveStatus(@Param("id") Long id,
                          @Param("isActive") Boolean isActive,
                          @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE time_slots SET start_time = #{startTime}, end_time = #{endTime}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateTimeRange(@Param("id") Long id,
                       @Param("startTime") LocalTime startTime,
                       @Param("endTime") LocalTime endTime,
                       @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE time_slots SET slot_type = #{slotType}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateSlotType(@Param("id") Long id,
                      @Param("slotType") String slotType,
                      @Param("updatedAt") LocalDateTime updatedAt);

    @Select("SELECT day_of_week, COUNT(*) as count " +
            "FROM time_slots WHERE deleted_at IS NULL " +
            "GROUP BY day_of_week ORDER BY count DESC")
    List<DayCount> getDayDistribution();

    @Select("SELECT slot_type, COUNT(*) as count " +
            "FROM time_slots WHERE deleted_at IS NULL " +
            "GROUP BY slot_type ORDER BY count DESC")
    List<TypeCount> getSlotTypeDistribution();

    @Select("SELECT " +
            "SUM(CASE WHEN TIME_TO_SEC(end_time) - TIME_TO_SEC(start_time) = 3000 THEN 1 ELSE 0 END) as count_50min, " +
            "SUM(CASE WHEN TIME_TO_SEC(end_time) - TIME_TO_SEC(start_time) = 3600 THEN 1 ELSE 0 END) as count_60min, " +
            "SUM(CASE WHEN TIME_TO_SEC(end_time) - TIME_TO_SEC(start_time) = 4500 THEN 1 ELSE 0 END) as count_75min, " +
            "SUM(CASE WHEN TIME_TO_SEC(end_time) - TIME_TO_SEC(start_time) = 5400 THEN 1 ELSE 0 END) as count_90min, " +
            "SUM(CASE WHEN TIME_TO_SEC(end_time) - TIME_TO_SEC(start_time) = 7200 THEN 1 ELSE 0 END) as count_120min " +
            "FROM time_slots WHERE deleted_at IS NULL")
    DurationCount getDurationDistribution();

    @Select("SELECT ts.* FROM time_slots ts " +
            "WHERE ts.day_of_week = #{dayOfWeek} AND ts.is_active = true " +
            "AND ts.deleted_at IS NULL AND NOT EXISTS (" +
            "    SELECT 1 FROM schedules s " +
            "    WHERE s.time_slot_id = ts.id AND s.deleted_at IS NULL" +
            ") " +
            "ORDER BY ts.start_time")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findAvailableTimeSlotsByDay(@Param("dayOfWeek") String dayOfWeek);

    @Select("SELECT ts.* FROM time_slots ts " +
            "JOIN schedules s ON ts.id = s.time_slot_id " +
            "WHERE ts.is_active = true AND ts.deleted_at IS NULL " +
            "AND s.deleted_at IS NULL AND s.semester_id = #{semesterId} " +
            "ORDER BY ts.day_of_week, ts.start_time")
    @ResultMap("timeSlotResultMap")
    List<TimeSlot> findScheduledTimeSlotsBySemester(@Param("semesterId") Long semesterId);

    // Result classes for distribution queries
    class DayCount {
        private String dayOfWeek;
        private Integer count;

        // Getters and setters
        public String getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    class TypeCount {
        private String slotType;
        private Integer count;

        // Getters and setters
        public String getSlotType() { return slotType; }
        public void setSlotType(String slotType) { this.slotType = slotType; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    class DurationCount {
        private Integer count50min;
        private Integer count60min;
        private Integer count75min;
        private Integer count90min;
        private Integer count120min;

        // Getters and setters
        public Integer getCount50min() { return count50min; }
        public void setCount50min(Integer count50min) { this.count50min = count50min; }
        public Integer getCount60min() { return count60min; }
        public void setCount60min(Integer count60min) { this.count60min = count60min; }
        public Integer getCount75min() { return count75min; }
        public void setCount75min(Integer count75min) { this.count75min = count75min; }
        public Integer getCount90min() { return count90min; }
        public void setCount90min(Integer count90min) { this.count90min = count90min; }
        public Integer getCount120min() { return count120min; }
        public void setCount120min(Integer count120min) { this.count120min = count120min; }
    }
}