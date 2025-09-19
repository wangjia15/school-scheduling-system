import axios from 'axios'
import { useToast } from 'vue-toastification'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

export interface ExportRequest {
  reportType: 'SCHEDULE_OVERVIEW' | 'TEACHER_WORKLOAD' | 'CLASSROOM_UTILIZATION' | 'CONFLICTS_REPORT' | 'STUDENT_SCHEDULES' | 'COURSE_CATALOG' | 'ROOM_ALLOCATION' | 'DEPARTMENT_SUMMARY' | 'COMPREHENSIVE_REPORT'
  format: 'PDF' | 'EXCEL' | 'CSV' | 'JSON'
  startDate?: string
  endDate?: string
  template?: string
  includeCharts?: boolean
  includeAnalytics?: boolean
  emailReport?: boolean
  emailRecipients?: string
  entityIds?: number[]
  customTitle?: string
  customDescription?: string
  pageSize?: number
  landscape?: boolean
  paperSize?: string
  includeHeaderFooter?: boolean
  watermark?: string
}

export interface ExportResponse {
  success: boolean
  message: string
  jobId?: string
  fileName?: string
  filePath?: string
  downloadUrl?: string
  fileSize?: number
  successfulCount?: number
  failedCount?: number
  totalCount?: number
  status?: string
  progress?: number
}

export interface ExportJob {
  jobId: string
  format: 'PDF' | 'EXCEL' | 'CSV' | 'JSON'
  request: ExportRequest
  createdAt: string
  completedAt?: string
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED'
  filePath?: string
  fileName?: string
  errorMessage?: string
  batchExport?: boolean
  batchResults?: ExportResponse[]
}

export interface ExportTemplate {
  id: string
  name: string
  description: string
}

export interface ExportFormatInfo {
  id: string
  name: string
  description: string
}

class ExportService {
  private toast = useToast()

  // Generate PDF export
  async generatePdfExport(request: ExportRequest): Promise<ExportResponse> {
    try {
      const response = await axios.post<ExportResponse>(`${API_BASE_URL}/exports/pdf`, request)
      return response.data
    } catch (error) {
      this.toast.error('Failed to generate PDF export')
      throw this.handleError(error)
    }
  }

  // Generate Excel export
  async generateExcelExport(request: ExportRequest): Promise<ExportResponse> {
    try {
      const response = await axios.post<ExportResponse>(`${API_BASE_URL}/exports/excel`, request)
      return response.data
    } catch (error) {
      this.toast.error('Failed to generate Excel export')
      throw this.handleError(error)
    }
  }

  // Generate batch export
  async generateBatchExport(request: ExportRequest): Promise<ExportResponse> {
    try {
      const response = await axios.post<ExportResponse>(`${API_BASE_URL}/exports/batch`, request)
      return response.data
    } catch (error) {
      this.toast.error('Failed to generate batch export')
      throw this.handleError(error)
    }
  }

  // Get export job status
  async getExportStatus(jobId: string): Promise<ExportJob> {
    try {
      const response = await axios.get<ExportJob>(`${API_BASE_URL}/exports/status/${jobId}`)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // Download exported file
  async downloadExport(fileName: string): Promise<void> {
    try {
      const response = await axios.get(`${API_BASE_URL}/exports/download/${fileName}`, {
        responseType: 'blob'
      })

      // Create download link
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', fileName)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch (error) {
      this.toast.error('Failed to download export file')
      throw this.handleError(error)
    }
  }

  // Email exported report
  async emailExport(jobId: string, recipients: string[]): Promise<ExportResponse> {
    try {
      const response = await axios.post<ExportResponse>(`${API_BASE_URL}/exports/email/${jobId}`, {
        recipients: recipients.join(','),
        subject: 'Scheduling System Export Report',
        body: 'Please find attached the exported report from the scheduling system.'
      })
      this.toast.success('Export report emailed successfully')
      return response.data
    } catch (error) {
      this.toast.error('Failed to email export report')
      throw this.handleError(error)
    }
  }

  // Get export history
  async getExportHistory(limit: number = 50): Promise<ExportJob[]> {
    try {
      const response = await axios.get<ExportJob[]>(`${API_BASE_URL}/exports/history?limit=${limit}`)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // Get available templates
  async getExportTemplates(): Promise<ExportTemplate[]> {
    try {
      const response = await axios.get<ExportTemplate[]>(`${API_BASE_URL}/exports/templates`)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // Get supported formats
  async getExportFormats(): Promise<ExportFormatInfo[]> {
    try {
      const response = await axios.get<ExportFormatInfo[]>(`${API_BASE_URL}/exports/formats`)
      return response.data
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // Quick PDF export by date
  async quickPdfExportByDate(date: string): Promise<ExportResponse> {
    try {
      const response = await axios.get<ExportResponse>(`${API_BASE_URL}/exports/schedules/pdf/${date}`)
      return response.data
    } catch (error) {
      this.toast.error('Failed to generate quick PDF export')
      throw this.handleError(error)
    }
  }

  // Quick Excel export by date
  async quickExcelExportByDate(date: string): Promise<ExportResponse> {
    try {
      const response = await axios.get<ExportResponse>(`${API_BASE_URL}/exports/schedules/excel/${date}`)
      return response.data
    } catch (error) {
      this.toast.error('Failed to generate quick Excel export')
      throw this.handleError(error)
    }
  }

  // Poll for job completion
  async pollJobCompletion(jobId: string, interval: number = 2000, maxAttempts: number = 30): Promise<ExportJob> {
    return new Promise((resolve, reject) => {
      let attempts = 0

      const poll = async () => {
        try {
          attempts++
          const job = await this.getExportStatus(jobId)

          if (job.status === 'COMPLETED') {
            resolve(job)
          } else if (job.status === 'FAILED') {
            reject(new Error(job.errorMessage || 'Export job failed'))
          } else if (attempts >= maxAttempts) {
            reject(new Error('Export job timed out'))
          } else {
            setTimeout(poll, interval)
          }
        } catch (error) {
          reject(error)
        }
      }

      poll()
    })
  }

  // Get analytics data for reports
  async getScheduleUtilization(semester: string, academicYear: string): Promise<any> {
    try {
      // This would typically call a separate analytics endpoint
      // For now, return mock data
      return {
        overallUtilization: 75,
        averageClassSize: 28,
        efficiencyScore: 85
      }
    } catch (error) {
      throw this.handleError(error)
    }
  }

  async getTeacherWorkloadAnalysis(semester: string, academicYear: string): Promise<any> {
    try {
      // This would typically call a separate analytics endpoint
      // For now, return mock data
      return {
        totalTeachers: 45,
        averageWorkload: 32,
        overloadedTeachers: 3,
        departmentWorkloads: [
          { department: 'Computer Science', teachers: 12, averageHours: 35, totalHours: 420 },
          { department: 'Mathematics', teachers: 8, averageHours: 30, totalHours: 240 },
          { department: 'Physics', teachers: 6, averageHours: 28, totalHours: 168 }
        ]
      }
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // Get recent reports
  async getRecentReports(): Promise<any[]> {
    try {
      const history = await this.getExportHistory(10)
      return history.map(job => ({
        id: job.jobId,
        name: `${job.request.reportType} - ${job.format}`,
        generatedAt: job.createdAt,
        downloadUrl: job.fileName ? `${API_BASE_URL}/exports/download/${job.fileName}` : null,
        status: job.status,
        fileSize: job.fileName ? 0 : 0 // Would calculate actual file size
      }))
    } catch (error) {
      throw this.handleError(error)
    }
  }

  // Error handling
  private handleError(error: any): Error {
    if (error.response) {
      const message = error.response.data?.message || error.response.statusText
      return new Error(`Export service error: ${message}`)
    } else if (error.request) {
      return new Error('Network error: Unable to connect to export service')
    } else {
      return new Error(`Export service error: ${error.message}`)
    }
  }

  // Utility methods
  getReportTypeName(type: string): string {
    const typeNames: { [key: string]: string } = {
      'SCHEDULE_OVERVIEW': 'Schedule Overview',
      'TEACHER_WORKLOAD': 'Teacher Workload Analysis',
      'CLASSROOM_UTILIZATION': 'Classroom Utilization Report',
      'CONFLICTS_REPORT': 'Schedule Conflicts Report',
      'STUDENT_SCHEDULES': 'Student Schedules',
      'COURSE_CATALOG': 'Course Catalog',
      'ROOM_ALLOCATION': 'Room Allocation Report',
      'DEPARTMENT_SUMMARY': 'Department Summary',
      'COMPREHENSIVE_REPORT': 'Comprehensive Report'
    }
    return typeNames[type] || type
  }

  getFormatIcon(format: string): string {
    const icons: { [key: string]: string } = {
      'PDF': '📄',
      'EXCEL': '📊',
      'CSV': '📋',
      'JSON': '📄'
    }
    return icons[format] || '📄'
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'PENDING': 'text-yellow-600',
      'PROCESSING': 'text-blue-600',
      'COMPLETED': 'text-green-600',
      'FAILED': 'text-red-600'
    }
    return colors[status] || 'text-gray-600'
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  formatDateTime(dateString: string): string {
    return new Date(dateString).toLocaleString()
  }
}

export default new ExportService()