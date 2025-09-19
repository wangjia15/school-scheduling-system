<template>
  <div class="space-y-6">
    <!-- Enrollment Statistics -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
              <Users class="w-4 h-4 text-blue-600" />
            </div>
            <div>
              <div class="text-2xl font-bold">{{ statistics.totalEnrolled }}</div>
              <div class="text-xs text-muted-foreground">Total Enrolled</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
              <UserCheck class="w-4 h-4 text-green-600" />
            </div>
            <div>
              <div class="text-2xl font-bold">{{ statistics.activeEnrollments }}</div>
              <div class="text-xs text-muted-foreground">Active Enrollments</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
              <Clock class="w-4 h-4 text-yellow-600" />
            </div>
            <div>
              <div class="text-2xl font-bold">{{ statistics.waitlisted }}</div>
              <div class="text-xs text-muted-foreground">Waitlisted</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-purple-100 rounded-full flex items-center justify-center">
              <TrendingUp class="w-4 h-4 text-purple-600" />
            </div>
            <div>
              <div class="text-2xl font-bold">{{ statistics.fillRate }}%</div>
              <div class="text-xs text-muted-foreground">Fill Rate</div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Tabs for different views -->
    <div class="border-b">
      <nav class="flex space-x-8">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="activeTab = tab.id"
          :class="[
            'py-2 px-1 border-b-2 font-medium text-sm',
            activeTab === tab.id
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground hover:border-muted-foreground'
          ]"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <!-- Tab Content -->
    <div v-if="activeTab === 'overview'">
      <EnrollmentOverview
        :course="course"
        :enrollments="enrollments"
        :statistics="statistics"
        @enroll-student="handleEnrollStudent"
        @drop-student="handleDropStudent"
        @waitlist-student="handleWaitlistStudent"
      />
    </div>

    <div v-else-if="activeTab === 'roster'">
      <EnrollmentRoster
        :course="course"
        :enrollments="enrollments"
        :waitlist="waitlist"
        @promote-from-waitlist="handlePromoteFromWaitlist"
        @remove-enrollment="handleRemoveEnrollment"
      />
    </div>

    <div v-else-if="activeTab === 'capacity'">
      <CapacityManagement
        :course="course"
        :enrollments="enrollments"
        :waitlist="waitlist"
        @update-capacity="handleUpdateCapacity"
        @manage-waitlist="handleManageWaitlist"
      />
    </div>

    <div v-else-if="activeTab === 'analytics'">
      <EnrollmentAnalytics
        :course="course"
        :enrollments="enrollments"
        :waitlist="waitlist"
      />
    </div>

    <!-- Enroll Student Modal -->
    <div v-if="showEnrollModal" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <Card class="w-full max-w-2xl">
        <CardHeader>
          <CardTitle>Enroll Student</CardTitle>
          <CardDescription>
            Add a student to {{ course.courseCode }} - {{ course.title }}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <EnrollmentForm
            :course="course"
            :available-slots="availableSlots"
            @submit="handleEnrollSubmit"
            @cancel="showEnrollModal = false"
          />
        </CardContent>
      </Card>
    </div>

    <!-- Waitlist Management Modal -->
    <div v-if="showWaitlistModal" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <Card class="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <CardHeader>
          <CardTitle>Waitlist Management</CardTitle>
          <CardDescription>
            Manage waitlist for {{ course.courseCode }}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <WaitlistManagement
            :course="course"
            :waitlist="waitlist"
            :available-slots="availableSlots"
            @promote-student="handlePromoteStudent"
            @remove-from-waitlist="handleRemoveFromWaitlist"
            @close="showWaitlistModal = false"
          />
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import EnrollmentOverview from './EnrollmentOverview.vue'
import EnrollmentRoster from './EnrollmentRoster.vue'
import CapacityManagement from './CapacityManagement.vue'
import EnrollmentAnalytics from './EnrollmentAnalytics.vue'
import EnrollmentForm from './EnrollmentForm.vue'
import WaitlistManagement from './WaitlistManagement.vue'
import { useToast } from 'vue-toastification'
import type { Course, Enrollment } from '@/types/course'
import { Users, UserCheck, Clock, TrendingUp } from 'lucide-vue-next'

interface Props {
  course: Course
}

interface Emits {
  (e: 'enrollment-updated', course: Course): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const toast = useToast()

// State
const activeTab = ref('overview')
const showEnrollModal = ref(false)
const showWaitlistModal = ref(false)
const enrollments = ref<Enrollment[]>([])
const waitlist = ref<Enrollment[]>([])

// Statistics
const statistics = reactive({
  totalEnrolled: 0,
  activeEnrollments: 0,
  waitlisted: 0,
  fillRate: 0
})

// Tabs
const tabs = [
  { id: 'overview', label: 'Overview' },
  { id: 'roster', label: 'Roster' },
  { id: 'capacity', label: 'Capacity' },
  { id: 'analytics', label: 'Analytics' }
]

// Computed
const availableSlots = computed(() => {
  const currentEnrollments = enrollments.value.filter(e => e.status === 'ACTIVE').length
  return Math.max(0, props.course.maxStudents - currentEnrollments)
})

// Methods
const loadEnrollments = async () => {
  try {
    // Mock data - in real implementation, this would call an API
    enrollments.value = []
    waitlist.value = []
    updateStatistics()
  } catch (error) {
    toast.error('Failed to load enrollments')
  }
}

const updateStatistics = () => {
  const activeEnrollments = enrollments.value.filter(e => e.status === 'ACTIVE').length
  statistics.totalEnrolled = enrollments.value.length
  statistics.activeEnrollments = activeEnrollments
  statistics.waitlisted = waitlist.value.length
  statistics.fillRate = props.course.maxStudents > 0
    ? Math.round((activeEnrollments / props.course.maxStudents) * 100)
    : 0
}

const handleEnrollStudent = () => {
  if (availableSlots.value === 0) {
    toast.warning('Course is full. Student will be added to waitlist.')
  }
  showEnrollModal.value = true
}

const handleDropStudent = (enrollment: Enrollment) => {
  // Handle dropping a student
  toast.info(`Dropping student from ${props.course.courseCode}`)
}

const handleWaitlistStudent = () => {
  showWaitlistModal.value = true
}

const handleEnrollSubmit = async (enrollmentData: any) => {
  try {
    // Mock API call
    const newEnrollment: Enrollment = {
      id: Date.now(),
      courseId: props.course.id,
      studentId: enrollmentData.studentId,
      studentName: enrollmentData.studentName,
      status: availableSlots.value > 0 ? 'ACTIVE' : 'WAITLISTED',
      enrolledAt: new Date().toISOString(),
      grade: undefined
    }

    if (newEnrollment.status === 'ACTIVE') {
      enrollments.value.push(newEnrollment)
    } else {
      waitlist.value.push(newEnrollment)
    }

    showEnrollModal.value = false
    toast.success('Student enrolled successfully')
    updateStatistics()
    emit('enrollment-updated', props.course)
  } catch (error) {
    toast.error('Failed to enroll student')
  }
}

const handlePromoteFromWaitlist = (enrollment: Enrollment) => {
  // Promote student from waitlist
  const index = waitlist.value.findIndex(w => w.id === enrollment.id)
  if (index !== -1) {
    waitlist.value.splice(index, 1)
    enrollment.status = 'ACTIVE'
    enrollments.value.push(enrollment)
    toast.success(`Student promoted from waitlist`)
    updateStatistics()
    emit('enrollment-updated', props.course)
  }
}

const handleRemoveEnrollment = (enrollment: Enrollment) => {
  // Remove enrollment
  const index = enrollments.value.findIndex(e => e.id === enrollment.id)
  if (index !== -1) {
    enrollments.value.splice(index, 1)
    toast.success('Enrollment removed')
    updateStatistics()
    emit('enrollment-updated', props.course)
  }
}

const handleUpdateCapacity = (newCapacity: number) => {
  // Update course capacity
  toast.success(`Capacity updated to ${newCapacity} students`)
  emit('enrollment-updated', props.course)
}

const handleManageWaitlist = () => {
  showWaitlistModal.value = true
}

const handlePromoteStudent = (enrollment: Enrollment) => {
  handlePromoteFromWaitlist(enrollment)
}

const handleRemoveFromWaitlist = (enrollment: Enrollment) => {
  const index = waitlist.value.findIndex(w => w.id === enrollment.id)
  if (index !== -1) {
    waitlist.value.splice(index, 1)
    toast.success('Student removed from waitlist')
    updateStatistics()
  }
}

// Lifecycle
onMounted(() => {
  loadEnrollments()
})

watch(() => props.course, () => {
  loadEnrollments()
})
</script>