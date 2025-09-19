<template>
  <div v-if="open" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
    <Card class="w-full max-w-4xl max-h-[90vh] overflow-y-auto">
      <CardHeader>
        <div class="flex items-center justify-between">
          <CardTitle>Assign Teacher to Course</CardTitle>
          <Button variant="ghost" size="icon" @click="$emit('update:open', false)">
            <X class="h-4 w-4" />
          </Button>
        </div>
        <CardDescription>
          {{ course.courseName }} ({{ course.courseCode }}) - Select a qualified teacher
        </CardDescription>
      </CardHeader>
      <CardContent>
        <!-- Course Information -->
        <div class="mb-6 p-4 bg-blue-50 rounded-lg">
          <h4 class="font-semibold text-blue-900 mb-2">Course Requirements</h4>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <span class="font-medium text-blue-700">Department:</span>
              <span class="text-blue-900 ml-1">{{ course.department.name }}</span>
            </div>
            <div>
              <span class="font-medium text-blue-700">Credits:</span>
              <span class="text-blue-900 ml-1">{{ course.credits }}</span>
            </div>
            <div>
              <span class="font-medium text-blue-700">Weekly Hours:</span>
              <span class="text-blue-900 ml-1">{{ course.weeklyHours }}h</span>
            </div>
            <div>
              <span class="font-medium text-blue-700">Max Students:</span>
              <span class="text-blue-900 ml-1">{{ course.maxStudents }}</span>
            </div>
          </div>
          <div v-if="course.requiredSpecializations.length > 0" class="mt-3">
            <span class="font-medium text-blue-700">Required Specializations:</span>
            <div class="flex flex-wrap gap-2 mt-1">
              <Badge v-for="spec in course.requiredSpecializations" :key="spec" variant="outline" class="text-xs">
                {{ spec }}
              </Badge>
            </div>
          </div>
        </div>

        <!-- Teacher Selection -->
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <h4 class="text-lg font-medium">Available Teachers</h4>
            <div class="flex items-center gap-2">
              <Input
                v-model="teacherSearch"
                placeholder="Search teachers..."
                class="w-64"
                @keyup.enter="searchTeachers"
              />
              <Select v-model="sortBy" @change="sortTeachers">
                <option value="match">Best Match</option>
                <option value="workload">Lowest Workload</option>
                <option value="experience">Most Experience</option>
                <option value="name">Name</option>
              </Select>
            </div>
          </div>

          <!-- Teacher Cards -->
          <div class="grid gap-4 max-h-96 overflow-y-auto">
            <div
              v-for="teacher in filteredTeachers"
              :key="teacher.id"
              class="border rounded-lg p-4 hover:shadow-md transition-shadow cursor-pointer"
              :class="{
                'border-primary bg-primary/5': selectedTeacher?.id === teacher.id,
                'border-gray-200': selectedTeacher?.id !== teacher.id
              }"
              @click="selectTeacher(teacher)"
            >
              <div class="flex items-start justify-between">
                <div class="flex items-start gap-3 flex-1">
                  <div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
                    <span class="text-sm font-medium text-primary">
                      {{ getInitials(teacher.user.firstName, teacher.user.lastName) }}
                    </span>
                  </div>
                  <div class="flex-1">
                    <div class="flex items-center gap-2">
                      <h5 class="font-medium">{{ teacher.user.firstName }} {{ teacher.user.lastName }}</h5>
                      <Badge variant="outline" class="text-xs">{{ teacher.title }}</Badge>
                      <Badge
                        v-if="isBestMatch(teacher)"
                        variant="default"
                        class="text-xs bg-green-500 hover:bg-green-600"
                      >
                        Best Match
                      </Badge>
                    </div>
                    <p class="text-sm text-gray-600">{{ teacher.department.name }}</p>
                    <div class="flex items-center gap-4 mt-2 text-sm text-gray-500">
                      <span>{{ teacher.employeeId }}</span>
                      <span>•</span>
                      <span>{{ teacher.user.email }}</span>
                    </div>

                    <!-- Specializations Match -->
                    <div class="mt-3">
                      <div class="flex items-center gap-2 mb-1">
                        <span class="text-xs font-medium text-gray-700">Specializations:</span>
                        <Badge
                          v-if="getSpecializationMatch(teacher) === 100"
                          variant="default"
                          class="text-xs bg-green-500 hover:bg-green-600"
                        >
                          100% Match
                        </Badge>
                        <Badge
                          v-else
                          variant="outline"
                          class="text-xs"
                        >
                          {{ getSpecializationMatch(teacher) }}% Match
                        </Badge>
                      </div>
                      <div class="flex flex-wrap gap-1">
                        <Badge
                          v-for="spec in teacher.specializations"
                          :key="spec.id"
                          variant="secondary"
                          class="text-xs"
                          :class="{
                            'bg-green-100 text-green-800': course.requiredSpecializations.includes(spec.subjectCode),
                            'bg-gray-100 text-gray-800': !course.requiredSpecializations.includes(spec.subjectCode)
                          }"
                        >
                          {{ spec.subjectCode }}
                          <span v-if="spec.certified" class="ml-1">✓</span>
                        </Badge>
                      </div>
                    </div>

                    <!-- Availability -->
                    <div class="mt-3">
                      <div class="flex items-center gap-2 mb-1">
                        <span class="text-xs font-medium text-gray-700">Availability:</span>
                        <Badge
                          :variant="getAvailabilityVariant(teacher)"
                          class="text-xs"
                        >
                          {{ getAvailableHours(teacher) }}h/week
                        </Badge>
                      </div>
                      <div class="text-xs text-gray-500">
                        Current workload: {{ getCurrentWorkload(teacher) }}h/{{ teacher.maxWeeklyHours }}h
                        ({{ getWorkloadPercentage(teacher) }}%)
                      </div>
                      <div class="w-full bg-gray-200 rounded-full h-1.5 mt-1">
                        <div
                          class="bg-blue-600 h-1.5 rounded-full"
                          :style="{ width: Math.min(getWorkloadPercentage(teacher), 100) + '%' }"
                          :class="getWorkloadColorClass(getWorkloadPercentage(teacher))"
                        ></div>
                      </div>
                    </div>

                    <!-- Conflicts Warning -->
                    <div v-if="getConflicts(teacher).length > 0" class="mt-3 p-2 bg-red-50 rounded border border-red-200">
                      <div class="flex items-center gap-1 text-red-700">
                        <AlertTriangle class="h-3 w-3" />
                        <span class="text-xs font-medium">{{ getConflicts(teacher).length }} scheduling conflicts detected</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="flex items-center ml-4">
                  <input
                    type="radio"
                    :name="'teacher-' + course.id"
                    :checked="selectedTeacher?.id === teacher.id"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                  />
                </div>
              </div>
            </div>

            <div v-if="filteredTeachers.length === 0" class="text-center py-8 text-gray-500">
              No qualified teachers found for this course
            </div>
          </div>
        </div>

        <!-- Assignment Summary -->
        <div v-if="selectedTeacher" class="mt-6 p-4 bg-green-50 rounded-lg border border-green-200">
          <h4 class="font-semibold text-green-900 mb-2">Assignment Summary</h4>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="font-medium text-green-700">Selected Teacher:</span>
              <span class="text-green-900 ml-1">{{ selectedTeacher.user.firstName }} {{ selectedTeacher.user.lastName }}</span>
            </div>
            <div>
              <span class="font-medium text-green-700">New Workload:</span>
              <span class="text-green-900 ml-1">{{ getNewWorkload(selectedTeacher) }}h/{{ selectedTeacher.maxWeeklyHours }}h</span>
            </div>
            <div>
              <span class="font-medium text-green-700">Specialization Match:</span>
              <span class="text-green-900 ml-1">{{ getSpecializationMatch(selectedTeacher) }}%</span>
            </div>
            <div>
              <span class="font-medium text-green-700">Conflicts:</span>
              <span class="text-green-900 ml-1">{{ getConflicts(selectedTeacher).length }}</span>
            </div>
          </div>
        </div>

        <!-- Form Actions -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t">
          <Button variant="outline" @click="$emit('cancel')">
            Cancel
          </Button>
          <Button
            @click="handleAssign"
            :disabled="!selectedTeacher || loading"
            :class="{ 'bg-red-600 hover:bg-red-700': getConflicts(selectedTeacher).length > 0 }"
          >
            <AlertTriangle v-if="getConflicts(selectedTeacher).length > 0" class="h-4 w-4 mr-2" />
            <Check v-else class="h-4 w-4 mr-2" />
            {{ getConflicts(selectedTeacher).length > 0 ? 'Assign with Conflicts' : 'Assign Teacher' }}
          </Button>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import Badge from '@/components/ui/Badge.vue'
import { X, Check, AlertTriangle } from 'lucide-vue-next'
import type { Course, Teacher } from '@/services/assignmentService'
import assignmentService from '@/services/assignmentService'

interface Props {
  open: boolean
  course: Course
  teachers: Teacher[]
}

interface Emits {
  'update:open': [value: boolean]
  'save': []
  'cancel': []
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const selectedTeacher = ref<Teacher | null>(null)
const teacherSearch = ref('')
const sortBy = ref('match')

// Computed
const filteredTeachers = computed(() => {
  let filtered = props.teachers

  // Filter by search
  if (teacherSearch.value) {
    const search = teacherSearch.value.toLowerCase()
    filtered = filtered.filter(teacher =>
      teacher.user.firstName.toLowerCase().includes(search) ||
      teacher.user.lastName.toLowerCase().includes(search) ||
      teacher.user.email.toLowerCase().includes(search) ||
      teacher.employeeId.toLowerCase().includes(search)
    )
  }

  // Filter by department match
  filtered = filtered.filter(teacher =>
    teacher.department.id === props.course.department.id
  )

  // Filter by specialization requirements
  if (props.course.requiredSpecializations.length > 0) {
    filtered = filtered.filter(teacher =>
      teacher.specializations.some(spec =>
        props.course.requiredSpecializations.includes(spec.subjectCode)
      )
    )
  }

  // Sort
  switch (sortBy.value) {
    case 'match':
      filtered.sort((a, b) => getSpecializationMatch(b) - getSpecializationMatch(a))
      break
    case 'workload':
      filtered.sort((a, b) => getWorkloadPercentage(a) - getWorkloadPercentage(b))
      break
    case 'experience':
      filtered.sort((a, b) => {
        const avgExpA = a.specializations.reduce((sum, spec) => sum + spec.yearsExperience, 0) / a.specializations.length
        const avgExpB = b.specializations.reduce((sum, spec) => sum + spec.yearsExperience, 0) / b.specializations.length
        return avgExpB - avgExpA
      })
      break
    case 'name':
      filtered.sort((a, b) => a.user.lastName.localeCompare(b.user.lastName))
      break
  }

  return filtered
})

// Methods
const selectTeacher = (teacher: Teacher) => {
  selectedTeacher.value = teacher
}

const getInitials = (firstName: string, lastName: string) => {
  return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase()
}

const getSpecializationMatch = (teacher: Teacher) => {
  if (props.course.requiredSpecializations.length === 0) return 100

  const matchingSpecs = teacher.specializations.filter(spec =>
    props.course.requiredSpecializations.includes(spec.subjectCode)
  ).length

  return Math.round((matchingSpecs / props.course.requiredSpecializations.length) * 100)
}

const isBestMatch = (teacher: Teacher) => {
  return getSpecializationMatch(teacher) === 100 && teacher.specializations.some(spec => spec.certified)
}

const getAvailableHours = (teacher: Teacher) => {
  // Mock calculation - replace with actual availability checking
  return Math.max(0, teacher.maxWeeklyHours - getCurrentWorkload(teacher))
}

const getCurrentWorkload = (teacher: Teacher) => {
  // Mock workload calculation - replace with actual API call
  return Math.floor(Math.random() * teacher.maxWeeklyHours * 0.8)
}

const getWorkloadPercentage = (teacher: Teacher) => {
  const current = getCurrentWorkload(teacher)
  return Math.round((current / teacher.maxWeeklyHours) * 100)
}

const getWorkloadColorClass = (percentage: number) => {
  if (percentage >= 90) return 'bg-red-500'
  if (percentage >= 75) return 'bg-yellow-500'
  return 'bg-green-500'
}

const getNewWorkload = (teacher: Teacher) => {
  return getCurrentWorkload(teacher) + props.course.weeklyHours
}

const getConflicts = (teacher: Teacher) => {
  // Mock conflict detection - replace with actual API call
  const conflicts = []
  if (getNewWorkload(teacher) > teacher.maxWeeklyHours) {
    conflicts.push('Exceeds maximum weekly hours')
  }
  if (Math.random() > 0.7) {
    conflicts.push('Schedule conflict with existing courses')
  }
  return conflicts
}

const getAvailabilityVariant = (teacher: Teacher) => {
  const available = getAvailableHours(teacher)
  if (available >= props.course.weeklyHours * 2) return 'default'
  if (available >= props.course.weeklyHours) return 'secondary'
  return 'destructive'
}

const handleAssign = async () => {
  if (!selectedTeacher.value) return

  loading.value = true
  try {
    await assignmentService.assignTeacherToCourse(
      props.course.id,
      selectedTeacher.value.id
    )

    emit('save')
  } catch (error) {
    console.error('Failed to assign teacher:', error)
  } finally {
    loading.value = false
  }
}

// Reset selection when modal opens
watch(() => props.open, (newOpen) => {
  if (newOpen) {
    selectedTeacher.value = null
    teacherSearch.value = ''
    sortBy.value = 'match'
  }
})
</script>