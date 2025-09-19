package com.school.scheduling.validation;

/**
 * Validation groups for different validation contexts
 */
public interface ValidationGroups {

    /**
     * Basic validation for entity creation
     */
    interface Create {
    }

    /**
     * Validation for entity updates
     */
    interface Update {
    }

    /**
     * Validation for entity deletion
     */
    interface Delete {
    }

    /**
     * Validation for scheduling operations
     */
    interface Scheduling {
    }

    /**
     * Validation for enrollment operations
     */
    interface Enrollment {
    }

    /**
     * Validation for teacher assignment
     */
    interface TeacherAssignment {
    }

    /**
     * Validation for classroom assignment
     */
    interface ClassroomAssignment {
    }

    /**
     * Validation for course prerequisites
     */
    interface Prerequisites {
    }

    /**
     * Validation for availability checking
     */
    interface Availability {
    }

    /**
     * Validation for capacity limits
     */
    interface Capacity {
    }

    /**
     * Validation for time slot conflicts
     */
    interface Conflict {
    }

    /**
     * Validation for business rules
     */
    interface BusinessRules {
    }

    /**
     * Validation for administrative operations
     */
    interface Admin {
    }

    /**
     * Validation for reporting operations
     */
    interface Reporting {
    }

    /**
     * Validation for import/export operations
     */
    interface ImportExport {
    }

    /**
     * Validation for bulk operations
     */
    interface Bulk {
    }

    /**
     * Combined validation for creation with business rules
     */
    interface CreateWithBusinessRules extends Create, BusinessRules {
    }

    /**
     * Combined validation for scheduling with all constraints
     */
    interface FullScheduling extends Scheduling, TeacherAssignment, ClassroomAssignment,
                                    Capacity, Conflict, BusinessRules {
    }

    /**
     * Combined validation for enrollment with prerequisites
     */
    interface FullEnrollment extends Enrollment, Prerequisites, Capacity, BusinessRules {
    }
}