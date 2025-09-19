import api from '@/utils/api'

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
  }
  department: {
    id: number
    name: string
  }
  specializations: TeacherSpecialization[]
  createdAt: string
  updatedAt: string
}

export interface TeacherSpecialization {
  id: number
  teacherId: number
  subjectCode: string
  proficiencyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT'
  yearsExperience: number
  certified?: boolean
  certificationDetails?: string
  subjectName?: string
  createdAt: string
  updatedAt: string
}

export interface TeacherAvailability {
  id: number
  teacherId: number
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY'
  startTime: string
  endTime: string
  availabilityType: 'PREFERRED' | 'AVAILABLE' | 'UNAVAILABLE' | 'RESTRICTED'
  maxClasses: number
  breakDuration: number
  requiresBreakBetweenClasses: boolean
  isRecurring: boolean
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface TeacherRequest {
  employeeId: string
  userId: number
  departmentId: number
  title: 'PROFESSOR' | 'ASSOCIATE_PROFESSOR' | 'ASSISTANT_PROFESSOR' | 'INSTRUCTOR' | 'ADJUNCT'
  maxWeeklyHours: number
  maxCoursesPerSemester: number
  officeLocation?: string
  phone?: string
  specializations?: TeacherSpecializationRequest[]
}

export interface TeacherSpecializationRequest {
  subjectCode: string
  subjectName?: string
  proficiencyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT'
  yearsExperience: number
  certified?: boolean
  certificationDetails?: string
}

export interface TeacherAvailabilityRequest {
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY'
  startTime: string
  endTime: string
  availabilityType: 'PREFERRED' | 'AVAILABLE' | 'UNAVAILABLE' | 'RESTRICTED'
  maxClasses: number
  breakDuration: number
  requiresBreakBetweenClasses: boolean
  isRecurring: boolean
  notes?: string
}

export interface TeacherFilters {
  search?: string
  departmentId?: number
  title?: string
  page?: number
  size?: number
  sort?: string
  direction?: 'ASC' | 'DESC'
}

export interface PaginatedTeachers {
  content: Teacher[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

class TeacherService {
  async getTeachers(filters: TeacherFilters = {}): Promise<PaginatedTeachers> {
    const params = new URLSearchParams()

    if (filters.search) params.append('search', filters.search)
    if (filters.departmentId) params.append('departmentId', filters.departmentId.toString())
    if (filters.title) params.append('title', filters.title)
    if (filters.page !== undefined) params.append('page', filters.page.toString())
    if (filters.size !== undefined) params.append('size', filters.size.toString())
    if (filters.sort) params.append('sort', filters.sort)
    if (filters.direction) params.append('direction', filters.direction)

    const response = await api.get(`/teachers?${params.toString()}`)
    return response.data.data
  }

  async getTeacherById(id: number): Promise<Teacher> {
    const response = await api.get(`/teachers/${id}`)
    return response.data.data
  }

  async createTeacher(teacher: TeacherRequest): Promise<Teacher> {
    const response = await api.post('/teachers', teacher)
    return response.data.data
  }

  async updateTeacher(id: number, teacher: Partial<TeacherRequest>): Promise<Teacher> {
    const response = await api.put(`/teachers/${id}`, teacher)
    return response.data.data
  }

  async deleteTeacher(id: number): Promise<void> {
    await api.delete(`/teachers/${id}`)
  }

  async getTeacherByEmployeeId(employeeId: string): Promise<Teacher> {
    const response = await api.get(`/teachers/by-employee/${employeeId}`)
    return response.data.data
  }

  async getTeachersByDepartment(departmentId: number): Promise<Teacher[]> {
    const response = await api.get(`/teachers/by-department/${departmentId}`)
    return response.data.data
  }

  async getTeachersBySpecialization(subjectCode: string): Promise<Teacher[]> {
    const response = await api.get(`/teachers/specializations/${subjectCode}`)
    return response.data.data
  }

  // Availability Methods
  async getTeacherAvailability(teacherId: number): Promise<TeacherAvailability[]> {
    const response = await api.get(`/teachers/${teacherId}/availability`)
    return response.data.data
  }

  async getTeacherAvailabilityByDay(teacherId: number, dayOfWeek: string): Promise<TeacherAvailability[]> {
    const response = await api.get(`/teachers/${teacherId}/availability/day/${dayOfWeek}`)
    return response.data.data
  }

  async addTeacherAvailability(teacherId: number, availability: TeacherAvailabilityRequest): Promise<TeacherAvailability> {
    const response = await api.post(`/teachers/${teacherId}/availability`, availability)
    return response.data.data
  }

  async updateTeacherAvailability(teacherId: number, availabilityId: number, availability: TeacherAvailabilityRequest): Promise<TeacherAvailability> {
    const response = await api.put(`/teachers/${teacherId}/availability/${availabilityId}`, availability)
    return response.data.data
  }

  async deleteTeacherAvailability(teacherId: number, availabilityId: number): Promise<void> {
    await api.delete(`/teachers/${teacherId}/availability/${availabilityId}`)
  }

  async findAvailableTeachersForTimeSlot(
    dayOfWeek: string,
    startTime: string,
    endTime: string,
    requiredHours?: number
  ): Promise<Teacher[]> {
    const params = new URLSearchParams()
    params.append('dayOfWeek', dayOfWeek)
    params.append('startTime', startTime)
    params.append('endTime', endTime)
    if (requiredHours !== undefined) {
      params.append('requiredHours', requiredHours.toString())
    }

    const response = await api.get(`/teachers/available-for-timeslot?${params.toString()}`)
    return response.data.data
  }
}

export default new TeacherService()