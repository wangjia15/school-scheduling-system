<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold tracking-tight">Export Manager</h1>
        <p class="text-muted-foreground">
          Generate and manage schedule exports in various formats
        </p>
      </div>
      <div class="flex items-center gap-2">
        <Button variant="outline" @click="refreshHistory">
          <RefreshCw class="h-4 w-4 mr-2" />
          Refresh
        </Button>
        <Button @click="showExportModal = true">
          <Download class="h-4 w-4 mr-2" />
          New Export
        </Button>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <Card class="cursor-pointer hover:shadow-md transition-shadow" @click="quickExportToday('PDF')">
        <CardContent class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-muted-foreground">Today's Schedule</p>
              <p class="text-2xl font-bold">PDF</p>
            </div>
            <FileText class="h-8 w-8 text-blue-600" />
          </div>
        </CardContent>
      </Card>

      <Card class="cursor-pointer hover:shadow-md transition-shadow" @click="quickExportToday('EXCEL')">
        <CardContent class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-muted-foreground">Today's Schedule</p>
              <p class="text-2xl font-bold">Excel</p>
            </div>
            <FileSpreadsheet class="h-8 w-8 text-green-600" />
          </div>
        </CardContent>
      </Card>

      <Card class="cursor-pointer hover:shadow-md transition-shadow" @click="openBatchExport">
        <CardContent class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-muted-foreground">Batch Export</p>
              <p class="text-2xl font-bold">Multiple</p>
            </div>
            <Layers class="h-8 w-8 text-purple-600" />
          </div>
        </CardContent>
      </Card>

      <Card class="cursor-pointer hover:shadow-md transition-shadow" @click="openTemplateManager">
        <CardContent class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-muted-foreground">Templates</p>
              <p class="text-2xl font-bold">Manage</p>
            </div>
            <LayoutTemplate class="h-8 w-8 text-orange-600" />
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Active Exports -->
    <Card v-if="activeExports.length > 0">
      <CardHeader>
        <CardTitle>Active Exports</CardTitle>
        <CardDescription>
          Currently running export jobs
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div class="space-y-4">
          <div
            v-for="job in activeExports"
            :key="job.jobId"
            class="flex items-center justify-between p-4 border rounded-lg"
          >
            <div class="flex items-center gap-4">
              <div class="flex items-center gap-2">
                <span class="text-lg">{{ getFormatIcon(job.format) }}</span>
                <div>
                  <p class="font-medium">{{ getReportTypeName(job.request.reportType) }}</p>
                  <p class="text-sm text-muted-foreground">{{ job.jobId }}</p>
                </div>
              </div>
              <Badge :variant="getStatusVariant(job.status)">
                {{ job.status }}
              </Badge>
            </div>
            <div class="flex items-center gap-2">
              <Progress v-if="job.status === 'PROCESSING'" :value="getJobProgress(job)" class="w-24" />
              <Button
                v-if="job.status === 'COMPLETED'"
                variant="outline"
                size="sm"
                @click="downloadExport(job.fileName)"
              >
                <Download class="h-4 w-4 mr-1" />
                Download
              </Button>
              <Button
                v-if="job.status === 'COMPLETED'"
                variant="outline"
                size="sm"
                @click="openEmailModal(job)"
              >
                <Mail class="h-4 w-4 mr-1" />
                Email
              </Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Export History -->
    <Card>
      <CardHeader>
        <CardTitle>Export History</CardTitle>
        <CardDescription>
          Recently generated export files
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div class="space-y-4">
          <div v-if="exportHistory.length === 0" class="text-center py-8">
            <FileX class="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <p class="text-muted-foreground">No export history available</p>
          </div>

          <div
            v-for="export in exportHistory"
            :key="export.id"
            class="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors"
          >
            <div class="flex items-center gap-4">
              <div class="flex items-center gap-2">
                <span class="text-lg">{{ getFormatIcon(export.request.format) }}</span>
                <div>
                  <p class="font-medium">{{ export.request.reportType }}</p>
                  <p class="text-sm text-muted-foreground">
                    {{ formatDateTime(export.createdAt) }}
                  </p>
                </div>
              </div>
              <Badge :variant="getStatusVariant(export.status)">
                {{ export.status }}
              </Badge>
            </div>
            <div class="flex items-center gap-2">
              <span v-if="export.fileSize" class="text-sm text-muted-foreground">
                {{ formatFileSize(export.fileSize) }}
              </span>
              <Button
                v-if="export.status === 'COMPLETED'"
                variant="outline"
                size="sm"
                @click="downloadExport(export.fileName)"
              >
                <Download class="h-4 w-4 mr-1" />
                Download
              </Button>
              <Button
                v-if="export.status === 'FAILED'"
                variant="outline"
                size="sm"
                @click="retryExport(export)"
              >
                <RotateCcw class="h-4 w-4 mr-1" />
                Retry
              </Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Export Modal -->
    <Modal v-if="showExportModal" @close="showExportModal = false">
      <Card class="w-full max-w-2xl">
        <CardHeader>
          <CardTitle>Create New Export</CardTitle>
          <CardDescription>
            Configure export parameters and generate report
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <!-- Report Type -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Report Type</label>
              <Select v-model="exportForm.reportType">
                <option value="SCHEDULE_OVERVIEW">Schedule Overview</option>
                <option value="TEACHER_WORKLOAD">Teacher Workload Analysis</option>
                <option value="CLASSROOM_UTILIZATION">Classroom Utilization</option>
                <option value="CONFLICTS_REPORT">Schedule Conflicts Report</option>
                <option value="STUDENT_SCHEDULES">Student Schedules</option>
                <option value="COURSE_CATALOG">Course Catalog</option>
                <option value="COMPREHENSIVE_REPORT">Comprehensive Report</option>
              </Select>
            </div>

            <!-- Export Format -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Export Format</label>
              <div class="grid grid-cols-2 gap-2">
                <Button
                  variant="outline"
                  :class="{ 'ring-2 ring-primary': exportForm.format === 'PDF' }"
                  @click="exportForm.format = 'PDF'"
                >
                  <FileText class="h-4 w-4 mr-2" />
                  PDF
                </Button>
                <Button
                  variant="outline"
                  :class="{ 'ring-2 ring-primary': exportForm.format === 'EXCEL' }"
                  @click="exportForm.format = 'EXCEL'"
                >
                  <FileSpreadsheet class="h-4 w-4 mr-2" />
                  Excel
                </Button>
              </div>
            </div>

            <!-- Date Range -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Start Date</label>
                <Input type="date" v-model="exportForm.startDate" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">End Date</label>
                <Input type="date" v-model="exportForm.endDate" />
              </div>
            </div>

            <!-- Options -->
            <div class="space-y-3">
              <div class="flex items-center">
                <input type="checkbox" v-model="exportForm.includeCharts" class="mr-2" />
                <label class="text-sm">Include charts and visualizations</label>
              </div>
              <div class="flex items-center">
                <input type="checkbox" v-model="exportForm.includeAnalytics" class="mr-2" />
                <label class="text-sm">Include analytics summary</label>
              </div>
              <div class="flex items-center">
                <input type="checkbox" v-model="exportForm.emailReport" class="mr-2" />
                <label class="text-sm">Email report when complete</label>
              </div>
            </div>

            <!-- Email Recipients (if email enabled) -->
            <div v-if="exportForm.emailReport">
              <label class="block text-sm font-medium text-gray-700 mb-1">Email Recipients</label>
              <Input
                type="email"
                v-model="exportForm.emailRecipients"
                placeholder="admin@school.edu, teacher@school.edu"
              />
            </div>
          </div>

          <div class="flex justify-end gap-2 pt-4">
            <Button variant="outline" @click="showExportModal = false">Cancel</Button>
            <Button @click="generateExport" :disabled="generatingExport">
              <Loader v-if="generatingExport" class="h-4 w-4 mr-2 animate-spin" />
              <Download v-else class="h-4 w-4 mr-2" />
              Generate Export
            </Button>
          </div>
        </CardContent>
      </Card>
    </Modal>

    <!-- Email Modal -->
    <Modal v-if="showEmailModal" @close="showEmailModal = false">
      <Card class="w-full max-w-md">
        <CardHeader>
          <CardTitle>Email Export Report</CardTitle>
          <CardDescription>
            Send the exported report via email
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Recipients</label>
              <Input
                type="email"
                v-model="emailForm.recipients"
                placeholder="Enter email addresses separated by commas"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Subject (optional)</label>
              <Input v-model="emailForm.subject" placeholder="Export Report from Scheduling System" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Message (optional)</label>
              <textarea
                v-model="emailForm.message"
                class="w-full p-2 border rounded-md"
                rows="3"
                placeholder="Please find attached the exported report..."
              />
            </div>
          </div>

          <div class="flex justify-end gap-2 pt-4">
            <Button variant="outline" @click="showEmailModal = false">Cancel</Button>
            <Button @click="sendEmail" :disabled="sendingEmail">
              <Mail class="h-4 w-4 mr-2" />
              Send Email
            </Button>
          </div>
        </CardContent>
      </Card>
    </Modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useToast } from 'vue-toastification'
import exportService, { ExportRequest, ExportJob } from '@/services/exportService'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import Modal from '@/components/ui/Modal.vue'
import Progress from '@/components/ui/Progress.vue'
import {
  RefreshCw,
  Download,
  FileText,
  FileSpreadsheet,
  Layers,
  LayoutTemplate,
  FileX,
  Mail,
  RotateCcw,
  Loader
} from 'lucide-vue-next'

const toast = useToast()

// State
const loading = ref(false)
const generatingExport = ref(false)
const sendingEmail = ref(false)
const showExportModal = ref(false)
const showEmailModal = ref(false)
const exportHistory = ref<ExportJob[]>([])
const activeExports = ref<ExportJob[]>([])
const selectedJob = ref<ExportJob | null>(null)

// Form data
const exportForm = ref<ExportRequest>({
  reportType: 'SCHEDULE_OVERVIEW',
  format: 'PDF',
  includeCharts: true,
  includeAnalytics: true,
  emailReport: false,
  emailRecipients: ''
})

const emailForm = ref({
  recipients: '',
  subject: '',
  message: ''
})

// Polling interval
let pollingInterval: number | null = null

// Load data
const loadExportHistory = async () => {
  try {
    exportHistory.value = await exportService.getExportHistory(50)
    updateActiveExports()
  } catch (error) {
    toast.error('Failed to load export history')
  }
}

const updateActiveExports = () => {
  activeExports.value = exportHistory.value.filter(job =>
    job.status === 'PENDING' || job.status === 'PROCESSING'
  )
}

// Generate export
const generateExport = async () => {
  generatingExport.value = true
  try {
    let response
    if (exportForm.value.format === 'PDF') {
      response = await exportService.generatePdfExport(exportForm.value)
    } else {
      response = await exportService.generateExcelExport(exportForm.value)
    }

    toast.success('Export job started successfully')
    showExportModal.value = false

    // Start polling for completion
    if (response.jobId) {
      pollJobCompletion(response.jobId)
    }

    // Refresh history
    await loadExportHistory()
  } catch (error) {
    toast.error('Failed to generate export')
  } finally {
    generatingExport.value = false
  }
}

// Quick export for today
const quickExportToday = async (format: 'PDF' | 'EXCEL') => {
  const today = new Date().toISOString().split('T')[0]
  const request: ExportRequest = {
    reportType: 'SCHEDULE_OVERVIEW',
    format: format,
    startDate: today,
    endDate: today
  }

  try {
    let response
    if (format === 'PDF') {
      response = await exportService.quickPdfExportByDate(today)
    } else {
      response = await exportService.quickExcelExportByDate(today)
    }

    toast.success(`${format} export started successfully`)

    if (response.jobId) {
      pollJobCompletion(response.jobId)
    }

    await loadExportHistory()
  } catch (error) {
    toast.error(`Failed to generate ${format} export`)
  }
}

// Poll job completion
const pollJobCompletion = async (jobId: string) => {
  try {
    const job = await exportService.pollJobCompletion(jobId)

    if (job.status === 'COMPLETED') {
      toast.success('Export completed successfully')
      // Auto-download if it's a quick export
      if (job.fileName) {
        await exportService.downloadExport(job.fileName)
      }
    } else if (job.status === 'FAILED') {
      toast.error('Export failed: ' + (job.errorMessage || 'Unknown error'))
    }

    await loadExportHistory()
  } catch (error) {
    toast.error('Failed to check export status')
  }
}

// Download export
const downloadExport = async (fileName?: string) => {
  if (!fileName) return

  try {
    await exportService.downloadExport(fileName)
    toast.success('Download started')
  } catch (error) {
    toast.error('Failed to download export')
  }
}

// Open email modal
const openEmailModal = (job: ExportJob) => {
  selectedJob.value = job
  emailForm.value = {
    recipients: '',
    subject: `Export Report: ${job.request.reportType}`,
    message: 'Please find attached the exported report from the scheduling system.'
  }
  showEmailModal.value = true
}

// Send email
const sendEmail = async () => {
  if (!selectedJob.value || !emailForm.value.recipients) return

  sendingEmail.value = true
  try {
    const recipients = emailForm.value.recipients.split(',').map(r => r.trim())
    await exportService.emailExport(selectedJob.value.jobId, recipients)

    toast.success('Email sent successfully')
    showEmailModal.value = false
    selectedJob.value = null
  } catch (error) {
    toast.error('Failed to send email')
  } finally {
    sendingEmail.value = false
  }
}

// Retry export
const retryExport = async (exportJob: ExportJob) => {
  try {
    const request = { ...exportJob.request }
    let response

    if (request.format === 'PDF') {
      response = await exportService.generatePdfExport(request)
    } else {
      response = await exportService.generateExcelExport(request)
    }

    toast.success('Export retry started successfully')

    if (response.jobId) {
      pollJobCompletion(response.jobId)
    }

    await loadExportHistory()
  } catch (error) {
    toast.error('Failed to retry export')
  }
}

// Refresh history
const refreshHistory = async () => {
  await loadExportHistory()
  toast.success('Export history refreshed')
}

// Open batch export
const openBatchExport = () => {
  toast.info('Batch export feature coming soon')
}

// Open template manager
const openTemplateManager = () => {
  toast.info('Template manager feature coming soon')
}

// Utility methods
const getReportTypeName = (type: string) => {
  return exportService.getReportTypeName(type)
}

const getFormatIcon = (format: string) => {
  return exportService.getFormatIcon(format)
}

const getStatusVariant = (status: string) => {
  switch (status) {
    case 'COMPLETED':
      return 'default'
    case 'FAILED':
      return 'destructive'
    case 'PROCESSING':
      return 'secondary'
    default:
      return 'outline'
  }
}

const getStatusColor = (status: string) => {
  return exportService.getStatusColor(status)
}

const getJobProgress = (job: ExportJob) => {
  // Simple progress simulation
  if (job.status === 'PROCESSING') {
    return Math.min(95, Math.floor(Math.random() * 30) + 70)
  }
  return 0
}

const formatDateTime = (dateString: string) => {
  return exportService.formatDateTime(dateString)
}

const formatFileSize = (bytes: number) => {
  return exportService.formatFileSize(bytes)
}

// Lifecycle
onMounted(async () => {
  await loadExportHistory()

  // Start polling for active jobs
  pollingInterval = window.setInterval(async () => {
    if (activeExports.value.length > 0) {
      await loadExportHistory()
    }
  }, 5000)
})

onUnmounted(() => {
  if (pollingInterval) {
    clearInterval(pollingInterval)
  }
})
</script>