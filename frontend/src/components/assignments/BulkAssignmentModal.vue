<template>
  <div v-if="open" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
    <Card class="w-full max-w-5xl max-h-[90vh] overflow-y-auto">
      <CardHeader>
        <div class="flex items-center justify-between">
          <CardTitle>Bulk Teacher Assignment</CardTitle>
          <Button variant="ghost" size="icon" @click="$emit('update:open', false)">
            <X class="h-4 w-4" />
          </Button>
        </div>
        <CardDescription>
          Assign multiple teachers to courses with automatic conflict detection
        </CardDescription>
      </CardHeader>
      <CardContent>
        <!-- Course Selection -->
        <div class="mb-6">
          <h4 class="text-lg font-medium mb-4">Select Courses to Assign</h4>
          <div class="max-h-64 overflow-y-auto border rounded-lg">
            <Table :headers="courseHeaders" :data="courses" striped>
              <template #cell-select="{ row }">
                <input
                  type="checkbox"
                  :checked="selectedCourses.has(row.id)"
                  @change="toggleCourseSelection(row)"
                  class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
              </template>
              <template #cell-courseInfo="{ row }">
                <div>
                  <div class="font-medium">{{ row.courseCode }}</div>
                  <div class="text-sm text-gray-600">{{ row.courseName }}</div>
                </div>
              </template>
              <template #cell-department="{ row }">
                <div>
                  <div class="font-medium">{{ row.department.name }}</div>
                  <div class="text-sm text-gray-600">{{ row.credits }} credits</div>
                </div>
              </template>
              <template #cell-requirements="{ row }">
                <div class="space-y-1">
                  <div class="text-sm">{{ row.weeklyHours }}h/week</div>
                  <div v-if="row.requiredSpecializations.length > 0" class="flex flex-wrap gap-1">
                    <Badge
                      v-for="spec in row.requiredSpecializations.slice(0, 2)"
                      :key="spec"
                      variant="outline"
                      class="text-xs"
                    >
                      {{ spec }}
                    </Badge>
                    <Badge
                      v-if="row.requiredSpecializations.length > 2"
                      variant="outline"
                      class="text-xs"
                    >
                      +{{ row.requiredSpecializations.length - 2 }}
                    </Badge>
                  </div>
                </div>
              </template>
            </Table>
          </div>
          <div class="mt-2 text-sm text-gray-600">
            {{ selectedCourses.size }} courses selected
          </div>
        </div>

        <!-- Assignment Strategy -->
        <div class="mb-6 p-4 bg-blue-50 rounded-lg">
          <h4 class="font-semibold text-blue-900 mb-3">Assignment Strategy</h4>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-blue-700 mb-2">Assignment Method</label>
              <Select v-model="assignmentStrategy">
                <option value="optimal">Optimal Matching (AI-powered)</option>
                <option value="workload">Balance Workload</option>
                <option value="experience">Prioritize Experience</option>
                <option value="manual">Manual Selection</option>
              </Select>
            </div>
            <div>
              <label class="block text-sm font-medium text-blue-700 mb-2">Conflict Handling</label>
              <Select v-model="conflictHandling">
                <option value="strict">Strict - No Conflicts Allowed</option>
                <option value="warning">Warning - Allow with Review</option>
                <option value="auto">Auto-Resolve When Possible</option>
              </Select>
            </div>
          </div>
          <div class="mt-4">
            <label class="flex items-center">
              <input
                v-model="autoAssign"
                type="checkbox"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <span class="ml-2 text-sm text-blue-900">Automatically assign best matches</span>
            </label>
          </div>
        </div>

        <!-- Manual Assignment (if strategy is manual) -->
        <div v-if="assignmentStrategy === 'manual'" class="mb-6">
          <h4 class="text-lg font-medium mb-4">Manual Assignments</h4>
          <div class="space-y-4 max-h-64 overflow-y-auto">
            <div
              v-for="courseId in Array.from(selectedCourses)"
              :key="courseId"
              class="border rounded-lg p-4"
            >
              <div class="flex items-center justify-between mb-3">
                <h5 class="font-medium">{{ getCourseById(courseId)?.courseCode }} - {{ getCourseById(courseId)?.courseName }}</h5>
                <Select v-model="manualAssignments[courseId]" @change="validateManualAssignment(courseId)">
                  <option value="">Select Teacher</option>
                  <option
                    v-for="teacher in getQualifiedTeachersForCourse(courseId)"
                    :key="teacher.id"
                    :value="teacher.id"
                  >
                    {{ teacher.user.firstName }} {{ teacher.user.lastName }} - {{ teacher.title }}
                  </option>
                </Select>
              </div>
              <div v-if="manualAssignments[courseId]" class="text-sm text-gray-600">
                <div class="flex items-center gap-2">
                  <span>Selected: {{ getTeacherById(manualAssignments[courseId])?.user.firstName }} {{ getTeacherById(manualAssignments[courseId])?.user.lastName }}</span>
                  <Badge
                    :variant="getManualAssignmentVariant(courseId)"
                    class="text-xs"
                  >
                    {{ getManualAssignmentStatus(courseId) }}
                  </Badge>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Assignment Preview -->
        <div v-if="showPreview" class="mb-6">
          <h4 class="text-lg font-medium mb-4">Assignment Preview</h4>
          <div class="space-y-3">
            <div
              v-for="assignment in previewAssignments"
              :key="assignment.courseId"
              class="border rounded-lg p-3"
              :class="{
                'border-green-200 bg-green-50': assignment.status === 'valid',
                'border-yellow-200 bg-yellow-50': assignment.status === 'warning',
                'border-red-200 bg-red-50': assignment.status === 'conflict'
              }"
            >
              <div class="flex items-center justify-between">
                <div>
                  <div class="font-medium">{{ assignment.courseCode }} - {{ assignment.courseName }}</div>
                  <div class="text-sm text-gray-600">
                    Teacher: {{ assignment.teacherName }} | Workload: {{ assignment.newWorkload }}h/{{ assignment.maxWorkload }}h
                  </div>
                </div>
                <div class="flex items-center gap-2">
                  <Badge :variant="getPreviewVariant(assignment.status)">
                    {{ assignment.status.toUpperCase() }}
                  </Badge>
                  <div v-if="assignment.conflicts.length > 0" class="text-xs text-red-600">
                    {{ assignment.conflicts.length }} conflicts
                  </div>
                </div>
              </div>
              <div v-if="assignment.conflicts.length > 0" class="mt-2 text-xs text-red-700">
                <div v-for="conflict in assignment.conflicts" :key="conflict" class="flex items-center gap-1">
                  <AlertTriangle class="h-3 w-3" />
                  {{ conflict }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Form Actions -->
        <div class="flex items-center justify-between pt-4 border-t">
          <div class="text-sm text-gray-600">
            {{ selectedCourses.size }} courses selected
          </div>
          <div class="flex items-center gap-3">
            <Button variant="outline" @click="$emit('cancel')">
              Cancel
            </Button>
            <Button
              v-if="!showPreview"
              @click="generatePreview"
              :disabled="selectedCourses.size === 0"
            >
              <Eye class="h-4 w-4 mr-2" />
              Preview Assignments
            </Button>
            <Button
              v-else
              @click="executeBulkAssignment"
              :disabled="loading || previewAssignments.some(a => a.status === 'conflict' && conflictHandling === 'strict')"
              :class="{ 'bg-yellow-600 hover:bg-yellow-700': previewAssignments.some(a => a.status === 'warning') }"
            >
              <Users class="h-4 w-4 mr-2" />
              {{ loading ? 'Assigning...' : 'Execute Assignments' }}
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Select from '@/components/ui/Select.vue'
import Badge from '@/components/ui/Badge.vue'
import Table from '@/components/ui/Table.vue'
import { X, Eye, Users, AlertTriangle } from 'lucide-vue-next'
import type { Course, Teacher } from '@/services/assignmentService'
import assignmentService from '@/services/assignmentService'

interface Props {
  open: boolean
  courses: Course[]
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
const selectedCourses = ref(new Set<number>())
const assignmentStrategy = ref('optimal')
const conflictHandling = ref('warning')
const autoAssign = ref(true)
const showPreview = ref(false)
const manualAssignments = reactive<Record<number, number>>({})
const previewAssignments = ref<any[]>([])

const courseHeaders = [
  { key: 'select', label: '', width: '50px' },
  { key: 'courseInfo', label: 'Course', width: '200px' },
  { key: 'department', label: 'Department', width: '150px' },
  { key: 'requirements', label: 'Requirements', width: '200px' }
]

// Computed
const selectedCoursesList = computed(() => {
  return props.courses.filter(course => selectedCourses.value.has(course.id))
})

// Methods
const toggleCourseSelection = (course: Course) => {
  if (selectedCourses.value.has(course.id)) {
    selectedCourses.value.delete(course.id)
    delete manualAssignments[course.id]
  } else {
    selectedCourses.value.add(course.id)
  }
  showPreview.value = false
}

const getCourseById = (courseId: number) => {
  return props.courses.find(course => course.id === courseId)
}

const getTeacherById = (teacherId: number) => {
  return props.teachers.find(teacher => teacher.id === teacherId)
}

const getQualifiedTeachersForCourse = (courseId: number) => {
  const course = getCourseById(courseId)
  if (!course) return []

  return props.teachers.filter(teacher => {
    // Same department
    if (teacher.department.id !== course.department.id) return false

    // Required specializations
    if (course.requiredSpecializations.length > 0) {
      const hasRequiredSpecialization = teacher.specializations.some(spec =>
        course.requiredSpecializations.includes(spec.subjectCode)
      )
      if (!hasRequiredSpecialization) return false
    }

    // Available workload
    const currentWorkload = teacher.currentWorkload || 0
    return currentWorkload + course.weeklyHours <= teacher.maxWeeklyHours
  })
}

const validateManualAssignment = (courseId: number) => {
  showPreview.value = false
}

const getManualAssignmentVariant = (courseId: number) => {
  const teacherId = manualAssignments[courseId]
  if (!teacherId) return 'secondary'

  const teacher = getTeacherById(teacherId)
  const course = getCourseById(courseId)
  if (!teacher || !course) return 'secondary'

  const newWorkload = (teacher.currentWorkload || 0) + course.weeklyHours
  if (newWorkload > teacher.maxWeeklyHours) return 'destructive'
  if (newWorkload > teacher.maxWeeklyHours * 0.9) return 'outline'
  return 'default'
}

const getManualAssignmentStatus = (courseId: number) => {
  const teacherId = manualAssignments[courseId]
  if (!teacherId) return 'Not Assigned'

  const teacher = getTeacherById(teacherId)
  const course = getCourseById(courseId)
  if (!teacher || !course) return 'Unknown'

  const newWorkload = (teacher.currentWorkload || 0) + course.weeklyHours
  if (newWorkload > teacher.maxWeeklyHours) return 'Overloaded'
  if (newWorkload > teacher.maxWeeklyHours * 0.9) return 'High Load'
  return 'Good Fit'
}

const generatePreview = async () => {
  try {
    const courseIds = Array.from(selectedCourses.value)
    const assignments = []

    for (const courseId of courseIds) {
      const course = getCourseById(courseId)
      if (!course) continue

      let teacherId: number
      if (assignmentStrategy.value === 'manual') {
        teacherId = manualAssignments[courseId]
        if (!teacherId) continue
      } else {
        // For optimal/auto assignment, find the best teacher
        const qualifiedTeachers = getQualifiedTeachersForCourse(courseId)
        if (qualifiedTeachers.length === 0) {
          assignments.push({
            courseId,
            courseCode: course.courseCode,
            courseName: course.courseName,
            status: 'conflict',
            teacherName: 'None',
            newWorkload: 0,
            maxWorkload: 0,
            conflicts: ['No qualified teachers available']
          })
          continue
        }

        // Select best teacher based on strategy
        let selectedTeacher = qualifiedTeachers[0]
        if (assignmentStrategy.value === 'workload') {
          selectedTeacher = qualifiedTeachers.reduce((best, current) =>
            (current.currentWorkload || 0) < (best.currentWorkload || 0) ? current : best
          )
        } else if (assignmentStrategy.value === 'experience') {
          selectedTeacher = qualifiedTeachers.reduce((best, current) => {
            const avgExpCurrent = current.specializations.reduce((sum, spec) => sum + spec.yearsExperience, 0) / current.specializations.length
            const avgExpBest = best.specializations.reduce((sum, spec) => sum + spec.yearsExperience, 0) / best.specializations.length
            return avgExpCurrent > avgExpBest ? current : best
          })
        }

        teacherId = selectedTeacher.id
      }

      const teacher = getTeacherById(teacherId)
      if (!teacher) continue

      const newWorkload = (teacher.currentWorkload || 0) + course.weeklyHours
      const conflicts = []

      // Check for conflicts
      if (newWorkload > teacher.maxWeeklyHours) {
        conflicts.push('Exceeds maximum weekly hours')
      }

      if (newWorkload > teacher.maxWeeklyHours * 0.9) {
        conflicts.push('High workload warning')
      }

      // Check specialization match
      if (course.requiredSpecializations.length > 0) {
        const matchingSpecs = teacher.specializations.filter(spec =>
          course.requiredSpecializations.includes(spec.subjectCode)
        )
        if (matchingSpecs.length < course.requiredSpecializations.length) {
          conflicts.push('Missing required specializations')
        }
      }

      let status: 'valid' | 'warning' | 'conflict' = 'valid'
      if (conflicts.some(c => c.includes('exceeds') || c.includes('missing'))) {
        status = 'conflict'
      } else if (conflicts.length > 0) {
        status = 'warning'
      }

      assignments.push({
        courseId,
        courseCode: course.courseCode,
        courseName: course.courseName,
        status,
        teacherName: `${teacher.user.firstName} ${teacher.user.lastName}`,
        newWorkload,
        maxWorkload: teacher.maxWeeklyHours,
        conflicts
      })
    }

    previewAssignments.value = assignments
    showPreview.value = true
  } catch (error) {
    console.error('Failed to generate preview:', error)
  }
}

const getPreviewVariant = (status: string) => {
  switch (status) {
    case 'valid': return 'default'
    case 'warning': return 'outline'
    case 'conflict': return 'destructive'
    default: return 'secondary'
  }
}

const executeBulkAssignment = async () => {
  loading.value = true
  try {
    const assignments = previewAssignments.value
      .filter(assignment => assignment.status !== 'conflict' || conflictHandling.value !== 'strict')
      .map(assignment => {
        const course = getCourseById(assignment.courseId)
        const teacher = props.teachers.find(t =>
          `${t.user.firstName} ${t.user.lastName}` === assignment.teacherName
        )

        return {
          courseId: assignment.courseId,
          teacherId: teacher?.id
        }
      })
      .filter(a => a.teacherId)

    const result = await assignmentService.bulkAssignTeachers({ assignments })

    if (result.failed > 0) {
      console.warn('Some assignments failed:', result.errors)
    }

    emit('save')
  } catch (error) {
    console.error('Failed to execute bulk assignment:', error)
  } finally {
    loading.value = false
  }
}

// Reset state when modal opens
watch(() => props.open, (newOpen) => {
  if (newOpen) {
    selectedCourses.value.clear()
    Object.keys(manualAssignments).forEach(key => delete manualAssignments[key])
    assignmentStrategy.value = 'optimal'
    conflictHandling.value = 'warning'
    autoAssign.value = true
    showPreview.value = false
    previewAssignments.value = []
  }
})
</script>