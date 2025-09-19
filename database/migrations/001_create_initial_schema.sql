-- Migration 001: Create Initial Database Schema
-- Issue #001: Database Schema Design
-- This migration creates the complete initial schema for the school scheduling system

-- Use the school scheduling database
USE IF EXISTS school_scheduling;
CREATE DATABASE IF NOT EXISTS school_scheduling;
USE school_scheduling;

-- Execute the initial schema creation
SOURCE ../schema/001_initial_schema.sql;

-- Insert initial time slots for scheduling
INSERT INTO time_slots (day_of_week, start_time, end_time, slot_type) VALUES
-- Monday time slots
('MONDAY', '08:00:00', '09:30:00', 'MORNING'),
('MONDAY', '09:45:00', '11:15:00', 'MORNING'),
('MONDAY', '11:30:00', '13:00:00', 'AFTERNOON'),
('MONDAY', '13:15:00', '14:45:00', 'AFTERNOON'),
('MONDAY', '15:00:00', '16:30:00', 'AFTERNOON'),
('MONDAY', '16:45:00', '18:15:00', 'EVENING'),
('MONDAY', '18:30:00', '20:00:00', 'EVENING'),

-- Tuesday time slots
('TUESDAY', '08:00:00', '09:30:00', 'MORNING'),
('TUESDAY', '09:45:00', '11:15:00', 'MORNING'),
('TUESDAY', '11:30:00', '13:00:00', 'AFTERNOON'),
('TUESDAY', '13:15:00', '14:45:00', 'AFTERNOON'),
('TUESDAY', '15:00:00', '16:30:00', 'AFTERNOON'),
('TUESDAY', '16:45:00', '18:15:00', 'EVENING'),
('TUESDAY', '18:30:00', '20:00:00', 'EVENING'),

-- Wednesday time slots
('WEDNESDAY', '08:00:00', '09:30:00', 'MORNING'),
('WEDNESDAY', '09:45:00', '11:15:00', 'MORNING'),
('WEDNESDAY', '11:30:00', '13:00:00', 'AFTERNOON'),
('WEDNESDAY', '13:15:00', '14:45:00', 'AFTERNOON'),
('WEDNESDAY', '15:00:00', '16:30:00', 'AFTERNOON'),
('WEDNESDAY', '16:45:00', '18:15:00', 'EVENING'),
('WEDNESDAY', '18:30:00', '20:00:00', 'EVENING'),

-- Thursday time slots
('THURSDAY', '08:00:00', '09:30:00', 'MORNING'),
('THURSDAY', '09:45:00', '11:15:00', 'MORNING'),
('THURSDAY', '11:30:00', '13:00:00', 'AFTERNOON'),
('THURSDAY', '13:15:00', '14:45:00', 'AFTERNOON'),
('THURSDAY', '15:00:00', '16:30:00', 'AFTERNOON'),
('THURSDAY', '16:45:00', '18:15:00', 'EVENING'),
('THURSDAY', '18:30:00', '20:00:00', 'EVENING'),

-- Friday time slots
('FRIDAY', '08:00:00', '09:30:00', 'MORNING'),
('FRIDAY', '09:45:00', '11:15:00', 'MORNING'),
('FRIDAY', '11:30:00', '13:00:00', 'AFTERNOON'),
('FRIDAY', '13:15:00', '14:45:00', 'AFTERNOON'),
('FRIDAY', '15:00:00', '16:30:00', 'AFTERNOON'),
('FRIDAY', '16:45:00', '18:15:00', 'EVENING'),

-- Saturday time slots (limited)
('SATURDAY', '08:00:00', '11:00:00', 'MORNING'),
('SATURDAY', '11:15:00', '14:15:00', 'AFTERNOON'),
('SATURDAY', '14:30:00', '17:30:00', 'AFTERNOON');

-- Create default admin user (password should be changed immediately)
INSERT INTO users (username, email, password_hash, first_name, last_name, role, is_active) VALUES
('admin', 'admin@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'System', 'Administrator', 'ADMIN', TRUE);

-- Insert default departments
INSERT INTO departments (name, code, description) VALUES
('Computer Science', 'CS', 'Department of Computer Science and Engineering'),
('Mathematics', 'MATH', 'Department of Mathematics and Statistics'),
('Physics', 'PHYS', 'Department of Physics'),
('Chemistry', 'CHEM', 'Department of Chemistry'),
('Biology', 'BIO', 'Department of Biological Sciences'),
('Engineering', 'ENG', 'College of Engineering'),
('Business', 'BUS', 'School of Business Administration'),
('Liberal Arts', 'LA', 'College of Liberal Arts'),
('Medicine', 'MED', 'School of Medicine'),
('Law', 'LAW', 'School of Law');

-- Create default academic year and semester
INSERT INTO semesters (name, academic_year, semester_type, start_date, end_date, is_current, registration_deadline) VALUES
('Fall 2024', '2024-2025', 'FALL', '2024-08-26', '2024-12-15', TRUE, '2024-08-20'),
('Spring 2025', '2024-2025', 'SPRING', '2025-01-13', '2025-05-10', FALSE, '2025-01-08'),
('Summer 2025', '2024-2025', 'SUMMER', '2025-06-02', '2025-07-25', FALSE, '2025-05-28');

-- Migration complete
SELECT 'Migration 001 completed successfully' as status;