# Database Performance Optimization Guide

## Overview
This document outlines performance optimization strategies for the School Scheduling System database. The schema is designed to handle 20,000 students, 500 teachers, and 150 classrooms with real-time conflict detection capabilities.

## Performance Requirements

### Target Benchmarks
- **Schedule Generation**: Complete scheduling for 20,000 students within 30 minutes
- **Database Queries**: All queries return within 3 seconds
- **Conflict Detection**: Real-time detection with < 1 second response time
- **Concurrent Users**: Support 50+ administrators during peak scheduling periods

### Scale Considerations
- **Students**: 20,000 active students
- **Teachers**: 500 faculty members
- **Classrooms**: 150 physical rooms
- **Courses**: ~500 active courses per semester
- **Schedules**: ~2,000-3,000 schedule entries per semester
- **Enrollments**: ~60,000-80,000 enrollment records per semester

## Indexing Strategy

### Primary Indexes
All tables have auto-incrementing primary keys with clustered indexes for optimal row retrieval.

### Foreign Key Indexes
```sql
-- Auto-created foreign key indexes for join optimization
INDEX idx_teachers_department (department_id)
INDEX idx_students_department (department_id)
INDEX idx_courses_department (department_id)
INDEX idx_course_offerings_course (course_id)
INDEX idx_course_offerings_semester (semester_id)
INDEX idx_course_offerings_teacher (teacher_id)
INDEX idx_enrollments_student (student_id)
INDEX idx_enrollments_offering (course_offering_id)
INDEX idx_schedules_offering (course_offering_id)
INDEX idx_schedules_classroom (classroom_id)
INDEX idx_schedules_time_slot (time_slot_id)
```

### Composite Indexes for Complex Queries
```sql
-- Teacher workload queries
INDEX idx_teachers_department_active_max (department_id, deleted_at, max_weekly_hours, max_courses_per_semester)

-- Student enrollment queries
INDEX idx_students_department_status (department_id, status, deleted_at)
INDEX idx_students_year_semester_gpa (enrollment_year, current_semester, gpa, status)

-- Course offering capacity queries
INDEX idx_course_offerings_capacity (max_enrollment, current_enrollment, is_open)
INDEX idx_course_offerings_filters (is_open, semester_id, teacher_id, deleted_at)

-- Schedule conflict detection
INDEX idx_schedules_teacher_conflict_check (course_offering_id, time_slot_id, schedule_date)
INDEX idx_schedules_classroom_conflict_check (classroom_id, time_slot_id, schedule_date)
INDEX idx_schedules_student_conflict_check (course_offering_id, schedule_date)

-- Enrollment management
INDEX idx_enrollments_student_status (student_id, status, course_offering_id)
INDEX idx_enrollments_offering_date (course_offering_id, enrollment_date, status)

-- Classroom availability
INDEX idx_classrooms_capacity_type (capacity, room_type, is_available, deleted_at)
```

### Selective Indexes for Search Performance
```sql
-- Full-text search for course and user lookup
FULLTEXT INDEX idx_courses_search ON courses(course_code, title, description)
FULLTEXT INDEX idx_users_search ON users(first_name, last_name, email)

-- Time-based queries
INDEX idx_semesters_dates (start_date, end_date)
INDEX idx_audit_timestamp (action_timestamp)
INDEX idx_conflicts_detected (detected_at)
```

## Query Optimization Patterns

### 1. Teacher Schedule Lookup
```sql
-- Optimized query for teacher schedule by semester
SELECT s.*, co.section_number, c.course_code, c.title, cr.name as classroom_name,
       ts.day_of_week, ts.start_time, ts.end_time
FROM schedules s
JOIN course_offerings co ON s.course_offering_id = co.id
JOIN courses c ON co.course_id = c.id
JOIN classrooms cr ON s.classroom_id = cr.id
JOIN time_slots ts ON s.time_slot_id = ts.id
WHERE co.teacher_id = ? AND co.semester_id = ?
  AND s.deleted_at IS NULL AND co.deleted_at IS NULL
ORDER BY ts.day_of_week, ts.start_time;
```

### 2. Student Enrollment by Department
```sql
-- Optimized query for student enrollment statistics
SELECT d.name as department_name, d.code as department_code,
       COUNT(s.id) as total_students,
       AVG(s.gpa) as average_gpa,
       COUNT(e.id) as total_enrollments
FROM departments d
LEFT JOIN students s ON d.id = s.department_id AND s.deleted_at IS NULL
LEFT JOIN enrollments e ON s.id = e.student_id AND e.status = 'ENROLLED'
WHERE d.deleted_at IS NULL
GROUP BY d.id
ORDER BY d.name;
```

### 3. Classroom Availability Query
```sql
-- Optimized query for classroom availability by time slot
SELECT cr.*, ts.day_of_week, ts.start_time, ts.end_time,
       CASE WHEN s.id IS NULL THEN 'AVAILABLE' ELSE 'OCCUPIED' END as status
FROM classrooms cr
CROSS JOIN time_slots ts
LEFT JOIN schedules s ON cr.id = s.classroom_id
                     AND ts.id = s.time_slot_id
                     AND s.schedule_date = ?
                     AND s.deleted_at IS NULL
WHERE cr.is_available = TRUE AND cr.deleted_at IS NULL
  AND ts.is_active = TRUE AND ts.deleted_at IS NULL
ORDER BY cr.building_code, cr.room_number, ts.day_of_week, ts.start_time;
```

### 4. Conflict Detection Query
```sql
-- Optimized query for detecting teacher double-booking
SELECT s1.id as schedule1_id, s2.id as schedule2_id,
       c1.course_code as course1_code, c2.course_code as course2_code,
       ts1.day_of_week, ts1.start_time, ts1.end_time
FROM schedules s1
JOIN schedules s2 ON s1.time_slot_id = s2.time_slot_id
               AND s1.schedule_date = s2.schedule_date
               AND s1.id != s2.id
JOIN course_offerings co1 ON s1.course_offering_id = co1.id
JOIN course_offerings co2 ON s2.course_offering_id = co2.id
JOIN courses c1 ON co1.course_id = c1.id
JOIN courses c2 ON co2.course_id = c2.id
JOIN time_slots ts1 ON s1.time_slot_id = ts1.id
WHERE co1.teacher_id = co2.teacher_id
  AND s1.deleted_at IS NULL AND s2.deleted_at IS NULL
  AND co1.deleted_at IS NULL AND co2.deleted_at IS NULL;
```

## Database Partitioning Strategy

### Partitioning by Academic Year
For large tables, consider partitioning by academic year to improve query performance:

```sql
-- Example partitioning for enrollments table
ALTER TABLE enrollments PARTITION BY RANGE (YEAR(enrollment_date)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);
```

### Partitioning Candidates
1. **Enrollments**: Partition by enrollment_date or academic year
2. **Schedules**: Partition by schedule_date or semester
3. **Audit_Log**: Partition by action_timestamp (time-based)
4. **Schedule_Conflicts**: Partition by detected_at

## Caching Strategy

### Application-Level Caching
1. **Current Semester**: Cache current semester information
2. **Time Slots**: Cache active time slots for scheduling
3. **Department Lists**: Cache department hierarchies
4. **User Profiles**: Cache frequently accessed user information

### Database-Level Caching
```sql
-- Configure MySQL query cache
SET GLOBAL query_cache_size = 67108864; -- 64MB
SET GLOBAL query_cache_type = ON;

-- Consider Redis for caching frequently accessed data
-- - Course catalog information
-- - Teacher assignments
-- - Student enrollment counts
```

## Connection Pooling

### Recommended Configuration
```sql
-- Connection pool settings for MySQL
SET GLOBAL max_connections = 200;
SET GLOBAL thread_cache_size = 16;
SET GLOBAL innodb_buffer_pool_size = 2G; -- Adjust based on available RAM
```

### Application Connection Pool
Configure connection pool in application:
- **Minimum Connections**: 10
- **Maximum Connections**: 50
- **Connection Timeout**: 30 seconds
- **Idle Timeout**: 300 seconds (5 minutes)

## Bulk Operations Optimization

### 1. Bulk Enrollment Updates
```sql
-- Use transactions for bulk operations
START TRANSACTION;
UPDATE course_offerings SET current_enrollment = 0 WHERE semester_id = ?;
UPDATE course_offerings co
SET current_enrollment = (
    SELECT COUNT(*)
    FROM enrollments e
    WHERE e.course_offering_id = co.id AND e.status = 'ENROLLED'
)
WHERE co.semester_id = ?;
COMMIT;
```

### 2. Bulk Schedule Generation
```sql
-- Use stored procedures for complex scheduling operations
DELIMITER //
CREATE PROCEDURE bulk_schedule_generation(
    IN p_semester_id BIGINT,
    IN p_teacher_id BIGINT,
    IN p_course_id BIGINT
)
BEGIN
    DECLARE v_time_slot_id BIGINT;
    DECLARE v_classroom_id BIGINT;
    DECLARE v_done INT DEFAULT FALSE;

    -- Cursor for available time slots
    DECLARE time_slot_cursor CURSOR FOR
        SELECT id FROM time_slots
        WHERE is_active = TRUE AND deleted_at IS NULL;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;

    OPEN time_slot_cursor;

    read_loop: LOOP
        FETCH time_slot_cursor INTO v_time_slot_id;
        IF v_done THEN
            LEAVE read_loop;
        END IF;

        -- Find available classroom for this time slot
        SELECT id INTO v_classroom_id
        FROM classrooms cr
        WHERE cr.is_available = TRUE
          AND cr.deleted_at IS NULL
          AND cr.capacity >= 30
          AND NOT EXISTS (
              SELECT 1 FROM schedules s
              WHERE s.classroom_id = cr.id
                AND s.time_slot_id = v_time_slot_id
                AND s.deleted_at IS NULL
          )
        LIMIT 1;

        IF v_classroom_id IS NOT NULL THEN
            -- Insert schedule
            INSERT INTO schedules (course_offering_id, classroom_id, time_slot_id, schedule_date)
            VALUES (
                (SELECT id FROM course_offerings
                 WHERE course_id = p_course_id
                   AND semester_id = p_semester_id
                   AND teacher_id = p_teacher_id),
                v_classroom_id,
                v_time_slot_id,
                CURDATE()
            );
        END IF;
    END LOOP;

    CLOSE time_slot_cursor;
END//
DELIMITER ;
```

## Monitoring and Maintenance

### Performance Monitoring Queries
```sql
-- Slow query log analysis
SELECT * FROM mysql.slow_log
WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 DAY)
ORDER BY query_time DESC;

-- Index usage statistics
SELECT * FROM sys.schema_unused_indexes
WHERE object_schema = 'school_scheduling';

-- Table size analysis
SELECT
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS table_size_mb,
    table_rows
FROM information_schema.tables
WHERE table_schema = 'school_scheduling'
ORDER BY table_size_mb DESC;
```

### Regular Maintenance Tasks
1. **Index Statistics Update**: Run weekly
2. **Table Optimization**: Run monthly
3. **Slow Query Analysis**: Review weekly
4. **Index Usage Review**: Monthly analysis
5. **Partition Maintenance**: Quarterly review

## Scaling Considerations

### Read Replicas
Consider implementing read replicas for:
- **Reporting Queries**: Student progress reports
- **Analytics Queries**: Utilization statistics
- **Backup Processes**: Database backups without affecting performance

### Database Sharding
For extreme scaling (100K+ students), consider sharding by:
- **Department**: Different departments on different shards
- **Academic Year**: Historical data on separate shards
- **Geographic**: Multiple campus locations

## Security Performance Considerations

### Query Performance with Security
```sql
-- Row-level security implementation
CREATE POLICY teacher_schedule_policy ON schedules
    FOR SELECT TO teachers
    USING (course_offering_id IN (
        SELECT id FROM course_offerings WHERE teacher_id = current_user_id()
    ));
```

### Audit Logging Performance
- Implement asynchronous audit logging
- Use batch inserts for audit records
- Consider separating audit logs to a dedicated table
- Archive old audit records regularly

## Testing Strategy

### Performance Testing Scenarios
1. **Concurrent Scheduling**: 50 administrators scheduling simultaneously
2. **Bulk Enrollment**: 1,000+ enrollments in a single transaction
3. **Conflict Detection**: Real-time conflict checking during scheduling
4. **Reporting Generation**: Large-scale report queries
5. **Peak Load Testing**: Registration period simulation

### Benchmarking Tools
- **JMeter**: Load testing for concurrent users
- **MySQL Benchmark**: Database performance testing
- **Custom Scripts**: Specific scheduling algorithm testing

This performance optimization guide ensures the database can handle the scale and complexity of university course scheduling while maintaining responsive performance for all users.