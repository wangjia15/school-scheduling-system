<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h2 class="text-2xl font-bold">Course Analytics Dashboard</h2>
        <p class="text-muted-foreground">Comprehensive insights and performance metrics for courses</p>
      </div>
      <div class="flex space-x-2">
        <Button @click="refreshData" :disabled="isLoading">
          <RefreshCw class="w-4 h-4 mr-2" />
          {{ isLoading ? 'Refreshing...' : 'Refresh' }}
        </Button>
        <Button @click="exportReport">
          <Download class="w-4 h-4 mr-2" />
          Export Report
        </Button>
      </div>
    </div>

    <!-- Time Period Selector -->
    <Card>
      <CardContent class="p-4">
        <div class="flex items-center space-x-4">
          <label class="text-sm font-medium">Time Period:</label>
          <SelectInput
            v-model="timePeriod"
            :options="timePeriodOptions"
            @change="loadAnalyticsData"
          />
          <div class="flex items-center space-x-2">
            <label class="text-sm font-medium">Custom Range:</label>
            <Input
              v-model="customDateRange.start"
              type="date"
              class="w-40"
            />
            <span>to</span>
            <Input
              v-model="customDateRange.end"
              type="date"
              class="w-40"
            />
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Key Metrics -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <Card>
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-muted-foreground">Total Courses</p>
              <p class="text-2xl font-bold">{{ metrics.totalCourses }}</p>
              <p class="text-xs text-muted-foreground">
                <span :class="metrics.courseGrowth >= 0 ? 'text-green-600' : 'text-red-600'">
                  {{ metrics.courseGrowth >= 0 ? '+' : '' }}{{ metrics.courseGrowth }}%
                </span>
                vs last period
              </p>
            </div>
            <BookOpen class="w-8 h-8 text-blue-600" />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-muted-foreground">Total Enrollments</p>
              <p class="text-2xl font-bold">{{ metrics.totalEnrollments.toLocaleString() }}</p>
              <p class="text-xs text-muted-foreground">
                <span :class="metrics.enrollmentGrowth >= 0 ? 'text-green-600' : 'text-red-600'">
                  {{ metrics.enrollmentGrowth >= 0 ? '+' : '' }}{{ metrics.enrollmentGrowth }}%
                </span>
                vs last period
              </p>
            </div>
            <Users class="w-8 h-8 text-green-600" />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-muted-foreground">Avg. Class Size</p>
              <p class="text-2xl font-bold">{{ metrics.averageClassSize }}</p>
              <p class="text-xs text-muted-foreground">
                Capacity: {{ metrics.capacityUtilization }}%
              </p>
            </div>
            <TrendingUp class="w-8 h-8 text-purple-600" />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-muted-foreground">Completion Rate</p>
              <p class="text-2xl font-bold">{{ metrics.completionRate }}%</p>
              <p class="text-xs text-muted-foreground">
                {{ metrics.completedCourses }} completed
              </p>
            </div>
            <Award class="w-8 h-8 text-orange-600" />
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Charts Section -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Enrollment Trends -->
      <Card>
        <CardHeader>
          <CardTitle>Enrollment Trends</CardTitle>
          <CardDescription>Course enrollment over time</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="h-64">
            <LineChart :data="enrollmentTrendData" />
          </div>
        </CardContent>
      </Card>

      <!-- Course Distribution by Level -->
      <Card>
        <CardHeader>
          <CardTitle>Course Distribution by Level</CardTitle>
          <CardDescription>Breakdown of courses by academic level</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="h-64">
            <PieChart :data="courseLevelData" />
          </div>
        </CardContent>
      </Card>

      <!-- Department Performance -->
      <Card>
        <CardHeader>
          <CardTitle>Department Performance</CardTitle>
          <CardDescription>Enrollment and completion rates by department</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="h-64">
            <BarChart :data="departmentPerformanceData" />
          </div>
        </CardContent>
      </Card>

      <!-- Capacity Utilization -->
      <Card>
        <CardHeader>
          <CardTitle>Capacity Utilization</CardTitle>
          <CardDescription>Classroom capacity usage across courses</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="h-64">
            <BarChart :data="capacityUtilizationData" />
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Top Performing Courses -->
    <Card>
      <CardHeader>
        <CardTitle>Top Performing Courses</CardTitle>
        <CardDescription>Courses with highest enrollment and completion rates</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b">
                <th class="text-left p-3">Course</th>
                <th class="text-left p-3">Department</th>
                <th class="text-left p-3">Enrollments</th>
                <th class="text-left p-3">Completion Rate</th>
                <th class="text-left p-3">Avg. Grade</th>
                <th class="text-left p-3">Capacity</th>
                <th class="text-left p-3">Trend</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="course in topPerformingCourses" :key="course.id" class="border-b hover:bg-muted/50">
                <td class="p-3">
                  <div class="font-medium">{{ course.courseCode }}</div>
                  <div class="text-sm text-muted-foreground">{{ course.title }}</div>
                </td>
                <td class="p-3">{{ course.department }}</td>
                <td class="p-3">{{ course.enrollments }}</td>
                <td class="p-3">
                  <div class="flex items-center space-x-2">
                    <div class="w-16 bg-gray-200 rounded-full h-2">
                      <div
                        class="bg-green-600 h-2 rounded-full"
                        :style="{ width: course.completionRate + '%' }"
                      ></div>
                    </div>
                    <span class="text-sm">{{ course.completionRate }}%</span>
                  </div>
                </td>
                <td class="p-3">{{ course.averageGrade }}%</td>
                <td class="p-3">
                  <div class="flex items-center space-x-2">
                    <div class="w-16 bg-gray-200 rounded-full h-2">
                      <div
                        class="bg-blue-600 h-2 rounded-full"
                        :style="{ width: course.capacityUtilization + '%' }"
                      ></div>
                    </div>
                    <span class="text-sm">{{ course.capacityUtilization }}%</span>
                  </div>
                </td>
                <td class="p-3">
                  <TrendingUp
                    v-if="course.trend === 'up'"
                    class="w-4 h-4 text-green-600"
                  />
                  <TrendingDown
                    v-else-if="course.trend === 'down'"
                    class="w-4 h-4 text-red-600"
                  />
                  <Minus
                    v-else
                    class="w-4 h-4 text-gray-600"
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>

    <!-- Prerequisite Analysis -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <Card>
        <CardHeader>
          <CardTitle>Prerequisite Completion Rates</CardTitle>
          <CardDescription>Success rates for prerequisite courses</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <div v-for="prereq in prerequisiteAnalysis" :key="prereq.courseId" class="flex items-center justify-between">
              <div>
                <div class="font-medium">{{ prereq.courseCode }}</div>
                <div class="text-sm text-muted-foreground">{{ prereq.title }}</div>
              </div>
              <div class="flex items-center space-x-2">
                <div class="w-20 bg-gray-200 rounded-full h-2">
                  <div
                    class="bg-blue-600 h-2 rounded-full"
                    :style="{ width: prereq.completionRate + '%' }"
                  ></div>
                </div>
                <span class="text-sm font-medium">{{ prereq.completionRate }}%</span>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Course Difficulty Distribution</CardTitle>
          <CardDescription>Distribution of courses by difficulty level</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <div v-for="difficulty in difficultyDistribution" :key="difficulty.level" class="flex items-center justify-between">
              <div class="flex items-center space-x-2">
                <Star
                  v-for="i in difficulty.level"
                  :key="i"
                  class="w-4 h-4 text-yellow-400 fill-current"
                />
                <span class="text-sm font-medium">{{ difficulty.label }}</span>
              </div>
              <div class="flex items-center space-x-2">
                <div class="w-20 bg-gray-200 rounded-full h-2">
                  <div
                    class="bg-purple-600 h-2 rounded-full"
                    :style="{ width: difficulty.percentage + '%' }"
                  ></div>
                </div>
                <span class="text-sm font-medium">{{ difficulty.percentage }}%</span>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Predictive Analytics -->
    <Card>
      <CardHeader>
        <CardTitle>Predictive Analytics</CardTitle>
        <CardDescription>AI-powered insights and recommendations</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div class="p-4 border rounded-lg">
            <div class="flex items-center space-x-2 mb-2">
              <Brain class="w-5 h-5 text-blue-600" />
              <span class="font-medium">Enrollment Forecast</span>
            </div>
            <p class="text-sm text-muted-foreground mb-2">
              Predicted {{ predictions.enrollmentGrowth }}% growth next semester
            </p>
            <div class="text-xs text-muted-foreground">
              Based on historical trends and current patterns
            </div>
          </div>

          <div class="p-4 border rounded-lg">
            <div class="flex items-center space-x-2 mb-2">
              <Target class="w-5 h-5 text-green-600" />
              <span class="font-medium">Capacity Planning</span>
            </div>
            <p class="text-sm text-muted-foreground mb-2">
              {{ predictions.capacityRecommendation }} additional sections needed
            </p>
            <div class="text-xs text-muted-foreground">
              Based on projected enrollment increases
            </div>
          </div>

          <div class="p-4 border rounded-lg">
            <div class="flex items-center space-x-2 mb-2">
              <AlertTriangle class="w-5 h-5 text-orange-600" />
              <span class="font-medium">At-Risk Courses</span>
            </div>
            <p class="text-sm text-muted-foreground mb-2">
              {{ predictions.atRiskCourses }} courses may need intervention
            </p>
            <div class="text-xs text-muted-foreground">
              Based on completion rates and enrollment patterns
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import SelectInput from '@/components/ui/SelectInput.vue'
import { useToast } from 'vue-toastification'
import {
  RefreshCw,
  Download,
  BookOpen,
  Users,
  TrendingUp,
  Award,
  LineChart,
  PieChart,
  BarChart,
  TrendingUp as TrendUpIcon,
  TrendingDown,
  Minus,
  Star,
  Brain,
  Target,
  AlertTriangle
} from 'lucide-vue-next'

// Chart Components (simplified for this example)
const LineChart = { template: '<div class="flex items-center justify-center h-full text-muted-foreground">Line Chart Placeholder</div>' }
const PieChart = { template: '<div class="flex items-center justify-center h-full text-muted-foreground">Pie Chart Placeholder</div>' }
const BarChart = { template: '<div class="flex items-center justify-center h-full text-muted-foreground">Bar Chart Placeholder</div>' }

const toast = useToast()

// State
const isLoading = ref(false)
const timePeriod = ref('last-semester')

// Date Range
const customDateRange = reactive({
  start: '',
  end: ''
})

// Analytics Metrics
const metrics = reactive({
  totalCourses: 0,
  totalEnrollments: 0,
  averageClassSize: 0,
  completionRate: 0,
  completedCourses: 0,
  courseGrowth: 0,
  enrollmentGrowth: 0,
  capacityUtilization: 0
})

// Predictions
const predictions = reactive({
  enrollmentGrowth: 0,
  capacityRecommendation: 0,
  atRiskCourses: 0
})

// Analytics Data
const topPerformingCourses = ref<any[]>([])
const prerequisiteAnalysis = ref<any[]>([])
const difficultyDistribution = ref<any[]>([])

// Chart Data
const enrollmentTrendData = ref<any[]>([])
const courseLevelData = ref<any[]>([])
const departmentPerformanceData = ref<any[]>([])
const capacityUtilizationData = ref<any[]>([])

// Options
const timePeriodOptions = [
  { value: 'last-week', label: 'Last Week' },
  { value: 'last-month', label: 'Last Month' },
  { value: 'last-semester', label: 'Last Semester' },
  { value: 'last-year', label: 'Last Year' },
  { value: 'custom', label: 'Custom Range' }
]

// Methods
const loadAnalyticsData = async () => {
  try {
    isLoading.value = true

    // Mock analytics data - in real implementation, this would call analytics APIs
    await new Promise(resolve => setTimeout(resolve, 1000))

    // Update metrics
    Object.assign(metrics, {
      totalCourses: 156,
      totalEnrollments: 8540,
      averageClassSize: 28,
      completionRate: 87,
      completedCourses: 7429,
      courseGrowth: 5.2,
      enrollmentGrowth: 8.7,
      capacityUtilization: 76
    })

    // Update predictions
    Object.assign(predictions, {
      enrollmentGrowth: 12,
      capacityRecommendation: 8,
      atRiskCourses: 5
    })

    // Update top performing courses
    topPerformingCourses.value = [
      {
        id: 1,
        courseCode: 'CS101',
        title: 'Introduction to Computer Science',
        department: 'Computer Science',
        enrollments: 320,
        completionRate: 94,
        averageGrade: 87,
        capacityUtilization: 89,
        trend: 'up'
      },
      {
        id: 2,
        courseCode: 'MATH201',
        title: 'Calculus II',
        department: 'Mathematics',
        enrollments: 280,
        completionRate: 89,
        averageGrade: 82,
        capacityUtilization: 84,
        trend: 'up'
      },
      {
        id: 3,
        courseCode: 'ENG101',
        title: 'English Composition',
        department: 'English',
        enrollments: 450,
        completionRate: 92,
        averageGrade: 85,
        capacityUtilization: 91,
        trend: 'stable'
      }
    ]

    // Update prerequisite analysis
    prerequisiteAnalysis.value = [
      {
        courseId: 1,
        courseCode: 'CS100',
        title: 'Computer Science Fundamentals',
        completionRate: 91
      },
      {
        courseId: 2,
        courseCode: 'MATH101',
        title: 'Calculus I',
        completionRate: 87
      },
      {
        courseId: 3,
        courseCode: 'PHYS101',
        title: 'Physics I',
        completionRate: 83
      }
    ]

    // Update difficulty distribution
    difficultyDistribution.value = [
      { level: 1, label: 'Beginner', percentage: 35 },
      { level: 2, label: 'Intermediate', percentage: 40 },
      { level: 3, label: 'Advanced', percentage: 20 },
      { level: 4, label: 'Expert', percentage: 5 }
    ]

    // Update chart data
    enrollmentTrendData.value = [
      { month: 'Jan', enrollments: 7200 },
      { month: 'Feb', enrollments: 7450 },
      { month: 'Mar', enrollments: 7680 },
      { month: 'Apr', enrollments: 7890 },
      { month: 'May', enrollments: 8120 },
      { month: 'Jun', enrollments: 8540 }
    ]

    courseLevelData.value = [
      { level: 'Undergraduate', count: 98, percentage: 63 },
      { level: 'Graduate', count: 45, percentage: 29 },
      { level: 'PhD', count: 13, percentage: 8 }
    ]

    departmentPerformanceData.value = [
      { department: 'Computer Science', enrollments: 2100, completion: 88 },
      { department: 'Mathematics', enrollments: 1850, completion: 91 },
      { department: 'Engineering', enrollments: 1680, completion: 85 },
      { department: 'Sciences', enrollments: 1420, completion: 89 },
      { department: 'Humanities', enrollments: 1490, completion: 92 }
    ]

    capacityUtilizationData.value = [
      { courseType: 'Lecture', utilization: 82 },
      { courseType: 'Lab', utilization: 68 },
      { courseType: 'Seminar', utilization: 91 },
      { courseType: 'Studio', utilization: 76 }
    ]

  } catch (error) {
    toast.error('Failed to load analytics data')
  } finally {
    isLoading.value = false
  }
}

const refreshData = async () => {
  await loadAnalyticsData()
  toast.success('Analytics data refreshed successfully')
}

const exportReport = () => {
  // Mock export functionality
  const reportData = {
    metrics,
    predictions,
    topPerformingCourses: topPerformingCourses.value,
    generatedAt: new Date().toISOString()
  }

  const blob = new Blob([JSON.stringify(reportData, null, 2)], { type: 'application/json' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `course-analytics-${new Date().toISOString().split('T')[0]}.json`
  a.click()
  window.URL.revokeObjectURL(url)

  toast.success('Analytics report exported successfully')
}

// Lifecycle
onMounted(() => {
  loadAnalyticsData()
})
</script>