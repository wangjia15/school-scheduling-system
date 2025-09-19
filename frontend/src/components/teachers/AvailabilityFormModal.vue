<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
      <div class="px-6 py-4 border-b border-gray-200">
        <h3 class="text-lg font-medium text-gray-900">
          {{ availability ? 'Edit Availability' : 'Add Availability' }}
        </h3>
      </div>

      <form @submit.prevent="handleSubmit" class="px-6 py-4 space-y-4">
        <!-- Day of Week -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Day of Week</label>
          <Select v-model="form.dayOfWeek" required>
            <option value="">Select a day</option>
            <option value="MONDAY">Monday</option>
            <option value="TUESDAY">Tuesday</option>
            <option value="WEDNESDAY">Wednesday</option>
            <option value="THURSDAY">Thursday</option>
            <option value="FRIDAY">Friday</option>
            <option value="SATURDAY">Saturday</option>
            <option value="SUNDAY">Sunday</option>
          </Select>
        </div>

        <!-- Time Range -->
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Start Time</label>
            <Input
              v-model="form.startTime"
              type="time"
              required
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">End Time</label>
            <Input
              v-model="form.endTime"
              type="time"
              required
            />
          </div>
        </div>

        <!-- Availability Type -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Availability Type</label>
          <Select v-model="form.availabilityType" required>
            <option value="">Select type</option>
            <option value="PREFERRED">Preferred</option>
            <option value="AVAILABLE">Available</option>
            <option value="UNAVAILABLE">Unavailable</option>
            <option value="RESTRICTED">Restricted</option>
          </Select>
        </div>

        <!-- Max Classes -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Maximum Classes</label>
          <Input
            v-model.number="form.maxClasses"
            type="number"
            min="1"
            max="10"
            required
          />
        </div>

        <!-- Break Duration -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Break Duration (hours)</label>
          <Input
            v-model.number="form.breakDuration"
            type="number"
            step="0.5"
            min="0"
            max="4"
          />
        </div>

        <!-- Break Between Classes -->
        <div class="flex items-center">
          <input
            v-model="form.requiresBreakBetweenClasses"
            type="checkbox"
            class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
          />
          <label class="ml-2 block text-sm text-gray-700">
            Require break between classes
          </label>
        </div>

        <!-- Recurring -->
        <div class="flex items-center">
          <input
            v-model="form.isRecurring"
            type="checkbox"
            class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
          />
          <label class="ml-2 block text-sm text-gray-700">
            Recurring weekly
          </label>
        </div>

        <!-- Notes -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Notes</label>
          <textarea
            v-model="form.notes"
            rows="3"
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            placeholder="Optional notes about this availability..."
          />
        </div>

        <!-- Error Message -->
        <div v-if="error" class="text-red-600 text-sm">{{ error }}</div>

        <!-- Actions -->
        <div class="flex justify-end gap-3 pt-4">
          <Button type="button" variant="outline" @click="$emit('close')">
            Cancel
          </Button>
          <Button type="submit" :disabled="loading">
            {{ loading ? 'Saving...' : (availability ? 'Update' : 'Add') }}
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
import teacherService, { type TeacherAvailability, type TeacherAvailabilityRequest } from '@/services/teacherService'

interface Props {
  teacherId: number
  availability?: TeacherAvailability | null
  existingAvailability: TeacherAvailability[]
}

interface Emits {
  (e: 'close'): void
  (e: 'save'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const error = ref('')

const form = reactive<TeacherAvailabilityRequest>({
  dayOfWeek: 'MONDAY',
  startTime: '',
  endTime: '',
  availabilityType: 'PREFERRED',
  maxClasses: 1,
  breakDuration: 0,
  requiresBreakBetweenClasses: false,
  isRecurring: true,
  notes: ''
})

// Initialize form when editing
watch(() => props.availability, (newAvailability) => {
  if (newAvailability) {
    form.dayOfWeek = newAvailability.dayOfWeek
    form.startTime = newAvailability.startTime
    form.endTime = newAvailability.endTime
    form.availabilityType = newAvailability.availabilityType
    form.maxClasses = newAvailability.maxClasses
    form.breakDuration = newAvailability.breakDuration
    form.requiresBreakBetweenClasses = newAvailability.requiresBreakBetweenClasses
    form.isRecurring = newAvailability.isRecurring
    form.notes = newAvailability.notes || ''
  } else {
    resetForm()
  }
}, { immediate: true })

const resetForm = () => {
  form.dayOfWeek = 'MONDAY'
  form.startTime = ''
  form.endTime = ''
  form.availabilityType = 'PREFERRED'
  form.maxClasses = 1
  form.breakDuration = 0
  form.requiresBreakBetweenClasses = false
  form.isRecurring = true
  form.notes = ''
  error.value = ''
}

const validateForm = () => {
  if (!form.startTime || !form.endTime) {
    error.value = 'Start and end times are required'
    return false
  }

  if (form.startTime >= form.endTime) {
    error.value = 'End time must be after start time'
    return false
  }

  // Check for overlapping availability (excluding current availability when editing)
  const existingForDay = props.existingAvailability.filter(slot =>
    slot.dayOfWeek === form.dayOfWeek &&
    (!props.availability || slot.id !== props.availability.id)
  )

  const start = new Date(`2024-01-01T${form.startTime}`)
  const end = new Date(`2024-01-01T${form.endTime}`)

  const hasOverlap = existingForDay.some(slot => {
    const slotStart = new Date(`2024-01-01T${slot.startTime}`)
    const slotEnd = new Date(`2024-01-01T${slot.endTime}`)

    return (start < slotEnd && end > slotStart)
  })

  if (hasOverlap) {
    error.value = 'This time slot overlaps with existing availability'
    return false
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
    if (props.availability) {
      await teacherService.updateTeacherAvailability(
        props.teacherId,
        props.availability.id,
        form
      )
    } else {
      await teacherService.addTeacherAvailability(props.teacherId, form)
    }

    emit('save')
  } catch (err: any) {
    console.error('Failed to save availability:', err)
    error.value = err.response?.data?.message || 'Failed to save availability'
  } finally {
    loading.value = false
  }
}
</script>