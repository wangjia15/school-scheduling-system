<template>
  <div class="space-y-6">
    <!-- Header with Stats -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <Users class="w-5 h-5 text-blue-600" />
            <div>
              <p class="text-sm text-muted-foreground">Total Enrolled</p>
              <p class="text-2xl font-bold">{{ enrollmentStats.totalEnrolled }}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <UserCheck class="w-5 h-5 text-green-600" />
            <div>
              <p class="text-sm text-muted-foreground">Active Students</p>
              <p class="text-2xl font-bold">{{ enrollmentStats.activeStudents }}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <Clock class="w-5 h-5 text-orange-600" />
            <div>
              <p class="text-sm text-muted-foreground">Waitlisted</p>
              <p class="text-2xl font-bold">{{ enrollmentStats.waitlisted }}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <TrendingUp class="w-5 h-5 text-purple-600" />
            <div>
              <p class="text-sm text-muted-foreground">Fill Rate</p>
              <p class="text-2xl font-bold">{{ enrollmentStats.fillRate }}%</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Course Selection and Filters -->
    <Card>
      <CardHeader>
        <CardTitle>Enrollment Management</CardTitle>
        <CardDescription>Manage student enrollments and waitlists for courses</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
          <div>
            <label class="block text-sm font-medium mb-1">Course</label>
            <SelectInput
              v-model="filters.courseId"
              :options="courseOptions"
              placeholder="Select a course"
              @change="loadEnrollmentData"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Semester</label>
            <SelectInput
              v-model="filters.semester"
              :options="semesterOptions"
              placeholder="Select semester"
              @change="loadEnrollmentData"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Status</label>
            <SelectInput
              v-model="filters.status"
              :options="statusOptions"
              placeholder="All statuses"
              @change="loadEnrollmentData"
            />
          </div>
        </div>

        <!-- Course Capacity Overview -->
        <div v-if="selectedCourse" class="mb-6 p-4 bg-muted rounded-lg">
          <div class="flex justify-between items-start mb-4">
            <div>
              <h3 class="font-semibold">{{ selectedCourse.courseCode }} - {{ selectedCourse.title }}</h3>
              <p class="text-sm text-muted-foreground">{{ selectedCourse.department.name }} • {{ selectedCourse.credits }} credits</p>
            </div>
            <Badge :variant="getCapacityVariant()">
              {{ getCapacityStatus() }}
            </Badge>
          </div>

          <!-- Capacity Progress Bar -->
          <div class="mb-4">
            <div class="flex justify-between text-sm mb-1">
              <span>Enrollment Progress</span>
              <span>{{ enrollmentStats.enrolled }} / {{ selectedCourse.maxStudents }}</span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-2">
              <div
                class="bg-blue-600 h-2 rounded-full transition-all duration-300"
                :style="{ width: getEnrollmentPercentage() + '%' }"
              ></div>
            </div>
          </div>

          <!-- Capacity Stats -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <span class="text-muted-foreground">Min Required:</span>
              <span class="font-medium ml-1">{{ selectedCourse.minStudents }}</span>
            </div>
            <div>
              <span class="text-muted-foreground">Max Capacity:</span>
              <span class="font-medium ml-1">{{ selectedCourse.maxStudents }}</span>
            </div>
            <div>
              <span class="text-muted-foreground">Available:</span>
              <span class="font-medium ml-1">{{ getAvailableSpots() }}</span>
            </div>
            <div>
              <span class="text-muted-foreground">Waitlist:</span>
              <span class="font-medium ml-1">{{ enrollmentStats.waitlisted }}</span>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Enrollment Actions -->
    <div class="flex justify-between items-center">
      <div>
        <h3 class="text-lg font-semibold">Student Enrollments</h3>
        <p class="text-sm text-muted-foreground">Manage enrollments and waitlists</p>
      </div>
      <div class="flex space-x-2">
        <Button @click="showManualEnrollment = true" variant="outline">
          <UserPlus class="w-4 h-4 mr-2" />
          Manual Enrollment
        </Button>
        <Button @click="processWaitlist" variant="outline" :disabled="!hasWaitlist()">
          <Users class="w-4 h-4 mr-2" />
          Process Waitlist
        </Button>
        <Button @click="exportEnrollmentData">
          <Download class="w-4 h-4 mr-2" />
          Export
        </Button>
      </div>
    </div>

    <!-- Enrollment List -->
    <Card>
      <CardContent class="p-0">
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b">
                <th class="text-left p-4 font-medium">Student ID</th>
                <th class="text-left p-4 font-medium">Name</th>
                <th class="text-left p-4 font-medium">Email</th>
                <th class="text-left p-4 font-medium">Status</th>
                <th class="text-left p-4 font-medium">Enrolled Date</th>
                <th class="text-left p-4 font-medium">Grade</th>
                <th class="text-left p-4 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading" class="border-b">
                <td colspan="7" class="p-4 text-center">
                  <div class="flex justify-center">
                    <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
                  </div>
                </td>
              </tr>
              <tr v-else-if="enrollments.length === 0" class="border-b">
                <td colspan="7" class="p-4 text-center text-muted-foreground">
                  No enrollments found
                </td>
              </tr>
              <tr v-for="enrollment in enrollments" :key="enrollment.id" class="border-b hover:bg-muted/50">
                <td class="p-4 font-medium">{{ enrollment.studentId }}</td>
                <td class="p-4">{{ enrollment.studentName }}</td>
                <td class="p-4">{{ enrollment.studentEmail }}</td>
                <td class="p-4">
                  <Badge :variant="getStatusVariant(enrollment.status)">
                    {{ formatStatus(enrollment.status) }}
                  </Badge>
                </td>
                <td class="p-4">{{ formatDate(enrollment.enrolledAt) }}</td>
                <td class="p-4">
                  <span v-if="enrollment.grade">{{ enrollment.grade }}%</span>
                  <span v-else class="text-muted-foreground">-</span>
                </td>
                <td class="p-4">
                  <div class="flex space-x-2">
                    <Button
                      v-if="enrollment.status === 'ACTIVE'"
                      variant="ghost"
                      size="sm"
                      @click="dropStudent(enrollment)"
                      class="text-destructive hover:text-destructive"
                    >
                      <UserMinus class="w-4 h-4" />
                    </Button>
                    <Button
                      v-if="enrollment.status === 'WAITLISTED'"
                      variant="ghost"
                      size="sm"
                      @click="enrollFromWaitlist(enrollment)"
                    >
                      <UserCheck class="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      @click="viewEnrollmentDetails(enrollment)"
                    >
                      <Eye class="w-4 h-4" />
                    </Button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>

    <!-- Manual Enrollment Modal -->
    <div v-if="showManualEnrollment" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <Card class="w-full max-w-md">
        <CardHeader>
          <CardTitle>Manual Enrollment</CardTitle>
          <CardDescription>Enroll a student in {{ selectedCourse?.courseCode }}</CardDescription>
        </CardHeader>
        <CardContent>
          <form @submit.prevent="performManualEnrollment" class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1">Student ID</label>
              <Input
                v-model="manualEnrollment.studentId"
                placeholder="Enter student ID"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">Student Email</label>
              <Input
                v-model="manualEnrollment.studentEmail"
                type="email"
                placeholder="Enter student email"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">Enrollment Type</label>
              <select
                v-model="manualEnrollment.enrollmentType"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              >
                <option value="REGULAR">Regular Enrollment</option>
                <option value="AUDIT">Audit</option>
                <option value="CREDIT_BY_EXAM">Credit by Exam</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1">Notes</label>
              <textarea
                v-model="manualEnrollment.notes"
                class="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                placeholder="Additional notes..."
                rows="3"
              ></textarea>
            </div>
            <div class="flex justify-end space-x-2">
              <Button type="button" variant="outline" @click="cancelManualEnrollment">
                Cancel
              </Button>
              <Button type="submit" :disabled="isEnrolling">
                {{ isEnrolling ? 'Enrolling...' : 'Enroll Student' }}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>

    <!-- Enrollment Details Modal -->
    <div v-if="showEnrollmentDetails" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <Card class="w-full max-w-lg">
        <CardHeader>
          <CardTitle>Enrollment Details</CardTitle>
          <CardDescription>View detailed enrollment information</CardDescription>
        </CardHeader>
        <CardContent v-if="selectedEnrollment">
          <div class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-sm font-medium">Student ID</label>
                <p class="text-sm text-muted-foreground">{{ selectedEnrollment.studentId }}</p>
              </div>
              <div>
                <label class="text-sm font-medium">Name</label>
                <p class="text-sm text-muted-foreground">{{ selectedEnrollment.studentName }}</p>
              </div>
              <div>
                <label class="text-sm font-medium">Email</label>
                <p class="text-sm text-muted-foreground">{{ selectedEnrollment.studentEmail }}</p>
              </div>
              <div>
                <label class="text-sm font-medium">Status</label>
                <Badge :variant="getStatusVariant(selectedEnrollment.status)">
                  {{ formatStatus(selectedEnrollment.status) }}
                </Badge>
              </div>
              <div>
                <label class="text-sm font-medium">Enrolled Date</label>
                <p class="text-sm text-muted-foreground">{{ formatDate(selectedEnrollment.enrolledAt) }}</p>
              </div>
              <div>
                <label class="text-sm font-medium">Grade</label>
                <p class="text-sm text-muted-foreground">
                  {{ selectedEnrollment.grade ? selectedEnrollment.grade + '%' : 'Not graded' }}
                </p>
              </div>
            </div>
            <div v-if="selectedEnrollment.notes">
              <label class="text-sm font-medium">Notes</label>
              <p class="text-sm text-muted-foreground">{{ selectedEnrollment.notes }}</p>
            </div>
            <div class="flex justify-end">
              <Button @click="showEnrollmentDetails = false" variant="outline">
                Close
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import SelectInput from '@/components/ui/SelectInput.vue'
import Badge from '@/components/ui/Badge.vue'
import { useToast } from 'vue-toastification'
import type { Course } from '@/types/course'
import { courseService } from '@/services/courseService'
import {
  Users,
  UserCheck,
  Clock,
  TrendingUp,
  UserPlus,
  Download,
  UserMinus,
  Eye
} from 'lucide-vue-next'

interface Props {
  courses: Course[]
}

const props = defineProps<Props>()

const toast = useToast()

// State
const loading = ref(false)
const isEnrolling = ref(false)
const showManualEnrollment = ref(false)
const showEnrollmentDetails = ref(false)
const enrollments = ref<any[]>([])
const selectedCourse = ref<Course | null>(null)
const selectedEnrollment = ref<any>(null)

// Filters
const filters = reactive({
  courseId: '',
  semester: '',
  status: ''
})

// Manual Enrollment Form
const manualEnrollment = reactive({
  studentId: '',
  studentEmail: '',
  enrollmentType: 'REGULAR',
  notes: ''
})

// Enrollment Stats
const enrollmentStats = reactive({
  totalEnrolled: 0,
  activeStudents: 0,
  waitlisted: 0,
  fillRate: 0,
  enrolled: 0
})

// Options
const courseOptions = computed(() => [
  { value: '', label: 'All Courses' },
  ...props.courses.map(course => ({
    value: course.id.toString(),
    label: `${course.courseCode} - ${course.title}`
  }))
])

const semesterOptions = [
  { value: '', label: 'All Semesters' },
  { value: '2024-FALL', label: 'Fall 2024' },
  { value: '2025-SPRING', label: 'Spring 2025' },
  { value: '2025-SUMMER', label: 'Summer 2025' }
]

const statusOptions = [
  { value: '', label: 'All Statuses' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'WAITLISTED', label: 'Waitlisted' },
  { value: 'DROPPED', label: 'Dropped' },
  { value: 'COMPLETED', label: 'Completed' }
]

// Methods
const loadEnrollmentData = async () => {
  if (!filters.courseId) return

  try {
    loading.value = true
    selectedCourse.value = props.courses.find(c => c.id.toString() === filters.courseId) || null

    // Mock enrollment data - in real implementation, this would call an API
    const mockEnrollments = [
      {
        id: 1,
        studentId: 'STU001',
        studentName: 'John Doe',
        studentEmail: 'john.doe@university.edu',
        status: 'ACTIVE',
        enrolledAt: '2024-09-01T10:00:00Z',
        grade: null,
        notes: 'Regular enrollment'
      },
      {
        id: 2,
        studentId: 'STU002',
        studentName: 'Jane Smith',
        studentEmail: 'jane.smith@university.edu',
        status: 'ACTIVE',
        enrolledAt: '2024-09-02T14:30:00Z',
        grade: null,
        notes: 'Regular enrollment'
      },
      {
        id: 3,
        studentId: 'STU003',
        studentName: 'Bob Johnson',
        studentEmail: 'bob.johnson@university.edu',
        status: 'WAITLISTED',
        enrolledAt: '2024-09-03T09:15:00Z',
        grade: null,
        notes: 'Waitlisted due to full capacity'
      }
    ]

    enrollments.value = mockEnrollments.filter(e =>
      (!filters.status || e.status === filters.status)
    )

    // Update stats
    updateEnrollmentStats()
  } catch (error) {
    toast.error('Failed to load enrollment data')
  } finally {
    loading.value = false
  }
}

const updateEnrollmentStats = () => {
  if (!selectedCourse.value) return

  const active = enrollments.value.filter(e => e.status === 'ACTIVE').length
  const waitlisted = enrollments.value.filter(e => e.status === 'WAITLISTED').length
  const maxCapacity = selectedCourse.value.maxStudents

  enrollmentStats.totalEnrolled = active + waitlisted
  enrollmentStats.activeStudents = active
  enrollmentStats.waitlisted = waitlisted
  enrollmentStats.enrolled = active
  enrollmentStats.fillRate = maxCapacity > 0 ? Math.round((active / maxCapacity) * 100) : 0
}

const getEnrollmentPercentage = () => {
  if (!selectedCourse.value) return 0
  return Math.round((enrollmentStats.enrolled / selectedCourse.value.maxStudents) * 100)
}

const getAvailableSpots = () => {
  if (!selectedCourse.value) return 0
  return Math.max(0, selectedCourse.value.maxStudents - enrollmentStats.enrolled)
}

const getCapacityVariant = () => {
  const percentage = getEnrollmentPercentage()
  if (percentage >= 100) return 'destructive'
  if (percentage >= 80) return 'secondary'
  return 'default'
}

const getCapacityStatus = () => {
  const percentage = getEnrollmentPercentage()
  if (percentage >= 100) return 'Full'
  if (percentage >= 80) return 'Nearly Full'
  if (percentage >= selectedCourse.value?.minStudents!) return 'Good'
  return 'Low Enrollment'
}

const getStatusVariant = (status: string) => {
  switch (status) {
    case 'ACTIVE': return 'default'
    case 'WAITLISTED': return 'secondary'
    case 'DROPPED': return 'destructive'
    case 'COMPLETED': return 'outline'
    default: return 'default'
  }
}

const formatStatus = (status: string) => {
  return status.charAt(0) + status.slice(1).toLowerCase()
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString()
}

const hasWaitlist = () => {
  return enrollments.value.some(e => e.status === 'WAITLISTED')
}

const performManualEnrollment = async () => {
  try {
    isEnrolling.value = true

    // Check capacity
    if (selectedCourse.value && enrollmentStats.enrolled >= selectedCourse.value.maxStudents) {
      // Add to waitlist
      enrollments.value.push({
        id: Date.now(),
        studentId: manualEnrollment.studentId,
        studentName: manualEnrollment.studentEmail.split('@')[0],
        studentEmail: manualEnrollment.studentEmail,
        status: 'WAITLISTED',
        enrolledAt: new Date().toISOString(),
        grade: null,
        notes: manualEnrollment.notes
      })
      toast.info('Course is full. Student added to waitlist.')
    } else {
      // Regular enrollment
      enrollments.value.push({
        id: Date.now(),
        studentId: manualEnrollment.studentId,
        studentName: manualEnrollment.studentEmail.split('@')[0],
        studentEmail: manualEnrollment.studentEmail,
        status: 'ACTIVE',
        enrolledAt: new Date().toISOString(),
        grade: null,
        notes: manualEnrollment.notes
      })
      toast.success('Student enrolled successfully')
    }

    cancelManualEnrollment()
    updateEnrollmentStats()
  } catch (error) {
    toast.error('Failed to enroll student')
  } finally {
    isEnrolling.value = false
  }
}

const cancelManualEnrollment = () => {
  showManualEnrollment.value = false
  Object.assign(manualEnrollment, {
    studentId: '',
    studentEmail: '',
    enrollmentType: 'REGULAR',
    notes: ''
  })
}

const dropStudent = async (enrollment: any) => {
  try {
    const index = enrollments.value.findIndex(e => e.id === enrollment.id)
    if (index > -1) {
      enrollments.value.splice(index, 1)
      toast.success('Student dropped successfully')
      updateEnrollmentStats()
    }
  } catch (error) {
    toast.error('Failed to drop student')
  }
}

const enrollFromWaitlist = async (enrollment: any) => {
  try {
    enrollment.status = 'ACTIVE'
    enrollment.enrolledAt = new Date().toISOString()
    toast.success('Student enrolled from waitlist')
    updateEnrollmentStats()
  } catch (error) {
    toast.error('Failed to enroll from waitlist')
  }
}

const processWaitlist = async () => {
  try {
    const waitlisted = enrollments.value.filter(e => e.status === 'WAITLISTED')
    const availableSpots = getAvailableSpots()
    const toEnroll = waitlisted.slice(0, availableSpots)

    toEnroll.forEach(enrollment => {
      enrollment.status = 'ACTIVE'
      enrollment.enrolledAt = new Date().toISOString()
    })

    toast.success(`Enrolled ${toEnroll.length} students from waitlist`)
    updateEnrollmentStats()
  } catch (error) {
    toast.error('Failed to process waitlist')
  }
}

const viewEnrollmentDetails = (enrollment: any) => {
  selectedEnrollment.value = enrollment
  showEnrollmentDetails.value = true
}

const exportEnrollmentData = () => {
  // Mock export functionality
  const data = enrollments.value.map(e => ({
    'Student ID': e.studentId,
    'Name': e.studentName,
    'Email': e.studentEmail,
    'Status': e.status,
    'Enrolled Date': formatDate(e.enrolledAt),
    'Grade': e.grade || ''
  }))

  // Create CSV content
  const headers = Object.keys(data[0] || {})
  const csv = [
    headers.join(','),
    ...data.map(row => headers.map(header => row[header]).join(','))
  ].join('\n')

  // Download file
  const blob = new Blob([csv], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `enrollments-${selectedCourse.value?.courseCode || 'all'}.csv`
  a.click()
  window.URL.revokeObjectURL(url)

  toast.success('Enrollment data exported successfully')
}

// Lifecycle
onMounted(() => {
  // Load initial data if a course is selected
  if (filters.courseId) {
    loadEnrollmentData()
  }
})
</script>