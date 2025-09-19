<template>
  <form @submit.prevent="handleSubmit" class="space-y-4">
    <!-- Basic Information -->
    <div>
      <label class="block text-sm font-medium mb-1">Section Number</label>
      <Input
        v-model="formData.sectionNumber"
        placeholder="e.g., 001, 002"
        :error="errors.sectionNumber"
        required
      />
      <p v-if="errors.sectionNumber" class="text-red-500 text-sm mt-1">{{ errors.sectionNumber }}</p>
    </div>

    <div>
      <label class="block text-sm font-medium mb-1">Semester</label>
      <SelectInput
        v-model="formData.semester"
        :options="semesterOptions"
        placeholder="Select semester"
        :error="errors.semester"
        required
      />
      <p v-if="errors.semester" class="text-red-500 text-sm mt-1">{{ errors.semester }}</p>
    </div>

    <div>
      <label class="block text-sm font-medium mb-1">Academic Year</label>
      <Input
        v-model="formData.academicYear"
        placeholder="e.g., 2024-2025"
        :error="errors.academicYear"
        required
      />
      <p v-if="errors.academicYear" class="text-red-500 text-sm mt-1">{{ errors.academicYear }}</p>
    </div>

    <!-- Schedule Information -->
    <div class="border-t pt-4">
      <h4 class="font-medium mb-3">Schedule Information</h4>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">Days</label>
          <div class="grid grid-cols-3 gap-2">
            <label
              v-for="day in daysOfWeek"
              :key="day.value"
              class="flex items-center space-x-2"
            >
              <input
                type="checkbox"
                :value="day.value"
                v-model="formData.days"
                class="rounded"
              />
              <span class="text-sm">{{ day.label }}</span>
            </label>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium mb-1">Time</label>
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="text-xs text-muted-foreground">Start Time</label>
              <Input
                v-model="formData.startTime"
                type="time"
                :error="errors.startTime"
              />
            </div>
            <div>
              <label class="text-xs text-muted-foreground">End Time</label>
              <Input
                v-model="formData.endTime"
                type="time"
                :error="errors.endTime"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Resources -->
    <div class="border-t pt-4">
      <h4 class="font-medium mb-3">Resources</h4>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">Instructor</label>
          <SelectInput
            v-model="formData.instructorId"
            :options="teacherOptions"
            placeholder="Select instructor"
            :error="errors.instructorId"
            required
          />
          <p v-if="errors.instructorId" class="text-red-500 text-sm mt-1">{{ errors.instructorId }}</p>
        </div>

        <div>
          <label class="block text-sm font-medium mb-1">Classroom</label>
          <SelectInput
            v-model="formData.classroomId"
            :options="classroomOptions"
            placeholder="Select classroom"
            :error="errors.classroomId"
            required
          />
          <p v-if="errors.classroomId" class="text-red-500 text-sm mt-1">{{ errors.classroomId }}</p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">Capacity</label>
          <Input
            v-model.number="formData.capacity"
            type="number"
            min="1"
            max="500"
            :error="errors.capacity"
            required
          />
          <p v-if="errors.capacity" class="text-red-500 text-sm mt-1">{{ errors.capacity }}</p>
        </div>

        <div>
          <label class="block text-sm font-medium mb-1">Waitlist Limit</label>
          <Input
            v-model.number="formData.waitlistLimit"
            type="number"
            min="0"
            max="100"
            placeholder="0"
          />
        </div>
      </div>
    </div>

    <!-- Additional Settings -->
    <div class="border-t pt-4">
      <h4 class="font-medium mb-3">Additional Settings</h4>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium mb-1">Delivery Method</label>
          <SelectInput
            v-model="formData.deliveryMethod"
            :options="deliveryMethodOptions"
            placeholder="Select delivery method"
          />
        </div>

        <div>
          <label class="block text-sm font-medium mb-1">Instruction Mode</label>
          <SelectInput
            v-model="formData.instructionMode"
            :options="instructionModeOptions"
            placeholder="Select instruction mode"
          />
        </div>
      </div>

      <div class="space-y-2">
        <div class="flex items-center space-x-2">
          <input
            type="checkbox"
            id="syncOnline"
            v-model="formData.syncOnline"
            class="rounded"
          />
          <label for="syncOnline" class="text-sm font-medium">
            Synchronous online component
          </label>
        </div>

        <div class="flex items-center space-x-2">
          <input
            type="checkbox"
            id="requiresLab"
            v-model="formData.requiresLab"
            class="rounded"
          />
          <label for="requiresLab" class="text-sm font-medium">
            Requires laboratory session
          </label>
        </div>
      </div>
    </div>

    <!-- Notes -->
    <div>
      <label class="block text-sm font-medium mb-1">Notes (Optional)</label>
      <textarea
        v-model="formData.notes"
        class="w-full p-2 border rounded-md text-sm"
        rows="3"
        placeholder="Add any additional notes or special requirements..."
      ></textarea>
    </div>

    <!-- Conflict Warning -->
    <div v-if="conflictWarning" class="p-3 bg-yellow-50 border-yellow-200 rounded-lg">
      <div class="flex items-center space-x-2">
        <AlertTriangle class="w-5 h-5 text-yellow-600" />
        <span class="font-medium text-yellow-800">Scheduling Conflict Detected</span>
      </div>
      <p class="text-sm text-yellow-700 mt-1">
        {{ conflictWarning }}
      </p>
    </div>

    <!-- Actions -->
    <div class="flex justify-end space-x-2 pt-4">
      <Button type="button" variant="outline" @click="$emit('cancel')">
        Cancel
      </Button>
      <Button
        type="submit"
        :disabled="isSubmitting || !!conflictWarning"
      >
        {{ isSubmitting ? 'Creating...' : 'Create Section' }}
      </Button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import SelectInput from '@/components/ui/SelectInput.vue'
import { useToast } from 'vue-toastification'
import { AlertTriangle } from 'lucide-vue-next'

interface Props {
  course: any
  teachers: any[]
  classrooms: any[]
}

interface Emits {
  (e: 'submit', data: any): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const toast = useToast()

// State
const isSubmitting = ref(false)

const formData = reactive({
  sectionNumber: '',
  semester: '',
  academicYear: '',
  days: [],
  startTime: '',
  endTime: '',
  instructorId: '',
  classroomId: '',
  capacity: 30,
  waitlistLimit: 0,
  deliveryMethod: 'IN_PERSON',
  instructionMode: 'FACE_TO_FACE',
  syncOnline: false,
  requiresLab: false,
  notes: ''
})

const errors = reactive<Record<string, string>>({})

// Options
const semesterOptions = [
  { value: 'FALL', label: 'Fall Semester' },
  { value: 'SPRING', label: 'Spring Semester' },
  { value: 'SUMMER', label: 'Summer Session' },
  { value: 'WINTER', label: 'Winter Session' }
]

const daysOfWeek = [
  { value: 'MONDAY', label: 'Mon' },
  { value: 'TUESDAY', label: 'Tue' },
  { value: 'WEDNESDAY', label: 'Wed' },
  { value: 'THURSDAY', label: 'Thu' },
  { value: 'FRIDAY', label: 'Fri' },
  { value: 'SATURDAY', label: 'Sat' },
  { value: 'SUNDAY', label: 'Sun' }
]

const deliveryMethodOptions = [
  { value: 'IN_PERSON', label: 'In Person' },
  { value: 'ONLINE', label: 'Online' },
  { value: 'HYBRID', label: 'Hybrid' },
  { value: 'BLENDED', label: 'Blended' }
]

const instructionModeOptions = [
  { value: 'FACE_TO_FACE', label: 'Face to Face' },
  { value: 'ONLINE_SYNCHRONOUS', label: 'Online Synchronous' },
  { value: 'ONLINE_ASYNCHRONOUS', label: 'Online Asynchronous' },
  { value: 'HYBRID_FLEXIBLE', label: 'Hybrid Flexible' }
]

// Computed
const teacherOptions = computed(() =>
  props.teachers.map(teacher => ({
    value: teacher.id.toString(),
    label: `${teacher.firstName} ${teacher.lastName} (${teacher.department})`
  }))
)

const classroomOptions = computed(() =>
  props.classrooms.map(classroom => ({
    value: classroom.id.toString(),
    label: `${classroom.building} ${classroom.roomNumber} (Capacity: ${classroom.capacity})`
  }))
)

const conflictWarning = computed(() => {
  // Mock conflict detection
  if (formData.instructorId && formData.startTime && formData.endTime && formData.days.length > 0) {
    return 'Instructor has another class scheduled at this time'
  }
  return null
})

// Methods
const validateForm = (): boolean => {
  errors.sectionNumber = formData.sectionNumber ? '' : 'Section number is required'
  errors.semester = formData.semester ? '' : 'Semester is required'
  errors.academicYear = formData.academicYear ? '' : 'Academic year is required'
  errors.instructorId = formData.instructorId ? '' : 'Instructor is required'
  errors.classroomId = formData.classroomId ? '' : 'Classroom is required'
  errors.capacity = formData.capacity && formData.capacity > 0 ? '' : 'Capacity must be greater than 0'

  // Validate time range
  if (formData.startTime && formData.endTime) {
    if (formData.startTime >= formData.endTime) {
      errors.startTime = 'Start time must be before end time'
      errors.endTime = 'End time must be after start time'
    }
  }

  // Validate days selection
  if (formData.days.length === 0) {
    errors.startTime = 'At least one day must be selected'
  }

  return Object.values(errors).every(error => !error)
}

const handleSubmit = async () => {
  if (!validateForm()) {
    toast.error('Please fix the errors in the form')
    return
  }

  if (conflictWarning.value) {
    toast.error('Please resolve scheduling conflicts before creating section')
    return
  }

  try {
    isSubmitting.value = true
    const sectionData = {
      ...formData,
      courseId: props.course.id
    }

    emit('submit', sectionData)
  } catch (error) {
    toast.error('Failed to create section')
  } finally {
    isSubmitting.value = false
  }
}

// Watch for form changes to clear conflicts
watch([() => formData.instructorId, () => formData.startTime, () => formData.endTime, () => formData.days], () => {
  // Clear time-related errors when values change
  if (formData.startTime && formData.endTime && formData.startTime < formData.endTime) {
    errors.startTime = ''
    errors.endTime = ''
  }
  if (formData.days.length > 0) {
    errors.startTime = ''
  }
})
</script>