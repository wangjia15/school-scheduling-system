# School Scheduling System Database

## Overview
This directory contains the complete database schema and migration scripts for the School Scheduling System. The database is designed to handle 20,000 students, 500 teachers, and 150 classrooms with real-time conflict detection capabilities.

## Architecture
- **Database**: MySQL 8.x
- **Design Pattern**: Third Normal Form (3NF)
- **Performance**: Optimized for large-scale scheduling operations
- **Features**: Soft delete, audit trail, conflict detection

## Directory Structure
```
database/
├── schema/
│   └── 001_initial_schema.sql          # Complete database schema
├── migrations/
│   ├── 001_create_initial_schema.sql   # Initial schema migration
│   ├── 002_add_performance_indexes.sql # Performance optimization
│   └── 003_add_sample_data.sql         # Sample test data
├── docs/
│   ├── schema_erd.md                   # Entity Relationship Diagram
│   └── performance_optimization.md     # Performance optimization guide
└── README.md                           # This file
```

## Core Entities

### Authentication & Authorization
- **Users** - Central authentication with role-based access
- **Departments** - Organizational structure

### Academic Structure
- **Courses** - Course catalog with prerequisites
- **Course_Offerings** - Specific course instances per semester
- **Semesters** - Academic terms and time periods

### Personnel Management
- **Teachers** - Faculty information with specializations
- **Teacher_Specializations** - Subject expertise tracking

### Student Management
- **Students** - Student profiles and enrollment tracking
- **Enrollments** - Student course registrations

### Resource Management
- **Classrooms** - Physical room inventory and equipment
- **Time_Slots** - Scheduling time blocks

### Scheduling Core
- **Schedules** - Core schedule assignments
- **Scheduling_Constraints** - Business rules and limitations
- **Schedule_Conflicts** - Conflict detection and tracking

### Audit & Logging
- **Audit_Log** - Comprehensive change tracking

## Key Features

### Data Integrity
- Third Normal Form compliance
- Referential integrity constraints
- Business rule validation
- Cascade delete rules

### Performance Optimization
- Composite indexes for scheduling queries
- Full-text search capabilities
- Optimized query patterns
- Connection pooling recommendations

### Soft Delete
- `deleted_at` timestamps for data preservation
- Query filtering for active records
- Audit trail for all changes

### Conflict Detection
- Real-time conflict identification
- Teacher double-booking prevention
- Classroom availability validation
- Student schedule conflict detection

## Quick Start

### 1. Create Database
```sql
CREATE DATABASE school_scheduling;
USE school_scheduling;
```

### 2. Run Migrations
```bash
# Run migrations in order
mysql -u username -p school_scheduling < migrations/001_create_initial_schema.sql
mysql -u username -p school_scheduling < migrations/002_add_performance_indexes.sql
mysql -u username -p school_scheduling < migrations/003_add_sample_data.sql
```

### 3. Verify Installation
```sql
-- Check table count
SELECT COUNT(*) as table_count FROM information_schema.tables
WHERE table_schema = 'school_scheduling';

-- Check sample data
SELECT COUNT(*) as teachers FROM teachers;
SELECT COUNT(*) as courses FROM courses;
SELECT COUNT(*) as classrooms FROM classrooms;
```

## Sample Data
The database includes sample data for testing:
- 14 classrooms across 6 departments
- 18 courses with prerequisites
- 6 teachers with specializations
- 10 students
- 11 course offerings for Fall 2024
- Complete time slot schedule (Monday-Saturday)

## Performance Benchmarks

### Target Performance
- **Schedule Generation**: < 30 minutes for 20K students
- **Database Queries**: < 3 seconds response time
- **Conflict Detection**: < 1 second real-time detection
- **Concurrent Users**: 50+ administrators

### Scale Support
- **Students**: 20,000+
- **Teachers**: 500+
- **Classrooms**: 150+
- **Courses**: 500+ per semester
- **Schedules**: 3,000+ per semester

## Documentation

### Schema Documentation
- **`docs/schema_erd.md`** - Complete Entity Relationship Diagram
- **`docs/performance_optimization.md`** - Performance optimization guide

### Key Views
- `v_current_semester` - Current academic term
- `v_active_teachers` - Active faculty with profiles
- `v_active_students` - Active students with profiles
- `v_course_offerings_enrollment` - Course offerings with enrollment counts
- `v_schedule_conflicts_summary` - Conflict status overview

## Maintenance

### Regular Tasks
1. **Update Statistics**: Weekly index statistics update
2. **Backup**: Daily database backups
3. **Archive**: Quarterly archive of old schedules
4. **Monitor**: Performance monitoring and optimization

### Performance Monitoring
```sql
-- Monitor slow queries
SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 10;

-- Check index usage
SELECT * FROM sys.schema_unused_indexes
WHERE object_schema = 'school_scheduling';
```

## Security Considerations

### Access Control
- Role-based access control implemented at application level
- Database user with limited privileges for application
- Separate admin user for database maintenance

### Data Protection
- Password hashing for user authentication
- Audit logging for all data changes
- Soft delete prevents accidental data loss

## Troubleshooting

### Common Issues
1. **Connection Errors**: Check MySQL connection settings
2. **Slow Queries**: Review index usage and query optimization
3. **Memory Issues**: Adjust MySQL buffer pool size
4. **Constraint Violations**: Check data integrity constraints

### Support
For database-related issues, refer to the documentation in the `docs/` directory or consult the performance optimization guide.

## Next Steps

This database schema provides the foundation for the School Scheduling System. The next development phases will include:

1. **Domain Model Development** - Java entity classes
2. **MyBatis Mappers** - Data access layer
3. **REST API Development** - Service endpoints
4. **Scheduling Algorithm** - Constraint-based optimization
5. **Frontend Integration** - Vue.js components

For more information about the overall project architecture, refer to the epic documentation in `.claude/epics/school-scheduling-system/`.