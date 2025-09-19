<template>
  <div class="container mx-auto px-4 py-8">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-2">Teacher Profiles</h1>
      <p class="text-gray-600">Manage teacher profiles, qualifications, and specializations</p>
    </div>

    <!-- Search and Filters -->
    <div class="mb-6 bg-white rounded-lg shadow-sm border p-6">
      <div class="flex flex-col lg:flex-row gap-4">
        <div class="flex-1">
          <div class="relative">
            <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Search teachers by name, employee ID, or email..."
              class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
        </div>
        <div class="flex gap-3">
          <Select v-model="departmentFilter" class="w-48">
            <option value="">All Departments</option>
            <option v-for="dept in departments" :key="dept.id" :value="dept.id">
              {{ dept.name }}
            </option>
          </Select>
          <Select v-model="titleFilter" class="w-48">
            <option value="">All Titles</option>
            <option value="PROFESSOR">Professor</option>
            <option value="ASSOCIATE_PROFESSOR">Associate Professor</option>
            <option value="ASSISTANT_PROFESSOR">Assistant Professor</option>
            <option value="INSTRUCTOR">Instructor</option>
            <option value="ADJUNCT">Adjunct</option>
          </Select>
          <Button @click="openAddProfileModal">
            <Plus class="h-4 w-4 mr-2" />
            Add Teacher
          </Button>
        </div>
      </div>
    </div>

    <!-- Teacher Profiles Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="teacher in filteredTeachers"
        :key="teacher.id"
        class="bg-white rounded-lg shadow-sm border hover:shadow-md transition-shadow"
      >
        <!-- Teacher Card Header -->
        <div class="p-6 border-b">
          <div class="flex items-start justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                <User class="h-6 w-6 text-blue-600" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-gray-900">
                  {{ teacher.user.firstName }} {{ teacher.user.lastName }}
                </h3>
                <p class="text-sm text-gray-600">{{ teacher.title }}</p>
              </div>
            </div>
            <div class="flex gap-1">
              <Button
                variant="ghost"
                size="icon"
                @click="editTeacher(teacher)"
                class="h-8 w-8"
              >
                <Edit2 class="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="icon"
                @click="deleteTeacher(teacher)"
                class="h-8 w-8 text-red-600 hover:text-red-800"
              >
                <Trash2 class="h-4 w-4" />
              </Button>
            </div>
          </div>

          <!-- Basic Info -->
          <div class="space-y-2 text-sm">
            <div class="flex items-center gap-2">
              <Building2 class="h-4 w-4 text-gray-400" />
              <span class="text-gray-600">{{ teacher.department.name }}</span>
            </div>
            <div class="flex items-center gap-2">
              <IdCard class="h-4 w-4 text-gray-400" />
              <span class="text-gray-600">{{ teacher.employeeId }}</span>
            </div>
            <div class="flex items-center gap-2">
              <Mail class="h-4 w-4 text-gray-400" />
              <span class="text-gray-600">{{ teacher.user.email }}</span>
            </div>
          </div>
        </div>

        <!-- Workload Summary -->
        <div class="p-6 border-b">
          <h4 class="text-sm font-medium text-gray-700 mb-3">Workload Summary</h4>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <div class="text-gray-600">Current Load</div>
              <div class="font-medium">{{ teacher.currentWorkload || 0 }}h / {{ teacher.maxWeeklyHours }}h</div>
            </div>
            <div>
              <div class="text-gray-600">Courses</div>
              <div class="font-medium">{{ teacher.assignedCourses?.length || 0 }}</div>
            </div>
          </div>
          <div class="mt-3">
            <div class="flex justify-between text-xs text-gray-600 mb-1">
              <span>Capacity Used</span>
              <span>{{ Math.round(((teacher.currentWorkload || 0) / teacher.maxWeeklyHours) * 100) }}%</span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-2">
              <div
                class="bg-blue-600 h-2 rounded-full transition-all duration-300"
                :style="{ width: `${Math.min(((teacher.currentWorkload || 0) / teacher.maxWeeklyHours) * 100, 100)}%` }"
              ></div>
            </div>
          </div>
        </div>

        <!-- Specializations -->
        <div class="p-6">
          <div class="flex items-center justify-between mb-3">
            <h4 class="text-sm font-medium text-gray-700">Specializations</h4>
            <Badge variant="outline" class="text-xs">
              {{ teacher.specializations?.length || 0 }}
            </Badge>
          </div>
          <div v-if="teacher.specializations?.length > 0" class="space-y-2">
            <div
              v-for="spec in teacher.specializations.slice(0, 3)"
              :key="spec.id"
              class="flex items-center justify-between text-xs"
            >
              <span class="text-gray-600">{{ spec.subjectCode }}</span>
              <Badge :variant="getProficiencyVariant(spec.proficiencyLevel)" class="text-xs">
                {{ spec.proficiencyLevel }}
              </Badge>
            </div>
            <div v-if="teacher.specializations.length > 3" class="text-xs text-gray-500">
              +{{ teacher.specializations.length - 3 }} more
            </div>
          </div>
          <div v-else class="text-xs text-gray-500">
            No specializations added
          </div>
        </div>

        <!-- Quick Actions -->
        <div class="p-4 bg-gray-50 border-t">
          <div class="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              @click="viewWorkloadDetails(teacher)"
              class="flex-1"
            >
              <BarChart3 class="h-3 w-3 mr-1" />
              Workload
            </Button>
            <Button
              variant="outline"
              size="sm"
              @click="manageAvailability(teacher)"
              class="flex-1"
            >
              <Calendar class="h-3 w-3 mr-1" />
              Availability
            </Button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="filteredTeachers.length === 0" class="text-center py-12">
      <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <Users class="h-8 w-8 text-gray-400" />
      </div>
      <h3 class="text-lg font-medium text-gray-900 mb-2">No teachers found</h3>
      <p class="text-gray-600 mb-4">
        {{ searchQuery || departmentFilter || titleFilter ? 'Try adjusting your filters' : 'Get started by adding your first teacher' }}
      </p>
      <Button v-if="!searchQuery && !departmentFilter && !titleFilter" @click="openAddProfileModal">
        <Plus class="h-4 w-4 mr-2" />
        Add Teacher
      </Button>
    </div>

    <!-- Profile Modal -->
    <TeacherProfileModal
      v-model:open="showProfileModal"
      :teacher="selectedTeacher"
      :departments="departments"
      @save="handleProfileSaved"
    />

    <!-- Workload Detail Modal -->
    <TeacherWorkloadDetailModal
      v-model:open="showWorkloadModal"
      :teacher="selectedTeacher"
      @optimize="openWorkloadOptimization"
    />

    <!-- Availability Modal -->
    <AvailabilityFormModal
      v-model:open="showAvailabilityModal"
      :teacher="selectedTeacher"
      @save="handleAvailabilitySaved"
    />

    <!-- Workload Optimization Modal -->
    <WorkloadOptimizationModal
      v-model:open="showOptimizationModal"
      :teacher="selectedTeacher"
      @optimized="handleOptimizationComplete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import Card from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Select from '@/components/ui/Select.vue'
import TeacherProfileModal from '@/components/teachers/TeacherProfileModal.vue'
import TeacherWorkloadDetailModal from '@/components/analytics/TeacherWorkloadDetailModal.vue'
import AvailabilityFormModal from '@/components/teachers/AvailabilityFormModal.vue'
import WorkloadOptimizationModal from '@/components/analytics/WorkloadOptimizationModal.vue'
import {
  Search, Plus, Edit2, Trash2, User, Building2, IdCard, Mail, BarChart3, Calendar, Users
} from 'lucide-vue-next'
import teacherService, { type Teacher, type TeacherFilters } from '@/services/teacherService'

// State
const teachers = ref<Teacher[]>([])
const departments = ref<Array<{ id: number; name: string }>>([])
const loading = ref(false)
const searchQuery = ref('')
const departmentFilter = ref('')
const titleFilter = ref('')
const showProfileModal = ref(false)
const showWorkloadModal = ref(false)
const showAvailabilityModal = ref(false)
const showOptimizationModal = ref(false)
const selectedTeacher = ref<Teacher | undefined>()

// Computed
const filteredTeachers = computed(() => {
  return teachers.value.filter(teacher => {
    const matchesSearch = !searchQuery.value ||
      `${teacher.user.firstName} ${teacher.user.lastName}`.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      teacher.employeeId.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      teacher.user.email.toLowerCase().includes(searchQuery.value.toLowerCase())

    const matchesDepartment = !departmentFilter.value || teacher.department.id === parseInt(departmentFilter.value)
    const matchesTitle = !titleFilter.value || teacher.title === titleFilter.value

    return matchesSearch && matchesDepartment && matchesTitle
  })
})

// Methods
const loadTeachers = async () => {
  loading.value = true
  try {
    const filters: TeacherFilters = {
      search: searchQuery.value || undefined,
      departmentId: departmentFilter.value ? parseInt(departmentFilter.value) : undefined,
      title: titleFilter.value || undefined,
      size: 100
    }

    const response = await teacherService.getTeachers(filters)
    teachers.value = response.content
  } catch (error) {
    console.error('Failed to load teachers:', error)
  } finally {
    loading.value = false
  }
}

const loadDepartments = async () => {
  try {
    // This would typically come from a department service
    departments.value = [
      { id: 1, name: 'Computer Science' },
      { id: 2, name: 'Mathematics' },
      { id: 3, name: 'Physics' },
      { id: 4, name: 'Chemistry' },
      { id: 5, name: 'Biology' },
      { id: 6, name: 'English' },
      { id: 7, name: 'History' },
      { id: 8, name: 'Psychology' }
    ]
  } catch (error) {
    console.error('Failed to load departments:', error)
  }
}

const getProficiencyVariant = (level: string) => {
  switch (level) {
    case 'EXPERT': return 'default'
    case 'ADVANCED': return 'outline'
    case 'INTERMEDIATE': return 'secondary'
    case 'BEGINNER': return 'destructive'
    default: return 'outline'
  }
}

const openAddProfileModal = () => {
  selectedTeacher.value = undefined
  showProfileModal.value = true
}

const editTeacher = (teacher: Teacher) => {
  selectedTeacher.value = teacher
  showProfileModal.value = true
}

const deleteTeacher = async (teacher: Teacher) => {
  if (confirm(`Are you sure you want to delete ${teacher.user.firstName} ${teacher.user.lastName}?`)) {
    try {
      await teacherService.deleteTeacher(teacher.id)
      await loadTeachers()
    } catch (error) {
      console.error('Failed to delete teacher:', error)
    }
  }
}

const viewWorkloadDetails = (teacher: Teacher) => {
  selectedTeacher.value = teacher
  showWorkloadModal.value = true
}

const manageAvailability = (teacher: Teacher) => {
  selectedTeacher.value = teacher
  showAvailabilityModal.value = true
}

const openWorkloadOptimization = (teacherId: number) => {
  showWorkloadModal.value = false
  showOptimizationModal.value = true
}

const handleProfileSaved = () => {
  showProfileModal.value = false
  loadTeachers()
}

const handleAvailabilitySaved = () => {
  showAvailabilityModal.value = false
  loadTeachers()
}

const handleOptimizationComplete = () => {
  showOptimizationModal.value = false
  loadTeachers()
}

// Lifecycle
onMounted(() => {
  loadDepartments()
  loadTeachers()
})
</script>