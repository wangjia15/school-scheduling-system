import exportService, {
  type ExportRequest,
  type ExportResponse,
  type ExportJob,
  type ExportTemplate,
  type ExportFormatInfo
} from '@/services/exportService'
import api from '@/utils/api'
import { useToast } from 'vue-toastification'

// Mock the api module and toast
jest.mock('@/utils/api')
jest.mock('vue-toastification')

const mockedApi = api as jest.Mocked<typeof api>
const mockToast = {
  error: jest.fn(),
  success: jest.fn(),
  info: jest.fn(),
  warning: jest.fn()
}

jest.mocked(useToast).mockReturnValue(mockToast)

describe('ExportService', () => {
  const mockExportRequest: ExportRequest = {
    reportType: 'SCHEDULE_OVERVIEW',
    format: 'PDF',
    startDate: '2024-01-01',
    endDate: '2024-01-31',
    includeCharts: true,
    includeAnalytics: true,
    emailReport: false,
    emailRecipients: ''
  }

  const mockExportResponse: ExportResponse = {
    success: true,
    message: 'Export job started successfully',
    jobId: 'job-123',
    fileName: 'schedule-overview-2024-01-01.pdf',
    filePath: '/exports/schedule-overview-2024-01-01.pdf',
    downloadUrl: '/api/v1/exports/download/schedule-overview-2024-01-01.pdf',
    fileSize: 1024000,
    successfulCount: 1,
    failedCount: 0,
    totalCount: 1,
    status: 'PENDING',
    progress: 0
  }

  const mockExportJob: ExportJob = {
    jobId: 'job-123',
    format: 'PDF',
    request: mockExportRequest,
    createdAt: '2024-01-15T10:00:00Z',
    completedAt: '2024-01-15T10:05:00Z',
    status: 'COMPLETED',
    fileName: 'schedule-overview-2024-01-01.pdf',
    filePath: '/exports/schedule-overview-2024-01-01.pdf',
    batchExport: false,
    batchResults: [mockExportResponse]
  }

  const mockExportTemplate: ExportTemplate = {
    id: 'standard',
    name: 'Standard Template',
    description: 'Standard export template with basic formatting'
  }

  const mockExportFormatInfo: ExportFormatInfo = {
    id: 'PDF',
    name: 'PDF Document',
    description: 'Portable Document Format with charts and analytics'
  }

  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('PDF Export Methods', () => {
    describe('generatePdfExport', () => {
      it('should generate PDF export successfully', async () => {
        mockedApi.post.mockResolvedValue({
          data: mockExportResponse
        })

        const result = await exportService.generatePdfExport(mockExportRequest)

        expect(mockedApi.post).toHaveBeenCalledWith('/exports/pdf', mockExportRequest)
        expect(result).toEqual(mockExportResponse)
        expect(mockToast.error).not.toHaveBeenCalled()
      })

      it('should handle API errors for PDF export', async () => {
        const error = new Error('Failed to generate PDF export')
        mockedApi.post.mockRejectedValue(error)

        await expect(exportService.generatePdfExport(mockExportRequest)).rejects.toThrow(error)
        expect(mockToast.error).toHaveBeenCalledWith('Failed to generate PDF export')
      })

      it('should handle network errors for PDF export', async () => {
        mockedApi.post.mockRejectedValue(new Error('Network error'))

        await expect(exportService.generatePdfExport(mockExportRequest)).rejects.toThrow()
        expect(mockToast.error).toHaveBeenCalledWith('Failed to generate PDF export')
      })
    })

    describe('quickPdfExportByDate', () => {
      it('should generate quick PDF export by date', async () => {
        const date = '2024-01-15'
        mockedApi.get.mockResolvedValue({
          data: mockExportResponse
        })

        const result = await exportService.quickPdfExportByDate(date)

        expect(mockedApi.get).toHaveBeenCalledWith(`/exports/schedules/pdf/${date}`)
        expect(result).toEqual(mockExportResponse)
      })

      it('should handle errors for quick PDF export', async () => {
        const date = '2024-01-15'
        mockedApi.get.mockRejectedValue(new Error('Failed to generate quick PDF export'))

        await expect(exportService.quickPdfExportByDate(date)).rejects.toThrow()
        expect(mockToast.error).toHaveBeenCalledWith('Failed to generate quick PDF export')
      })
    })
  })

  describe('Excel Export Methods', () => {
    describe('generateExcelExport', () => {
      it('should generate Excel export successfully', async () => {
        const excelRequest = { ...mockExportRequest, format: 'EXCEL' }
        mockedApi.post.mockResolvedValue({
          data: mockExportResponse
        })

        const result = await exportService.generateExcelExport(excelRequest)

        expect(mockedApi.post).toHaveBeenCalledWith('/exports/excel', excelRequest)
        expect(result).toEqual(mockExportResponse)
      })

      it('should handle API errors for Excel export', async () => {
        const excelRequest = { ...mockExportRequest, format: 'EXCEL' }
        mockedApi.post.mockRejectedValue(new Error('Failed to generate Excel export'))

        await expect(exportService.generateExcelExport(excelRequest)).rejects.toThrow()
        expect(mockToast.error).toHaveBeenCalledWith('Failed to generate Excel export')
      })
    })

    describe('quickExcelExportByDate', () => {
      it('should generate quick Excel export by date', async () => {
        const date = '2024-01-15'
        mockedApi.get.mockResolvedValue({
          data: mockExportResponse
        })

        const result = await exportService.quickExcelExportByDate(date)

        expect(mockedApi.get).toHaveBeenCalledWith(`/exports/schedules/excel/${date}`)
        expect(result).toEqual(mockExportResponse)
      })
    })
  })

  describe('Batch Export Methods', () => {
    describe('generateBatchExport', () => {
      it('should generate batch export successfully', async () => {
        const batchRequest = { ...mockExportRequest, entityIds: [1, 2, 3] }
        const batchResponse = { ...mockExportResponse, message: 'Batch export job started successfully' }
        mockedApi.post.mockResolvedValue({
          data: batchResponse
        })

        const result = await exportService.generateBatchExport(batchRequest)

        expect(mockedApi.post).toHaveBeenCalledWith('/exports/batch', batchRequest)
        expect(result).toEqual(batchResponse)
      })

      it('should handle errors for batch export', async () => {
        const batchRequest = { ...mockExportRequest, entityIds: [1, 2, 3] }
        mockedApi.post.mockRejectedValue(new Error('Failed to generate batch export'))

        await expect(exportService.generateBatchExport(batchRequest)).rejects.toThrow()
        expect(mockToast.error).toHaveBeenCalledWith('Failed to generate batch export')
      })
    })
  })

  describe('Export Status Methods', () => {
    describe('getExportStatus', () => {
      it('should get export job status', async () => {
        const jobId = 'job-123'
        mockedApi.get.mockResolvedValue({
          data: mockExportJob
        })

        const result = await exportService.getExportStatus(jobId)

        expect(mockedApi.get).toHaveBeenCalledWith(`/exports/status/${jobId}`)
        expect(result).toEqual(mockExportJob)
      })

      it('should handle errors when getting export status', async () => {
        const jobId = 'job-123'
        mockedApi.get.mockRejectedValue(new Error('Export job not found'))

        await expect(exportService.getExportStatus(jobId)).rejects.toThrow()
      })
    })

    describe('pollJobCompletion', () => {
      it('should poll job completion until completed', async () => {
        const jobId = 'job-123'
        const completedJob = { ...mockExportJob, status: 'COMPLETED' }

        // Mock first call returns processing, second returns completed
        mockedApi.get
          .mockResolvedValueOnce({ data: { ...mockExportJob, status: 'PROCESSING' } })
          .mockResolvedValueOnce({ data: completedJob })

        const result = await exportService.pollJobCompletion(jobId, 100, 5)

        expect(result).toEqual(completedJob)
        expect(mockedApi.get).toHaveBeenCalledTimes(2)
      })

      it('should timeout when job takes too long', async () => {
        const jobId = 'job-123'
        mockedApi.get.mockResolvedValue({
          data: { ...mockExportJob, status: 'PROCESSING' }
        })

        await expect(exportService.pollJobCompletion(jobId, 100, 2)).rejects.toThrow('Export job timed out')
        expect(mockedApi.get).toHaveBeenCalledTimes(2)
      })

      it('should handle failed jobs', async () => {
        const jobId = 'job-123'
        const failedJob = { ...mockExportJob, status: 'FAILED', errorMessage: 'Generation failed' }
        mockedApi.get.mockResolvedValue({
          data: failedJob
        })

        await expect(exportService.pollJobCompletion(jobId, 100, 5)).rejects.toThrow('Generation failed')
      })
    })
  })

  describe('File Download Methods', () => {
    describe('downloadExport', () => {
      it('should download export file successfully', async () => {
        const fileName = 'schedule-overview-2024-01-01.pdf'
        const mockBlob = new Blob(['mock pdf content'], { type: 'application/pdf' })
        mockedApi.get.mockResolvedValue({
          data: mockBlob
        })

        // Mock URL and DOM methods
        const mockUrl = 'blob:mock-url'
        jest.spyOn(URL, 'createObjectURL').mockReturnValue(mockUrl)
        const mockCreateElement = jest.spyOn(document, 'createElement')
        const mockLink = {
          href: '',
          setAttribute: jest.fn(),
          click: jest.fn()
        }
        mockCreateElement.mockReturnValue(mockLink as any)
        jest.spyOn(document.body, 'appendChild').mockImplementation()
        jest.spyOn(document.body, 'removeChild').mockImplementation()
        jest.spyOn(URL, 'revokeObjectURL').mockImplementation()

        await exportService.downloadExport(fileName)

        expect(mockedApi.get).toHaveBeenCalledWith(`/exports/download/${fileName}`, {
          responseType: 'blob'
        })
        expect(mockLink.click).toHaveBeenCalled()
        expect(mockToast.success).toHaveBeenCalledWith('Download started')
      })

      it('should handle download errors', async () => {
        const fileName = 'schedule-overview-2024-01-01.pdf'
        mockedApi.get.mockRejectedValue(new Error('File not found'))

        await expect(exportService.downloadExport(fileName)).rejects.toThrow()
        expect(mockToast.error).toHaveBeenCalledWith('Failed to download export file')
      })
    })
  })

  describe('Email Export Methods', () => {
    describe('emailExport', () => {
      it('should email export successfully', async () => {
        const jobId = 'job-123'
        const recipients = ['admin@school.edu', 'teacher@school.edu']
        const emailResponse = { ...mockExportResponse, message: 'Export report emailed successfully' }
        mockedApi.post.mockResolvedValue({
          data: emailResponse
        })

        const result = await exportService.emailExport(jobId, recipients)

        expect(mockedApi.post).toHaveBeenCalledWith(`/exports/email/${jobId}`, {
          recipients: 'admin@school.edu,teacher@school.edu',
          subject: 'Scheduling System Export Report',
          body: 'Please find attached the exported report from the scheduling system.'
        })
        expect(result).toEqual(emailResponse)
        expect(mockToast.success).toHaveBeenCalledWith('Export report emailed successfully')
      })

      it('should handle email errors', async () => {
        const jobId = 'job-123'
        const recipients = ['admin@school.edu']
        mockedApi.post.mockRejectedValue(new Error('Failed to send email'))

        await expect(exportService.emailExport(jobId, recipients)).rejects.toThrow()
        expect(mockToast.error).toHaveBeenCalledWith('Failed to email export report')
      })
    })
  })

  describe('Export History Methods', () => {
    describe('getExportHistory', () => {
      it('should get export history with default limit', async () => {
        const mockHistory = [mockExportJob]
        mockedApi.get.mockResolvedValue({
          data: mockHistory
        })

        const result = await exportService.getExportHistory()

        expect(mockedApi.get).toHaveBeenCalledWith('/exports/history?limit=50')
        expect(result).toEqual(mockHistory)
      })

      it('should get export history with custom limit', async () => {
        const mockHistory = [mockExportJob]
        mockedApi.get.mockResolvedValue({
          data: mockHistory
        })

        const result = await exportService.getExportHistory(25)

        expect(mockedApi.get).toHaveBeenCalledWith('/exports/history?limit=25')
        expect(result).toEqual(mockHistory)
      })

      it('should handle empty export history', async () => {
        mockedApi.get.mockResolvedValue({
          data: []
        })

        const result = await exportService.getExportHistory()

        expect(result).toEqual([])
      })
    })
  })

  describe('Template and Format Methods', () => {
    describe('getExportTemplates', () => {
      it('should get available export templates', async () => {
        const mockTemplates = [mockExportTemplate]
        mockedApi.get.mockResolvedValue({
          data: mockTemplates
        })

        const result = await exportService.getExportTemplates()

        expect(mockedApi.get).toHaveBeenCalledWith('/exports/templates')
        expect(result).toEqual(mockTemplates)
      })
    })

    describe('getExportFormats', () => {
      it('should get supported export formats', async () => {
        const mockFormats = [mockExportFormatInfo]
        mockedApi.get.mockResolvedValue({
          data: mockFormats
        })

        const result = await exportService.getExportFormats()

        expect(mockedApi.get).toHaveBeenCalledWith('/exports/formats')
        expect(result).toEqual(mockFormats)
      })
    })
  })

  describe('Analytics Methods', () => {
    describe('getScheduleUtilization', () => {
      it('should get schedule utilization analytics', async () => {
        const mockAnalytics = {
          overallUtilization: 75,
          averageClassSize: 28,
          efficiencyScore: 85
        }
        mockedApi.get.mockResolvedValue({
          data: mockAnalytics
        })

        const result = await exportService.getScheduleUtilization('FALL', '2024')

        expect(result).toEqual(mockAnalytics)
      })

      it('should handle analytics errors', async () => {
        mockedApi.get.mockRejectedValue(new Error('Analytics service unavailable'))

        await expect(exportService.getScheduleUtilization('FALL', '2024')).rejects.toThrow()
      })
    })

    describe('getTeacherWorkloadAnalysis', () => {
      it('should get teacher workload analysis', async () => {
        const mockAnalysis = {
          totalTeachers: 45,
          averageWorkload: 32,
          overloadedTeachers: 3,
          departmentWorkloads: [
            { department: 'Computer Science', teachers: 12, averageHours: 35, totalHours: 420 }
          ]
        }
        mockedApi.get.mockResolvedValue({
          data: mockAnalysis
        })

        const result = await exportService.getTeacherWorkloadAnalysis('FALL', '2024')

        expect(result).toEqual(mockAnalysis)
      })
    })

    describe('getRecentReports', () => {
      it('should get recent reports', async () => {
        const mockReports = [{
          id: 'job-123',
          name: 'SCHEDULE_OVERVIEW - PDF',
          generatedAt: '2024-01-15T10:00:00Z',
          downloadUrl: '/api/v1/exports/download/schedule-overview.pdf',
          status: 'COMPLETED',
          fileSize: 1024000
        }]

        // Mock getExportHistory
        const mockHistory = [mockExportJob]
        mockedApi.get.mockResolvedValue({
          data: mockHistory
        })

        const result = await exportService.getRecentReports()

        expect(mockedApi.get).toHaveBeenCalledWith('/exports/history?limit=10')
        expect(result).toHaveLength(1)
        expect(result[0].id).toBe('job-123')
        expect(result[0].name).toBe('SCHEDULE_OVERVIEW - PDF')
      })
    })
  })

  describe('Utility Methods', () => {
    describe('getReportTypeName', () => {
      it('should return correct report type names', () => {
        expect(exportService.getReportTypeName('SCHEDULE_OVERVIEW')).toBe('Schedule Overview')
        expect(exportService.getReportTypeName('TEACHER_WORKLOAD')).toBe('Teacher Workload Analysis')
        expect(exportService.getReportTypeName('UNKNOWN_TYPE')).toBe('UNKNOWN_TYPE')
      })
    })

    describe('getFormatIcon', () => {
      it('should return correct format icons', () => {
        expect(exportService.getFormatIcon('PDF')).toBe('📄')
        expect(exportService.getFormatIcon('EXCEL')).toBe('📊')
        expect(exportService.getFormatIcon('CSV')).toBe('📋')
        expect(exportService.getFormatIcon('UNKNOWN')).toBe('📄')
      })
    })

    describe('getStatusColor', () => {
      it('should return correct status colors', () => {
        expect(exportService.getStatusColor('PENDING')).toBe('text-yellow-600')
        expect(exportService.getStatusColor('PROCESSING')).toBe('text-blue-600')
        expect(exportService.getStatusColor('COMPLETED')).toBe('text-green-600')
        expect(exportService.getStatusColor('FAILED')).toBe('text-red-600')
        expect(exportService.getStatusColor('UNKNOWN')).toBe('text-gray-600')
      })
    })

    describe('formatFileSize', () => {
      it('should format file sizes correctly', () => {
        expect(exportService.formatFileSize(0)).toBe('0 Bytes')
        expect(exportService.formatFileSize(1024)).toBe('1 KB')
        expect(exportService.formatFileSize(1048576)).toBe('1 MB')
        expect(exportService.formatFileSize(1073741824)).toBe('1 GB')
      })
    })

    describe('formatDateTime', () => {
      it('should format date and time correctly', () => {
        const result = exportService.formatDateTime('2024-01-15T10:30:00Z')
        expect(result).toContain('2024')
        expect(result).toContain('10:30')
      })
    })
  })
})