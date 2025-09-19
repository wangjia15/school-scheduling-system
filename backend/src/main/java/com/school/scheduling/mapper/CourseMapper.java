package com.school.scheduling.mapper;

import com.school.scheduling.domain.Course;
import com.school.scheduling.domain.CoursePrerequisite;
import com.school.scheduling.domain.CourseOffering;
import com.school.scheduling.domain.Department;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface CourseMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO courses (course_code, title, description, department_id, credits, " +
            "contact_hours_per_week, theory_hours, lab_hours, level, is_active, max_students, " +
            "min_students, requires_lab, created_at, updated_at) " +
            "VALUES (#{courseCode}, #{title}, #{description}, #{departmentId}, #{credits}, " +
            "#{contactHoursPerWeek}, #{theoryHours}, #{labHours}, #{level}, #{isActive}, " +
            "#{maxStudents}, #{minStudents}, #{requiresLab}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course course);

    @Update("UPDATE courses SET course_code = #{courseCode}, title = #{title}, " +
            "description = #{description}, department_id = #{departmentId}, credits = #{credits}, " +
            "contact_hours_per_week = #{contactHoursPerWeek}, theory_hours = #{theoryHours}, " +
            "lab_hours = #{labHours}, level = #{level}, is_active = #{isActive}, " +
            "max_students = #{maxStudents}, min_students = #{minStudents}, " +
            "requires_lab = #{requiresLab}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Course course);

    @Delete("UPDATE courses SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Delete("DELETE FROM courses WHERE id = #{id}")
    int hardDelete(@Param("id") Long id);

    @Select("SELECT * FROM courses WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "courseResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "courseCode", column = "course_code"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "departmentId", column = "department_id"),
        @Result(property = "credits", column = "credits"),
        @Result(property = "contactHoursPerWeek", column = "contact_hours_per_week"),
        @Result(property = "theoryHours", column = "theory_hours"),
        @Result(property = "labHours", column = "lab_hours"),
        @Result(property = "level", column = "level", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "isActive", column = "is_active"),
        @Result(property = "maxStudents", column = "max_students"),
        @Result(property = "minStudents", column = "min_students"),
        @Result(property = "requiresLab", column = "requires_lab"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<Course> findById(@Param("id") Long id);

    @Select("SELECT * FROM courses WHERE course_code = #{courseCode} AND deleted_at IS NULL")
    @ResultMap("courseResultMap")
    Optional<Course> findByCourseCode(@Param("courseCode") String courseCode);

    @Select("SELECT * FROM courses WHERE department_id = #{departmentId} AND deleted_at IS NULL ORDER BY course_code")
    @ResultMap("courseResultMap")
    List<Course> findByDepartmentId(@Param("departmentId") Long departmentId);

    @Select("<script>" +
            "SELECT * FROM courses WHERE deleted_at IS NULL " +
            "<if test='level != null'>AND level = #{level}</if> " +
            "<if test='isActive != null'>AND is_active = #{isActive}</if> " +
            "<if test='requiresLab != null'>AND requires_lab = #{requiresLab}</if> " +
            "<if test='minCredits != null'>AND credits >= #{minCredits}</if> " +
            "<if test='maxCredits != null'>AND credits <= #{maxCredits}</if> " +
            "ORDER BY course_code" +
            "</script>")
    @ResultMap("courseResultMap")
    List<Course> findByCriteria(@Param("level") String level,
                               @Param("isActive") Boolean isActive,
                               @Param("requiresLab") Boolean requiresLab,
                               @Param("minCredits") Integer minCredits,
                               @Param("maxCredits") Integer maxCredits);

    @Select("SELECT * FROM courses WHERE deleted_at IS NULL ORDER BY course_code")
    @ResultMap("courseResultMap")
    List<Course> findAll();

    @Select("SELECT COUNT(*) FROM courses WHERE deleted_at IS NULL")
    long countAll();

    // Prerequisite Methods
    @Select("SELECT * FROM course_prerequisites WHERE course_id = #{courseId} AND deleted_at IS NULL")
    @Results(id = "coursePrerequisiteResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "courseId", column = "course_id"),
        @Result(property = "prerequisiteCourseId", column = "prerequisite_course_id"),
        @Result(property = "isMandatory", column = "is_mandatory"),
        @Result(property = "minimumGrade", column = "minimum_grade"),
        @Result(property = "createdAt", column = "created_at")
    })
    List<CoursePrerequisite> findPrerequisitesByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT cp.* FROM course_prerequisites cp " +
            "JOIN courses c ON cp.course_id = c.id " +
            "WHERE cp.prerequisite_course_id = #{prerequisiteCourseId} " +
            "AND cp.deleted_at IS NULL AND c.deleted_at IS NULL")
    @ResultMap("coursePrerequisiteResultMap")
    List<CoursePrerequisite> findCoursesThatRequireThisPrerequisite(@Param("prerequisiteCourseId") Long prerequisiteCourseId);

    @Select("SELECT c.* FROM courses c " +
            "JOIN course_prerequisites cp ON c.id = cp.course_id " +
            "WHERE cp.prerequisite_course_id = #{prerequisiteCourseId} " +
            "AND cp.is_mandatory = true " +
            "AND c.deleted_at IS NULL AND cp.deleted_at IS NULL")
    @ResultMap("courseResultMap")
    List<Course> findMandatoryPrerequisiteCourses(@Param("prerequisiteCourseId") Long prerequisiteCourseId);

    // Prerequisite Validation Queries
    @Select("SELECT COUNT(*) FROM course_prerequisites cp " +
            "JOIN courses c ON cp.prerequisite_course_id = c.id " +
            "WHERE cp.course_id = #{courseId} " +
            "AND cp.deleted_at IS NULL AND c.deleted_at IS NULL")
    int countPrerequisites(@Param("courseId") Long courseId);

    @Select("SELECT EXISTS (" +
            "SELECT 1 FROM course_prerequisites cp " +
            "JOIN courses c ON cp.prerequisite_course_id = c.id " +
            "WHERE cp.course_id = #{courseId} " +
            "AND cp.is_mandatory = true " +
            "AND cp.deleted_at IS NULL AND c.deleted_at IS NULL" +
            ")")
    boolean hasMandatoryPrerequisites(@Param("courseId") Long courseId);

    @Select("SELECT c.* FROM courses c " +
            "WHERE c.id IN (" +
            "SELECT cp.prerequisite_course_id FROM course_prerequisites cp " +
            "WHERE cp.course_id = #{courseId} AND cp.deleted_at IS NULL" +
            ") " +
            "AND c.deleted_at IS NULL " +
            "ORDER BY c.course_code")
    @ResultMap("courseResultMap")
    List<Course> getPrerequisiteCourses(@Param("courseId") Long courseId);

    // Course Offering Methods
    @Select("SELECT * FROM course_offerings WHERE course_id = #{courseId} AND deleted_at IS NULL " +
            "ORDER BY created_at DESC")
    List<CourseOffering> findOfferingsByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT COUNT(*) FROM course_offerings " +
            "WHERE course_id = #{courseId} AND deleted_at IS NULL")
    int countOfferings(@Param("courseId") Long courseId);

    @Select("SELECT COUNT(*) FROM enrollments e " +
            "JOIN course_offerings co ON e.course_offering_id = co.id " +
            "WHERE co.course_id = #{courseId} " +
            "AND e.status = 'ENROLLED' " +
            "AND e.deleted_at IS NULL AND co.deleted_at IS NULL")
    int countTotalEnrollments(@Param("courseId") Long courseId);

    // Advanced Scheduling Queries
    @Select("SELECT c.* FROM courses c " +
            "WHERE c.deleted_at IS NULL " +
            "AND c.is_active = true " +
            "AND c.requires_lab = #{requiresLab} " +
            "AND c.max_students >= #{minCapacity} " +
            "AND NOT EXISTS (" +
            "SELECT 1 FROM course_prerequisites cp " +
            "WHERE cp.course_id = c.id " +
            "AND cp.is_mandatory = true " +
            "AND cp.deleted_at IS NULL" +
            ")")
    @ResultMap("courseResultMap")
    List<Course> findCoursesWithoutMandatoryPrerequisites(@Param("requiresLab") Boolean requiresLab,
                                                           @Param("minCapacity") Integer minCapacity);

    @Select("SELECT c.* FROM courses c " +
            "LEFT JOIN course_offerings co ON c.id = co.course_id AND co.deleted_at IS NULL " +
            "WHERE c.deleted_at IS NULL " +
            "AND c.is_active = true " +
            "AND c.level = #{level} " +
            "AND (co.id IS NULL OR co.current_enrollment < c.max_students) " +
            "GROUP BY c.id " +
            "HAVING COUNT(co.id) < #{maxOfferings}")
    @ResultMap("courseResultMap")
    List<Course> findAvailableCoursesForLevel(@Param("level") String level,
                                              @Param("maxOfferings") Integer maxOfferings);

    @Select("SELECT c.* FROM courses c " +
            "JOIN departments d ON c.department_id = d.id " +
            "WHERE c.deleted_at IS NULL " +
            "AND c.is_active = true " +
            "AND c.max_students > #{currentEnrollment} " +
            "AND c.min_students <= #{currentEnrollment} " +
            "AND d.id = #{departmentId}")
    @ResultMap("courseResultMap")
    List<Course> findCoursesByDepartmentAndCapacity(@Param("departmentId") Long departmentId,
                                                   @Param("currentEnrollment") Integer currentEnrollment);

    // Search and Filter Methods
    @Select("<script>" +
            "SELECT c.* FROM courses c " +
            "LEFT JOIN departments d ON c.department_id = d.id " +
            "WHERE c.deleted_at IS NULL " +
            "<if test='searchText != null and !searchText.isEmpty()'>" +
            "AND (c.course_code LIKE CONCAT('%', #{searchText}, '%') " +
            "OR c.title LIKE CONCAT('%', #{searchText}, '%') " +
            "OR c.description LIKE CONCAT('%', #{searchText}, '%')) " +
            "</if>" +
            "<if test='departmentId != null'>AND c.department_id = #{departmentId}</if> " +
            "<if test='level != null'>AND c.level = #{level}</if> " +
            "<if test='minCredits != null'>AND c.credits >= #{minCredits}</if> " +
            "<if test='maxCredits != null'>AND c.credits <= #{maxCredits}</if> " +
            "<if test='requiresLab != null'>AND c.requires_lab = #{requiresLab}</if> " +
            "<if test='isActive != null'>AND c.is_active = #{isActive}</if> " +
            "ORDER BY c.course_code" +
            "</script>")
    @ResultMap("courseResultMap")
    List<Course> searchCourses(@Param("searchText") String searchText,
                              @Param("departmentId") Long departmentId,
                              @Param("level") String level,
                              @Param("minCredits") Integer minCredits,
                              @Param("maxCredits") Integer maxCredits,
                              @Param("requiresLab") Boolean requiresLab,
                              @Param("isActive") Boolean isActive);

    // Course Statistics and Analytics
    @Select("SELECT c.level, COUNT(*) as course_count, AVG(c.credits) as avg_credits, " +
            "AVG(c.contact_hours_per_week) as avg_hours FROM courses c " +
            "WHERE c.deleted_at IS NULL " +
            "GROUP BY c.level " +
            "ORDER BY c.level")
    List<CourseStatistics> getCourseStatisticsByLevel();

    @Select("SELECT d.name as department_name, COUNT(c.id) as course_count, " +
            "AVG(c.credits) as avg_credits, " +
            "SUM(CASE WHEN c.requires_lab = true THEN 1 ELSE 0 END) as lab_course_count " +
            "FROM courses c " +
            "JOIN departments d ON c.department_id = d.id " +
            "WHERE c.deleted_at IS NULL " +
            "GROUP BY d.name " +
            "ORDER BY d.name")
    List<DepartmentCourseStatistics> getCourseStatisticsByDepartment();

    // Capacity and Enrollment Analysis
    @Select("SELECT c.id, c.course_code, c.title, c.max_students, " +
            "COALESCE(AVG(co.current_enrollment), 0) as avg_enrollment, " +
            "COALESCE(MAX(co.current_enrollment), 0) as max_enrollment, " +
            "COUNT(co.id) as offering_count " +
            "FROM courses c " +
            "LEFT JOIN course_offerings co ON c.id = co.course_id AND co.deleted_at IS NULL " +
            "WHERE c.deleted_at IS NULL " +
            "GROUP BY c.id, c.course_code, c.title, c.max_students " +
            "HAVING COUNT(co.id) > 0 " +
            "ORDER BY c.course_code")
    List<CourseCapacityAnalysis> getCourseCapacityAnalysis();

    // Batch Operations
    @Insert("<script>" +
            "INSERT INTO courses (course_code, title, description, department_id, credits, " +
            "contact_hours_per_week, theory_hours, lab_hours, level, is_active, max_students, " +
            "min_students, requires_lab, created_at, updated_at) VALUES " +
            "<foreach collection='courses' item='course' separator=','>" +
            "(#{course.courseCode}, #{course.title}, #{course.description}, #{course.departmentId}, " +
            "#{course.credits}, #{course.contactHoursPerWeek}, #{course.theoryHours}, #{course.labHours}, " +
            "#{course.level}, #{course.isActive}, #{course.maxStudents}, #{course.minStudents}, " +
            "#{course.requiresLab}, #{course.createdAt}, #{course.updatedAt})" +
            "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("courses") List<Course> courses);

    @Update("<script>" +
            "<foreach collection='courses' item='course' separator=';'>" +
            "UPDATE courses SET course_code = #{course.courseCode}, title = #{course.title}, " +
            "description = #{course.description}, department_id = #{course.departmentId}, " +
            "credits = #{course.credits}, contact_hours_per_week = #{course.contactHoursPerWeek}, " +
            "theory_hours = #{course.theoryHours}, lab_hours = #{course.labHours}, " +
            "level = #{course.level}, is_active = #{course.isActive}, " +
            "max_students = #{course.maxStudents}, min_students = #{course.minStudents}, " +
            "requires_lab = #{course.requiresLab}, updated_at = #{course.updatedAt} " +
            "WHERE id = #{course.id}" +
            "</foreach>" +
            "</script>")
    int batchUpdate(@Param("courses") List<Course> courses);

    // Complex Prerequisite Validation
    @Select("SELECT COUNT(*) FROM course_prerequisites cp " +
            "JOIN courses c ON cp.prerequisite_course_id = c.id " +
            "WHERE cp.course_id = #{courseId} " +
            "AND cp.is_mandatory = true " +
            "AND cp.deleted_at IS NULL AND c.deleted_at IS NULL " +
            "AND NOT EXISTS (" +
            "SELECT 1 FROM enrollments e " +
            "JOIN course_offerings co ON e.course_offering_id = co.id " +
            "WHERE co.course_id = c.id " +
            "AND e.student_id = #{studentId} " +
            "AND e.status = 'COMPLETED' " +
            "AND (e.grade IS NULL OR e.grade >= cp.minimum_grade) " +
            "AND e.deleted_at IS NULL AND co.deleted_at IS NULL" +
            ")")
    int countUnmetMandatoryPrerequisites(@Param("courseId") Long courseId,
                                       @Param("studentId") Long studentId);

    // Course Similarity and Recommendations
    @Select("SELECT c2.* FROM courses c1 " +
            "JOIN courses c2 ON c1.department_id = c2.department_id " +
            "WHERE c1.id = #{courseId} " +
            "AND c2.id != #{courseId} " +
            "AND c2.deleted_at IS NULL " +
            "AND c2.is_active = true " +
            "AND c2.level = c1.level " +
            "AND ABS(c2.credits - c1.credits) <= 1 " +
            "ORDER BY c2.course_code " +
            "LIMIT 5")
    @ResultMap("courseResultMap")
    List<Course> findSimilarCourses(@Param("courseId") Long courseId);

    // Result classes for complex queries
    interface CourseStatistics {
        String getLevel();
        Integer getCourseCount();
        BigDecimal getAvgCredits();
        BigDecimal getAvgHours();
    }

    interface DepartmentCourseStatistics {
        String getDepartmentName();
        Integer getCourseCount();
        BigDecimal getAvgCredits();
        Integer getLabCourseCount();
    }

    interface CourseCapacityAnalysis {
        Long getId();
        String getCourseCode();
        String getTitle();
        Integer getMaxStudents();
        BigDecimal getAvgEnrollment();
        Integer getMaxEnrollment();
        Integer getOfferingCount();
    }
}