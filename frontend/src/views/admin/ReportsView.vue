<template>
  <AdminLayout>
    <div class="space-y-6">
      <!-- Page Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">Reports & Analytics</h1>
          <p class="text-muted-foreground">
            Generate comprehensive reports and analyze scheduling data
          </p>
        </div>
        <div class="flex items-center gap-2">
          <Button variant="outline" @click="showDateFilterModal = true">
            <Calendar class="h-4 w-4 mr-2" />
            Date Range
          </Button>
          <Button @click="showGenerateReportModal = true">
            <BarChart3 class="h-4 w-4 mr-2" />
            Generate Report
          </Button>
        </div>
      </div>

      <!-- Report Type Selection -->
      <Card>
        <CardHeader>
          <CardTitle>Report Type</CardTitle>
          <CardDescription>
            Select the type of report you want to generate
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Button
              variant="outline"
              class="h-20 flex-col"
              :class="{ 'ring-2 ring-primary': selectedReportType === 'utilization' }"
              @click="selectedReportType = 'utilization'"
            >
              <TrendingUp class="h-6 w-6 mb-2" />
              Utilization Report
            </Button>
            <Button
              variant="outline"
              class="h-20 flex-col"
              :class="{ 'ring-2 ring-primary': selectedReportType === 'workload' }"
              @click="selectedReportType = 'workload'"
            >
              <Users class="h-6 w-6 mb-2" />
              Teacher Workload
            </Button>
            <Button
              variant="outline"
              class="h-20 flex-col"
              :class="{ 'ring-2 ring-primary': selectedReportType === 'capacity' }"
              @click="selectedReportType = 'capacity'"
            >
              <DoorOpen class="h-6 w-6 mb-2" />
              Room Capacity
            </Button>
            <Button
              variant="outline"
              class="h-20 flex-col"
              :class="{ 'ring-2 ring-primary': selectedReportType === 'conflicts' }"
              @click="selectedReportType = 'conflicts'"
            >
              <AlertTriangle class="h-6 w-6 mb-2" />
              Conflicts Report
            </Button>
          </div>
        </CardContent>
      </Card>

      <!-- Date Range Filter -->
      <Card v-if="selectedReportType">
        <CardHeader>
          <CardTitle>Report Parameters</CardTitle>
          <CardDescription>
            Configure the report parameters and filters
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="grid gap-4 md:grid-cols-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Semester</label>
              <Select v-model="reportFilters.semester">
                <option value="FALL_2024">Fall 2024</option>
                <option value="SPRING_2024">Spring 2024</option>
                <option value="SUMMER_2024">Summer 2024</option>
                <option value="FALL_2023">Fall 2023</option>
              </Select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Academic Year</label>
              <Select v-model="reportFilters.academicYear">
                <option value="2024-2025">2024-2025</option>
                <option value="2023-2024">2023-2024</option>
                <option value="2022-2023">2022-2023</option>
              </Select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Export Format</label>
              <Select v-model="reportFilters.format">
                <option value="PDF">PDF</option>
                <option value="EXCEL">Excel</option>
                <option value="CSV">CSV</option>
                <option value="JSON">JSON</option>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Analytics Overview -->
      <div v-if="selectedReportType" class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        <!-- Key Metrics -->
        <Card>
          <CardHeader>
            <CardTitle>Key Metrics</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="space-y-4">
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium">Overall Utilization</span>
                <Badge variant="secondary">{{ analytics.overallUtilization }}%</Badge>
              </div>
              <div class="w-full bg-secondary rounded-full h-2">
                <div
                  class="bg-primary h-2 rounded-full transition-all duration-300"
                  :style="{ width: analytics.overallUtilization + '%' }"
                ></div>
              </div>
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-muted-foreground">Peak Hours</span>
                  <span class="font-medium">{{ analytics.peakHours }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-muted-foreground">Avg. Class Size</span>
                  <span class="font-medium">{{ analytics.averageClassSize }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-muted-foreground">Efficiency Score</span>
                  <span class="font-medium">{{ analytics.efficiencyScore }}/100</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- Top Performers -->
        <Card>
          <CardHeader>
            <CardTitle>Top Performers</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="space-y-3">
              <div
                v-for="teacher in analytics.topTeachers"
                :key="teacher.id"
                class="flex items-center justify-between"
              >
                <div class="flex items-center gap-2">
                  <div class="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center">
                    <span class="text-blue-800 text-xs font-bold">
                      {{ teacher.name.charAt(0) }}
                    </span>
                  </div>
                  <div>
                    <p class="text-sm font-medium">{{ teacher.name }}</p>
                    <p class="text-xs text-muted-foreground">{{ teacher.department }}</p>
                  </div>
                </div>
                <Badge variant="outline">{{ teacher.utilization }}%</Badge>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- Recent Reports -->
        <Card>
          <CardHeader>
            <CardTitle>Recent Reports</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="space-y-3">
              <div
                v-for="report in recentReports"
                :key="report.id"
                class="flex items-center justify-between"
              >
                <div>
                  <p class="text-sm font-medium">{{ report.name }}</p>
                  <p class="text-xs text-muted-foreground">{{ report.generatedAt }}</p>
                </div>
                <Button variant="ghost" size="sm" @click="downloadReport(report)">
                  <Download class="h-3 w-3" />
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- Detailed Analytics Chart -->
      <Card v-if="selectedReportType === 'utilization'">
        <CardHeader>
          <CardTitle>Schedule Utilization Trends</CardTitle>
          <CardDescription>
            Weekly utilization patterns and trends
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="h-64 flex items-center justify-center bg-muted rounded-lg">
            <div class="text-center">
              <BarChart3 class="h-12 w-12 mx-auto mb-2 text-muted-foreground" />
              <p class="text-sm text-muted-foreground">Interactive chart visualization</p>
              <p class="text-xs text-muted-foreground">Will be implemented with chart library integration</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Teacher Workload Analysis -->
      <Card v-if="selectedReportType === 'workload'">
        <CardHeader>
          <CardTitle>Teacher Workload Analysis</CardTitle>
          <CardDescription>
            Comprehensive workload distribution across departments
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <div class="grid gap-4 md:grid-cols-3">
              <div class="text-center p-4 bg-blue-50 rounded-lg">
                <div class="text-2xl font-bold text-blue-700">{{ analytics.totalTeachers }}</div>
                <div class="text-sm text-blue-600">Total Teachers</div>
              </div>
              <div class="text-center p-4 bg-green-50 rounded-lg">
                <div class="text-2xl font-bold text-green-700">{{ analytics.avgWorkload }}h</div>
                <div class="text-sm text-green-600">Avg Weekly Hours</div>
              </div>
              <div class="text-center p-4 bg-orange-50 rounded-lg">
                <div class="text-2xl font-bold text-orange-700">{{ analytics.overloadedTeachers }}</div>
                <div class="text-sm text-orange-600">Overloaded</div>
              </div>
            </div>

            <div class="space-y-2">
              <div
                v-for="dept in analytics.departmentWorkloads"
                :key="dept.department"
                class="flex items-center justify-between p-3 border rounded-lg"
              >
                <div>
                  <p class="text-sm font-medium">{{ dept.department }}</p>
                  <p class="text-xs text-muted-foreground">{{ dept.teachers }} teachers</p>
                </div>
                <div class="text-right">
                  <p class="text-sm font-medium">{{ dept.averageHours }}h avg</p>
                  <p class="text-xs text-muted-foreground">{{ dept.totalHours }}h total</p>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Generate Report Button -->
      <Card v-if="selectedReportType">
        <CardHeader>
          <CardTitle>Generate Report</CardTitle>
          <CardDescription>
            Create and download the selected report
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium">{{ getReportTypeName(selectedReportType) }}</p>
              <p class="text-xs text-muted-foreground">
                {{ reportFilters.semester }} • {{ reportFilters.academicYear }}
              </p>
            </div>
            <Button @click="generateReport" :disabled="generatingReport">
              <FileText v-if="!generatingReport" class="h-4 w-4 mr-2" />
              <Loader v-else class="h-4 w-4 mr-2 animate-spin" />
              {{ generatingReport ? 'Generating...' : 'Generate Report' }}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Date Filter Modal -->
    <Modal v-if="showDateFilterModal" @close="showDateFilterModal = false">
      <Card class="w-full max-w-md">
        <CardHeader>
          <CardTitle>Date Range Filter</CardTitle>
          <CardDescription>Set custom date range for reports</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Start Date</label>
              <Input type="date" v-model="customDateRange.start" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">End Date</label>
              <Input type="date" v-model="customDateRange.end" />
            </div>
            <div class="flex items-center justify-end gap-2">
              <Button variant="outline" @click="showDateFilterModal = false">Cancel</Button>
              <Button @click="applyDateRange">Apply</Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </Modal>

    <!-- Generate Report Modal -->
    <Modal v-if="showGenerateReportModal" @close="showGenerateReportModal = false">
      <Card class="w-full max-w-lg">
        <CardHeader>
          <CardTitle>Generate Custom Report</CardTitle>
          <CardDescription>Configure report generation options</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Report Type</label>
              <Select v-model="customReport.type">
                <option value="utilization">Utilization Report</option>
                <option value="workload">Teacher Workload</option>
                <option value="capacity">Room Capacity</option>
                <option value="conflicts">Conflicts Report</option>
                <option value="comprehensive">Comprehensive Report</option>
              </Select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Include Charts</label>
              <div class="flex items-center">
                <input type="checkbox" v-model="customReport.includeCharts" class="mr-2" />
                <span class="text-sm">Include visualizations</span>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Email Report</label>
              <div class="flex items-center">
                <input type="checkbox" v-model="customReport.emailReport" class="mr-2" />
                <span class="text-sm">Send via email when complete</span>
              </div>
            </div>
            <div class="flex items-center justify-end gap-2">
              <Button variant="outline" @click="showGenerateReportModal = false">Cancel</Button>
              <Button @click="generateCustomReport">Generate</Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </Modal>
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
import Select from '@/components/ui/Select.vue'
import Input from '@/components/ui/Input.vue'
import Modal from '@/components/ui/Modal.vue'
import reportService from '@/services/reportService'
import {
  BarChart3,
  Calendar,
  Users,
  DoorOpen,
  AlertTriangle,
  Download,
  FileText,
  TrendingUp,
  Loader,
  Filter
} from 'lucide-vue-next'

const router = useRouter()
const toast = useToast()

// State
const loading = ref(false)
const generatingReport = ref(false)
const selectedReportType = ref<string | null>(null)
const showDateFilterModal = ref(false)
const showGenerateReportModal = ref(false)

// Report filters
const reportFilters = ref({
  semester: 'FALL_2024',
  academicYear: '2024-2025',
  format: 'PDF',
  startDate: '',
  endDate: ''
})

const customDateRange = ref({
  start: '',
  end: ''
})

const customReport = ref({
  type: 'utilization',
  includeCharts: true,
  emailReport: false
})

// Analytics data
const analytics = ref({
  overallUtilization: 0,
  peakHours: '10:00 AM - 2:00 PM',
  averageClassSize: 0,
  efficiencyScore: 0,
  totalTeachers: 0,
  avgWorkload: 0,
  overloadedTeachers: 0,
  topTeachers: [] as Array<{
    id: number
    name: string
    department: string
    utilization: number
  }>,
  departmentWorkloads: [] as Array<{
    department: string
    teachers: number
    averageHours: number
    totalHours: number
  }>()
})

const recentReports = ref([])

// Load analytics data
const loadAnalytics = async () => {
  if (!selectedReportType.value) return

  loading.value = true
  try {
    // Load analytics based on selected report type
    if (selectedReportType.value === 'utilization') {
      const utilizationData = await reportService.getScheduleUtilization(
        reportFilters.value.semester,
        reportFilters.value.academicYear
      )

      analytics.value = {
        ...analytics.value,
        overallUtilization: utilizationData.overallUtilization || 0,
        averageClassSize: utilizationData.averageClassSize || 0,
        efficiencyScore: utilizationData.efficiencyScore || 0
      }
    } else if (selectedReportType.value === 'workload') {
      const workloadData = await reportService.getTeacherWorkloadAnalysis(
        reportFilters.value.semester,
        reportFilters.value.academicYear
      )

      analytics.value = {
        ...analytics.value,
        totalTeachers: workloadData.totalTeachers || 0,
        avgWorkload: workloadData.averageWorkload || 0,
        overloadedTeachers: workloadData.overloadedTeachers || 0,
        departmentWorkloads: workloadData.departmentWorkloads || []
      }
    }

    // Load recent reports
    const reportsResponse = await reportService.getRecentReports()
    recentReports.value = reportsResponse.slice(0, 5)
  } catch (error) {
    console.error('Failed to load analytics:', error)
    toast.error('Failed to load analytics data')
  } finally {
    loading.value = false
  }
}

// Generate report
const generateReport = async () => {
  if (!selectedReportType.value) return

  generatingReport.value = true
  try {
    const reportRequest = {
      type: selectedReportType.value,
      semester: reportFilters.value.semester,
      academicYear: reportFilters.value.academicYear,
      format: reportFilters.value.format,
      startDate: reportFilters.value.startDate || undefined,
      endDate: reportFilters.value.endDate || undefined
    }

    const report = await reportService.generateReport(reportRequest)

    toast.success('Report generated successfully')

    // Download the report
    if (report.downloadUrl) {
      window.open(report.downloadUrl, '_blank')
    }

    // Refresh recent reports
    loadAnalytics()
  } catch (error) {
    console.error('Failed to generate report:', error)
    toast.error('Failed to generate report')
  } finally {
    generatingReport.value = false
  }
}

// Generate custom report
const generateCustomReport = async () => {
  showGenerateReportModal.value = false
  selectedReportType.value = customReport.value.type
  await generateReport()
}

// Apply date range
const applyDateRange = () => {
  reportFilters.value.startDate = customDateRange.value.start
  reportFilters.value.endDate = customDateRange.value.end
  showDateFilterModal.value = false
  loadAnalytics()
}

// Download report
const downloadReport = async (report: any) => {
  try {
    window.open(report.downloadUrl, '_blank')
  } catch (error) {
    toast.error('Failed to download report')
  }
}

// Utility functions
const getReportTypeName = (type: string) => {
  switch (type) {
    case 'utilization':
      return 'Schedule Utilization Report'
    case 'workload':
      return 'Teacher Workload Analysis'
    case 'capacity':
      return 'Room Capacity Report'
    case 'conflicts':
      return 'Conflicts Report'
    case 'comprehensive':
      return 'Comprehensive Report'
    default:
      return 'Custom Report'
  }
}

// Load initial data
onMounted(() => {
  // Load default analytics
  selectedReportType.value = 'utilization'
  loadAnalytics()
})
</script>