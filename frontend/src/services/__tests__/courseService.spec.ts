import { describe, it, expect, beforeEach, vi } from 'vitest'
import axios from 'axios'
import { courseService } from '../courseService'

// Mock axios
vi.mock('axios')
const mockedAxios = axios as any

// Mock auth store
const mockAuthStore = {
  token: 'mock-token',
  logout: vi.fn()
}

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => mockAuthStore
}))

describe('CourseService', () => {
  let originalEnv: any

  beforeEach(() => {
    originalEnv = process.env
    process.env = { ...originalEnv }

    // Reset axios mock
    mockedAxios.create.mockReturnValue({
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() }
      }
    })

    vi.clearAllMocks()
  })

  afterEach(() => {
    process.env = originalEnv
  })

  describe('getCourses', () => {
    it('fetches courses with default parameters', async () => {
      const mockResponse = {
        data: {
          data: {
            courses: [
              { id: 1, courseCode: 'CS101', title: 'Intro to CS' }
            ],
            total: 1,
            page: 0,
            size: 20,
            totalPages: 1
          }
        }
      }

      mockedAxios.create().get.mockResolvedValue(mockResponse)

      const result = await courseService.getCourses()

      expect(mockedAxios.create().get).toHaveBeenCalledWith(
        'courses?page=0&size=20&sort=id&direction=ASC'
      )
      expect(result).toEqual(mockResponse.data.data)
    })

    it('fetches courses with filters', async () => {
      const mockResponse = {
        data: {
          data: {
            courses: [
              { id: 1, courseCode: 'CS101', title: 'Intro to CS' }
            ],
            total: 1,
            page: 0,
            size: 20,
            totalPages: 1
          }
        }
      }

      mockedAxios.create().get.mockResolvedValue(mockResponse)

      const filters = {
        search: 'CS',
        departmentId: 1,
        level: 'UNDERGRADUATE',
        active: true
      }

      const result = await courseService.getCourses(0, 20, 'id', 'ASC', filters)

      expect(mockedAxios.create().get).toHaveBeenCalledWith(
        'courses?page=0&size=20&sort=id&direction=ASC&search=CS&departmentId=1&level=UNDERGRADUATE&active=true'
      )
      expect(result).toEqual(mockResponse.data.data)
    })

    it('handles API errors gracefully', async () => {
      mockedAxios.create().get.mockRejectedValue(new Error('Network error'))

      await expect(courseService.getCourses()).rejects.toThrow('Failed to fetch courses')
    })
  })

  describe('getCourseById', () => {
    it('fetches course by ID', async () => {
      const mockResponse = {
        data: {
          data: { id: 1, courseCode: 'CS101', title: 'Intro to CS' }
        }
      }

      mockedAxios.create().get.mockResolvedValue(mockResponse)

      const result = await courseService.getCourseById(1)

      expect(mockedAxios.create().get).toHaveBeenCalledWith('courses/1')
      expect(result).toEqual(mockResponse.data.data)
    })

    it('handles not found errors', async () => {
      mockedAxios.create().get.mockRejectedValue(new Error('Course not found'))

      await expect(courseService.getCourseById(999)).rejects.toThrow('Failed to fetch course')
    })
  })

  describe('getCourseByCode', () => {
    it('fetches course by course code', async () => {
      const mockResponse = {
        data: {
          data: { id: 1, courseCode: 'CS101', title: 'Intro to CS' }
        }
      }

      mockedAxios.create().get.mockResolvedValue(mockResponse)

      const result = await courseService.getCourseByCode('CS101')

      expect(mockedAxios.create().get).toHaveBeenCalledWith('courses/code/CS101')
      expect(result).toEqual(mockResponse.data.data)
    })
  })

  describe('createCourse', () => {
    it('creates a new course', async () => {
      const mockCourse = {
        courseCode: 'CS201',
        title: 'Data Structures',
        departmentId: 1,
        credits: 3,
        contactHoursPerWeek: 3,
        theoryHours: 3,
        labHours: 0,
        level: 'UNDERGRADUATE',
        maxStudents: 30,
        minStudents: 5,
        requiresLab: false,
        prerequisites: []
      }

      const mockResponse = {
        data: {
          data: { id: 2, ...mockCourse }
        }
      }

      mockedAxios.create().post.mockResolvedValue(mockResponse)

      const result = await courseService.createCourse(mockCourse)

      expect(mockedAxios.create().post).toHaveBeenCalledWith('courses', mockCourse)
      expect(result).toEqual(mockResponse.data.data)
    })

    it('handles validation errors', async () => {
      const mockCourse = {
        courseCode: '', // Invalid empty course code
        title: 'Test Course',
        departmentId: 1,
        credits: 3,
        contactHoursPerWeek: 3,
        theoryHours: 3,
        labHours: 0,
        level: 'UNDERGRADUATE',
        maxStudents: 30,
        minStudents: 5,
        requiresLab: false,
        prerequisites: []
      }

      mockedAxios.create().post.mockRejectedValue(new Error('Validation failed'))

      await expect(courseService.createCourse(mockCourse)).rejects.toThrow('Failed to create course')
    })
  })

  describe('updateCourse', () => {
    it('updates an existing course', async () => {
      const courseUpdate = {
        title: 'Updated Course Title',
        maxStudents: 35
      }

      const mockResponse = {
        data: {
          data: { id: 1, courseCode: 'CS101', ...courseUpdate }
        }
      }

      mockedAxios.create().put.mockResolvedValue(mockResponse)

      const result = await courseService.updateCourse(1, courseUpdate)

      expect(mockedAxios.create().put).toHaveBeenCalledWith('courses/1', courseUpdate)
      expect(result).toEqual(mockResponse.data.data)
    })
  })

  describe('deleteCourse', () => {
    it('deletes a course', async () => {
      mockedAxios.create().delete.mockResolvedValue({})

      await courseService.deleteCourse(1)

      expect(mockedAxios.create().delete).toHaveBeenCalledWith('courses/1')
    })

    it('handles deletion errors', async () => {
      mockedAxios.create().delete.mockRejectedValue(new Error('Cannot delete course with active enrollments'))

      await expect(courseService.deleteCourse(1)).rejects.toThrow('Failed to delete course')
    })
  })

  describe('getCoursesByDepartment', () => {
    it('fetches courses by department ID', async () => {
      const mockResponse = {
        data: {
          data: [
            { id: 1, courseCode: 'CS101', title: 'Intro to CS' },
            { id: 2, courseCode: 'CS201', title: 'Data Structures' }
          ]
        }
      }

      mockedAxios.create().get.mockResolvedValue(mockResponse)

      const result = await courseService.getCoursesByDepartment(1)

      expect(mockedAxios.create().get).toHaveBeenCalledWith('courses/by-department/1')
      expect(result).toEqual(mockResponse.data.data)
    })
  })

  describe('getCoursesByLevel', () => {
    it('fetches courses by level', async () => {
      const mockResponse = {
        data: {
          data: [
            { id: 1, courseCode: 'CS101', title: 'Intro to CS', level: 'UNDERGRADUATE' }
          ]
        }
      }

      mockedAxios.create().get.mockResolvedValue(mockResponse)

      const result = await courseService.getCoursesByLevel('UNDERGRADUATE')

      expect(mockedAxios.create().get).toHaveBeenCalledWith('courses/by-level/UNDERGRADUATE')
      expect(result).toEqual(mockResponse.data.data)
    })
  })

  describe('getCoursePrerequisites', () => {
    it('fetches course prerequisites', async () => {
      const mockResponse = {
        data: {
          data: [
            { id: 1, courseCode: 'CS101', title: 'Intro to CS' }
          ]
        }
      }

      mockedAxios.create().get.mockResolvedValue(mockResponse)

      const result = await courseService.getCoursePrerequisites(2)

      expect(mockedAxios.create().get).toHaveBeenCalledWith('courses/prerequisites/2')
      expect(result).toEqual(mockResponse.data.data)
    })
  })

  describe('getLabCourses', () => {
    it('fetches lab courses', async () => {
      const mockResponse = {
        data: {
          data: [
            { id: 1, courseCode: 'CS101L', title: 'CS Lab', requiresLab: true }
          ]
        }
      }

      mockedAxios.create().get.mockResolvedValue(mockResponse)

      const result = await courseService.getLabCourses()

      expect(mockedAxios.create().get).toHaveBeenCalledWith('courses/lab-courses')
      expect(result).toEqual(mockResponse.data.data)
    })
  })

  describe('validatePrerequisites', () => {
    it('validates course prerequisites', async () => {
      const mockResponse = {
        data: {
          data: {
            isValid: true,
            issues: [],
            circularDependencies: []
          }
        }
      }

      mockedAxios.create().get.mockResolvedValue(mockResponse)

      const result = await courseService.validatePrerequisites(1)

      expect(mockedAxios.create().get).toHaveBeenCalledWith('courses/1/validate-prerequisites')
      expect(result).toEqual(mockResponse.data.data)
    })
  })

  describe('axios configuration', () => {
    it('configures axios with correct base URL', () => {
      process.env.VITE_API_BASE_URL = 'https://api.example.com'

      courseService = new (courseService as any).constructor()

      expect(mockedAxios.create).toHaveBeenCalledWith(
        expect.objectContaining({
          baseURL: 'https://api.example.com/api/v1',
          timeout: 10000,
          headers: {
            'Content-Type': 'application/json'
          }
        })
      )
    })

    it('uses default base URL when env variable not set', () => {
      delete process.env.VITE_API_BASE_URL

      courseService = new (courseService as any).constructor()

      expect(mockedAxios.create).toHaveBeenCalledWith(
        expect.objectContaining({
          baseURL: 'http://localhost:8080/api/v1'
        })
      )
    })

    it('adds authorization header to requests', async () => {
      const mockResponse = { data: { data: [] } }
      mockedAxios.create().get.mockResolvedValue(mockResponse)

      await courseService.getCourses()

      // Check that the interceptor was set up
      expect(mockedAxios.create().interceptors.request.use).toHaveBeenCalled()
    })
  })
})