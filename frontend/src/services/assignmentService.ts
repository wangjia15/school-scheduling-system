import api from '@/utils/api'

export interface Course {
  id: number
  courseCode: string
  courseName: string
  department: {
    id: number
    name: string
  }
  credits: number
  weeklyHours: number
  maxStudents: number
  semester: string
  year: number
  requiredSpecializations: string[]
  assignedTeacher?: Teacher | null
  schedule?: {
    dayOfWeek: string
    startTime: string
    endTime: string
    classroom: {
      id: number
      name: string
      building: string
      capacity: number
    }
  }
  hasConflicts: boolean
  conflictCount: number
  createdAt: string
  updatedAt: string
}

export interface Teacher {
  id: number
  employeeId: string
  userId: number
  departmentId: number
  title: string
  maxWeeklyHours: number
  maxCoursesPerSemester: number
  officeLocation?: string
  phone?: string
  user: {
    id: number
    firstName: string
    lastName: string
    email: string
    isActive: boolean
  }
  department: {
    id: number
    name: string
  }
  specializations: TeacherSpecialization[]
  currentWorkload: number
  availableHours: number
  createdAt: string
  updatedAt: string
}

export interface TeacherSpecialization {
  id: number
  teacherId: number
  subjectCode: string
  subjectName?: string
  proficiencyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT'
  yearsExperience: number
  certified?: boolean
  certificationDetails?: string
  createdAt: string
  updatedAt: string
}

export interface AssignmentConflict {
  id: number
  courseId: number
  teacherId: number
  conflictType: 'SCHEDULE_OVERLAP' | 'WORKLOAD_EXCEEDED' | 'ROOM_CONFLICT' | 'PREREQUISITE_CONFLICT'
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  description: string
  conflictingCourses: {
    id: number
    courseCode: string
    courseName: string
    schedule: {
      dayOfWeek: string
      startTime: string
      endTime: string
    }
  }[]
  suggestedResolution: string
  isResolved: boolean
  createdAt: string
}

export interface AssignmentFilters {
  search?: string
  departmentId?: number
  status?: 'assigned' | 'unassigned' | 'conflict'
  page?: number
  size?: number
  sort?: string
  direction?: 'ASC' | 'DESC'
}

export interface PaginatedCourses {
  content: Course[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export interface AssignmentRequest {
  teacherId: number
  schedule?: {
    dayOfWeek: string
    startTime: string
    endTime: string
    classroomId: number
  }
}

export interface BulkAssignmentRequest {
  assignments: {
    courseId: number
    teacherId: number
    schedule?: {
      dayOfWeek: string
      startTime: string
      endTime: string
      classroomId: number
    }
  }[]
}

export interface ConflictResolutionRequest {
  conflictId: number
  resolution: 'ACCEPT_CONFLICT' | 'CHANGE_TEACHER' | 'CHANGE_SCHEDULE' | 'CHANGE_ROOM' | 'CANCEL_ASSIGNMENT'
  resolutionDetails?: string
}

class AssignmentService {
  async getCourses(filters: AssignmentFilters = {}): Promise<PaginatedCourses> {
    const params = new URLSearchParams()

    if (filters.search) params.append('search', filters.search)
    if (filters.departmentId) params.append('departmentId', filters.departmentId.toString())
    if (filters.status) params.append('status', filters.status)
    if (filters.page !== undefined) params.append('page', filters.page.toString())
    if (filters.size !== undefined) params.append('size', filters.size.toString())
    if (filters.sort) params.append('sort', filters.sort)
    if (filters.direction) params.append('direction', filters.direction)

    const response = await api.get(`/courses/assignments?${params.toString()}`)
    return response.data.data
  }

  async getCourseById(id: number): Promise<Course> {
    const response = await api.get(`/courses/${id}`)
    return response.data.data
  }

  async getAvailableTeachers(): Promise<Teacher[]> {
    const response = await api.get('/teachers/available-for-assignment')
    return response.data.data
  }

  async getTeachersForCourse(courseId: number): Promise<Teacher[]> {
    const response = await api.get(`/courses/${courseId}/available-teachers`)
    return response.data.data
  }

  async assignTeacherToCourse(courseId: number, request: AssignmentRequest): Promise<Course> {
    const response = await api.post(`/courses/${courseId}/assign-teacher`, request)
    return response.data.data
  }

  async unassignTeacher(courseId: number): Promise<void> {
    await api.delete(`/courses/${courseId}/assign-teacher`)
  }

  async bulkAssignTeachers(request: BulkAssignmentRequest): Promise<{ success: number; failed: number; errors: string[] }> {
    const response = await api.post('/courses/bulk-assign', request)
    return response.data.data
  }

  async getCourseConflicts(courseId: number): Promise<AssignmentConflict[]> {
    const response = await api.get(`/courses/${courseId}/conflicts`)
    return response.data.data
  }

  async getAllConflicts(filters?: { severity?: string; isResolved?: boolean }): Promise<AssignmentConflict[]> {
    const params = new URLSearchParams()
    if (filters?.severity) params.append('severity', filters.severity)
    if (filters?.isResolved !== undefined) params.append('isResolved', filters.isResolved.toString())

    const response = await api.get(`/assignments/conflicts?${params.toString()}`)
    return response.data.data
  }

  async resolveConflict(conflictId: number, request: ConflictResolutionRequest): Promise<AssignmentConflict> {
    const response = await api.put(`/assignments/conflicts/${conflictId}/resolve`, request)
    return response.data.data
  }

  async autoResolveConflicts(courseId?: number): Promise<{ resolved: number; remaining: number }> {
    const params = courseId ? `?courseId=${courseId}` : ''
    const response = await api.post(`/assignments/conflicts/auto-resolve${params}`)
    return response.data.data
  }

  async getAssignmentStats(): Promise<{
    totalAssignments: number
    activeAssignments: number
    unassignedCourses: number
    conflicts: number
    averageTeacherLoad: number
    departmentStats: Array<{
      departmentId: number
      departmentName: string
      totalCourses: number
      assignedCourses: number
      averageWorkload: number
    }>
  }> {
    const response = await api.get('/assignments/stats')
    return response.data.data
  }

  async validateAssignment(courseId: number, teacherId: number, schedule?: {
    dayOfWeek: string
    startTime: string
    endTime: string
    classroomId?: number
  }): Promise<{
    isValid: boolean
    conflicts: AssignmentConflict[]
    warnings: string[]
    suggestions: string[]
  }> {
    const response = await api.post(`/courses/${courseId}/validate-assignment`, {
      teacherId,
      schedule
    })
    return response.data.data
  }

  async getTeacherScheduleConflicts(teacherId: number): Promise<AssignmentConflict[]> {
    const response = await api.get(`/teachers/${teacherId}/schedule-conflicts`)
    return response.data.data
  }

  async getOptimalAssignments(courseIds: number[]): Promise<Array<{
    courseId: number
    recommendedTeacherId: number
    confidence: number
    reasoning: string
  }>> {
    const response = await api.post('/assignments/optimal-suggestions', { courseIds })
    return response.data.data
  }
}

export default new AssignmentService()