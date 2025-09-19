import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CourseForm from '../CourseForm.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import SelectInput from '@/components/ui/SelectInput.vue'

// Mock components
vi.mock('@/components/ui/Card.vue', () => ({
  Card: { template: '<div><slot /></div>' },
  CardHeader: { template: '<div><slot /></div>' },
  CardTitle: { template: '<div><slot /></div>' },
  CardDescription: { template: '<div><slot /></div>' },
  CardContent: { template: '<div><slot /></div>' }
}))

describe('CourseForm', () => {
  let wrapper: any

  const mockDepartments = [
    { id: 1, code: 'CS', name: 'Computer Science' },
    { id: 2, code: 'MATH', name: 'Mathematics' }
  ]

  const mockCourse = {
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
    maxStudents: 30,
    minStudents: 5,
    requiresLab: false,
    prerequisites: []
  }

  beforeEach(() => {
    wrapper = mount(CourseForm, {
      props: {
        departments: mockDepartments,
        course: mockCourse
      },
      global: {
        components: {
          Button,
          Input,
          SelectInput
        }
      }
    })
  })

  it('renders course form with all fields', () => {
    expect(wrapper.find('form').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="e.g., CS101"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="Course title"]').exists()).toBe(true)
    expect(wrapper.find('select').exists()).toBe(true)
  })

  it('populates form with course data when editing', () => {
    const courseCodeInput = wrapper.find('input[placeholder="e.g., CS101"]')
    const titleInput = wrapper.find('input[placeholder="Course title"]')

    expect(courseCodeInput.element.value).toBe('CS101')
    expect(titleInput.element.value).toBe('Introduction to Computer Science')
  })

  it('validates required fields', async () => {
    // Clear all required fields
    await wrapper.setData({
      formData: {
        courseCode: '',
        title: '',
        departmentId: 0,
        credits: 0,
        contactHoursPerWeek: 0,
        theoryHours: 0,
        labHours: 0,
        level: '',
        maxStudents: 0,
        minStudents: 0,
        requiresLab: false
      }
    })

    await wrapper.find('form').trigger('submit')

    // Should show validation errors
    expect(wrapper.emitted('submit')).toBeFalsy()
  })

  it('validates hours distribution', async () => {
    await wrapper.setData({
      formData: {
        ...wrapper.vm.formData,
        contactHoursPerWeek: 5,
        theoryHours: 3,
        labHours: 1
      }
    })

    await wrapper.find('form').trigger('submit')

    // Should show error for mismatched hours
    expect(wrapper.vm.errors.contactHoursPerWeek).toContain('must equal')
  })

  it('validates enrollment range', async () => {
    await wrapper.setData({
      formData: {
        ...wrapper.vm.formData,
        minStudents: 50,
        maxStudents: 30
      }
    })

    await wrapper.find('form').trigger('submit')

    // Should show error for invalid range
    expect(wrapper.vm.errors.minStudents).toContain('cannot exceed')
    expect(wrapper.vm.errors.maxStudents).toContain('cannot be less')
  })

  it('emits submit event with valid data', async () => {
    await wrapper.setData({
      formData: {
        courseCode: 'CS201',
        title: 'Data Structures',
        description: 'Advanced data structures',
        departmentId: 1,
        credits: 3,
        contactHoursPerWeek: 3,
        theoryHours: 3,
        labHours: 0,
        level: 'UNDERGRADUATE',
        maxStudents: 35,
        minStudents: 5,
        requiresLab: false,
        prerequisites: []
      }
    })

    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('submit')).toBeTruthy()
    const emittedData = wrapper.emitted('submit')[0][0]
    expect(emittedData.courseCode).toBe('CS201')
    expect(emittedData.title).toBe('Data Structures')
  })

  it('emits cancel event when cancel button clicked', async () => {
    await wrapper.find('button[type="button"]').trigger('click')
    expect(wrapper.emitted('cancel')).toBeTruthy()
  })

  it('shows correct title for create mode', () => {
    wrapper = mount(CourseForm, {
      props: {
        departments: mockDepartments
      },
      global: {
        components: {
          Button,
          Input,
          SelectInput
        }
      }
    })

    expect(wrapper.text()).toContain('Create New Course')
  })

  it('shows correct title for edit mode', () => {
    expect(wrapper.text()).toContain('Edit Course')
  })

  it('formats level options correctly', () => {
    const levelOptions = wrapper.vm.levelOptions
    expect(levelOptions).toHaveLength(3)
    expect(levelOptions[0].value).toBe('UNDERGRADUATE')
    expect(levelOptions[0].label).toBe('Undergraduate')
    expect(levelOptions[1].value).toBe('GRADUATE')
    expect(levelOptions[1].label).toBe('Graduate')
    expect(levelOptions[2].value).toBe('PHD')
    expect(levelOptions[2].label).toBe('PhD')
  })

  it('formats department options correctly', () => {
    const departmentOptions = wrapper.vm.departmentOptions
    expect(departmentOptions).toHaveLength(2)
    expect(departmentOptions[0].value).toBe('1')
    expect(departmentOptions[0].label).toBe('CS - Computer Science')
    expect(departmentOptions[1].value).toBe('2')
    expect(departmentOptions[1].label).toBe('MATH - Mathematics')
  })
})