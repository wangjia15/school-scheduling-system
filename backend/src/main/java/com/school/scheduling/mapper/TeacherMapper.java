package com.school.scheduling.mapper;

import com.school.scheduling.domain.Teacher;
import com.school.scheduling.domain.TeacherSpecialization;
import com.school.scheduling.domain.Department;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface TeacherMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO teachers (user_id, employee_id, department_id, title, max_weekly_hours, " +
            "max_courses_per_semester, office_location, phone, created_at, updated_at) " +
            "VALUES (#{userId}, #{employeeId}, #{departmentId}, #{title}, #{maxWeeklyHours}, " +
            "#{maxCoursesPerSemester}, #{officeLocation}, #{phone}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Teacher teacher);

    @Update("UPDATE teachers SET user_id = #{userId}, employee_id = #{employeeId}, " +
            "department_id = #{departmentId}, title = #{title}, max_weekly_hours = #{maxWeeklyHours}, " +
            "max_courses_per_semester = #{maxCoursesPerSemester}, office_location = #{officeLocation}, " +
            "phone = #{phone}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Teacher teacher);

    @Delete("UPDATE teachers SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Delete("DELETE FROM teachers WHERE id = #{id}")
    int hardDelete(@Param("id") Long id);

    @Select("SELECT * FROM teachers WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "teacherResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "employeeId", column = "employee_id"),
        @Result(property = "departmentId", column = "department_id"),
        @Result(property = "title", column = "title", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "maxWeeklyHours", column = "max_weekly_hours"),
        @Result(property = "maxCoursesPerSemester", column = "max_courses_per_semester"),
        @Result(property = "officeLocation", column = "office_location"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at"),
        @Result(property = "specializations", column = "id",
                many = @Many(select = "com.school.scheduling.mapper.TeacherSpecializationMapper.findByTeacherId",
                           fetchType = FetchType.LAZY))
    })
    Optional<Teacher> findById(@Param("id") Long id);

    @Select("SELECT * FROM teachers WHERE employee_id = #{employeeId} AND deleted_at IS NULL")
    @ResultMap("teacherResultMap")
    Optional<Teacher> findByEmployeeId(@Param("employeeId") String employeeId);

    @Select("SELECT * FROM teachers WHERE user_id = #{userId} AND deleted_at IS NULL")
    @ResultMap("teacherResultMap")
    Optional<Teacher> findByUserId(@Param("userId") Long userId);

    @Select("<script>" +
            "SELECT * FROM teachers WHERE deleted_at IS NULL " +
            "<if test='departmentId != null'>AND department_id = #{departmentId}</if> " +
            "<if test='title != null'>AND title = #{title}</if> " +
            "<if test='isActive != null'>AND is_active = #{isActive}</if> " +
            "ORDER BY created_at DESC" +
            "</script>")
    @ResultMap("teacherResultMap")
    List<Teacher> findByCriteria(@Param("departmentId") Long departmentId,
                                @Param("title") String title,
                                @Param("isActive") Boolean isActive);

    @Select("SELECT * FROM teachers WHERE deleted_at IS NULL ORDER BY created_at DESC")
    @ResultMap("teacherResultMap")
    List<Teacher> findAll();

    @Select("SELECT * FROM teachers WHERE deleted_at IS NULL AND is_active = true ORDER BY created_at DESC")
    @ResultMap("teacherResultMap")
    List<Teacher> findAllActive();

    @Select("SELECT COUNT(*) FROM teachers WHERE deleted_at IS NULL")
    long countAll();

    // Specialization Methods
    @Select("SELECT * FROM teacher_specializations WHERE teacher_id = #{teacherId} AND deleted_at IS NULL")
    @Results(id = "teacherSpecializationResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "teacherId", column = "teacher_id"),
        @Result(property = "subjectCode", column = "subject_code"),
        @Result(property = "proficiencyLevel", column = "proficiency_level", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "yearsExperience", column = "years_experience"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<TeacherSpecialization> findSpecializationsByTeacherId(@Param("teacherId") Long teacherId);

    // Availability and Workload Queries
    @Select("SELECT COUNT(*) FROM schedules s " +
            "JOIN course_offerings co ON s.course_offering_id = co.id " +
            "WHERE co.teacher_id = #{teacherId} " +
            "AND s.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND s.deleted_at IS NULL")
    int countScheduledClasses(@Param("teacherId") Long teacherId,
                             @Param("startDate") String startDate,
                             @Param("endDate") String endDate);

    @Select("SELECT SUM(c.contact_hours_per_week) FROM schedules s " +
            "JOIN course_offerings co ON s.course_offering_id = co.id " +
            "JOIN courses c ON co.course_id = c.id " +
            "WHERE co.teacher_id = #{teacherId} " +
            "AND s.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND s.deleted_at IS NULL")
    BigDecimal calculateTotalWeeklyHours(@Param("teacherId") Long teacherId,
                                        @Param("startDate") String startDate,
                                        @Param("endDate") String endDate);

    @Select("SELECT COUNT(DISTINCT co.id) FROM schedules s " +
            "JOIN course_offerings co ON s.course_offering_id = co.id " +
            "JOIN semesters se ON co.semester_id = se.id " +
            "WHERE co.teacher_id = #{teacherId} " +
            "AND se.is_current = true " +
            "AND s.deleted_at IS NULL")
    int countCurrentSemesterCourses(@Param("teacherId") Long teacherId);

    // Advanced Scheduling Queries
    @Select("SELECT t.* FROM teachers t " +
            "LEFT JOIN schedules s ON t.id = (SELECT teacher_id FROM course_offerings WHERE id = s.course_offering_id) " +
            "LEFT JOIN time_slots ts ON s.time_slot_id = ts.id " +
            "WHERE t.deleted_at IS NULL " +
            "AND (s.id IS NULL OR s.schedule_date != #{date} OR ts.id != #{timeSlotId}) " +
            "AND t.max_weekly_hours >= (SELECT COALESCE(SUM(c.contact_hours_per_week), 0) " +
            "FROM schedules s2 " +
            "JOIN course_offerings co2 ON s2.course_offering_id = co2.id " +
            "JOIN courses c ON co2.course_id = c.id " +
            "WHERE co2.teacher_id = t.id AND s2.schedule_date = #{date} AND s2.deleted_at IS NULL)")
    @ResultMap("teacherResultMap")
    List<Teacher> findAvailableTeachers(@Param("date") String date, @Param("timeSlotId") Long timeSlotId);

    @Select("SELECT t.* FROM teachers t " +
            "JOIN teacher_specializations ts ON t.id = ts.teacher_id " +
            "WHERE ts.subject_code = #{subjectCode} " +
            "AND t.deleted_at IS NULL " +
            "AND ts.proficiency_level = #{proficiencyLevel}")
    @ResultMap("teacherResultMap")
    List<Teacher> findBySubjectAndProficiency(@Param("subjectCode") String subjectCode,
                                              @Param("proficiencyLevel") String proficiencyLevel);

    @Select("SELECT t.* FROM teachers t " +
            "JOIN departments d ON t.department_id = d.id " +
            "WHERE d.id = #{departmentId} " +
            "AND t.max_courses_per_semester > (SELECT COUNT(DISTINCT co.id) " +
            "FROM course_offerings co " +
            "JOIN semesters s ON co.semester_id = s.id " +
            "WHERE co.teacher_id = t.id AND s.is_current = true) " +
            "AND t.deleted_at IS NULL")
    @ResultMap("teacherResultMap")
    List<Teacher> findAvailableByDepartment(@Param("departmentId") Long departmentId);

    // Search and Filter Methods
    @Select("<script>" +
            "SELECT DISTINCT t.* FROM teachers t " +
            "LEFT JOIN users u ON t.user_id = u.id " +
            "LEFT JOIN departments d ON t.department_id = d.id " +
            "LEFT JOIN teacher_specializations ts ON t.id = ts.teacher_id " +
            "WHERE t.deleted_at IS NULL " +
            "<if test='searchText != null and !searchText.isEmpty()'>" +
            "AND (u.first_name LIKE CONCAT('%', #{searchText}, '%') " +
            "OR u.last_name LIKE CONCAT('%', #{searchText}, '%') " +
            "OR t.employee_id LIKE CONCAT('%', #{searchText}, '%') " +
            "OR ts.subject_code LIKE CONCAT('%', #{searchText}, '%')) " +
            "</if>" +
            "<if test='departmentId != null'>AND t.department_id = #{departmentId}</if> " +
            "<if test='subjectCode != null'>AND ts.subject_code = #{subjectCode}</if> " +
            "<if test='minExperience != null'>AND ts.years_experience >= #{minExperience}</if> " +
            "<if test='title != null'>AND t.title = #{title}</if> " +
            "ORDER BY t.created_at DESC" +
            "</script>")
    @ResultMap("teacherResultMap")
    List<Teacher> searchTeachers(@Param("searchText") String searchText,
                                @Param("departmentId") Long departmentId,
                                @Param("subjectCode") String subjectCode,
                                @Param("minExperience") Integer minExperience,
                                @Param("title") String title);

    // Statistics and Reporting
    @Select("SELECT d.name as department_name, t.title, COUNT(*) as count " +
            "FROM teachers t " +
            "JOIN departments d ON t.department_id = d.id " +
            "WHERE t.deleted_at IS NULL " +
            "GROUP BY d.name, t.title " +
            "ORDER BY d.name, t.title")
    List<Department> getTeacherCountByDepartmentAndTitle();

    @Select("SELECT AVG(t.max_weekly_hours) as avg_hours, AVG(t.max_courses_per_semester) as avg_courses " +
            "FROM teachers t WHERE t.deleted_at IS NULL")
    TeacherStatistics getTeacherStatistics();

    // Batch Operations
    @Insert("<script>" +
            "INSERT INTO teachers (user_id, employee_id, department_id, title, max_weekly_hours, " +
            "max_courses_per_semester, office_location, phone, created_at, updated_at) VALUES " +
            "<foreach collection='teachers' item='teacher' separator=','>" +
            "(#{teacher.userId}, #{teacher.employeeId}, #{teacher.departmentId}, #{teacher.title}, " +
            "#{teacher.maxWeeklyHours}, #{teacher.maxCoursesPerSemester}, #{teacher.officeLocation}, " +
            "#{teacher.phone}, #{teacher.createdAt}, #{teacher.updatedAt})" +
            "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("teachers") List<Teacher> teachers);

    @Update("<script>" +
            "<foreach collection='teachers' item='teacher' separator=';'>" +
            "UPDATE teachers SET user_id = #{teacher.userId}, employee_id = #{teacher.employeeId}, " +
            "department_id = #{teacher.departmentId}, title = #{teacher.title}, " +
            "max_weekly_hours = #{teacher.maxWeeklyHours}, max_courses_per_semester = #{teacher.maxCoursesPerSemester}, " +
            "office_location = #{teacher.officeLocation}, phone = #{teacher.phone}, " +
            "updated_at = #{teacher.updatedAt} WHERE id = #{teacher.id}" +
            "</foreach>" +
            "</script>")
    int batchUpdate(@Param("teachers") List<Teacher> teachers);

    // Export support methods
    @Select("<script>" +
            "SELECT * FROM teachers WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "AND deleted_at IS NULL" +
            "</script>")
    @ResultMap("teacherResultMap")
    List<Teacher> findByIds(@Param("ids") List<Long> ids);

    // Custom result class for statistics
    interface TeacherStatistics {
        BigDecimal getAvgHours();
        BigDecimal getAvgCourses();
    }
}