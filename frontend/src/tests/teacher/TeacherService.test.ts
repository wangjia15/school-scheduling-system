import teacherService, {
  type Teacher,
  type TeacherAvailability,
  type TeacherRequest,
  type TeacherAvailabilityRequest,
  type PaginatedTeachers
} from '@/services/teacherService'
import api from '@/utils/api'

// Mock the api module
jest.mock('@/utils/api')

const mockedApi = api as jest.Mocked<typeof api>

describe('TeacherService', () => {
  const mockTeacher: Teacher = {
    id: 1,
    employeeId: 'EMP001',
    userId: 1,
    departmentId: 1,
    title: 'PROFESSOR',
    maxWeeklyHours: 40,
    maxCoursesPerSemester: 5,
    officeLocation: 'Building A, Room 101',
    phone: '+1 (555) 123-4567',
    user: {
      id: 1,
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@university.edu'
    },
    department: {
      id: 1,
      name: 'Computer Science'
    },
    specializations: [
      {
        id: 1,
        teacherId: 1,
        subjectCode: 'CS101',
        proficiencyLevel: 'EXPERT',
        yearsExperience: 10,
        certified: true,
        certificationDetails: 'PhD in Computer Science',
        subjectName: 'Introduction to Computer Science',
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z'
      }
    ],
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  }

  const mockAvailability: TeacherAvailability = {
    id: 1,
    teacherId: 1,
    dayOfWeek: 'MONDAY',
    startTime: '09:00',
    endTime: '12:00',
    availabilityType: 'PREFERRED',
    maxClasses: 2,
    breakDuration: 0,
    requiresBreakBetweenClasses: false,
    isRecurring: true,
    notes: 'Morning classes',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  }

  const mockPaginatedTeachers: PaginatedTeachers = {
    content: [mockTeacher],
    totalElements: 1,
    totalPages: 1,
    size: 20,
    number: 0,
    first: true,
    last: true
  }

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('getTeachers', () => {
    it('should fetch teachers with default filters', async () => {
      mockedApi.get.mockResolvedValue({
        data: {
          data: mockPaginatedTeachers
        }
      })

      const result = await teacherService.getTeachers()

      expect(mockedApi.get).toHaveBeenCalledWith('/teachers?page=0&size=20&sort=id&direction=ASC')
      expect(result).toEqual(mockPaginatedTeachers)
    })

    it('should fetch teachers with custom filters', async () => {
      const filters = {
        search: 'John',
        departmentId: 1,
        title: 'PROFESSOR',
        page: 1,
        size: 10
      }

      mockedApi.get.mockResolvedValue({
        data: {
          data: mockPaginatedTeachers
        }
      })

      await teacherService.getTeachers(filters)

      expect(mockedApi.get).toHaveBeenCalledWith(
        '/teachers?search=John&departmentId=1&title=PROFESSOR&page=1&size=10&sort=id&direction=ASC'
      )
    })

    it('should handle API errors', async () => {
      mockedApi.get.mockRejectedValue(new Error('API Error'))

      await expect(teacherService.getTeachers()).rejects.toThrow('API Error')
    })
  })

  describe('getTeacherById', () => {
    it('should fetch teacher by ID', async () => {
      mockedApi.get.mockResolvedValue({
        data: {
          data: mockTeacher
        }
      })

      const result = await teacherService.getTeacherById(1)

      expect(mockedApi.get).toHaveBeenCalledWith('/teachers/1')
      expect(result).toEqual(mockTeacher)
    })
  })

  describe('createTeacher', () => {
    it('should create a new teacher', async () => {
      const teacherRequest: TeacherRequest = {
        employeeId: 'EMP002',
        userId: 2,
        departmentId: 1,
        title: 'ASSISTANT_PROFESSOR',
        maxWeeklyHours: 40,
        maxCoursesPerSemester: 4,
        officeLocation: 'Building B, Room 201'
      }

      mockedApi.post.mockResolvedValue({
        data: {
          data: mockTeacher
        }
      })

      const result = await teacherService.createTeacher(teacherRequest)

      expect(mockedApi.post).toHaveBeenCalledWith('/teachers', teacherRequest)
      expect(result).toEqual(mockTeacher)
    })
  })

  describe('updateTeacher', () => {
    it('should update an existing teacher', async () => {
      const updateData = {
        officeLocation: 'Building C, Room 301',
        phone: '+1 (555) 987-6543'
      }

      mockedApi.put.mockResolvedValue({
        data: {
          data: { ...mockTeacher, ...updateData }
        }
      })

      const result = await teacherService.updateTeacher(1, updateData)

      expect(mockedApi.put).toHaveBeenCalledWith('/teachers/1', updateData)
      expect(result.officeLocation).toBe('Building C, Room 301')
    })
  })

  describe('deleteTeacher', () => {
    it('should delete a teacher', async () => {
      mockedApi.delete.mockResolvedValue({})

      await teacherService.deleteTeacher(1)

      expect(mockedApi.delete).toHaveBeenCalledWith('/teachers/1')
    })
  })

  describe('Teacher Availability Methods', () => {
    describe('getTeacherAvailability', () => {
      it('should fetch teacher availability', async () => {
        mockedApi.get.mockResolvedValue({
          data: {
            data: [mockAvailability]
          }
        })

        const result = await teacherService.getTeacherAvailability(1)

        expect(mockedApi.get).toHaveBeenCalledWith('/teachers/1/availability')
        expect(result).toEqual([mockAvailability])
      })
    })

    describe('addTeacherAvailability', () => {
      it('should add teacher availability', async () => {
        const availabilityRequest: TeacherAvailabilityRequest = {
          dayOfWeek: 'TUESDAY',
          startTime: '14:00',
          endTime: '17:00',
          availabilityType: 'AVAILABLE',
          maxClasses: 1,
          breakDuration: 0,
          requiresBreakBetweenClasses: false,
          isRecurring: true
        }

        mockedApi.post.mockResolvedValue({
          data: {
            data: mockAvailability
          }
        })

        const result = await teacherService.addTeacherAvailability(1, availabilityRequest)

        expect(mockedApi.post).toHaveBeenCalledWith('/teachers/1/availability', availabilityRequest)
        expect(result).toEqual(mockAvailability)
      })
    })

    describe('updateTeacherAvailability', () => {
      it('should update teacher availability', async () => {
        const updateRequest: TeacherAvailabilityRequest = {
          dayOfWeek: 'MONDAY',
          startTime: '10:00',
          endTime: '12:00',
          availabilityType: 'PREFERRED',
          maxClasses: 1,
          breakDuration: 0.5,
          requiresBreakBetweenClasses: true,
          isRecurring: true,
          notes: 'Updated morning availability'
        }

        mockedApi.put.mockResolvedValue({
          data: {
            data: { ...mockAvailability, ...updateRequest }
          }
        })

        const result = await teacherService.updateTeacherAvailability(1, 1, updateRequest)

        expect(mockedApi.put).toHaveBeenCalledWith('/teachers/1/availability/1', updateRequest)
        expect(result.maxClasses).toBe(1)
      })
    })

    describe('deleteTeacherAvailability', () => {
      it('should delete teacher availability', async () => {
        mockedApi.delete.mockResolvedValue({})

        await teacherService.deleteTeacherAvailability(1, 1)

        expect(mockedApi.delete).toHaveBeenCalledWith('/teachers/1/availability/1')
      })
    })
  })

  describe('Utility Methods', () => {
    describe('getTeacherByEmployeeId', () => {
      it('should fetch teacher by employee ID', async () => {
        mockedApi.get.mockResolvedValue({
          data: {
            data: mockTeacher
          }
        })

        const result = await teacherService.getTeacherByEmployeeId('EMP001')

        expect(mockedApi.get).toHaveBeenCalledWith('/teachers/by-employee/EMP001')
        expect(result).toEqual(mockTeacher)
      })
    })

    describe('getTeachersByDepartment', () => {
      it('should fetch teachers by department', async () => {
        mockedApi.get.mockResolvedValue({
          data: {
            data: [mockTeacher]
          }
        })

        const result = await teacherService.getTeachersByDepartment(1)

        expect(mockedApi.get).toHaveBeenCalledWith('/teachers/by-department/1')
        expect(result).toEqual([mockTeacher])
      })
    })

    describe('getTeachersBySpecialization', () => {
      it('should fetch teachers by specialization', async () => {
        mockedApi.get.mockResolvedValue({
          data: {
            data: [mockTeacher]
          }
        })

        const result = await teacherService.getTeachersBySpecialization('CS101')

        expect(mockedApi.get).toHaveBeenCalledWith('/teachers/specializations/CS101')
        expect(result).toEqual([mockTeacher])
      })
    })

    describe('findAvailableTeachersForTimeSlot', () => {
      it('should find available teachers for time slot', async () => {
        mockedApi.get.mockResolvedValue({
          data: {
            data: [mockTeacher]
          }
        })

        const result = await teacherService.findAvailableTeachersForTimeSlot(
          'MONDAY',
          '09:00',
          '12:00',
          3
        )

        expect(mockedApi.get).toHaveBeenCalledWith(
          '/teachers/available-for-timeslot?dayOfWeek=MONDAY&startTime=09:00&endTime=12:00&requiredHours=3'
        )
        expect(result).toEqual([mockTeacher])
      })

      it('should find available teachers without required hours', async () => {
        mockedApi.get.mockResolvedValue({
          data: {
            data: [mockTeacher]
          }
        })

        await teacherService.findAvailableTeachersForTimeSlot('MONDAY', '09:00', '12:00')

        expect(mockedApi.get).toHaveBeenCalledWith(
          '/teachers/available-for-timeslot?dayOfWeek=MONDAY&startTime=09:00&endTime=12:00'
        )
      })
    })
  })
})