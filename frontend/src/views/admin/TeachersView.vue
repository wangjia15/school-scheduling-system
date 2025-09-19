<template>
  <AdminLayout>
    <div class="space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">Teacher Management</h1>
          <p class="text-muted-foreground">
            Manage teacher profiles, availability, and specializations
          </p>
        </div>
        <div class="flex items-center gap-2">
          <Button variant="outline" @click="refreshData">
            <RefreshCw class="h-4 w-4 mr-2" />
            Refresh
          </Button>
          <Button @click="showAddTeacherModal = true">
            <Plus class="h-4 w-4 mr-2" />
            Add Teacher
          </Button>
        </div>
      </div>

      <!-- Stats Cards -->
      <div class="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Total Teachers</CardTitle>
            <Users class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ stats.totalTeachers }}</div>
            <p class="text-xs text-muted-foreground">
              {{ stats.activeTeachers }} active
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Departments</CardTitle>
            <Building class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ stats.totalDepartments }}</div>
            <p class="text-xs text-muted-foreground">
              Across all faculties
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Avg. Workload</CardTitle>
            <Clock class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ stats.averageWorkload }}h</div>
            <p class="text-xs text-muted-foreground">
              Per week
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Availability</CardTitle>
            <CalendarCheck class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ stats.availableSlots }}</div>
            <p class="text-xs text-muted-foreground">
              Available slots
            </p>
          </CardContent>
        </Card>
      </div>

      <!-- Filters and Search -->
      <Card>
        <CardHeader>
          <CardTitle>Search & Filter</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex flex-col md:flex-row gap-4">
            <div class="relative flex-1">
              <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                v-model="searchQuery"
                placeholder="Search teachers by name, email, or employee ID..."
                class="pl-10"
                @keyup.enter="searchTeachers"
              />
            </div>
            <Select v-model="selectedDepartment" @change="searchTeachers">
              <option value="">All Departments</option>
              <option v-for="dept in departments" :key="dept.id" :value="dept.id">
                {{ dept.name }}
              </option>
            </Select>
            <Select v-model="selectedTitle" @change="searchTeachers">
              <option value="">All Titles</option>
              <option value="PROFESSOR">Professor</option>
              <option value="ASSOCIATE_PROFESSOR">Associate Professor</option>
              <option value="ASSISTANT_PROFESSOR">Assistant Professor</option>
              <option value="INSTRUCTOR">Instructor</option>
              <option value="ADJUNCT">Adjunct</option>
            </Select>
            <Button variant="outline" @click="resetFilters">
              <RotateCcw class="h-4 w-4 mr-2" />
              Reset
            </Button>
          </div>
        </CardContent>
      </Card>

      <!-- Teachers Table -->
      <Card>
        <CardHeader>
          <CardTitle>Teacher Directory</CardTitle>
          <CardDescription>
            {{ pagination.totalElements }} teachers found
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="rounded-md border">
            <Table :headers="tableHeaders" :data="teachers" striped hoverable>
              <template #cell-name="{ row }">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center">
                    <span class="text-sm font-medium text-primary">
                      {{ getInitials(row.user.firstName, row.user.lastName) }}
                    </span>
                  </div>
                  <div>
                    <div class="font-medium">{{ row.user.firstName }} {{ row.user.lastName }}</div>
                    <div class="text-sm text-muted-foreground">{{ row.user.email }}</div>
                  </div>
                </div>
              </template>

              <template #cell-employeeId="{ row }">
                <Badge variant="outline">{{ row.employeeId }}</Badge>
              </template>

              <template #cell-department="{ row }">
                <div>
                  <div class="font-medium">{{ row.department.name }}</div>
                  <div class="text-sm text-muted-foreground">{{ formatTitle(row.title) }}</div>
                </div>
              </template>

              <template #cell-workload="{ row }">
                <div class="space-y-1">
                  <div class="flex justify-between text-sm">
                    <span>{{ getCurrentWorkload(row) }}h / {{ row.maxWeeklyHours }}h</span>
                    <span>{{ getWorkloadPercentage(row) }}%</span>
                  </div>
                  <div class="w-full bg-secondary rounded-full h-2">
                    <div
                      class="bg-primary h-2 rounded-full transition-all"
                      :style="{ width: Math.min(getWorkloadPercentage(row), 100) + '%' }"
                      :class="getWorkloadColorClass(getWorkloadPercentage(row))"
                    ></div>
                  </div>
                </div>
              </template>

              <template #cell-specializations="{ row }">
                <div class="flex flex-wrap gap-1">
                  <Badge
                    v-for="spec in row.specializations.slice(0, 2)"
                    :key="spec.id"
                    variant="secondary"
                    class="text-xs"
                  >
                    {{ spec.subjectCode }}
                  </Badge>
                  <Badge
                    v-if="row.specializations.length > 2"
                    variant="outline"
                    class="text-xs"
                  >
                    +{{ row.specializations.length - 2 }}
                  </Badge>
                </div>
              </template>

              <template #cell-status="{ row }">
                <Badge :variant="row.user.isActive ? 'default' : 'secondary'">
                  {{ row.user.isActive ? 'Active' : 'Inactive' }}
                </Badge>
              </template>

              <template #cell-actions="{ row }">
                <div class="flex items-center gap-2">
                  <Button variant="ghost" size="sm" @click="viewTeacher(row)">
                    <Eye class="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="sm" @click="editTeacher(row)">
                    <Pencil class="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="sm" @click="viewAvailability(row)">
                    <Calendar class="h-4 w-4" />
                  </Button>
                  <DropdownMenu>
                    <DropdownMenuTrigger as-child>
                      <Button variant="ghost" size="sm">
                        <MoreHorizontal class="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem @click="viewTeacherSchedule(row)">
                        <CalendarDays class="h-4 w-4 mr-2" />
                        View Schedule
                      </DropdownMenuItem>
                      <DropdownMenuItem @click="editTeacher(row)">
                        <Pencil class="h-4 w-4 mr-2" />
                        Edit Profile
                      </DropdownMenuItem>
                      <DropdownMenuItem @click="viewAvailability(row)">
                        <Clock class="h-4 w-4 mr-2" />
                        Manage Availability
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem
                        @click="toggleTeacherStatus(row)"
                        :class="row.user.isActive ? 'text-destructive' : ''"
                      >
                        <Power class="h-4 w-4 mr-2" />
                        {{ row.user.isActive ? 'Deactivate' : 'Activate' }}
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
              of {{ pagination.totalElements }} teachers
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
    </div>

    <!-- Add/Edit Teacher Modal -->
    <TeacherFormModal
      v-model:open="showAddTeacherModal"
      :teacher="selectedTeacher"
      :departments="departments"
      @save="handleTeacherSave"
      @cancel="showAddTeacherModal = false"
    />

    <!-- Teacher Detail Modal -->
    <TeacherDetailModal
      v-model:open="showTeacherDetailModal"
      :teacher="selectedTeacher"
      @edit="editTeacher"
      @close="showTeacherDetailModal = false"
    />
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import Badge from '@/components/ui/Badge.vue'
import Table from '@/components/ui/Table.vue'
import TeacherFormModal from '@/components/teachers/TeacherFormModal.vue'
import TeacherDetailModal from '@/components/teachers/TeacherDetailModal.vue'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import {
  Users,
  Plus,
  Filter,
  Search,
  RefreshCw,
  Eye,
  Pencil,
  Calendar,
  MoreHorizontal,
  RotateCcw,
  Building,
  Clock,
  CalendarCheck,
  CalendarDays,
  Power
} from 'lucide-vue-next'
import teacherService, { type Teacher, type TeacherFilters } from '@/services/teacherService'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const toast = useToast()
const authStore = useAuthStore()

// State
const teachers = ref<Teacher[]>([])
const departments = ref<{ id: number; name: string }[]>([])
const loading = ref(false)
const searchQuery = ref('')
const selectedDepartment = ref('')
const selectedTitle = ref('')
const selectedTeacher = ref<Teacher | null>(null)
const showAddTeacherModal = ref(false)
const showTeacherDetailModal = ref(false)

// Pagination
const pagination = ref({
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0
})

// Stats
const stats = ref({
  totalTeachers: 0,
  activeTeachers: 0,
  totalDepartments: 0,
  averageWorkload: 0,
  availableSlots: 0
})

// Table headers
const tableHeaders = [
  { key: 'name', label: 'Teacher', width: '250px' },
  { key: 'employeeId', label: 'Employee ID', width: '120px' },
  { key: 'department', label: 'Department', width: '200px' },
  { key: 'workload', label: 'Workload', width: '150px' },
  { key: 'specializations', label: 'Specializations', width: '150px' },
  { key: 'status', label: 'Status', width: '100px' },
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

// Methods
const loadTeachers = async () => {
  loading.value = true
  try {
    const filters: TeacherFilters = {
      page: pagination.value.page,
      size: pagination.value.size,
      search: searchQuery.value || undefined,
      departmentId: selectedDepartment.value ? parseInt(selectedDepartment.value) : undefined,
      title: selectedTitle.value || undefined
    }

    const response = await teacherService.getTeachers(filters)
    teachers.value = response.content
    pagination.value = {
      page: response.number,
      size: response.size,
      totalElements: response.totalElements,
      totalPages: response.totalPages
    }
  } catch (error) {
    toast.error('Failed to load teachers')
    console.error('Error loading teachers:', error)
  } finally {
    loading.value = false
  }
}

const loadDepartments = async () => {
  try {
    // Mock departments for now - replace with API call
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
    // Mock stats for now - replace with API calls
    stats.value = {
      totalTeachers: 156,
      activeTeachers: 142,
      totalDepartments: 8,
      averageWorkload: 18,
      availableSlots: 1240
    }
  } catch (error) {
    console.error('Error loading stats:', error)
  }
}

const searchTeachers = () => {
  pagination.value.page = 0
  loadTeachers()
}

const resetFilters = () => {
  searchQuery.value = ''
  selectedDepartment.value = ''
  selectedTitle.value = ''
  pagination.value.page = 0
  loadTeachers()
}

const changePage = (page: number) => {
  pagination.value.page = page
  loadTeachers()
}

const refreshData = () => {
  loadTeachers()
  loadStats()
  toast.info('Data refreshed')
}

const addTeacher = () => {
  selectedTeacher.value = null
  showAddTeacherModal.value = true
}

const editTeacher = (teacher: Teacher) => {
  selectedTeacher.value = teacher
  showAddTeacherModal.value = true
}

const viewTeacher = (teacher: Teacher) => {
  selectedTeacher.value = teacher
  showTeacherDetailModal.value = true
}

const viewAvailability = (teacher: Teacher) => {
  router.push(`/teachers/${teacher.id}/availability`)
}

const viewTeacherSchedule = (teacher: Teacher) => {
  // Navigate to teacher schedule view
  console.log('View schedule for teacher:', teacher.id)
}

const toggleTeacherStatus = async (teacher: Teacher) => {
  try {
    // Mock status toggle - replace with API call
    teacher.user.isActive = !teacher.user.isActive
    toast.success(`Teacher ${teacher.user.isActive ? 'activated' : 'deactivated'} successfully`)
    await loadTeachers()
  } catch (error) {
    toast.error('Failed to update teacher status')
  }
}

const handleTeacherSave = async (teacherData: any) => {
  try {
    if (selectedTeacher.value) {
      // Update existing teacher
      await teacherService.updateTeacher(selectedTeacher.value.id, teacherData)
      toast.success('Teacher updated successfully')
    } else {
      // Create new teacher
      await teacherService.createTeacher(teacherData)
      toast.success('Teacher created successfully')
    }

    showAddTeacherModal.value = false
    await loadTeachers()
    await loadStats()
  } catch (error) {
    toast.error('Failed to save teacher')
    console.error('Error saving teacher:', error)
  }
}

// Utility functions
const getInitials = (firstName: string, lastName: string) => {
  return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase()
}

const formatTitle = (title: string) => {
  return title.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}

const getCurrentWorkload = (teacher: Teacher) => {
  // Mock workload calculation - replace with actual API call
  return Math.floor(Math.random() * teacher.maxWeeklyHours)
}

const getWorkloadPercentage = (teacher: Teacher) => {
  const current = getCurrentWorkload(teacher)
  return Math.round((current / teacher.maxWeeklyHours) * 100)
}

const getWorkloadColorClass = (percentage: number) => {
  if (percentage >= 90) return 'bg-red-500'
  if (percentage >= 75) return 'bg-yellow-500'
  return 'bg-green-500'
}

// Initialize
onMounted(async () => {
  await Promise.all([
    loadTeachers(),
    loadDepartments(),
    loadStats()
  ])
})
</script>