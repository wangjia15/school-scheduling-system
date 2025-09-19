import axios, { AxiosInstance } from 'axios';
import { useAuthStore } from '@/stores/auth';

interface Classroom {
  id: number;
  roomNumber: string;
  building: string;
  capacity: number;
  roomType: 'CLASSROOM' | 'LABORATORY' | 'LECTURE_HALL' | 'SEMINAR_ROOM' | 'COMPUTER_LAB';
  equipment: string[];
  features: string[];
  isActive: boolean;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

interface ClassroomRequest {
  roomNumber: string;
  building: string;
  capacity: number;
  roomType: 'CLASSROOM' | 'LABORATORY' | 'LECTURE_HALL' | 'SEMINAR_ROOM' | 'COMPUTER_LAB';
  equipment: string[];
  features: string[];
  isActive: boolean;
  notes?: string;
}

interface ClassroomFilters {
  search?: string;
  building?: string;
  roomType?: string;
  minCapacity?: number;
  isActive?: boolean;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
}

interface PaginatedClassrooms {
  content: Classroom[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

class ClassroomService {
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

  async getClassrooms(filters: ClassroomFilters = {}): Promise<PaginatedClassrooms> {
    try {
      const params = new URLSearchParams();

      if (filters.search) params.append('search', filters.search);
      if (filters.building) params.append('building', filters.building);
      if (filters.roomType) params.append('roomType', filters.roomType);
      if (filters.minCapacity !== undefined) params.append('minCapacity', filters.minCapacity.toString());
      if (filters.isActive !== undefined) params.append('isActive', filters.isActive.toString());
      if (filters.page !== undefined) params.append('page', filters.page.toString());
      if (filters.size !== undefined) params.append('size', filters.size.toString());
      if (filters.sort) params.append('sort', filters.sort);
      if (filters.direction) params.append('direction', filters.direction);

      const response = await this.api.get(`/classrooms?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching classrooms:', error);
      throw new Error('Failed to fetch classrooms');
    }
  }

  async getClassroomById(id: number): Promise<Classroom> {
    try {
      const response = await this.api.get(`/classrooms/${id}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching classroom:', error);
      throw new Error('Failed to fetch classroom');
    }
  }

  async createClassroom(classroom: ClassroomRequest): Promise<Classroom> {
    try {
      const response = await this.api.post('/classrooms', classroom);
      return response.data.data;
    } catch (error) {
      console.error('Error creating classroom:', error);
      throw new Error('Failed to create classroom');
    }
  }

  async updateClassroom(id: number, classroom: Partial<ClassroomRequest>): Promise<Classroom> {
    try {
      const response = await this.api.put(`/classrooms/${id}`, classroom);
      return response.data.data;
    } catch (error) {
      console.error('Error updating classroom:', error);
      throw new Error('Failed to update classroom');
    }
  }

  async deleteClassroom(id: number): Promise<void> {
    try {
      await this.api.delete(`/classrooms/${id}`);
    } catch (error) {
      console.error('Error deleting classroom:', error);
      throw new Error('Failed to delete classroom');
    }
  }

  async getClassroomsByBuilding(building: string): Promise<Classroom[]> {
    try {
      const response = await this.api.get(`/classrooms/by-building/${building}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching classrooms by building:', error);
      throw new Error('Failed to fetch classrooms by building');
    }
  }

  async getClassroomsByType(roomType: string): Promise<Classroom[]> {
    try {
      const response = await this.api.get(`/classrooms/by-type/${roomType}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching classrooms by type:', error);
      throw new Error('Failed to fetch classrooms by type');
    }
  }

  async getAvailableClassrooms(
    startTime: string,
    endTime: string,
    dayOfWeek: string,
    requiredCapacity?: number,
    requiredEquipment?: string[]
  ): Promise<Classroom[]> {
    try {
      const params = new URLSearchParams();
      params.append('startTime', startTime);
      params.append('endTime', endTime);
      params.append('dayOfWeek', dayOfWeek);

      if (requiredCapacity !== undefined) {
        params.append('requiredCapacity', requiredCapacity.toString());
      }

      if (requiredEquipment && requiredEquipment.length > 0) {
        requiredEquipment.forEach(equipment => {
          params.append('requiredEquipment', equipment);
        });
      }

      const response = await this.api.get(`/classrooms/available?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching available classrooms:', error);
      throw new Error('Failed to fetch available classrooms');
    }
  }

  async getClassroomUtilization(
    startDate: string,
    endDate: string
  ): Promise<{
    classroomId: number;
    roomNumber: string;
    building: string;
    totalHours: number;
    utilizedHours: number;
    utilizationRate: number;
  }[]> {
    try {
      const params = new URLSearchParams();
      params.append('startDate', startDate);
      params.append('endDate', endDate);

      const response = await this.api.get(`/classrooms/utilization?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching classroom utilization:', error);
      throw new Error('Failed to fetch classroom utilization');
    }
  }
}

export const classroomService = new ClassroomService();
export type { Classroom, ClassroomRequest, ClassroomFilters, PaginatedClassrooms };