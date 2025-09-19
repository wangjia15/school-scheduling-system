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
          <Button variant="outline" @click="refreshData">
            <RefreshCw class="h-4 w-4 mr-2" />
            Refresh
          </Button>
          <Button @click="generateSchedule">
            <Calendar class="h-4 w-4 mr-2" />
            Generate Schedule
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
      <div class="grid gap-6 md:grid-cols-2">
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
                    <p class="text-sm font-medium">{{ conflict.title }}</p>
                    <p class="text-xs text-muted-foreground">{{ conflict.time }}</p>
                  </div>
                </div>
                <Badge :variant="conflictStatusVariant(conflict.status)">
                  {{ conflict.status }}
                </Badge>
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
import { ref, onMounted } from 'vue'
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
import {
  Users,
  BookOpen,
  DoorOpen,
  AlertTriangle,
  RefreshCw,
  Calendar,
  Download
} from 'lucide-vue-next'

const router = useRouter()
const toast = useToast()

const stats = ref({
  totalTeachers: 156,
  newTeachersThisMonth: 12,
  activeCourses: 89,
  coursesThisSemester: 76,
  totalClassrooms: 45,
  availableClassrooms: 38,
  activeConflicts: 8,
  newConflictsToday: 3
})

const scheduleStats = ref({
  totalClasses: 342,
  scheduledClasses: 298,
  unscheduledClasses: 44,
  generatedPercentage: 87
})

const recentConflicts = ref([
  {
    id: 1,
    title: 'Teacher double-booked: Prof. Smith',
    time: 'Monday 10:00 AM',
    severity: 'high',
    status: 'open'
  },
  {
    id: 2,
    title: 'Room capacity exceeded: CS101',
    time: 'Tuesday 2:00 PM',
    severity: 'medium',
    status: 'in-progress'
  },
  {
    id: 3,
    title: 'Prerequisite conflict: MATH201',
    time: 'Wednesday 11:00 AM',
    severity: 'low',
    status: 'resolved'
  }
])

const refreshData = () => {
  toast.info('Refreshing dashboard data...')
  // Implement data refresh
}

const generateSchedule = () => {
  toast.info('Starting schedule generation...')
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

const exportSchedule = () => {
  toast.success('Schedule export started')
}

const conflictSeverityClasses = (severity: string) => {
  switch (severity) {
    case 'high':
      return 'text-red-500'
    case 'medium':
      return 'text-yellow-500'
    case 'low':
      return 'text-blue-500'
    default:
      return 'text-gray-500'
  }
}

const conflictStatusVariant = (status: string) => {
  switch (status) {
    case 'open':
      return 'destructive'
    case 'in-progress':
      return 'secondary'
    case 'resolved':
      return 'default'
    default:
      return 'outline'
  }
}

onMounted(() => {
  // Load dashboard data
  console.log('Admin dashboard mounted')
})
</script>