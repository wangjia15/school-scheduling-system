<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
      <div class="px-6 py-4 border-b border-gray-200">
        <h3 class="text-lg font-medium text-gray-900">
          {{ teacher ? 'Edit Teacher' : 'Add New Teacher' }}
        </h3>
      </div>

      <form @submit.prevent="handleSubmit" class="px-6 py-4 space-y-4">
        <!-- Basic Information -->
        <div class="space-y-4">
          <h4 class="text-sm font-medium text-gray-900 border-b pb-2">Basic Information</h4>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">User</label>
              <Select v-model="form.userId" required>
                <option value="">Select a user</option>
                <option v-for="user in users" :key="user.id" :value="user.id">
                  {{ user.firstName }} {{ user.lastName }} ({{ user.email }})
                </option>
              </Select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Employee ID</label>
              <Input
                v-model="form.employeeId"
                required
                placeholder="e.g., EMP001"
              />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Department</label>
              <Select v-model="form.departmentId" required>
                <option value="">Select department</option>
                <option v-for="dept in departments" :key="dept.id" :value="dept.id">
                  {{ dept.name }}
                </option>
              </Select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Title</label>
              <Select v-model="form.title" required>
                <option value="">Select title</option>
                <option value="PROFESSOR">Professor</option>
                <option value="ASSOCIATE_PROFESSOR">Associate Professor</option>
                <option value="ASSISTANT_PROFESSOR">Assistant Professor</option>
                <option value="INSTRUCTOR">Instructor</option>
                <option value="ADJUNCT">Adjunct</option>
              </Select>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Max Weekly Hours</label>
              <Input
                v-model.number="form.maxWeeklyHours"
                type="number"
                min="1"
                max="60"
                step="0.5"
                required
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Max Courses per Semester</label>
              <Input
                v-model.number="form.maxCoursesPerSemester"
                type="number"
                min="1"
                max="10"
                required
              />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Office Location</label>
              <Input
                v-model="form.officeLocation"
                placeholder="e.g., Building A, Room 101"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
              <Input
                v-model="form.phone"
                placeholder="e.g., +1 (555) 123-4567"
              />
            </div>
          </div>
        </div>

        <!-- Specializations -->
        <div class="space-y-4">
          <div class="flex justify-between items-center">
            <h4 class="text-sm font-medium text-gray-900 border-b pb-2">Specializations</h4>
            <Button type="button" variant="outline" size="sm" @click="addSpecialization">
              <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              Add Specialization
            </Button>
          </div>

          <div v-for="(spec, index) in form.specializations" :key="index" class="border rounded-lg p-4 space-y-3">
            <div class="flex justify-between items-center">
              <h5 class="text-sm font-medium text-gray-700">Specialization {{ index + 1 }}</h5>
              <Button type="button" variant="ghost" size="sm" @click="removeSpecialization(index)">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </Button>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Subject Code</label>
                <Input
                  v-model="spec.subjectCode"
                  required
                  placeholder="e.g., CS101"
                />
              </div>

              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Subject Name</label>
                <Input
                  v-model="spec.subjectName"
                  placeholder="e.g., Introduction to Computer Science"
                />
              </div>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Proficiency Level</label>
                <Select v-model="spec.proficiencyLevel" required>
                  <option value="BEGINNER">Beginner</option>
                  <option value="INTERMEDIATE">Intermediate</option>
                  <option value="ADVANCED">Advanced</option>
                  <option value="EXPERT">Expert</option>
                </Select>
              </div>

              <div>
                <label class="block text-xs font-medium text-gray-700 mb-1">Years of Experience</label>
                <Input
                  v-model.number="spec.yearsExperience"
                  type="number"
                  min="0"
                  max="50"
                  required
                />
              </div>
            </div>

            <div class="flex items-center gap-4">
              <div class="flex items-center">
                <input
                  v-model="spec.certified"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label class="ml-2 block text-xs text-gray-700">Certified</label>
              </div>

              <div v-if="spec.certified" class="flex-1">
                <label class="block text-xs font-medium text-gray-700 mb-1">Certification Details</label>
                <Input
                  v-model="spec.certificationDetails"
                  placeholder="e.g., PhD in Computer Science"
                />
              </div>
            </div>
          </div>

          <div v-if="form.specializations.length === 0" class="text-center py-4 text-gray-500">
            No specializations added yet
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="error" class="text-red-600 text-sm">{{ error }}</div>

        <!-- Actions -->
        <div class="flex justify-end gap-3 pt-4 border-t">
          <Button type="button" variant="outline" @click="$emit('close')">
            Cancel
          </Button>
          <Button type="submit" :disabled="loading">
            {{ loading ? 'Saving...' : (teacher ? 'Update' : 'Create') }}
          </Button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import teacherService, { type Teacher, type TeacherRequest, type TeacherSpecializationRequest } from '@/services/teacherService'

interface Props {
  teacher?: Teacher | null
  departments: Array<{ id: number; name: string }>
  users: Array<{ id: number; firstName: string; lastName: string; email: string }>
}

interface Emits {
  (e: 'close'): void
  (e: 'save'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const error = ref('')

const form = reactive<TeacherRequest>({
  employeeId: '',
  userId: 0,
  departmentId: 0,
  title: 'INSTRUCTOR',
  maxWeeklyHours: 40,
  maxCoursesPerSemester: 5,
  officeLocation: '',
  phone: '',
  specializations: []
})

// Initialize form when editing
watch(() => props.teacher, (newTeacher) => {
  if (newTeacher) {
    form.employeeId = newTeacher.employeeId
    form.userId = newTeacher.userId
    form.departmentId = newTeacher.departmentId
    form.title = newTeacher.title
    form.maxWeeklyHours = newTeacher.maxWeeklyHours
    form.maxCoursesPerSemester = newTeacher.maxCoursesPerSemester
    form.officeLocation = newTeacher.officeLocation || ''
    form.phone = newTeacher.phone || ''
    form.specializations = newTeacher.specializations.map(spec => ({
      subjectCode: spec.subjectCode,
      subjectName: spec.subjectName || '',
      proficiencyLevel: spec.proficiencyLevel,
      yearsExperience: spec.yearsExperience,
      certified: spec.certified || false,
      certificationDetails: spec.certificationDetails || ''
    }))
  } else {
    resetForm()
  }
}, { immediate: true })

const resetForm = () => {
  form.employeeId = ''
  form.userId = 0
  form.departmentId = 0
  form.title = 'INSTRUCTOR'
  form.maxWeeklyHours = 40
  form.maxCoursesPerSemester = 5
  form.officeLocation = ''
  form.phone = ''
  form.specializations = []
  error.value = ''
}

const addSpecialization = () => {
  form.specializations.push({
    subjectCode: '',
    subjectName: '',
    proficiencyLevel: 'INTERMEDIATE',
    yearsExperience: 0,
    certified: false,
    certificationDetails: ''
  })
}

const removeSpecialization = (index: number) => {
  form.specializations.splice(index, 1)
}

const validateForm = () => {
  if (!form.employeeId.trim()) {
    error.value = 'Employee ID is required'
    return false
  }

  if (!form.userId) {
    error.value = 'User selection is required'
    return false
  }

  if (!form.departmentId) {
    error.value = 'Department selection is required'
    return false
  }

  if (form.maxWeeklyHours < 1 || form.maxWeeklyHours > 60) {
    error.value = 'Max weekly hours must be between 1 and 60'
    return false
  }

  if (form.maxCoursesPerSemester < 1 || form.maxCoursesPerSemester > 10) {
    error.value = 'Max courses per semester must be between 1 and 10'
    return false
  }

  // Validate specializations
  for (let i = 0; i < form.specializations.length; i++) {
    const spec = form.specializations[i]
    if (!spec.subjectCode.trim()) {
      error.value = `Subject code is required for specialization ${i + 1}`
      return false
    }
    if (spec.yearsExperience < 0 || spec.yearsExperience > 50) {
      error.value = `Years of experience must be between 0 and 50 for specialization ${i + 1}`
      return false
    }
  }

  error.value = ''
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  loading.value = true
  error.value = ''

  try {
    if (props.teacher) {
      await teacherService.updateTeacher(props.teacher.id, form)
    } else {
      await teacherService.createTeacher(form)
    }

    emit('save')
  } catch (err: any) {
    console.error('Failed to save teacher:', err)
    error.value = err.response?.data?.message || 'Failed to save teacher'
  } finally {
    loading.value = false
  }
}
</script>