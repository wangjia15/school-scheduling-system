<template>
  <Card>
    <CardHeader>
      <CardTitle>Prerequisite Management</CardTitle>
      <CardDescription>
        Manage course prerequisites and visualize dependency relationships
      </CardDescription>
    </CardHeader>
    <CardContent>
      <div class="space-y-6">
        <!-- Current Course Info -->
        <div class="p-4 bg-muted rounded-lg">
          <h3 class="font-semibold mb-2">Current Course</h3>
          <div class="flex items-center space-x-4">
            <Badge variant="default">{{ course.courseCode }}</Badge>
            <span class="font-medium">{{ course.title }}</span>
            <Badge :variant="getLevelVariant(course.level)">{{ formatLevel(course.level) }}</Badge>
          </div>
        </div>

        <!-- Prerequisites Section -->
        <div>
          <div class="flex justify-between items-center mb-4">
            <h3 class="font-semibold">Prerequisites</h3>
            <Button size="sm" @click="showAddPrerequisite = true">
              <Plus class="w-4 h-4 mr-2" />
              Add Prerequisite
            </Button>
          </div>

          <div v-if="course.prerequisites && course.prerequisites.length > 0">
            <div class="space-y-2">
              <div
                v-for="prerequisite in course.prerequisites"
                :key="prerequisite.id"
                class="flex items-center justify-between p-3 border rounded-lg"
              >
                <div class="flex items-center space-x-3">
                  <div class="w-2 h-8 bg-blue-500 rounded-full"></div>
                  <div>
                    <div class="font-medium">{{ prerequisite.prerequisiteCourse.courseCode }}</div>
                    <div class="text-sm text-muted-foreground">{{ prerequisite.prerequisiteCourse.title }}</div>
                    <div class="text-xs text-muted-foreground">
                      {{ prerequisite.department.name }} • {{ prerequisite.credits }} credits
                    </div>
                  </div>
                </div>
                <div class="flex items-center space-x-2">
                  <Badge v-if="prerequisite.isMandatory" variant="default">Mandatory</Badge>
                  <Badge v-else variant="secondary">Optional</Badge>
                  <Badge v-if="prerequisite.minimumGrade" variant="outline">
                    Min: {{ prerequisite.minimumGrade }}%
                  </Badge>
                  <Button
                    variant="ghost"
                    size="sm"
                    @click="removePrerequisite(prerequisite)"
                    class="text-destructive"
                  >
                    <X class="w-4 h-4" />
                  </Button>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-muted-foreground">
            No prerequisites defined for this course
          </div>
        </div>

        <!-- Prerequisite Tree Visualization -->
        <div>
          <h3 class="font-semibold mb-4">Prerequisite Tree</h3>
          <div class="border rounded-lg p-4 bg-card">
            <PrerequisiteTreeVisualizer
              :course="course"
              :expanded-nodes="expandedNodes"
              @toggle-node="toggleNode"
            />
          </div>
        </div>

        <!-- Courses that depend on this one -->
        <div>
          <h3 class="font-semibold mb-4">Required For</h3>
          <div v-if="dependentCourses.length > 0">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div
                v-for="dependent in dependentCourses"
                :key="dependent.id"
                class="p-3 border rounded-lg"
              >
                <div class="font-medium">{{ dependent.courseCode }}</div>
                <div class="text-sm text-muted-foreground">{{ dependent.title }}</div>
                <div class="text-xs text-muted-foreground">
                  {{ dependent.department.name }} • {{ dependent.credits }} credits
                </div>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-muted-foreground">
            This course is not a prerequisite for any other courses
          </div>
        </div>

        <!-- Validation Results -->
        <div v-if="validationResults">
          <h3 class="font-semibold mb-4">Validation Results</h3>
          <div :class="[
            'p-4 rounded-lg',
            validationResults.isValid ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'
          ]">
            <div class="flex items-center space-x-2 mb-2">
              <CheckCircle v-if="validationResults.isValid" class="w-5 h-5 text-green-600" />
              <XCircle v-else class="w-5 h-5 text-red-600" />
              <span class="font-medium">
                {{ validationResults.isValid ? 'Valid Prerequisites' : 'Issues Found' }}
              </span>
            </div>
            <ul v-if="!validationResults.isValid && validationResults.issues.length > 0" class="text-sm space-y-1">
              <li v-for="issue in validationResults.issues" :key="issue" class="text-red-700">
                • {{ issue }}
              </li>
            </ul>
            <ul v-if="validationResults.circularDependencies.length > 0" class="text-sm space-y-1">
              <li v-for="dep in validationResults.circularDependencies" :key="dep" class="text-red-700">
                • Circular dependency: {{ dep }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </CardContent>

    <!-- Add Prerequisite Modal -->
    <div v-if="showAddPrerequisite" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <Card class="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <CardHeader>
          <CardTitle>Add Prerequisite</CardTitle>
          <CardDescription>
            Select courses that are prerequisites for {{ course.courseCode }}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <!-- Course Search -->
            <div>
              <label class="block text-sm font-medium mb-1">Search Courses</label>
              <Input
                v-model="prerequisiteSearch"
                placeholder="Search by course code or title..."
                @input="searchCourses"
              />
            </div>

            <!-- Available Courses -->
            <div class="max-h-60 overflow-y-auto border rounded-lg">
              <div
                v-for="availableCourse in availableCourses"
                :key="availableCourse.id"
                class="p-3 hover:bg-muted cursor-pointer border-b last:border-b-0"
                @click="selectCourse(availableCourse)"
              >
                <div class="flex items-center justify-between">
                  <div>
                    <div class="font-medium">{{ availableCourse.courseCode }}</div>
                    <div class="text-sm text-muted-foreground">{{ availableCourse.title }}</div>
                    <div class="text-xs text-muted-foreground">
                      {{ availableCourse.department.name }} • {{ availableCourse.credits }} credits
                    </div>
                  </div>
                  <Button size="sm" variant="outline">
                    Select
                  </Button>
                </div>
              </div>
            </div>

            <!-- Selected Course Configuration -->
            <div v-if="selectedPrerequisiteCourse" class="p-4 bg-muted rounded-lg">
              <h4 class="font-medium mb-3">{{ selectedPrerequisiteCourse.courseCode }} - {{ selectedPrerequisiteCourse.title }}</h4>
              <div class="space-y-3">
                <div class="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    id="isMandatory"
                    v-model="newPrerequisite.isMandatory"
                    class="rounded"
                  />
                  <label for="isMandatory" class="text-sm font-medium">Mandatory Prerequisite</label>
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">Minimum Grade (optional)</label>
                  <Input
                    v-model.number="newPrerequisite.minimumGrade"
                    type="number"
                    min="0"
                    max="100"
                    placeholder="e.g., 60"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium mb-1">Notes (optional)</label>
                  <textarea
                    v-model="newPrerequisite.notes"
                    class="w-full p-2 border rounded-md text-sm"
                    rows="2"
                    placeholder="Additional requirements or notes..."
                  ></textarea>
                </div>
              </div>
            </div>

            <div class="flex justify-end space-x-2">
              <Button variant="outline" @click="cancelAddPrerequisite">
                Cancel
              </Button>
              <Button
                @click="addPrerequisite"
                :disabled="!selectedPrerequisiteCourse"
              >
                Add Prerequisite
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </Card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Badge from '@/components/ui/Badge.vue'
import PrerequisiteTreeVisualizer from './PrerequisiteTreeVisualizer.vue'
import { useToast } from 'vue-toastification'
import type { Course, CoursePrerequisite } from '@/types/course'
import { courseService } from '@/services/courseService'
import { Plus, X, CheckCircle, XCircle } from 'lucide-vue-next'

interface Props {
  course: Course
}

interface Emits {
  (e: 'prerequisites-updated', course: Course): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const toast = useToast()

// State
const showAddPrerequisite = ref(false)
const prerequisiteSearch = ref('')
const availableCourses = ref<Course[]>([])
const selectedPrerequisiteCourse = ref<Course | null>(null)
const dependentCourses = ref<Course[]>([])
const expandedNodes = ref<Set<number>>(new Set())
const validationResults = ref<any>(null)

const newPrerequisite = reactive({
  isMandatory: true,
  minimumGrade: undefined as number | undefined,
  notes: ''
})

// Computed
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

// Methods
const loadDependentCourses = async () => {
  try {
    // Mock data for now - in real implementation, this would call an API
    dependentCourses.value = []
  } catch (error) {
    console.error('Failed to load dependent courses:', error)
  }
}

const searchCourses = async () => {
  try {
    if (prerequisiteSearch.value.trim()) {
      // Mock search - in real implementation, this would call an API
      availableCourses.value = [
        // Add mock courses here
      ]
    } else {
      availableCourses.value = []
    }
  } catch (error) {
    console.error('Failed to search courses:', error)
  }
}

const selectCourse = (course: Course) => {
  selectedPrerequisiteCourse.value = course
}

const cancelAddPrerequisite = () => {
  showAddPrerequisite.value = false
  selectedPrerequisiteCourse.value = null
  prerequisiteSearch.value = ''
  Object.assign(newPrerequisite, {
    isMandatory: true,
    minimumGrade: undefined,
    notes: ''
  })
}

const addPrerequisite = async () => {
  if (!selectedPrerequisiteCourse.value) return

  try {
    // In real implementation, this would call an API to add the prerequisite
    const newPrereq: CoursePrerequisite = {
      id: Date.now(), // Mock ID
      prerequisiteCourse: selectedPrerequisiteCourse.value,
      isMandatory: newPrerequisite.isMandatory,
      minimumGrade: newPrerequisite.minimumGrade,
      notes: newPrerequisite.notes
    }

    // Update the course's prerequisites
    if (!props.course.prerequisites) {
      props.course.prerequisites = []
    }
    props.course.prerequisites.push(newPrereq)

    emit('prerequisites-updated', props.course)
    cancelAddPrerequisite()
    toast.success('Prerequisite added successfully')

    // Re-validate prerequisites
    await validatePrerequisites()
  } catch (error) {
    toast.error('Failed to add prerequisite')
  }
}

const removePrerequisite = async (prerequisite: CoursePrerequisite) => {
  try {
    // In real implementation, this would call an API to remove the prerequisite
    const index = props.course.prerequisites?.findIndex(p => p.id === prerequisite.id)
    if (index !== undefined && index !== -1) {
      props.course.prerequisites?.splice(index, 1)
    }

    emit('prerequisites-updated', props.course)
    toast.success('Prerequisite removed successfully')

    // Re-validate prerequisites
    await validatePrerequisites()
  } catch (error) {
    toast.error('Failed to remove prerequisite')
  }
}

const toggleNode = (courseId: number) => {
  if (expandedNodes.value.has(courseId)) {
    expandedNodes.value.delete(courseId)
  } else {
    expandedNodes.value.add(courseId)
  }
}

const validatePrerequisites = async () => {
  try {
    // In real implementation, this would call an API
    validationResults.value = {
      isValid: true,
      issues: [],
      circularDependencies: []
    }
  } catch (error) {
    console.error('Failed to validate prerequisites:', error)
  }
}

// Lifecycle
onMounted(() => {
  loadDependentCourses()
  validatePrerequisites()
})

watch(() => props.course.prerequisites, () => {
  validatePrerequisites()
}, { deep: true })
</script>