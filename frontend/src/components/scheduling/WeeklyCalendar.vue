<template>
  <div class="w-full bg-white rounded-lg border">
    <!-- Calendar Header -->
    <div class="flex items-center justify-between p-4 border-b">
      <div class="flex items-center gap-4">
        <Button variant="outline" size="icon" @click="previousWeek">
          <ChevronLeft class="h-4 w-4" />
        </Button>
        <h2 class="text-lg font-semibold">{{ currentWeekRange }}</h2>
        <Button variant="outline" size="icon" @click="nextWeek">
          <ChevronRight class="h-4 w-4" />
        </Button>
        <Button variant="outline" @click="currentWeek">
          Today
        </Button>
      </div>
      <div class="flex items-center gap-2">
        <Button variant="outline" size="sm">
          <Filter class="h-4 w-4 mr-2" />
          Filter
        </Button>
        <Select v-model="viewMode">
          <option value="week">Week</option>
          <option value="day">Day</option>
          <option value="month">Month</option>
        </Select>
      </div>
    </div>

    <!-- Calendar Grid -->
    <div class="p-4">
      <!-- Time Column Headers -->
      <div class="grid grid-cols-8 gap-1 mb-2">
        <div class="text-sm font-medium text-muted-foreground p-2">Time</div>
        <div
          v-for="day in weekDays"
          :key="day.date"
          class="text-center"
        >
          <div class="text-sm font-medium">{{ day.day }}</div>
          <div class="text-xs text-muted-foreground">{{ day.date }}</div>
        </div>
      </div>

      <!-- Calendar Body -->
      <div class="grid grid-cols-8 gap-1">
        <!-- Time Slots -->
        <div class="space-y-1">
          <div
            v-for="timeSlot in timeSlots"
            :key="timeSlot"
            class="text-xs text-muted-foreground p-2 h-16 border-r"
          >
            {{ timeSlot }}
          </div>
        </div>

        <!-- Day Columns -->
        <div
          v-for="day in weekDays"
          :key="day.date"
          class="space-y-1"
        >
          <div
            v-for="timeSlot in timeSlots"
            :key="`${day.date}-${timeSlot}`"
            class="relative h-16 border rounded hover:bg-muted/50 cursor-pointer"
            @click="handleSlotClick(day, timeSlot)"
          >
            <!-- Schedule Items -->
            <div
              v-for="schedule in getSchedulesForSlot(day.date, timeSlot)"
              :key="schedule.id"
              :class="scheduleItemClasses(schedule)"
              class="absolute inset-x-1 top-1 rounded p-1 text-xs overflow-hidden"
              @click.stop="handleScheduleClick(schedule)"
            >
              <div class="font-medium truncate">{{ schedule.courseName }}</div>
              <div class="text-xs opacity-90 truncate">{{ schedule.teacherName }}</div>
              <div class="text-xs opacity-75 truncate">{{ schedule.classroom }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Legend -->
    <div class="border-t p-4">
      <div class="flex items-center gap-4 text-sm">
        <span class="font-medium">Legend:</span>
        <div class="flex items-center gap-2">
          <div class="w-3 h-3 bg-blue-500 rounded"></div>
          <span>Scheduled</span>
        </div>
        <div class="flex items-center gap-2">
          <div class="w-3 h-3 bg-red-500 rounded"></div>
          <span>Conflict</span>
        </div>
        <div class="flex items-center gap-2">
          <div class="w-3 h-3 bg-yellow-500 rounded"></div>
          <span>Pending</span>
        </div>
        <div class="flex items-center gap-2">
          <div class="w-3 h-3 bg-green-500 rounded"></div>
          <span>Confirmed</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { startOfWeek, endOfWeek, addWeeks, subWeeks, format, eachDayOfInterval, parseISO } from 'date-fns'
import Button from '@/components/ui/Button.vue'
import Select from '@/components/ui/Select.vue'
import { ChevronLeft, ChevronRight, Filter } from 'lucide-vue-next'

interface ScheduleItem {
  id: string
  courseName: string
  teacherName: string
  classroom: string
  startTime: string
  endTime: string
  status: 'scheduled' | 'conflict' | 'pending' | 'confirmed'
}

const currentWeekStart = ref(new Date())
const viewMode = ref('week')
const schedules = ref<ScheduleItem[]>([])

const emit = defineEmits<{
  'slot-click': [day: string, time: string]
  'schedule-click': [schedule: ScheduleItem]
}>()

const timeSlots = [
  '08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00'
]

const weekDays = computed(() => {
  const start = startOfWeek(currentWeekStart.value, { weekStartsOn: 1 }) // Monday
  const end = endOfWeek(currentWeekStart.value, { weekStartsOn: 1 })
  const days = eachDayOfInterval({ start, end })

  return days.map(day => ({
    day: format(day, 'EEE'),
    date: format(day, 'MM/dd'),
    fullDate: format(day, 'yyyy-MM-dd')
  }))
})

const currentWeekRange = computed(() => {
  const start = startOfWeek(currentWeekStart.value, { weekStartsOn: 1 })
  const end = endOfWeek(currentWeekStart.value, { weekStartsOn: 1 })
  return `${format(start, 'MMM dd')} - ${format(end, 'MMM dd, yyyy')}`
})

const previousWeek = () => {
  currentWeekStart.value = subWeeks(currentWeekStart.value, 1)
}

const nextWeek = () => {
  currentWeekStart.value = addWeeks(currentWeekStart.value, 1)
}

const currentWeek = () => {
  currentWeekStart.value = new Date()
}

const handleSlotClick = (day: any, timeSlot: string) => {
  emit('slot-click', day.fullDate, timeSlot)
}

const handleScheduleClick = (schedule: ScheduleItem) => {
  emit('schedule-click', schedule)
}

const getSchedulesForSlot = (date: string, timeSlot: string) => {
  return schedules.value.filter(schedule => {
    const scheduleDate = format(parseISO(schedule.startTime), 'yyyy-MM-dd')
    const scheduleTime = format(parseISO(schedule.startTime), 'HH:mm')
    return scheduleDate === date && scheduleTime === timeSlot
  })
}

const scheduleItemClasses = (schedule: ScheduleItem) => {
  const baseClasses = 'text-white'

  switch (schedule.status) {
    case 'scheduled':
      return `${baseClasses} bg-blue-500`
    case 'conflict':
      return `${baseClasses} bg-red-500`
    case 'pending':
      return `${baseClasses} bg-yellow-500`
    case 'confirmed':
      return `${baseClasses} bg-green-500`
    default:
      return `${baseClasses} bg-gray-500`
  }
}

const loadSampleData = () => {
  schedules.value = [
    {
      id: '1',
      courseName: 'CS101 Introduction to Programming',
      teacherName: 'Dr. Smith',
      classroom: 'Room 101',
      startTime: '2024-01-15T09:00:00',
      endTime: '2024-01-15T10:00:00',
      status: 'confirmed'
    },
    {
      id: '2',
      courseName: 'MATH201 Calculus I',
      teacherName: 'Prof. Johnson',
      classroom: 'Room 205',
      startTime: '2024-01-15T10:00:00',
      endTime: '2024-01-15T11:00:00',
      status: 'scheduled'
    },
    {
      id: '3',
      courseName: 'PHYS150 Physics I',
      teacherName: 'Dr. Williams',
      classroom: 'Lab A',
      startTime: '2024-01-15T14:00:00',
      endTime: '2024-01-15T16:00:00',
      status: 'conflict'
    }
  ]
}

onMounted(() => {
  loadSampleData()
})
</script>