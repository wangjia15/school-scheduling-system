package com.school.scheduling.mapper;

import com.school.scheduling.domain.TeacherSpecialization;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface TeacherSpecializationMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO teacher_specializations (teacher_id, subject_code, proficiency_level, " +
            "years_experience, created_at, updated_at) " +
            "VALUES (#{teacherId}, #{subjectCode}, #{proficiencyLevel}, " +
            "#{yearsExperience}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TeacherSpecialization teacherSpecialization);

    @Update("UPDATE teacher_specializations SET teacher_id = #{teacherId}, subject_code = #{subjectCode}, " +
            "proficiency_level = #{proficiencyLevel}, years_experience = #{yearsExperience}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(TeacherSpecialization teacherSpecialization);

    @Delete("UPDATE teacher_specializations SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Select("SELECT * FROM teacher_specializations WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "teacherSpecializationResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "teacherId", column = "teacher_id"),
        @Result(property = "subjectCode", column = "subject_code"),
        @Result(property = "proficiencyLevel", column = "proficiency_level", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "yearsExperience", column = "years_experience"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<TeacherSpecialization> findById(@Param("id") Long id);

    @Select("SELECT * FROM teacher_specializations WHERE teacher_id = #{teacherId} " +
            "AND subject_code = #{subjectCode} AND deleted_at IS NULL")
    @ResultMap("teacherSpecializationResultMap")
    Optional<TeacherSpecialization> findByTeacherAndSubject(@Param("teacherId") Long teacherId,
                                                           @Param("subjectCode") String subjectCode);

    @Select("SELECT * FROM teacher_specializations WHERE teacher_id = #{teacherId} AND deleted_at IS NULL " +
            "ORDER BY proficiency_level DESC, years_experience DESC")
    @ResultMap("teacherSpecializationResultMap")
    List<TeacherSpecialization> findByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT * FROM teacher_specializations WHERE subject_code = #{subjectCode} AND deleted_at IS NULL " +
            "ORDER BY proficiency_level DESC, years_experience DESC")
    @ResultMap("teacherSpecializationResultMap")
    List<TeacherSpecialization> findBySubjectCode(@Param("subjectCode") String subjectCode);

    @Select("SELECT * FROM teacher_specializations WHERE proficiency_level = #{proficiencyLevel} " +
            "AND deleted_at IS NULL ORDER BY subject_code")
    @ResultMap("teacherSpecializationResultMap")
    List<TeacherSpecialization> findByProficiencyLevel(@Param("proficiencyLevel") String proficiencyLevel);

    @Select("SELECT * FROM teacher_specializations WHERE years_experience >= #{minYears} " +
            "AND deleted_at IS NULL ORDER BY years_experience DESC")
    @ResultMap("teacherSpecializationResultMap")
    List<TeacherSpecialization> findByMinimumExperience(@Param("minYears") Integer minYears);

    @Select("SELECT * FROM teacher_specializations WHERE subject_code = #{subjectCode} " +
            "AND proficiency_level IN ('ADVANCED', 'EXPERT') AND deleted_at IS NULL " +
            "ORDER BY years_experience DESC")
    @ResultMap("teacherSpecializationResultMap")
    List<TeacherSpecialization> findExpertsInSubject(@Param("subjectCode") String subjectCode);

    @Select("SELECT * FROM teacher_specializations WHERE subject_code = #{subjectCode} " +
            "AND proficiency_level = 'EXPERT' AND years_experience >= #{minYears} " +
            "AND deleted_at IS NULL ORDER BY years_experience DESC")
    @ResultMap("teacherSpecializationResultMap")
    List<TeacherSpecialization> findSeniorExpertsInSubject(@Param("subjectCode") String subjectCode,
                                                           @Param("minYears") Integer minYears);

    @Select("<script>" +
            "SELECT * FROM teacher_specializations WHERE deleted_at IS NULL " +
            "<if test='teacherId != null'>AND teacher_id = #{teacherId}</if> " +
            "<if test='subjectCode != null and !subjectCode.isEmpty()'>AND subject_code = #{subjectCode}</if> " +
            "<if test='proficiencyLevel != null'>AND proficiency_level = #{proficiencyLevel}</if> " +
            "<if test='minYearsExperience != null'>AND years_experience >= #{minYearsExperience}</if> " +
            "<if test='canTeachAdvanced != null and canTeachAdvanced'>AND proficiency_level IN ('ADVANCED', 'EXPERT')</if> " +
            "<if test='canTeachGraduate != null and canTeachGraduate'>AND proficiency_level = 'EXPERT'</if> " +
            "ORDER BY proficiency_level DESC, years_experience DESC" +
            "</script>")
    @ResultMap("teacherSpecializationResultMap")
    List<TeacherSpecialization> findByCriteria(@Param("teacherId") Long teacherId,
                                              @Param("subjectCode") String subjectCode,
                                              @Param("proficiencyLevel") String proficiencyLevel,
                                              @Param("minYearsExperience") Integer minYearsExperience,
                                              @Param("canTeachAdvanced") Boolean canTeachAdvanced,
                                              @Param("canTeachGraduate") Boolean canTeachGraduate);

    @Select("SELECT COUNT(*) FROM teacher_specializations WHERE teacher_id = #{teacherId} AND deleted_at IS NULL")
    int countByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT COUNT(*) FROM teacher_specializations WHERE subject_code = #{subjectCode} " +
            "AND proficiency_level IN ('ADVANCED', 'EXPERT') AND deleted_at IS NULL")
    int countExpertsBySubject(@Param("subjectCode") String subjectCode);

    @Select("SELECT AVG(years_experience) FROM teacher_specializations WHERE subject_code = #{subjectCode} " +
            "AND deleted_at IS NULL")
    Double getAverageExperienceBySubject(@Param("subjectCode") String subjectCode);

    @Select("SELECT proficiency_level, COUNT(*) as count FROM teacher_specializations " +
            "WHERE deleted_at IS NULL GROUP BY proficiency_level ORDER BY count DESC")
    List<ProficiencyCount> getProficiencyDistribution();

    @Update("UPDATE teacher_specializations SET proficiency_level = #{proficiencyLevel}, " +
            "years_experience = #{yearsExperience}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateProficiencyAndExperience(@Param("id") Long id,
                                       @Param("proficiencyLevel") String proficiencyLevel,
                                       @Param("yearsExperience") Integer yearsExperience,
                                       @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE teacher_specializations SET years_experience = #{yearsExperience}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateYearsExperience(@Param("id") Long id,
                            @Param("yearsExperience") Integer yearsExperience,
                            @Param("updatedAt") LocalDateTime updatedAt);

    @Select("SELECT ts.* FROM teacher_specializations ts " +
            "JOIN teachers t ON ts.teacher_id = t.id " +
            "WHERE ts.subject_code = #{subjectCode} AND ts.proficiency_level IN ('ADVANCED', 'EXPERT') " +
            "AND ts.deleted_at IS NULL AND t.is_active = true " +
            "ORDER BY ts.proficiency_level DESC, ts.years_experience DESC")
    @ResultMap("teacherSpecializationResultMap")
    List<TeacherSpecialization> findAvailableExpertsInSubject(@Param("subjectCode") String subjectCode);

    // Result class for proficiency distribution
    class ProficiencyCount {
        private String proficiencyLevel;
        private Integer count;

        // Getters and setters
        public String getProficiencyLevel() { return proficiencyLevel; }
        public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}