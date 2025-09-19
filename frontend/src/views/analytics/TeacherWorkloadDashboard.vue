<template>
  <div class="p-6 space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Teacher Workload Analytics</h1>
        <p class="text-gray-600 mt-2">
          Comprehensive workload tracking and optimization insights
        </p>
      </div>
      <div class="flex items-center gap-2">
        <Select v-model="selectedSemester" @change="loadData" class="w-48">
          <option value="2024-FALL">Fall 2024</option>
          <option value="2024-SPRING">Spring 2024</option>
          <option value="2023-FALL">Fall 2023</option>
        </Select>
        <Button variant="outline" @click="refreshData">
          <RefreshCw class="h-4 w-4 mr-2" />
          Refresh
        </Button>
        <Button @click="exportReport">
          <Download class="h-4 w-4 mr-2" />
          Export Report
        </Button>
      </div>
    </div>

    <!-- Executive Summary -->
    <div class="grid gap-4 md:grid-cols-4">
      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">Total Teachers</CardTitle>
          <Users class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ summary.totalTeachers }}</div>
          <p class="text-xs text-muted-foreground">
            {{ summary.activeTeachers }} active
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">Avg. Workload</CardTitle>
          <Clock class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold">{{ summary.averageWorkload }}h</div>
          <p class="text-xs text-muted-foreground">
            per week
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">Overutilized</CardTitle>
          <TrendingUp class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold text-red-600">{{ summary.overutilizedTeachers }}</div>
          <p class="text-xs text-muted-foreground">
            >90% capacity
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle class="text-sm font-medium">Underutilized</CardTitle>
          <TrendingDown class="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div class="text-2xl font-bold text-blue-600">{{ summary.underutilizedTeachers }}</div>
          <p class="text-xs text-muted-foreground">
            <50% capacity
          </p>
        </CardContent>
      </Card>
    </div>

    <!-- Workload Distribution Chart -->
    <div class="grid gap-6 md:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>Workload Distribution</CardTitle>
          <CardDescription>
            Distribution of teachers across workload ranges
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <!-- Workload Range Bars -->
            <div v-for="range in workloadRanges" :key="range.label" class="space-y-2">
              <div class="flex justify-between items-center">
                <span class="text-sm font-medium">{{ range.label }}</span>
                <span class="text-sm text-gray-600">{{ range.count }} teachers ({{ range.percentage }}%)</span>
              </div>
              <div class="w-full bg-gray-200 rounded-full h-3">
                <div
                  class="h-3 rounded-full transition-all duration-300"
                  :class="range.colorClass"
                  :style="{ width: range.percentage + '%' }"
                ></div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Department Workload</CardTitle>
          <CardDescription>
            Average workload by department
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <div v-for="dept in departmentWorkloads" :key="dept.name" class="space-y-2">
              <div class="flex justify-between items-center">
                <span class="text-sm font-medium">{{ dept.name }}</span>
                <div class="flex items-center gap-2">
                  <span class="text-sm text-gray-600">{{ dept.averageWorkload }}h</span>
                  <Badge
                    :variant="getWorkloadBadgeVariant(dept.utilization)"
                    class="text-xs"
                  >
                    {{ dept.utilization }}%
                  </Badge>
                </div>
              </div>
              <div class="w-full bg-gray-200 rounded-full h-2">
                <div
                  class="h-2 rounded-full transition-all duration-300"
                  :class="getWorkloadColorClass(dept.utilization)"
                  :style="{ width: dept.utilization + '%' }"
                ></div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Detailed Teacher Workload Table -->
    <Card>
      <CardHeader>
        <div class="flex items-center justify-between">
          <div>
            <CardTitle>Detailed Teacher Workload</CardTitle>
            <CardDescription>
              Individual teacher workload and assignment details
            </CardDescription>
          </div>
          <div class="flex items-center gap-2">
            <Input
              v-model="teacherSearch"
              placeholder="Search teachers..."
              class="w-64"
              @keyup.enter="searchTeachers"
            />
            <Select v-model="workloadFilter" @change="filterTeachers">
              <option value="">All Workload Levels</option>
              <option value="overutilized">Overutilized (>90%)</option>
              <option value="high">High (75-90%)</option>
              <option value="optimal">Optimal (50-75%)</option>
              <option value="underutilized">Underutilized (<50%)</option>
            </Select>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div class="rounded-md border">
          <Table :headers="workloadTableHeaders" :data="filteredTeachers" striped hoverable>
            <template #cell-teacher="{ row }">
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center">
                  <span class="text-sm font-medium text-primary">
                    {{ getInitials(row.user.firstName, row.user.lastName) }}
                  </span>
                </div>
                <div>
                  <div class="font-medium">{{ row.user.firstName }} {{ row.user.lastName }}</div>
                  <div class="text-sm text-muted-foreground">{{ row.title }} • {{ row.department.name }}</div>
                </div>
              </div>
            </template>

            <template #cell-currentWorkload="{ row }">
              <div class="space-y-1">
                <div class="flex justify-between text-sm">
                  <span>{{ row.currentWorkload }}h / {{ row.maxWeeklyHours }}h</span>
                  <span>{{ getWorkloadPercentage(row) }}%</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2">
                  <div
                    class="h-2 rounded-full transition-all"
                    :class="getWorkloadColorClass(getWorkloadPercentage(row))"
                    :style="{ width: Math.min(getWorkloadPercentage(row), 100) + '%' }"
                  ></div>
                </div>
              </div>
            </template>

            <template #cell-assignments="{ row }">
              <div class="space-y-1">
                <div class="text-sm font-medium">{{ row.assignedCourses.length }} courses</div>
                <div class="text-xs text-muted-foreground">
                  {{ getTotalCredits(row) }} credits
                </div>
              </div>
            </template>

            <template #cell-availability="{ row }">
              <div class="space-y-1">
                <div class="text-sm">{{ row.availableHours }}h available</div>
                <div class="text-xs text-muted-foreground">
                  {{ row.availableDays }} days/week
                </div>
              </div>
            </template>

            <template #cell-status="{ row }">
              <Badge :variant="getWorkloadBadgeVariant(getWorkloadPercentage(row))">
                {{ getWorkloadStatus(row) }}
              </Badge>
            </template>

            <template #cell-actions="{ row }">
              <div class="flex items-center gap-2">
                <Button variant="ghost" size="sm" @click="viewTeacherDetails(row)">
                  <Eye class="h-4 w-4" />
                </Button>
                <Button variant="ghost" size="sm" @click="optimizeWorkload(row)">
                  <Settings class="h-4 w-4" />
                </Button>
                <DropdownMenu>
                  <DropdownMenuTrigger as-child>
                    <Button variant="ghost" size="sm">
                      <MoreHorizontal class="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem @click="viewTeacherDetails(row)">
                      <Eye class="h-4 w-4 mr-2" />
                      View Details
                    </DropdownMenuItem>
                    <DropdownMenuItem @click="viewSchedule(row)">
                      <Calendar class="h-4 w-4 mr-2" />
                      View Schedule
                    </DropdownMenuItem>
                    <DropdownMenuItem @click="optimizeWorkload(row)">
                      <Settings class="h-4 w-4 mr-2" />
                      Optimize Workload
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem @click="reassignCourses(row)">
                      <UserMinus class="h-4 w-4 mr-2" />
                      Reassign Courses
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            </template>
          </Table>
        </div>
      </CardContent>
    </Card>

    <!-- Workload Optimization Insights -->
    <Card>
      <CardHeader>
        <CardTitle>Optimization Insights</CardTitle>
        <CardDescription>
          AI-powered recommendations for workload balancing
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <div
            v-for="insight in optimizationInsights"
            :key="insight.id"
            class="border rounded-lg p-4"
            :class="getInsightBorderClass(insight.priority)"
          >
            <div class="flex items-start gap-3">
              <div :class="getInsightIconClass(insight.priority)">
                <Lightbulb class="h-5 w-5" />
              </div>
              <div class="flex-1">
                <div class="flex items-center gap-2 mb-1">
                  <h5 class="font-medium text-sm">{{ insight.title }}</h5>
                  <Badge :variant="getInsightVariant(insight.priority)" class="text-xs">
                    {{ insight.priority }}
                  </Badge>
                </div>
                <p class="text-sm text-gray-600 mb-2">{{ insight.description }}</p>
                <div class="space-y-1">
                  <div v-for="action in insight.actions" :key="action" class="text-xs text-gray-700">
                    • {{ action }}
                  </div>
                </div>
                <div class="mt-3">
                  <Button
                    variant="outline"
                    size="sm"
                    @click="applyOptimization(insight)"
                  >
                    Apply
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Teacher Detail Modal -->
    <TeacherWorkloadDetailModal
      v-model:open="showTeacherDetailModal"
      :teacher="selectedTeacher"
      @close="showTeacherDetailModal = false"
    />

    <!-- Workload Optimization Modal -->
    <WorkloadOptimizationModal
      v-model:open="showOptimizationModal"
      :teacher="selectedTeacher"
      :insights="optimizationInsights"
      @apply="handleOptimizationApplied"
      @close="showOptimizationModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
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
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import {
  Users,
  Clock,
  TrendingUp,
  TrendingDown,
  RefreshCw,
  Download,
  Eye,
  Settings,
  Calendar,
  MoreHorizontal,
  UserMinus,
  Lightbulb
} from 'lucide-vue-next'
import TeacherWorkloadDetailModal from '@/components/analytics/TeacherWorkloadDetailModal.vue'
import WorkloadOptimizationModal from '@/components/analytics/WorkloadOptimizationModal.vue'
import { type Teacher } from '@/services/teacherService'
import analyticsService from '@/services/analyticsService'

const toast = useToast()

// State
const teachers = ref<Teacher[]>([])
const loading = ref(false)
const selectedSemester = ref('2024-FALL')
const teacherSearch = ref('')
const workloadFilter = ref('')
const selectedTeacher = ref<Teacher | null>(null)
const showTeacherDetailModal = ref(false)
const showOptimizationModal = ref(false)

// Summary Data
const summary = reactive({
  totalTeachers: 0,
  activeTeachers: 0,
  averageWorkload: 0,
  overutilizedTeachers: 0,
  underutilizedTeachers: 0
})

// Workload Ranges
const workloadRanges = ref([
  { label: '0-25% (Very Light)', min: 0, max: 25, count: 0, percentage: 0, colorClass: 'bg-blue-500' },
  { label: '26-50% (Light)', min: 26, max: 50, count: 0, percentage: 0, colorClass: 'bg-green-500' },
  { label: '51-75% (Optimal)', min: 51, max: 75, count: 0, percentage: 0, colorClass: 'bg-yellow-500' },
  { label: '76-90% (High)', min: 76, max: 90, count: 0, percentage: 0, colorClass: 'bg-orange-500' },
  { label: '91-100% (Very High)', min: 91, max: 100, count: 0, percentage: 0, colorClass: 'bg-red-500' }
])

const departmentWorkloads = ref([
  { name: 'Computer Science', totalTeachers: 25, averageWorkload: 18.5, utilization: 62 },
  { name: 'Mathematics', totalTeachers: 18, averageWorkload: 16.2, utilization: 54 },
  { name: 'Physics', totalTeachers: 15, averageWorkload: 22.1, utilization: 74 },
  { name: 'Chemistry', totalTeachers: 12, averageWorkload: 19.8, utilization: 66 },
  { name: 'Biology', totalTeachers: 20, averageWorkload: 17.3, utilization: 58 },
  { name: 'English', totalTeachers: 22, averageWorkload: 15.6, utilization: 52 },
  { name: 'History', totalTeachers: 16, averageWorkload: 14.2, utilization: 47 },
  { name: 'Economics', totalTeachers: 14, averageWorkload: 20.4, utilization: 68 }
])

const optimizationInsights = ref([
  {
    id: 1,
    priority: 'HIGH',
    title: 'Rebalance Computer Science Department',
    description: '5 teachers are overutilized while 3 are underutilized in CS department.',
    actions: [
      'Transfer 2 courses from Dr. Smith to Dr. Johnson',
      'Adjust office hours for better distribution',
      'Consider hiring part-time adjunct faculty'
    ],
    impact: 'high'
  },
  {
    id: 2,
    priority: 'MEDIUM',
    title: 'Optimize Physics Lab Assignments',
    description: 'Physics department has uneven lab teaching loads.',
    actions: [
      'Redistribute lab sections among qualified faculty',
      'Schedule labs more efficiently across the week',
      'Cross-train teachers for multiple lab types'
    ],
    impact: 'medium'
  },
  {
    id: 3,
    priority: 'LOW',
    title: 'Improve Mathematics Department Efficiency',
    description: 'Some math teachers have availability for additional courses.',
    actions: [
      'Assign additional sections to available teachers',
      'Optimize scheduling to maximize classroom usage',
      'Consider hybrid course offerings'
    ],
    impact: 'low'
  }
])

// Table Headers
const workloadTableHeaders = [
  { key: 'teacher', label: 'Teacher', width: '250px' },
  { key: 'currentWorkload', label: 'Current Workload', width: '200px' },
  { key: 'assignments', label: 'Assignments', width: '120px' },
  { key: 'availability', label: 'Availability', width: '120px' },
  { key: 'status', label: 'Status', width: '120px' },
  { key: 'actions', label: 'Actions', width: '120px' }
]

// Computed
const filteredTeachers = computed(() => {
  let filtered = teachers.value

  // Search filter
  if (teacherSearch.value) {
    const search = teacherSearch.value.toLowerCase()
    filtered = filtered.filter(teacher =>
      teacher.user.firstName.toLowerCase().includes(search) ||
      teacher.user.lastName.toLowerCase().includes(search) ||
      teacher.user.email.toLowerCase().includes(search) ||
      teacher.employeeId.toLowerCase().includes(search)
    )
  }

  // Workload filter
  if (workloadFilter.value) {
    filtered = filtered.filter(teacher => {
      const percentage = getWorkloadPercentage(teacher)
      switch (workloadFilter.value) {
        case 'overutilized': return percentage > 90
        case 'high': return percentage > 75 && percentage <= 90
        case 'optimal': return percentage > 50 && percentage <= 75
        case 'underutilized': return percentage <= 50
        default: return true
      }
    })
  }

  return filtered
})

// Methods
const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadTeachers(),
      loadSummary(),
      loadWorkloadDistribution(),
      loadOptimizationInsights()
    ])
  } catch (error) {
    toast.error('Failed to load workload data')
    console.error('Error loading workload data:', error)
  } finally {
    loading.value = false
  }
}

const loadTeachers = async () => {
  try {
    // Mock data - replace with actual API call
    teachers.value = [
      {
        id: 1,
        employeeId: 'EMP001',
        userId: 1,
        departmentId: 1,
        title: 'PROFESSOR',
        maxWeeklyHours: 40,
        maxCoursesPerSemester: 5,
        user: { id: 1, firstName: 'John', lastName: 'Smith', email: 'john.smith@university.edu' },
        department: { id: 1, name: 'Computer Science' },
        specializations: [],
        currentWorkload: 36,
        availableHours: 4,
        availableDays: 2,
        assignedCourses: [
          { id: 1, courseCode: 'CS101', courseName: 'Intro to Programming', credits: 3 },
          { id: 2, courseCode: 'CS201', courseName: 'Data Structures', credits: 4 }
        ]
      },
      // Add more mock teachers as needed
    ]
  } catch (error) {
    console.error('Error loading teachers:', error)
  }
}

const loadSummary = async () => {
  try {
    // Mock summary data
    summary.totalTeachers = 142
    summary.activeTeachers = 138
    summary.averageWorkload = 17.8
    summary.overutilizedTeachers = 18
    summary.underutilizedTeachers = 35
  } catch (error) {
    console.error('Error loading summary:', error)
  }
}

const loadWorkloadDistribution = async () => {
  try {
    // Calculate workload distribution
    const distribution = [0, 0, 0, 0, 0] // Very Light, Light, Optimal, High, Very High

    teachers.value.forEach(teacher => {
      const percentage = getWorkloadPercentage(teacher)
      if (percentage <= 25) distribution[0]++
      else if (percentage <= 50) distribution[1]++
      else if (percentage <= 75) distribution[2]++
      else if (percentage <= 90) distribution[3]++
      else distribution[4]++
    })

    workloadRanges.value.forEach((range, index) => {
      range.count = distribution[index]
      range.percentage = teachers.value.length > 0 ? Math.round((distribution[index] / teachers.value.length) * 100) : 0
    })
  } catch (error) {
    console.error('Error loading workload distribution:', error)
  }
}

const loadOptimizationInsights = async () => {
  try {
    // Mock insights - replace with actual AI-powered recommendations
    // optimizationInsights.value is already populated with mock data
  } catch (error) {
    console.error('Error loading optimization insights:', error)
  }
}

const refreshData = () => {
  loadData()
  toast.info('Workload data refreshed')
}

const searchTeachers = () => {
  // Filtering is handled by computed property
}

const filterTeachers = () => {
  // Filtering is handled by computed property
}

const exportReport = () => {
  toast.info('Export functionality would be implemented here')
}

const viewTeacherDetails = (teacher: Teacher) => {
  selectedTeacher.value = teacher
  showTeacherDetailModal.value = true
}

const viewSchedule = (teacher: Teacher) => {
  toast.info('Schedule view would be implemented here')
}

const optimizeWorkload = (teacher: Teacher) => {
  selectedTeacher.value = teacher
  showOptimizationModal.value = true
}

const reassignCourses = (teacher: Teacher) => {
  toast.info('Course reassignment would be implemented here')
}

const applyOptimization = (insight: any) => {
  selectedTeacher.value = null
  showOptimizationModal.value = true
}

const handleOptimizationApplied = () => {
  showOptimizationModal.value = false
  loadData()
  toast.success('Workload optimization applied successfully')
}

// Utility functions
const getInitials = (firstName: string, lastName: string) => {
  return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase()
}

const getWorkloadPercentage = (teacher: Teacher) => {
  return Math.round((teacher.currentWorkload / teacher.maxWeeklyHours) * 100)
}

const getWorkloadColorClass = (percentage: number) => {
  if (percentage >= 90) return 'bg-red-500'
  if (percentage >= 75) return 'bg-orange-500'
  if (percentage >= 50) return 'bg-yellow-500'
  return 'bg-green-500'
}

const getWorkloadBadgeVariant = (percentage: number) => {
  if (percentage >= 90) return 'destructive'
  if (percentage >= 75) return 'outline'
  if (percentage >= 50) return 'default'
  return 'secondary'
}

const getWorkloadStatus = (teacher: Teacher) => {
  const percentage = getWorkloadPercentage(teacher)
  if (percentage >= 90) return 'Overutilized'
  if (percentage >= 75) return 'High'
  if (percentage >= 50) return 'Optimal'
  return 'Underutilized'
}

const getTotalCredits = (teacher: Teacher) => {
  return teacher.assignedCourses.reduce((total, course) => total + course.credits, 0)
}

const getInsightBorderClass = (priority: string) => {
  switch (priority) {
    case 'HIGH': return 'border-red-300 bg-red-50'
    case 'MEDIUM': return 'border-yellow-300 bg-yellow-50'
    case 'LOW': return 'border-blue-300 bg-blue-50'
    default: return 'border-gray-300 bg-gray-50'
  }
}

const getInsightIconClass = (priority: string) => {
  switch (priority) {
    case 'HIGH': return 'text-red-600 bg-red-100 rounded-full p-1'
    case 'MEDIUM': return 'text-yellow-600 bg-yellow-100 rounded-full p-1'
    case 'LOW': return 'text-blue-600 bg-blue-100 rounded-full p-1'
    default: return 'text-gray-600 bg-gray-100 rounded-full p-1'
  }
}

const getInsightVariant = (priority: string) => {
  switch (priority) {
    case 'HIGH': return 'destructive'
    case 'MEDIUM': return 'outline'
    case 'LOW': return 'secondary'
    default: return 'outline'
  }
}

// Initialize
onMounted(async () => {
  await loadData()
})
</script>