// Course Management Module Components Export
// This file exports all course management related components

// Core Course Management
export { default as CourseList } from './CourseList.vue'
export { default as CourseForm } from './CourseForm.vue'

// Prerequisite Management
export { default as PrerequisiteTree } from './PrerequisiteTree.vue'
export { default as PrerequisiteTreeManager } from './PrerequisiteTreeManager.vue'
export { default as PrerequisiteTreeVisualizer } from './PrerequisiteTreeVisualizer.vue'

// Enrollment Management
export { default as EnrollmentManagement } from './EnrollmentManagement.vue'

// Course Catalog
export { default as AdvancedCourseCatalog } from './AdvancedCourseCatalog.vue'
export { default as CourseComparison } from './CourseComparison.vue'

// Scheduling
export { default as CourseSchedulingInterface } from './CourseSchedulingInterface.vue'

// Analytics
export { default as CourseAnalyticsDashboard } from './CourseAnalyticsDashboard.vue'

// Existing Components
export { default as CapacityManagement } from './CapacityManagement.vue'
export { default as ConflictResolution } from './ConflictResolution.vue'
export { default as CourseCalendarView } from './CourseCalendarView.vue'
export { default as CourseCatalog } from './CourseCatalog.vue'
export { default as CourseScheduleBuilder } from './CourseScheduleBuilder.vue'
export { default as CourseScheduling } from './CourseScheduling.vue'

// Types and Services
export type {
  Course,
  CoursePrerequisite,
  CourseRequest,
  CourseFilter,
  CourseSearchResponse,
  CourseTreeNode
} from '@/types/course'

export { courseService } from '@/services/courseService'

// Module Info
export const COURSE_MODULE_INFO = {
  name: 'Course Management Module',
  version: '1.0.0',
  description: 'Comprehensive course management system with prerequisite trees, enrollment tracking, and advanced analytics',
  features: [
    'Course CRUD Operations',
    'Visual Prerequisite Tree Management',
    'Enrollment Tracking & Waitlisting',
    'Advanced Course Catalog',
    'Course Scheduling Interface',
    'Capacity Management',
    'Analytics Dashboard',
    'Course Comparison Tools'
  ],
  components: [
    'CourseList',
    'CourseForm',
    'PrerequisiteTreeManager',
    'EnrollmentManagement',
    'AdvancedCourseCatalog',
    'CourseSchedulingInterface',
    'CourseAnalyticsDashboard'
  ]
}

// Utility Functions
export const getCourseLevelVariant = (level: string) => {
  switch (level) {
    case 'UNDERGRADUATE': return 'default'
    case 'GRADUATE': return 'secondary'
    case 'PHD': return 'destructive'
    default: return 'default'
  }
}

export const formatCourseLevel = (level: string) => {
  return level.charAt(0) + level.slice(1).toLowerCase()
}

export const getCourseType = (course: any) => {
  if (course.isHybridCourse?.()) return 'Hybrid'
  if (course.isLabCourse?.()) return 'Lab'
  return 'Lecture'
}

export const calculateEnrollmentPercentage = (enrolled: number, capacity: number) => {
  return capacity > 0 ? Math.round((enrolled / capacity) * 100) : 0
}

export const getCapacityStatus = (enrolled: number, capacity: number) => {
  const percentage = calculateEnrollmentPercentage(enrolled, capacity)
  if (percentage >= 100) return 'Full'
  if (percentage >= 80) return 'Nearly Full'
  if (percentage >= 50) return 'Good Availability'
  return 'Low Enrollment'
}

// Validation Functions
export const validateCourseData = (course: any) => {
  const errors: string[] = []

  if (!course.courseCode) errors.push('Course code is required')
  if (!course.title) errors.push('Course title is required')
  if (!course.departmentId) errors.push('Department is required')
  if (!course.credits || course.credits < 1 || course.credits > 6) {
    errors.push('Credits must be between 1 and 6')
  }
  if (!course.level) errors.push('Course level is required')
  if (course.minStudents && course.maxStudents && course.minStudents > course.maxStudents) {
    errors.push('Minimum students cannot exceed maximum students')
  }

  return {
    isValid: errors.length === 0,
    errors
  }
}

export const validatePrerequisites = (prerequisites: any[]) => {
  const issues: string[] = []

  // Check for circular dependencies (simplified)
  const courseIds = new Set()
  prerequisites.forEach(prereq => {
    if (courseIds.has(prereq.prerequisiteCourseId)) {
      issues.push(`Duplicate prerequisite: ${prereq.prerequisiteCourseId}`)
    }
    courseIds.add(prereq.prerequisiteCourseId)
  })

  return {
    isValid: issues.length === 0,
    issues
  }
}

// Analytics Helpers
export const calculateCourseMetrics = (courses: any[]) => {
  const totalCourses = courses.length
  const totalEnrollments = courses.reduce((sum, course) => sum + (course.enrollments || 0), 0)
  const averageClassSize = totalCourses > 0 ? Math.round(totalEnrollments / totalCourses) : 0
  const activeCourses = courses.filter(course => course.isActive).length

  return {
    totalCourses,
    totalEnrollments,
    averageClassSize,
    activeCourses,
    inactiveCourses: totalCourses - activeCourses
  }
}

export const generateCourseRecommendations = (studentProfile: any, courses: any[]) => {
  // Simple recommendation algorithm
  const recommendations = courses
    .filter(course => {
      // Filter based on student level, completed prerequisites, etc.
      return course.isActive && course.level === studentProfile.level
    })
    .sort((a, b) => {
      // Sort by enrollment (popular courses) or relevance
      return (b.enrollments || 0) - (a.enrollments || 0)
    })
    .slice(0, 5)

  return recommendations
}