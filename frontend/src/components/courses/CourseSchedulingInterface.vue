<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h2 class="text-2xl font-bold">Course Scheduling Interface</h2>
        <p class="text-muted-foreground">Plan and visualize course schedules with timeline management</p>
      </div>
      <div class="flex space-x-2">
        <Button @click="generateSchedule" :disabled="isGenerating">
          <Calendar class="w-4 h-4 mr-2" />
          {{ isGenerating ? 'Generating...' : 'Generate Schedule' }}
        </Button>
        <Button @click="saveSchedule" variant="outline">
          <Save class="w-4 h-4 mr-2" />
          Save Schedule
        </Button>
      </div>
    </div>

    <!-- Schedule Configuration -->
    <Card>
      <CardHeader>
        <CardTitle>Schedule Configuration</CardTitle>
        <CardDescription>Configure parameters for schedule generation</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label class="block text-sm font-medium mb-1">Semester</label>
            <SelectInput
              v-model="scheduleConfig.semester"
              :options="semesterOptions"
              @change="loadScheduleData"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Academic Year</label>
            <SelectInput
              v-model="scheduleConfig.year"
              :options="yearOptions"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Schedule Type</label>
            <SelectInput
              v-model="scheduleConfig.type"
              :options="scheduleTypeOptions"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Optimization Goal</label>
            <SelectInput
              v-model="scheduleConfig.optimization"
              :options="optimizationOptions"
            />
          </div>
        </div>

        <!-- Advanced Options -->
        <div class="mt-4">
          <Button
            @click="showAdvancedOptions = !showAdvancedOptions"
            variant="outline"
            size="sm"
          >
            <Settings class="w-4 h-4 mr-2" />
            {{ showAdvancedOptions ? 'Hide' : 'Show' }} Advanced Options
          </Button>
        </div>

        <div v-if="showAdvancedOptions" class="mt-4 grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-sm font-medium mb-1">Max Courses per Day</label>
            <Input
              v-model.number="scheduleConfig.maxCoursesPerDay"
              type="number"
              min="1"
              max="10"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Min Gap Between Classes (min)</label>
            <Input
              v-model.number="scheduleConfig.minGap"
              type="number"
              min="0"
              max="180"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Preferred Days</label>
            <div class="flex flex-wrap gap-2 mt-2">
              <label
                v-for="day in weekDays"
                :key="day.value"
                class="flex items-center space-x-1"
              >
                <input
                  type="checkbox"
                  :value="day.value"
                  v-model="scheduleConfig.preferredDays"
                  class="rounded"
                />
                <span class="text-sm">{{ day.label }}</span>
              </label>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Statistics Overview -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <Calendar class="w-5 h-5 text-blue-600" />
            <div>
              <p class="text-sm text-muted-foreground">Total Courses</p>
              <p class="text-2xl font-bold">{{ scheduleStats.totalCourses }}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <Clock class="w-5 h-5 text-green-600" />
            <div>
              <p class="text-sm text-muted-foreground">Total Hours</p>
              <p class="text-2xl font-bold">{{ scheduleStats.totalHours }}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <AlertTriangle class="w-5 h-5 text-orange-600" />
            <div>
              <p class="text-sm text-muted-foreground">Conflicts</p>
              <p class="text-2xl font-bold">{{ scheduleStats.conflicts }}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <TrendingUp class="w-5 h-5 text-purple-600" />
            <div>
              <p class="text-sm text-muted-foreground">Efficiency</p>
              <p class="text-2xl font-bold">{{ scheduleStats.efficiency }}%</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- View Toggle -->
    <div class="flex justify-between items-center">
      <div class="flex space-x-2">
        <Button
          @click="viewMode = 'timeline'"
          :variant="viewMode === 'timeline' ? 'default' : 'outline'"
          size="sm"
        >
          <CalendarDays class="w-4 h-4 mr-2" />
          Timeline
        </Button>
        <Button
          @click="viewMode = 'calendar'"
          :variant="viewMode === 'calendar' ? 'default' : 'outline'"
          size="sm"
        >
          <Calendar class="w-4 h-4 mr-2" />
          Calendar
        </Button>
        <Button
          @click="viewMode = 'grid'"
          :variant="viewMode === 'grid' ? 'default' : 'outline'"
          size="sm"
        >
          <Grid3X3 class="w-4 h-4 mr-2" />
          Grid
        </Button>
      </div>
      <div class="flex space-x-2">
        <Button @click="checkConflicts" variant="outline" size="sm">
          <Search class="w-4 h-4 mr-2" />
          Check Conflicts
        </Button>
        <Button @click="optimizeSchedule" variant="outline" size="sm">
          <Zap class="w-4 h-4 mr-2" />
          Optimize
        </Button>
      </div>
    </div>

    <!-- Timeline View -->
    <div v-if="viewMode === 'timeline'" class="border rounded-lg overflow-hidden">
      <div class="bg-muted p-4 border-b">
        <h3 class="font-semibold">Weekly Timeline</h3>
      </div>
      <div class="overflow-x-auto">
        <div class="min-w-full">
          <!-- Time Headers -->
          <div class="grid grid-cols-8 border-b">
            <div class="p-2 bg-muted font-medium text-sm">Time</div>
            <div v-for="day in weekDays" :key="day.value" class="p-2 bg-muted font-medium text-sm text-center">
              {{ day.label }}
            </div>
          </div>

          <!-- Time Slots -->
          <div v-for="timeSlot in timeSlots" :key="timeSlot.time" class="grid grid-cols-8 border-b">
            <div class="p-2 bg-muted text-sm font-medium">
              {{ timeSlot.time }}
            </div>
            <div v-for="day in weekDays" :key="day.value" class="p-2 min-h-[60px] border-l relative">
              <div
                v-if="getScheduledCourse(day.value, timeSlot.time)"
                class="absolute inset-1 bg-blue-100 border border-blue-300 rounded p-1 text-xs"
              >
                <div class="font-medium">{{ getScheduledCourse(day.value, timeSlot.time)?.courseCode }}</div>
                <div class="text-blue-600">{{ getScheduledCourse(day.value, timeSlot.time)?.title }}</div>
                <div class="text-blue-500">{{ getScheduledCourse(day.value, timeSlot.time)?.room }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Calendar View -->
    <div v-else-if="viewMode === 'calendar'" class="border rounded-lg overflow-hidden">
      <div class="bg-muted p-4 border-b flex justify-between items-center">
        <h3 class="font-semibold">Monthly Calendar</h3>
        <div class="flex space-x-2">
          <Button @click="previousMonth" variant="outline" size="sm">
            <ChevronLeft class="w-4 h-4" />
          </Button>
          <span class="font-medium">{{ currentMonth }}</span>
          <Button @click="nextMonth" variant="outline" size="sm">
            <ChevronRight class="w-4 h-4" />
          </Button>
        </div>
      </div>
      <div class="p-4">
        <div class="grid grid-cols-7 gap-1">
          <div v-for="day in ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']" :key="day" class="p-2 text-center font-medium text-sm bg-muted">
            {{ day }}
          </div>
          <div v-for="day in calendarDays" :key="day.date" class="min-h-[80px] border rounded p-1">
            <div class="text-sm font-medium mb-1">{{ day.day }}</div>
            <div v-if="day.courses.length > 0" class="space-y-1">
              <div
                v-for="course in day.courses"
                :key="course.id"
                class="text-xs bg-blue-100 rounded px-1 py-0.5"
              >
                {{ course.courseCode }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Grid View -->
    <div v-else-if="viewMode === 'grid'" class="border rounded-lg overflow-hidden">
      <div class="bg-muted p-4 border-b">
        <h3 class="font-semibold">Course Schedule Grid</h3>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-muted">
            <tr>
              <th class="text-left p-3">Course</th>
              <th v-for="day in weekDays" :key="day.value" class="text-left p-3">
                {{ day.label }}
              </th>
              <th class="text-left p-3">Room</th>
              <th class="text-left p-3">Instructor</th>
              <th class="text-left p-3">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="scheduledCourse in scheduledCourses" :key="scheduledCourse.id" class="border-t">
              <td class="p-3">
                <div class="font-medium">{{ scheduledCourse.courseCode }}</div>
                <div class="text-sm text-muted-foreground">{{ scheduledCourse.title }}</div>
              </td>
              <td v-for="day in weekDays" :key="day.value" class="p-3">
                <div v-if="getDaySchedule(scheduledCourse, day.value)">
                  {{ getDaySchedule(scheduledCourse, day.value)?.time }}
                </div>
              </td>
              <td class="p-3">{{ scheduledCourse.room }}</td>
              <td class="p-3">{{ scheduledCourse.instructor }}</td>
              <td class="p-3">
                <div class="flex space-x-1">
                  <Button variant="ghost" size="sm" @click="editSchedule(scheduledCourse)">
                    <Edit class="w-4 h-4" />
                  </Button>
                  <Button variant="ghost" size="sm" @click="removeSchedule(scheduledCourse)">
                    <Trash2 class="w-4 h-4" />
                  </Button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Course Assignment Panel -->
    <Card>
      <CardHeader>
        <CardTitle>Course Assignment</CardTitle>
        <CardDescription>Assign courses to time slots and rooms</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-sm font-medium mb-1">Select Course</label>
            <SelectInput
              v-model="assignment.courseId"
              :options="courseOptions"
              placeholder="Select a course"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Day</label>
            <SelectInput
              v-model="assignment.day"
              :options="dayOptions"
              placeholder="Select day"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Time</label>
            <SelectInput
              v-model="assignment.time"
              :options="timeSlotOptions"
              placeholder="Select time"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Room</label>
            <SelectInput
              v-model="assignment.room"
              :options="roomOptions"
              placeholder="Select room"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Instructor</label>
            <SelectInput
              v-model="assignment.instructor"
              :options="instructorOptions"
              placeholder="Select instructor"
            />
          </div>
          <div class="flex items-end">
            <Button @click="addCourseAssignment" class="w-full">
              <Plus class="w-4 h-4 mr-2" />
              Assign Course
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Conflict Detection Results -->
    <div v-if="conflicts.length > 0" class="border border-red-200 rounded-lg p-4 bg-red-50">
      <div class="flex items-center mb-2">
        <AlertTriangle class="w-5 h-5 text-red-600 mr-2" />
        <span class="font-medium text-red-800">Schedule Conflicts Detected</span>
      </div>
      <ul class="text-sm text-red-700 space-y-1">
        <li v-for="conflict in conflicts" :key="conflict.id" class="flex items-start">
          <span class="text-red-500 mr-2">•</span>
          {{ conflict.description }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import SelectInput from '@/components/ui/SelectInput.vue'
import { useToast } from 'vue-toastification'
import type { Course } from '@/types/course'
import {
  Calendar,
  Save,
  CalendarDays,
  Grid3X3,
  Search,
  Zap,
  Settings,
  Clock,
  AlertTriangle,
  TrendingUp,
  ChevronLeft,
  ChevronRight,
  Edit,
  Trash2,
  Plus
} from 'lucide-vue-next'

interface Props {
  courses: Course[]
  teachers: Array<{ id: number; name: string; email: string }>
  classrooms: Array<{ id: number; code: string; capacity: number; type: string }>
}

const props = defineProps<Props>()

const toast = useToast()

// State
const loading = ref(false)
const isGenerating = ref(false)
const viewMode = ref<'timeline' | 'calendar' | 'grid'>('timeline')
const showAdvancedOptions = ref(false)
const conflicts = ref<any[]>([])
const scheduledCourses = ref<any[]>([])

// Schedule Configuration
const scheduleConfig = reactive({
  semester: '2025-SPRING',
  year: '2025',
  type: 'regular',
  optimization: 'balanced',
  maxCoursesPerDay: 4,
  minGap: 30,
  preferredDays: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY']
})

// Course Assignment
const assignment = reactive({
  courseId: '',
  day: '',
  time: '',
  room: '',
  instructor: ''
})

// Calendar State
const currentDate = ref(new Date())

// Schedule Statistics
const scheduleStats = reactive({
  totalCourses: 0,
  totalHours: 0,
  conflicts: 0,
  efficiency: 0
})

// Time Slots
const timeSlots = [
  { time: '08:00', end: '09:00' },
  { time: '09:00', end: '10:00' },
  { time: '10:00', end: '11:00' },
  { time: '11:00', end: '12:00' },
  { time: '12:00', end: '13:00' },
  { time: '13:00', end: '14:00' },
  { time: '14:00', end: '15:00' },
  { time: '15:00', end: '16:00' },
  { time: '16:00', end: '17:00' },
  { time: '17:00', end: '18:00' },
  { time: '18:00', end: '19:00' },
  { time: '19:00', end: '20:00' }
]

const weekDays = [
  { value: 'MONDAY', label: 'Monday' },
  { value: 'TUESDAY', label: 'Tuesday' },
  { value: 'WEDNESDAY', label: 'Wednesday' },
  { value: 'THURSDAY', label: 'Thursday' },
  { value: 'FRIDAY', label: 'Friday' },
  { value: 'SATURDAY', label: 'Saturday' },
  { value: 'SUNDAY', label: 'Sunday' }
]

// Options
const semesterOptions = [
  { value: '2025-SPRING', label: 'Spring 2025' },
  { value: '2025-SUMMER', label: 'Summer 2025' },
  { value: '2025-FALL', label: 'Fall 2025' }
]

const yearOptions = [
  { value: '2024', label: '2024' },
  { value: '2025', label: '2025' },
  { value: '2026', label: '2026' }
]

const scheduleTypeOptions = [
  { value: 'regular', label: 'Regular Semester' },
  { value: 'summer', label: 'Summer Session' },
  { value: 'intensive', label: 'Intensive Course' }
]

const optimizationOptions = [
  { value: 'balanced', label: 'Balanced Schedule' },
  { value: 'compact', label: 'Compact Days' },
  { value: 'spread', label: 'Spread Out' },
  { value: 'early', label: 'Early Classes' },
  { value: 'late', label: 'Late Classes' }
]

const courseOptions = computed(() => [
  { value: '', label: 'Select Course' },
  ...props.courses.map(course => ({
    value: course.id.toString(),
    label: `${course.courseCode} - ${course.title}`
  }))
])

const dayOptions = [
  { value: '', label: 'Select Day' },
  ...weekDays.map(day => ({ value: day.value, label: day.label }))
]

const timeSlotOptions = [
  { value: '', label: 'Select Time' },
  ...timeSlots.map(slot => ({ value: slot.time, label: slot.time }))
]

const roomOptions = computed(() => [
  { value: '', label: 'Select Room' },
  ...props.classrooms.map(room => ({
    value: room.code,
    label: `${room.code} (Capacity: ${room.capacity})`
  }))
])

const instructorOptions = computed(() => [
  { value: '', label: 'Select Instructor' },
  ...props.teachers.map(teacher => ({
    value: teacher.id.toString(),
    label: teacher.name
  }))
])

// Calendar Computed
const currentMonth = computed(() => {
  return currentDate.value.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
})

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const daysInMonth = lastDay.getDate()
  const startingDayOfWeek = firstDay.getDay()

  const days = []

  // Add empty cells for days before the first day of the month
  for (let i = 0; i < startingDayOfWeek; i++) {
    days.push({ day: '', date: '', courses: [] })
  }

  // Add days of the month
  for (let day = 1; day <= daysInMonth; day++) {
    const date = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    const courses = scheduledCourses.value.filter(course => {
      // Simple mock check - in real implementation, this would be more sophisticated
      return Math.random() > 0.7
    })
    days.push({ day, date, courses })
  }

  return days
})

// Methods
const loadScheduleData = async () => {
  try {
    loading.value = true
    // Mock schedule data - in real implementation, this would call an API
    scheduledCourses.value = [
      {
        id: 1,
        courseCode: 'CS101',
        title: 'Introduction to Computer Science',
        schedule: {
          MONDAY: { time: '09:00-10:00', room: 'CS101' },
          WEDNESDAY: { time: '09:00-10:00', room: 'CS101' },
          FRIDAY: { time: '09:00-10:00', room: 'CS101' }
        },
        instructor: 'Dr. Smith'
      },
      {
        id: 2,
        courseCode: 'MATH201',
        title: 'Calculus II',
        schedule: {
          TUESDAY: { time: '10:00-11:00', room: 'MATH201' },
          THURSDAY: { time: '10:00-11:00', room: 'MATH201' }
        },
        instructor: 'Dr. Johnson'
      }
    ]
    updateScheduleStats()
  } catch (error) {
    toast.error('Failed to load schedule data')
  } finally {
    loading.value = false
  }
}

const updateScheduleStats = () => {
  scheduleStats.totalCourses = scheduledCourses.value.length
  scheduleStats.totalHours = scheduledCourses.value.reduce((total, course) => {
    const hours = Object.values(course.schedule).length * 1 // 1 hour per session
    return total + hours
  }, 0)
  scheduleStats.conflicts = conflicts.value.length
  scheduleStats.efficiency = Math.min(100, Math.round((scheduleStats.totalHours / (scheduledCourses.value.length * 15)) * 100))
}

const getScheduledCourse = (day: string, time: string) => {
  const course = scheduledCourses.value.find(c => c.schedule[day]?.time.startsWith(time))
  if (course) {
    return {
      courseCode: course.courseCode,
      title: course.title,
      room: course.schedule[day].room
    }
  }
  return null
}

const getDaySchedule = (course: any, day: string) => {
  return course.schedule[day] || null
}

const generateSchedule = async () => {
  try {
    isGenerating.value = true
    // Mock schedule generation - in real implementation, this would call an optimization algorithm
    await new Promise(resolve => setTimeout(resolve, 2000))

    // Add some mock scheduled courses
    const newCourses = props.courses.slice(0, 3).map((course, index) => ({
      id: Date.now() + index,
      courseCode: course.courseCode,
      title: course.title,
      schedule: {
        [weekDays[index % 5].value]: { time: '10:00-11:00', room: `ROOM${index + 1}` }
      },
      instructor: props.teachers[index]?.name || 'TBD'
    }))

    scheduledCourses.value.push(...newCourses)
    updateScheduleStats()
    toast.success('Schedule generated successfully')
  } catch (error) {
    toast.error('Failed to generate schedule')
  } finally {
    isGenerating.value = false
  }
}

const saveSchedule = async () => {
  try {
    // Mock save operation - in real implementation, this would call an API
    toast.success('Schedule saved successfully')
  } catch (error) {
    toast.error('Failed to save schedule')
  }
}

const checkConflicts = async () => {
  try {
    // Mock conflict detection - in real implementation, this would call an API
    conflicts.value = []

    // Check for time conflicts
    const timeSlots: Record<string, any[]> = {}
    scheduledCourses.value.forEach(course => {
      Object.entries(course.schedule).forEach(([day, schedule]: [string, any]) => {
        const key = `${day}-${schedule.time}`
        if (!timeSlots[key]) timeSlots[key] = []
        timeSlots[key].push(course)
      })
    })

    Object.entries(timeSlots).forEach(([key, courses]) => {
      if (courses.length > 1) {
        conflicts.value.push({
          id: Date.now(),
          description: `Time conflict: ${courses.map(c => c.courseCode).join(', ')} at ${key}`
        })
      }
    })

    if (conflicts.value.length === 0) {
      toast.success('No conflicts detected')
    } else {
      toast.error(`${conflicts.value.length} conflicts detected`)
    }
  } catch (error) {
    toast.error('Failed to check conflicts')
  }
}

const optimizeSchedule = async () => {
  try {
    // Mock optimization - in real implementation, this would call an optimization algorithm
    toast.success('Schedule optimized successfully')
  } catch (error) {
    toast.error('Failed to optimize schedule')
  }
}

const addCourseAssignment = () => {
  if (!assignment.courseId || !assignment.day || !assignment.time) {
    toast.error('Please fill in all required fields')
    return
  }

  const course = props.courses.find(c => c.id.toString() === assignment.courseId)
  if (!course) return

  const newAssignment = {
    id: Date.now(),
    courseCode: course.courseCode,
    title: course.title,
    schedule: {
      [assignment.day]: { time: assignment.time, room: assignment.room || 'TBD' }
    },
    instructor: assignment.instructor || 'TBD'
  }

  scheduledCourses.value.push(newAssignment)
  updateScheduleStats()

  // Reset form
  Object.assign(assignment, {
    courseId: '',
    day: '',
    time: '',
    room: '',
    instructor: ''
  })

  toast.success('Course assigned successfully')
}

const editSchedule = (scheduledCourse: any) => {
  // In real implementation, this would open an edit modal
  toast.info('Edit schedule functionality not implemented yet')
}

const removeSchedule = (scheduledCourse: any) => {
  const index = scheduledCourses.value.findIndex(c => c.id === scheduledCourse.id)
  if (index > -1) {
    scheduledCourses.value.splice(index, 1)
    updateScheduleStats()
    toast.success('Course removed from schedule')
  }
}

const previousMonth = () => {
  currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth() - 1, 1)
}

const nextMonth = () => {
  currentDate.value = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth() + 1, 1)
}

// Lifecycle
onMounted(() => {
  loadScheduleData()
})
</script>