<template>
  <div class="space-y-6">
    <!-- Scheduling Overview -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
              <Calendar class="w-4 h-4 text-blue-600" />
            </div>
            <div>
              <div class="text-2xl font-bold">{{ scheduledSections }}</div>
              <div class="text-xs text-muted-foreground">Scheduled Sections</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
              <Clock class="w-4 h-4 text-green-600" />
            </div>
            <div>
              <div class="text-2xl font-bold">{{ totalHours }}</div>
              <div class="text-xs text-muted-foreground">Total Hours</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-purple-100 rounded-full flex items-center justify-center">
              <Users class="w-4 h-4 text-purple-600" />
            </div>
            <div>
              <div class="text-2xl font-bold">{{ totalCapacity }}</div>
              <div class="text-xs text-muted-foreground">Total Capacity</div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-4">
          <div class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
              <AlertTriangle class="w-4 h-4 text-yellow-600" />
            </div>
            <div>
              <div class="text-2xl font-bold">{{ conflicts }}</div>
              <div class="text-xs text-muted-foreground">Conflicts</div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Tabs for different scheduling views -->
    <div class="border-b">
      <nav class="flex space-x-8">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="activeTab = tab.id"
          :class="[
            'py-2 px-1 border-b-2 font-medium text-sm',
            activeTab === tab.id
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground hover:border-muted-foreground'
          ]"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <!-- Tab Content -->
    <div v-if="activeTab === 'schedule'">
      <CourseScheduleBuilder
        :course="course"
        :sections="sections"
        :teachers="teachers"
        :classrooms="classrooms"
        @section-created="handleSectionCreated"
        @section-updated="handleSectionUpdated"
        @section-deleted="handleSectionDeleted"
      />
    </div>

    <div v-else-if="activeTab === 'calendar'">
      <CourseCalendarView
        :course="course"
        :sections="sections"
        @section-selected="handleSectionSelected"
      />
    </div>

    <div v-else-if="activeTab === 'conflicts'">
      <ConflictResolution
        :course="course"
        :conflicts="conflictsList"
        @conflict-resolved="handleConflictResolved"
      />
    </div>

    <div v-else-if="activeTab === 'resources'">
      <ResourceAllocation
        :course="course"
        :sections="sections"
        :teachers="teachers"
        :classrooms="classrooms"
        @resource-allocated="handleResourceAllocated"
      />
    </div>

    <!-- Create Section Modal -->
    <div v-if="showCreateModal" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <Card class="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <CardHeader>
          <CardTitle>Create Course Section</CardTitle>
          <CardDescription>
            Add a new section for {{ course.courseCode }} - {{ course.title }}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <SectionForm
            :course="course"
            :teachers="teachers"
            :classrooms="classrooms"
            @submit="handleSectionSubmit"
            @cancel="showCreateModal = false"
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
import CourseScheduleBuilder from './CourseScheduleBuilder.vue'
import CourseCalendarView from './CourseCalendarView.vue'
import ConflictResolution from './ConflictResolution.vue'
import ResourceAllocation from './ResourceAllocation.vue'
import SectionForm from './SectionForm.vue'
import { useToast } from 'vue-toastification'
import { Calendar, Clock, Users, AlertTriangle } from 'lucide-vue-next'

interface Props {
  course: any
}

interface Emits {
  (e: 'schedule-updated', course: any): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const toast = useToast()

// State
const activeTab = ref('schedule')
const showCreateModal = ref(false)
const sections = ref([])
const teachers = ref([])
const classrooms = ref([])
const conflictsList = ref([])

// Tabs
const tabs = [
  { id: 'schedule', label: 'Schedule Builder' },
  { id: 'calendar', label: 'Calendar View' },
  { id: 'conflicts', label: 'Conflicts' },
  { id: 'resources', label: 'Resources' }
]

// Computed
const scheduledSections = computed(() => sections.value.length)
const totalHours = computed(() => {
  return sections.value.reduce((total, section) => total + (section.weeklyHours || 0), 0)
})
const totalCapacity = computed(() => {
  return sections.value.reduce((total, section) => total + (section.capacity || 0), 0)
})
const conflicts = computed(() => conflictsList.value.length)

// Methods
const loadData = async () => {
  try {
    // Mock data - in real implementation, this would call APIs
    sections.value = []
    teachers.value = []
    classrooms.value = []
    conflictsList.value = []
  } catch (error) {
    toast.error('Failed to load scheduling data')
  }
}

const handleSectionCreated = (section: any) => {
  sections.value.push(section)
  toast.success('Section created successfully')
  emit('schedule-updated', props.course)
}

const handleSectionUpdated = (section: any) => {
  const index = sections.value.findIndex(s => s.id === section.id)
  if (index !== -1) {
    sections.value[index] = section
    toast.success('Section updated successfully')
    emit('schedule-updated', props.course)
  }
}

const handleSectionDeleted = (sectionId: number) => {
  const index = sections.value.findIndex(s => s.id === sectionId)
  if (index !== -1) {
    sections.value.splice(index, 1)
    toast.success('Section deleted successfully')
    emit('schedule-updated', props.course)
  }
}

const handleSectionSubmit = async (sectionData: any) => {
  try {
    // Mock API call
    const newSection = {
      id: Date.now(),
      courseId: props.course.id,
      ...sectionData,
      createdAt: new Date().toISOString()
    }

    handleSectionCreated(newSection)
    showCreateModal.value = false
  } catch (error) {
    toast.error('Failed to create section')
  }
}

const handleSectionSelected = (section: any) => {
  console.log('Section selected:', section)
}

const handleConflictResolved = (conflictId: number) => {
  const index = conflictsList.value.findIndex(c => c.id === conflictId)
  if (index !== -1) {
    conflictsList.value.splice(index, 1)
    toast.success('Conflict resolved')
  }
}

const handleResourceAllocated = (allocation: any) => {
  console.log('Resource allocated:', allocation)
  toast.success('Resource allocated successfully')
}

// Lifecycle
onMounted(() => {
  loadData()
})
</script>