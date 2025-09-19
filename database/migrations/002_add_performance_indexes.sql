-- Migration 002: Add Performance Optimization Indexes
-- Issue #001: Database Schema Design
-- This migration adds additional performance indexes for large-scale operations

USE school_scheduling;

-- Composite indexes for complex scheduling queries
CREATE INDEX IF NOT EXISTS idx_schedules_teacher_conflict_check ON schedules(course_offering_id, time_slot_id, schedule_date);
CREATE INDEX IF NOT EXISTS idx_schedules_classroom_conflict_check ON schedules(classroom_id, time_slot_id, schedule_date);
CREATE INDEX IF NOT EXISTS idx_schedules_student_conflict_check ON schedules(course_offering_id, schedule_date);

-- Indexes for constraint validation
CREATE INDEX IF NOT EXISTS idx_constraints_active_priority ON scheduling_constraints(is_active, priority, constraint_type);
CREATE INDEX IF NOT EXISTS idx_conflicts_unresolved ON schedule_conflicts(resolution_status, severity, conflict_type);

-- Indexes for enrollment management
CREATE INDEX IF NOT EXISTS idx_enrollments_student_status ON enrollments(student_id, status, course_offering_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_offering_date ON enrollments(course_offering_id, enrollment_date, status);

-- Indexes for course offerings with filters
CREATE INDEX IF NOT EXISTS idx_course_offerings_filters ON course_offerings(is_open, semester_id, teacher_id, deleted_at);
CREATE INDEX IF NOT EXISTS idx_course_offerings_capacity ON course_offerings(max_enrollment, current_enrollment, is_open);

-- Indexes for teacher workload calculations
CREATE INDEX IF NOT EXISTS idx_teacher_specializations_subject ON teacher_specializations(subject_code, proficiency_level);
CREATE INDEX IF NOT EXISTS idx_teachers_department_active_max ON teachers(department_id, deleted_at, max_weekly_hours, max_courses_per_semester);

-- Indexes for student progression tracking
CREATE INDEX IF NOT EXISTS idx_students_year_semester_gpa ON students(enrollment_year, current_semester, gpa, status);
CREATE INDEX IF NOT EXISTS idx_students_department_year ON students(department_id, enrollment_year, status);

-- Indexes for classroom availability
CREATE INDEX IF NOT EXISTS idx_classrooms_capacity_type ON classrooms(capacity, room_type, is_available, deleted_at);
CREATE INDEX IF NOT EXISTS idx_classrooms_equipment ON classrooms(has_projector, has_computer, has_whiteboard, is_available);

-- Indexes for audit logging performance
CREATE INDEX IF NOT EXISTS idx_audit_user_timestamp ON audit_log(user_id, action_timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_table_action_timestamp ON audit_log(table_name, action, action_timestamp);

-- Create function for updating enrollment counts (for bulk operations)
DELIMITER //

CREATE FUNCTION IF NOT EXISTS update_enrollment_count(p_offering_id BIGINT) RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE v_count INT;

    SELECT COUNT(*) INTO v_count
    FROM enrollments
    WHERE course_offering_id = p_offering_id AND status = 'ENROLLED';

    UPDATE course_offerings
    SET current_enrollment = v_count
    WHERE id = p_offering_id;

    RETURN v_count;
END//

DELIMITER ;

-- Migration complete
SELECT 'Migration 002 completed successfully' as status;