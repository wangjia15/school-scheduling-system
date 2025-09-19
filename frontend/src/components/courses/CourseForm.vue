<template>
  <Card>
    <CardHeader>
      <CardTitle>{{ isEditing ? 'Edit Course' : 'Create New Course' }}</CardTitle>
      <CardDescription>
        {{ isEditing ? 'Update course information' : 'Add a new course to the catalog' }}
      </CardDescription>
    </CardHeader>
    <CardContent>
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium mb-1">Course Code</label>
            <Input
              v-model="formData.courseCode"
              placeholder="e.g., CS101"
              :error="errors.courseCode"
              required
            />
            <p v-if="errors.courseCode" class="text-red-500 text-sm mt-1">{{ errors.courseCode }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Title</label>
            <Input
              v-model="formData.title"
              placeholder="Course title"
              :error="errors.title"
              required
            />
            <p v-if="errors.title" class="text-red-500 text-sm mt-1">{{ errors.title }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Department</label>
            <SelectInput
              v-model="formData.departmentId"
              :options="departmentOptions"
              placeholder="Select department"
              :error="errors.departmentId"
              required
            />
            <p v-if="errors.departmentId" class="text-red-500 text-sm mt-1">{{ errors.departmentId }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Level</label>
            <SelectInput
              v-model="formData.level"
              :options="levelOptions"
              placeholder="Select level"
              :error="errors.level"
              required
            />
            <p v-if="errors.level" class="text-red-500 text-sm mt-1">{{ errors.level }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Credits</label>
            <Input
              v-model.number="formData.credits"
              type="number"
              min="1"
              max="6"
              :error="errors.credits"
              required
            />
            <p v-if="errors.credits" class="text-red-500 text-sm mt-1">{{ errors.credits }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Contact Hours/Week</label>
            <Input
              v-model.number="formData.contactHoursPerWeek"
              type="number"
              step="0.5"
              min="0.5"
              max="20"
              :error="errors.contactHoursPerWeek"
              required
            />
            <p v-if="errors.contactHoursPerWeek" class="text-red-500 text-sm mt-1">{{ errors.contactHoursPerWeek }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Theory Hours</label>
            <Input
              v-model.number="formData.theoryHours"
              type="number"
              step="0.5"
              min="0"
              max="20"
              :error="errors.theoryHours"
              required
            />
            <p v-if="errors.theoryHours" class="text-red-500 text-sm mt-1">{{ errors.theoryHours }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Lab Hours</label>
            <Input
              v-model.number="formData.labHours"
              type="number"
              step="0.5"
              min="0"
              max="20"
              :error="errors.labHours"
              required
            />
            <p v-if="errors.labHours" class="text-red-500 text-sm mt-1">{{ errors.labHours }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Min Students</label>
            <Input
              v-model.number="formData.minStudents"
              type="number"
              min="1"
              max="500"
              :error="errors.minStudents"
              required
            />
            <p v-if="errors.minStudents" class="text-red-500 text-sm mt-1">{{ errors.minStudents }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Max Students</label>
            <Input
              v-model.number="formData.maxStudents"
              type="number"
              min="1"
              max="500"
              :error="errors.maxStudents"
              required
            />
            <p v-if="errors.maxStudents" class="text-red-500 text-sm mt-1">{{ errors.maxStudents }}</p>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium mb-1">Description</label>
          <textarea
            v-model="formData.description"
            class="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
            placeholder="Course description"
            rows="3"
          ></textarea>
        </div>

        <div class="flex items-center space-x-2">
          <input
            type="checkbox"
            id="requiresLab"
            v-model="formData.requiresLab"
            class="rounded border-gray-300"
          />
          <label for="requiresLab" class="text-sm font-medium">Requires Laboratory</label>
        </div>

        <div class="flex justify-end space-x-2 pt-4">
          <Button type="button" variant="outline" @click="$emit('cancel')">
            Cancel
          </Button>
          <Button type="submit" :disabled="isSubmitting">
            {{ isSubmitting ? 'Saving...' : (isEditing ? 'Update' : 'Create') }}
          </Button>
        </div>
      </form>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import SelectInput from '@/components/ui/SelectInput.vue'
import { useToast } from 'vue-toastification'
import type { CourseRequest, Course } from '@/types/course'
import { courseService } from '@/services/courseService'

interface Props {
  course?: Course
  departments: Array<{ id: number; code: string; name: string }>
}

interface Emits {
  (e: 'submit', course: CourseRequest): void
  (e: 'cancel'): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const toast = useToast()
const isSubmitting = ref(false)
const isEditing = computed(() => props.course !== undefined)

const formData = reactive<CourseRequest>({
  courseCode: '',
  title: '',
  description: '',
  departmentId: 0,
  credits: 3,
  contactHoursPerWeek: 3,
  theoryHours: 3,
  labHours: 0,
  level: 'UNDERGRADUATE',
  maxStudents: 30,
  minStudents: 5,
  requiresLab: false,
  prerequisites: []
})

const errors = reactive<Record<string, string>>({})

const departmentOptions = computed(() =>
  props.departments.map(dept => ({
    value: dept.id,
    label: `${dept.code} - ${dept.name}`
  }))
)

const levelOptions = [
  { value: 'UNDERGRADUATE', label: 'Undergraduate' },
  { value: 'GRADUATE', label: 'Graduate' },
  { value: 'PHD', label: 'PhD' }
]

const validateForm = (): boolean => {
  errors.courseCode = formData.courseCode ? '' : 'Course code is required'
  errors.title = formData.title ? '' : 'Title is required'
  errors.departmentId = formData.departmentId ? '' : 'Department is required'
  errors.level = formData.level ? '' : 'Level is required'
  errors.credits = formData.credits && formData.credits >= 1 && formData.credits <= 6 ? '' : 'Credits must be between 1 and 6'
  errors.contactHoursPerWeek = formData.contactHoursPerWeek && formData.contactHoursPerWeek >= 0.5 && formData.contactHoursPerWeek <= 20 ? '' : 'Contact hours must be between 0.5 and 20'
  errors.theoryHours = formData.theoryHours !== undefined && formData.theoryHours >= 0 && formData.theoryHours <= 20 ? '' : 'Theory hours must be between 0 and 20'
  errors.labHours = formData.labHours !== undefined && formData.labHours >= 0 && formData.labHours <= 20 ? '' : 'Lab hours must be between 0 and 20'
  errors.minStudents = formData.minStudents && formData.minStudents >= 1 && formData.minStudents <= 500 ? '' : 'Min students must be between 1 and 500'
  errors.maxStudents = formData.maxStudents && formData.maxStudents >= 1 && formData.maxStudents <= 500 ? '' : 'Max students must be between 1 and 500'

  // Validate hours distribution
  if (formData.theoryHours !== undefined && formData.labHours !== undefined && formData.contactHoursPerWeek !== undefined) {
    const totalHours = formData.theoryHours + formData.labHours
    if (Math.abs(totalHours - formData.contactHoursPerWeek) > 0.1) {
      errors.contactHoursPerWeek = 'Contact hours must equal theory + lab hours'
    }
  }

  // Validate enrollment range
  if (formData.minStudents && formData.maxStudents && formData.minStudents > formData.maxStudents) {
    errors.minStudents = 'Min students cannot exceed max students'
    errors.maxStudents = 'Max students cannot be less than min students'
  }

  return Object.values(errors).every(error => !error)
}

const handleSubmit = async () => {
  if (!validateForm()) {
    toast.error('Please fix the errors in the form')
    return
  }

  try {
    isSubmitting.value = true
    emit('submit', { ...formData })
  } catch (error) {
    toast.error('Failed to save course')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  if (props.course) {
    Object.assign(formData, {
      courseCode: props.course.courseCode,
      title: props.course.title,
      description: props.course.description || '',
      departmentId: props.course.department.id,
      credits: props.course.credits,
      contactHoursPerWeek: props.course.contactHoursPerWeek,
      theoryHours: props.course.theoryHours,
      labHours: props.course.labHours,
      level: props.course.level,
      maxStudents: props.course.maxStudents,
      minStudents: props.course.minStudents,
      requiresLab: props.course.requiresLab,
      prerequisites: props.course.prerequisites?.map(prereq => ({
        prerequisiteCourseId: prereq.prerequisiteCourse.id,
        isMandatory: prereq.isMandatory,
        minimumGrade: prereq.minimumGrade,
        notes: prereq.notes
      })) || []
    })
  }
})
</script>