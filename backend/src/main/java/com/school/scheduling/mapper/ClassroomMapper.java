package com.school.scheduling.mapper;

import com.school.scheduling.domain.Classroom;
import com.school.scheduling.domain.Schedule;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ClassroomMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO classrooms (building_code, room_number, name, capacity, room_type, " +
            "has_projector, has_computer, has_whiteboard, special_equipment, is_available, " +
            "created_at, updated_at) " +
            "VALUES (#{buildingCode}, #{roomNumber}, #{name}, #{capacity}, #{roomType}, " +
            "#{hasProjector}, #{hasComputer}, #{hasWhiteboard}, #{specialEquipment}, #{isAvailable}, " +
            "#{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Classroom classroom);

    @Update("UPDATE classrooms SET building_code = #{buildingCode}, room_number = #{roomNumber}, " +
            "name = #{name}, capacity = #{capacity}, room_type = #{roomType}, " +
            "has_projector = #{hasProjector}, has_computer = #{hasComputer}, " +
            "has_whiteboard = #{hasWhiteboard}, special_equipment = #{specialEquipment}, " +
            "is_available = #{isAvailable}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Classroom classroom);

    @Delete("UPDATE classrooms SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Delete("DELETE FROM classrooms WHERE id = #{id}")
    int hardDelete(@Param("id") Long id);

    @Select("SELECT * FROM classrooms WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "classroomResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "buildingCode", column = "building_code"),
        @Result(property = "roomNumber", column = "room_number"),
        @Result(property = "name", column = "name"),
        @Result(property = "capacity", column = "capacity"),
        @Result(property = "roomType", column = "room_type", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "hasProjector", column = "has_projector"),
        @Result(property = "hasComputer", column = "has_computer"),
        @Result(property = "hasWhiteboard", column = "has_whiteboard"),
        @Result(property = "specialEquipment", column = "special_equipment", typeHandler = org.apache.ibatis.type.JsonTypeHandler.class),
        @Result(property = "isAvailable", column = "is_available"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<Classroom> findById(@Param("id") Long id);

    @Select("SELECT * FROM classrooms WHERE building_code = #{buildingCode} " +
            "AND room_number = #{roomNumber} AND deleted_at IS NULL")
    @ResultMap("classroomResultMap")
    Optional<Classroom> findByLocation(@Param("buildingCode") String buildingCode,
                                      @Param("roomNumber") String roomNumber);

    @Select("SELECT * FROM classrooms WHERE building_code = #{buildingCode} AND deleted_at IS NULL " +
            "ORDER BY room_number")
    @ResultMap("classroomResultMap")
    List<Classroom> findByBuildingCode(@Param("buildingCode") String buildingCode);

    @Select("<script>" +
            "SELECT * FROM classrooms WHERE deleted_at IS NULL " +
            "<if test='roomType != null'>AND room_type = #{roomType}</if> " +
            "<if test='minCapacity != null'>AND capacity >= #{minCapacity}</if> " +
            "<if test='maxCapacity != null'>AND capacity <= #{maxCapacity}</if> " +
            "<if test='hasProjector != null'>AND has_projector = #{hasProjector}</if> " +
            "<if test='hasComputer != null'>AND has_computer = #{hasComputer}</if> " +
            "<if test='hasWhiteboard != null'>AND has_whiteboard = #{hasWhiteboard}</if> " +
            "<if test='isAvailable != null'>AND is_available = #{isAvailable}</if> " +
            "ORDER BY building_code, room_number" +
            "</script>")
    @ResultMap("classroomResultMap")
    List<Classroom> findByCriteria(@Param("roomType") String roomType,
                                  @Param("minCapacity") Integer minCapacity,
                                  @Param("maxCapacity") Integer maxCapacity,
                                  @Param("hasProjector") Boolean hasProjector,
                                  @Param("hasComputer") Boolean hasComputer,
                                  @Param("hasWhiteboard") Boolean hasWhiteboard,
                                  @Param("isAvailable") Boolean isAvailable);

    @Select("SELECT * FROM classrooms WHERE deleted_at IS NULL ORDER BY building_code, room_number")
    @ResultMap("classroomResultMap")
    List<Classroom> findAll();

    @Select("SELECT * FROM classrooms WHERE deleted_at IS NULL AND is_available = true ORDER BY building_code, room_number")
    @ResultMap("classroomResultMap")
    List<Classroom> findAllActive();

    @Select("<script>" +
            "SELECT * FROM classrooms WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_at IS NULL" +
            "</script>")
    @ResultMap("classroomResultMap")
    List<Classroom> findByIds(@Param("ids") List<Long> ids);

    @Select("SELECT COUNT(*) FROM classrooms WHERE deleted_at IS NULL")
    long countAll();

    // Availability and Scheduling Queries
    @Select("SELECT COUNT(*) FROM schedules " +
            "WHERE classroom_id = #{classroomId} " +
            "AND schedule_date = #{date} " +
            "AND deleted_at IS NULL")
    int countSchedulesForDate(@Param("classroomId") Long classroomId,
                              @Param("date") String date);

    @Select("SELECT * FROM schedules " +
            "WHERE classroom_id = #{classroomId} " +
            "AND schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted_at IS NULL " +
            "ORDER BY schedule_date")
    @ResultMap("scheduleResultMap")
    List<Schedule> findSchedulesByDateRange(@Param("classroomId") Long classroomId,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

    @Select("SELECT c.* FROM classrooms c " +
            "WHERE c.deleted_at IS NULL " +
            "AND c.is_available = true " +
            "AND c.capacity >= #{minCapacity} " +
            "AND c.id NOT IN (" +
            "SELECT DISTINCT s.classroom_id FROM schedules s " +
            "WHERE s.schedule_date = #{date} " +
            "AND s.time_slot_id = #{timeSlotId} " +
            "AND s.deleted_at IS NULL" +
            ")")
    @ResultMap("classroomResultMap")
    List<Classroom> findAvailableClassrooms(@Param("date") String date,
                                           @Param("timeSlotId") Long timeSlotId,
                                           @Param("minCapacity") Integer minCapacity);

    @Select("SELECT c.* FROM classrooms c " +
            "WHERE c.deleted_at IS NULL " +
            "AND c.is_available = true " +
            "AND c.capacity >= #{minCapacity} " +
            "AND c.room_type = #{roomType} " +
            "AND c.id NOT IN (" +
            "SELECT DISTINCT s.classroom_id FROM schedules s " +
            "WHERE s.schedule_date = #{date} " +
            "AND s.time_slot_id = #{timeSlotId} " +
            "AND s.deleted_at IS NULL" +
            ")")
    @ResultMap("classroomResultMap")
    List<Classroom> findAvailableClassroomsByType(@Param("date") String date,
                                                  @Param("timeSlotId") Long timeSlotId,
                                                  @Param("minCapacity") Integer minCapacity,
                                                  @Param("roomType") String roomType);

    // Equipment Search Methods
    @Select("SELECT * FROM classrooms " +
            "WHERE deleted_at IS NULL " +
            "AND is_available = true " +
            "AND capacity >= #{minCapacity} " +
            "AND has_projector = #{hasProjector} " +
            "AND has_computer = #{hasComputer} " +
            "AND has_whiteboard = #{hasWhiteboard}")
    @ResultMap("classroomResultMap")
    List<Classroom> findByEquipmentRequirements(@Param("minCapacity") Integer minCapacity,
                                                @Param("hasProjector") Boolean hasProjector,
                                                @Param("hasComputer") Boolean hasComputer,
                                                @Param("hasWhiteboard") Boolean hasWhiteboard);

    @Select("<script>" +
            "SELECT * FROM classrooms " +
            "WHERE deleted_at IS NULL " +
            "AND is_available = true " +
            "AND JSON_CONTAINS(special_equipment, JSON_ARRAY(#{equipment})) " +
            "<if test='minCapacity != null'>AND capacity >= #{minCapacity}</if> " +
            "<if test='roomType != null'>AND room_type = #{roomType}</if> " +
            "ORDER BY building_code, room_number" +
            "</script>")
    @ResultMap("classroomResultMap")
    List<Classroom> findBySpecialEquipment(@Param("equipment") String equipment,
                                         @Param("minCapacity") Integer minCapacity,
                                         @Param("roomType") String roomType);

    // Building and Capacity Analysis
    @Select("SELECT building_code, COUNT(*) as room_count, " +
            "SUM(capacity) as total_capacity, AVG(capacity) as avg_capacity " +
            "FROM classrooms " +
            "WHERE deleted_at IS NULL " +
            "GROUP BY building_code " +
            "ORDER BY building_code")
    List<BuildingCapacityStats> getBuildingCapacityStats();

    @Select("SELECT room_type, COUNT(*) as count, " +
            "MIN(capacity) as min_capacity, MAX(capacity) as max_capacity, AVG(capacity) as avg_capacity " +
            "FROM classrooms " +
            "WHERE deleted_at IS NULL " +
            "GROUP BY room_type " +
            "ORDER BY room_type")
    List<RoomTypeStats> getRoomTypeStats();

    // Utilization Analysis
    @Select("SELECT c.*, " +
            "(SELECT COUNT(*) FROM schedules s WHERE s.classroom_id = c.id " +
            "AND s.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND s.deleted_at IS NULL) as scheduled_hours, " +
            "(SELECT COUNT(DISTINCT s.schedule_date) FROM schedules s WHERE s.classroom_id = c.id " +
            "AND s.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND s.deleted_at IS NULL) as days_used " +
            "FROM classrooms c " +
            "WHERE c.deleted_at IS NULL " +
            "ORDER BY building_code, room_number")
    @ResultMap("classroomResultMap")
    List<Classroom> findWithUtilization(@Param("startDate") String startDate,
                                       @Param("endDate") String endDate);

    @Select("SELECT c.id, c.building_code, c.room_number, c.name, c.capacity, " +
            "COUNT(DISTINCT s.id) as schedule_count, " +
            "COUNT(DISTINCT s.schedule_date) as days_used, " +
            "COUNT(DISTINCT s.time_slot_id) as time_slots_used " +
            "FROM classrooms c " +
            "LEFT JOIN schedules s ON c.id = s.classroom_id AND s.deleted_at IS NULL " +
            "WHERE c.deleted_at IS NULL " +
            "AND s.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY c.id, c.building_code, c.room_number, c.name, c.capacity " +
            "HAVING COUNT(DISTINCT s.id) >= #{minUsage} " +
            "ORDER BY schedule_count DESC")
    List<ClassroomUtilization> getClassroomUtilization(@Param("startDate") String startDate,
                                                      @Param("endDate") String endDate,
                                                      @Param("minUsage") Integer minUsage);

    // Search and Filter Methods
    @Select("<script>" +
            "SELECT * FROM classrooms " +
            "WHERE deleted_at IS NULL " +
            "<if test='searchText != null and !searchText.isEmpty()'>" +
            "AND (building_code LIKE CONCAT('%', #{searchText}, '%') " +
            "OR room_number LIKE CONCAT('%', #{searchText}, '%') " +
            "OR name LIKE CONCAT('%', #{searchText}, '%')) " +
            "</if>" +
            "<if test='buildingCode != null and !buildingCode.isEmpty()'>" +
            "AND building_code = #{buildingCode}" +
            "</if>" +
            "<if test='roomType != null'>AND room_type = #{roomType}</if> " +
            "<if test='minCapacity != null'>AND capacity >= #{minCapacity}</if> " +
            "<if test='maxCapacity != null'>AND capacity <= #{maxCapacity}</if> " +
            "<if test='hasProjector != null'>AND has_projector = #{hasProjector}</if> " +
            "<if test='hasComputer != null'>AND has_computer = #{hasComputer}</if> " +
            "<if test='hasWhiteboard != null'>AND has_whiteboard = #{hasWhiteboard}</if> " +
            "<if test='isAvailable != null'>AND is_available = #{isAvailable}</if> " +
            "ORDER BY building_code, room_number" +
            "</script>")
    @ResultMap("classroomResultMap")
    List<Classroom> searchClassrooms(@Param("searchText") String searchText,
                                    @Param("buildingCode") String buildingCode,
                                    @Param("roomType") String roomType,
                                    @Param("minCapacity") Integer minCapacity,
                                    @Param("maxCapacity") Integer maxCapacity,
                                    @Param("hasProjector") Boolean hasProjector,
                                    @Param("hasComputer") Boolean hasComputer,
                                    @Param("hasWhiteboard") Boolean hasWhiteboard,
                                    @Param("isAvailable") Boolean isAvailable);

    // Conflict Detection
    @Select("SELECT c.* FROM classrooms c " +
            "JOIN schedules s ON c.id = s.classroom_id " +
            "WHERE c.deleted_at IS NULL " +
            "AND s.deleted_at IS NULL " +
            "AND s.schedule_date = #{date} " +
            "AND s.time_slot_id = #{timeSlotId} " +
            "AND s.id != #{excludeScheduleId}")
    @ResultMap("classroomResultMap")
    List<Classroom> findConflictingClassrooms(@Param("date") String date,
                                             @Param("timeSlotId") Long timeSlotId,
                                             @Param("excludeScheduleId") Long excludeScheduleId);

    // Advanced Availability Queries
    @Select("SELECT c.* FROM classrooms c " +
            "WHERE c.deleted_at IS NULL " +
            "AND c.is_available = true " +
            "AND c.capacity >= #{minCapacity} " +
            "AND c.id NOT IN (" +
            "SELECT s.classroom_id FROM schedules s " +
            "WHERE s.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND s.time_slot_id = #{timeSlotId} " +
            "AND s.deleted_at IS NULL" +
            ")")
    @ResultMap("classroomResultMap")
    List<Classroom> findAvailableForRecurring(@Param("startDate") String startDate,
                                              @Param("endDate") String endDate,
                                              @Param("timeSlotId") Long timeSlotId,
                                              @Param("minCapacity") Integer minCapacity);

    // Batch Operations
    @Insert("<script>" +
            "INSERT INTO classrooms (building_code, room_number, name, capacity, room_type, " +
            "has_projector, has_computer, has_whiteboard, special_equipment, is_available, " +
            "created_at, updated_at) VALUES " +
            "<foreach collection='classrooms' item='classroom' separator=','>" +
            "(#{classroom.buildingCode}, #{classroom.roomNumber}, #{classroom.name}, " +
            "#{classroom.capacity}, #{classroom.roomType}, #{classroom.hasProjector}, " +
            "#{classroom.hasComputer}, #{classroom.hasWhiteboard}, #{classroom.specialEquipment}, " +
            "#{classroom.isAvailable}, #{classroom.createdAt}, #{classroom.updatedAt})" +
            "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("classrooms") List<Classroom> classrooms);

    @Update("<script>" +
            "<foreach collection='classrooms' item='classroom' separator=';'>" +
            "UPDATE classrooms SET building_code = #{classroom.buildingCode}, " +
            "room_number = #{classroom.roomNumber}, name = #{classroom.name}, " +
            "capacity = #{classroom.capacity}, room_type = #{classroom.roomType}, " +
            "has_projector = #{classroom.hasProjector}, has_computer = #{classroom.hasComputer}, " +
            "has_whiteboard = #{classroom.hasWhiteboard}, special_equipment = #{classroom.specialEquipment}, " +
            "is_available = #{classroom.isAvailable}, updated_at = #{classroom.updatedAt} " +
            "WHERE id = #{classroom.id}" +
            "</foreach>" +
            "</script>")
    int batchUpdate(@Param("classrooms") List<Classroom> classrooms);

    // Result classes for complex queries
    interface BuildingCapacityStats {
        String getBuildingCode();
        Integer getRoomCount();
        Integer getTotalCapacity();
        Double getAvgCapacity();
    }

    interface RoomTypeStats {
        String getRoomType();
        Integer getCount();
        Integer getMinCapacity();
        Integer getMaxCapacity();
        Double getAvgCapacity();
    }

    interface ClassroomUtilization {
        Long getId();
        String getBuildingCode();
        String getRoomNumber();
        String getName();
        Integer getCapacity();
        Integer getScheduleCount();
        Integer getDaysUsed();
        Integer getTimeSlotsUsed();
    }
}