package com.school.scheduling.mapper;

import com.school.scheduling.domain.CoursePrerequisite;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface CoursePrerequisiteMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO course_prerequisites (course_id, prerequisite_course_id, is_mandatory, " +
            "minimum_grade, created_at, updated_at) " +
            "VALUES (#{courseId}, #{prerequisiteCourseId}, #{isMandatory}, " +
            "#{minimumGrade}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CoursePrerequisite coursePrerequisite);

    @Update("UPDATE course_prerequisites SET course_id = #{courseId}, prerequisite_course_id = #{prerequisiteCourseId}, " +
            "is_mandatory = #{isMandatory}, minimum_grade = #{minimumGrade}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(CoursePrerequisite coursePrerequisite);

    @Delete("UPDATE course_prerequisites SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Select("SELECT * FROM course_prerequisites WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "coursePrerequisiteResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "courseId", column = "course_id"),
        @Result(property = "prerequisiteCourseId", column = "prerequisite_course_id"),
        @Result(property = "isMandatory", column = "is_mandatory"),
        @Result(property = "minimumGrade", column = "minimum_grade"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<CoursePrerequisite> findById(@Param("id") Long id);

    @Select("SELECT * FROM course_prerequisites WHERE course_id = #{courseId} " +
            "AND prerequisite_course_id = #{prerequisiteCourseId} AND deleted_at IS NULL")
    @ResultMap("coursePrerequisiteResultMap")
    Optional<CoursePrerequisite> findByCourseAndPrerequisite(@Param("courseId") Long courseId,
                                                             @Param("prerequisiteCourseId") Long prerequisiteCourseId);

    @Select("SELECT * FROM course_prerequisites WHERE course_id = #{courseId} AND deleted_at IS NULL " +
            "ORDER BY is_mandatory DESC, minimum_grade DESC")
    @ResultMap("coursePrerequisiteResultMap")
    List<CoursePrerequisite> findByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT * FROM course_prerequisites WHERE prerequisite_course_id = #{prerequisiteCourseId} " +
            "AND deleted_at IS NULL ORDER BY course_id")
    @ResultMap("coursePrerequisiteResultMap")
    List<CoursePrerequisite> findByPrerequisiteCourseId(@Param("prerequisiteCourseId") Long prerequisiteCourseId);

    @Select("SELECT * FROM course_prerequisites WHERE is_mandatory = true AND deleted_at IS NULL")
    @ResultMap("coursePrerequisiteResultMap")
    List<CoursePrerequisite> findMandatoryPrerequisites();

    @Select("SELECT * FROM course_prerequisites WHERE is_mandatory = false AND deleted_at IS NULL")
    @ResultMap("coursePrerequisiteResultMap")
    List<CoursePrerequisite> findOptionalPrerequisites();

    @Select("SELECT * FROM course_prerequisites WHERE minimum_grade >= #{minimumGrade} " +
            "AND deleted_at IS NULL ORDER BY minimum_grade DESC")
    @ResultMap("coursePrerequisiteResultMap")
    List<CoursePrerequisite> findByMinimumGradeOrHigher(@Param("minimumGrade") BigDecimal minimumGrade);

    @Select("SELECT * FROM course_prerequisites WHERE minimum_grade IS NOT NULL " +
            "AND minimum_grade > #{minimumGrade} AND deleted_at IS NULL " +
            "ORDER BY minimum_grade DESC")
    @ResultMap("coursePrerequisiteResultMap")
    List<CoursePrerequisite> findWithGradeRequirementsAbove(@Param("minimumGrade") BigDecimal minimumGrade);

    @Select("<script>" +
            "SELECT * FROM course_prerequisites WHERE deleted_at IS NULL " +
            "<if test='courseId != null'>AND course_id = #{courseId}</if> " +
            "<if test='prerequisiteCourseId != null'>AND prerequisite_course_id = #{prerequisiteCourseId}</if> " +
            "<if test='isMandatory != null'>AND is_mandatory = #{isMandatory}</if> " +
            "<if test='hasGradeRequirement != null'>AND " +
            "<choose>" +
            "<when test='hasGradeRequirement'>minimum_grade IS NOT NULL</when>" +
            "<otherwise>minimum_grade IS NULL</otherwise>" +
            "</choose></if> " +
            "<if test='minGrade != null'>AND minimum_grade >= #{minGrade}</if> " +
            "ORDER BY is_mandatory DESC, minimum_grade DESC" +
            "</script>")
    @ResultMap("coursePrerequisiteResultMap")
    List<CoursePrerequisite> findByCriteria(@Param("courseId") Long courseId,
                                           @Param("prerequisiteCourseId") Long prerequisiteCourseId,
                                           @Param("isMandatory") Boolean isMandatory,
                                           @Param("hasGradeRequirement") Boolean hasGradeRequirement,
                                           @Param("minGrade") BigDecimal minGrade);

    @Select("SELECT COUNT(*) FROM course_prerequisites WHERE course_id = #{courseId} AND deleted_at IS NULL")
    int countByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT COUNT(*) FROM course_prerequisites WHERE prerequisite_course_id = #{prerequisiteCourseId} " +
            "AND deleted_at IS NULL")
    int countByPrerequisiteCourseId(@Param("prerequisiteCourseId") Long prerequisiteCourseId);

    @Select("SELECT COUNT(*) FROM course_prerequisites WHERE course_id = #{courseId} " +
            "AND is_mandatory = true AND deleted_at IS NULL")
    int countMandatoryPrerequisitesByCourse(@Param("courseId") Long courseId);

    @Select("SELECT COUNT(*) FROM course_prerequisites WHERE course_id = #{courseId} " +
            "AND minimum_grade IS NOT NULL AND deleted_at IS NULL")
    int countPrerequisitesWithGradeRequirements(@Param("courseId") Long courseId);

    @Update("UPDATE course_prerequisites SET is_mandatory = #{isMandatory}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateMandatoryStatus(@Param("id") Long id,
                             @Param("isMandatory") Boolean isMandatory,
                             @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE course_prerequisites SET minimum_grade = #{minimumGrade}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateMinimumGrade(@Param("id") Long id,
                          @Param("minimumGrade") BigDecimal minimumGrade,
                          @Param("updatedAt") LocalDateTime updatedAt);

    @Select("SELECT COALESCE(MAX(minimum_grade), 0) FROM course_prerequisites " +
            "WHERE course_id = #{courseId} AND deleted_at IS NULL")
    BigDecimal getHighestMinimumGradeForCourse(@Param("courseId") Long courseId);

    @Select("SELECT COALESCE(MIN(minimum_grade), 100) FROM course_prerequisites " +
            "WHERE course_id = #{courseId} AND deleted_at IS NULL")
    BigDecimal getLowestMinimumGradeForCourse(@Param("courseId") Long courseId);

    @Select("SELECT AVG(minimum_grade) FROM course_prerequisites " +
            "WHERE course_id = #{courseId} AND minimum_grade IS NOT NULL AND deleted_at IS NULL")
    Double getAverageMinimumGradeForCourse(@Param("courseId") Long courseId);

    @Select("SELECT is_mandatory, COUNT(*) as count FROM course_prerequisites " +
            "WHERE course_id = #{courseId} AND deleted_at IS NULL " +
            "GROUP BY is_mandatory ORDER BY is_mandatory DESC")
    List<MandatoryCount> getMandatoryDistributionByCourse(@Param("courseId") Long courseId);

    @Select("SELECT cp.* FROM course_prerequisites cp " +
            "JOIN courses c ON cp.prerequisite_course_id = c.id " +
            "WHERE cp.course_id = #{courseId} AND cp.is_mandatory = true " +
            "AND cp.deleted_at IS NULL AND c.deleted_at IS NULL " +
            "ORDER BY c.course_identifier")
    @ResultMap("coursePrerequisiteResultMap")
    List<CoursePrerequisite> findMandatoryPrerequisitesWithCourseInfo(@Param("courseId") Long courseId);

    // Result class for mandatory distribution
    class MandatoryCount {
        private Boolean isMandatory;
        private Integer count;

        // Getters and setters
        public Boolean getIsMandatory() { return isMandatory; }
        public void setIsMandatory(Boolean isMandatory) { this.isMandatory = isMandatory; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}