import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import TeacherAssignmentView from '@/views/assignments/TeacherAssignmentView.vue'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Select from '@/components/ui/Select.vue'

// Mock the assignment service
const mockAssignmentService = {
  getCourses: vi.fn(),
  getAvailableTeachers: vi.fn(),
  getTeachersForCourse: vi.fn(),
  assignTeacherToCourse: vi.fn(),
  unassignTeacher: vi.fn(),
  bulkAssignTeachers: vi.fn(),
  getAssignmentStats: vi.fn(),
  validateAssignment: vi.fn(),
  getOptimalAssignments: vi.fn()
}

// Mock the teacher service
const mockTeacherService = {
  getTeachers: vi.fn()
}

vi.mock('@/services/assignmentService', () => ({
  default: mockAssignmentService
}))

vi.mock('@/services/teacherService', () => ({
  default: mockTeacherService
}))

describe('TeacherAssignmentView', () => {
  const mockCourses = {
    content: [
      {
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
        assignedTeacher: {
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
          specializations: [],
          currentWorkload: 20,
          availableHours: 20,
          createdAt: '2024-01-01T00:00:00Z',
          updatedAt: '2024-01-01T00:00:00Z'
        },
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
      },
      {
        id: 2,
        courseCode: 'CS201',
        courseName: 'Data Structures',
        department: {
          id: 1,
          name: 'Computer Science'
        },
        credits: 4,
        weeklyHours: 4,
        maxStudents: 25,
        semester: 'FALL',
        year: 2024,
        requiredSpecializations: ['CS201'],
        assignedTeacher: null,
        schedule: null,
        hasConflicts: false,
        conflictCount: 0,
        createdAt: '2024-01-01T00:00:00Z',
        updatedAt: '2024-01-01T00:00:00Z'
      }
    ],
    totalElements: 2,
    totalPages: 1,
    size: 20,
    number: 0,
    first: true,
    last: true
  }

  const mockTeachers = [
    {
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
          subjectName: 'Introduction to Computer Science'
        }
      ],
      currentWorkload: 20,
      availableHours: 20,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 2,
      employeeId: 'EMP002',
      userId: 2,
      departmentId: 1,
      title: 'ASSISTANT_PROFESSOR',
      maxWeeklyHours: 40,
      maxCoursesPerSemester: 4,
      user: {
        id: 2,
        firstName: 'Jane',
        lastName: 'Smith',
        email: 'jane.smith@university.edu',
        isActive: true
      },
      department: {
        id: 1,
        name: 'Computer Science'
      },
      specializations: [
        {
          id: 2,
          teacherId: 2,
          subjectCode: 'CS201',
          proficiencyLevel: 'ADVANCED',
          yearsExperience: 5,
          certified: false,
          subjectName: 'Data Structures'
        }
      ],
      currentWorkload: 16,
      availableHours: 24,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    }
  ]

  const mockStats = {
    totalAssignments: 2,
    activeAssignments: 1,
    unassignedCourses: 1,
    conflicts: 0,
    averageTeacherLoad: 18,
    departmentStats: [
      {
        departmentId: 1,
        departmentName: 'Computer Science',
        totalCourses: 2,
        assignedCourses: 1,
        averageWorkload: 18
      }
    ]
  }

  const createWrapper = (props = {}) => {
    return mount(TeacherAssignmentView, {
      props: {
        ...props
      },
      global: {
        components: {
          Card,
          Button,
          Badge,
          Select
        },
        stubs: {
          'teacher-assignment-modal': true,
          'bulk-assignment-modal': true,
          'conflict-resolution-modal': true
        }
      }
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()

    // Setup default mock responses
    mockAssignmentService.getCourses.mockResolvedValue(mockCourses)
    mockAssignmentService.getAvailableTeachers.mockResolvedValue(mockTeachers)
    mockAssignmentService.getAssignmentStats.mockResolvedValue(mockStats)
    mockTeacherService.getTeachers.mockResolvedValue({ content: mockTeachers })
  })

  describe('Initial Loading', () => {
    it('renders correctly with loading state', () => {
      const wrapper = createWrapper()

      expect(wrapper.find('h1').text()).toBe('Teacher Assignment')
      expect(wrapper.text()).toContain('Assign teachers to courses with intelligent matching and conflict detection')
    })

    it('shows loading skeletons while loading data', () => {
      // Mock slow loading
      mockAssignmentService.getCourses.mockReturnValue(new Promise(() => {}))
      mockAssignmentService.getAssignmentStats.mockReturnValue(new Promise(() => {}))

      const wrapper = createWrapper()

      expect(wrapper.findAll('.animate-pulse').length).toBeGreaterThan(0)
    })
  })

  describe('Data Loading', () => {
    it('loads courses on mount', async () => {
      const wrapper = createWrapper()

      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 0))

      expect(mockAssignmentService.getCourses).toHaveBeenCalled()
    })

    it('loads assignment statistics on mount', async () => {
      const wrapper = createWrapper()

      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 0))

      expect(mockAssignmentService.getAssignmentStats).toHaveBeenCalled()
    })

    it('loads available teachers on mount', async () => {
      const wrapper = createWrapper()

      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 0))

      expect(mockAssignmentService.getAvailableTeachers).toHaveBeenCalled()
    })

    it('shows summary statistics after loading', async () => {
      const wrapper = createWrapper()

      // Simulate data loading completion
      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('2 Courses')
      expect(wrapper.text()).toContain('1 Assigned')
      expect(wrapper.text()).toContain('1 Unassigned')
      expect(wrapper.text()).toContain('0 Conflicts')
    })

    it('handles loading errors gracefully', async () => {
      mockAssignmentService.getCourses.mockRejectedValue(new Error('API Error'))
      mockAssignmentService.getAssignmentStats.mockRejectedValue(new Error('API Error'))

      const wrapper = createWrapper()

      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 0))

      expect(wrapper.vm.loading).toBe(false)
      expect(wrapper.vm.error).toBe('Failed to load assignment data')
      expect(wrapper.text()).toContain('Failed to load assignment data')
    })
  })

  describe('Course Table', () => {
    it('displays courses in table format', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      expect(wrapper.text()).toContain('CS101')
      expect(wrapper.text()).toContain('Introduction to Computer Science')
      expect(wrapper.text()).toContain('CS201')
      expect(wrapper.text()).toContain('Data Structures')
    })

    it('shows assignment status for each course', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      expect(wrapper.text()).toContain('John Doe') // Assigned teacher for CS101
      expect(wrapper.text()).toContain('Not Assigned') // CS201
    })

    it('shows schedule information when available', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      expect(wrapper.text()).toContain('Monday')
      expect(wrapper.text()).toContain('09:00 - 11:00')
      expect(wrapper.text()).toContain('Room 101')
    })

    it('shows conflict indicators', async () => {
      const coursesWithConflicts = [
        {
          ...mockCourses.content[0],
          hasConflicts: true,
          conflictCount: 1
        },
        mockCourses.content[1]
      ]

      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: coursesWithConflicts,
        stats: { ...mockStats, conflicts: 1 },
        availableTeachers: mockTeachers
      })

      expect(wrapper.text()).toContain('1 Conflict')
    })
  })

  describe('Course Filtering', () => {
    it('filters courses by assignment status', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Filter to show only assigned courses
      await wrapper.vm.filterCourses('assigned')

      expect(wrapper.vm.filteredCourses).toHaveLength(1)
      expect(wrapper.vm.filteredCourses[0].assignedTeacher).toBeDefined()
    })

    it('filters courses by department', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Filter by Computer Science department
      await wrapper.vm.filterCourses('all', 1)

      expect(wrapper.vm.filteredCourses).toHaveLength(2)
    })

    it('filters courses by search query', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Search for "Data Structures"
      await wrapper.vm.searchCourses('Data Structures')

      expect(wrapper.vm.filteredCourses).toHaveLength(1)
      expect(wrapper.vm.filteredCourses[0].courseName).toBe('Data Structures')
    })

    it('shows no results when no courses match filters', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Search for non-existent course
      await wrapper.vm.searchCourses('Nonexistent Course')

      expect(wrapper.vm.filteredCourses).toHaveLength(0)
      expect(wrapper.text()).toContain('No courses found')
    })
  })

  describe('Assignment Actions', () => {
    it('opens assignment modal when assign button is clicked', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Find assign button for unassigned course
      const assignButton = wrapper.findAll('button').find(btn =>
        btn.text().includes('Assign Teacher')
      )
      await assignButton.trigger('click')

      expect(wrapper.vm.selectedCourse).toBeDefined()
      expect(wrapper.vm.showAssignmentModal).toBe(true)
    })

    it('opens assignment modal when course row is clicked', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Click on a course row
      const courseRow = wrapper.find('tr')
      await courseRow.trigger('click')

      expect(wrapper.vm.selectedCourse).toBeDefined()
      expect(wrapper.vm.showAssignmentModal).toBe(true)
    })

    it('shows bulk assignment button when courses are selected', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers,
        selectedCourses: new Set([1, 2])
      })

      expect(wrapper.vm.showBulkActions).toBe(true)
      expect(wrapper.text()).toContain('Bulk Assign (2)')
    })

    it('opens bulk assignment modal when bulk assign button is clicked', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers,
        selectedCourses: new Set([1, 2])
      })

      const bulkButton = wrapper.findAll('button').find(btn =>
        btn.text().includes('Bulk Assign')
      )
      await bulkButton.trigger('click')

      expect(wrapper.vm.showBulkModal).toBe(true)
    })

    it('opens conflict resolution modal when conflicts exist', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: { ...mockStats, conflicts: 1 },
        availableTeachers: mockTeachers
      })

      const conflictButton = wrapper.findAll('button').find(btn =>
        btn.text().includes('View Conflicts')
      )
      await conflictButton.trigger('click')

      expect(wrapper.vm.showConflictModal).toBe(true)
    })
  })

  describe('Course Selection', () => {
    it('allows selecting individual courses', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Select first course
      await wrapper.vm.toggleCourseSelection(1)

      expect(wrapper.vm.selectedCourses.has(1)).toBe(true)
      expect(wrapper.vm.selectedCourses.size).toBe(1)
    })

    it('allows selecting all courses', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Select all courses
      await wrapper.vm.selectAllCourses()

      expect(wrapper.vm.selectedCourses.size).toBe(2)
      expect(wrapper.vm.allSelected).toBe(true)
    })

    it('allows deselecting all courses', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers,
        selectedCourses: new Set([1, 2])
      })

      // Deselect all courses
      await wrapper.vm.selectAllCourses()

      expect(wrapper.vm.selectedCourses.size).toBe(0)
      expect(wrapper.vm.allSelected).toBe(false)
    })

    it('updates allSelected state when courses are manually selected', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Select both courses manually
      await wrapper.vm.toggleCourseSelection(1)
      await wrapper.vm.toggleCourseSelection(2)

      expect(wrapper.vm.allSelected).toBe(true)

      // Deselect one course
      await wrapper.vm.toggleCourseSelection(1)

      expect(wrapper.vm.allSelected).toBe(false)
    })
  })

  describe('Statistics Summary', () => {
    it('displays comprehensive assignment statistics', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      expect(wrapper.text()).toContain('Total Courses: 2')
      expect(wrapper.text()).toContain('Assigned: 1')
      expect(wrapper.text()).toContain('Unassigned: 1')
      expect(wrapper.text()).toContain('Conflicts: 0')
      expect(wrapper.text()).toContain('Available Teachers: 2')
      expect(wrapper.text()).toContain('Avg. Workload: 18h')
    })

    it('shows color-coded statistics', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Check for proper color coding
      const statsCards = wrapper.findAll('.border')
      const assignedCard = statsCards.find(card => card.text().includes('Assigned: 1'))
      const unassignedCard = statsCards.find(card => card.text().includes('Unassigned: 1'))

      expect(assignedCard?.classes()).toContain('border-green-200')
      expect(unassignedCard?.classes()).toContain('border-yellow-200')
    })
  })

  describe('Modal Event Handling', () => {
    it('handles assignment modal events', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Simulate assignment modal save
      await wrapper.vm.handleAssignmentSaved()

      expect(wrapper.vm.showAssignmentModal).toBe(false)
      expect(mockAssignmentService.getCourses).toHaveBeenCalled()
    })

    it('handles bulk assignment modal events', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Simulate bulk modal save
      await wrapper.vm.handleBulkAssignmentSaved()

      expect(wrapper.vm.showBulkModal).toBe(false)
      expect(wrapper.vm.selectedCourses.size).toBe(0)
      expect(mockAssignmentService.getCourses).toHaveBeenCalled()
    })

    it('handles conflict resolution modal events', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      // Simulate conflict modal save
      await wrapper.vm.handleConflictResolved()

      expect(wrapper.vm.showConflictModal).toBe(false)
      expect(mockAssignmentService.getCourses).toHaveBeenCalled()
    })
  })

  describe('Refresh Functionality', () => {
    it('refreshes all data when refresh button is clicked', async () => {
      const wrapper = createWrapper()

      await wrapper.setData({
        loading: false,
        courses: mockCourses.content,
        stats: mockStats,
        availableTeachers: mockTeachers
      })

      const refreshButton = wrapper.findAll('button').find(btn =>
        btn.text().includes('Refresh')
      )
      await refreshButton.trigger('click')

      expect(mockAssignmentService.getCourses).toHaveBeenCalledTimes(2)
      expect(mockAssignmentService.getAssignmentStats).toHaveBeenCalledTimes(2)
      expect(mockAssignmentService.getAvailableTeachers).toHaveBeenCalledTimes(2)
    })
  })

  describe('Responsive Design', () => {
    it('renders correctly on different screen sizes', () => {
      const wrapper = createWrapper()

      // Check for responsive classes
      expect(wrapper.find('.grid').classes()).toContain('grid-cols-1')
      expect(wrapper.find('.grid').classes()).toContain('md:grid-cols-2')
      expect(wrapper.find('.grid').classes()).toContain('lg:grid-cols-3')
    })

    it('shows mobile-optimized table', () => {
      const wrapper = createWrapper()

      // Check for responsive table classes
      const table = wrapper.find('table')
      expect(table.classes()).toContain('overflow-x-auto')
    })
  })
})