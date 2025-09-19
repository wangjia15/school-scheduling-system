# School Scheduling System - Entity Relationship Diagram

## Overview
This document describes the Entity Relationship Diagram (ERD) for the School Scheduling System database schema. The design follows Third Normal Form (3NF) and supports the complex requirements of university course scheduling.

## Core Entity Groups

### 1. Authentication & Authorization Layer
```
Users (1) ←→ (1) Teachers
    ↓ (1)
Students (1) ←→ (1) Users
    ↓ (1)
Departments (1) ←→ (N) Teachers
              ←→ (N) Students
              ←→ (N) Courses
```

**Users Table**: Central authentication table
- `id` (PK) - Primary key
- `username`, `email` - Authentication credentials
- `first_name`, `last_name` - Personal information
- `role` - Authorization level (ADMIN, TEACHER, STUDENT, DEPARTMENT_HEAD)
- `is_active` - Account status
- `created_at`, `updated_at` - Audit timestamps
- `deleted_at` - Soft delete timestamp

### 2. Academic Structure
```
Departments (1) ←→ (N) Courses
              ←→ (N) Teachers
              ←→ (N) Students

Courses (1) ←→ (N) Course_Offerings
       ↓ (1,N)
Course_Prerequisites (self-referencing)
```

**Departments Table**: Organizational structure
- `id` (PK) - Primary key
- `name`, `code` - Department identification
- `head_id` (FK) - References users table
- `created_at`, `updated_at` - Audit timestamps
- `deleted_at` - Soft delete timestamp

**Courses Table**: Course catalog definitions
- `id` (PK) - Primary key
- `course_code`, `title` - Course identification
- `department_id` (FK) - Belongs to department
- `credits`, `contact_hours_per_week` - Academic requirements
- `level` - Course level (UNDERGRADUATE, GRADUATE, PHD)
- `max_students`, `min_students` - Capacity constraints
- `requires_lab` - Laboratory requirement flag

### 3. Personnel Management
```
Teachers (1) ←→ (N) Teacher_Specializations
       ↓ (1,N)
Course_Offerings (N) ←→ (1) Teachers
```

**Teachers Table**: Faculty information
- `id` (PK) - Primary key
- `user_id` (FK) - References users table
- `employee_id` - Unique employee identifier
- `department_id` (FK) - Department affiliation
- `title` - Academic title (PROFESSOR, ASSOCIATE_PROFESSOR, etc.)
- `max_weekly_hours`, `max_courses_per_semester` - Workload limits
- `office_location`, `phone` - Contact information

**Teacher_Specializations Table**: Subject expertise
- `id` (PK) - Primary key
- `teacher_id` (FK) - References teachers table
- `subject_code` - Subject area code
- `proficiency_level` - Expertise level
- `years_experience` - Experience in subject

### 4. Student Management
```
Students (1) ←→ (N) Enrollments
       ↓ (1,N)
Course_Offerings (N) ←→ (N) Enrollments
```

**Students Table**: Student information
- `id` (PK) - Primary key
- `user_id` (FK) - References users table
- `student_id` - Unique student identifier
- `department_id` (FK) - Department affiliation
- `enrollment_year`, `graduation_year` - Academic timeline
- `current_semester` - Current academic progress
- `gpa` - Academic performance
- `status` - Enrollment status (ACTIVE, INACTIVE, GRADUATED, SUSPENDED)

### 5. Resource Management
```
Classrooms (1) ←→ (N) Schedules
```

**Classrooms Table**: Physical resources
- `id` (PK) - Primary key
- `building_code`, `room_number` - Location identification
- `name` - Descriptive name
- `capacity` - Maximum occupancy
- `room_type` - Room classification
- `has_projector`, `has_computer`, `has_whiteboard` - Equipment flags
- `special_equipment` - Additional equipment (JSON)
- `is_available` - Availability status

### 6. Time & Scheduling Structure
```
Semesters (1) ←→ (N) Course_Offerings
         ↓ (1,N)
Schedules (N) ←→ (1) Semesters

Time_Slots (1) ←→ (N) Schedules
```

**Semesters Table**: Academic terms
- `id` (PK) - Primary key
- `name`, `academic_year` - Term identification
- `semester_type` - Term type (FALL, SPRING, SUMMER, WINTER)
- `start_date`, `end_date` - Term duration
- `is_current` - Current term flag
- `registration_deadline` - Registration cutoff

**Time_Slots Table**: Schedule time blocks
- `id` (PK) - Primary key
- `day_of_week` - Day of week
- `start_time`, `end_time` - Time range
- `slot_type` - Time classification (MORNING, AFTERNOON, EVENING)
- `is_active` - Active status

### 7. Scheduling Core
```
Course_Offerings (1) ←→ (N) Schedules
              ↓ (1,N)
Enrollments (N) ←→ (1) Course_Offerings
```

**Course_Offerings Table**: Specific course instances
- `id` (PK) - Primary key
- `course_id` (FK) - References courses table
- `semester_id` (FK) - References semesters table
- `section_number` - Section identifier
- `teacher_id` (FK) - Assigned teacher
- `max_enrollment`, `current_enrollment` - Enrollment tracking
- `schedule_type` - Meeting pattern
- `is_open` - Registration status

**Schedules Table**: Core scheduling assignments
- `id` (PK) - Primary key
- `course_offering_id` (FK) - References course_offerings table
- `classroom_id` (FK) - Assigned classroom
- `time_slot_id` (FK) - Assigned time slot
- `schedule_date` - Specific date
- `is_recurring` - Recurring flag
- `recurrence_pattern` - Recurrence specification

**Enrollments Table**: Student course registrations
- `id` (PK) - Primary key
- `student_id` (FK) - References students table
- `course_offering_id` (FK) - References course_offerings table
- `enrollment_date` - Registration date
- `status` - Enrollment status (ENROLLED, DROPPED, COMPLETED, etc.)
- `grade`, `grade_letter` - Academic performance

### 8. Constraint & Conflict Management
```
Scheduling_Constraints (1) ←→ (N) Schedule_Conflicts
                    ↓ (1,N)
Schedules (N) ←→ (N) Schedule_Conflicts
```

**Scheduling_Constraints Table**: Business rules
- `id` (PK) - Primary key
- `name`, `description` - Constraint description
- `constraint_type` - Constraint category
- `entity_id`, `entity_type` - Affected entity
- `constraint_data` - Constraint configuration (JSON)
- `priority` - Constraint priority level
- `is_active` - Active status

**Schedule_Conflicts Table**: Conflict tracking
- `id` (PK) - Primary key
- `conflict_type` - Conflict category
- `severity` - Impact level
- `description` - Conflict description
- `schedule_id_1`, `schedule_id_2` - Conflicting schedules
- `entity_id`, `entity_type` - Affected entity
- `resolution_status` - Resolution state
- `detected_at`, `resolved_at` - Timeline tracking

### 9. Audit & Logging
```
Users (1) ←→ (N) Audit_Log
       ↓ (1,N)
All Tables ←→ (N) Audit_Log
```

**Audit_Log Table**: Change tracking
- `id` (PK) - Primary key
- `table_name` - Affected table
- `record_id` - Affected record
- `action` - Operation type (INSERT, UPDATE, DELETE)
- `old_values`, `new_values` - Data changes (JSON)
- `user_id` (FK) - User who made change
- `action_timestamp` - Change timestamp
- `ip_address`, `user_agent` - Request metadata

## Key Relationships & Constraints

### Primary Key Relationships
- Each table has an auto-incrementing `id` primary key
- All foreign keys reference these primary keys
- Cascading delete rules ensure referential integrity

### Foreign Key Constraints
- `Users → Teachers/Students` (1:1) - User profile relationship
- `Departments → Teachers/Students/Courses` (1:N) - Organizational structure
- `Courses → Course_Offerings` (1:N) - Course instances
- `Course_Offerings → Schedules/Enrollments` (1:N) - Schedule assignments
- `Teachers → Course_Offerings/Teacher_Specializations` (1:N) - Faculty assignments
- `Students → Enrollments` (1:N) - Student registrations
- `Classrooms → Schedules` (1:N) - Resource allocation
- `Semesters → Course_Offerings` (1:N) - Term-based organization
- `Time_Slots → Schedules` (1:N) - Time-based organization

### Unique Constraints
- `Users.username`, `Users.email` - Unique authentication
- `Teachers.employee_id`, `Students.student_id` - Unique identifiers
- `Classrooms(building_code, room_number)` - Unique location
- `Courses.course_code` - Unique course identification
- `Course_Offerings(course_id, semester_id, section_number)` - Unique offering
- `Schedules(classroom_id, time_slot_id, schedule_date)` - Conflict prevention
- `Enrollments(student_id, course_offering_id)` - Unique enrollment

### Data Validation Constraints
- `CHECK (end_time > start_time)` - Time slot validation
- `CHECK (current_enrollment <= max_enrollment)` - Enrollment capacity
- `CHECK (end_date > start_date)` - Semester date validation
- `ENUM` constraints for controlled vocabularies
- `NOT NULL` constraints for required fields

## Performance Optimization

### Indexing Strategy
1. **Primary Indexes**: Auto-created on primary keys
2. **Foreign Key Indexes**: Auto-created for join optimization
3. **Composite Indexes**: Optimized for common query patterns
4. **Selective Indexes**: Based on query frequency and selectivity
5. **Full-text Indexes**: For search functionality

### Key Query Patterns Optimized
- Teacher schedule lookup by semester
- Student enrollment by department and status
- Classroom availability by time and capacity
- Course offerings by department and semester
- Conflict detection by time and location
- Audit trail by user and timestamp

## Database Views

### Pre-defined Views for Common Queries
1. **v_current_semester**: Current academic term
2. **v_active_teachers**: Active faculty with profile information
3. **v_active_students**: Active students with profile information
4. **v_course_offerings_enrollment**: Course offerings with enrollment counts
5. **v_schedule_conflicts_summary**: Conflict status overview

## Triggers for Data Integrity

### Automated Business Logic
1. **tr_update_enrollment_count**: Auto-update enrollment counts
2. **tr_update_enrollment_count_drop**: Handle enrollment status changes

This ERD provides a comprehensive foundation for the school scheduling system, supporting the complex requirements of university course scheduling while maintaining data integrity and performance optimization.