<template>
  <div class="p-6 space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Teacher Course Assignment</h1>
        <p class="text-gray-600 mt-2">
          Assign teachers to courses with availability and conflict checking
        </p>
      </div>
      <div class="flex items-center gap-2">
        <Button variant="outline" @click="refreshData">
          <RefreshCw class="h-4 w-4 mr-2" />
          Refresh
        </Button>
        <Button @click="showBulkAssignModal = true">
          <Users class="h-4 w-4 mr-2" />
          Bulk Assign
        </Button>
      </div>
    </div>

    <!-- Assignment Summary Stats -->
    <div class="grid gap-4 md:grid-cols-4">
      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">Total Assignments</CardTitle>
          <BookOpen class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ stats.totalAssignments }}</div>
          <p class="text-xs text-muted-foreground">
            {{ stats.activeAssignments }} active
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">Unassigned Courses</CardTitle>
          <AlertCircle class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ stats.unassignedCourses }}</div>
          <p class="text-xs text-muted-foreground">
            Need teachers
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">Conflicts Detected</CardTitle>
          <XCircle class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold text-red-600">{{ stats.conflicts }}</div>
          <p class="text-xs text-muted-foreground">
            Require resolution
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">Avg. Teacher Load</CardTitle>
          <TrendingUp class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ stats.averageTeacherLoad }}</div>
          <p class="text-xs text-muted-foreground">
            Courses per teacher
          </p>
        </CardContent>
      </Card>
    </div>

    <!-- Filters and Search -->
    <Card>
      <CardHeader>
        <CardTitle>Filter Courses</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="flex flex-col md:flex-row gap-4">
          <div class="relative flex-1">
            <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              v-model="courseSearchQuery"
              placeholder="Search courses by code, name, or department..."
              class="pl-10"
              @keyup.enter="searchCourses"
            />
          </div>
          <Select v-model="selectedDepartment" @change="searchCourses">
            <option value="">All Departments</option>
            <option v-for="dept in departments" :key="dept.id" :value="dept.id">
              {{ dept.name }}
            </option>
          </Select>
          <Select v-model="assignmentStatus" @change="searchCourses">
            <option value="">All Status</option>
            <option value="assigned">Assigned</option>
            <option value="unassigned">Unassigned</option>
            <option value="conflict">Has Conflicts</option>
          </Select>
          <Button variant="outline" @click="resetFilters">
            <RotateCcw class="h-4 w-4 mr-2" />
            Reset
          </Button>
        </div>
      </CardContent>
    </Card>

    <!-- Courses Table -->
    <Card>
      <CardHeader>
        <CardTitle>Course Assignments</CardTitle>
        <CardDescription>
          {{ pagination.totalElements }} courses found
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div class="rounded-md border">
          <Table :headers="courseTableHeaders" :data="courses" striped hoverable>
            <template #cell-courseCode="{ row }">
              <div class="flex items-center gap-3">
                <div>
                  <div class="font-medium">{{ row.courseCode }}</div>
                  <div class="text-sm text-muted-foreground">{{ row.courseName }}</div>
                </div>
              </div>
            </template>

            <template #cell-department="{ row }">
              <div>
                <div class="font-medium">{{ row.department.name }}</div>
                <div class="text-sm text-muted-foreground">{{ row.credits }} credits</div>
              </div>
            </template>

            <template #cell-teacher="{ row }">
              <div v-if="row.assignedTeacher">
                <div class="flex items-center gap-2">
                  <div class="w-6 h-6 rounded-full bg-primary/10 flex items-center justify-center">
                    <span class="text-xs font-medium text-primary">
                      {{ getInitials(row.assignedTeacher.user.firstName, row.assignedTeacher.user.lastName) }}
                    </span>
                  </div>
                  <div>
                    <div class="font-medium text-sm">
                      {{ row.assignedTeacher.user.firstName }} {{ row.assignedTeacher.user.lastName }}
                    </div>
                    <div class="text-xs text-muted-foreground">{{ row.assignedTeacher.title }}</div>
                  </div>
                </div>
              </div>
              <div v-else class="text-gray-500">
                <Button variant="outline" size="sm" @click="assignTeacher(row)">
                  <UserPlus class="h-3 w-3 mr-1" />
                  Assign
                </Button>
              </div>
            </template>

            <template #cell-schedule="{ row }">
              <div v-if="row.schedule" class="space-y-1">
                <div class="text-sm font-medium">{{ row.schedule.dayOfWeek }}</div>
                <div class="text-xs text-muted-foreground">
                  {{ row.schedule.startTime }} - {{ row.schedule.endTime }}
                </div>
                <div class="text-xs text-muted-foreground">{{ row.schedule.classroom.name }}</div>
              </div>
              <div v-else class="text-gray-500 text-sm">
                No schedule
              </div>
            </template>

            <template #cell-status="{ row }">
              <div class="flex items-center gap-2">
                <Badge :variant="getAssignmentStatusVariant(row)">
                  {{ getAssignmentStatus(row) }}
                </Badge>
                <div v-if="row.hasConflicts" class="flex items-center gap-1 text-red-600">
                  <AlertCircle class="h-3 w-3" />
                  <span class="text-xs">{{ row.conflictCount }} conflicts</span>
                </div>
              </div>
            </template>

            <template #cell-actions="{ row }">
              <div class="flex items-center gap-2">
                <Button variant="ghost" size="sm" @click="viewCourseDetails(row)">
                  <Eye class="h-4 w-4" />
                </Button>
                <Button variant="ghost" size="sm" @click="editAssignment(row)">
                  <Pencil class="h-4 w-4" />
                </Button>
                <Button
                  v-if="row.assignedTeacher"
                  variant="ghost"
                  size="sm"
                  @click="viewConflicts(row)"
                  :class="{ 'text-red-600': row.hasConflicts }"
                >
                  <AlertTriangle class="h-4 w-4" />
                </Button>
                <DropdownMenu>
                  <DropdownMenuTrigger as-child>
                    <Button variant="ghost" size="sm">
                      <MoreHorizontal class="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem @click="viewCourseDetails(row)">
                      <Eye class="h-4 w-4 mr-2" />
                      View Details
                    </DropdownMenuItem>
                    <DropdownMenuItem @click="editAssignment(row)">
                      <Pencil class="h-4 w-4 mr-2" />
                      {{ row.assignedTeacher ? 'Reassign Teacher' : 'Assign Teacher' }}
                    </DropdownMenuItem>
                    <DropdownMenuItem v-if="row.assignedTeacher" @click="viewConflicts(row)">
                      <AlertTriangle class="h-4 w-4 mr-2" />
                      View Conflicts
                    </DropdownMenuItem>
                    <DropdownMenuItem v-if="row.assignedTeacher" @click="unassignTeacher(row)" class="text-destructive">
                      <UserMinus class="h-4 w-4 mr-2" />
                      Unassign Teacher
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            </template>
          </Table>
        </div>

        <!-- Pagination -->
        <div class="flex items-center justify-between mt-4">
          <div class="text-sm text-muted-foreground">
            Showing {{ (pagination.page * pagination.size) + 1 }} to
            {{ Math.min((pagination.page + 1) * pagination.size, pagination.totalElements) }}
            of {{ pagination.totalElements }} courses
          </div>
          <div class="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              :disabled="pagination.page === 0"
              @click="changePage(pagination.page - 1)"
            >
              Previous
            </Button>
            <div class="flex items-center gap-1">
              <Button
                v-for="pageNum in visiblePageNumbers"
                :key="pageNum"
                variant="outline"
                size="sm"
                :class="{ 'bg-primary text-primary-foreground': pageNum === pagination.page }"
                @click="changePage(pageNum)"
              >
                {{ pageNum + 1 }}
              </Button>
            </div>
            <Button
              variant="outline"
              size="sm"
              :disabled="pagination.page === pagination.totalPages - 1"
              @click="changePage(pagination.page + 1)"
            >
              Next
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Assignment Modal -->
    <TeacherAssignmentModal
      v-model:open="showAssignmentModal"
      :course="selectedCourse"
      :teachers="teachers"
      @save="handleAssignmentSave"
    />

    <!-- Bulk Assignment Modal -->
    <BulkAssignmentModal
      v-model:open="showBulkAssignModal"
      :courses="unassignedCourses"
      :teachers="teachers"
      @save="handleBulkAssignmentSave"
    />

    <!-- Conflict Resolution Modal -->
    <ConflictResolutionModal
      v-model:open="showConflictModal"
      :course="selectedCourse"
      :conflicts="selectedConflicts"
      @resolve="handleConflictResolution"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import Card, { CardHeader, CardTitle, CardContent } from '@/components/ui/Card.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import Badge from '@/components/ui/Badge.vue'
import Table from '@/components/ui/Table.vue'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import {
  BookOpen,
  Users,
  RefreshCw,
  Search,
  RotateCcw,
  Eye,
  Pencil,
  UserPlus,
  UserMinus,
  MoreHorizontal,
  AlertCircle,
  XCircle,
  TrendingUp,
  AlertTriangle
} from 'lucide-vue-next'
import TeacherAssignmentModal from '@/components/assignments/TeacherAssignmentModal.vue'
import BulkAssignmentModal from '@/components/assignments/BulkAssignmentModal.vue'
import ConflictResolutionModal from '@/components/assignments/ConflictResolutionModal.vue'
import assignmentService, { type Course, type Teacher, type AssignmentFilters } from '@/services/assignmentService'

const router = useRouter()
const toast = useToast()

// State
const courses = ref<Course[]>([])
const teachers = ref<Teacher[]>([])
const departments = ref<{ id: number; name: string }[]>([])
const loading = ref(false)
const courseSearchQuery = ref('')
const selectedDepartment = ref('')
const assignmentStatus = ref('')
const selectedCourse = ref<Course | null>(null)
const showAssignmentModal = ref(false)
const showBulkAssignModal = ref(false)
const showConflictModal = ref(false)
const selectedConflicts = ref<any[]>([])

// Pagination
const pagination = ref({
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0
})

// Stats
const stats = ref({
  totalAssignments: 0,
  activeAssignments: 0,
  unassignedCourses: 0,
  conflicts: 0,
  averageTeacherLoad: 0
})

// Table headers
const courseTableHeaders = [
  { key: 'courseCode', label: 'Course', width: '250px' },
  { key: 'department', label: 'Department', width: '200px' },
  { key: 'teacher', label: 'Assigned Teacher', width: '200px' },
  { key: 'schedule', label: 'Schedule', width: '150px' },
  { key: 'status', label: 'Status', width: '120px' },
  { key: 'actions', label: 'Actions', width: '120px' }
]

// Computed
const visiblePageNumbers = computed(() => {
  const current = pagination.value.page
  const total = pagination.value.totalPages
  const delta = 2

  let start = Math.max(0, current - delta)
  let end = Math.min(total - 1, current + delta)

  if (end - start < 2 * delta) {
    start = Math.max(0, end - 2 * delta)
    end = Math.min(total - 1, start + 2 * delta)
  }

  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

const unassignedCourses = computed(() => {
  return courses.value.filter(course => !course.assignedTeacher)
})

// Methods
const loadCourses = async () => {
  loading.value = true
  try {
    const filters: AssignmentFilters = {
      page: pagination.value.page,
      size: pagination.value.size,
      search: courseSearchQuery.value || undefined,
      departmentId: selectedDepartment.value ? parseInt(selectedDepartment.value) : undefined,
      status: assignmentStatus.value || undefined
    }

    const response = await assignmentService.getCourses(filters)
    courses.value = response.content
    pagination.value = {
      page: response.number,
      size: response.size,
      totalElements: response.totalElements,
      totalPages: response.totalPages
    }
  } catch (error) {
    toast.error('Failed to load courses')
    console.error('Error loading courses:', error)
  } finally {
    loading.value = false
  }
}

const loadTeachers = async () => {
  try {
    teachers.value = await assignmentService.getAvailableTeachers()
  } catch (error) {
    console.error('Error loading teachers:', error)
  }
}

const loadDepartments = async () => {
  try {
    // Mock departments - replace with API call
    departments.value = [
      { id: 1, name: 'Computer Science' },
      { id: 2, name: 'Mathematics' },
      { id: 3, name: 'Physics' },
      { id: 4, name: 'Chemistry' },
      { id: 5, name: 'Biology' },
      { id: 6, name: 'English' },
      { id: 7, name: 'History' },
      { id: 8, name: 'Economics' }
    ]
  } catch (error) {
    console.error('Error loading departments:', error)
  }
}

const loadStats = async () => {
  try {
    // Mock stats - replace with API calls
    stats.value = {
      totalAssignments: 248,
      activeAssignments: 236,
      unassignedCourses: 12,
      conflicts: 8,
      averageTeacherLoad: 3.2
    }
  } catch (error) {
    console.error('Error loading stats:', error)
  }
}

const searchCourses = () => {
  pagination.value.page = 0
  loadCourses()
}

const resetFilters = () => {
  courseSearchQuery.value = ''
  selectedDepartment.value = ''
  assignmentStatus.value = ''
  pagination.value.page = 0
  loadCourses()
}

const changePage = (page: number) => {
  pagination.value.page = page
  loadCourses()
}

const refreshData = () => {
  loadCourses()
  loadStats()
  toast.info('Data refreshed')
}

const assignTeacher = (course: Course) => {
  selectedCourse.value = course
  showAssignmentModal.value = true
}

const editAssignment = (course: Course) => {
  selectedCourse.value = course
  showAssignmentModal.value = true
}

const unassignTeacher = async (course: Course) => {
  if (!confirm('Are you sure you want to unassign this teacher?')) {
    return
  }

  try {
    await assignmentService.unassignTeacher(course.id)
    toast.success('Teacher unassigned successfully')
    await loadCourses()
    await loadStats()
  } catch (error) {
    toast.error('Failed to unassign teacher')
    console.error('Error unassigning teacher:', error)
  }
}

const viewCourseDetails = (course: Course) => {
  router.push(`/courses/${course.id}`)
}

const viewConflicts = async (course: Course) => {
  try {
    selectedCourse.value = course
    selectedConflicts.value = await assignmentService.getCourseConflicts(course.id)
    showConflictModal.value = true
  } catch (error) {
    toast.error('Failed to load conflicts')
    console.error('Error loading conflicts:', error)
  }
}

const handleAssignmentSave = () => {
  showAssignmentModal.value = false
  loadCourses()
  loadStats()
}

const handleBulkAssignmentSave = () => {
  showBulkAssignModal.value = false
  loadCourses()
  loadStats()
}

const handleConflictResolution = () => {
  showConflictModal.value = false
  loadCourses()
  loadStats()
}

// Utility functions
const getInitials = (firstName: string, lastName: string) => {
  return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase()
}

const getAssignmentStatus = (course: Course) => {
  if (course.hasConflicts) return 'Conflict'
  if (course.assignedTeacher) return 'Assigned'
  return 'Unassigned'
}

const getAssignmentStatusVariant = (course: Course) => {
  if (course.hasConflicts) return 'destructive'
  if (course.assignedTeacher) return 'default'
  return 'secondary'
}

// Initialize
onMounted(async () => {
  await Promise.all([
    loadCourses(),
    loadTeachers(),
    loadDepartments(),
    loadStats()
  ])
})
</script>