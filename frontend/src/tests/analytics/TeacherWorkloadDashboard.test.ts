import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import TeacherWorkloadDashboard from '@/views/analytics/TeacherWorkloadDashboard.vue'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Select from '@/components/ui/Select.vue'

// Mock the assignment service
const mockAssignmentService = {
  getAssignmentStats: vi.fn()
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

describe('TeacherWorkloadDashboard', () => {
  const mockTeachers = {
    content: [
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
        currentWorkload: 32,
        availableHours: 8,
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
    ],
    totalElements: 2,
    totalPages: 1,
    size: 100,
    number: 0,
    first: true,
    last: true
  }

  const mockStats = {
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
      },
      {
        departmentId: 2,
        departmentName: 'Mathematics',
        totalCourses: 15,
        assignedCourses: 12,
        averageWorkload: 28
      }
    ]
  }

  const createWrapper = (props = {}) => {
    return mount(TeacherWorkloadDashboard, {
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
          'teacher-workload-detail-modal': true,
          'workload-optimization-modal': true
        }
      }
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()

    // Setup default mock responses
    mockAssignmentService.getAssignmentStats.mockResolvedValue(mockStats)
    mockTeacherService.getTeachers.mockResolvedValue(mockTeachers)
  })

  describe('Initial Loading', () => {
    it('renders correctly with loading state', () => {
      const wrapper = createWrapper()

      expect(wrapper.find('h1').text()).toBe('Teacher Workload Dashboard')
      expect(wrapper.text()).toContain('Workload Analytics')
      expect(wrapper.text()).toContain('Loading workload data...')
    })

    it('shows loading skeletons while loading data', () => {
      // Mock slow loading
      mockAssignmentService.getAssignmentStats.mockReturnValue(new Promise(() => {}))
      mockTeacherService.getTeachers.mockReturnValue(new Promise(() => {}))

      const wrapper = createWrapper()

      expect(wrapper.findAll('.animate-pulse').length).toBeGreaterThan(0)
      expect(wrapper.text()).toContain('Loading workload data...')
    })
  })

  describe('Data Loading', () => {
    it('loads workload statistics on mount', async () => {
      const wrapper = createWrapper()

      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 0))

      expect(mockAssignmentService.getAssignmentStats).toHaveBeenCalled()
    })

    it('loads teachers data on mount', async () => {
      const wrapper = createWrapper()

      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 0))

      expect(mockTeacherService.getTeachers).toHaveBeenCalledWith(
        expect.objectContaining({
          size: 100
        })
      )
    })

    it('shows executive summary cards after loading', async () => {
      const wrapper = createWrapper()

      // Simulate data loading completion
      await wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Total Teachers')
      expect(wrapper.text()).toContain('Average Workload')
      expect(wrapper.text()).toContain('Conflicts')
      expect(wrapper.text()).toContain('Unassigned Courses')
    })

    it('handles loading errors gracefully', async () => {
      mockAssignmentService.getAssignmentStats.mockRejectedValue(new Error('API Error'))
      mockTeacherService.getTeachers.mockRejectedValue(new Error('API Error'))

      const wrapper = createWrapper()

      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 0))

      expect(wrapper.vm.loading).toBe(false)
      expect(wrapper.vm.error).toBe('Failed to load workload data')
      expect(wrapper.text()).toContain('Failed to load workload data')
    })
  })

  describe('Executive Summary Cards', () => {
    beforeEach(async () => {
      const wrapper = createWrapper()
      await wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })
      await wrapper.vm.$nextTick()
    })

    it('displays total teachers count', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      expect(wrapper.text()).toContain('2 Teachers')
    })

    it('displays average workload', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      expect(wrapper.text()).toContain('32h')
    })

    it('displays conflicts count', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      expect(wrapper.text()).toContain('3 Conflicts')
    })

    it('displays unassigned courses count', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      expect(wrapper.text()).toContain('5 Unassigned')
    })

    it('shows color-coded conflict indicators', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      const conflictCard = wrapper.findAll('.border').find(card =>
        card.text().includes('Conflicts')
      )
      expect(conflictCard?.classes).toContain('border-orange-200')
    })
  })

  describe('Workload Distribution', () => {
    it('calculates workload distribution correctly', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      const distribution = wrapper.vm.workloadDistribution

      expect(distribution.underloaded).toBe(1) // Jane with 16h
      expect(distribution.optimal).toBe(0)
      expect(distribution.overloaded).toBe(1) // John with 32h (80% of 40h)
    })

    it('displays workload distribution chart', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      expect(wrapper.text()).toContain('Workload Distribution')
      expect(wrapper.text()).toContain('Underloaded')
      expect(wrapper.text()).toContain('Optimal')
      expect(wrapper.text()).toContain('Overloaded')
    })
  })

  describe('Department Analysis', () => {
    it('displays department workload stats', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      expect(wrapper.text()).toContain('Department Workload')
      expect(wrapper.text()).toContain('Computer Science')
      expect(wrapper.text()).toContain('35h avg')
      expect(wrapper.text()).toContain('Mathematics')
      expect(wrapper.text()).toContain('28h avg')
    })
  })

  describe('Teacher Table', () => {
    it('displays teacher workload table', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      expect(wrapper.text()).toContain('Teacher Workload Details')
      expect(wrapper.text()).toContain('John Doe')
      expect(wrapper.text()).toContain('Jane Smith')
      expect(wrapper.text()).toContain('32h / 40h')
      expect(wrapper.text()).toContain('16h / 40h')
    })

    it('shows workload utilization badges', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      expect(wrapper.text()).toContain('80%')
      expect(wrapper.text()).toContain('40%')
    })

    it('filters teachers by workload status', async () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      // Test filtering by "Overloaded"
      await wrapper.vm.updateFilter('workloadStatus', 'overloaded')

      expect(wrapper.vm.filteredTeachers).toHaveLength(1)
      expect(wrapper.vm.filteredTeachers[0].user.firstName).toBe('John')

      // Test filtering by "Underloaded"
      await wrapper.vm.updateFilter('workloadStatus', 'underloaded')

      expect(wrapper.vm.filteredTeachers).toHaveLength(1)
      expect(wrapper.vm.filteredTeachers[0].user.firstName).toBe('Jane')
    })

    it('filters teachers by department', async () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      await wrapper.vm.updateFilter('department', 1)

      expect(wrapper.vm.filteredTeachers).toHaveLength(2) // Both teachers are in CS
    })

    it('filters teachers by search query', async () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      await wrapper.vm.updateFilter('search', 'John')

      expect(wrapper.vm.filteredTeachers).toHaveLength(1)
      expect(wrapper.vm.filteredTeachers[0].user.firstName).toBe('John')
    })
  })

  describe('Workload Analysis', () => {
    it('calculates department analysis correctly', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      const analysis = wrapper.vm.departmentAnalysis

      expect(analysis['Computer Science']).toBeDefined()
      expect(analysis['Computer Science'].totalTeachers).toBe(2)
      expect(analysis['Computer Science'].averageWorkload).toBe(24) // (32 + 16) / 2
    })

    it('identifies optimization opportunities', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      const opportunities = wrapper.vm.optimizationOpportunities

      expect(opportunities).toHaveLength(2) // John (overloaded) and Jane (underloaded)
    })
  })

  describe('Modal Interactions', () => {
    it('opens teacher detail modal when view details is clicked', async () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      // Find and click view details button for John
      const viewButton = wrapper.findAll('button').find(btn =>
        btn.text().includes('View Details')
      )
      await viewButton.trigger('click')

      expect(wrapper.vm.selectedTeacher).toBeDefined()
      expect(wrapper.vm.showDetailModal).toBe(true)
    })

    it('opens optimization modal when optimize is clicked', async () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      // Find and click optimize button for overloaded teacher
      const optimizeButton = wrapper.findAll('button').find(btn =>
        btn.text().includes('Optimize')
      )
      await optimizeButton.trigger('click')

      expect(wrapper.vm.selectedTeacher).toBeDefined()
      expect(wrapper.vm.showOptimizationModal).toBe(true)
    })

    it('emits events when modals are closed', async () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content,
        showDetailModal: true
      })

      // Simulate modal close
      await wrapper.vm.closeDetailModal()

      expect(wrapper.vm.showDetailModal).toBe(false)
      expect(wrapper.vm.selectedTeacher).toBeNull()
    })
  })

  describe('Refresh Functionality', () => {
    it('refreshes data when refresh button is clicked', async () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      const refreshButton = wrapper.findAll('button').find(btn =>
        btn.text().includes('Refresh')
      )
      await refreshButton.trigger('click')

      expect(mockAssignmentService.getAssignmentStats).toHaveBeenCalledTimes(2)
      expect(mockTeacherService.getTeachers).toHaveBeenCalledTimes(2)
    })

    it('shows loading state during refresh', async () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      // Mock slow refresh
      mockAssignmentService.getAssignmentStats.mockReturnValue(new Promise(() => {}))
      mockTeacherService.getTeachers.mockReturnValue(new Promise(() => {}))

      const refreshButton = wrapper.findAll('button').find(btn =>
        btn.text().includes('Refresh')
      )
      await refreshButton.trigger('click')

      expect(wrapper.vm.loading).toBe(true)
    })
  })

  describe('Responsive Design', () => {
    it('renders correctly on different screen sizes', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      // Check for responsive classes
      expect(wrapper.find('.grid').classes()).toContain('grid-cols-1')
      expect(wrapper.find('.grid').classes()).toContain('md:grid-cols-2')
      expect(wrapper.find('.grid').classes()).toContain('lg:grid-cols-4')
    })

    it('shows mobile-optimized layout', () => {
      const wrapper = createWrapper()
      wrapper.setData({
        loading: false,
        stats: mockStats,
        teachers: mockTeachers.content
      })

      // Check for mobile-first responsive classes
      const cards = wrapper.findAll('.border')
      cards.forEach(card => {
        expect(card.classes()).toContain('p-6') // Consistent padding
      })
    })
  })
})