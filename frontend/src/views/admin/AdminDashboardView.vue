<template>
  <AdminLayout>
    <div class="space-y-6">
      <!-- Page Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">Dashboard</h1>
          <p class="text-muted-foreground">
            Welcome back! Here's what's happening with your scheduling system.
          </p>
        </div>
        <div class="flex items-center gap-2">
          <div class="flex items-center gap-2 text-sm text-muted-foreground">
            <component :is="isConnected ? Wifi : WifiOff" class="h-4 w-4" :class="isConnected ? 'text-green-500' : 'text-red-500'" />
            <span>{{ isConnected ? 'Connected' : 'Disconnected' }}</span>
            <span v-if="lastUpdate">• Updated {{ lastUpdate.toLocaleTimeString() }}</span>
          </div>
          <Button variant="outline" @click="refreshData" :disabled="loading">
            <RefreshCw class="h-4 w-4 mr-2" :class="{ 'animate-spin': loading }" />
            Refresh
          </Button>
          <Button @click="generateSchedule">
            <Calendar class="h-4 w-4 mr-2" />
            Generate Schedule
          </Button>
          <Button variant="outline" @click="toggleNotifications" class="relative">
            <Bell class="h-4 w-4 mr-2" />
            Notifications
            <Badge v-if="notifications.length > 0" class="absolute -top-2 -right-2 h-5 w-5 p-0 text-xs">
              {{ notifications.length }}
            </Badge>
          </Button>
        </div>
      </div>

      <!-- Stats Cards -->
      <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Total Teachers</CardTitle>
            <Users class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ stats.totalTeachers }}</div>
            <p class="text-xs text-muted-foreground">
              +{{ stats.newTeachersThisMonth }} from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Active Courses</CardTitle>
            <BookOpen class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ stats.activeCourses }}</div>
            <p class="text-xs text-muted-foreground">
              {{ stats.coursesThisSemester }} this semester
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Classrooms</CardTitle>
            <DoorOpen class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ stats.totalClassrooms }}</div>
            <p class="text-xs text-muted-foreground">
              {{ stats.availableClassrooms }} available now
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Conflicts</CardTitle>
            <AlertTriangle class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ stats.activeConflicts }}</div>
            <p class="text-xs text-muted-foreground">
              +{{ stats.newConflictsToday }} today
            </p>
          </CardContent>
        </Card>
      </div>

      <!-- Main Content Grid -->
      <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        <!-- Recent Conflicts -->
        <Card>
          <CardHeader>
            <CardTitle>Recent Conflicts</CardTitle>
            <CardDescription>
              Latest scheduling conflicts that need attention
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="space-y-4">
              <div
                v-for="conflict in recentConflicts"
                :key="conflict.id"
                class="flex items-center justify-between p-3 border rounded-lg"
              >
                <div class="flex items-center gap-3">
                  <div :class="conflictSeverityClasses(conflict.severity)">
                    <AlertTriangle class="h-4 w-4" />
                  </div>
                  <div>
                    <p class="text-sm font-medium">{{ conflict.description }}</p>
                    <p class="text-xs text-muted-foreground">{{ new Date(conflict.createdAt).toLocaleString() }}</p>
                  </div>
                </div>
                <Badge :variant="conflictStatusVariant(conflict.status)">
                  {{ conflict.status }}
                </Badge>
              </div>
              <div v-if="recentConflicts.length === 0" class="text-center py-4 text-muted-foreground">
                No conflicts found
              </div>
            </div>
            <div class="mt-4">
              <Button variant="outline" class="w-full" @click="viewAllConflicts">
                View All Conflicts
              </Button>
            </div>
          </CardContent>
        </Card>

        <!-- Schedule Overview -->
        <Card>
          <CardHeader>
            <CardTitle>Schedule Overview</CardTitle>
            <CardDescription>
              Current semester schedule status
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="space-y-4">
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium">Schedule Generated</span>
                <Badge variant="secondary">{{ scheduleStats.generatedPercentage }}%</Badge>
              </div>
              <div class="w-full bg-secondary rounded-full h-2">
                <div
                  class="bg-primary h-2 rounded-full transition-all duration-300"
                  :style="{ width: scheduleStats.generatedPercentage + '%' }"
                ></div>
              </div>

              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-muted-foreground">Total Classes</span>
                  <span class="font-medium">{{ scheduleStats.totalClasses }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-muted-foreground">Scheduled Classes</span>
                  <span class="font-medium">{{ scheduleStats.scheduledClasses }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-muted-foreground">Unscheduled Classes</span>
                  <span class="font-medium text-orange-600">{{ scheduleStats.unscheduledClasses }}</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- Recent Activity -->
        <Card>
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
            <CardDescription>
              Latest system activities and user actions
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="space-y-4">
              <div
                v-for="activity in recentActivities"
                :key="activity.id"
                class="flex items-start gap-3 p-3 border rounded-lg"
              >
                <Activity class="h-4 w-4 text-blue-500 mt-0.5" />
                <div class="flex-1">
                  <p class="text-sm font-medium">{{ activity.action }}</p>
                  <p class="text-xs text-muted-foreground">
                    {{ activity.user }} • {{ activity.timestamp }}
                  </p>
                  <p v-if="activity.details" class="text-xs text-muted-foreground mt-1">
                    {{ activity.details }}
                  </p>
                </div>
              </div>
              <div v-if="recentActivities.length === 0" class="text-center py-4 text-muted-foreground">
                No recent activity
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- Quick Actions -->
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>
            Common administrative tasks
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Button variant="outline" class="h-20 flex-col" @click="navigateToTeachers">
              <Users class="h-6 w-6 mb-2" />
              Manage Teachers
            </Button>
            <Button variant="outline" class="h-20 flex-col" @click="navigateToCourses">
              <BookOpen class="h-6 w-6 mb-2" />
              Manage Courses
            </Button>
            <Button variant="outline" class="h-20 flex-col" @click="navigateToClassrooms">
              <DoorOpen class="h-6 w-6 mb-2" />
              Manage Classrooms
            </Button>
            <Button variant="outline" class="h-20 flex-col" @click="exportSchedule">
              <Download class="h-6 w-6 mb-2" />
              Export Schedule
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import { websocketService } from '@/services/websocketService'
import teacherService from '@/services/teacherService'
import courseService from '@/services/courseService'
import classroomService from '@/services/classroomService'
import schedulingService from '@/services/schedulingService'
import conflictService, { type Conflict } from '@/services/conflictService'
import {
  Users,
  BookOpen,
  DoorOpen,
  AlertTriangle,
  RefreshCw,
  Calendar,
  Download,
  Wifi,
  WifiOff,
  Activity,
  Bell,
  TrendingUp,
  Clock
} from 'lucide-vue-next'

const router = useRouter()
const toast = useToast()

// State
const loading = ref(false)
const isConnected = ref(false)
const lastUpdate = ref<Date | null>(null)
const notifications = ref<Array<{ id: number; message: string; timestamp: string; severity: 'info' | 'warning' | 'error' | 'success' }>>([])

// Stats
const stats = ref({
  totalTeachers: 0,
  newTeachersThisMonth: 0,
  activeCourses: 0,
  coursesThisSemester: 0,
  totalClassrooms: 0,
  availableClassrooms: 0,
  activeConflicts: 0,
  newConflictsToday: 0,
  systemHealth: 100,
  activeUsers: 0
})

const scheduleStats = ref({
  totalClasses: 0,
  scheduledClasses: 0,
  unscheduledClasses: 0,
  generatedPercentage: 0,
  currentSemester: 'FALL_2024',
  currentAcademicYear: '2024-2025'
})

const recentConflicts = ref<Conflict[]>([])
const recentActivities = ref<Array<{
  id: number
  action: string
  user: string
  timestamp: string
  details: string
}>([])

// Load dashboard data
const loadDashboardData = async () => {
  if (loading.value) return

  loading.value = true
  try {
    // Load basic stats
    const [teachers, courses, classrooms, schedules, conflicts] = await Promise.all([
      teacherService.getTeachers({ size: 1 }),
      courseService.getCourses({ size: 1 }),
      classroomService.getClassrooms({ size: 1 }),
      schedulingService.getScheduleStats(scheduleStats.value.currentSemester, scheduleStats.value.currentAcademicYear),
      conflictService.getConflicts({ size: 5, sort: 'createdAt,desc' })
    ])

    // Update stats
    stats.value = {
      ...stats.value,
      totalTeachers: teachers.totalElements || 0,
      activeCourses: courses.totalElements || 0,
      totalClassrooms: classrooms.totalElements || 0,
      activeConflicts: conflicts.totalElements || 0
    }

    // Update schedule stats
    if (schedules) {
      scheduleStats.value = {
        ...scheduleStats.value,
        totalClasses: schedules.totalSchedules || 0,
        scheduledClasses: schedules.scheduledSchedules || 0,
        unscheduledClasses: (schedules.totalSchedules || 0) - (schedules.scheduledSchedules || 0),
        generatedPercentage: schedules.totalSchedules > 0
          ? Math.round(((schedules.scheduledSchedules || 0) / schedules.totalSchedules) * 100)
          : 0
      }
    }

    // Update recent conflicts
    recentConflicts.value = conflicts.content || []

    lastUpdate.value = new Date()
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
    toast.error('Failed to load dashboard data')
  } finally {
    loading.value = false
  }
}

// WebSocket event handlers
const handleScheduleUpdate = (message: any) => {
  addNotification('Schedule updated', 'info')
  loadDashboardData()
}

const handleTeacherUpdate = (message: any) => {
  addNotification('Teacher information updated', 'info')
  loadDashboardData()
}

const handleClassroomUpdate = (message: any) => {
  addNotification('Classroom information updated', 'info')
  loadDashboardData()
}

const handleSystemNotification = (message: any) => {
  addNotification(message.payload.title, message.payload.severity.toLowerCase() as any)
}

const handleUserActivity = (message: any) => {
  // Add to recent activities
  recentActivities.value.unshift({
    id: Date.now(),
    action: message.payload.action,
    user: message.payload.username,
    timestamp: new Date().toLocaleTimeString(),
    details: message.payload.details || ''
  })

  // Keep only last 10 activities
  if (recentActivities.value.length > 10) {
    recentActivities.value = recentActivities.value.slice(0, 10)
  }
}

const addNotification = (message: string, severity: 'info' | 'warning' | 'error' | 'success') => {
  const notification = {
    id: Date.now(),
    message,
    timestamp: new Date().toLocaleTimeString(),
    severity
  }

  notifications.value.unshift(notification)

  // Keep only last 20 notifications
  if (notifications.value.length > 20) {
    notifications.value = notifications.value.slice(0, 20)
  }

  // Show toast for important notifications
  if (severity === 'error') {
    toast.error(message)
  } else if (severity === 'warning') {
    toast.warning(message)
  }
}

// Actions
const refreshData = () => {
  loadDashboardData()
  toast.info('Dashboard data refreshed')
}

const generateSchedule = () => {
  router.push('/admin/scheduling')
}

const viewAllConflicts = () => {
  router.push('/admin/conflicts')
}

const navigateToTeachers = () => {
  router.push('/admin/teachers')
}

const navigateToCourses = () => {
  router.push('/admin/courses')
}

const navigateToClassrooms = () => {
  router.push('/admin/classrooms')
}

const exportSchedule = async () => {
  try {
    // This would use the report service to generate an export
    toast.success('Schedule export started')
  } catch (error) {
    toast.error('Failed to export schedule')
  }
}

const toggleNotifications = () => {
  // Show/hide notifications panel
}

// Utility functions
const conflictSeverityClasses = (severity: string) => {
  switch (severity) {
    case 'HIGH':
      return 'text-red-500'
    case 'MEDIUM':
      return 'text-yellow-500'
    case 'LOW':
      return 'text-blue-500'
    default:
      return 'text-gray-500'
  }
}

const conflictStatusVariant = (status: string) => {
  switch (status) {
    case 'OPEN':
      return 'destructive'
    case 'IN_PROGRESS':
      return 'secondary'
    case 'RESOLVED':
      return 'default'
    default:
      return 'outline'
  }
}

const notificationIcon = (severity: string) => {
  switch (severity) {
    case 'error':
      return AlertTriangle
    case 'warning':
      return AlertTriangle
    case 'success':
      return Activity
    default:
      return Bell
  }
}

const notificationColor = (severity: string) => {
  switch (severity) {
    case 'error':
      return 'text-red-500'
    case 'warning':
      return 'text-yellow-500'
    case 'success':
      return 'text-green-500'
    default:
      return 'text-blue-500'
  }
}

// Lifecycle
let unsubscribeSchedule: (() => void) | null = null
let unsubscribeTeacher: (() => void) | null = null
let unsubscribeClassroom: (() => void) | null = null
let unsubscribeSystem: (() => void) | null = null
let unsubscribeActivity: (() => void) | null = null

onMounted(async () => {
  // Load initial data
  await loadDashboardData()

  // Setup WebSocket subscriptions
  unsubscribeSchedule = websocketService.subscribeToScheduleUpdates(handleScheduleUpdate)
  unsubscribeTeacher = websocketService.subscribeToTeacherUpdates(handleTeacherUpdate)
  unsubscribeClassroom = websocketService.subscribeToClassroomUpdates(handleClassroomUpdate)
  unsubscribeSystem = websocketService.subscribeToSystemNotifications(handleSystemNotification)
  unsubscribeActivity = websocketService.subscribeToUserActivity(handleUserActivity)

  // Request real-time updates
  websocketService.requestScheduleUpdates(scheduleStats.value.currentSemester, scheduleStats.value.currentAcademicYear)
  websocketService.requestConflictUpdates()
  websocketService.requestTeacherUpdates()
  websocketService.requestClassroomUpdates()

  // Setup connection status monitoring
  isConnected.value = websocketService.connected

  // Auto-refresh every 30 seconds
  const refreshInterval = setInterval(loadDashboardData, 30000)

  // Store interval for cleanup
  const cleanup = () => {
    clearInterval(refreshInterval)
  }

  // Return cleanup function
  onUnmounted(() => {
    cleanup()

    // Cleanup WebSocket subscriptions
    if (unsubscribeSchedule) unsubscribeSchedule()
    if (unsubscribeTeacher) unsubscribeTeacher()
    if (unsubscribeClassroom) unsubscribeClassroom()
    if (unsubscribeSystem) unsubscribeSystem()
    if (unsubscribeActivity) unsubscribeActivity()
  })
})
</script>
</script>