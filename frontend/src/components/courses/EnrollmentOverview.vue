<template>
  <div class="space-y-6">
    <!-- Course Capacity Overview -->
    <Card>
      <CardHeader>
        <CardTitle>Course Capacity</CardTitle>
        <CardDescription>Current enrollment status and capacity</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <!-- Capacity Progress -->
          <div class="space-y-2">
            <div class="flex justify-between items-center">
              <span class="text-sm font-medium">Enrollment Progress</span>
              <span class="text-sm text-muted-foreground">{{ enrolledCount }}/{{ course.maxStudents }}</span>
            </div>
            <div class="w-full bg-secondary rounded-full h-2">
              <div
                class="bg-primary h-2 rounded-full transition-all duration-300"
                :style="{ width: `${fillPercentage}%` }"
              ></div>
            </div>
            <div class="text-xs text-muted-foreground">
              {{ fillPercentage }}% filled
            </div>
          </div>

          <!-- Waitlist Status -->
          <div class="space-y-2">
            <div class="flex justify-between items-center">
              <span class="text-sm font-medium">Waitlist</span>
              <Badge :variant="waitlist.length > 0 ? 'destructive' : 'secondary'">
                {{ waitlist.length }} students
              </Badge>
            </div>
            <div v-if="waitlist.length > 0" class="text-xs text-muted-foreground">
              Next: {{ waitlist[0]?.studentName }}
            </div>
          </div>

          <!-- Available Actions -->
          <div class="space-y-2">
            <div class="text-sm font-medium">Actions</div>
            <div class="flex space-x-2">
              <Button
                size="sm"
                @click="$emit('enroll-student')"
                :disabled="enrolledCount >= course.maxStudents"
              >
                <Plus class="w-3 h-3 mr-1" />
                Enroll Student
              </Button>
              <Button
                size="sm"
                variant="outline"
                @click="$emit('waitlist-student')"
              >
                <Clock class="w-3 h-3 mr-1" />
                Manage Waitlist
              </Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Enrollment Statistics -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <Card>
        <CardContent class="p-4">
          <div class="text-center">
            <div class="text-2xl font-bold text-blue-600">{{ enrolledCount }}</div>
            <div class="text-xs text-muted-foreground">Currently Enrolled</div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="text-center">
            <div class="text-2xl font-bold text-green-600">{{ availableSlots }}</div>
            <div class="text-xs text-muted-foreground">Available Slots</div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="text-center">
            <div class="text-2xl font-bold text-yellow-600">{{ waitlist.length }}</div>
            <div class="text-xs text-muted-foreground">On Waitlist</div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="text-center">
            <div class="text-2xl font-bold text-purple-600">{{ droppedCount }}</div>
            <div class="text-xs text-muted-foreground">Dropped</div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Recent Enrollment Activity -->
    <Card>
      <CardHeader>
        <CardTitle>Recent Activity</CardTitle>
        <CardDescription>Latest enrollment changes</CardDescription>
      </CardHeader>
      <CardContent>
        <div v-if="recentActivity.length === 0" class="text-center py-4 text-muted-foreground">
          No recent enrollment activity
        </div>
        <div v-else class="space-y-3">
          <div
            v-for="activity in recentActivity"
            :key="activity.id"
            class="flex items-center justify-between p-3 bg-muted rounded-lg"
          >
            <div class="flex items-center space-x-3">
              <div class="w-8 h-8 rounded-full flex items-center justify-center"
                   :class="getActivityIconClass(activity.type)">
                <component :is="getActivityIcon(activity.type)" class="w-4 h-4" />
              </div>
              <div>
                <div class="font-medium text-sm">{{ activity.studentName }}</div>
                <div class="text-xs text-muted-foreground">{{ activity.description }}</div>
              </div>
            </div>
            <div class="text-xs text-muted-foreground">
              {{ formatTime(activity.timestamp) }}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Quick Actions -->
    <Card>
      <CardHeader>
        <CardTitle>Quick Actions</CardTitle>
        <CardDescription>Common enrollment management tasks</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <Button variant="outline" class="h-20 flex-col" @click="$emit('enroll-student')">
            <UserPlus class="w-6 h-6 mb-2" />
            <span>Enroll Student</span>
          </Button>
          <Button variant="outline" class="h-20 flex-col" @click="$emit('waitlist-student')">
            <Users class="w-6 h-6 mb-2" />
            <span>Manage Waitlist</span>
          </Button>
          <Button variant="outline" class="h-20 flex-col">
            <Download class="w-6 h-6 mb-2" />
            <span>Export Roster</span>
          </Button>
          <Button variant="outline" class="h-20 flex-col">
            <Mail class="w-6 h-6 mb-2" />
            <span>Email Students</span>
          </Button>
          <Button variant="outline" class="h-20 flex-col">
            <Calendar class="w-6 h-6 mb-2" />
            <span>Schedule Reminders</span>
          </Button>
          <Button variant="outline" class="h-20 flex-col">
            <FileText class="w-6 h-6 mb-2" />
            <span>Generate Report</span>
          </Button>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import { UserPlus, Users, Download, Mail, Calendar, FileText, Plus, Clock } from 'lucide-vue-next'

interface Props {
  course: any
  enrollments: any[]
  statistics: any
}

interface Emits {
  (e: 'enroll-student'): void
  (e: 'drop-student', enrollment: any): void
  (e: 'waitlist-student'): void
}

const props = defineProps<Props>()
defineEmits<Emits>()

// Mock waitlist data
const waitlist = ref([])

// Computed
const enrolledCount = computed(() => {
  return props.enrollments.filter(e => e.status === 'ACTIVE').length
})

const availableSlots = computed(() => {
  return Math.max(0, props.course.maxStudents - enrolledCount.value)
})

const fillPercentage = computed(() => {
  return props.course.maxStudents > 0
    ? Math.round((enrolledCount.value / props.course.maxStudents) * 100)
    : 0
})

const droppedCount = computed(() => {
  return props.enrollments.filter(e => e.status === 'DROPPED').length
})

const recentActivity = computed(() => {
  // Mock recent activity data
  return [
    {
      id: 1,
      studentName: 'John Doe',
      type: 'ENROLL',
      description: 'Enrolled in course',
      timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 2,
      studentName: 'Jane Smith',
      type: 'WAITLIST',
      description: 'Added to waitlist',
      timestamp: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString()
    }
  ]
})

// Methods
const getActivityIcon = (type: string) => {
  switch (type) {
    case 'ENROLL': return UserPlus
    case 'WAITLIST': return Users
    case 'DROP': return Plus
    default: return UserPlus
  }
}

const getActivityIconClass = (type: string) => {
  switch (type) {
    case 'ENROLL': return 'bg-green-100 text-green-600'
    case 'WAITLIST': return 'bg-yellow-100 text-yellow-600'
    case 'DROP': return 'bg-red-100 text-red-600'
    default: return 'bg-gray-100 text-gray-600'
  }
}

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 1) return 'Just now'
  if (minutes < 60) return `${minutes}m ago`
  if (hours < 24) return `${hours}h ago`
  return `${days}d ago`
}
</script>