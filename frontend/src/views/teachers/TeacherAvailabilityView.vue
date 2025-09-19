<template>
  <div class="p-6 space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Teacher Availability</h1>
        <p class="text-gray-600 mt-2">
          {{ teacher ? `${teacher.user.firstName} ${teacher.user.lastName}` : '' }} - Manage weekly availability and preferences
        </p>
      </div>
      <div class="flex items-center gap-2">
        <Button variant="outline" @click="goBack">
          <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back to Teachers
        </Button>
        <Button @click="showAddModal = true" class="flex items-center gap-2">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Add Availability
        </Button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
    </div>

    <!-- Content -->
    <div v-else-if="teacher" class="space-y-6">
      <!-- Teacher Info Card -->
      <Card>
        <CardContent class="p-6">
          <div class="flex items-center space-x-4">
            <div class="h-16 w-16 rounded-full bg-blue-100 flex items-center justify-center">
              <span class="text-blue-800 text-xl font-bold">
                {{ teacher.user.firstName.charAt(0) }}{{ teacher.user.lastName.charAt(0) }}
              </span>
            </div>
            <div>
              <h2 class="text-xl font-semibold text-gray-900">
                {{ teacher.user.firstName }} {{ teacher.user.lastName }}
              </h2>
              <p class="text-gray-600">{{ teacher.title }} • {{ teacher.department.name }}</p>
              <div class="flex items-center gap-4 mt-2 text-sm text-gray-500">
                <span>{{ teacher.employeeId }}</span>
                <span>•</span>
                <span>{{ teacher.user.email }}</span>
                <span>•</span>
                <span>{{ teacher.maxWeeklyHours }}h/week</span>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Weekly Calendar -->
      <Card>
        <CardHeader>
          <CardTitle>Weekly Availability Schedule</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="grid grid-cols-1 lg:grid-cols-7 gap-4">
            <div v-for="day in weekDays" :key="day.value" class="border rounded-lg p-4">
              <h3 class="font-semibold text-gray-900 mb-3">{{ day.label }}</h3>
              <div class="space-y-2">
                <div v-for="slot in getAvailabilityForDay(day.value)"
                     :key="slot.id"
                     :class="getAvailabilityClass(slot.availabilityType)"
                     class="rounded p-3 text-sm">
                  <div class="flex justify-between items-start">
                    <div>
                      <div class="font-medium">{{ slot.startTime }} - {{ slot.endTime }}</div>
                      <div class="text-xs opacity-75 capitalize">{{ slot.availabilityType.toLowerCase() }}</div>
                    </div>
                    <div class="flex items-center gap-1">
                      <Button variant="ghost" size="sm" @click="editAvailability(slot)">
                        <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                        </svg>
                      </Button>
                      <Button variant="ghost" size="sm" @click="deleteAvailability(slot)">
                        <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </Button>
                    </div>
                  </div>
                  <div v-if="slot.notes" class="text-xs mt-1 opacity-75">
                    {{ slot.notes }}
                  </div>
                </div>
                <div v-if="getAvailabilityForDay(day.value).length === 0" class="text-gray-400 text-sm text-center py-4">
                  No availability set
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Availability Summary -->
      <Card>
        <CardHeader>
          <CardTitle>Availability Summary</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div class="text-center p-4 bg-green-50 rounded-lg">
              <div class="text-2xl font-bold text-green-700">{{ getTotalHours() }}h</div>
              <div class="text-sm text-green-600">Total Available Hours</div>
            </div>
            <div class="text-center p-4 bg-blue-50 rounded-lg">
              <div class="text-2xl font-bold text-blue-700">{{ getPreferredHours() }}h</div>
              <div class="text-sm text-blue-600">Preferred Hours</div>
            </div>
            <div class="text-center p-4 bg-purple-50 rounded-lg">
              <div class="text-2xl font-bold text-purple-700">{{ getAvailableDays() }}</div>
              <div class="text-sm text-purple-600">Days Available</div>
            </div>
            <div class="text-center p-4 bg-orange-50 rounded-lg">
              <div class="text-2xl font-bold text-orange-700">{{ teacher.maxWeeklyHours }}h</div>
              <div class="text-sm text-orange-600">Max Weekly Hours</div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Add Availability Modal -->
    <AvailabilityFormModal
      v-model:open="showAddModal"
      :teacher-id="teacherId"
      :existing-availability="availability"
      @close="closeModals"
      @save="handleAvailabilitySave"
    />

    <!-- Edit Availability Modal -->
    <AvailabilityFormModal
      v-model:open="showEditModal"
      :teacher-id="teacherId"
      :availability="selectedAvailability"
      :existing-availability="availability"
      @close="closeModals"
      @save="handleAvailabilitySave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Card, { CardHeader, CardTitle, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import teacherService, { type Teacher, type TeacherAvailability } from '@/services/teacherService'
import AvailabilityFormModal from '@/components/teachers/AvailabilityFormModal.vue'

const route = useRoute()
const router = useRouter()

const teacherId = computed(() => parseInt(route.params.id as string))

// State
const loading = ref(false)
const showAddModal = ref(false)
const showEditModal = ref(false)
const teacher = ref<Teacher | null>(null)
const availability = ref<TeacherAvailability[]>([])
const selectedAvailability = ref<TeacherAvailability | null>(null)

const weekDays = [
  { value: 'MONDAY', label: 'Monday' },
  { value: 'TUESDAY', label: 'Tuesday' },
  { value: 'WEDNESDAY', label: 'Wednesday' },
  { value: 'THURSDAY', label: 'Thursday' },
  { value: 'FRIDAY', label: 'Friday' },
  { value: 'SATURDAY', label: 'Saturday' },
  { value: 'SUNDAY', label: 'Sunday' }
]

// Methods
const loadTeacher = async () => {
  try {
    teacher.value = await teacherService.getTeacherById(teacherId.value)
  } catch (error) {
    console.error('Failed to load teacher:', error)
    router.push('/teachers')
  }
}

const loadAvailability = async () => {
  if (!teacherId.value) return

  try {
    availability.value = await teacherService.getTeacherAvailability(teacherId.value)
  } catch (error) {
    console.error('Failed to load availability:', error)
  }
}

const getAvailabilityForDay = (dayOfWeek: string) => {
  return availability.value.filter(slot => slot.dayOfWeek === dayOfWeek)
}

const getAvailabilityClass = (type: string) => {
  switch (type) {
    case 'PREFERRED':
      return 'bg-green-100 text-green-800 border-green-200'
    case 'AVAILABLE':
      return 'bg-blue-100 text-blue-800 border-blue-200'
    case 'UNAVAILABLE':
      return 'bg-red-100 text-red-800 border-red-200'
    case 'RESTRICTED':
      return 'bg-yellow-100 text-yellow-800 border-yellow-200'
    default:
      return 'bg-gray-100 text-gray-800 border-gray-200'
  }
}

const getTotalHours = () => {
  return availability.value.reduce((total, slot) => {
    if (slot.availabilityType === 'PREFERRED' || slot.availabilityType === 'AVAILABLE') {
      const start = new Date(`2024-01-01T${slot.startTime}`)
      const end = new Date(`2024-01-01T${slot.endTime}`)
      const hours = (end.getTime() - start.getTime()) / (1000 * 60 * 60)
      return total + hours
    }
    return total
  }, 0)
}

const getPreferredHours = () => {
  return availability.value.reduce((total, slot) => {
    if (slot.availabilityType === 'PREFERRED') {
      const start = new Date(`2024-01-01T${slot.startTime}`)
      const end = new Date(`2024-01-01T${slot.endTime}`)
      const hours = (end.getTime() - start.getTime()) / (1000 * 60 * 60)
      return total + hours
    }
    return total
  }, 0)
}

const getAvailableDays = () => {
  const availableDays = new Set(
    availability.value
      .filter(slot => slot.availabilityType === 'PREFERRED' || slot.availabilityType === 'AVAILABLE')
      .map(slot => slot.dayOfWeek)
  )
  return availableDays.size
}

const editAvailability = (slot: TeacherAvailability) => {
  selectedAvailability.value = slot
  showEditModal.value = true
}

const deleteAvailability = async (slot: TeacherAvailability) => {
  if (!confirm('Are you sure you want to delete this availability slot?')) {
    return
  }

  try {
    await teacherService.deleteTeacherAvailability(teacherId.value, slot.id)
    await loadAvailability()
  } catch (error) {
    console.error('Failed to delete availability:', error)
  }
}

const closeModals = () => {
  showAddModal.value = false
  showEditModal.value = false
  selectedAvailability.value = null
}

const handleAvailabilitySave = () => {
  closeModals()
  loadAvailability()
}

const goBack = () => {
  router.push('/teachers')
}

// Lifecycle
onMounted(async () => {
  loading.value = true
  try {
    await loadTeacher()
    await loadAvailability()
  } finally {
    loading.value = false
  }
})
</script>