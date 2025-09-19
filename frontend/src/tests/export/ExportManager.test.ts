import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import ExportManager from '@/components/exports/ExportManager.vue'
import exportService from '@/services/exportService'
import { useToast } from 'vue-toastification'
import { ref } from 'vue'

// Mock the export service and toast
vi.mock('@/services/exportService')
vi.mock('vue-toastification')

const mockToast = {
  error: vi.fn(),
  success: vi.fn(),
  info: vi.fn(),
  warning: vi.fn()
}

vi.mocked(useToast).mockReturnValue(mockToast)

describe('ExportManager', () => {
  const mockExportJob = {
    jobId: 'job-123',
    format: 'PDF' as const,
    request: {
      reportType: 'SCHEDULE_OVERVIEW',
      format: 'PDF' as const,
      startDate: '2024-01-01',
      endDate: '2024-01-31'
    },
    createdAt: '2024-01-15T10:00:00Z',
    completedAt: '2024-01-15T10:05:00Z',
    status: 'COMPLETED' as const,
    fileName: 'schedule-overview.pdf',
    filePath: '/exports/schedule-overview.pdf'
  }

  const mockExportResponse = {
    success: true,
    message: 'Export job started successfully',
    jobId: 'job-123',
    status: 'PENDING' as const
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()

    // Mock export service methods
    vi.mocked(exportService.getExportHistory).mockResolvedValue([mockExportJob])
    vi.mocked(exportService.generatePdfExport).mockResolvedValue(mockExportResponse)
    vi.mocked(exportService.generateExcelExport).mockResolvedValue(mockExportResponse)
    vi.mocked(exportService.quickPdfExportByDate).mockResolvedValue(mockExportResponse)
    vi.mocked(exportService.quickExcelExportByDate).mockResolvedValue(mockExportResponse)
    vi.mocked(exportService.pollJobCompletion).mockResolvedValue(mockExportJob)
    vi.mocked(exportService.downloadExport).mockResolvedValue(undefined)
    vi.mocked(exportService.emailExport).mockResolvedValue(mockExportResponse)
    vi.mocked(exportService.getReportTypeName).mockImplementation((type) => type)
    vi.mocked(exportService.getFormatIcon).mockImplementation((format) => format)
    vi.mocked(exportService.getStatusColor).mockImplementation((status) => `text-${status.toLowerCase()}-600`)
    vi.mocked(exportService.formatDateTime).mockImplementation((date) => date)
    vi.mocked(exportService.formatFileSize).mockImplementation((bytes) => `${bytes} bytes`)
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  const mountComponent = () => {
    return mount(ExportManager, {
      global: {
        stubs: {
          Card: { template: '<div><slot /></div>' },
          CardHeader: { template: '<div><slot /></div>' },
          CardTitle: { template: '<div><slot /></div>' },
          CardDescription: { template: '<div><slot /></div>' },
          CardContent: { template: '<div><slot /></div>' },
          Button: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          Badge: { template: '<span><slot /></span>' },
          Input: { template: '<input v-model="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />' },
          Select: { template: '<select v-model="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><slot /></select>' },
          Modal: { template: '<div v-if="show"><slot /></div>', props: ['show'] },
          Progress: { template: '<div :value="value"><slot /></div>', props: ['value'] }
        },
        provide: {
          toast: mockToast
        }
      }
    })
  }

  it('renders export manager correctly', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('h1').text()).toBe('Export Manager')
    expect(wrapper.text()).toContain('Generate and manage schedule exports in various formats')
  })

  it('loads export history on mount', async () => {
    const wrapper = mountComponent()
    await vi.runAllTimersAsync()

    expect(exportService.getExportHistory).toHaveBeenCalledWith(50)
  })

  it('displays export history when available', async () => {
    const wrapper = mountComponent()
    await vi.runAllTimersAsync()

    expect(wrapper.text()).toContain('Export History')
    expect(wrapper.text()).toContain('schedule-overview.pdf')
  })

  it('shows empty state when no export history', async () => {
    vi.mocked(exportService.getExportHistory).mockResolvedValue([])
    const wrapper = mountComponent()
    await vi.runAllTimersAsync()

    expect(wrapper.text()).toContain('No export history available')
  })

  it('opens export modal when new export button is clicked', async () => {
    const wrapper = mountComponent()

    await wrapper.find('[data-testid="new-export-button"]').trigger('click')

    expect(wrapper.vm.showExportModal).toBe(true)
  })

  it('generates PDF export correctly', async () => {
    const wrapper = mountComponent()
    await wrapper.vm.showExportModal = true

    // Set form data
    await wrapper.setData({
      exportForm: {
        reportType: 'SCHEDULE_OVERVIEW',
        format: 'PDF',
        startDate: '2024-01-01',
        endDate: '2024-01-31',
        includeCharts: true,
        includeAnalytics: true,
        emailReport: false,
        emailRecipients: ''
      }
    })

    await wrapper.vm.generateExport()

    expect(exportService.generatePdfExport).toHaveBeenCalledWith(
      expect.objectContaining({
        reportType: 'SCHEDULE_OVERVIEW',
        format: 'PDF'
      })
    )
    expect(mockToast.success).toHaveBeenCalledWith('Export job started successfully')
    expect(wrapper.vm.showExportModal).toBe(false)
  })

  it('generates Excel export correctly', async () => {
    const wrapper = mountComponent()
    await wrapper.vm.showExportModal = true

    // Set form data for Excel
    await wrapper.setData({
      exportForm: {
        reportType: 'SCHEDULE_OVERVIEW',
        format: 'EXCEL',
        startDate: '2024-01-01',
        endDate: '2024-01-31',
        includeCharts: false,
        includeAnalytics: false,
        emailReport: false,
        emailRecipients: ''
      }
    })

    await wrapper.vm.generateExport()

    expect(exportService.generateExcelExport).toHaveBeenCalledWith(
      expect.objectContaining({
        reportType: 'SCHEDULE_OVERVIEW',
        format: 'EXCEL'
      })
    )
  })

  it('handles export generation errors', async () => {
    vi.mocked(exportService.generatePdfExport).mockRejectedValue(new Error('Export failed'))
    const wrapper = mountComponent()
    await wrapper.vm.showExportModal = true

    await wrapper.setData({
      exportForm: {
        reportType: 'SCHEDULE_OVERVIEW',
        format: 'PDF',
        startDate: '2024-01-01',
        endDate: '2024-01-31',
        includeCharts: true,
        includeAnalytics: true,
        emailReport: false,
        emailRecipients: ''
      }
    })

    await wrapper.vm.generateExport()

    expect(mockToast.error).toHaveBeenCalledWith('Failed to generate export')
  })

  it('performs quick PDF export for today', async () => {
    const wrapper = mountComponent()

    await wrapper.vm.quickExportToday('PDF')

    expect(exportService.quickPdfExportByDate).toHaveBeenCalledWith(
      expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/)
    )
    expect(mockToast.success).toHaveBeenCalledWith('PDF export started successfully')
  })

  it('performs quick Excel export for today', async () => {
    const wrapper = mountComponent()

    await wrapper.vm.quickExportToday('EXCEL')

    expect(exportService.quickExcelExportByDate).toHaveBeenCalledWith(
      expect.stringMatching(/^\d{4}-\d{2}-\d{2}$/)
    )
    expect(mockToast.success).toHaveBeenCalledWith('Excel export started successfully')
  })

  it('downloads export file correctly', async () => {
    const wrapper = mountComponent()

    await wrapper.vm.downloadExport('schedule-overview.pdf')

    expect(exportService.downloadExport).toHaveBeenCalledWith('schedule-overview.pdf')
    expect(mockToast.success).toHaveBeenCalledWith('Download started')
  })

  it('handles download errors', async () => {
    vi.mocked(exportService.downloadExport).mockRejectedValue(new Error('Download failed'))
    const wrapper = mountComponent()

    await wrapper.vm.downloadExport('schedule-overview.pdf')

    expect(mockToast.error).toHaveBeenCalledWith('Failed to download export')
  })

  it('opens email modal correctly', async () => {
    const wrapper = mountComponent()

    await wrapper.vm.openEmailModal(mockExportJob)

    expect(wrapper.vm.showEmailModal).toBe(true)
    expect(wrapper.vm.selectedJob).toBe(mockExportJob)
    expect(wrapper.vm.emailForm.subject).toBe('Export Report: SCHEDULE_OVERVIEW')
  })

  it('sends email export correctly', async () => {
    const wrapper = mountComponent()
    await wrapper.vm.showEmailModal = true
    await wrapper.vm.selectedJob = mockExportJob

    await wrapper.setData({
      emailForm: {
        recipients: 'admin@school.edu, teacher@school.edu',
        subject: 'Test Export Report',
        message: 'Please find attached the export report.'
      }
    })

    await wrapper.vm.sendEmail()

    expect(exportService.emailExport).toHaveBeenCalledWith('job-123', [
      'admin@school.edu',
      'teacher@school.edu'
    ])
    expect(mockToast.success).toHaveBeenCalledWith('Email sent successfully')
    expect(wrapper.vm.showEmailModal).toBe(false)
  })

  it('validates email recipients before sending', async () => {
    const wrapper = mountComponent()
    await wrapper.vm.showEmailModal = true
    await wrapper.vm.selectedJob = mockExportJob

    await wrapper.setData({
      emailForm: {
        recipients: '', // Empty recipients
        subject: 'Test Export Report',
        message: 'Please find attached the export report.'
      }
    })

    await wrapper.vm.sendEmail()

    expect(exportService.emailExport).not.toHaveBeenCalled()
    expect(mockToast.error).not.toHaveBeenCalled() // Should silently fail validation
  })

  it('retries failed exports correctly', async () => {
    const failedJob = { ...mockExportJob, status: 'FAILED' as const }
    const wrapper = mountComponent()

    await wrapper.vm.retryExport(failedJob)

    expect(exportService.generatePdfExport).toHaveBeenCalledWith(
      expect.objectContaining({
        reportType: 'SCHEDULE_OVERVIEW',
        format: 'PDF'
      })
    )
    expect(mockToast.success).toHaveBeenCalledWith('Export retry started successfully')
  })

  it('refreshes export history', async () => {
    const wrapper = mountComponent()

    await wrapper.vm.refreshHistory()

    expect(exportService.getExportHistory).toHaveBeenCalledWith(50)
    expect(mockToast.success).toHaveBeenCalledWith('Export history refreshed')
  })

  it('displays active exports correctly', async () => {
    const activeJob = { ...mockExportJob, status: 'PROCESSING' as const }
    vi.mocked(exportService.getExportHistory).mockResolvedValue([activeJob])
    const wrapper = mountComponent()
    await vi.runAllTimersAsync()

    expect(wrapper.text()).toContain('Active Exports')
    expect(wrapper.text()).toContain('PROCESSING')
  })

  it('updates active exports when history is loaded', async () => {
    const processingJob = { ...mockExportJob, status: 'PROCESSING' as const }
    const completedJob = { ...mockExportJob, status: 'COMPLETED' as const }
    vi.mocked(exportService.getExportHistory).mockResolvedValue([processingJob, completedJob])
    const wrapper = mountComponent()
    await vi.runAllTimersAsync()

    expect(wrapper.vm.activeExports).toHaveLength(1)
    expect(wrapper.vm.activeExports[0].status).toBe('PROCESSING')
  })

  it('polls active jobs periodically', async () => {
    const processingJob = { ...mockExportJob, status: 'PROCESSING' as const }
    vi.mocked(exportService.getExportHistory).mockResolvedValue([processingJob])
    const wrapper = mountComponent()
    await vi.runAllTimersAsync()

    // Advance timer to trigger polling
    await vi.advanceTimersByTime(5000)

    expect(exportService.getExportHistory).toHaveBeenCalledTimes(2)
  })

  it('stops polling when component is unmounted', async () => {
    const wrapper = mountComponent()
    await vi.runAllTimersAsync()

    wrapper.unmount()
    await vi.advanceTimersByTime(5000)

    // Should not call getExportHistory again after unmount
    expect(exportService.getExportHistory).toHaveBeenCalledTimes(1)
  })

  it('calculates job progress correctly', () => {
    const wrapper = mountComponent()
    const processingJob = { ...mockExportJob, status: 'PROCESSING' as const }

    const progress = wrapper.vm.getJobProgress(processingJob)

    expect(progress).toBeGreaterThanOrEqual(70)
    expect(progress).toBeLessThanOrEqual(100)
  })

  it('returns 0 progress for non-processing jobs', () => {
    const wrapper = mountComponent()
    const completedJob = { ...mockExportJob, status: 'COMPLETED' as const }

    const progress = wrapper.vm.getJobProgress(completedJob)

    expect(progress).toBe(0)
  })

  it('displays correct status variants', () => {
    const wrapper = mountComponent()

    expect(wrapper.vm.getStatusVariant('COMPLETED')).toBe('default')
    expect(wrapper.vm.getStatusVariant('FAILED')).toBe('destructive')
    expect(wrapper.vm.getStatusVariant('PROCESSING')).toBe('secondary')
    expect(wrapper.vm.getStatusVariant('PENDING')).toBe('outline')
  })

  it('shows info toast for batch export placeholder', () => {
    const wrapper = mountComponent()

    wrapper.vm.openBatchExport()

    expect(mockToast.info).toHaveBeenCalledWith('Batch export feature coming soon')
  })

  it('shows info toast for template manager placeholder', () => {
    const wrapper = mountComponent()

    wrapper.vm.openTemplateManager()

    expect(mockToast.info).toHaveBeenCalledWith('Template manager feature coming soon')
  })

  it('polls job completion after export generation', async () => {
    const wrapper = mountComponent()

    await wrapper.vm.showExportModal = true
    await wrapper.setData({
      exportForm: {
        reportType: 'SCHEDULE_OVERVIEW',
        format: 'PDF',
        startDate: '2024-01-01',
        endDate: '2024-01-31',
        includeCharts: true,
        includeAnalytics: true,
        emailReport: false,
        emailRecipients: ''
      }
    })

    await wrapper.vm.generateExport()

    expect(exportService.pollJobCompletion).toHaveBeenCalledWith('job-123')
  })

  it('handles job completion errors gracefully', async () => {
    vi.mocked(exportService.pollJobCompletion).mockRejectedValue(new Error('Job failed'))
    const wrapper = mountComponent()

    await wrapper.vm.showExportModal = true
    await wrapper.setData({
      exportForm: {
        reportType: 'SCHEDULE_OVERVIEW',
        format: 'PDF',
        startDate: '2024-01-01',
        endDate: '2024-01-31',
        includeCharts: true,
        includeAnalytics: true,
        emailReport: false,
        emailRecipients: ''
      }
    })

    await wrapper.vm.generateExport()

    expect(mockToast.error).toHaveBeenCalledWith('Failed to check export status')
  })
})