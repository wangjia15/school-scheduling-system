<template>
  <div class="space-y-6">
    <!-- Header with statistics -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
              <BookOpen class="w-4 h-4 text-blue-600" />
            </div>
            <div>
              <div class="text-2xl font-bold">{{ statistics.totalCourses }}</div>
              <div class="text-xs text-muted-foreground">Total Courses</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
              <GraduationCap class="w-4 h-4 text-green-600" />
            </div>
            <div>
              <div class="text-2xl font-bold">{{ statistics.activeCourses }}</div>
              <div class="text-xs text-muted-foreground">Active Courses</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-purple-100 rounded-full flex items-center justify-center">
              <Flask class="w-4 h-4 text-purple-600" />
            </div>
            <div>
              <div class="text-2xl font-bold">{{ statistics.labCourses }}</div>
              <div class="text-xs text-muted-foreground">Lab Courses</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-orange-100 rounded-full flex items-center justify-center">
              <Clock class="w-4 h-4 text-orange-600" />
            </div>
            <div>
              <div class="text-2xl font-bold">{{ statistics.avgCredits }}</div>
              <div class="text-xs text-muted-foreground">Avg Credits</div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Advanced Filters -->
    <Card>
      <CardHeader>
        <CardTitle>Search & Filter</CardTitle>
        <CardDescription>Find courses by various criteria</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div>
            <label class="block text-sm font-medium mb-1">Search</label>
            <Input
              v-model="filters.search"
              placeholder="Course code, title, or description..."
              @input="debouncedSearch"
            />
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Department</label>
            <SelectInput
              v-model="filters.departmentId"
              :options="departmentOptions"
              placeholder="All departments"
              @change="handleFilterChange"
            />
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Level</label>
            <SelectInput
              v-model="filters.level"
              :options="levelOptions"
              placeholder="All levels"
              @change="handleFilterChange"
            />
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Course Type</label>
            <SelectInput
              v-model="filters.courseType"
              :options="courseTypeOptions"
              placeholder="All types"
              @change="handleFilterChange"
            />
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Credits Range</label>
            <div class="flex space-x-2">
              <Input
                v-model.number="filters.minCredits"
                type="number"
                placeholder="Min"
                min="1"
                max="6"
                @input="debouncedSearch"
              />
              <Input
                v-model.number="filters.maxCredits"
                type="number"
                placeholder="Max"
                min="1"
                max="6"
                @input="debouncedSearch"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Status</label>
            <SelectInput
              v-model="filters.active"
              :options="statusOptions"
              placeholder="All statuses"
              @change="handleFilterChange"
            />
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Lab Requirement</label>
            <SelectInput
              v-model="filters.requiresLab"
              :options="labOptions"
              placeholder="All"
              @change="handleFilterChange"
            />
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Sort By</label>
            <SelectInput
              v-model="sortBy"
              :options="sortOptions"
              @change="handleSortChange"
            />
          </div>
        </div>

        <div class="flex justify-between items-center mt-4">
          <div class="text-sm text-muted-foreground">
            {{ courses.length }} courses found
          </div>
          <div class="flex space-x-2">
            <Button variant="outline" size="sm" @click="resetFilters">
              <RotateCcw class="w-4 h-4 mr-2" />
              Reset Filters
            </Button>
            <Button size="sm" @click="exportCatalog">
              <Download class="w-4 h-4 mr-2" />
              Export
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Course Grid -->
    <div v-if="loading" class="flex justify-center py-8">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
    </div>

    <div v-else-if="courses.length === 0" class="text-center py-8">
      <BookOpen class="w-12 h-12 mx-auto text-muted-foreground mb-4" />
      <p class="text-muted-foreground">No courses found matching your criteria</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="course in courses"
        :key="course.id"
        class="bg-card border rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
        @click="viewCourseDetails(course)"
      >
        <!-- Course Header -->
        <div class="flex justify-between items-start mb-3">
          <div>
            <div class="flex items-center space-x-2">
              <h3 class="font-semibold text-lg">{{ course.courseCode }}</h3>
              <Badge :variant="getLevelVariant(course.level)" size="sm">
                {{ formatLevel(course.level) }}
              </Badge>
            </div>
            <p class="text-sm text-muted-foreground">{{ course.department.name }}</p>
          </div>
          <Badge :variant="course.isActive ? 'default' : 'secondary'">
            {{ course.isActive ? 'Active' : 'Inactive' }}
          </Badge>
        </div>

        <!-- Course Title -->
        <h4 class="font-medium mb-2 line-clamp-2">{{ course.title }}</h4>

        <!-- Course Description -->
        <p v-if="course.description" class="text-sm text-muted-foreground mb-3 line-clamp-3">
          {{ course.description }}
        </p>

        <!-- Course Details -->
        <div class="grid grid-cols-2 gap-2 text-xs">
          <div class="flex items-center space-x-1">
            <Star class="w-3 h-3 text-yellow-500" />
            <span>{{ course.credits }} credits</span>
          </div>
          <div class="flex items-center space-x-1">
            <Clock class="w-3 h-3 text-blue-500" />
            <span>{{ course.contactHoursPerWeek }}h/week</span>
          </div>
          <div class="flex items-center space-x-1">
            <Users class="w-3 h-3 text-green-500" />
            <span>{{ course.minStudents }}-{{ course.maxStudents }}</span>
          </div>
          <div v-if="course.requiresLab" class="flex items-center space-x-1">
            <Flask class="w-3 h-3 text-purple-500" />
            <span>Lab required</span>
          </div>
        </div>

        <!-- Prerequisites -->
        <div v-if="course.prerequisites && course.prerequisites.length > 0" class="mt-3 pt-3 border-t">
          <div class="flex items-center space-x-2 mb-2">
            <GitBranch class="w-3 h-3 text-orange-500" />
            <span class="text-xs font-medium">Prerequisites ({{ course.prerequisites.length }})</span>
          </div>
          <div class="text-xs text-muted-foreground">
            {{ course.prerequisites.slice(0, 2).map(p => p.prerequisiteCourse.courseCode).join(', ') }}
            <span v-if="course.prerequisites.length > 2">
              +{{ course.prerequisites.length - 2 }} more
            </span>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex justify-between items-center mt-4 pt-3 border-t">
          <div class="flex space-x-2">
            <Button variant="ghost" size="sm" @click.stop="viewCourseDetails(course)">
              <Eye class="w-3 h-3" />
            </Button>
            <Button
              v-if="hasPrerequisites(course)"
              variant="ghost"
              size="sm"
              @click.stop="viewPrerequisites(course)"
            >
              <GitBranch class="w-3 h-3" />
            </Button>
          </div>
          <Button variant="outline" size="sm" @click.stop="enrollInCourse(course)">
            <Plus class="w-3 h-3 mr-1" />
            Enroll
          </Button>
        </div>
      </div>
    </div>

    <!-- Load More -->
    <div v-if="hasMoreCourses" class="flex justify-center">
      <Button
        variant="outline"
        @click="loadMoreCourses"
        :disabled="loadingMore"
      >
        {{ loadingMore ? 'Loading...' : 'Load More' }}
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import SelectInput from '@/components/ui/SelectInput.vue'
import Badge from '@/components/ui/Badge.vue'
import { useToast } from 'vue-toastification'
import type { Course, CourseFilter } from '@/types/course'
import { courseService } from '@/services/courseService'
import {
  BookOpen,
  GraduationCap,
  Flask,
  Clock,
  Star,
  Users,
  GitBranch,
  Eye,
  Plus,
  RotateCcw,
  Download
} from 'lucide-vue-next'

interface Props {
  departments: Array<{ id: number; code: string; name: string }>
}

const props = defineProps<Props>()

const toast = useToast()

// State
const courses = ref<Course[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMoreCourses = ref(true)

// Filters
const filters = reactive<CourseFilter>({
  search: '',
  departmentId: undefined,
  level: undefined,
  active: undefined,
  minCredits: undefined,
  maxCredits: undefined,
  requiresLab: undefined,
  courseType: undefined
})

const sortBy = ref('title')
const sortDirection = ref('ASC')
const currentPage = ref(0)

// Statistics
const statistics = reactive({
  totalCourses: 0,
  activeCourses: 0,
  labCourses: 0,
  avgCredits: 0
})

// Options
const departmentOptions = computed(() => [
  { value: '', label: 'All Departments' },
  ...props.departments.map(dept => ({
    value: dept.id.toString(),
    label: `${dept.code} - ${dept.name}`
  }))
])

const levelOptions = [
  { value: '', label: 'All Levels' },
  { value: 'UNDERGRADUATE', label: 'Undergraduate' },
  { value: 'GRADUATE', label: 'Graduate' },
  { value: 'PHD', label: 'PhD' }
]

const courseTypeOptions = [
  { value: '', label: 'All Types' },
  { value: 'CORE', label: 'Core Course' },
  { value: 'ELECTIVE', label: 'Elective' },
  { value: 'SPECIAL', label: 'Special Course' }
]

const statusOptions = [
  { value: '', label: 'All Statuses' },
  { value: 'true', label: 'Active' },
  { value: 'false', label: 'Inactive' }
]

const labOptions = [
  { value: '', label: 'All' },
  { value: 'true', label: 'Requires Lab' },
  { value: 'false', label: 'No Lab Required' }
]

const sortOptions = [
  { value: 'title', label: 'Title' },
  { value: 'courseCode', label: 'Course Code' },
  { value: 'credits', label: 'Credits' },
  { value: 'level', label: 'Level' },
  { value: 'department', label: 'Department' }
]

// Methods
const loadCourses = async (reset: boolean = false) => {
  try {
    if (reset) {
      loading.value = true
      currentPage.value = 0
      courses.value = []
    } else {
      loadingMore.value = true
    }

    const response = await courseService.getCourses(
      currentPage.value,
      20,
      sortBy.value,
      sortDirection.value,
      filters
    )

    if (reset) {
      courses.value = response.courses
    } else {
      courses.value.push(...response.courses)
    }

    hasMoreCourses.value = (currentPage.value + 1) * 20 < response.total
    currentPage.value++

    // Update statistics
    updateStatistics(response.courses)
  } catch (error) {
    toast.error('Failed to load courses')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const updateStatistics = (courseList: Course[]) => {
  statistics.totalCourses = courseList.length
  statistics.activeCourses = courseList.filter(c => c.isActive).length
  statistics.labCourses = courseList.filter(c => c.requiresLab).length
  statistics.avgCredits = courseList.length > 0
    ? Math.round((courseList.reduce((sum, c) => sum + c.credits, 0) / courseList.length) * 10) / 10
    : 0
}

let searchTimeout: NodeJS.Timeout
const debouncedSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    loadCourses(true)
  }, 300)
}

const handleFilterChange = () => {
  loadCourses(true)
}

const handleSortChange = () => {
  loadCourses(true)
}

const resetFilters = () => {
  Object.assign(filters, {
    search: '',
    departmentId: undefined,
    level: undefined,
    active: undefined,
    minCredits: undefined,
    maxCredits: undefined,
    requiresLab: undefined,
    courseType: undefined
  })
  sortBy.value = 'title'
  sortDirection.value = 'ASC'
  loadCourses(true)
}

const loadMoreCourses = () => {
  loadCourses(false)
}

const viewCourseDetails = (course: Course) => {
  // Navigate to course details or show modal
  toast.info(`Viewing details for ${course.courseCode}`)
}

const viewPrerequisites = (course: Course) => {
  // Navigate to prerequisite management
  toast.info(`Viewing prerequisites for ${course.courseCode}`)
}

const enrollInCourse = (course: Course) => {
  // Handle course enrollment
  toast.info(`Enrolling in ${course.courseCode}`)
}

const exportCatalog = () => {
  // Export course catalog
  toast.info('Exporting course catalog...')
}

const getLevelVariant = (level: string) => {
  switch (level) {
    case 'UNDERGRADUATE': return 'default'
    case 'GRADUATE': return 'secondary'
    case 'PHD': return 'destructive'
    default: return 'default'
  }
}

const formatLevel = (level: string) => {
  return level.charAt(0) + level.slice(1).toLowerCase()
}

const hasPrerequisites = (course: Course) => {
  return course.prerequisites && course.prerequisites.length > 0
}

// Lifecycle
onMounted(() => {
  loadCourses(true)
})
</script>