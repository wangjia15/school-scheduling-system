import axios, { AxiosInstance } from 'axios';
import { useAuthStore } from '@/stores/auth';

interface Schedule {
  id: number;
  courseId: number;
  teacherId: number;
  classroomId: number;
  section: string;
  semester: string;
  academicYear: string;
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  startTime: string;
  endTime: string;
  status: 'DRAFT' | 'SCHEDULED' | 'CONFIRMED' | 'CANCELLED';
  isRecurring: boolean;
  recurringPattern?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
  course: {
    id: number;
    courseCode: string;
    title: string;
    credits: number;
  };
  teacher: {
    id: number;
    employeeId: string;
    user: {
      firstName: string;
      lastName: string;
    };
  };
  classroom: {
    id: number;
    roomNumber: string;
    building: string;
    capacity: number;
  };
}

interface ScheduleRequest {
  courseId: number;
  teacherId: number;
  classroomId: number;
  section: string;
  semester: string;
  academicYear: string;
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  startTime: string;
  endTime: string;
  status: 'DRAFT' | 'SCHEDULED' | 'CONFIRMED' | 'CANCELLED';
  isRecurring: boolean;
  recurringPattern?: string;
  notes?: string;
}

interface ScheduleFilters {
  search?: string;
  semester?: string;
  academicYear?: string;
  dayOfWeek?: string;
  teacherId?: number;
  courseId?: number;
  classroomId?: number;
  status?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
}

interface PaginatedSchedules {
  content: Schedule[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

interface ScheduleGenerationRequest {
  semester: string;
  academicYear: string;
  autoResolveConflicts: boolean;
  optimizationStrategy: 'BALANCED' | 'TEACHER_PREFERENCE' | 'ROOM_UTILIZATION' | 'MINIMIZE_CONFLICTS';
  constraints: {
    maxDailyHoursPerTeacher: number;
    minBreakBetweenClasses: number;
    preferSameRoomForSameCourse: boolean;
    considerTeacherSpecialization: boolean;
  };
}

interface ScheduleGenerationResponse {
  totalSchedules: number;
  successfulSchedules: number;
  conflicts: Conflict[];
  warnings: string[];
  processingTime: number;
}

interface Conflict {
  id: number;
  type: 'TEACHER_CONFLICT' | 'ROOM_CONFLICT' | 'STUDENT_CONFLICT' | 'PREREQUISITE_CONFLICT' | 'CAPACITY_CONFLICT';
  severity: 'HIGH' | 'MEDIUM' | 'LOW';
  description: string;
  affectedSchedules: number[];
  suggestedResolution: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'IGNORED';
  createdAt: string;
  updatedAt: string;
}

interface ScheduleStats {
  totalSchedules: number;
  scheduledSchedules: number;
  confirmedSchedules: number;
  cancelledSchedules: number;
  totalTeachersUtilized: number;
  totalClassroomsUtilized: number;
  averageUtilizationRate: number;
  conflictsCount: number;
}

class SchedulingService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
      timeout: 30000, // Longer timeout for schedule generation
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

  async getSchedules(filters: ScheduleFilters = {}): Promise<PaginatedSchedules> {
    try {
      const params = new URLSearchParams();

      if (filters.search) params.append('search', filters.search);
      if (filters.semester) params.append('semester', filters.semester);
      if (filters.academicYear) params.append('academicYear', filters.academicYear);
      if (filters.dayOfWeek) params.append('dayOfWeek', filters.dayOfWeek);
      if (filters.teacherId !== undefined) params.append('teacherId', filters.teacherId.toString());
      if (filters.courseId !== undefined) params.append('courseId', filters.courseId.toString());
      if (filters.classroomId !== undefined) params.append('classroomId', filters.classroomId.toString());
      if (filters.status) params.append('status', filters.status);
      if (filters.page !== undefined) params.append('page', filters.page.toString());
      if (filters.size !== undefined) params.append('size', filters.size.toString());
      if (filters.sort) params.append('sort', filters.sort);
      if (filters.direction) params.append('direction', filters.direction);

      const response = await this.api.get(`/schedules?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching schedules:', error);
      throw new Error('Failed to fetch schedules');
    }
  }

  async getScheduleById(id: number): Promise<Schedule> {
    try {
      const response = await this.api.get(`/schedules/${id}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching schedule:', error);
      throw new Error('Failed to fetch schedule');
    }
  }

  async createSchedule(schedule: ScheduleRequest): Promise<Schedule> {
    try {
      const response = await this.api.post('/schedules', schedule);
      return response.data.data;
    } catch (error) {
      console.error('Error creating schedule:', error);
      throw new Error('Failed to create schedule');
    }
  }

  async updateSchedule(id: number, schedule: Partial<ScheduleRequest>): Promise<Schedule> {
    try {
      const response = await this.api.put(`/schedules/${id}`, schedule);
      return response.data.data;
    } catch (error) {
      console.error('Error updating schedule:', error);
      throw new Error('Failed to update schedule');
    }
  }

  async deleteSchedule(id: number): Promise<void> {
    try {
      await this.api.delete(`/schedules/${id}`);
    } catch (error) {
      console.error('Error deleting schedule:', error);
      throw new Error('Failed to delete schedule');
    }
  }

  async generateSchedules(request: ScheduleGenerationRequest): Promise<ScheduleGenerationResponse> {
    try {
      const response = await this.api.post('/schedules/generate', request);
      return response.data.data;
    } catch (error) {
      console.error('Error generating schedules:', error);
      throw new Error('Failed to generate schedules');
    }
  }

  async getConflicts(filters: {
    type?: string;
    severity?: string;
    status?: string;
    page?: number;
    size?: number;
  } = {}): Promise<{
    content: Conflict[];
    totalElements: number;
    totalPages: number;
  }> {
    try {
      const params = new URLSearchParams();

      if (filters.type) params.append('type', filters.type);
      if (filters.severity) params.append('severity', filters.severity);
      if (filters.status) params.append('status', filters.status);
      if (filters.page !== undefined) params.append('page', filters.page.toString());
      if (filters.size !== undefined) params.append('size', filters.size.toString());

      const response = await this.api.get(`/schedules/conflicts?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching conflicts:', error);
      throw new Error('Failed to fetch conflicts');
    }
  }

  async resolveConflict(conflictId: number, resolution: string): Promise<void> {
    try {
      await this.api.post(`/schedules/conflicts/${conflictId}/resolve`, { resolution });
    } catch (error) {
      console.error('Error resolving conflict:', error);
      throw new Error('Failed to resolve conflict');
    }
  }

  async getScheduleStats(semester: string, academicYear: string): Promise<ScheduleStats> {
    try {
      const params = new URLSearchParams();
      params.append('semester', semester);
      params.append('academicYear', academicYear);

      const response = await this.api.get(`/schedules/stats?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching schedule stats:', error);
      throw new Error('Failed to fetch schedule stats');
    }
  }

  async getTeacherSchedule(teacherId: number, semester: string, academicYear: string): Promise<Schedule[]> {
    try {
      const params = new URLSearchParams();
      params.append('semester', semester);
      params.append('academicYear', academicYear);

      const response = await this.api.get(`/schedules/teacher/${teacherId}?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching teacher schedule:', error);
      throw new Error('Failed to fetch teacher schedule');
    }
  }

  async getClassroomSchedule(classroomId: number, semester: string, academicYear: string): Promise<Schedule[]> {
    try {
      const params = new URLSearchParams();
      params.append('semester', semester);
      params.append('academicYear', academicYear);

      const response = await this.api.get(`/schedules/classroom/${classroomId}?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching classroom schedule:', error);
      throw new Error('Failed to fetch classroom schedule');
    }
  }

  async getWeeklySchedule(
    semester: string,
    academicYear: string,
    startWeek?: string
  ): Promise<{
    weekStart: string;
    weekEnd: string;
    schedules: Schedule[];
  }> {
    try {
      const params = new URLSearchParams();
      params.append('semester', semester);
      params.append('academicYear', academicYear);
      if (startWeek) params.append('startWeek', startWeek);

      const response = await this.api.get(`/schedules/weekly?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching weekly schedule:', error);
      throw new Error('Failed to fetch weekly schedule');
    }
  }

  async validateSchedule(schedule: Partial<ScheduleRequest>): Promise<{
    isValid: boolean;
    conflicts: Conflict[];
    warnings: string[];
  }> {
    try {
      const response = await this.api.post('/schedules/validate', schedule);
      return response.data.data;
    } catch (error) {
      console.error('Error validating schedule:', error);
      throw new Error('Failed to validate schedule');
    }
  }
}

export const schedulingService = new SchedulingService();
export type {
  Schedule,
  ScheduleRequest,
  ScheduleFilters,
  PaginatedSchedules,
  ScheduleGenerationRequest,
  ScheduleGenerationResponse,
  Conflict,
  ScheduleStats
};