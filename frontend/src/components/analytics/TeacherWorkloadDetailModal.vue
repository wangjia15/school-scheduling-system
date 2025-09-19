<template>
  <div v-if="open" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
    <Card class="w-full max-w-6xl max-h-[90vh] overflow-y-auto">
      <CardHeader>
        <div class="flex items-center justify-between">
          <CardTitle>Teacher Workload Details</CardTitle>
          <Button variant="ghost" size="icon" @click="$emit('update:open', false)">
            <X class="h-4 w-4" />
          </Button>
        </div>
        <CardDescription>
          Detailed workload analysis for {{ teacher.user.firstName }} {{ teacher.user.lastName }}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <!-- Teacher Summary -->
        <div class="mb-6 p-4 bg-blue-50 rounded-lg">
          <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <span class="text-sm font-medium text-blue-700">Employee ID:</span>
              <div class="text-blue-900">{{ teacher.employeeId }}</div>
            </div>
            <div>
              <span class="text-sm font-medium text-blue-700">Department:</span>
              <div class="text-blue-900">{{ teacher.department.name }}</div>
            </div>
            <div>
              <span class="text-sm font-medium text-blue-700">Title:</span>
              <div class="text-blue-900">{{ teacher.title }}</div>
            </div>
            <div>
              <span class="text-sm font-medium text-blue-700">Current Workload:</span>
              <div class="text-blue-900">{{ teacher.currentWorkload }}h / {{ teacher.maxWeeklyHours }}h</div>
            </div>
          </div>
        </div>

        <!-- Workload Overview Charts -->
        <div class="mb-6">
          <h4 class="text-lg font-medium mb-4">Workload Overview</h4>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Workload Distribution -->
            <div class="border rounded-lg p-4">
              <h5 class="font-medium mb-3">Workload Distribution</h5>
              <div class="space-y-3">
                <div class="flex justify-between items-center">
                  <span class="text-sm">Assigned Courses</span>
                  <span class="font-medium">{{ teacher.assignedCourses?.length || 0 }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-sm">Total Weekly Hours</span>
                  <span class="font-medium">{{ teacher.currentWorkload }}h</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-sm">Capacity Used</span>
                  <span class="font-medium">{{ Math.round((teacher.currentWorkload / teacher.maxWeeklyHours) * 100) }}%</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2">
                  <div
                    class="bg-blue-600 h-2 rounded-full transition-all duration-300"
                    :style="{ width: `${Math.min((teacher.currentWorkload / teacher.maxWeeklyHours) * 100, 100)}%` }"
                  ></div>
                </div>
              </div>
            </div>

            <!-- Workload by Day -->
            <div class="border rounded-lg p-4">
              <h5 class="font-medium mb-3">Workload by Day</h5>
              <div class="space-y-2">
                <div v-for="dayWorkload in getWorkloadByDay()" :key="dayWorkload.day" class="flex items-center justify-between">
                  <span class="text-sm w-16">{{ dayWorkload.day }}</span>
                  <div class="flex-1 mx-3">
                    <div class="bg-gray-200 rounded-full h-2">
                      <div
                        class="bg-green-600 h-2 rounded-full transition-all duration-300"
                        :style="{ width: `${Math.min((dayWorkload.hours / 8) * 100, 100)}%` }"
                      ></div>
                    </div>
                  </div>
                  <span class="text-sm font-medium w-12 text-right">{{ dayWorkload.hours }}h</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Course Assignments -->
        <div class="mb-6">
          <h4 class="text-lg font-medium mb-4">Course Assignments</h4>
          <div class="overflow-x-auto">
            <Table :headers="assignmentHeaders" :data="teacher.assignedCourses || []" striped>
              <template #cell-courseInfo="{ row }">
                <div>
                  <div class="font-medium">{{ row.courseCode }}</div>
                  <div class="text-sm text-gray-600">{{ row.courseName }}</div>
                </div>
              </template>
              <template #cell-schedule="{ row }">
                <div v-if="row.schedule">
                  <div class="text-sm">{{ row.schedule.dayOfWeek }}</div>
                  <div class="text-xs text-gray-600">{{ row.schedule.startTime }} - {{ row.schedule.endTime }}</div>
                </div>
                <span v-else class="text-gray-500">Not scheduled</span>
              </template>
              <template #cell-classroom="{ row }">
                <div v-if="row.schedule?.classroom">
                  <div class="text-sm">{{ row.schedule.classroom.name }}</div>
                  <div class="text-xs text-gray-600">{{ row.schedule.classroom.building }}</div>
                </div>
                <span v-else class="text-gray-500">Not assigned</span>
              </template>
              <template #cell-workload="{ row }">
                <div class="text-center">
                  <div class="font-medium">{{ row.weeklyHours }}h</div>
                  <div class="text-xs text-gray-600">per week</div>
                </div>
              </template>
              <template #cell-status="{ row }">
                <Badge :variant="getAssignmentStatusVariant(row)">
                  {{ getAssignmentStatus(row) }}
                </Badge>
              </template>
            </Table>
          </div>
        </div>

        <!-- Specializations -->
        <div class="mb-6">
          <h4 class="text-lg font-medium mb-4">Teaching Specializations</h4>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div
              v-for="specialization in teacher.specializations"
              :key="specialization.id"
              class="border rounded-lg p-4"
            >
              <div class="flex items-center justify-between mb-2">
                <h5 class="font-medium">{{ specialization.subjectCode }}</h5>
                <Badge :variant="getProficiencyVariant(specialization.proficiencyLevel)">
                  {{ specialization.proficiencyLevel }}
                </Badge>
              </div>
              <div class="text-sm text-gray-600">
                <div>{{ specialization.subjectName || specialization.subjectCode }}</div>
                <div>{{ specialization.yearsExperience }} years experience</div>
                <div v-if="specialization.certified" class="text-green-600 mt-1">
                  ✓ Certified
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Workload Recommendations -->
        <div v-if="getWorkloadRecommendations().length > 0" class="mb-6">
          <h4 class="text-lg font-medium mb-4">Workload Recommendations</h4>
          <div class="space-y-3">
            <div
              v-for="recommendation in getWorkloadRecommendations()"
              :key="recommendation.type"
              class="border rounded-lg p-4"
              :class="getRecommendationClass(recommendation.type)"
            >
              <div class="flex items-start gap-3">
                <div :class="getRecommendationIconClass(recommendation.type)">
                  <component :is="getRecommendationIcon(recommendation.type)" class="h-5 w-5" />
                </div>
                <div class="flex-1">
                  <h5 class="font-medium mb-1">{{ recommendation.title }}</h5>
                  <p class="text-sm text-gray-600">{{ recommendation.description }}</p>
                  <div v-if="recommendation.action" class="mt-2">
                    <Button variant="outline" size="sm" @click="recommendation.action">
                      {{ recommendation.actionText }}
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Form Actions -->
        <div class="flex items-center justify-between pt-4 border-t">
          <div class="text-sm text-gray-600">
            {{ teacher.assignedCourses?.length || 0 }} courses assigned
          </div>
          <div class="flex items-center gap-3">
            <Button variant="outline" @click="$emit('update:open', false)">
              Close
            </Button>
            <Button @click="openOptimizationModal">
              <Target class="h-4 w-4 mr-2" />
              Optimize Workload
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Table from '@/components/ui/Table.vue'
import { X, Target, AlertTriangle, Info, Check, TrendingUp } from 'lucide-vue-next'
import type { Teacher } from '@/services/teacherService'

interface Props {
  open: boolean
  teacher: Teacher
}

interface Emits {
  'update:open': [value: boolean]
  'optimize': [teacherId: number]
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const assignmentHeaders = [
  { key: 'courseInfo', label: 'Course', width: '200px' },
  { key: 'schedule', label: 'Schedule', width: '120px' },
  { key: 'classroom', label: 'Classroom', width: '120px' },
  { key: 'workload', label: 'Workload', width: '100px' },
  { key: 'status', label: 'Status', width: '100px' }
]

// Computed
const workloadPercentage = computed(() => {
  return Math.round((props.teacher.currentWorkload / props.teacher.maxWeeklyHours) * 100)
})

// Methods
const getWorkloadByDay = () => {
  const dayMap: Record<string, number> = {
    'Monday': 0,
    'Tuesday': 0,
    'Wednesday': 0,
    'Thursday': 0,
    'Friday': 0,
    'Saturday': 0,
    'Sunday': 0
  }

  props.teacher.assignedCourses?.forEach(course => {
    if (course.schedule) {
      const day = course.schedule.dayOfWeek
      if (dayMap.hasOwnProperty(day)) {
        dayMap[day] += course.weeklyHours
      }
    }
  })

  return Object.entries(dayMap).map(([day, hours]) => ({
    day,
    hours
  }))
}

const getAssignmentStatusVariant = (course: any) => {
  if (course.hasConflicts) return 'destructive'
  if (!course.schedule) return 'secondary'
  return 'default'
}

const getAssignmentStatus = (course: any) => {
  if (course.hasConflicts) return 'Has Conflicts'
  if (!course.schedule) return 'Not Scheduled'
  return 'Scheduled'
}

const getProficiencyVariant = (level: string) => {
  switch (level) {
    case 'EXPERT': return 'default'
    case 'ADVANCED': return 'outline'
    case 'INTERMEDIATE': return 'secondary'
    case 'BEGINNER': return 'destructive'
    default: return 'outline'
  }
}

const getWorkloadRecommendations = () => {
  const recommendations = []
  const utilization = workloadPercentage.value

  if (utilization > 90) {
    recommendations.push({
      type: 'overload',
      title: 'Workload Overload',
      description: `Teacher is at ${utilization}% capacity. Consider redistributing some courses or reducing hours.`,
      action: () => emit('optimize', props.teacher.id),
      actionText: 'Optimize Now'
    })
  }

  if (utilization < 50 && props.teacher.assignedCourses?.length > 0) {
    recommendations.push({
      type: 'underload',
      title: 'Underutilized Capacity',
      description: `Teacher has ${props.teacher.maxWeeklyHours - props.teacher.currentWorkload} hours available for additional courses.`,
      action: () => emit('optimize', props.teacher.id),
      actionText: 'Find More Courses'
    })
  }

  if (props.teacher.specializations.length === 0) {
    recommendations.push({
      type: 'specialization',
      title: 'No Specializations',
      description: 'Teacher has no registered specializations. Add specializations to improve course matching.',
      action: undefined,
      actionText: undefined
    })
  }

  const unscheduledCourses = props.teacher.assignedCourses?.filter(course => !course.schedule).length || 0
  if (unscheduledCourses > 0) {
    recommendations.push({
      type: 'scheduling',
      title: 'Unscheduled Courses',
      description: `${unscheduledCourses} courses need to be scheduled. Complete scheduling to avoid conflicts.`,
      action: undefined,
      actionText: undefined
    })
  }

  return recommendations
}

const getRecommendationClass = (type: string) => {
  switch (type) {
    case 'overload': return 'border-red-300 bg-red-50'
    case 'underload': return 'border-blue-300 bg-blue-50'
    case 'specialization': return 'border-yellow-300 bg-yellow-50'
    case 'scheduling': return 'border-orange-300 bg-orange-50'
    default: return 'border-gray-300 bg-gray-50'
  }
}

const getRecommendationIconClass = (type: string) => {
  switch (type) {
    case 'overload': return 'text-red-600 bg-red-100 rounded-full p-1'
    case 'underload': return 'text-blue-600 bg-blue-100 rounded-full p-1'
    case 'specialization': return 'text-yellow-600 bg-yellow-100 rounded-full p-1'
    case 'scheduling': return 'text-orange-600 bg-orange-100 rounded-full p-1'
    default: return 'text-gray-600 bg-gray-100 rounded-full p-1'
  }
}

const getRecommendationIcon = (type: string) => {
  switch (type) {
    case 'overload': return AlertTriangle
    case 'underload': return TrendingUp
    case 'specialization': return Info
    case 'scheduling': return Check
    default: return Info
  }
}

const openOptimizationModal = () => {
  emit('optimize', props.teacher.id)
}
</script>