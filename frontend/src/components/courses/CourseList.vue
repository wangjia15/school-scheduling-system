<template>
  <div class="space-y-6">
    <!-- Header with actions -->
    <div class="flex justify-between items-center">
      <div>
        <h2 class="text-2xl font-bold">Course Catalog</h2>
        <p class="text-muted-foreground">Manage courses and prerequisites</p>
      </div>
      <Button @click="showCreateForm = true">
        <Plus class="w-4 h-4 mr-2" />
        Add Course
      </Button>
    </div>

    <!-- Search and filters -->
    <Card>
      <CardContent class="p-6">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label class="block text-sm font-medium mb-1">Search</label>
            <Input
              v-model="filters.search"
              placeholder="Search courses..."
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
            <label class="block text-sm font-medium mb-1">Status</label>
            <SelectInput
              v-model="filters.active"
              :options="statusOptions"
              placeholder="All statuses"
              @change="handleFilterChange"
            />
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Course table -->
    <Card>
      <CardContent class="p-0">
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b">
                <th class="text-left p-4 font-medium">Course Code</th>
                <th class="text-left p-4 font-medium">Title</th>
                <th class="text-left p-4 font-medium">Department</th>
                <th class="text-left p-4 font-medium">Level</th>
                <th class="text-left p-4 font-medium">Credits</th>
                <th class="text-left p-4 font-medium">Students</th>
                <th class="text-left p-4 font-medium">Status</th>
                <th class="text-left p-4 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading" class="border-b">
                <td colspan="8" class="p-4 text-center">
                  <div class="flex justify-center">
                    <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
                  </div>
                </td>
              </tr>
              <tr v-else-if="courses.length === 0" class="border-b">
                <td colspan="8" class="p-4 text-center text-muted-foreground">
                  No courses found
                </td>
              </tr>
              <tr v-for="course in courses" :key="course.id" class="border-b hover:bg-muted/50">
                <td class="p-4">
                  <div class="font-medium">{{ course.courseCode }}</div>
                  <div class="text-sm text-muted-foreground">{{ course.department.code }}</div>
                </td>
                <td class="p-4">
                  <div class="font-medium">{{ course.title }}</div>
                  <div v-if="course.description" class="text-sm text-muted-foreground truncate max-w-xs">
                    {{ course.description }}
                  </div>
                </td>
                <td class="p-4">{{ course.department.name }}</td>
                <td class="p-4">
                  <Badge :variant="getLevelVariant(course.level)">
                    {{ formatLevel(course.level) }}
                  </Badge>
                </td>
                <td class="p-4">{{ course.credits }}</td>
                <td class="p-4">
                  <div class="text-sm">
                    <span class="font-medium">{{ course.minStudents }}</span>
                    <span class="text-muted-foreground"> - </span>
                    <span class="font-medium">{{ course.maxStudents }}</span>
                  </div>
                </td>
                <td class="p-4">
                  <Badge :variant="course.isActive ? 'default' : 'secondary'">
                    {{ course.isActive ? 'Active' : 'Inactive' }}
                  </Badge>
                </td>
                <td class="p-4">
                  <div class="flex space-x-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      @click="viewCourse(course)"
                      title="View details"
                    >
                      <Eye class="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      @click="editCourse(course)"
                      title="Edit course"
                    >
                      <Edit class="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      @click="managePrerequisites(course)"
                      title="Manage prerequisites"
                    >
                      <GitBranch class="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      @click="confirmDelete(course)"
                      title="Delete course"
                      class="text-destructive hover:text-destructive"
                    >
                      <Trash2 class="w-4 h-4" />
                    </Button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>

    <!-- Pagination -->
    <div v-if="pagination.totalPages > 1" class="flex justify-between items-center">
      <div class="text-sm text-muted-foreground">
        Showing {{ (pagination.page * pagination.size) + 1 }} to
        {{ Math.min((pagination.page + 1) * pagination.size, pagination.total) }}
        of {{ pagination.total }} courses
      </div>
      <div class="flex space-x-2">
        <Button
          variant="outline"
          size="sm"
          :disabled="pagination.page === 0"
          @click="changePage(pagination.page - 1)"
        >
          Previous
        </Button>
        <Button
          variant="outline"
          size="sm"
          :disabled="pagination.page >= pagination.totalPages - 1"
          @click="changePage(pagination.page + 1)"
        >
          Next
        </Button>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showCreateForm || showEditForm" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <div class="bg-background rounded-lg shadow-lg w-full max-w-4xl max-h-[90vh] overflow-y-auto">
        <CourseForm
          :course="selectedCourse"
          :departments="departments"
          @submit="handleFormSubmit"
          @cancel="cancelForm"
          @success="formSuccess"
        />
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <Card class="w-full max-w-md">
        <CardHeader>
          <CardTitle>Confirm Delete</CardTitle>
          <CardDescription>
            Are you sure you want to delete "{{ selectedCourse?.title }}"? This action cannot be undone.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="flex justify-end space-x-2">
            <Button variant="outline" @click="cancelDelete">Cancel</Button>
            <Button variant="destructive" @click="confirmDeleteAction" :disabled="isDeleting">
              {{ isDeleting ? 'Deleting...' : 'Delete' }}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import SelectInput from '@/components/ui/SelectInput.vue'
import Badge from '@/components/ui/Badge.vue'
import CourseForm from './CourseForm.vue'
import { useToast } from 'vue-toastification'
import type { Course, CourseFilter, CourseRequest } from '@/types/course'
import { courseService } from '@/services/courseService'
import { Plus, Eye, Edit, GitBranch, Trash2 } from 'lucide-vue-next'

interface Props {
  departments: Array<{ id: number; code: string; name: string }>
}

const props = defineProps<Props>()

const toast = useToast()

// State
const courses = ref<Course[]>([])
const loading = ref(false)
const isDeleting = ref(false)
const showCreateForm = ref(false)
const showEditForm = ref(false)
const showDeleteModal = ref(false)
const selectedCourse = ref<Course | null>(null)

// Filters
const filters = reactive<CourseFilter>({
  search: '',
  departmentId: undefined,
  level: undefined,
  active: undefined
})

// Pagination
const pagination = reactive({
  page: 0,
  size: 20,
  total: 0,
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

const statusOptions = [
  { value: '', label: 'All Statuses' },
  { value: 'true', label: 'Active' },
  { value: 'false', label: 'Inactive' }
]

// Debounced search
let searchTimeout: NodeJS.Timeout
const debouncedSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    pagination.page = 0
    loadCourses()
  }, 300)
}

// Methods
const loadCourses = async () => {
  try {
    loading.value = true
    const response = await courseService.getCourses(
      pagination.page,
      pagination.size,
      'id',
      'ASC',
      filters
    )

    courses.value = response.courses
    pagination.total = response.total
    pagination.totalPages = Math.ceil(response.total / response.size)
  } catch (error) {
    toast.error('Failed to load courses')
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  pagination.page = 0
  loadCourses()
}

const changePage = (page: number) => {
  pagination.page = page
  loadCourses()
}

const viewCourse = (course: Course) => {
  // Navigate to course details or show modal
  toast.info('Course details view not implemented yet')
}

const editCourse = (course: Course) => {
  selectedCourse.value = course
  showEditForm.value = true
}

const managePrerequisites = (course: Course) => {
  // Navigate to prerequisite management
  toast.info('Prerequisite management not implemented yet')
}

const confirmDelete = (course: Course) => {
  selectedCourse.value = course
  showDeleteModal.value = true
}

const cancelDelete = () => {
  selectedCourse.value = null
  showDeleteModal.value = false
}

const confirmDeleteAction = async () => {
  if (!selectedCourse.value) return

  try {
    isDeleting.value = true
    await courseService.deleteCourse(selectedCourse.value.id)
    toast.success('Course deleted successfully')
    loadCourses()
    cancelDelete()
  } catch (error) {
    toast.error('Failed to delete course')
  } finally {
    isDeleting.value = false
  }
}

const handleFormSubmit = async (formData: CourseRequest) => {
  try {
    if (selectedCourse.value) {
      await courseService.updateCourse(selectedCourse.value.id, formData)
      toast.success('Course updated successfully')
    } else {
      await courseService.createCourse(formData)
      toast.success('Course created successfully')
    }
    cancelForm()
    loadCourses()
  } catch (error) {
    toast.error('Failed to save course')
    throw error
  }
}

const cancelForm = () => {
  showCreateForm.value = false
  showEditForm.value = false
  selectedCourse.value = null
}

const formSuccess = () => {
  cancelForm()
  loadCourses()
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
onMounted(() => {
  loadCourses()
})
</script>