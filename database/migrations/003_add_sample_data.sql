-- Migration 003: Add Sample Data for Testing
-- Issue #001: Database Schema Design
-- This migration adds sample data for testing the scheduling system

USE school_scheduling;

-- Insert sample classrooms
INSERT INTO classrooms (building_code, room_number, name, capacity, room_type, has_projector, has_computer, has_whiteboard) VALUES
('CS', '101', 'Computer Science Lecture Hall 101', 150, 'LECTURE_HALL', TRUE, FALSE, TRUE),
('CS', '102', 'Computer Science Lecture Hall 102', 120, 'LECTURE_HALL', TRUE, FALSE, TRUE),
('CS', '201', 'Computer Laboratory 201', 30, 'COMPUTER_LAB', TRUE, TRUE, TRUE),
('CS', '202', 'Computer Laboratory 202', 30, 'COMPUTER_LAB', TRUE, TRUE, TRUE),
('CS', '301', 'Seminar Room 301', 25, 'SEMINAR_ROOM', TRUE, FALSE, TRUE),
('CS', '302', 'Seminar Room 302', 20, 'SEMINAR_ROOM', TRUE, FALSE, TRUE),
('ENG', '101', 'Engineering Lecture Hall', 200, 'LECTURE_HALL', TRUE, FALSE, TRUE),
('ENG', '201', 'Engineering Lab', 40, 'LABORATORY', TRUE, FALSE, TRUE),
('MATH', '101', 'Mathematics Classroom', 80, 'LECTURE_HALL', TRUE, FALSE, TRUE),
('PHYS', '101', 'Physics Laboratory', 35, 'LABORATORY', TRUE, FALSE, TRUE),
('CHEM', '101', 'Chemistry Laboratory', 30, 'LABORATORY', TRUE, FALSE, TRUE),
('BIO', '101', 'Biology Laboratory', 25, 'LABORATORY', TRUE, FALSE, TRUE),
('BUS', '101', 'Business Lecture Hall', 100, 'LECTURE_HALL', TRUE, FALSE, TRUE),
('LA', '101', 'Liberal Arts Classroom', 60, 'LECTURE_HALL', TRUE, FALSE, TRUE);

-- Insert sample courses
INSERT INTO courses (course_code, title, description, department_id, credits, contact_hours_per_week, theory_hours, lab_hours, level, max_students, min_students, requires_lab) VALUES
-- Computer Science courses
('CS101', 'Introduction to Computer Science', 'Fundamental concepts of programming and computer systems', 1, 3, 4.0, 3.0, 1.0, 'UNDERGRADUATE', 35, 10, TRUE),
('CS102', 'Data Structures', 'Advanced data structures and algorithms', 1, 3, 4.0, 3.0, 1.0, 'UNDERGRADUATE', 30, 8, TRUE),
('CS201', 'Database Systems', 'Database design and SQL programming', 1, 3, 4.0, 3.0, 1.0, 'UNDERGRADUATE', 30, 8, TRUE),
('CS202', 'Software Engineering', 'Software development methodologies', 1, 3, 3.0, 3.0, 0.0, 'UNDERGRADUATE', 35, 10, FALSE),
('CS301', 'Algorithm Design', 'Advanced algorithm analysis and design', 1, 3, 3.0, 3.0, 0.0, 'UNDERGRADUATE', 25, 5, FALSE),
('CS302', 'Computer Networks', 'Networking principles and protocols', 1, 3, 4.0, 3.0, 1.0, 'UNDERGRADUATE', 30, 8, TRUE),
('CS401', 'Artificial Intelligence', 'Introduction to AI concepts and applications', 1, 3, 3.0, 3.0, 0.0, 'UNDERGRADUATE', 25, 5, FALSE),
('CS501', 'Machine Learning', 'Advanced machine learning algorithms', 1, 3, 4.0, 3.0, 1.0, 'GRADUATE', 20, 3, TRUE),

-- Mathematics courses
('MATH101', 'Calculus I', 'Differential and integral calculus', 2, 4, 5.0, 4.0, 1.0, 'UNDERGRADUATE', 40, 12, FALSE),
('MATH102', 'Calculus II', 'Advanced calculus techniques', 2, 4, 5.0, 4.0, 1.0, 'UNDERGRADUATE', 35, 10, FALSE),
('MATH201', 'Linear Algebra', 'Matrix algebra and vector spaces', 2, 3, 3.0, 3.0, 0.0, 'UNDERGRADUATE', 35, 10, FALSE),
('MATH301', 'Probability and Statistics', 'Statistical analysis and probability theory', 2, 3, 3.0, 3.0, 0.0, 'UNDERGRADUATE', 35, 10, FALSE),

-- Physics courses
('PHYS101', 'General Physics I', 'Mechanics and thermodynamics', 3, 4, 5.0, 3.0, 2.0, 'UNDERGRADUATE', 35, 10, TRUE),
('PHYS102', 'General Physics II', 'Electricity and magnetism', 3, 4, 5.0, 3.0, 2.0, 'UNDERGRADUATE', 35, 10, TRUE),
('PHYS201', 'Modern Physics', 'Quantum mechanics and relativity', 3, 3, 3.0, 3.0, 0.0, 'UNDERGRADUATE', 25, 5, FALSE),

-- Business courses
('BUS101', 'Introduction to Business', 'Fundamental business concepts', 7, 3, 3.0, 3.0, 0.0, 'UNDERGRADUATE', 50, 15, FALSE),
('BUS201', 'Business Finance', 'Financial management principles', 7, 3, 3.0, 3.0, 0.0, 'UNDERGRADUATE', 40, 12, FALSE),
('BUS301', 'Marketing Management', 'Strategic marketing concepts', 7, 3, 3.0, 3.0, 0.0, 'UNDERGRADUATE', 35, 10, FALSE);

-- Set up course prerequisites
INSERT INTO course_prerequisites (course_id, prerequisite_course_id, is_mandatory, minimum_grade) VALUES
-- CS prerequisites
((SELECT id FROM courses WHERE course_code = 'CS102'), (SELECT id FROM courses WHERE course_code = 'CS101'), TRUE, 60.00),
((SELECT id FROM courses WHERE course_code = 'CS201'), (SELECT id FROM courses WHERE course_code = 'CS102'), TRUE, 65.00),
((SELECT id FROM courses WHERE course_code = 'CS202'), (SELECT id FROM courses WHERE course_code = 'CS102'), TRUE, 65.00),
((SELECT id FROM courses WHERE course_code = 'CS301'), (SELECT id FROM courses WHERE course_code = 'CS102'), TRUE, 70.00),
((SELECT id FROM courses WHERE course_code = 'CS302'), (SELECT id FROM courses WHERE course_code = 'CS102'), TRUE, 65.00),
((SELECT id FROM courses WHERE course_code = 'CS401'), (SELECT id FROM courses WHERE course_code = 'CS301'), TRUE, 70.00),
((SELECT id FROM courses WHERE course_code = 'CS501'), (SELECT id FROM courses WHERE course_code = 'CS301'), TRUE, 75.00),

-- Math prerequisites
((SELECT id FROM courses WHERE course_code = 'MATH102'), (SELECT id FROM courses WHERE course_code = 'MATH101'), TRUE, 60.00),
((SELECT id FROM courses WHERE course_code = 'MATH201'), (SELECT id FROM courses WHERE course_code = 'MATH102'), TRUE, 65.00),
((SELECT id FROM courses WHERE course_code = 'MATH301'), (SELECT id FROM courses WHERE course_code = 'MATH101'), TRUE, 60.00),

-- Physics prerequisites
((SELECT id FROM courses WHERE course_code = 'PHYS102'), (SELECT id FROM courses WHERE course_code = 'PHYS101'), TRUE, 60.00),
((SELECT id FROM courses WHERE course_code = 'PHYS201'), (SELECT id FROM courses WHERE course_code = 'PHYS102'), TRUE, 65.00),

-- Business prerequisites
((SELECT id FROM courses WHERE course_code = 'BUS201'), (SELECT id FROM courses WHERE course_code = 'BUS101'), TRUE, 60.00),
((SELECT id FROM courses WHERE course_code = 'BUS301'), (SELECT id FROM courses WHERE course_code = 'BUS101'), TRUE, 60.00);

-- Insert sample users and teachers
INSERT INTO users (username, email, password_hash, first_name, last_name, role, is_active) VALUES
('john.smith', 'john.smith@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'John', 'Smith', 'TEACHER', TRUE),
('jane.doe', 'jane.doe@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Jane', 'Doe', 'TEACHER', TRUE),
('robert.johnson', 'robert.johnson@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Robert', 'Johnson', 'TEACHER', TRUE),
('mary.williams', 'mary.williams@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Mary', 'Williams', 'TEACHER', TRUE),
('david.brown', 'david.brown@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'David', 'Brown', 'TEACHER', TRUE),
('sarah.davis', 'sarah.davis@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Sarah', 'Davis', 'TEACHER', TRUE);

-- Insert teacher records
INSERT INTO teachers (user_id, employee_id, department_id, title, max_weekly_hours, max_courses_per_semester, office_location, phone) VALUES
((SELECT id FROM users WHERE username = 'john.smith'), 'EMP001', 1, 'PROFESSOR', 40.0, 5, 'CS-301', '555-001'),
((SELECT id FROM users WHERE username = 'jane.doe'), 'EMP002', 1, 'ASSOCIATE_PROFESSOR', 40.0, 4, 'CS-302', '555-002'),
((SELECT id FROM users WHERE username = 'robert.johnson'), 'EMP003', 2, 'PROFESSOR', 40.0, 5, 'MATH-201', '555-003'),
((SELECT id FROM users WHERE username = 'mary.williams'), 'EMP004', 3, 'ASSISTANT_PROFESSOR', 40.0, 4, 'PHYS-101', '555-004'),
((SELECT id FROM users WHERE username = 'david.brown'), 'EMP005', 7, 'ASSOCIATE_PROFESSOR', 40.0, 4, 'BUS-201', '555-005'),
((SELECT id FROM users WHERE username = 'sarah.davis'), 'EMP006', 1, 'INSTRUCTOR', 30.0, 3, 'CS-201', '555-006');

-- Insert teacher specializations
INSERT INTO teacher_specializations (teacher_id, subject_code, proficiency_level, years_experience) VALUES
((SELECT id FROM teachers WHERE employee_id = 'EMP001'), 'Computer Science', 'EXPERT', 15),
((SELECT id FROM teachers WHERE employee_id = 'EMP001'), 'Algorithms', 'EXPERT', 15),
((SELECT id FROM teachers WHERE employee_id = 'EMP001'), 'Machine Learning', 'ADVANCED', 8),
((SELECT id FROM teachers WHERE employee_id = 'EMP002'), 'Computer Science', 'ADVANCED', 10),
((SELECT id FROM teachers WHERE employee_id = 'EMP002'), 'Database Systems', 'EXPERT', 10),
((SELECT id FROM teachers WHERE employee_id = 'EMP003'), 'Mathematics', 'EXPERT', 20),
((SELECT id FROM teachers WHERE employee_id = 'EMP003'), 'Statistics', 'ADVANCED', 15),
((SELECT id FROM teachers WHERE employee_id = 'EMP004'), 'Physics', 'ADVANCED', 8),
((SELECT id FROM teachers WHERE employee_id = 'EMP004'), 'Quantum Mechanics', 'INTERMEDIATE', 5),
((SELECT id FROM teachers WHERE employee_id = 'EMP005'), 'Business', 'ADVANCED', 12),
((SELECT id FROM teachers WHERE employee_id = 'EMP005'), 'Finance', 'EXPERT', 12),
((SELECT id FROM teachers WHERE employee_id = 'EMP006'), 'Computer Science', 'INTERMEDIATE', 5),
((SELECT id FROM teachers WHERE employee_id = 'EMP006'), 'Programming', 'ADVANCED', 5);

-- Insert sample students
INSERT INTO users (username, email, password_hash, first_name, last_name, role, is_active) VALUES
('alice.jones', 'alice.jones@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Alice', 'Jones', 'STUDENT', TRUE),
('bob.miller', 'bob.miller@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Bob', 'Miller', 'STUDENT', TRUE),
('charlie.wilson', 'charlie.wilson@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Charlie', 'Wilson', 'STUDENT', TRUE),
('diana.taylor', 'diana.taylor@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Diana', 'Taylor', 'STUDENT', TRUE),
('edward.clark', 'edward.clark@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Edward', 'Clark', 'STUDENT', TRUE),
('fiona.white', 'fiona.white@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Fiona', 'White', 'STUDENT', TRUE),
('george.harris', 'george.harris@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'George', 'Harris', 'STUDENT', TRUE),
('helen.martin', 'helen.martin@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Helen', 'Martin', 'STUDENT', TRUE),
('ivan.thompson', 'ivan.thompson@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Ivan', 'Thompson', 'STUDENT', TRUE),
('julia.garcia', 'julia.garcia@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Julia', 'Garia', 'STUDENT', TRUE);

-- Insert student records
INSERT INTO students (user_id, student_id, department_id, enrollment_year, current_semester, gpa, status) VALUES
((SELECT id FROM users WHERE username = 'alice.jones'), 'STU001', 1, 2024, 1, 3.85, 'ACTIVE'),
((SELECT id FROM users WHERE username = 'bob.miller'), 'STU002', 1, 2024, 1, 3.45, 'ACTIVE'),
((SELECT id FROM users WHERE username = 'charlie.wilson'), 'STU003', 2, 2024, 1, 3.70, 'ACTIVE'),
((SELECT id FROM users WHERE username = 'diana.taylor'), 'STU004', 3, 2024, 1, 3.90, 'ACTIVE'),
((SELECT id FROM users WHERE username = 'edward.clark'), 'STU005', 7, 2024, 1, 3.50, 'ACTIVE'),
((SELECT id FROM users WHERE username = 'fiona.white'), 'STU006', 1, 2024, 1, 3.60, 'ACTIVE'),
((SELECT id FROM users WHERE username = 'george.harris'), 'STU007', 2, 2024, 1, 3.30, 'ACTIVE'),
((SELECT id FROM users WHERE username = 'helen.martin'), 'STU008', 7, 2024, 1, 3.75, 'ACTIVE'),
((SELECT id FROM users WHERE username = 'ivan.thompson'), 'STU009', 1, 2024, 1, 3.20, 'ACTIVE'),
((SELECT id FROM users WHERE username = 'julia.garcia'), 'STU010', 3, 2024, 1, 3.95, 'ACTIVE');

-- Insert sample course offerings
INSERT INTO course_offerings (course_id, semester_id, section_number, teacher_id, max_enrollment, current_enrollment, schedule_type, is_open) VALUES
-- Fall 2024 Computer Science courses
((SELECT id FROM courses WHERE course_code = 'CS101'), (SELECT id FROM semesters WHERE name = 'Fall 2024'), '001', (SELECT id FROM teachers WHERE employee_id = 'EMP006'), 35, 0, 'REGULAR', TRUE),
((SELECT id FROM courses WHERE course_code = 'CS101'), (SELECT id FROM semesters WHERE name = 'Fall 2024'), '002', (SELECT id FROM teachers WHERE employee_id = 'EMP006'), 35, 0, 'REGULAR', TRUE),
((SELECT id FROM courses WHERE course_code = 'CS102'), (SELECT id FROM semesters WHERE name = 'Fall 2024'), '001', (SELECT id FROM teachers WHERE employee_id = 'EMP002'), 30, 0, 'REGULAR', TRUE),
((SELECT id FROM courses WHERE course_code = 'CS201'), (SELECT id FROM semesters WHERE name = 'Fall 2024'), '001', (SELECT id FROM teachers WHERE employee_id = 'EMP002'), 30, 0, 'REGULAR', TRUE),
((SELECT id FROM courses WHERE course_code = 'CS301'), (SELECT id FROM semesters WHERE name = 'Fall 2024'), '001', (SELECT id FROM teachers WHERE employee_id = 'EMP001'), 25, 0, 'REGULAR', TRUE),

-- Fall 2024 Mathematics courses
((SELECT id FROM courses WHERE course_code = 'MATH101'), (SELECT id FROM semesters WHERE name = 'Fall 2024'), '001', (SELECT id FROM teachers WHERE employee_id = 'EMP003'), 40, 0, 'REGULAR', TRUE),
((SELECT id FROM courses WHERE course_code = 'MATH102'), (SELECT id FROM semesters WHERE name = 'Fall 2024'), '001', (SELECT id FROM teachers WHERE employee_id = 'EMP003'), 35, 0, 'REGULAR', TRUE),
((SELECT id FROM courses WHERE course_code = 'MATH201'), (SELECT id FROM semesters WHERE name = 'Fall 2024'), '001', (SELECT id FROM teachers WHERE employee_id = 'EMP003'), 35, 0, 'REGULAR', TRUE),

-- Fall 2024 Physics courses
((SELECT id FROM courses WHERE course_code = 'PHYS101'), (SELECT id FROM semesters WHERE name = 'Fall 2024'), '001', (SELECT id FROM teachers WHERE employee_id = 'EMP004'), 35, 0, 'REGULAR', TRUE),
((SELECT id FROM courses WHERE course_code = 'PHYS102'), (SELECT id FROM semesters WHERE name = 'Fall 2024'), '001', (SELECT id FROM teachers WHERE employee_id = 'EMP004'), 35, 0, 'REGULAR', TRUE),

-- Fall 2024 Business courses
((SELECT id FROM courses WHERE course_code = 'BUS101'), (SELECT id FROM semesters WHERE name = 'Fall 2024'), '001', (SELECT id FROM teachers WHERE employee_id = 'EMP005'), 50, 0, 'REGULAR', TRUE),
((SELECT id FROM courses WHERE course_code = 'BUS201'), (SELECT id FROM semesters WHERE name = 'Fall 2024'), '001', (SELECT id FROM teachers WHERE employee_id = 'EMP005'), 40, 0, 'REGULAR', TRUE);

-- Migration complete
SELECT 'Migration 003 completed successfully - Sample data added' as status;