import axios, { AxiosInstance } from 'axios';
import { useAuthStore } from '@/stores/auth';
import type { Conflict } from './schedulingService';

interface ConflictResolution {
  id: number;
  conflictId: number;
  resolution: string;
  actionTaken: string;
  resolvedBy: number;
  resolvedAt: string;
  notes?: string;
}

interface BulkConflictResolution {
  conflictIds: number[];
  resolution: string;
  actionTaken: string;
  notes?: string;
}

interface ConflictAnalytics {
  totalConflicts: number;
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
  conflictsByStatus: {
    status: string;
    count: number;
    percentage: number;
  }[];
  resolutionRate: number;
  averageResolutionTime: number;
  trendingConflicts: {
    type: string;
    trend: 'increasing' | 'decreasing' | 'stable';
    changePercentage: number;
  }[];
}

interface ConflictFilters {
  search?: string;
  type?: string;
  severity?: string;
  status?: string;
  teacherId?: number;
  courseId?: number;
  classroomId?: number;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
}

interface PaginatedConflicts {
  content: Conflict[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

class ConflictService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
      timeout: 10000,
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

  async getConflicts(filters: ConflictFilters = {}): Promise<PaginatedConflicts> {
    try {
      const params = new URLSearchParams();

      if (filters.search) params.append('search', filters.search);
      if (filters.type) params.append('type', filters.type);
      if (filters.severity) params.append('severity', filters.severity);
      if (filters.status) params.append('status', filters.status);
      if (filters.teacherId !== undefined) params.append('teacherId', filters.teacherId.toString());
      if (filters.courseId !== undefined) params.append('courseId', filters.courseId.toString());
      if (filters.classroomId !== undefined) params.append('classroomId', filters.classroomId.toString());
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      if (filters.page !== undefined) params.append('page', filters.page.toString());
      if (filters.size !== undefined) params.append('size', filters.size.toString());
      if (filters.sort) params.append('sort', filters.sort);
      if (filters.direction) params.append('direction', filters.direction);

      const response = await this.api.get(`/conflicts?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching conflicts:', error);
      throw new Error('Failed to fetch conflicts');
    }
  }

  async getConflictById(id: number): Promise<Conflict> {
    try {
      const response = await this.api.get(`/conflicts/${id}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching conflict:', error);
      throw new Error('Failed to fetch conflict');
    }
  }

  async resolveConflict(
    conflictId: number,
    resolution: string,
    actionTaken: string,
    notes?: string
  ): Promise<ConflictResolution> {
    try {
      const response = await this.api.post(`/conflicts/${conflictId}/resolve`, {
        resolution,
        actionTaken,
        notes
      });
      return response.data.data;
    } catch (error) {
      console.error('Error resolving conflict:', error);
      throw new Error('Failed to resolve conflict');
    }
  }

  async bulkResolveConflicts(request: BulkConflictResolution): Promise<ConflictResolution[]> {
    try {
      const response = await this.api.post('/conflicts/bulk-resolve', request);
      return response.data.data;
    } catch (error) {
      console.error('Error bulk resolving conflicts:', error);
      throw new Error('Failed to bulk resolve conflicts');
    }
  }

  async updateConflictStatus(
    conflictId: number,
    status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'IGNORED'
  ): Promise<Conflict> {
    try {
      const response = await this.api.patch(`/conflicts/${conflictId}/status`, { status });
      return response.data.data;
    } catch (error) {
      console.error('Error updating conflict status:', error);
      throw new Error('Failed to update conflict status');
    }
  }

  async getConflictResolutions(conflictId: number): Promise<ConflictResolution[]> {
    try {
      const response = await this.api.get(`/conflicts/${conflictId}/resolutions`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching conflict resolutions:', error);
      throw new Error('Failed to fetch conflict resolutions');
    }
  }

  async getConflictAnalytics(
    startDate: string,
    endDate: string
  ): Promise<ConflictAnalytics> {
    try {
      const params = new URLSearchParams();
      params.append('startDate', startDate);
      params.append('endDate', endDate);

      const response = await this.api.get(`/conflicts/analytics?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching conflict analytics:', error);
      throw new Error('Failed to fetch conflict analytics');
    }
  }

  async getConflictsByTeacher(teacherId: number): Promise<Conflict[]> {
    try {
      const response = await this.api.get(`/conflicts/teacher/${teacherId}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching teacher conflicts:', error);
      throw new Error('Failed to fetch teacher conflicts');
    }
  }

  async getConflictsByClassroom(classroomId: number): Promise<Conflict[]> {
    try {
      const response = await this.api.get(`/conflicts/classroom/${classroomId}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching classroom conflicts:', error);
      throw new Error('Failed to fetch classroom conflicts');
    }
  }

  async getConflictsByCourse(courseId: number): Promise<Conflict[]> {
    try {
      const response = await this.api.get(`/conflicts/course/${courseId}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching course conflicts:', error);
      throw new Error('Failed to fetch course conflicts');
    }
  }

  async autoResolveConflicts(
    conflictIds: number[],
    strategy: 'PRIORITY_BASED' | 'TIME_BASED' | 'RESOURCE_BASED'
  ): Promise<{
    resolved: number;
    failed: number;
    resolutions: ConflictResolution[];
  }> {
    try {
      const response = await this.api.post('/conflicts/auto-resolve', {
        conflictIds,
        strategy
      });
      return response.data.data;
    } catch (error) {
      console.error('Error auto-resolving conflicts:', error);
      throw new Error('Failed to auto-resolve conflicts');
    }
  }

  async exportConflicts(
    filters: ConflictFilters = {},
    format: 'CSV' | 'PDF' | 'EXCEL' = 'CSV'
  ): Promise<Blob> {
    try {
      const params = new URLSearchParams();

      if (filters.search) params.append('search', filters.search);
      if (filters.type) params.append('type', filters.type);
      if (filters.severity) params.append('severity', filters.severity);
      if (filters.status) params.append('status', filters.status);
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      params.append('format', format);

      const response = await this.api.get(`/conflicts/export?${params}`, {
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      console.error('Error exporting conflicts:', error);
      throw new Error('Failed to export conflicts');
    }
  }

  async getConflictTrends(
    days: number = 30
  ): Promise<{
    dates: string[];
    counts: number[];
    resolutions: number[];
  }> {
    try {
      const response = await this.api.get(`/conflicts/trends?days=${days}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching conflict trends:', error);
      throw new Error('Failed to fetch conflict trends');
    }
  }
}

export const conflictService = new ConflictService();
export type {
  ConflictResolution,
  BulkConflictResolution,
  ConflictAnalytics,
  ConflictFilters,
  PaginatedConflicts
};