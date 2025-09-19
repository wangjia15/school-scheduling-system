<template>
  <div class="p-6 space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-bold text-gray-900">Teachers</h1>
        <p class="text-gray-600 mt-2">Manage teacher profiles, availability, and specializations</p>
      </div>
      <Button @click="showCreateModal = true" class="flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Add Teacher
      </Button>
    </div>

    <!-- Filters -->
    <Card>
      <CardHeader>
        <CardTitle>Filters</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Search</label>
            <Input
              v-model="filters.search"
              placeholder="Search by name, ID, or subject..."
              @keyup.enter="loadTeachers"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Department</label>
            <Select v-model="filters.departmentId">
              <option value="">All Departments</option>
              <option v-for="dept in departments" :key="dept.id" :value="dept.id">
                {{ dept.name }}
              </option>
            </Select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Title</label>
            <Select v-model="filters.title">
              <option value="">All Titles</option>
              <option value="PROFESSOR">Professor</option>
              <option value="ASSOCIATE_PROFESSOR">Associate Professor</option>
              <option value="ASSISTANT_PROFESSOR">Assistant Professor</option>
              <option value="INSTRUCTOR">Instructor</option>
              <option value="ADJUNCT">Adjunct</option>
            </Select>
          </div>
          <div class="flex items-end">
            <Button @click="loadTeachers" class="w-full">Apply Filters</Button>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Teachers Table -->
    <Card>
      <CardHeader>
        <CardTitle>Teachers ({{ teachers.totalElements }})</CardTitle>
      </CardHeader>
      <CardContent>
        <div v-if="loading" class="flex justify-center py-8">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>

        <div v-else-if="teachers.content.length === 0" class="text-center py-8">
          <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
          </svg>
          <h3 class="mt-2 text-sm font-medium text-gray-900">No teachers found</h3>
          <p class="mt-1 text-sm text-gray-500">Try adjusting your filters or add a new teacher.</p>
        </div>

        <div v-else>
          <Table>
            <thead>
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Teacher</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Employee ID</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Department</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Title</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Workload</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Specializations</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="teacher in teachers.content" :key="teacher.id">
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <div class="flex-shrink-0 h-10 w-10">
                      <div class="h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center">
                        <span class="text-blue-800 font-medium">
                          {{ teacher.user.firstName.charAt(0) }}{{ teacher.user.lastName.charAt(0) }}
                        </span>
                      </div>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-gray-900">
                        {{ teacher.user.firstName }} {{ teacher.user.lastName }}
                      </div>
                      <div class="text-sm text-gray-500">{{ teacher.user.email }}</div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ teacher.employeeId }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ teacher.department.name }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <Badge :variant="getTitleBadgeVariant(teacher.title)">
                    {{ formatTitle(teacher.title) }}
                  </Badge>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  <div>{{ teacher.maxWeeklyHours }}h/week</div>
                  <div class="text-gray-500">{{ teacher.maxCoursesPerSemester }} courses/semester</div>
                </td>
                <td class="px-6 py-4 text-sm text-gray-900">
                  <div class="flex flex-wrap gap-1">
                    <Badge v-for="spec in teacher.specializations.slice(0, 2)"
                           :key="spec.id"
                           variant="outline"
                           class="text-xs">
                      {{ spec.subjectCode }}
                    </Badge>
                    <Badge v-if="teacher.specializations.length > 2"
                           variant="outline"
                           class="text-xs">
                      +{{ teacher.specializations.length - 2 }}
                    </Badge>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <div class="flex items-center gap-2">
                    <Button variant="outline" size="sm" @click="viewTeacher(teacher)">
                      View
                    </Button>
                    <Button variant="outline" size="sm" @click="editTeacher(teacher)">
                      Edit
                    </Button>
                    <Button variant="outline" size="sm" @click="manageAvailability(teacher)">
                      Schedule
                    </Button>
                  </div>
                </td>
              </tr>
            </tbody>
          </Table>

          <!-- Pagination -->
          <div v-if="teachers.totalPages > 1" class="mt-4 flex justify-between items-center">
            <div class="text-sm text-gray-700">
              Showing {{ teachers.number * teachers.size + 1 }} to
              {{ Math.min((teachers.number + 1) * teachers.size, teachers.totalElements) }}
              of {{ teachers.totalElements }} results
            </div>
            <div class="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                :disabled="teachers.first"
                @click="goToPage(teachers.number - 1)"
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                :disabled="teachers.last"
                @click="goToPage(teachers.number + 1)"
              >
                Next
              </Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Create/Edit Teacher Modal -->
    <TeacherFormModal
      v-if="showCreateModal || showEditModal"
      :teacher="selectedTeacher"
      :departments="departments"
      :users="users"
      @close="closeModals"
      @save="handleTeacherSave"
    />

    <!-- Teacher Detail Modal -->
    <TeacherDetailModal
      v-if="showDetailModal"
      :teacher="selectedTeacher"
      @close="showDetailModal = false"
      @edit="editTeacher"
      @manage-availability="manageAvailability"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Card, { CardHeader, CardTitle, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import Badge from '@/components/ui/Badge.vue'
import Table from '@/components/ui/Table.vue'
import teacherService, { type Teacher, type TeacherFilters, type PaginatedTeachers } from '@/services/teacherService'
import TeacherFormModal from '@/components/teachers/TeacherFormModal.vue'
import TeacherDetailModal from '@/components/teachers/TeacherDetailModal.vue'

const router = useRouter()

// State
const loading = ref(false)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDetailModal = ref(false)
const selectedTeacher = ref<Teacher | null>(null)
const teachers = ref<PaginatedTeachers>({ content: [], totalElements: 0, totalPages: 0, size: 0, number: 0, first: true, last: true })
const departments = ref([])
const users = ref([])

const filters = reactive<TeacherFilters>({
  search: '',
  departmentId: undefined,
  title: '',
  page: 0,
  size: 20,
  sort: 'id',
  direction: 'ASC'
})

// Methods
const loadTeachers = async () => {
  loading.value = true
  try {
    teachers.value = await teacherService.getTeachers(filters)
  } catch (error) {
    console.error('Failed to load teachers:', error)
  } finally {
    loading.value = false
  }
}

const loadDepartments = async () => {
  try {
    // TODO: Implement department service
    departments.value = [
      { id: 1, name: 'Computer Science' },
      { id: 2, name: 'Mathematics' },
      { id: 3, name: 'Physics' }
    ]
  } catch (error) {
    console.error('Failed to load departments:', error)
  }
}

const loadUsers = async () => {
  try {
    // TODO: Implement user service
    users.value = [
      { id: 1, firstName: 'John', lastName: 'Doe', email: 'john.doe@university.edu' },
      { id: 2, firstName: 'Jane', lastName: 'Smith', email: 'jane.smith@university.edu' }
    ]
  } catch (error) {
    console.error('Failed to load users:', error)
  }
}

const viewTeacher = (teacher: Teacher) => {
  selectedTeacher.value = teacher
  showDetailModal.value = true
}

const editTeacher = (teacher: Teacher) => {
  selectedTeacher.value = teacher
  showEditModal.value = true
  showDetailModal.value = false
}

const manageAvailability = (teacher: Teacher) => {
  router.push(`/teachers/${teacher.id}/availability`)
}

const closeModals = () => {
  showCreateModal.value = false
  showEditModal.value = false
  selectedTeacher.value = null
}

const handleTeacherSave = () => {
  closeModals()
  loadTeachers()
}

const goToPage = (page: number) => {
  filters.page = page
  loadTeachers()
}

const getTitleBadgeVariant = (title: string) => {
  switch (title) {
    case 'PROFESSOR':
      return 'default'
    case 'ASSOCIATE_PROFESSOR':
      return 'secondary'
    case 'ASSISTANT_PROFESSOR':
      return 'outline'
    default:
      return 'secondary'
  }
}

const formatTitle = (title: string) => {
  return title.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase())
}

// Lifecycle
onMounted(() => {
  loadTeachers()
  loadDepartments()
  loadUsers()
})
</script>