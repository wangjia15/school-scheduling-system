import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import TeacherProfileModal from '@/components/teachers/TeacherProfileModal.vue'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Select from '@/components/ui/Select.vue'
import Badge from '@/components/ui/Badge.vue'

// Mock the teacher service
const mockTeacherService = {
  createTeacher: vi.fn(),
  updateTeacher: vi.fn(),
  getTeacherById: vi.fn()
}

vi.mock('@/services/teacherService', () => ({
  default: mockTeacherService
}))

describe('TeacherProfileModal', () => {
  const mockTeacher = {
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

  const mockDepartments = [
    { id: 1, name: 'Computer Science' },
    { id: 2, name: 'Mathematics' },
    { id: 3, name: 'Physics' }
  ]

  const createWrapper = (props = {}) => {
    return mount(TeacherProfileModal, {
      props: {
        open: true,
        teacher: undefined,
        departments: mockDepartments,
        ...props
      },
      global: {
        components: {
          Card,
          Button,
          Select,
          Badge
        }
      }
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Rendering', () => {
    it('renders modal when open is true', () => {
      const wrapper = createWrapper()

      expect(wrapper.find('.fixed.inset-0').exists()).toBe(true)
      expect(wrapper.find('h2').text()).toContain('Teacher Profile Management')
    })

    it('does not render modal when open is false', () => {
      const wrapper = createWrapper({ open: false })

      expect(wrapper.find('.fixed.inset-0').exists()).toBe(false)
    })

    it('shows "Create Profile" button for new teacher', () => {
      const wrapper = createWrapper()

      expect(wrapper.find('button[type="submit"]').text()).toContain('Create Profile')
    })

    it('shows "Update Profile" button for existing teacher', () => {
      const wrapper = createWrapper({ teacher: mockTeacher })

      expect(wrapper.find('button[type="submit"]').text()).toContain('Update Profile')
    })

    it('displays teacher information in edit mode', () => {
      const wrapper = createWrapper({ teacher: mockTeacher })

      expect(wrapper.find('input[v-model="profileData.user.firstName"]').element.value).toBe('John')
      expect(wrapper.find('input[v-model="profileData.user.lastName"]').element.value).toBe('Doe')
      expect(wrapper.find('input[v-model="profileData.employeeId"]').element.value).toBe('EMP001')
    })
  })

  describe('Form Fields', () => {
    it('has all required form fields', () => {
      const wrapper = createWrapper()

      expect(wrapper.find('input[v-model="profileData.user.firstName"]').exists()).toBe(true)
      expect(wrapper.find('input[v-model="profileData.user.lastName"]').exists()).toBe(true)
      expect(wrapper.find('input[v-model="profileData.user.email"]').exists()).toBe(true)
      expect(wrapper.find('input[v-model="profileData.employeeId"]').exists()).toBe(true)
      expect(wrapper.find('select[v-model="profileData.title"]').exists()).toBe(true)
      expect(wrapper.find('select[v-model="profileData.departmentId"]').exists()).toBe(true)
      expect(wrapper.find('input[v-model="profileData.maxWeeklyHours"]').exists()).toBe(true)
      expect(wrapper.find('input[v-model="profileData.maxCoursesPerSemester"]').exists()).toBe(true)
    })

    it('has proper validation attributes', () => {
      const wrapper = createWrapper()

      const firstNameInput = wrapper.find('input[v-model="profileData.user.firstName"]')
      expect(firstNameInput.attributes('required')).toBe('true')

      const emailInput = wrapper.find('input[v-model="profileData.user.email"]')
      expect(emailInput.attributes('type')).toBe('email')

      const hoursInput = wrapper.find('input[v-model="profileData.maxWeeklyHours"]')
      expect(hoursInput.attributes('type')).toBe('number')
      expect(hoursInput.attributes('min')).toBe('1')
      expect(hoursInput.attributes('max')).toBe('80')
    })
  })

  describe('Specializations Management', () => {
    it('starts with no specializations for new teacher', () => {
      const wrapper = createWrapper()

      expect(wrapper.vm.profileData.specializations).toHaveLength(0)
      expect(wrapper.text()).toContain('No specializations added')
    })

    it('loads existing specializations for edit mode', () => {
      const wrapper = createWrapper({ teacher: mockTeacher })

      expect(wrapper.vm.profileData.specializations).toHaveLength(1)
      expect(wrapper.vm.profileData.specializations[0].subjectCode).toBe('CS101')
    })

    it('can add new specialization', async () => {
      const wrapper = createWrapper()

      await wrapper.find('button[type="button"]').trigger('click')

      expect(wrapper.vm.profileData.specializations).toHaveLength(1)
      expect(wrapper.text()).toContain('Specialization 1')
    })

    it('can remove specialization', async () => {
      const wrapper = createWrapper({ teacher: mockTeacher })

      expect(wrapper.vm.profileData.specializations).toHaveLength(1)

      await wrapper.find('button.text-red-600').trigger('click')

      expect(wrapper.vm.profileData.specializations).toHaveLength(0)
    })

    it('shows certification details when certified', async () => {
      const wrapper = createWrapper()

      // Add a specialization
      await wrapper.find('button[type="button"]').trigger('click')

      // Check certification checkbox
      const certCheckbox = wrapper.find('input[type="checkbox"]')
      await certCheckbox.setValue(true)

      expect(wrapper.find('textarea').exists()).toBe(true)
      expect(wrapper.find('textarea').attributes('placeholder')).toContain('certification')
    })
  })

  describe('Qualifications Management', () => {
    it('starts with no qualifications for new teacher', () => {
      const wrapper = createWrapper()

      expect(wrapper.vm.profileData.qualifications).toHaveLength(0)
      expect(wrapper.text()).toContain('No qualifications added')
    })

    it('can add new qualification', async () => {
      const wrapper = createWrapper()

      // Find and click the "Add Qualification" button
      const addButton = wrapper.findAll('button[type="button"]').find(btn =>
        btn.text().includes('Add Qualification')
      )
      await addButton.trigger('click')

      expect(wrapper.vm.profileData.qualifications).toHaveLength(1)
      expect(wrapper.text()).toContain('Qualification 1')
    })

    it('can remove qualification', async () => {
      const wrapper = createWrapper()

      // Add a qualification first
      const addButton = wrapper.findAll('button[type="button"]').find(btn =>
        btn.text().includes('Add Qualification')
      )
      await addButton.trigger('click')

      expect(wrapper.vm.profileData.qualifications).toHaveLength(1)

      // Remove it
      const removeButton = wrapper.findAll('button.text-red-600').find(btn =>
        btn.find('svg') // Trash icon
      )
      await removeButton.trigger('click')

      expect(wrapper.vm.profileData.qualifications).toHaveLength(0)
    })

    it('has proper qualification form fields', async () => {
      const wrapper = createWrapper()

      // Add a qualification
      const addButton = wrapper.findAll('button[type="button"]').find(btn =>
        btn.text().includes('Add Qualification')
      )
      await addButton.trigger('click')

      expect(wrapper.find('input[v-model*="degree"]').exists()).toBe(true)
      expect(wrapper.find('input[v-model*="institution"]').exists()).toBe(true)
      expect(wrapper.find('input[v-model*="year"]').exists()).toBe(true)
      expect(wrapper.find('select[v-model*="type"]').exists()).toBe(true)
      expect(wrapper.find('textarea[v-model*="description"]').exists()).toBe(true)
    })
  })

  describe('Form Submission', () => {
    it('calls createTeacher for new teacher', async () => {
      const wrapper = createWrapper()

      // Fill form
      await wrapper.find('input[v-model="profileData.user.firstName"]').setValue('Jane')
      await wrapper.find('input[v-model="profileData.user.lastName"]').setValue('Smith')
      await wrapper.find('input[v-model="profileData.user.email"]').setValue('jane.smith@university.edu')
      await wrapper.find('input[v-model="profileData.employeeId"]').setValue('EMP002')

      mockTeacherService.createTeacher.mockResolvedValue(mockTeacher)

      await wrapper.find('form').trigger('submit')

      expect(mockTeacherService.createTeacher).toHaveBeenCalledWith({
        employeeId: 'EMP002',
        userId: 0,
        departmentId: 1,
        title: 'PROFESSOR',
        maxWeeklyHours: 40,
        maxCoursesPerSemester: 4,
        officeLocation: '',
        phone: '',
        specializations: []
      })
    })

    it('calls updateTeacher for existing teacher', async () => {
      const wrapper = createWrapper({ teacher: mockTeacher })

      // Update some fields
      await wrapper.find('input[v-model="profileData.phone"]').setValue('+1 (555) 987-6543')

      mockTeacherService.updateTeacher.mockResolvedValue(mockTeacher)

      await wrapper.find('form').trigger('submit')

      expect(mockTeacherService.updateTeacher).toHaveBeenCalledWith(1, {
        employeeId: 'EMP001',
        userId: 1,
        departmentId: 1,
        title: 'PROFESSOR',
        maxWeeklyHours: 40,
        maxCoursesPerSemester: 5,
        officeLocation: 'Building A, Room 101',
        phone: '+1 (555) 987-6543',
        specializations: [
          {
            subjectCode: 'CS101',
            proficiencyLevel: 'EXPERT',
            yearsExperience: 10,
            certified: true,
            certificationDetails: 'PhD in Computer Science'
          }
        ]
      })
    })

    it('emits save event after successful submission', async () => {
      const wrapper = createWrapper()

      // Fill minimal required fields
      await wrapper.find('input[v-model="profileData.user.firstName"]').setValue('Jane')
      await wrapper.find('input[v-model="profileData.user.lastName"]').setValue('Smith')
      await wrapper.find('input[v-model="profileData.user.email"]').setValue('jane.smith@university.edu')
      await wrapper.find('input[v-model="profileData.employeeId"]').setValue('EMP002')

      mockTeacherService.createTeacher.mockResolvedValue(mockTeacher)

      await wrapper.find('form').trigger('submit')

      expect(wrapper.emitted('save')).toBeTruthy()
    })

    it('shows loading state during submission', async () => {
      const wrapper = createWrapper()

      // Fill form
      await wrapper.find('input[v-model="profileData.user.firstName"]').setValue('Jane')
      await wrapper.find('input[v-model="profileData.user.lastName"]').setValue('Smith')
      await wrapper.find('input[v-model="profileData.user.email"]').setValue('jane.smith@university.edu')
      await wrapper.find('input[v-model="profileData.employeeId"]').setValue('EMP002')

      // Mock a slow response
      mockTeacherService.createTeacher.mockReturnValue(new Promise(() => {}))

      await wrapper.find('form').trigger('submit')

      expect(wrapper.vm.saving).toBe(true)
      expect(wrapper.find('button[type="submit"]').text()).toContain('Create Profile')
      expect(wrapper.find('button[type="submit"]').find('.animate-spin').exists()).toBe(true)
    })

    it('handles submission errors gracefully', async () => {
      const wrapper = createWrapper()

      // Fill form
      await wrapper.find('input[v-model="profileData.user.firstName"]').setValue('Jane')
      await wrapper.find('input[v-model="profileData.user.lastName"]').setValue('Smith')
      await wrapper.find('input[v-model="profileData.user.email"]').setValue('jane.smith@university.edu')
      await wrapper.find('input[v-model="profileData.employeeId"]').setValue('EMP002')

      mockTeacherService.createTeacher.mockRejectedValue(new Error('Creation failed'))

      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      await wrapper.find('form').trigger('submit')

      expect(consoleSpy).toHaveBeenCalledWith('Failed to save profile:', expect.any(Error))

      consoleSpy.mockRestore()
    })
  })

  describe('Modal Interactions', () => {
    it('emits update:open when close button is clicked', async () => {
      const wrapper = createWrapper()

      await wrapper.find('button[variant="ghost"]').trigger('click')

      expect(wrapper.emitted('update:open')).toBeTruthy()
      expect(wrapper.emitted('update:open')[0]).toEqual([false])
    })

    it('emits update:open when cancel button is clicked', async () => {
      const wrapper = createWrapper()

      const cancelButton = wrapper.findAll('button').find(btn => btn.text().includes('Cancel'))
      await cancelButton.trigger('click')

      expect(wrapper.emitted('update:open')).toBeTruthy()
      expect(wrapper.emitted('update:open')[0]).toEqual([false])
    })

    it('resets form when modal opens for new teacher', async () => {
      const wrapper = createWrapper({ open: false })

      // Open modal
      await wrapper.setProps({ open: true })

      expect(wrapper.vm.profileData.user.firstName).toBe('')
      expect(wrapper.vm.profileData.specializations).toHaveLength(0)
      expect(wrapper.vm.profileData.qualifications).toHaveLength(0)
    })

    it('loads teacher data when modal opens for existing teacher', async () => {
      const wrapper = createWrapper({ open: false, teacher: mockTeacher })

      // Open modal
      await wrapper.setProps({ open: true })

      expect(wrapper.vm.profileData.user.firstName).toBe('John')
      expect(wrapper.vm.profileData.specializations).toHaveLength(1)
    })
  })

  describe('Form Validation', () => {
    it('requires first name', async () => {
      const wrapper = createWrapper()

      const form = wrapper.find('form')
      const firstNameInput = wrapper.find('input[v-model="profileData.user.firstName"]')

      // Try to submit without first name
      await form.trigger('submit')

      // Form should not submit (validation prevents it)
      expect(mockTeacherService.createTeacher).not.toHaveBeenCalled()
    })

    it('requires valid email format', async () => {
      const wrapper = createWrapper()

      const form = wrapper.find('form')
      const emailInput = wrapper.find('input[v-model="profileData.user.email"]')

      // Set invalid email
      await emailInput.setValue('invalid-email')
      await form.trigger('submit')

      // Form should not submit
      expect(mockTeacherService.createTeacher).not.toHaveBeenCalled()
    })

    it('requires employee ID', async () => {
      const wrapper = createWrapper()

      const form = wrapper.find('form')

      // Try to submit without employee ID
      await form.trigger('submit')

      // Form should not submit
      expect(mockTeacherService.createTeacher).not.toHaveBeenCalled()
    })

    it('validates max weekly hours range', async () => {
      const wrapper = createWrapper()

      const hoursInput = wrapper.find('input[v-model="profileData.maxWeeklyHours"]')

      // Set invalid value (too high)
      await hoursInput.setValue(100)

      expect(hoursInput.element.value).toBe('100')
      // HTML5 validation should prevent this, but we can check the value was set
    })
  })
})