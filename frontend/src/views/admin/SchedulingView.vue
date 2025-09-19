<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">Scheduling</h1>
          <p class="text-muted-foreground">
            Manage and generate course schedules
          </p>
        </div>
        <div class="flex items-center gap-2">
          <Select v-model="currentSemester" @change="handleSemesterChange" class="w-40">
            <option v-for="option in semesterOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </Select>
          <Select v-model="currentAcademicYear" @change="handleSemesterChange" class="w-32">
            <option v-for="option in academicYearOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </Select>
          <Button variant="outline">
            <Filter class="h-4 w-4 mr-2" />
            Filters
          </Button>
          <Button @click="generateSchedule">
            <Play class="h-4 w-4 mr-2" />
            Generate Schedule
          </Button>
        </div>
      </div>

      <!-- Stats Overview -->
      <div class="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Total Schedules</CardTitle>
            <Calendar class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ scheduleStats.totalSchedules }}</div>
            <p class="text-xs text-muted-foreground">
              {{ scheduleStats.confirmedSchedules }} confirmed
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Conflicts</CardTitle>
            <AlertTriangle class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ scheduleStats.conflictsCount }}</div>
            <p class="text-xs text-muted-foreground">
              Need resolution
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Utilization</CardTitle>
            <BarChart3 class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ scheduleStats.averageUtilizationRate }}%</div>
            <p class="text-xs text-muted-foreground">
              Average rate
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Resources</CardTitle>
            <Settings class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ scheduleStats.totalTeachersUtilized }}/{{ scheduleStats.totalClassroomsUtilized }}</div>
            <p class="text-xs text-muted-foreground">
              Teachers/Classrooms
            </p>
          </CardContent>
        </Card>
      </div>

      <!-- Weekly Calendar -->
      <Card>
        <CardHeader>
          <div class="flex items-center justify-between">
            <div>
              <CardTitle>Weekly Schedule</CardTitle>
              <CardDescription>
                View and manage the weekly schedule calendar
              </CardDescription>
            </div>
            <div class="flex items-center gap-2">
              <Button variant="outline" size="sm" @click="exportSchedule">
                <Download class="h-4 w-4 mr-2" />
                Export
              </Button>
              <Button variant="outline" size="sm" @click="showScheduleFormModal = true">
                <Plus class="h-4 w-4 mr-2" />
                Add Schedule
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <WeeklyCalendar
            @slot-click="handleSlotClick"
            @schedule-click="handleScheduleClick"
          />
        </CardContent>
      </Card>

      <!-- Timeline View -->
      <Card>
        <CardHeader>
          <CardTitle>Schedule Timeline</CardTitle>
          <CardDescription>
            View schedules by teacher, classroom, or course
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ScheduleTimeline @schedule-click="handleScheduleClick" />
        </CardContent>
      </Card>
    </div>
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
import Badge from '@/components/ui/Badge.vue'
import Select from '@/components/ui/Select.vue'
import { Calendar, Filter, Play, Settings, BarChart3, AlertTriangle, Download, Plus } from 'lucide-vue-next'
import WeeklyCalendar from '@/components/scheduling/WeeklyCalendar.vue'
import ScheduleTimeline from '@/components/scheduling/ScheduleTimeline.vue'
import schedulingService, { type Schedule, type ScheduleStats } from '@/services/schedulingService'

const router = useRouter()
const toast = useToast()

// State
const currentSemester = ref('FALL_2024')
const currentAcademicYear = ref('2024-2025')
const schedules = ref<Schedule[]>([])
const selectedSlot = ref<{ day: string; time: string } | null>(null)
const selectedSchedule = ref<Schedule | null>(null)
const showScheduleFormModal = ref(false)
const showScheduleDetailModal = ref(false)
const showScheduleGenerationModal = ref(false)
const loading = ref(false)

// Stats
const scheduleStats = ref<ScheduleStats>({
  totalSchedules: 0,
  scheduledSchedules: 0,
  confirmedSchedules: 0,
  cancelledSchedules: 0,
  totalTeachersUtilized: 0,
  totalClassroomsUtilized: 0,
  averageUtilizationRate: 0,
  conflictsCount: 0
})

// Computed
const semesterOptions = [
  { value: 'FALL_2024', label: 'Fall 2024' },
  { value: 'SPRING_2024', label: 'Spring 2024' },
  { value: 'SUMMER_2024', label: 'Summer 2024' },
  { value: 'FALL_2023', label: 'Fall 2023' }
]

const academicYearOptions = [
  { value: '2024-2025', label: '2024-2025' },
  { value: '2023-2024', label: '2023-2024' },
  { value: '2022-2023', label: '2022-2023' }
]

// Methods
const loadScheduleStats = async () => {
  try {
    const stats = await schedulingService.getScheduleStats(currentSemester.value, currentAcademicYear.value)
    scheduleStats.value = stats
  } catch (error) {
    toast.error('Failed to load schedule statistics')
    console.error('Error loading schedule stats:', error)
  }
}

const loadSchedules = async () => {
  try {
    const response = await schedulingService.getSchedules({
      semester: currentSemester.value,
      academicYear: currentAcademicYear.value,
      size: 100
    })
    schedules.value = response.content
  } catch (error) {
    toast.error('Failed to load schedules')
    console.error('Error loading schedules:', error)
  }
}

const handleSemesterChange = async () => {
  await Promise.all([loadScheduleStats(), loadSchedules()])
}

const exportSchedule = () => {
  // Export schedule functionality
  toast.info('Schedule export started')
}

const generateSchedule = () => {
  showScheduleGenerationModal.value = true
}

const handleSlotClick = (day: string, time: string) => {
  selectedSlot.value = { day, time }
  showScheduleFormModal.value = true
}

const handleScheduleClick = (schedule: any) => {
  selectedSchedule.value = schedule
  showScheduleDetailModal.value = true
}

// Initialize
onMounted(async () => {
  await Promise.all([loadScheduleStats(), loadSchedules()])
})
</script>