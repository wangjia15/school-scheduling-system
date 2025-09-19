package com.school.scheduling.mapper;

import com.school.scheduling.domain.AuditLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AuditLogMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO audit_log (table_name, record_id, action, old_values, " +
            "new_values, user_id, action_timestamp, ip_address, user_agent, " +
            "created_at, updated_at) " +
            "VALUES (#{tableName}, #{recordId}, #{action}, #{oldValues}, #{newValues}, " +
            "#{userId}, #{actionTimestamp}, #{ipAddress}, #{userAgent}, " +
            "#{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditLog auditLog);

    @Update("UPDATE audit_log SET table_name = #{tableName}, record_id = #{recordId}, " +
            "action = #{action}, old_values = #{oldValues}, new_values = #{newValues}, " +
            "user_id = #{userId}, action_timestamp = #{actionTimestamp}, ip_address = #{ipAddress}, " +
            "user_agent = #{userAgent}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(AuditLog auditLog);

    @Delete("UPDATE audit_log SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Select("SELECT * FROM audit_log WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "auditLogResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "tableName", column = "table_name"),
        @Result(property = "recordId", column = "record_id"),
        @Result(property = "action", column = "action", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "oldValues", column = "old_values"),
        @Result(property = "newValues", column = "new_values"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "actionTimestamp", column = "action_timestamp"),
        @Result(property = "ipAddress", column = "ip_address"),
        @Result(property = "userAgent", column = "user_agent"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<AuditLog> findById(@Param("id") Long id);

    @Select("SELECT * FROM audit_log WHERE table_name = #{tableName} " +
            "AND deleted_at IS NULL ORDER BY action_timestamp DESC")
    @ResultMap("auditLogResultMap")
    List<AuditLog> findByTableName(@Param("tableName") String tableName);

    @Select("SELECT * FROM audit_log WHERE record_id = #{recordId} " +
            "AND deleted_at IS NULL ORDER BY action_timestamp DESC")
    @ResultMap("auditLogResultMap")
    List<AuditLog> findByRecordId(@Param("recordId") Long recordId);

    @Select("SELECT * FROM audit_log WHERE user_id = #{userId} " +
            "AND deleted_at IS NULL ORDER BY action_timestamp DESC")
    @ResultMap("auditLogResultMap")
    List<AuditLog> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM audit_log WHERE action = #{action} " +
            "AND deleted_at IS NULL ORDER BY action_timestamp DESC")
    @ResultMap("auditLogResultMap")
    List<AuditLog> findByAction(@Param("action") String action);

    @Select("SELECT * FROM audit_log WHERE action_timestamp >= #{startTime} " +
            "AND action_timestamp <= #{endTime} AND deleted_at IS NULL " +
            "ORDER BY action_timestamp DESC")
    @ResultMap("auditLogResultMap")
    List<AuditLog> findByActionTimestampRange(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM audit_log WHERE ip_address = #{ipAddress} " +
            "AND deleted_at IS NULL ORDER BY action_timestamp DESC")
    @ResultMap("auditLogResultMap")
    List<AuditLog> findByIpAddress(@Param("ipAddress") String ipAddress);

    @Select("<script>" +
            "SELECT * FROM audit_log WHERE deleted_at IS NULL " +
            "<if test='tableName != null and !tableName.isEmpty()'>AND table_name = #{tableName}</if> " +
            "<if test='recordId != null'>AND record_id = #{recordId}</if> " +
            "<if test='action != null'>AND action = #{action}</if> " +
            "<if test='userId != null'>AND user_id = #{userId}</if> " +
            "<if test='ipAddress != null and !ipAddress.isEmpty()'>AND ip_address = #{ipAddress}</if> " +
            "<if test='startTime != null'>AND action_timestamp >= #{startTime}</if> " +
            "<if test='endTime != null'>AND action_timestamp <= #{endTime}</if> " +
            "<if test='hasOldData != null'>" +
            "<choose>" +
            "<when test='hasOldData'>AND old_values IS NOT NULL</when>" +
            "<otherwise>AND old_values IS NULL</otherwise>" +
            "</choose></if> " +
            "<if test='hasNewData != null'>" +
            "<choose>" +
            "<when test='hasNewData'>AND new_values IS NOT NULL</when>" +
            "<otherwise>AND new_values IS NULL</otherwise>" +
            "</choose></if> " +
            "ORDER BY action_timestamp DESC" +
            "</script>")
    @ResultMap("auditLogResultMap")
    List<AuditLog> findByCriteria(@Param("tableName") String tableName,
                                  @Param("recordId") Long recordId,
                                  @Param("action") String action,
                                  @Param("userId") Long userId,
                                  @Param("ipAddress") String ipAddress,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime,
                                  @Param("hasOldData") Boolean hasOldData,
                                  @Param("hasNewData") Boolean hasNewData);

    @Select("SELECT COUNT(*) FROM audit_log WHERE table_name = #{tableName} " +
            "AND deleted_at IS NULL")
    int countByTableName(@Param("tableName") String tableName);

    @Select("SELECT COUNT(*) FROM audit_log WHERE user_id = #{userId} " +
            "AND deleted_at IS NULL")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM audit_log WHERE action = #{action} " +
            "AND deleted_at IS NULL")
    int countByAction(@Param("action") String action);

    @Select("SELECT COUNT(*) FROM audit_log WHERE action_timestamp >= #{startTime} " +
            "AND action_timestamp <= #{endTime} AND deleted_at IS NULL")
    int countByActionTimestampRange(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    @Select("SELECT table_name, COUNT(*) as count " +
            "FROM audit_log WHERE deleted_at IS NULL " +
            "GROUP BY table_name ORDER BY count DESC")
    List<TableCount> getTableActivityDistribution();

    @Select("SELECT action, COUNT(*) as count " +
            "FROM audit_log WHERE deleted_at IS NULL " +
            "GROUP BY action ORDER BY count DESC")
    List<ActionCount> getActionDistribution();

    @Select("SELECT user_id, COUNT(*) as count " +
            "FROM audit_log WHERE deleted_at IS NULL " +
            "GROUP BY user_id ORDER BY count DESC")
    List<UserCount> getUserActivityDistribution();

    @Select("SELECT ip_address, COUNT(*) as count " +
            "FROM audit_log WHERE deleted_at IS NULL AND ip_address IS NOT NULL " +
            "GROUP BY ip_address ORDER BY count DESC")
    List<IpCount> getIpAddressDistribution();

    @Select("SELECT DATE(action_timestamp) as action_date, COUNT(*) as count " +
            "FROM audit_log WHERE deleted_at IS NULL " +
            "GROUP BY DATE(action_timestamp) ORDER BY action_date DESC")
    List<DailyCount> getDailyActivityCounts();

    @Select("SELECT HOUR(action_timestamp) as action_hour, COUNT(*) as count " +
            "FROM audit_log WHERE deleted_at IS NULL " +
            "AND DATE(action_timestamp) = CURRENT_DATE " +
            "GROUP BY HOUR(action_timestamp) ORDER BY action_hour")
    List<HourlyCount> getTodayHourlyActivity();

    // Result classes for distribution queries
    class TableCount {
        private String tableName;
        private Integer count;

        // Getters and setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    class ActionCount {
        private String action;
        private Integer count;

        // Getters and setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    class UserCount {
        private Long userId;
        private Integer count;

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    class IpCount {
        private String ipAddress;
        private Integer count;

        // Getters and setters
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    class DailyCount {
        private String actionDate;
        private Integer count;

        // Getters and setters
        public String getActionDate() { return actionDate; }
        public void setActionDate(String actionDate) { this.actionDate = actionDate; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    class HourlyCount {
        private Integer actionHour;
        private Integer count;

        // Getters and setters
        public Integer getActionHour() { return actionHour; }
        public void setActionHour(Integer actionHour) { this.actionHour = actionHour; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}