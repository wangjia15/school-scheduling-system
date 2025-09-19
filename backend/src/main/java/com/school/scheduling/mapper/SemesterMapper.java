package com.school.scheduling.mapper;

import com.school.scheduling.domain.Semester;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface SemesterMapper {

    // Basic CRUD Operations
    @Insert("INSERT INTO semesters (name, academic_year, semester_type, start_date, end_date, " +
            "is_current, registration_deadline, created_at, updated_at) " +
            "VALUES (#{name}, #{academicYear}, #{semesterType}, #{startDate}, #{endDate}, " +
            "#{isCurrent}, #{registrationDeadline}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Semester semester);

    @Update("UPDATE semesters SET name = #{name}, academic_year = #{academicYear}, " +
            "semester_type = #{semesterType}, start_date = #{startDate}, end_date = #{endDate}, " +
            "is_current = #{isCurrent}, registration_deadline = #{registrationDeadline}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(Semester semester);

    @Delete("UPDATE semesters SET deleted_at = #{deletedAt} WHERE id = #{id}")
    int softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    @Select("SELECT * FROM semesters WHERE id = #{id} AND deleted_at IS NULL")
    @Results(id = "semesterResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "academicYear", column = "academic_year"),
        @Result(property = "semesterType", column = "semester_type", typeHandler = org.apache.ibatis.type.EnumTypeHandler.class),
        @Result(property = "startDate", column = "start_date"),
        @Result(property = "endDate", column = "end_date"),
        @Result(property = "isCurrent", column = "is_current"),
        @Result(property = "registrationDeadline", column = "registration_deadline"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "deletedAt", column = "deleted_at")
    })
    Optional<Semester> findById(@Param("id") Long id);

    @Select("SELECT * FROM semesters WHERE name = #{name} AND deleted_at IS NULL")
    @ResultMap("semesterResultMap")
    Optional<Semester> findByName(@Param("name") String name);

    @Select("SELECT * FROM semesters WHERE academic_year = #{academicYear} " +
            "AND semester_type = #{semesterType} AND deleted_at IS NULL")
    @ResultMap("semesterResultMap")
    Optional<Semester> findByYearAndType(@Param("academicYear") String academicYear,
                                        @Param("semesterType") String semesterType);

    @Select("SELECT * FROM semesters WHERE academic_year = #{academicYear} AND deleted_at IS NULL " +
            "ORDER BY start_date")
    @ResultMap("semesterResultMap")
    List<Semester> findByAcademicYear(@Param("academicYear") String academicYear);

    @Select("SELECT * FROM semesters WHERE semester_type = #{semesterType} AND deleted_at IS NULL " +
            "ORDER BY start_date DESC")
    @ResultMap("semesterResultMap")
    List<Semester> findBySemesterType(@Param("semesterType") String semesterType);

    @Select("SELECT * FROM semesters WHERE is_current = true AND deleted_at IS NULL")
    @ResultMap("semesterResultMap")
    Optional<Semester> findCurrentSemester();

    @Select("SELECT * FROM semesters WHERE start_date >= #{startDate} AND deleted_at IS NULL " +
            "ORDER BY start_date")
    @ResultMap("semesterResultMap")
    List<Semester> findUpcomingSemesters(@Param("startDate") LocalDate startDate);

    @Select("SELECT * FROM semesters WHERE end_date <= #{endDate} AND deleted_at IS NULL " +
            "ORDER BY end_date DESC")
    @ResultMap("semesterResultMap")
    List<Semester> findCompletedSemesters(@Param("endDate") LocalDate endDate);

    @Select("SELECT * FROM semesters WHERE start_date <= #{currentDate} " +
            "AND end_date >= #{currentDate} AND deleted_at IS NULL " +
            "ORDER BY start_date")
    @ResultMap("semesterResultMap")
    List<Semester> findActiveSemesters(@Param("currentDate") LocalDate currentDate);

    @Select("SELECT * FROM semesters WHERE registration_deadline >= #{currentDate} " +
            "AND deleted_at IS NULL ORDER BY registration_deadline")
    @ResultMap("semesterResultMap")
    List<Semester> findOpenRegistrationSemesters(@Param("currentDate") LocalDate currentDate);

    @Select("<script>" +
            "SELECT * FROM semesters WHERE deleted_at IS NULL " +
            "<if test='academicYear != null and !academicYear.isEmpty()'>AND academic_year = #{academicYear}</if> " +
            "<if test='semesterType != null'>AND semester_type = #{semesterType}</if> " +
            "<if test='isCurrent != null'>AND is_current = #{isCurrent}</if> " +
            "<if test='nameSearch != null and !nameSearch.isEmpty()'>" +
            "AND (name LIKE CONCAT('%', #{nameSearch}, '%') OR academic_year LIKE CONCAT('%', #{nameSearch}, '%'))" +
            "</if> " +
            "<if test='startDate != null'>AND start_date >= #{startDate}</if> " +
            "<if test='endDate != null'>AND end_date <= #{endDate}</if> " +
            "ORDER BY start_date DESC" +
            "</script>")
    @ResultMap("semesterResultMap")
    List<Semester> findByCriteria(@Param("academicYear") String academicYear,
                                 @Param("semesterType") String semesterType,
                                 @Param("isCurrent") Boolean isCurrent,
                                 @Param("nameSearch") String nameSearch,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    @Select("SELECT * FROM semesters WHERE deleted_at IS NULL ORDER BY start_date DESC")
    @ResultMap("semesterResultMap")
    List<Semester> findAll();

    @Select("SELECT COUNT(*) FROM semesters WHERE academic_year = #{academicYear} AND deleted_at IS NULL")
    int countByAcademicYear(@Param("academicYear") String academicYear);

    @Select("SELECT COUNT(*) FROM semesters WHERE semester_type = #{semesterType} AND deleted_at IS NULL")
    int countBySemesterType(@Param("semesterType") String semesterType);

    @Select("SELECT COUNT(*) FROM semesters WHERE is_current = true AND deleted_at IS NULL")
    int countCurrentSemesters();

    @Update("UPDATE semesters SET is_current = #{isCurrent}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateCurrentStatus(@Param("id") Long id,
                           @Param("isCurrent") Boolean isCurrent,
                           @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE semesters SET registration_deadline = #{registrationDeadline}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int updateRegistrationDeadline(@Param("id") Long id,
                                  @Param("registrationDeadline") LocalDate registrationDeadline,
                                  @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE semesters SET is_current = false, updated_at = #{updatedAt} " +
            "WHERE id != #{currentId} AND deleted_at IS NULL")
    int unsetCurrentForOtherSemesters(@Param("currentId") Long currentId,
                                     @Param("updatedAt") LocalDateTime updatedAt);

    @Select("SELECT s.* FROM semesters s " +
            "WHERE s.academic_year = #{academicYear} AND s.deleted_at IS NULL " +
            "ORDER BY s.start_date")
    @ResultMap("semesterResultMap")
    List<Semester> findSemestersByYearWithOfferings(@Param("academicYear") String academicYear);

    @Select("SELECT COUNT(*) FROM semesters " +
            "WHERE start_date <= #{currentDate} AND end_date >= #{currentDate} " +
            "AND deleted_at IS NULL")
    int countActiveSemesters(@Param("currentDate") LocalDate currentDate);

    @Select("SELECT COUNT(*) FROM semesters " +
            "WHERE registration_deadline >= #{currentDate} AND deleted_at IS NULL")
    int countOpenRegistrationSemesters(@Param("currentDate") LocalDate currentDate);

    @Select("SELECT COUNT(*) FROM semesters WHERE deleted_at IS NULL")
    int countAllSemesters();

    @Select("SELECT MIN(start_date) FROM semesters WHERE deleted_at IS NULL")
    LocalDate getEarliestSemesterStartDate();

    @Select("SELECT MAX(end_date) FROM semesters WHERE deleted_at IS NULL")
    LocalDate getLatestSemesterEndDate();

    @Select("SELECT academic_year, COUNT(*) as semester_count " +
            "FROM semesters WHERE deleted_at IS NULL " +
            "GROUP BY academic_year ORDER BY academic_year DESC")
    List<YearCount> getAcademicYearDistribution();

    @Select("SELECT semester_type, COUNT(*) as count " +
            "FROM semesters WHERE deleted_at IS NULL " +
            "GROUP BY semester_type ORDER BY count DESC")
    List<TypeCount> getSemesterTypeDistribution();

    // Result classes for distribution queries
    class YearCount {
        private String academicYear;
        private Integer semesterCount;

        // Getters and setters
        public String getAcademicYear() { return academicYear; }
        public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
        public Integer getSemesterCount() { return semesterCount; }
        public void setSemesterCount(Integer semesterCount) { this.semesterCount = semesterCount; }
    }

    class TypeCount {
        private String semesterType;
        private Integer count;

        // Getters and setters
        public String getSemesterType() { return semesterType; }
        public void setSemesterType(String semesterType) { this.semesterType = semesterType; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
}