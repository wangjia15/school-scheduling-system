<template>
  <AdminLayout>
    <div class="space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">Classroom Management</h1>
          <p class="text-muted-foreground">
            Manage classroom inventory, capacity, and equipment
          </p>
        </div>
        <div class="flex items-center gap-2">
          <Button variant="outline" @click="refreshData">
            <RefreshCw class="h-4 w-4 mr-2" />
            Refresh
          </Button>
          <Button @click="showAddClassroomModal = true">
            <Plus class="h-4 w-4 mr-2" />
            Add Classroom
          </Button>
        </div>
      </div>

      <!-- Stats Cards -->
      <div class="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Total Classrooms</CardTitle>
            <DoorOpen class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ stats.totalClassrooms }}</div>
            <p class="text-xs text-muted-foreground">
              {{ stats.activeClassrooms }} active
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Total Capacity</CardTitle>
            <Users class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ stats.totalCapacity }}</div>
            <p class="text-xs text-muted-foreground">
              Student seats
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Buildings</CardTitle>
            <Building class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ stats.totalBuildings }}</div>
            <p class="text-xs text-muted-foreground">
              Campus buildings
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">Utilization</CardTitle>
            <BarChart3 class="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ stats.utilizationRate }}%</div>
            <p class="text-xs text-muted-foreground">
              Average utilization
            </p>
          </CardContent>
        </Card>
      </div>

      <!-- Filters and Search -->
      <Card>
        <CardHeader>
          <CardTitle>Search & Filter</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex flex-col md:flex-row gap-4">
            <div class="relative flex-1">
              <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                v-model="searchQuery"
                placeholder="Search classrooms by room number, building..."
                class="pl-10"
                @keyup.enter="searchClassrooms"
              />
            </div>
            <Select v-model="selectedBuilding" @change="searchClassrooms">
              <option value="">All Buildings</option>
              <option v-for="building in buildings" :key="building" :value="building">
                {{ building }}
              </option>
            </Select>
            <Select v-model="selectedRoomType" @change="searchClassrooms">
              <option value="">All Types</option>
              <option value="CLASSROOM">Classroom</option>
              <option value="LABORATORY">Laboratory</option>
              <option value="LECTURE_HALL">Lecture Hall</option>
              <option value="SEMINAR_ROOM">Seminar Room</option>
              <option value="COMPUTER_LAB">Computer Lab</option>
            </Select>
            <Button variant="outline" @click="resetFilters">
              <RotateCcw class="h-4 w-4 mr-2" />
              Reset
            </Button>
          </div>
        </CardContent>
      </Card>

      <!-- Classrooms Table -->
      <Card>
        <CardHeader>
          <div class="flex items-center justify-between">
            <div>
              <CardTitle>Classroom Directory</CardTitle>
              <CardDescription>
                {{ pagination.totalElements }} classrooms found
              </CardDescription>
            </div>
            <div class="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                @click="exportClassrooms"
              >
                <Download class="h-4 w-4 mr-2" />
                Export
              </Button>
              <Button
                variant="outline"
                size="sm"
                @click="showAvailabilityCalendar = true"
              >
                <Calendar class="h-4 w-4 mr-2" />
                Availability
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div class="rounded-md border">
            <Table :headers="tableHeaders" :data="classrooms" striped hoverable>
              <template #cell-roomNumber="{ row }">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center">
                    <DoorOpen class="h-4 w-4 text-primary" />
                  </div>
                  <div>
                    <div class="font-medium">{{ row.roomNumber }}</div>
                    <div class="text-sm text-muted-foreground">{{ row.building }}</div>
                  </div>
                </div>
              </template>

              <template #cell-capacity="{ row }">
                <div class="flex items-center gap-2">
                  <Users class="h-4 w-4 text-muted-foreground" />
                  <span class="font-medium">{{ row.capacity }}</span>
                </div>
              </template>

              <template #cell-roomType="{ row }">
                <Badge :variant="getRoomTypeVariant(row.roomType)">
                  {{ formatRoomType(row.roomType) }}
                </Badge>
              </template>

              <template #cell-equipment="{ row }">
                <div class="flex flex-wrap gap-1">
                  <Badge
                    v-for="equipment in row.equipment.slice(0, 3)"
                    :key="equipment"
                    variant="secondary"
                    class="text-xs"
                  >
                    {{ equipment }}
                  </Badge>
                  <Badge
                    v-if="row.equipment.length > 3"
                    variant="outline"
                    class="text-xs"
                  >
                    +{{ row.equipment.length - 3 }}
                  </Badge>
                </div>
              </template>

              <template #cell-utilization="{ row }">
                <div class="space-y-1">
                  <div class="flex justify-between text-sm">
                    <span>{{ getUtilizationHours(row) }}h / 40h</span>
                    <span>{{ getUtilizationRate(row) }}%</span>
                  </div>
                  <div class="w-full bg-secondary rounded-full h-2">
                    <div
                      class="bg-primary h-2 rounded-full transition-all"
                      :style="{ width: Math.min(getUtilizationRate(row), 100) + '%' }"
                      :class="getUtilizationColorClass(getUtilizationRate(row))"
                    ></div>
                  </div>
                </div>
              </template>

              <template #cell-status="{ row }">
                <Badge :variant="row.isActive ? 'default' : 'secondary'">
                  {{ row.isActive ? 'Active' : 'Inactive' }}
                </Badge>
              </template>

              <template #cell-actions="{ row }">
                <div class="flex items-center gap-2">
                  <Button variant="ghost" size="sm" @click="viewClassroom(row)">
                    <Eye class="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="sm" @click="editClassroom(row)">
                    <Pencil class="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="sm" @click="viewSchedule(row)">
                    <Calendar class="h-4 w-4" />
                  </Button>
                  <DropdownMenu>
                    <DropdownMenuTrigger as-child>
                      <Button variant="ghost" size="sm">
                        <MoreHorizontal class="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem @click="viewClassroomDetails(row)">
                        <Info class="h-4 w-4 mr-2" />
                        View Details
                      </DropdownMenuItem>
                      <DropdownMenuItem @click="editClassroom(row)">
                        <Pencil class="h-4 w-4 mr-2" />
                        Edit Classroom
                      </DropdownMenuItem>
                      <DropdownMenuItem @click="viewSchedule(row)">
                        <Calendar class="h-4 w-4 mr-2" />
                        View Schedule
                      </DropdownMenuItem>
                      <DropdownMenuItem @click="manageMaintenance(row)">
                        <Wrench class="h-4 w-4 mr-2" />
                        Manage Maintenance
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem
                        @click="toggleClassroomStatus(row)"
                        :class="row.isActive ? 'text-destructive' : ''"
                      >
                        <Power class="h-4 w-4 mr-2" />
                        {{ row.isActive ? 'Deactivate' : 'Activate' }}
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              </template>
            </Table>
          </div>

          <!-- Pagination -->
          <div class="flex items-center justify-between mt-4">
            <div class="text-sm text-muted-foreground">
              Showing {{ (pagination.page * pagination.size) + 1 }} to
              {{ Math.min((pagination.page + 1) * pagination.size, pagination.totalElements) }}
              of {{ pagination.totalElements }} classrooms
            </div>
            <div class="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                :disabled="pagination.page === 0"
                @click="changePage(pagination.page - 1)"
              >
                Previous
              </Button>
              <div class="flex items-center gap-1">
                <Button
                  v-for="pageNum in visiblePageNumbers"
                  :key="pageNum"
                  variant="outline"
                  size="sm"
                  :class="{ 'bg-primary text-primary-foreground': pageNum === pagination.page }"
                  @click="changePage(pageNum)"
                >
                  {{ pageNum + 1 }}
                </Button>
              </div>
              <Button
                variant="outline"
                size="sm"
                :disabled="pagination.page === pagination.totalPages - 1"
                @click="changePage(pagination.page + 1)"
              >
                Next
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Add/Edit Classroom Modal -->
    <ClassroomFormModal
      v-model:open="showAddClassroomModal"
      :classroom="selectedClassroom"
      @save="handleClassroomSave"
      @cancel="showAddClassroomModal = false"
    />

    <!-- Classroom Detail Modal -->
    <ClassroomDetailModal
      v-model:open="showClassroomDetailModal"
      :classroom="selectedClassroom"
      @edit="editClassroom"
      @close="showClassroomDetailModal = false"
    />
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import Badge from '@/components/ui/Badge.vue'
import Table from '@/components/ui/Table.vue'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import {
  DoorOpen,
  Plus,
  Filter,
  Search,
  RefreshCw,
  Eye,
  Pencil,
  Calendar,
  MoreHorizontal,
  RotateCcw,
  Building,
  Users,
  BarChart3,
  Download,
  Power,
  Wrench,
  Info
} from 'lucide-vue-next'
import classroomService, { type Classroom, type ClassroomFilters } from '@/services/classroomService'

const router = useRouter()
const toast = useToast()

// State
const classrooms = ref<Classroom[]>([])
const buildings = ref<string[]>([])
const loading = ref(false)
const searchQuery = ref('')
const selectedBuilding = ref('')
const selectedRoomType = ref('')
const selectedClassroom = ref<Classroom | null>(null)
const showAddClassroomModal = ref(false)
const showClassroomDetailModal = ref(false)
const showAvailabilityCalendar = ref(false)

// Pagination
const pagination = ref({
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0
})

// Stats
const stats = ref({
  totalClassrooms: 0,
  activeClassrooms: 0,
  totalCapacity: 0,
  totalBuildings: 0,
  utilizationRate: 0
})

// Table headers
const tableHeaders = [
  { key: 'roomNumber', label: 'Classroom', width: '200px' },
  { key: 'capacity', label: 'Capacity', width: '100px' },
  { key: 'roomType', label: 'Type', width: '150px' },
  { key: 'equipment', label: 'Equipment', width: '200px' },
  { key: 'utilization', label: 'Utilization', width: '150px' },
  { key: 'status', label: 'Status', width: '100px' },
  { key: 'actions', label: 'Actions', width: '120px' }
]

// Computed
const visiblePageNumbers = computed(() => {
  const current = pagination.value.page
  const total = pagination.value.totalPages
  const delta = 2

  let start = Math.max(0, current - delta)
  let end = Math.min(total - 1, current + delta)

  if (end - start < 2 * delta) {
    start = Math.max(0, end - 2 * delta)
    end = Math.min(total - 1, start + 2 * delta)
  }

  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

// Methods
const loadClassrooms = async () => {
  loading.value = true
  try {
    const filters: ClassroomFilters = {
      page: pagination.value.page,
      size: pagination.value.size,
      search: searchQuery.value || undefined,
      building: selectedBuilding.value || undefined,
      roomType: selectedRoomType.value || undefined
    }

    const response = await classroomService.getClassrooms(filters)
    classrooms.value = response.content
    pagination.value = {
      page: response.number,
      size: response.size,
      totalElements: response.totalElements,
      totalPages: response.totalPages
    }
  } catch (error) {
    toast.error('Failed to load classrooms')
    console.error('Error loading classrooms:', error)
  } finally {
    loading.value = false
  }
}

const loadBuildings = async () => {
  try {
    // Mock buildings for now - replace with API call
    buildings.value = ['Science Building', 'Engineering Building', 'Library', 'Student Center', 'Administration']
  } catch (error) {
    console.error('Error loading buildings:', error)
  }
}

const loadStats = async () => {
  try {
    // Mock stats for now - replace with API calls
    stats.value = {
      totalClassrooms: 45,
      activeClassrooms: 42,
      totalCapacity: 1850,
      totalBuildings: 5,
      utilizationRate: 78
    }
  } catch (error) {
    console.error('Error loading stats:', error)
  }
}

const searchClassrooms = () => {
  pagination.value.page = 0
  loadClassrooms()
}

const resetFilters = () => {
  searchQuery.value = ''
  selectedBuilding.value = ''
  selectedRoomType.value = ''
  pagination.value.page = 0
  loadClassrooms()
}

const changePage = (page: number) => {
  pagination.value.page = page
  loadClassrooms()
}

const refreshData = () => {
  loadClassrooms()
  loadStats()
  toast.info('Data refreshed')
}

const addClassroom = () => {
  selectedClassroom.value = null
  showAddClassroomModal.value = true
}

const editClassroom = (classroom: Classroom) => {
  selectedClassroom.value = classroom
  showAddClassroomModal.value = true
}

const viewClassroom = (classroom: Classroom) => {
  selectedClassroom.value = classroom
  showClassroomDetailModal.value = true
}

const viewSchedule = (classroom: Classroom) => {
  // Navigate to classroom schedule view
  console.log('View schedule for classroom:', classroom.id)
}

const viewClassroomDetails = (classroom: Classroom) => {
  selectedClassroom.value = classroom
  showClassroomDetailModal.value = true
}

const manageMaintenance = (classroom: Classroom) => {
  // Navigate to maintenance management
  console.log('Manage maintenance for classroom:', classroom.id)
}

const toggleClassroomStatus = async (classroom: Classroom) => {
  try {
    // Mock status toggle - replace with API call
    classroom.isActive = !classroom.isActive
    toast.success(`Classroom ${classroom.isActive ? 'activated' : 'deactivated'} successfully`)
    await loadClassrooms()
  } catch (error) {
    toast.error('Failed to update classroom status')
  }
}

const exportClassrooms = () => {
  // Export classrooms data
  toast.info('Classroom export started')
}

const handleClassroomSave = async (classroomData: any) => {
  try {
    if (selectedClassroom.value) {
      // Update existing classroom
      await classroomService.updateClassroom(selectedClassroom.value.id, classroomData)
      toast.success('Classroom updated successfully')
    } else {
      // Create new classroom
      await classroomService.createClassroom(classroomData)
      toast.success('Classroom created successfully')
    }

    showAddClassroomModal.value = false
    await loadClassrooms()
    await loadStats()
  } catch (error) {
    toast.error('Failed to save classroom')
    console.error('Error saving classroom:', error)
  }
}

// Utility functions
const formatRoomType = (roomType: string) => {
  return roomType.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}

const getRoomTypeVariant = (roomType: string) => {
  switch (roomType) {
    case 'LECTURE_HALL':
      return 'default'
    case 'LABORATORY':
    case 'COMPUTER_LAB':
      return 'secondary'
    case 'CLASSROOM':
      return 'outline'
    default:
      return 'outline'
  }
}

const getUtilizationHours = (classroom: Classroom) => {
  // Mock utilization calculation - replace with actual API call
  return Math.floor(Math.random() * 40)
}

const getUtilizationRate = (classroom: Classroom) => {
  const hours = getUtilizationHours(classroom)
  return Math.round((hours / 40) * 100)
}

const getUtilizationColorClass = (percentage: number) => {
  if (percentage >= 90) return 'bg-red-500'
  if (percentage >= 75) return 'bg-yellow-500'
  return 'bg-green-500'
}

// Initialize
onMounted(async () => {
  await Promise.all([
    loadClassrooms(),
    loadBuildings(),
    loadStats()
  ])
})
</script>