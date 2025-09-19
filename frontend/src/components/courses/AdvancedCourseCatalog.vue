<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h2 class="text-2xl font-bold">Advanced Course Catalog</h2>
        <p class="text-muted-foreground">Search, filter, and explore courses with advanced criteria</p>
      </div>
      <div class="flex space-x-2">
        <Button @click="showComparison = true" variant="outline" :disabled="selectedCourses.length < 2">
          <BarChart3 class="w-4 h-4 mr-2" />
          Compare ({{ selectedCourses.length }})
        </Button>
        <Button @click="exportCatalog">
          <Download class="w-4 h-4 mr-2" />
          Export
        </Button>
      </div>
    </div>

    <!-- Advanced Search Section -->
    <Card>
      <CardHeader>
        <CardTitle>Advanced Search</CardTitle>
        <CardDescription>Search courses by multiple criteria</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-sm font-medium mb-1">Search</label>
            <Input
              v-model="searchFilters.query"
              placeholder="Course code, title, or description..."
              @input="debouncedSearch"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Department</label>
            <SelectInput
              v-model="searchFilters.departmentId"
              :options="departmentOptions"
              placeholder="All departments"
              @change="performSearch"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Level</label>
            <SelectInput
              v-model="searchFilters.level"
              :options="levelOptions"
              placeholder="All levels"
              @change="performSearch"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Credits</label>
            <SelectInput
              v-model="searchFilters.credits"
              :options="creditOptions"
              placeholder="Any credits"
              @change="performSearch"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Schedule Type</label>
            <SelectInput
              v-model="searchFilters.scheduleType"
              :options="scheduleOptions"
              placeholder="Any type"
              @change="performSearch"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Availability</label>
            <SelectInput
              v-model="searchFilters.availability"
              :options="availabilityOptions"
              placeholder="Any availability"
              @change="performSearch"
            />
          </div>
        </div>

        <!-- Advanced Filters Toggle -->
        <div class="mt-4">
          <Button
            @click="showAdvancedFilters = !showAdvancedFilters"
            variant="outline"
            size="sm"
          >
            <Settings class="w-4 h-4 mr-2" />
            {{ showAdvancedFilters ? 'Hide' : 'Show' }} Advanced Filters
          </Button>
        </div>

        <!-- Advanced Filters -->
        <div v-if="showAdvancedFilters" class="mt-4 grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-sm font-medium mb-1">Min Students</label>
            <Input
              v-model.number="searchFilters.minStudents"
              type="number"
              placeholder="Min capacity"
              @input="debouncedSearch"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Max Students</label>
            <Input
              v-model.number="searchFilters.maxStudents"
              type="number"
              placeholder="Max capacity"
              @input="debouncedSearch"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Lab Required</label>
            <select
              v-model="searchFilters.requiresLab"
              class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              @change="performSearch"
            >
              <option value="">Any</option>
              <option value="true">Yes</option>
              <option value="false">No</option>
            </select>
          </div>
        </div>

        <!-- Search Actions -->
        <div class="mt-4 flex justify-between items-center">
          <div class="text-sm text-muted-foreground">
            Found {{ searchResults.total }} courses
          </div>
          <div class="flex space-x-2">
            <Button @click="clearFilters" variant="outline" size="sm">
              Clear Filters
            </Button>
            <Button @click="performSearch" size="sm">
              <Search class="w-4 h-4 mr-2" />
              Search
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Results View Toggle -->
    <div class="flex justify-between items-center">
      <div class="flex space-x-2">
        <Button
          @click="viewMode = 'grid'"
          :variant="viewMode === 'grid' ? 'default' : 'outline'"
          size="sm"
        >
          <Grid3X3 class="w-4 h-4 mr-2" />
          Grid
        </Button>
        <Button
          @click="viewMode = 'list'"
          :variant="viewMode === 'list' ? 'default' : 'outline'"
          size="sm"
        >
          <List class="w-4 h-4 mr-2" />
          List
        </Button>
        <Button
          @click="viewMode = 'table'"
          :variant="viewMode === 'table' ? 'default' : 'outline'"
          size="sm"
        >
          <Table class="w-4 h-4 mr-2" />
          Table
        </Button>
      </div>
      <div class="flex items-center space-x-2">
        <label class="text-sm font-medium">Sort by:</label>
        <SelectInput
          v-model="sortBy"
          :options="sortOptions"
          @change="performSearch"
        />
      </div>
    </div>

    <!-- Search Results -->
    <div v-if="loading" class="flex justify-center py-8">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
    </div>

    <div v-else-if="searchResults.courses.length === 0" class="text-center py-8">
      <Search class="w-12 h-12 mx-auto text-muted-foreground mb-4" />
      <h3 class="text-lg font-semibold mb-2">No courses found</h3>
      <p class="text-muted-foreground">Try adjusting your search criteria</p>
    </div>

    <!-- Grid View -->
    <div v-else-if="viewMode === 'grid'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="course in searchResults.courses"
        :key="course.id"
        class="border rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
        :class="{ 'ring-2 ring-primary': isSelected(course) }"
        @click="toggleCourseSelection(course)"
      >
        <div class="flex justify-between items-start mb-2">
          <div>
            <h3 class="font-semibold">{{ course.courseCode }}</h3>
            <Badge :variant="getLevelVariant(course.level)" class="text-xs">
              {{ formatLevel(course.level) }}
            </Badge>
          </div>
          <input
            type="checkbox"
            :checked="isSelected(course)"
            @click.stop
            @change="toggleCourseSelection(course)"
            class="rounded"
          />
        </div>

        <h4 class="font-medium mb-1">{{ course.title }}</h4>
        <p class="text-sm text-muted-foreground mb-3 line-clamp-2">
          {{ course.description }}
        </p>

        <div class="space-y-2 text-sm">
          <div class="flex justify-between">
            <span class="text-muted-foreground">Department:</span>
            <span>{{ course.department.name }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-muted-foreground">Credits:</span>
            <span>{{ course.credits }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-muted-foreground">Hours:</span>
            <span>{{ course.contactHoursPerWeek }}/week</span>
          </div>
          <div class="flex justify-between">
            <span class="text-muted-foreground">Capacity:</span>
            <span>{{ course.minStudents }}-{{ course.maxStudents }}</span>
          </div>
        </div>

        <div class="flex flex-wrap gap-1 mt-3">
          <Badge v-if="course.requiresLab" variant="outline" class="text-xs">
            Lab Required
          </Badge>
          <Badge v-if="course.hasPrerequisites" variant="outline" class="text-xs">
            Has Prerequisites
          </Badge>
          <Badge :variant="course.isActive ? 'default' : 'secondary'" class="text-xs">
            {{ course.isActive ? 'Active' : 'Inactive' }}
          </Badge>
        </div>
      </div>
    </div>

    <!-- List View -->
    <div v-else-if="viewMode === 'list'" class="space-y-3">
      <div
        v-for="course in searchResults.courses"
        :key="course.id"
        class="border rounded-lg p-4 hover:bg-muted/50 transition-colors"
        :class="{ 'ring-2 ring-primary': isSelected(course) }"
      >
        <div class="flex items-start space-x-4">
          <input
            type="checkbox"
            :checked="isSelected(course)"
            @change="toggleCourseSelection(course)"
            class="rounded mt-1"
          />
          <div class="flex-1">
            <div class="flex justify-between items-start mb-2">
              <div>
                <div class="flex items-center space-x-2 mb-1">
                  <h3 class="font-semibold">{{ course.courseCode }}</h3>
                  <Badge :variant="getLevelVariant(course.level)">
                    {{ formatLevel(course.level) }}
                  </Badge>
                  <Badge :variant="course.isActive ? 'default' : 'secondary'">
                    {{ course.isActive ? 'Active' : 'Inactive' }}
                  </Badge>
                </div>
                <h4 class="font-medium text-lg">{{ course.title }}</h4>
              </div>
              <div class="text-right">
                <div class="text-lg font-semibold">{{ course.credits }} credits</div>
                <div class="text-sm text-muted-foreground">{{ course.contactHoursPerWeek }} hrs/week</div>
              </div>
            </div>

            <p class="text-muted-foreground mb-3">{{ course.description }}</p>

            <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
              <div>
                <span class="text-muted-foreground">Department:</span>
                <span class="ml-1 font-medium">{{ course.department.name }}</span>
              </div>
              <div>
                <span class="text-muted-foreground">Capacity:</span>
                <span class="ml-1 font-medium">{{ course.minStudents }}-{{ course.maxStudents }}</span>
              </div>
              <div>
                <span class="text-muted-foreground">Theory/Lab:</span>
                <span class="ml-1 font-medium">{{ course.theoryHours }}/{{ course.labHours }}</span>
              </div>
              <div>
                <span class="text-muted-foreground">Prerequisites:</span>
                <span class="ml-1 font-medium">{{ course.hasPrerequisites ? 'Yes' : 'None' }}</span>
              </div>
            </div>

            <div class="flex flex-wrap gap-2 mt-3">
              <Badge v-if="course.requiresLab" variant="outline">
                <FlaskConical class="w-3 h-3 mr-1" />
                Lab Required
              </Badge>
              <Badge v-if="course.hasPrerequisites" variant="outline">
                <GitBranch class="w-3 h-3 mr-1" />
                Has Prerequisites
              </Badge>
              <Badge v-if="course.isLabCourse()" variant="outline">
                <Beaker class="w-3 h-3 mr-1" />
                Lab Course
              </Badge>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Table View -->
    <div v-else-if="viewMode === 'table'" class="border rounded-lg overflow-hidden">
      <table class="w-full">
        <thead class="bg-muted">
          <tr>
            <th class="text-left p-4">
              <input
                type="checkbox"
                :checked="allSelected"
                @change="toggleSelectAll"
                class="rounded"
              />
            </th>
            <th class="text-left p-4 font-medium">Course Code</th>
            <th class="text-left p-4 font-medium">Title</th>
            <th class="text-left p-4 font-medium">Department</th>
            <th class="text-left p-4 font-medium">Level</th>
            <th class="text-left p-4 font-medium">Credits</th>
            <th class="text-left p-4 font-medium">Hours</th>
            <th class="text-left p-4 font-medium">Capacity</th>
            <th class="text-left p-4 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="course in searchResults.courses"
            :key="course.id"
            class="border-t hover:bg-muted/50"
          >
            <td class="p-4">
              <input
                type="checkbox"
                :checked="isSelected(course)"
                @change="toggleCourseSelection(course)"
                class="rounded"
              />
            </td>
            <td class="p-4 font-medium">{{ course.courseCode }}</td>
            <td class="p-4">{{ course.title }}</td>
            <td class="p-4">{{ course.department.name }}</td>
            <td class="p-4">
              <Badge :variant="getLevelVariant(course.level)" class="text-xs">
                {{ formatLevel(course.level) }}
              </Badge>
            </td>
            <td class="p-4">{{ course.credits }}</td>
            <td class="p-4">{{ course.contactHoursPerWeek }}</td>
            <td class="p-4">{{ course.minStudents }}-{{ course.maxStudents }}</td>
            <td class="p-4">
              <div class="flex space-x-1">
                <Button variant="ghost" size="sm" @click="viewCourseDetails(course)">
                  <Eye class="w-4 h-4" />
                </Button>
                <Button variant="ghost" size="sm" @click="addToComparison(course)">
                  <Plus class="w-4 h-4" />
                </Button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="searchResults.totalPages > 1" class="flex justify-between items-center">
      <div class="text-sm text-muted-foreground">
        Showing {{ (searchResults.page * searchResults.size) + 1 }} to
        {{ Math.min((searchResults.page + 1) * searchResults.size, searchResults.total) }}
        of {{ searchResults.total }} courses
      </div>
      <div class="flex space-x-2">
        <Button
          variant="outline"
          size="sm"
          :disabled="searchResults.page === 0"
          @click="changePage(searchResults.page - 1)"
        >
          Previous
        </Button>
        <Button
          variant="outline"
          size="sm"
          :disabled="searchResults.page >= searchResults.totalPages - 1"
          @click="changePage(searchResults.page + 1)"
        >
          Next
        </Button>
      </div>
    </div>

    <!-- Course Comparison Modal -->
    <div v-if="showComparison" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <Card class="w-full max-w-6xl max-h-[90vh] overflow-y-auto">
        <CardHeader>
          <CardTitle>Course Comparison</CardTitle>
          <CardDescription>Compare selected courses side by side</CardDescription>
        </CardHeader>
        <CardContent>
          <CourseComparison
            :courses="selectedCourses"
            @remove-course="removeFromComparison"
            @close="showComparison = false"
          />
        </CardContent>
      </Card>
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
import CourseComparison from './CourseComparison.vue'
import { useToast } from 'vue-toastification'
import type { Course } from '@/types/course'
import { courseService } from '@/services/courseService'
import {
  Search,
  Download,
  BarChart3,
  Grid3X3,
  List,
  Table,
  Settings,
  Eye,
  Plus,
  FlaskConical,
  GitBranch,
  Beaker
} from 'lucide-vue-next'

interface Props {
  courses: Course[]
  departments: Array<{ id: number; code: string; name: string }>
}

const props = defineProps<Props>()

const toast = useToast()

// State
const loading = ref(false)
const viewMode = ref<'grid' | 'list' | 'table'>('grid')
const showAdvancedFilters = ref(false)
const showComparison = ref(false)
const selectedCourses = ref<Course[]>([])

// Search
const searchFilters = reactive({
  query: '',
  departmentId: '',
  level: '',
  credits: '',
  scheduleType: '',
  availability: '',
  minStudents: '',
  maxStudents: '',
  requiresLab: ''
})

const sortBy = ref('relevance')
const searchResults = reactive({
  courses: [] as Course[],
  total: 0,
  page: 0,
  size: 20,
  totalPages: 0
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

const creditOptions = [
  { value: '', label: 'Any Credits' },
  { value: '1', label: '1 Credit' },
  { value: '2', label: '2 Credits' },
  { value: '3', label: '3 Credits' },
  { value: '4', label: '4 Credits' },
  { value: '5', label: '5 Credits' },
  { value: '6', label: '6 Credits' }
]

const scheduleOptions = [
  { value: '', label: 'Any Schedule' },
  { value: 'day', label: 'Day Classes' },
  { value: 'evening', label: 'Evening Classes' },
  { value: 'weekend', label: 'Weekend Classes' },
  { value: 'online', label: 'Online Classes' }
]

const availabilityOptions = [
  { value: '', label: 'Any Availability' },
  { value: 'open', label: 'Open for Registration' },
  { value: 'limited', label: 'Limited Space' },
  { value: 'full', label: 'Full' },
  { value: 'waitlist', label: 'Waitlist Available' }
]

const sortOptions = [
  { value: 'relevance', label: 'Relevance' },
  { value: 'courseCode', label: 'Course Code' },
  { value: 'title', label: 'Title' },
  { value: 'credits', label: 'Credits' },
  { value: 'department', label: 'Department' },
  { value: 'level', label: 'Level' }
]

// Computed
const allSelected = computed(() => {
  return searchResults.courses.length > 0 && searchResults.courses.every(course => isSelected(course))
})

// Methods
const performSearch = async () => {
  try {
    loading.value = true

    // Build filter object
    const filters: any = {}
    if (searchFilters.query) filters.search = searchFilters.query
    if (searchFilters.departmentId) filters.departmentId = parseInt(searchFilters.departmentId)
    if (searchFilters.level) filters.level = searchFilters.level
    if (searchFilters.credits) filters.credits = parseInt(searchFilters.credits)
    if (searchFilters.minStudents) filters.minStudents = parseInt(searchFilters.minStudents)
    if (searchFilters.maxStudents) filters.maxStudents = parseInt(searchFilters.maxStudents)
    if (searchFilters.requiresLab) filters.requiresLab = searchFilters.requiresLab === 'true'

    // Mock search results - in real implementation, this would call an API
    let filteredCourses = [...props.courses]

    // Apply filters
    if (filters.search) {
      const query = filters.search.toLowerCase()
      filteredCourses = filteredCourses.filter(course =>
        course.courseCode.toLowerCase().includes(query) ||
        course.title.toLowerCase().includes(query) ||
        (course.description && course.description.toLowerCase().includes(query))
      )
    }

    if (filters.departmentId) {
      filteredCourses = filteredCourses.filter(course => course.department.id === filters.departmentId)
    }

    if (filters.level) {
      filteredCourses = filteredCourses.filter(course => course.level === filters.level)
    }

    if (filters.credits) {
      filteredCourses = filteredCourses.filter(course => course.credits === filters.credits)
    }

    // Apply sorting
    if (sortBy.value === 'courseCode') {
      filteredCourses.sort((a, b) => a.courseCode.localeCompare(b.courseCode))
    } else if (sortBy.value === 'title') {
      filteredCourses.sort((a, b) => a.title.localeCompare(b.title))
    } else if (sortBy.value === 'credits') {
      filteredCourses.sort((a, b) => a.credits - b.credits)
    }

    // Apply pagination
    const start = searchResults.page * searchResults.size
    const end = start + searchResults.size
    const paginatedCourses = filteredCourses.slice(start, end)

    Object.assign(searchResults, {
      courses: paginatedCourses,
      total: filteredCourses.length,
      totalPages: Math.ceil(filteredCourses.length / searchResults.size)
    })
  } catch (error) {
    toast.error('Failed to search courses')
  } finally {
    loading.value = false
  }
}

const debouncedSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    searchResults.page = 0
    performSearch()
  }, 300)
}

const clearFilters = () => {
  Object.assign(searchFilters, {
    query: '',
    departmentId: '',
    level: '',
    credits: '',
    scheduleType: '',
    availability: '',
    minStudents: '',
    maxStudents: '',
    requiresLab: ''
  })
  performSearch()
}

const changePage = (page: number) => {
  searchResults.page = page
  performSearch()
}

const isSelected = (course: Course) => {
  return selectedCourses.value.some(c => c.id === course.id)
}

const toggleCourseSelection = (course: Course) => {
  const index = selectedCourses.value.findIndex(c => c.id === course.id)
  if (index > -1) {
    selectedCourses.value.splice(index, 1)
  } else {
    selectedCourses.value.push(course)
  }
}

const toggleSelectAll = () => {
  if (allSelected.value) {
    selectedCourses.value = []
  } else {
    selectedCourses.value = [...searchResults.courses]
  }
}

const addToComparison = (course: Course) => {
  if (!isSelected(course)) {
    selectedCourses.value.push(course)
    toast.success('Course added to comparison')
  }
}

const removeFromComparison = (course: Course) => {
  const index = selectedCourses.value.findIndex(c => c.id === course.id)
  if (index > -1) {
    selectedCourses.value.splice(index, 1)
  }
}

const viewCourseDetails = (course: Course) => {
  // Navigate to course details or show modal
  toast.info('Course details view not implemented yet')
}

const exportCatalog = () => {
  const data = searchResults.courses.map(course => ({
    'Course Code': course.courseCode,
    'Title': course.title,
    'Department': course.department.name,
    'Level': course.level,
    'Credits': course.credits,
    'Contact Hours': course.contactHoursPerWeek,
    'Theory Hours': course.theoryHours,
    'Lab Hours': course.labHours,
    'Min Students': course.minStudents,
    'Max Students': course.maxStudents,
    'Requires Lab': course.requiresLab ? 'Yes' : 'No',
    'Active': course.isActive ? 'Yes' : 'No',
    'Description': course.description || ''
  }))

  // Create CSV content
  const headers = Object.keys(data[0] || {})
  const csv = [
    headers.join(','),
    ...data.map(row => headers.map(header => `"${row[header]}"`).join(','))
  ].join('\n')

  // Download file
  const blob = new Blob([csv], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `course-catalog-${new Date().toISOString().split('T')[0]}.csv`
  a.click()
  window.URL.revokeObjectURL(url)

  toast.success('Course catalog exported successfully')
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

// Lifecycle
let searchTimeout: NodeJS.Timeout
onMounted(() => {
  performSearch()
})
</script>