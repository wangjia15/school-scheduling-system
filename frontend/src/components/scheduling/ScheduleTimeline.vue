<template>
  <div class="w-full bg-white rounded-lg border">
    <!-- Header -->
    <div class="flex items-center justify-between p-4 border-b">
      <h3 class="text-lg font-semibold">Schedule Timeline</h3>
      <div class="flex items-center gap-2">
        <Button variant="outline" size="sm">
          <Filter class="h-4 w-4 mr-2" />
          Filter
        </Button>
        <Select v-model="timelineView" class="w-32">
          <option value="teacher">By Teacher</option>
          <option value="classroom">By Classroom</option>
          <option value="course">By Course</option>
        </Select>
      </div>
    </div>

    <!-- Timeline Content -->
    <div class="p-4">
      <!-- Time Header -->
      <div class="flex border-b pb-2 mb-2">
        <div class="w-48 flex-shrink-0"></div>
        <div class="flex-1 grid grid-cols-12 gap-1">
          <div
            v-for="hour in hours"
            :key="hour"
            class="text-xs text-center text-muted-foreground"
          >
            {{ hour }}
          </div>
        </div>
      </div>

      <!-- Timeline Items -->
      <div class="space-y-2">
        <div
          v-for="item in timelineItems"
          :key="item.id"
          class="flex items-center"
        >
          <!-- Entity Label -->
          <div class="w-48 flex-shrink-0 pr-4">
            <div class="font-medium text-sm">{{ item.name }}</div>
            <div class="text-xs text-muted-foreground">{{ item.subtitle }}</div>
          </div>

          <!-- Timeline Bars -->
          <div class="flex-1 relative h-8">
            <div class="absolute inset-0 grid grid-cols-12 gap-1">
              <div
                v-for="hour in hours"
                :key="hour"
                class="border-r border-muted"
              ></div>
            </div>

            <!-- Schedule Blocks -->
            <div
              v-for="schedule in item.schedules"
              :key="schedule.id"
              :class="scheduleBlockClasses(schedule.status)"
              :style="{
                left: `${calculatePosition(schedule.startTime)}%`,
                width: `${calculateWidth(schedule.startTime, schedule.endTime)}%`
              }"
              class="absolute top-1 bottom-1 rounded px-2 py-1 text-xs text-white cursor-pointer hover:opacity-90 transition-opacity"
              @click="handleScheduleClick(schedule)"
            >
              <div class="truncate font-medium">{{ schedule.courseName }}</div>
              <div class="truncate opacity-90">{{ schedule.location }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Button from '@/components/ui/Button.vue'
import Select from '@/components/ui/Select.vue'
import { Filter } from 'lucide-vue-next'

interface ScheduleBlock {
  id: string
  courseName: string
  location: string
  startTime: string
  endTime: string
  status: 'scheduled' | 'conflict' | 'confirmed'
}

interface TimelineItem {
  id: string
  name: string
  subtitle: string
  schedules: ScheduleBlock[]
}

const timelineView = ref('teacher')

const emit = defineEmits<{
  'schedule-click': [schedule: ScheduleBlock]
}>()

const hours = [
  '08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00'
]

const timelineItems = ref<TimelineItem[]>([
  {
    id: '1',
    name: 'Dr. Smith',
    subtitle: 'Computer Science',
    schedules: [
      {
        id: 's1',
        courseName: 'CS101',
        location: 'Room 101',
        startTime: '09:00',
        endTime: '10:00',
        status: 'confirmed'
      },
      {
        id: 's2',
        courseName: 'CS201',
        location: 'Room 205',
        startTime: '14:00',
        endTime: '16:00',
        status: 'scheduled'
      }
    ]
  },
  {
    id: '2',
    name: 'Prof. Johnson',
    subtitle: 'Mathematics',
    schedules: [
      {
        id: 's3',
        courseName: 'MATH201',
        location: 'Room 301',
        startTime: '10:00',
        endTime: '11:00',
        status: 'confirmed'
      },
      {
        id: 's4',
        courseName: 'MATH301',
        location: 'Room 302',
        startTime: '15:00',
        endTime: '17:00',
        status: 'conflict'
      }
    ]
  },
  {
    id: '3',
    name: 'Dr. Williams',
    subtitle: 'Physics',
    schedules: [
      {
        id: 's5',
        courseName: 'PHYS150',
        location: 'Lab A',
        startTime: '11:00',
        endTime: '13:00',
        status: 'scheduled'
      }
    ]
  }
])

const calculatePosition = (time: string): number => {
  const [hours, minutes] = time.split(':').map(Number)
  const totalMinutes = (hours - 8) * 60 + minutes
  const totalDayMinutes = 12 * 60 // 8 AM to 8 PM
  return (totalMinutes / totalDayMinutes) * 100
}

const calculateWidth = (startTime: string, endTime: string): number => {
  const startMinutes = calculatePosition(startTime)
  const endMinutes = calculatePosition(endTime)
  return Math.max(endMinutes - startMinutes, 5) // Minimum width of 5%
}

const handleScheduleClick = (schedule: ScheduleBlock) => {
  emit('schedule-click', schedule)
}

const scheduleBlockClasses = (status: string) => {
  switch (status) {
    case 'confirmed':
      return 'bg-green-500'
    case 'scheduled':
      return 'bg-blue-500'
    case 'conflict':
      return 'bg-red-500'
    default:
      return 'bg-gray-500'
  }
}
</script>