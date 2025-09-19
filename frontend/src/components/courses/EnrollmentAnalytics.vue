<template>
  <div class="space-y-6">
    <!-- Key Metrics -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <Card>
        <CardContent class="p-4">
          <div class="text-center">
            <div class="text-2xl font-bold text-blue-600">{{ enrollmentRate }}%</div>
            <div class="text-xs text-muted-foreground">Enrollment Rate</div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="text-center">
            <div class="text-2xl font-bold text-green-600">{{ avgGpa }}</div>
            <div class="text-xs text-muted-foreground">Average GPA</div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="text-center">
            <div class="text-2xl font-bold text-yellow-600">{{ retentionRate }}%</div>
            <div class="text-xs text-muted-foreground">Retention Rate</div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="text-center">
            <div class="text-2xl font-bold text-purple-600">{{ satisfactionScore }}</div>
            <div class="text-xs text-muted-foreground">Satisfaction</div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Charts Section -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Enrollment Trend -->
      <Card>
        <CardHeader>
          <CardTitle>Enrollment Trend</CardTitle>
          <CardDescription>Enrollment changes over time</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="h-64 flex items-center justify-center text-muted-foreground">
            <BarChart class="w-12 h-12" />
            <span class="ml-2">Enrollment chart would appear here</span>
          </div>
        </CardContent>
      </Card>

      <!-- Grade Distribution -->
      <Card>
        <CardHeader>
          <CardTitle>Grade Distribution</CardTitle>
          <CardDescription>Distribution of student grades</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="h-64 flex items-center justify-center text-muted-foreground">
            <PieChart class="w-12 h-12" />
            <span class="ml-2">Grade distribution chart would appear here</span>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Detailed Statistics -->
    <Card>
      <CardHeader>
        <CardTitle>Detailed Statistics</CardTitle>
        <CardDescription>Comprehensive enrollment analytics</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div class="space-y-2">
            <div class="text-sm font-medium">Enrollment Metrics</div>
            <div class="text-sm text-muted-foreground">
              <div>Total Enrolled: {{ totalEnrolled }}</div>
              <div>Active Enrollments: {{ activeEnrollments }}</div>
              <div>Waitlisted: {{ waitlistCount }}</div>
              <div>Dropped: {{ droppedCount }}</div>
            </div>
          </div>

          <div class="space-y-2">
            <div class="text-sm font-medium">Capacity Metrics</div>
            <div class="text-sm text-muted-foreground">
              <div>Fill Rate: {{ fillRate }}%</div>
              <div>Available Slots: {{ availableSlots }}</div>
              <div>Waitlist Utilization: {{ waitlistUtilization }}%</div>
              <div>Capacity Efficiency: {{ capacityEfficiency }}%</div>
            </div>
          </div>

          <div class="space-y-2">
            <div class="text-sm font-medium">Student Demographics</div>
            <div class="text-sm text-muted-foreground">
              <div>Average Year: {{ avgYear }}</div>
              <div>Major Diversity: {{ majorDiversity }}</div>
              <div>International Students: {{ internationalStudents }}%</div>
              <div>First Generation: {{ firstGeneration }}%</div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Export Options -->
    <Card>
      <CardHeader>
        <CardTitle>Export Analytics</CardTitle>
        <CardDescription>Download detailed reports and analytics</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <Button variant="outline" class="h-20 flex-col">
            <Download class="w-6 h-6 mb-2" />
            <span>Full Report</span>
          </Button>
          <Button variant="outline" class="h-20 flex-col">
            <FileSpreadsheet class="w-6 h-6 mb-2" />
            <span>Excel Data</span>
          </Button>
          <Button variant="outline" class="h-20 flex-col">
            <BarChart3 class="w-6 h-6 mb-2" />
            <span>Charts Only</span>
          </Button>
          <Button variant="outline" class="h-20 flex-col">
            <FileText class="w-6 h-6 mb-2" />
            <span>Summary PDF</span>
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
import {
  BarChart,
  PieChart,
  Download,
  FileSpreadsheet,
  BarChart3,
  FileText
} from 'lucide-vue-next'

interface Props {
  course: any
  enrollments: any[]
  waitlist: any[]
}

const props = defineProps<Props>()

// Mock analytics data
const enrollmentRate = ref(85)
const avgGpa = ref(3.2)
const retentionRate = ref(92)
const satisfactionScore = ref(4.3)

// Computed
const totalEnrolled = computed(() => props.enrollments.length)
const activeEnrollments = computed(() => props.enrollments.filter(e => e.status === 'ACTIVE').length)
const waitlistCount = computed(() => props.waitlist.length)
const droppedCount = computed(() => props.enrollments.filter(e => e.status === 'DROPPED').length)

const fillRate = computed(() => {
  return props.course.maxStudents > 0
    ? Math.round((activeEnrollments.value / props.course.maxStudents) * 100)
    : 0
})

const availableSlots = computed(() => {
  return Math.max(0, props.course.maxStudents - activeEnrollments.value)
})

const waitlistUtilization = computed(() => {
  return props.course.maxStudents > 0
    ? Math.round((waitlistCount.value / props.course.maxStudents) * 100)
    : 0
})

const capacityEfficiency = computed(() => {
  const totalCapacity = props.course.maxStudents + props.course.maxStudents * 0.2 // 20% waitlist buffer
  return totalCapacity > 0
    ? Math.round(((activeEnrollments.value + waitlistCount.value) / totalCapacity) * 100)
    : 0
})

const avgYear = ref(2.8)
const majorDiversity = ref(12)
const internationalStudents = ref(15)
const firstGeneration = ref(23)
</script>