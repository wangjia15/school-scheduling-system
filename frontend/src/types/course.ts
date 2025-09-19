export interface Course {
  id: number;
  courseCode: string;
  title: string;
  description?: string;
  department: {
    id: number;
    code: string;
    name: string;
  };
  credits: number;
  contactHoursPerWeek: number;
  theoryHours: number;
  labHours: number;
  level: 'UNDERGRADUATE' | 'GRADUATE' | 'PHD';
  isActive: boolean;
  maxStudents: number;
  minStudents: number;
  requiresLab: boolean;
  prerequisites: CoursePrerequisite[];
  createdAt: string;
  updatedAt: string;
}

export interface CoursePrerequisite {
  id: number;
  prerequisiteCourse: Course;
  isMandatory: boolean;
  minimumGrade?: number;
  notes?: string;
}

export interface CourseRequest {
  courseCode: string;
  title: string;
  description?: string;
  departmentId: number;
  credits: number;
  contactHoursPerWeek: number;
  theoryHours: number;
  labHours: number;
  level: 'UNDERGRADUATE' | 'GRADUATE' | 'PHD';
  maxStudents: number;
  minStudents: number;
  requiresLab: boolean;
  prerequisites?: PrerequisiteRequest[];
}

export interface PrerequisiteRequest {
  prerequisiteCourseId: number;
  isMandatory: boolean;
  minimumGrade?: number;
  notes?: string;
}

export interface CourseFilter {
  search?: string;
  departmentId?: number;
  level?: string;
  active?: boolean;
}

export interface CourseSearchResponse {
  courses: Course[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

export interface CourseTreeNode {
  course: Course;
  children: CourseTreeNode[];
  depth: number;
  isExpanded: boolean;
  isSelected: boolean;
}