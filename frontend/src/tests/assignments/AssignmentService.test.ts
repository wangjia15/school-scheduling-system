import assignmentService, {
  type Course,
  type Teacher,
  type AssignmentConflict,
  type AssignmentFilters,
  type PaginatedCourses,
  type AssignmentRequest,
  type BulkAssignmentRequest,
  type ConflictResolutionRequest
} from '@/services/assignmentService'
import api from '@/utils/api'

// Mock the api module
jest.mock('@/utils/api')

const mockedApi = api as jest.Mocked<typeof api>

describe('AssignmentService', () => {
  const mockTeacher: Teacher = {
    id: 1,
    employeeId: 'EMP001',
    userId: 1,
    departmentId: 1,
    title: 'PROFESSOR',
    maxWeeklyHours: 40,
    maxCoursesPerSemester: 5,
    user: {
      id: 1,
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@university.edu',
      isActive: true
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
    currentWorkload: 20,
    availableHours: 20,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  }

  const mockCourse: Course = {
    id: 1,
    courseCode: 'CS101',
    courseName: 'Introduction to Computer Science',
    department: {
      id: 1,
      name: 'Computer Science'
    },
    credits: 3,
    weeklyHours: 4,
    maxStudents: 30,
    semester: 'FALL',
    year: 2024,
    requiredSpecializations: ['CS101'],
    assignedTeacher: mockTeacher,
    schedule: {
      dayOfWeek: 'Monday',
      startTime: '09:00',
      endTime: '11:00',
      classroom: {
        id: 1,
        name: 'Room 101',
        building: 'Computer Science Building',
        capacity: 30
      }
    },
    hasConflicts: false,
    conflictCount: 0,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  }

  const mockConflict: AssignmentConflict = {
    id: 1,
    courseId: 1,
    teacherId: 1,
    conflictType: 'SCHEDULE_OVERLAP',
    severity: 'HIGH',
    description: 'Teacher has overlapping schedule with another course',
    conflictingCourses: [
      {
        id: 2,
        courseCode: 'CS201',
        courseName: 'Data Structures',
        schedule: {
          dayOfWeek: 'Monday',
          startTime: '10:00',
          endTime: '12:00'
        }
      }
    ],
    suggestedResolution: 'Reschedule one of the conflicting courses to a different time slot',
    isResolved: false,
    createdAt: '2024-01-01T00:00:00Z'
  }

  const mockPaginatedCourses: PaginatedCourses = {
    content: [mockCourse],
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

  describe('getCourses', () => {
    it('should fetch courses with default filters', async () => {
      mockedApi.get.mockResolvedValue({
        data: {
          data: mockPaginatedCourses
        }
      })

      const result = await assignmentService.getCourses()

      expect(mockedApi.get).toHaveBeenCalledWith('/courses/assignments?page=0&size=20')
      expect(result).toEqual(mockPaginatedCourses)
    })

    it('should fetch courses with custom filters', async () => {
      const filters: AssignmentFilters = {
        search: 'Computer Science',
        departmentId: 1,
        status: 'assigned',
        page: 1,
        size: 10,
        sort: 'courseName',
        direction: 'ASC'
      }

      mockedApi.get.mockResolvedValue({
        data: {
          data: mockPaginatedCourses
        }
      })

      await assignmentService.getCourses(filters)

      expect(mockedApi.get).toHaveBeenCalledWith(
        '/courses/assignments?search=Computer%20Science&departmentId=1&status=assigned&page=1&size=10&sort=courseName&direction=ASC'
      )
    })

    it('should handle API errors', async () => {
      mockedApi.get.mockRejectedValue(new Error('API Error'))

      await expect(assignmentService.getCourses()).rejects.toThrow('API Error')
    })
  })

  describe('getCourseById', () => {
    it('should fetch course by ID', async () => {
      mockedApi.get.mockResolvedValue({
        data: {
          data: mockCourse
        }
      })

      const result = await assignmentService.getCourseById(1)

      expect(mockedApi.get).toHaveBeenCalledWith('/courses/1')
      expect(result).toEqual(mockCourse)
    })
  })

  describe('getAvailableTeachers', () => {
    it('should fetch available teachers', async () => {
      mockedApi.get.mockResolvedValue({
        data: {
          data: [mockTeacher]
        }
      })

      const result = await assignmentService.getAvailableTeachers()

      expect(mockedApi.get).toHaveBeenCalledWith('/teachers/available-for-assignment')
      expect(result).toEqual([mockTeacher])
    })
  })

  describe('getTeachersForCourse', () => {
    it('should fetch teachers qualified for a specific course', async () => {
      mockedApi.get.mockResolvedValue({
        data: {
          data: [mockTeacher]
        }
      })

      const result = await assignmentService.getTeachersForCourse(1)

      expect(mockedApi.get).toHaveBeenCalledWith('/courses/1/available-teachers')
      expect(result).toEqual([mockTeacher])
    })
  })

  describe('assignTeacherToCourse', () => {
    it('should assign teacher to course', async () => {
      const request: AssignmentRequest = {
        teacherId: 1,
        schedule: {
          dayOfWeek: 'Monday',
          startTime: '09:00',
          endTime: '11:00',
          classroomId: 1
        }
      }

      mockedApi.post.mockResolvedValue({
        data: {
          data: { ...mockCourse, assignedTeacher: mockTeacher }
        }
      })

      const result = await assignmentService.assignTeacherToCourse(1, request)

      expect(mockedApi.post).toHaveBeenCalledWith('/courses/1/assign-teacher', request)
      expect(result.assignedTeacher).toEqual(mockTeacher)
    })

    it('should assign teacher without schedule', async () => {
      const request: AssignmentRequest = {
        teacherId: 1
      }

      mockedApi.post.mockResolvedValue({
        data: {
          data: { ...mockCourse, assignedTeacher: mockTeacher }
        }
      })

      const result = await assignmentService.assignTeacherToCourse(1, request)

      expect(mockedApi.post).toHaveBeenCalledWith('/courses/1/assign-teacher', request)
      expect(result.assignedTeacher).toEqual(mockTeacher)
    })
  })

  describe('unassignTeacher', () => {
    it('should unassign teacher from course', async () => {
      mockedApi.delete.mockResolvedValue({})

      await assignmentService.unassignTeacher(1)

      expect(mockedApi.delete).toHaveBeenCalledWith('/courses/1/assign-teacher')
    })
  })

  describe('bulkAssignTeachers', () => {
    it('should bulk assign teachers to courses', async () => {
      const request: BulkAssignmentRequest = {
        assignments: [
          {
            courseId: 1,
            teacherId: 1,
            schedule: {
              dayOfWeek: 'Monday',
              startTime: '09:00',
              endTime: '11:00',
              classroomId: 1
            }
          }
        ]
      }

      const expectedResult = {
        success: 1,
        failed: 0,
        errors: []
      }

      mockedApi.post.mockResolvedValue({
        data: {
          data: expectedResult
        }
      })

      const result = await assignmentService.bulkAssignTeachers(request)

      expect(mockedApi.post).toHaveBeenCalledWith('/courses/bulk-assign', request)
      expect(result).toEqual(expectedResult)
    })

    it('should handle bulk assignment failures', async () => {
      const request: BulkAssignmentRequest = {
        assignments: [
          {
            courseId: 1,
            teacherId: 1
          },
          {
            courseId: 2,
            teacherId: 2
          }
        ]
      }

      const expectedResult = {
        success: 1,
        failed: 1,
        errors: ['Teacher 2 is not qualified for course 2']
      }

      mockedApi.post.mockResolvedValue({
        data: {
          data: expectedResult
        }
      })

      const result = await assignmentService.bulkAssignTeachers(request)

      expect(result).toEqual(expectedResult)
      expect(result.failed).toBe(1)
      expect(result.errors).toHaveLength(1)
    })
  })

  describe('Conflict Management', () => {
    describe('getCourseConflicts', () => {
      it('should fetch conflicts for a specific course', async () => {
        mockedApi.get.mockResolvedValue({
          data: {
            data: [mockConflict]
          }
        })

        const result = await assignmentService.getCourseConflicts(1)

        expect(mockedApi.get).toHaveBeenCalledWith('/courses/1/conflicts')
        expect(result).toEqual([mockConflict])
      })
    })

    describe('getAllConflicts', () => {
      it('should fetch all conflicts', async () => {
        mockedApi.get.mockResolvedValue({
          data: {
            data: [mockConflict]
          }
        })

        const result = await assignmentService.getAllConflicts()

        expect(mockedApi.get).toHaveBeenCalledWith('/assignments/conflicts')
        expect(result).toEqual([mockConflict])
      })

      it('should fetch conflicts with filters', async () => {
        const filters = {
          severity: 'HIGH',
          isResolved: false
        }

        mockedApi.get.mockResolvedValue({
          data: {
            data: [mockConflict]
          }
        })

        await assignmentService.getAllConflicts(filters)

        expect(mockedApi.get).toHaveBeenCalledWith('/assignments/conflicts?severity=HIGH&isResolved=false')
      })
    })

    describe('resolveConflict', () => {
      it('should resolve a conflict', async () => {
        const request: ConflictResolutionRequest = {
          conflictId: 1,
          resolution: 'CHANGE_SCHEDULE',
          resolutionDetails: 'Rescheduled to Tuesday 14:00-16:00'
        }

        const resolvedConflict = {
          ...mockConflict,
          isResolved: true,
          resolutionDetails: 'Rescheduled to Tuesday 14:00-16:00'
        }

        mockedApi.put.mockResolvedValue({
          data: {
            data: resolvedConflict
          }
        })

        const result = await assignmentService.resolveConflict(1, request)

        expect(mockedApi.put).toHaveBeenCalledWith('/assignments/conflicts/1/resolve', request)
        expect(result.isResolved).toBe(true)
      })
    })

    describe('autoResolveConflicts', () => {
      it('should auto-resolve conflicts for all courses', async () => {
        const result = {
          resolved: 2,
          remaining: 1
        }

        mockedApi.post.mockResolvedValue({
          data: {
            data: result
          }
        })

        const response = await assignmentService.autoResolveConflicts()

        expect(mockedApi.post).toHaveBeenCalledWith('/assignments/conflicts/auto-resolve')
        expect(response).toEqual(result)
      })

      it('should auto-resolve conflicts for specific course', async () => {
        const result = {
          resolved: 1,
          remaining: 0
        }

        mockedApi.post.mockResolvedValue({
          data: {
            data: result
          }
        })

        const response = await assignmentService.autoResolveConflicts(1)

        expect(mockedApi.post).toHaveBeenCalledWith('/assignments/conflicts/auto-resolve?courseId=1')
        expect(response).toEqual(result)
      })
    })
  })

  describe('getAssignmentStats', () => {
    it('should fetch assignment statistics', async () => {
      const stats = {
        totalAssignments: 50,
        activeAssignments: 45,
        unassignedCourses: 5,
        conflicts: 3,
        averageTeacherLoad: 32,
        departmentStats: [
          {
            departmentId: 1,
            departmentName: 'Computer Science',
            totalCourses: 20,
            assignedCourses: 18,
            averageWorkload: 35
          }
        ]
      }

      mockedApi.get.mockResolvedValue({
        data: {
          data: stats
        }
      })

      const result = await assignmentService.getAssignmentStats()

      expect(mockedApi.get).toHaveBeenCalledWith('/assignments/stats')
      expect(result).toEqual(stats)
    })
  })

  describe('validateAssignment', () => {
    it('should validate assignment without schedule', async () => {
      const validationResult = {
        isValid: true,
        conflicts: [],
        warnings: ['Teacher workload will be 80% of capacity'],
        suggestions: ['Consider adding a backup teacher']
      }

      mockedApi.post.mockResolvedValue({
        data: {
          data: validationResult
        }
      })

      const result = await assignmentService.validateAssignment(1, 1)

      expect(mockedApi.post).toHaveBeenCalledWith('/courses/1/validate-assignment', {
        teacherId: 1
      })
      expect(result).toEqual(validationResult)
    })

    it('should validate assignment with schedule', async () => {
      const schedule = {
        dayOfWeek: 'Monday',
        startTime: '09:00',
        endTime: '11:00',
        classroomId: 1
      }

      const validationResult = {
        isValid: false,
        conflicts: [mockConflict],
        warnings: [],
        suggestions: []
      }

      mockedApi.post.mockResolvedValue({
        data: {
          data: validationResult
        }
      })

      const result = await assignmentService.validateAssignment(1, 1, schedule)

      expect(mockedApi.post).toHaveBeenCalledWith('/courses/1/validate-assignment', {
        teacherId: 1,
        schedule
      })
      expect(result.isValid).toBe(false)
    })
  })

  describe('getTeacherScheduleConflicts', () => {
    it('should fetch schedule conflicts for teacher', async () => {
      mockedApi.get.mockResolvedValue({
        data: {
          data: [mockConflict]
        }
      })

      const result = await assignmentService.getTeacherScheduleConflicts(1)

      expect(mockedApi.get).toHaveBeenCalledWith('/teachers/1/schedule-conflicts')
      expect(result).toEqual([mockConflict])
    })
  })

  describe('getOptimalAssignments', () => {
    it('should get optimal assignment suggestions', async () => {
      const suggestions = [
        {
          courseId: 1,
          recommendedTeacherId: 1,
          confidence: 0.95,
          reasoning: 'Teacher has relevant specialization and available workload capacity'
        }
      ]

      mockedApi.post.mockResolvedValue({
        data: {
          data: suggestions
        }
      })

      const result = await assignmentService.getOptimalAssignments([1, 2, 3])

      expect(mockedApi.post).toHaveBeenCalledWith('/assignments/optimal-suggestions', {
        courseIds: [1, 2, 3]
      })
      expect(result).toEqual(suggestions)
    })
  })
})