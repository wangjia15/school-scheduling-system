import axios, { AxiosInstance } from 'axios';
import { useAuthStore } from '@/stores/auth';

interface ScheduleUtilizationReport {
  totalSchedules: number;
  scheduledSchedules: number;
  utilizationRate: number;
  teacherUtilization: {
    teacherId: number;
    teacherName: string;
    scheduledHours: number;
    maxHours: number;
    utilizationRate: number;
  }[];
  classroomUtilization: {
    classroomId: number;
    roomNumber: string;
    building: string;
    scheduledHours: number;
    availableHours: number;
    utilizationRate: number;
  }[];
  departmentUtilization: {
    departmentId: number;
    departmentName: string;
    scheduledCourses: number;
    totalCourses: number;
    utilizationRate: number;
  }[];
}

interface ConflictReport {
  totalConflicts: number;
  resolvedConflicts: number;
  openConflicts: number;
  resolutionRate: number;
  averageResolutionTime: number;
  conflictsByType: {
    type: string;
    count: number;
    percentage: number;
  }[];
  conflictsBySeverity: {
    severity: string;
    count: number;
    percentage: number;
  }[];
  conflictTrends: {
    date: string;
    newConflicts: number;
    resolvedConflicts: number;
  }[];
}

interface TeacherWorkloadReport {
  teacherId: number;
  teacherName: string;
  department: string;
  scheduledCourses: number;
  scheduledHours: number;
  maxWeeklyHours: number;
  workloadPercentage: number;
  courses: {
    courseCode: string;
    courseName: string;
    credits: number;
    scheduledHours: number;
  }[];
  availability: {
    dayOfWeek: string;
    preferredHours: number;
    scheduledHours: number;
  }[];
}

interface ClassroomUsageReport {
  classroomId: number;
  roomNumber: string;
  building: string;
  roomType: string;
  capacity: number;
  totalPossibleHours: number;
  scheduledHours: number;
  utilizationRate: number;
  peakUsageTimes: {
    dayOfWeek: string;
    timeSlot: string;
    utilization: number;
  }[];
  maintenancePeriods: {
    startDate: string;
    endDate: string;
    reason: string;
  }[];
}

interface CourseEnrollmentReport {
  courseId: number;
  courseCode: string;
  courseName: string;
  maxStudents: number;
  enrolledStudents: number;
  waitlistedStudents: number;
  enrollmentRate: number;
  sections: {
    section: string;
    teacher: string;
    classroom: string;
    schedule: string;
    enrolled: number;
    capacity: number;
  }[];
}

interface SystemMetricsReport {
  totalTeachers: number;
  totalStudents: number;
  totalClassrooms: number;
  totalCourses: number;
  activeSemester: string;
  systemHealth: {
    databaseStatus: 'HEALTHY' | 'DEGRADED' | 'UNHEALTHY';
    apiResponseTime: number;
    lastBackup: string;
    activeUsers: number;
  };
  performanceMetrics: {
    averageScheduleGenerationTime: number;
    averageConflictDetectionTime: number;
    peakConcurrentUsers: number;
  };
}

interface ReportRequest {
  reportType: string;
  parameters: Record<string, any>;
  format: 'PDF' | 'EXCEL' | 'CSV' | 'JSON';
}

interface GeneratedReport {
  id: string;
  reportType: string;
  parameters: Record<string, any>;
  status: 'PENDING' | 'GENERATING' | 'COMPLETED' | 'FAILED';
  downloadUrl?: string;
  generatedAt?: string;
  error?: string;
}

class ReportService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
      timeout: 30000, // Longer timeout for report generation
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add auth interceptor
    this.api.interceptors.request.use((config) => {
      const authStore = useAuthStore();
      if (authStore.token) {
        config.headers.Authorization = `Bearer ${authStore.token}`;
      }
      return config;
    });

    // Add response interceptor for error handling
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          const authStore = useAuthStore();
          authStore.logout();
        }
        return Promise.reject(error);
      }
    );
  }

  async getScheduleUtilization(
    semester: string,
    academicYear: string
  ): Promise<ScheduleUtilizationReport> {
    try {
      const params = new URLSearchParams();
      params.append('semester', semester);
      params.append('academicYear', academicYear);

      const response = await this.api.get(`/reports/schedule-utilization?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching schedule utilization report:', error);
      throw new Error('Failed to fetch schedule utilization report');
    }
  }

  async getConflictReport(
    startDate: string,
    endDate: string
  ): Promise<ConflictReport> {
    try {
      const params = new URLSearchParams();
      params.append('startDate', startDate);
      params.append('endDate', endDate);

      const response = await this.api.get(`/reports/conflicts?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching conflict report:', error);
      throw new Error('Failed to fetch conflict report');
    }
  }

  async getTeacherWorkloadReport(
    semester: string,
    academicYear: string,
    departmentId?: number
  ): Promise<TeacherWorkloadReport[]> {
    try {
      const params = new URLSearchParams();
      params.append('semester', semester);
      params.append('academicYear', academicYear);
      if (departmentId !== undefined) {
        params.append('departmentId', departmentId.toString());
      }

      const response = await this.api.get(`/reports/teacher-workload?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching teacher workload report:', error);
      throw new Error('Failed to fetch teacher workload report');
    }
  }

  async getClassroomUsageReport(
    startDate: string,
    endDate: string,
    building?: string
  ): Promise<ClassroomUsageReport[]> {
    try {
      const params = new URLSearchParams();
      params.append('startDate', startDate);
      params.append('endDate', endDate);
      if (building) params.append('building', building);

      const response = await this.api.get(`/reports/classroom-usage?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching classroom usage report:', error);
      throw new Error('Failed to fetch classroom usage report');
    }
  }

  async getCourseEnrollmentReport(
    semester: string,
    academicYear: string,
    departmentId?: number
  ): Promise<CourseEnrollmentReport[]> {
    try {
      const params = new URLSearchParams();
      params.append('semester', semester);
      params.append('academicYear', academicYear);
      if (departmentId !== undefined) {
        params.append('departmentId', departmentId.toString());
      }

      const response = await this.api.get(`/reports/course-enrollment?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching course enrollment report:', error);
      throw new Error('Failed to fetch course enrollment report');
    }
  }

  async getSystemMetrics(): Promise<SystemMetricsReport> {
    try {
      const response = await this.api.get('/reports/system-metrics');
      return response.data.data;
    } catch (error) {
      console.error('Error fetching system metrics:', error);
      throw new Error('Failed to fetch system metrics');
    }
  }

  async generateReport(request: ReportRequest): Promise<GeneratedReport> {
    try {
      const response = await this.api.post('/reports/generate', request);
      return response.data.data;
    } catch (error) {
      console.error('Error generating report:', error);
      throw new Error('Failed to generate report');
    }
  }

  async downloadReport(reportId: string): Promise<Blob> {
    try {
      const response = await this.api.get(`/reports/${reportId}/download`, {
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      console.error('Error downloading report:', error);
      throw new Error('Failed to download report');
    }
  }

  async getReportHistory(
    page: number = 0,
    size: number = 20
  ): Promise<{
    content: GeneratedReport[];
    totalElements: number;
    totalPages: number;
  }> {
    try {
      const params = new URLSearchParams();
      params.append('page', page.toString());
      params.append('size', size.toString());

      const response = await this.api.get(`/reports/history?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching report history:', error);
      throw new Error('Failed to fetch report history');
    }
  }

  async getDashboardStats(): Promise<{
    totalSchedules: number;
    activeConflicts: number;
    systemUtilization: number;
    pendingActions: number;
    recentActivity: {
      type: string;
      description: string;
      timestamp: string;
    }[];
  }> {
    try {
      const response = await this.api.get('/reports/dashboard-stats');
      return response.data.data;
    } catch (error) {
      console.error('Error fetching dashboard stats:', error);
      throw new Error('Failed to fetch dashboard stats');
    }
  }

  async exportScheduleCalendar(
    semester: string,
    academicYear: string,
    format: 'PDF' | 'ICAL' | 'EXCEL' = 'PDF'
  ): Promise<Blob> {
    try {
      const params = new URLSearchParams();
      params.append('semester', semester);
      params.append('academicYear', academicYear);
      params.append('format', format);

      const response = await this.api.get(`/reports/export-calendar?${params}`, {
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      console.error('Error exporting schedule calendar:', error);
      throw new Error('Failed to export schedule calendar');
    }
  }
}

export const reportService = new ReportService();
export type {
  ScheduleUtilizationReport,
  ConflictReport,
  TeacherWorkloadReport,
  ClassroomUsageReport,
  CourseEnrollmentReport,
  SystemMetricsReport,
  ReportRequest,
  GeneratedReport
};