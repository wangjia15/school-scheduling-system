import axios, { AxiosInstance } from 'axios';
import { useAuthStore } from '@/stores/auth';
import type {
  Course,
  CourseRequest,
  CourseFilter,
  CourseSearchResponse,
  CoursePrerequisite
} from '@/types/course';

class CourseService {
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

  async getCourses(
    page: number = 0,
    size: number = 20,
    sort: string = 'id',
    direction: string = 'ASC',
    filter?: CourseFilter
  ): Promise<CourseSearchResponse> {
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sort,
        direction,
      });

      if (filter?.search) params.append('search', filter.search);
      if (filter?.departmentId) params.append('departmentId', filter.departmentId.toString());
      if (filter?.level) params.append('level', filter.level);
      if (filter?.active !== undefined) params.append('active', filter.active.toString());

      const response = await this.api.get(`/courses?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching courses:', error);
      throw new Error('Failed to fetch courses');
    }
  }

  async getCourseById(id: number): Promise<Course> {
    try {
      const response = await this.api.get(`/courses/${id}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching course:', error);
      throw new Error('Failed to fetch course');
    }
  }

  async getCourseByCode(courseCode: string): Promise<Course> {
    try {
      const response = await this.api.get(`/courses/code/${courseCode}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching course by code:', error);
      throw new Error('Failed to fetch course by code');
    }
  }

  async createCourse(course: CourseRequest): Promise<Course> {
    try {
      const response = await this.api.post('/courses', course);
      return response.data.data;
    } catch (error) {
      console.error('Error creating course:', error);
      throw new Error('Failed to create course');
    }
  }

  async updateCourse(id: number, course: CourseRequest): Promise<Course> {
    try {
      const response = await this.api.put(`/courses/${id}`, course);
      return response.data.data;
    } catch (error) {
      console.error('Error updating course:', error);
      throw new Error('Failed to update course');
    }
  }

  async deleteCourse(id: number): Promise<void> {
    try {
      await this.api.delete(`/courses/${id}`);
    } catch (error) {
      console.error('Error deleting course:', error);
      throw new Error('Failed to delete course');
    }
  }

  async getCoursesByDepartment(departmentId: number): Promise<Course[]> {
    try {
      const response = await this.api.get(`/courses/by-department/${departmentId}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching courses by department:', error);
      throw new Error('Failed to fetch courses by department');
    }
  }

  async getCoursesByLevel(level: string): Promise<Course[]> {
    try {
      const response = await this.api.get(`/courses/by-level/${level}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching courses by level:', error);
      throw new Error('Failed to fetch courses by level');
    }
  }

  async getCoursePrerequisites(courseId: number): Promise<Course[]> {
    try {
      const response = await this.api.get(`/courses/prerequisites/${courseId}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching course prerequisites:', error);
      throw new Error('Failed to fetch course prerequisites');
    }
  }

  async getLabCourses(): Promise<Course[]> {
    try {
      const response = await this.api.get('/courses/lab-courses');
      return response.data.data;
    } catch (error) {
      console.error('Error fetching lab courses:', error);
      throw new Error('Failed to fetch lab courses');
    }
  }

  async validatePrerequisites(courseId: number): Promise<{
    isValid: boolean;
    issues: string[];
    circularDependencies: string[];
  }> {
    try {
      const response = await this.api.get(`/courses/${courseId}/validate-prerequisites`);
      return response.data.data;
    } catch (error) {
      console.error('Error validating prerequisites:', error);
      throw new Error('Failed to validate prerequisites');
    }
  }

  async getPrerequisiteTree(courseId: number): Promise<any> {
    try {
      const response = await this.api.get(`/courses/${courseId}/prerequisite-tree`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching prerequisite tree:', error);
      throw new Error('Failed to fetch prerequisite tree');
    }
  }

  async getCoursesForPrerequisites(search?: string): Promise<Course[]> {
    try {
      const params = new URLSearchParams();
      if (search) params.append('search', search);

      const response = await this.api.get(`/courses/for-prerequisites?${params}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching courses for prerequisites:', error);
      throw new Error('Failed to fetch courses for prerequisites');
    }
  }
}

export const courseService = new CourseService();