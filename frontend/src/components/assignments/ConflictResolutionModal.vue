<template>
  <div v-if="open" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
    <Card class="w-full max-w-3xl max-h-[90vh] overflow-y-auto">
      <CardHeader>
        <div class="flex items-center justify-between">
          <CardTitle>Schedule Conflict Resolution</CardTitle>
          <Button variant="ghost" size="icon" @click="$emit('update:open', false)">
            <X class="h-4 w-4" />
          </Button>
        </div>
        <CardDescription>
          {{ course.courseName }} ({{ course.courseCode }}) - Resolve scheduling conflicts
        </CardDescription>
      </CardHeader>
      <CardContent>
        <!-- Course Summary -->
        <div class="mb-6 p-4 bg-blue-50 rounded-lg">
          <h4 class="font-semibold text-blue-900 mb-2">Course Information</h4>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <span class="font-medium text-blue-700">Department:</span>
              <span class="text-blue-900 ml-1">{{ course.department.name }}</span>
            </div>
            <div>
              <span class="font-medium text-blue-700">Weekly Hours:</span>
              <span class="text-blue-900 ml-1">{{ course.weeklyHours }}h</span>
            </div>
            <div>
              <span class="font-medium text-blue-700">Assigned Teacher:</span>
              <span class="text-blue-900 ml-1">{{ course.assignedTeacher?.user.firstName }} {{ course.assignedTeacher?.user.lastName }}</span>
            </div>
            <div>
              <span class="font-medium text-blue-700">Schedule:</span>
              <span class="text-blue-900 ml-1" v-if="course.schedule">
                {{ course.schedule.dayOfWeek }} {{ course.schedule.startTime }}-{{ course.schedule.endTime }}
              </span>
              <span class="text-blue-900 ml-1" v-else>Not scheduled</span>
            </div>
          </div>
        </div>

        <!-- Conflicts List -->
        <div class="mb-6">
          <div class="flex items-center justify-between mb-4">
            <h4 class="text-lg font-medium">Detected Conflicts ({{ conflicts.length }})</h4>
            <Button
              v-if="conflicts.length > 0"
              variant="outline"
              size="sm"
              @click="autoResolveConflicts"
              :disabled="autoResolving"
            >
              <RefreshCw v-if="!autoResolving" class="h-4 w-4 mr-2" />
              <Loader v-else class="h-4 w-4 mr-2 animate-spin" />
              Auto-Resolve
            </Button>
          </div>

          <div class="space-y-4">
            <div
              v-for="conflict in conflicts"
              :key="conflict.id"
              class="border rounded-lg p-4"
              :class="getConflictBorderClass(conflict.severity)"
            >
              <div class="flex items-start justify-between mb-3">
                <div class="flex items-start gap-3">
                  <div :class="getConflictIconClass(conflict.severity)">
                    <AlertTriangle class="h-5 w-5" />
                  </div>
                  <div class="flex-1">
                    <div class="flex items-center gap-2 mb-1">
                      <h5 class="font-medium">{{ formatConflictType(conflict.conflictType) }}</h5>
                      <Badge :variant="getConflictVariant(conflict.severity)" class="text-xs">
                        {{ conflict.severity }}
                      </Badge>
                      <Badge v-if="conflict.isResolved" variant="outline" class="text-xs bg-green-100 text-green-800">
                        Resolved
                      </Badge>
                    </div>
                    <p class="text-sm text-gray-600 mb-2">{{ conflict.description }}</p>

                    <!-- Conflicting Courses -->
                    <div v-if="conflict.conflictingCourses.length > 0" class="mb-3">
                      <h6 class="text-sm font-medium text-gray-700 mb-2">Conflicting Courses:</h6>
                      <div class="space-y-2">
                        <div
                          v-for="conflictingCourse in conflict.conflictingCourses"
                          :key="conflictingCourse.id"
                          class="flex items-center justify-between p-2 bg-gray-50 rounded"
                        >
                          <div>
                            <div class="font-medium text-sm">{{ conflictingCourse.courseCode }}</div>
                            <div class="text-xs text-gray-600">{{ conflictingCourse.courseName }}</div>
                          </div>
                          <div class="text-xs text-gray-500">
                            {{ conflictingCourse.schedule.dayOfWeek }}
                            {{ conflictingCourse.schedule.startTime }}-{{ conflictingCourse.schedule.endTime }}
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- Resolution Options -->
                    <div v-if="!conflict.isResolved" class="space-y-2">
                      <h6 class="text-sm font-medium text-gray-700">Suggested Resolution:</h6>
                      <p class="text-sm text-blue-700 bg-blue-50 p-2 rounded">
                        {{ conflict.suggestedResolution }}
                      </p>

                      <div class="flex flex-wrap gap-2 mt-3">
                        <Button
                          v-for="option in getResolutionOptions(conflict)"
                          :key="option.value"
                          variant="outline"
                          size="sm"
                          @click="selectResolution(conflict.id, option.value)"
                          :class="{
                            'bg-primary text-primary-foreground': selectedResolutions[conflict.id] === option.value
                          }"
                        >
                          {{ option.label }}
                        </Button>
                      </div>

                      <div v-if="selectedResolutions[conflict.id]" class="mt-3">
                        <label class="block text-sm font-medium text-gray-700 mb-1">Resolution Details:</label>
                        <textarea
                          v-model="resolutionDetails[conflict.id]"
                          rows="2"
                          class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                          placeholder="Optional additional details about the resolution..."
                        />
                      </div>
                    </div>

                    <!-- Resolved Info -->
                    <div v-else class="mt-3 p-3 bg-green-50 rounded border border-green-200">
                      <div class="flex items-center gap-2 text-green-800">
                        <Check class="h-4 w-4" />
                        <span class="text-sm font-medium">Resolved on {{ formatDate(conflict.resolvedAt) }}</span>
                      </div>
                      <p v-if="conflict.resolutionDetails" class="text-sm text-green-700 mt-1">
                        {{ conflict.resolutionDetails }}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="conflicts.length === 0" class="text-center py-8 text-gray-500">
              No conflicts detected for this course
            </div>
          </div>
        </div>

        <!-- Resolution Summary -->
        <div v-if="hasSelectedResolutions" class="mb-6 p-4 bg-yellow-50 rounded-lg border border-yellow-200">
          <h4 class="font-semibold text-yellow-900 mb-2">Resolution Summary</h4>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span class="font-medium text-yellow-700">Conflicts to Resolve:</span>
              <span class="text-yellow-900 ml-1">{{ Object.keys(selectedResolutions).length }}</span>
            </div>
            <div>
              <span class="font-medium text-yellow-700">Resolution Methods:</span>
              <span class="text-yellow-900 ml-1">
                {{ Object.values(selectedResolutions).filter(Boolean).length }} selected
              </span>
            </div>
          </div>
        </div>

        <!-- Form Actions -->
        <div class="flex items-center justify-between pt-4 border-t">
          <div class="text-sm text-gray-600">
            {{ conflicts.filter(c => c.isResolved).length }} of {{ conflicts.length }} conflicts resolved
          </div>
          <div class="flex items-center gap-3">
            <Button variant="outline" @click="$emit('cancel')">
              Cancel
            </Button>
            <Button
              v-if="hasSelectedResolutions"
              @click="applyResolutions"
              :disabled="loading"
            >
              <Check class="h-4 w-4 mr-2" />
              Apply Resolutions
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import { X, AlertTriangle, Check, RefreshCw, Loader } from 'lucide-vue-next'
import type { Course, AssignmentConflict } from '@/services/assignmentService'
import assignmentService, { type ConflictResolutionRequest } from '@/services/assignmentService'

interface Props {
  open: boolean
  course: Course
  conflicts: AssignmentConflict[]
}

interface Emits {
  'update:open': [value: boolean]
  'resolve': []
  'cancel': []
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const autoResolving = ref(false)
const selectedResolutions = reactive<Record<number, string>>({})
const resolutionDetails = reactive<Record<number, string>>({})

// Computed
const hasSelectedResolutions = computed(() => {
  return Object.keys(selectedResolutions).some(key => selectedResolutions[key])
})

// Methods
const getConflictBorderClass = (severity: string) => {
  switch (severity) {
    case 'CRITICAL': return 'border-red-300 bg-red-50'
    case 'HIGH': return 'border-orange-300 bg-orange-50'
    case 'MEDIUM': return 'border-yellow-300 bg-yellow-50'
    case 'LOW': return 'border-blue-300 bg-blue-50'
    default: return 'border-gray-300 bg-gray-50'
  }
}

const getConflictIconClass = (severity: string) => {
  switch (severity) {
    case 'CRITICAL': return 'text-red-600 bg-red-100 rounded-full p-1'
    case 'HIGH': return 'text-orange-600 bg-orange-100 rounded-full p-1'
    case 'MEDIUM': return 'text-yellow-600 bg-yellow-100 rounded-full p-1'
    case 'LOW': return 'text-blue-600 bg-blue-100 rounded-full p-1'
    default: return 'text-gray-600 bg-gray-100 rounded-full p-1'
  }
}

const getConflictVariant = (severity: string) => {
  switch (severity) {
    case 'CRITICAL': return 'destructive'
    case 'HIGH': return 'destructive'
    case 'MEDIUM': return 'outline'
    case 'LOW': return 'secondary'
    default: return 'outline'
  }
}

const formatConflictType = (type: string) => {
  return type.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}

const getResolutionOptions = (conflict: AssignmentConflict) => {
  const baseOptions = [
    { value: 'ACCEPT_CONFLICT', label: 'Accept Conflict' },
    { value: 'CHANGE_SCHEDULE', label: 'Change Schedule' },
    { value: 'CHANGE_ROOM', label: 'Change Room' }
  ]

  if (conflict.conflictType === 'WORKLOAD_EXCEEDED') {
    baseOptions.push({ value: 'REDUCE_HOURS', label: 'Reduce Hours' })
  }

  if (conflict.conflictType === 'SCHEDULE_OVERLAP') {
    baseOptions.push({ value: 'CHANGE_TEACHER', label: 'Change Teacher' })
  }

  baseOptions.push({ value: 'CANCEL_ASSIGNMENT', label: 'Cancel Assignment' })

  return baseOptions
}

const selectResolution = (conflictId: number, resolution: string) => {
  selectedResolutions[conflictId] = resolution
}

const autoResolveConflicts = async () => {
  autoResolving.value = true
  try {
    const result = await assignmentService.autoResolveConflicts(props.course.id)

    if (result.resolved > 0) {
      // Update conflicts to show resolved status
      props.conflicts.forEach(conflict => {
        if (result.resolved > 0) {
          conflict.isResolved = true
          conflict.resolvedAt = new Date().toISOString()
        }
      })
    }

    emit('resolve')
  } catch (error) {
    console.error('Failed to auto-resolve conflicts:', error)
  } finally {
    autoResolving.value = false
  }
}

const applyResolutions = async () => {
  loading.value = true
  try {
    const resolutionPromises = Object.entries(selectedResolutions)
      .filter(([_, resolution]) => resolution)
      .map(([conflictId, resolution]) => {
        const request: ConflictResolutionRequest = {
          conflictId: parseInt(conflictId),
          resolution: resolution as any,
          resolutionDetails: resolutionDetails[conflictId]
        }
        return assignmentService.resolveConflict(parseInt(conflictId), request)
      })

    await Promise.all(resolutionPromises)

    emit('resolve')
  } catch (error) {
    console.error('Failed to apply resolutions:', error)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString()
}
</script>