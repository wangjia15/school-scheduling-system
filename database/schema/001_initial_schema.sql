-- School Scheduling System Database Schema
-- Issue #001: Database Schema Design
--
-- This schema implements a normalized database design for a university scheduling system
-- handling 20,000 students, 500 teachers, and 150 classrooms.
--
-- Design Principles:
-- - Third Normal Form (3NF) compliance
-- - Soft delete capability with deleted_at timestamps
-- - Audit timestamps (created_at, updated_at)
-- - Proper indexing for performance optimization
-- - Constraint validation for data integrity
-- - Support for real-time conflict detection

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- CORE ENTITIES
-- ============================================

-- Users table for authentication and authorization
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'TEACHER', 'STUDENT', 'DEPARTMENT_HEAD') NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    INDEX idx_users_role (role),
    INDEX idx_users_active (is_active),
    INDEX idx_users_deleted (deleted_at)
);

-- Departments for organizational structure
CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    head_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (head_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_departments_code (code),
    INDEX idx_departments_deleted (deleted_at)
);

-- Teachers table with profile and specialization information
CREATE TABLE teachers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    department_id BIGINT NOT NULL,
    title ENUM('PROFESSOR', 'ASSOCIATE_PROFESSOR', 'ASSISTANT_PROFESSOR', 'INSTRUCTOR', 'ADJUNCT') NOT NULL,
    max_weekly_hours DECIMAL(4,1) NOT NULL DEFAULT 40.0,
    max_courses_per_semester INT NOT NULL DEFAULT 5,
    office_location VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    INDEX idx_teachers_department (department_id),
    INDEX idx_teachers_employee_id (employee_id),
    INDEX idx_teachers_deleted (deleted_at)
);

-- Teacher specializations (many-to-many relationship with subjects)
CREATE TABLE teacher_specializations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT NOT NULL,
    subject_code VARCHAR(20) NOT NULL,
    proficiency_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT') NOT NULL DEFAULT 'INTERMEDIATE',
    years_experience INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE,
    UNIQUE KEY uk_teacher_subject (teacher_id, subject_code),
    INDEX idx_specializations_teacher (teacher_id),
    INDEX idx_specializations_subject (subject_code)
);

-- Students table with enrollment information
CREATE TABLE students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    department_id BIGINT NOT NULL,
    enrollment_year INT NOT NULL,
    graduation_year INT,
    current_semester INT NOT NULL DEFAULT 1,
    gpa DECIMAL(3,2) DEFAULT 0.00,
    status ENUM('ACTIVE', 'INACTIVE', 'GRADUATED', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    INDEX idx_students_department (department_id),
    INDEX idx_students_student_id (student_id),
    INDEX idx_students_status (status),
    INDEX idx_students_deleted (deleted_at)
);

-- Classrooms table with capacity and equipment information
CREATE TABLE classrooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_code VARCHAR(20) NOT NULL,
    room_number VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    room_type ENUM('LECTURE_HALL', 'LABORATORY', 'SEMINAR_ROOM', 'COMPUTER_LAB', 'STUDIO', 'CONFERENCE_ROOM') NOT NULL,
    has_projector BOOLEAN NOT NULL DEFAULT TRUE,
    has_computer BOOLEAN NOT NULL DEFAULT FALSE,
    has_whiteboard BOOLEAN NOT NULL DEFAULT TRUE,
    special_equipment TEXT, -- JSON array of special equipment
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    UNIQUE KEY uk_classroom_location (building_code, room_number),
    INDEX idx_classrooms_capacity (capacity),
    INDEX idx_classrooms_type (room_type),
    INDEX idx_classrooms_available (is_available),
    INDEX idx_classrooms_deleted (deleted_at)
);

-- ============================================
-- COURSE MANAGEMENT
-- ============================================

-- Courses table with course definitions
CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    department_id BIGINT NOT NULL,
    credits INT NOT NULL,
    contact_hours_per_week DECIMAL(3,1) NOT NULL,
    theory_hours DECIMAL(3,1) DEFAULT 0.0,
    lab_hours DECIMAL(3,1) DEFAULT 0.0,
    level ENUM('UNDERGRADUATE', 'GRADUATE', 'PHD') NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    max_students INT DEFAULT 30,
    min_students INT DEFAULT 5,
    requires_lab BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    INDEX idx_courses_department (department_id),
    INDEX idx_courses_code (course_code),
    INDEX idx_courses_level (level),
    INDEX idx_courses_active (is_active),
    INDEX idx_courses_deleted (deleted_at)
);

-- Course prerequisites (self-referencing many-to-many)
CREATE TABLE course_prerequisites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    prerequisite_course_id BIGINT NOT NULL,
    is_mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    minimum_grade DECIMAL(3,2) DEFAULT 60.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (prerequisite_course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY uk_course_prerequisite (course_id, prerequisite_course_id),
    INDEX idx_prerequisites_course (course_id),
    INDEX idx_prerequisites_prereq (prerequisite_course_id)
);

-- Course offerings (specific instances of courses in semesters)
CREATE TABLE course_offerings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    semester_id BIGINT NOT NULL,
    section_number VARCHAR(10) NOT NULL,
    teacher_id BIGINT NOT NULL,
    max_enrollment INT NOT NULL DEFAULT 30,
    current_enrollment INT NOT NULL DEFAULT 0,
    schedule_type ENUM('REGULAR', 'WEEKEND', 'EVENING', 'ONLINE') NOT NULL DEFAULT 'REGULAR',
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    syllabus_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (semester_id) REFERENCES semesters(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_offering_section (course_id, semester_id, section_number),
    INDEX idx_offerings_course (course_id),
    INDEX idx_offerings_semester (semester_id),
    INDEX idx_offerings_teacher (teacher_id),
    INDEX idx_offerings_deleted (deleted_at),
    CHECK (current_enrollment <= max_enrollment)
);

-- ============================================
-- SCHEDULING ENTITIES
-- ============================================

-- Academic terms/semesters
CREATE TABLE semesters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    academic_year VARCHAR(9) NOT NULL, -- Format: 2024-2025
    semester_type ENUM('FALL', 'SPRING', 'SUMMER', 'WINTER') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    registration_deadline DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    UNIQUE KEY uk_semester_year_type (academic_year, semester_type),
    INDEX idx_semesters_dates (start_date, end_date),
    INDEX idx_semesters_current (is_current),
    INDEX idx_semesters_deleted (deleted_at),
    CHECK (end_date > start_date)
);

-- Time slots for scheduling
CREATE TABLE time_slots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_type ENUM('MORNING', 'AFTERNOON', 'EVENING') NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    UNIQUE KEY uk_time_slot_day_time (day_of_week, start_time, end_time),
    INDEX idx_time_slots_day (day_of_week),
    INDEX idx_time_slots_type (slot_type),
    INDEX idx_time_slots_deleted (deleted_at),
    CHECK (end_time > start_time)
);

-- Schedule entries (core scheduling table)
CREATE TABLE schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_offering_id BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    schedule_date DATE NOT NULL,
    is_recurring BOOLEAN NOT NULL DEFAULT TRUE,
    recurrence_pattern VARCHAR(50), -- e.g., 'WEEKLY', 'BIWEEKLY'
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (course_offering_id) REFERENCES course_offerings(id) ON DELETE CASCADE,
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id) ON DELETE RESTRICT,
    FOREIGN KEY (time_slot_id) REFERENCES time_slots(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_schedule_conflict (classroom_id, time_slot_id, schedule_date),
    INDEX idx_schedules_offering (course_offering_id),
    INDEX idx_schedules_classroom (classroom_id),
    INDEX idx_schedules_time_slot (time_slot_id),
    INDEX idx_schedules_date (schedule_date),
    INDEX idx_schedules_deleted (deleted_at)
);

-- Student enrollments in course offerings
CREATE TABLE enrollments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_offering_id BIGINT NOT NULL,
    enrollment_date DATE NOT NULL,
    status ENUM('ENROLLED', 'DROPPED', 'COMPLETED', 'WITHDRAWN', 'FAILED') NOT NULL DEFAULT 'ENROLLED',
    grade DECIMAL(3,2),
    grade_letter VARCHAR(2),
    is_attending BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_offering_id) REFERENCES course_offerings(id) ON DELETE CASCADE,
    UNIQUE KEY uk_enrollment_student_course (student_id, course_offering_id),
    INDEX idx_enrollments_student (student_id),
    INDEX idx_enrollments_offering (course_offering_id),
    INDEX idx_enrollments_status (status),
    INDEX idx_enrollments_deleted (deleted_at)
);

-- ============================================
-- CONSTRAINT AND CONFLICT MANAGEMENT
-- ============================================

-- Scheduling constraints
CREATE TABLE scheduling_constraints (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    constraint_type ENUM('TEACHER_AVAILABILITY', 'CLASSROOM_AVAILABILITY', 'STUDENT_CONFLICT', 'DEPARTMENT_RESTRICTION', 'CAPACITY_LIMIT') NOT NULL,
    entity_id BIGINT, -- Can reference teacher, classroom, or student
    entity_type ENUM('TEACHER', 'CLASSROOM', 'STUDENT', 'DEPARTMENT'),
    constraint_data JSON NOT NULL, -- Flexible constraint configuration
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    priority INT DEFAULT 1, -- 1=High, 2=Medium, 3=Low
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    INDEX idx_constraints_type (constraint_type),
    INDEX idx_constraints_entity (entity_type, entity_id),
    INDEX idx_constraints_priority (priority),
    INDEX idx_constraints_active (is_active),
    INDEX idx_constraints_deleted (deleted_at)
);

-- Schedule conflicts detection and tracking
CREATE TABLE schedule_conflicts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conflict_type ENUM('TEACHER_DOUBLE_BOOKING', 'CLASSROOM_DOUBLE_BOOKING', 'STUDENT_SCHEDULE_CONFLICT', 'CAPACITY_EXCEEDED', 'PREREQUISITE_NOT_MET', 'EQUIPMENT_MISMATCH') NOT NULL,
    severity ENUM('CRITICAL', 'HIGH', 'MEDIUM', 'LOW') NOT NULL DEFAULT 'HIGH',
    description TEXT NOT NULL,
    schedule_id_1 BIGINT, -- First conflicting schedule
    schedule_id_2 BIGINT, -- Second conflicting schedule (if applicable)
    entity_id BIGINT, -- Affected entity (teacher, classroom, student)
    entity_type ENUM('TEACHER', 'CLASSROOM', 'STUDENT', 'COURSE_OFFERING'),
    detected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    resolution_status ENUM('PENDING', 'RESOLVED', 'IGNORED', 'DEFERRED') NOT NULL DEFAULT 'PENDING',
    resolution_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id_1) REFERENCES schedules(id) ON DELETE SET NULL,
    FOREIGN KEY (schedule_id_2) REFERENCES schedules(id) ON DELETE SET NULL,
    INDEX idx_conflicts_type (conflict_type),
    INDEX idx_conflicts_severity (severity),
    INDEX idx_conflicts_status (resolution_status),
    INDEX idx_conflicts_entity (entity_type, entity_id),
    INDEX idx_conflicts_detected (detected_at)
);

-- ============================================
-- AUDIT AND LOGGING
-- ============================================

-- Audit log for tracking changes
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    table_name VARCHAR(50) NOT NULL,
    record_id BIGINT NOT NULL,
    action ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    old_values JSON,
    new_values JSON,
    user_id BIGINT NOT NULL,
    action_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_audit_table (table_name),
    INDEX idx_audit_record (table_name, record_id),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_timestamp (action_timestamp)
);

-- ============================================
-- VIEWS FOR COMMON QUERIES
-- ============================================

-- Current semester view
CREATE VIEW v_current_semester AS
SELECT * FROM semesters WHERE is_current = TRUE AND deleted_at IS NULL;

-- Active teachers view
CREATE VIEW v_active_teachers AS
SELECT
    t.*,
    u.first_name,
    u.last_name,
    u.email,
    d.name as department_name,
    d.code as department_code
FROM teachers t
JOIN users u ON t.user_id = u.id
JOIN departments d ON t.department_id = d.id
WHERE t.deleted_at IS NULL AND u.is_active = TRUE;

-- Active students view
CREATE VIEW v_active_students AS
SELECT
    s.*,
    u.first_name,
    u.last_name,
    u.email,
    d.name as department_name,
    d.code as department_code
FROM students s
JOIN users u ON s.user_id = u.id
JOIN departments d ON s.department_id = d.id
WHERE s.deleted_at IS NULL AND s.status = 'ACTIVE';

-- Course offerings with enrollment counts
CREATE VIEW v_course_offerings_enrollment AS
SELECT
    co.*,
    c.course_code,
    c.title as course_title,
    c.credits,
    t.employee_id as teacher_employee_id,
    CONCAT(u.first_name, ' ', u.last_name) as teacher_name,
    cr.name as classroom_name,
    cr.capacity as classroom_capacity,
    s.name as semester_name,
    s.academic_year,
    COUNT(e.id) as enrolled_count,
    (co.max_enrollment - COUNT(e.id)) as available_seats
FROM course_offerings co
JOIN courses c ON co.course_id = c.id
JOIN teachers t ON co.teacher_id = t.id
JOIN users u ON t.user_id = u.id
LEFT JOIN classrooms cr ON co.id = cr.id
JOIN semesters s ON co.semester_id = s.id
LEFT JOIN enrollments e ON co.id = e.course_offering_id AND e.status = 'ENROLLED'
WHERE co.deleted_at IS NULL AND co.is_open = TRUE
GROUP BY co.id;

-- Schedule conflicts summary view
CREATE VIEW v_schedule_conflicts_summary AS
SELECT
    conflict_type,
    severity,
    resolution_status,
    COUNT(*) as conflict_count,
    MAX(detected_at) as last_detected
FROM schedule_conflicts
WHERE resolution_status != 'RESOLVED'
GROUP BY conflict_type, severity, resolution_status
ORDER BY severity, conflict_count DESC;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- PERFORMANCE OPTIMIZATION INDEXES
-- ============================================

-- Composite indexes for common query patterns
CREATE INDEX idx_schedules_teacher_lookup ON schedules(course_offering_id, classroom_id, time_slot_id, schedule_date);
CREATE INDEX idx_enrollments_student_semester ON enrollments(student_id, course_offering_id);
CREATE INDEX idx_course_offerings_semester_department ON course_offerings(semester_id, course_id);
CREATE INDEX idx_teachers_department_active ON teachers(department_id, deleted_at);
CREATE INDEX idx_students_department_status ON students(department_id, status, deleted_at);

-- Full-text search indexes for search functionality
CREATE FULLTEXT INDEX idx_courses_search ON courses(course_code, title, description);
CREATE FULLTEXT INDEX idx_users_search ON users(first_name, last_name, email);

-- ============================================
-- TRIGGERS FOR DATA INTEGRITY
-- ============================================

DELIMITER //

-- Trigger to update course offering enrollment count
CREATE TRIGGER tr_update_enrollment_count
AFTER INSERT ON enrollments
FOR EACH ROW
BEGIN
    IF NEW.status = 'ENROLLED' THEN
        UPDATE course_offerings
        SET current_enrollment = current_enrollment + 1
        WHERE id = NEW.course_offering_id;
    END IF;
END//

CREATE TRIGGER tr_update_enrollment_count_drop
AFTER UPDATE ON enrollments
FOR EACH ROW
BEGIN
    IF OLD.status = 'ENROLLED' AND NEW.status != 'ENROLLED' THEN
        UPDATE course_offerings
        SET current_enrollment = current_enrollment - 1
        WHERE id = NEW.course_offering_id;
    ELSIF OLD.status != 'ENROLLED' AND NEW.status = 'ENROLLED' THEN
        UPDATE course_offerings
        SET current_enrollment = current_enrollment + 1
        WHERE id = NEW.course_offering_id;
    END IF;
END//

DELIMITER ;