import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import TeacherListView from '@/views/teachers/TeacherListView.vue'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import Badge from '@/components/ui/Badge.vue'
import Table from '@/components/ui/Table.vue'

// Mock the router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

// Mock the teacher service
const mockTeacherService = {
  getTeachers: vi.fn(),
  getTeacherById: vi.fn(),
  createTeacher: vi.fn(),
  updateTeacher: vi.fn(),
  deleteTeacher: vi.fn(),
  getTeacherByEmployeeId: vi.fn(),
  getTeachersByDepartment: vi.fn(),
  getTeachersBySpecialization: vi.fn(),
  getTeacherAvailability: vi.fn(),
  getTeacherAvailabilityByDay: vi.fn(),
  addTeacherAvailability: vi.fn(),
  updateTeacherAvailability: vi.fn(),
  deleteTeacherAvailability: vi.fn(),
  findAvailableTeachersForTimeSlot: vi.fn()
}

vi.mock('@/services/teacherService', () => ({
  default: mockTeacherService
}))

describe('TeacherListView', () => {
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
    ],
    totalElements: 1,
    totalPages: 1,
    size: 20,
    number: 0,
    first: true,
    last: true
  }

  const mockDepartments = [
    { id: 1, name: 'Computer Science' },
    { id: 2, name: 'Mathematics' },
    { id: 3, name: 'Physics' }
  ]

  const mockUsers = [
    { id: 1, firstName: 'John', lastName: 'Doe', email: 'john.doe@university.edu' },
    { id: 2, firstName: 'Jane', lastName: 'Smith', email: 'jane.smith@university.edu' }
  ]

  beforeEach(() => {
    vi.clearAllMocks()

    // Setup default mock responses
    mockTeacherService.getTeachers.mockResolvedValue(mockTeachers)
  })

  it('renders correctly with teachers', async () => {
    const wrapper = mount(TeacherListView, {
      global: {
        components: {
          Card,
          Button,
          Input,
          Select,
          Badge,
          Table
        },
        stubs: {
          'card-header': true,
          'card-title': true,
          'card-content': true,
          'teacher-form-modal': true,
          'teacher-detail-modal': true
        }
      }
    })

    await wrapper.vm.$nextTick()

    expect(wrapper.find('h1').text()).toBe('Teachers')
    expect(wrapper.find('p.text-gray-600').text()).toContain('Manage teacher profiles')
    expect(wrapper.text()).toContain('Teachers (1)')
  })

  it('shows loading state initially', () => {
    mockTeacherService.getTeachers.mockReturnValue(new Promise(() => {}))

    const wrapper = mount(TeacherListView, {
      global: {
        components: {
          Card,
          Button,
          Input,
          Select,
          Badge,
          Table
        },
        stubs: {
          'card-header': true,
          'card-title': true,
          'card-content': true,
          'teacher-form-modal': true,
          'teacher-detail-modal': true
        }
      }
    })

    expect(wrapper.find('[data-testid="loading"]').exists()).toBe(true)
  })

  it('shows empty state when no teachers', async () => {
    mockTeacherService.getTeachers.mockResolvedValue({
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: 20,
      number: 0,
      first: true,
      last: true
    })

    const wrapper = mount(TeacherListView, {
      global: {
        components: {
          Card,
          Button,
          Input,
          Select,
          Badge,
          Table
        },
        stubs: {
          'card-header': true,
          'card-title': true,
          'card-content': true,
          'teacher-form-modal': true,
          'teacher-detail-modal': true
        }
      }
    })

    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('No teachers found')
  })

  it('handles API errors gracefully', async () => {
    mockTeacherService.getTeachers.mockRejectedValue(new Error('API Error'))

    const wrapper = mount(TeacherListView, {
      global: {
        components: {
          Card,
          Button,
          Input,
          Select,
          Badge,
          Table
        },
        stubs: {
          'card-header': true,
          'card-title': true,
          'card-content': true,
          'teacher-form-modal': true,
          'teacher-detail-modal': true
        }
      }
    })

    await wrapper.vm.$nextTick()

    // Should not crash and should show empty state
    expect(wrapper.text()).toContain('No teachers found')
  })

  it('opens create modal when Add Teacher button is clicked', async () => {
    const wrapper = mount(TeacherListView, {
      global: {
        components: {
          Card,
          Button,
          Input,
          Select,
          Badge,
          Table
        },
        stubs: {
          'card-header': true,
          'card-title': true,
          'card-content': true,
          'teacher-form-modal': true,
          'teacher-detail-modal': true
        }
      }
    })

    await wrapper.vm.$nextTick()

    const addButton = wrapper.find('button')
    await addButton.trigger('click')

    expect(wrapper.vm.showCreateModal).toBe(true)
  })

  it('navigates to availability page when Schedule button is clicked', async () => {
    const wrapper = mount(TeacherListView, {
      global: {
        components: {
          Card,
          Button,
          Input,
          Select,
          Badge,
          Table
        },
        stubs: {
          'card-header': true,
          'card-title': true,
          'card-content': true,
          'teacher-form-modal': true,
          'teacher-detail-modal': true
        }
      }
    })

    await wrapper.vm.$nextTick()

    const scheduleButton = wrapper.findAll('button').find(btn => btn.text().includes('Schedule'))
    if (scheduleButton) {
      await scheduleButton.trigger('click')
      expect(mockPush).toHaveBeenCalledWith('/teachers/1/availability')
    }
  })

  it('filters teachers when Apply Filters button is clicked', async () => {
    const wrapper = mount(TeacherListView, {
      global: {
        components: {
          Card,
          Button,
          Input,
          Select,
          Badge,
          Table
        },
        stubs: {
          'card-header': true,
          'card-title': true,
          'card-content': true,
          'teacher-form-modal': true,
          'teacher-detail-modal': true
        }
      }
    })

    await wrapper.vm.$nextTick()

    // Set filter values
    await wrapper.find('input[placeholder*="Search"]').setValue('John')
    await wrapper.find('select').setValue('PROFESSOR')

    const applyButton = wrapper.findAll('button').find(btn => btn.text().includes('Apply Filters'))
    await applyButton.trigger('click')

    expect(mockTeacherService.getTeachers).toHaveBeenCalledWith(
      expect.objectContaining({
        search: 'John',
        title: 'PROFESSOR'
      })
    )
  })

  it('formats teacher title correctly', () => {
    const wrapper = mount(TeacherListView, {
      global: {
        components: {
          Card,
          Button,
          Input,
          Select,
          Badge,
          Table
        },
        stubs: {
          'card-header': true,
          'card-title': true,
          'card-content': true,
          'teacher-form-modal': true,
          'teacher-detail-modal': true
        }
      }
    })

    expect(wrapper.vm.formatTitle('PROFESSOR')).toBe('Professor')
    expect(wrapper.vm.formatTitle('ASSISTANT_PROFESSOR')).toBe('Assistant Professor')
    expect(wrapper.vm.formatTitle('ADJUNCT')).toBe('Adjunct')
  })

  it('returns correct badge variant for teacher title', () => {
    const wrapper = mount(TeacherListView, {
      global: {
        components: {
          Card,
          Button,
          Input,
          Select,
          Badge,
          Table
        },
        stubs: {
          'card-header': true,
          'card-title': true,
          'card-content': true,
          'teacher-form-modal': true,
          'teacher-detail-modal': true
        }
      }
    })

    expect(wrapper.vm.getTitleBadgeVariant('PROFESSOR')).toBe('default')
    expect(wrapper.vm.getTitleBadgeVariant('ASSOCIATE_PROFESSOR')).toBe('secondary')
    expect(wrapper.vm.getTitleBadgeVariant('ASSISTANT_PROFESSOR')).toBe('outline')
  })

  it('handles pagination correctly', async () => {
    const wrapper = mount(TeacherListView, {
      global: {
        components: {
          Card,
          Button,
          Input,
          Select,
          Badge,
          Table
        },
        stubs: {
          'card-header': true,
          'card-title': true,
          'card-content': true,
          'teacher-form-modal': true,
          'teacher-detail-modal': true
        }
      }
    })

    await wrapper.vm.$nextTick()

    // Test going to next page
    await wrapper.vm.goToPage(1)
    expect(wrapper.vm.filters.page).toBe(1)
    expect(mockTeacherService.getTeachers).toHaveBeenCalled()
  })

  it('displays teacher information correctly', async () => {
    const wrapper = mount(TeacherListView, {
      global: {
        components: {
          Card,
          Button,
          Input,
          Select,
          Badge,
          Table
        },
        stubs: {
          'card-header': true,
          'card-title': true,
          'card-content': true,
          'teacher-form-modal': true,
          'teacher-detail-modal': true
        }
      }
    })

    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('John Doe')
    expect(wrapper.text()).toContain('EMP001')
    expect(wrapper.text()).toContain('Computer Science')
    expect(wrapper.text()).toContain('40h/week')
    expect(wrapper.text()).toContain('5 courses/semester')
    expect(wrapper.text()).toContain('CS101')
  })
})