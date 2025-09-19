import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CourseList from '../CourseList.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import SelectInput from '@/components/ui/SelectInput.vue'
import Badge from '@/components/ui/Badge.vue'

// Mock components
vi.mock('@/components/ui/Card.vue', () => ({
  Card: { template: '<div><slot /></div>' },
  CardHeader: { template: '<div><slot /></div>' },
  CardTitle: { template: '<div><slot /></div>' },
  CardDescription: { template: '<div><slot /></div>' },
  CardContent: { template: '<div><slot /></div>' }
}))

vi.mock('@/components/ui/Alert.vue', () => ({
  default: { template: '<div><slot /></div>' }
}))

// Mock course service
const mockCourseService = {
  getCourses: vi.fn(),
  createCourse: vi.fn(),
  updateCourse: vi.fn(),
  deleteCourse: vi.fn()
}

vi.mock('@/services/courseService', () => ({
  courseService: mockCourseService
}))

describe('CourseList', () => {
  let wrapper: any

  const mockDepartments = [
    { id: 1, code: 'CS', name: 'Computer Science' },
    { id: 2, code: 'MATH', name: 'Mathematics' }
  ]

  const mockCourses = [
    {
      id: 1,
      courseCode: 'CS101',
      title: 'Introduction to Computer Science',
      description: 'Basic computer science concepts',
      department: { id: 1, code: 'CS', name: 'Computer Science' },
      credits: 3,
      contactHoursPerWeek: 3,
      theoryHours: 3,
      labHours: 0,
      level: 'UNDERGRADUATE',
      isActive: true,
      maxStudents: 30,
      minStudents: 5,
      requiresLab: false,
      prerequisites: []
    },
    {
      id: 2,
      courseCode: 'MATH101',
      title: 'Calculus I',
      description: 'Introduction to calculus',
      department: { id: 2, code: 'MATH', name: 'Mathematics' },
      credits: 4,
      contactHoursPerWeek: 4,
      theoryHours: 4,
      labHours: 0,
      level: 'UNDERGRADUATE',
      isActive: true,
      maxStudents: 40,
      minStudents: 10,
      requiresLab: false,
      prerequisites: []
    }
  ]

  beforeEach(() => {
    mockCourseService.getCourses.mockResolvedValue({
      courses: mockCourses,
      total: 2,
      page: 0,
      size: 20,
      totalPages: 1
    })

    wrapper = mount(CourseList, {
      props: {
        departments: mockDepartments
      },
      global: {
        components: {
          Button,
          Input,
          SelectInput,
          Badge
        },
        mocks: {
          $toast: {
            success: vi.fn(),
            error: vi.fn(),
            warning: vi.fn(),
            info: vi.fn()
          }
        }
      }
    })
  })

  it('renders course list with header and actions', () => {
    expect(wrapper.find('h2').text()).toBe('Course Catalog')
    expect(wrapper.find('button').text()).toContain('Add Course')
  })

  it('loads courses on mount', async () => {
    await wrapper.vm.loadCourses()
    expect(mockCourseService.getCourses).toHaveBeenCalledWith(
      0, 20, 'id', 'ASC', {}
    )
  })

  it('displays courses in table format', async () => {
    await wrapper.vm.loadCourses()
    await wrapper.vm.$nextTick()

    const rows = wrapper.findAll('tbody tr')
    expect(rows).toHaveLength(2)
    expect(rows[0].text()).toContain('CS101')
    expect(rows[1].text()).toContain('MATH101')
  })

  it('shows loading state while loading', async () => {
    wrapper.setData({ loading: true })
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('shows empty state when no courses', async () => {
    mockCourseService.getCourses.mockResolvedValue({
      courses: [],
      total: 0,
      page: 0,
      size: 20,
      totalPages: 0
    })

    await wrapper.vm.loadCourses()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('No courses found')
  })

  it('filters courses by search term', async () => {
    await wrapper.setData({ filters: { search: 'CS101' } })
    await wrapper.vm.handleFilterChange()

    expect(mockCourseService.getCourses).toHaveBeenCalledWith(
      0, 20, 'id', 'ASC', { search: 'CS101' }
    )
  })

  it('filters courses by department', async () => {
    await wrapper.setData({ filters: { departmentId: 1 } })
    await wrapper.vm.handleFilterChange()

    expect(mockCourseService.getCourses).toHaveBeenCalledWith(
      0, 20, 'id', 'ASC', { departmentId: 1 }
    )
  })

  it('filters courses by level', async () => {
    await wrapper.setData({ filters: { level: 'UNDERGRADUATE' } })
    await wrapper.vm.handleFilterChange()

    expect(mockCourseService.getCourses).toHaveBeenCalledWith(
      0, 20, 'id', 'ASC', { level: 'UNDERGRADUATE' }
    )
  })

  it('filters courses by active status', async () => {
    await wrapper.setData({ filters: { active: true } })
    await wrapper.vm.handleFilterChange()

    expect(mockCourseService.getCourses).toHaveBeenCalledWith(
      0, 20, 'id', 'ASC', { active: true }
    )
  })

  it('debounces search input', async () => {
    vi.useFakeTimers()

    wrapper.vm.filters.search = 'test'
    await wrapper.vm.debouncedSearch()

    expect(mockCourseService.getCourses).not.toHaveBeenCalled()

    vi.advanceTimersByTime(300)

    expect(mockCourseService.getCourses).toHaveBeenCalled()

    vi.useRealTimers()
  })

  it('shows create form modal when add course clicked', async () => {
    await wrapper.find('button').trigger('click')
    expect(wrapper.vm.showCreateForm).toBe(true)
  })

  it('shows edit form modal when edit course clicked', async () => {
    await wrapper.vm.loadCourses()
    await wrapper.vm.$nextTick()

    const editButton = wrapper.find('button[title="Edit course"]')
    await editButton.trigger('click')

    expect(wrapper.vm.showEditForm).toBe(true)
    expect(wrapper.vm.selectedCourse).toEqual(mockCourses[0])
  })

  it('shows delete confirmation modal when delete course clicked', async () => {
    await wrapper.vm.loadCourses()
    await wrapper.vm.$nextTick()

    const deleteButton = wrapper.find('button[title="Delete course"]')
    await deleteButton.trigger('click')

    expect(wrapper.vm.showDeleteModal).toBe(true)
    expect(wrapper.vm.selectedCourse).toEqual(mockCourses[0])
  })

  it('deletes course when confirmed', async () => {
    mockCourseService.deleteCourse.mockResolvedValue({})

    await wrapper.vm.loadCourses()
    await wrapper.vm.$nextTick()

    wrapper.vm.selectedCourse = mockCourses[0]
    await wrapper.vm.confirmDeleteAction()

    expect(mockCourseService.deleteCourse).toHaveBeenCalledWith(mockCourses[0].id)
    expect(wrapper.vm.showDeleteModal).toBe(false)
  })

  it('handles delete error gracefully', async () => {
    mockCourseService.deleteCourse.mockRejectedValue(new Error('Delete failed'))

    await wrapper.vm.loadCourses()
    await wrapper.vm.$nextTick()

    wrapper.vm.selectedCourse = mockCourses[0]
    await wrapper.vm.confirmDeleteAction()

    expect(wrapper.vm.$toast.error).toHaveBeenCalledWith('Failed to delete course')
  })

  it('formats level badge correctly', () => {
    const variant = wrapper.vm.getLevelVariant('UNDERGRADUATE')
    expect(variant).toBe('default')

    const graduateVariant = wrapper.vm.getLevelVariant('GRADUATE')
    expect(graduateVariant).toBe('secondary')

    const phdVariant = wrapper.vm.getLevelVariant('PHD')
    expect(phdVariant).toBe('destructive')
  })

  it('formats level display correctly', () => {
    const formatted = wrapper.vm.formatLevel('UNDERGRADUATE')
    expect(formatted).toBe('Undergraduate')

    const formattedGrad = wrapper.vm.formatLevel('GRADUATE')
    expect(formattedGrad).toBe('Graduate')

    const formattedPhd = wrapper.vm.formatLevel('PHD')
    expect(formattedPhd).toBe('Phd')
  })

  it('shows pagination when multiple pages', async () => {
    mockCourseService.getCourses.mockResolvedValue({
      courses: mockCourses,
      total: 50,
      page: 0,
      size: 20,
      totalPages: 3
    })

    await wrapper.vm.loadCourses()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.pagination.totalPages).toBe(3)
    expect(wrapper.find('button[disabled="false"]').text()).toContain('Next')
  })

  it('changes page correctly', async () => {
    await wrapper.vm.changePage(1)
    expect(wrapper.vm.pagination.page).toBe(1)
    expect(mockCourseService.getCourses).toHaveBeenCalledWith(
      1, 20, 'id', 'ASC', {}
    )
  })

  it('disables previous button on first page', async () => {
    await wrapper.vm.loadCourses()
    await wrapper.vm.$nextTick()

    const prevButton = wrapper.findAll('button')[0]
    expect(prevButton.attributes('disabled')).toBe('')
  })

  it('enables previous button on subsequent pages', async () => {
    wrapper.vm.pagination.page = 1
    await wrapper.vm.$nextTick()

    const prevButton = wrapper.findAll('button')[0]
    expect(prevButton.attributes('disabled')).toBeUndefined()
  })
})