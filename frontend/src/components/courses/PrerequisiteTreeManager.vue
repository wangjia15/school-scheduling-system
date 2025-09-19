<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h3 class="text-lg font-semibold">Prerequisite Tree Manager</h3>
        <p class="text-sm text-muted-foreground">
          Manage course prerequisites with visual tree structure
        </p>
      </div>
      <div class="flex space-x-2">
        <Button @click="expandAll" variant="outline" size="sm">
          <ChevronDown class="w-4 h-4 mr-1" />
          Expand All
        </Button>
        <Button @click="collapseAll" variant="outline" size="sm">
          <ChevronRight class="w-4 h-4 mr-1" />
          Collapse All
        </Button>
        <Button @click="validatePrerequisites" variant="outline" size="sm">
          <CheckCircle class="w-4 h-4 mr-1" />
          Validate
        </Button>
      </div>
    </div>

    <!-- Validation Messages -->
    <div v-if="validationResults.issues.length > 0" class="border border-red-200 rounded-lg p-4 bg-red-50">
      <div class="flex items-center mb-2">
        <AlertTriangle class="w-5 h-5 text-red-600 mr-2" />
        <span class="font-medium text-red-800">Prerequisite Issues Detected</span>
      </div>
      <ul class="text-sm text-red-700 space-y-1">
        <li v-for="issue in validationResults.issues" :key="issue" class="flex items-start">
          <span class="text-red-500 mr-2">•</span>
          {{ issue }}
        </li>
      </ul>
    </div>

    <div v-if="validationResults.circularDependencies.length > 0" class="border border-orange-200 rounded-lg p-4 bg-orange-50">
      <div class="flex items-center mb-2">
        <AlertTriangle class="w-5 h-5 text-orange-600 mr-2" />
        <span class="font-medium text-orange-800">Circular Dependencies</span>
      </div>
      <ul class="text-sm text-orange-700 space-y-1">
        <li v-for="dep in validationResults.circularDependencies" :key="dep" class="flex items-start">
          <span class="text-orange-500 mr-2">•</span>
          {{ dep }}
        </li>
      </ul>
    </div>

    <!-- Current Course Info -->
    <Card>
      <CardContent class="p-4">
        <div class="flex items-center space-x-4">
          <div class="flex-1">
            <h4 class="font-medium">{{ currentCourse.courseCode }} - {{ currentCourse.title }}</h4>
            <p class="text-sm text-muted-foreground">{{ currentCourse.department.name }} • {{ currentCourse.credits }} credits</p>
          </div>
          <Badge :variant="getLevelVariant(currentCourse.level)">
            {{ formatLevel(currentCourse.level) }}
          </Badge>
        </div>
      </CardContent>
    </Card>

    <!-- Prerequisite Tree -->
    <div class="border rounded-lg p-4 bg-background">
      <div v-if="!hasPrerequisites" class="text-center py-8">
        <GitBranch class="w-12 h-12 mx-auto text-muted-foreground mb-4" />
        <p class="text-muted-foreground">No prerequisites configured</p>
        <Button @click="showAddPrerequisite = true" class="mt-4">
          <Plus class="w-4 h-4 mr-2" />
          Add Prerequisite
        </Button>
      </div>

      <div v-else class="space-y-4">
        <!-- Tree Visualization -->
        <div class="overflow-x-auto">
          <div class="min-w-full">
            <PrerequisiteTree
              :nodes="treeNodes"
              :selected-node="selectedNode"
              @node-select="handleNodeSelect"
              @node-toggle="handleNodeToggle"
              @node-remove="handleNodeRemove"
            />
          </div>
        </div>

        <!-- Add Prerequisite Button -->
        <div class="flex justify-center pt-4">
          <Button @click="showAddPrerequisite = true" variant="outline">
            <Plus class="w-4 h-4 mr-2" />
            Add Prerequisite
          </Button>
        </div>
      </div>
    </div>

    <!-- Prerequisite Details Panel -->
    <div v-if="selectedPrerequisite" class="border rounded-lg p-4 bg-muted/50">
      <div class="flex justify-between items-start mb-4">
        <h4 class="font-medium">Prerequisite Details</h4>
        <Button @click="selectedPrerequisite = null" variant="ghost" size="sm">
          <X class="w-4 h-4" />
        </Button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="text-sm font-medium">Course</label>
          <p class="text-sm text-muted-foreground">
            {{ selectedPrerequisite.prerequisiteCourse.courseCode }} - {{ selectedPrerequisite.prerequisiteCourse.title }}
          </p>
        </div>
        <div>
          <label class="text-sm font-medium">Mandatory</label>
          <p class="text-sm text-muted-foreground">
            {{ selectedPrerequisite.isMandatory ? 'Yes' : 'No' }}
          </p>
        </div>
        <div>
          <label class="text-sm font-medium">Minimum Grade</label>
          <p class="text-sm text-muted-foreground">
            {{ selectedPrerequisite.minimumGrade || 'None' }}
          </p>
        </div>
        <div>
          <label class="text-sm font-medium">Notes</label>
          <p class="text-sm text-muted-foreground">
            {{ selectedPrerequisite.notes || 'None' }}
          </p>
        </div>
      </div>

      <div class="flex space-x-2 mt-4">
        <Button @click="editPrerequisite(selectedPrerequisite)" variant="outline" size="sm">
          <Edit class="w-4 h-4 mr-1" />
          Edit
        </Button>
        <Button @click="removePrerequisite(selectedPrerequisite)" variant="destructive" size="sm">
          <Trash2 class="w-4 h-4 mr-1" />
          Remove
        </Button>
      </div>
    </div>

    <!-- Add/Edit Prerequisite Modal -->
    <div v-if="showAddPrerequisite || showEditPrerequisite" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <Card class="w-full max-w-md">
        <CardHeader>
          <CardTitle>{{ showAddPrerequisite ? 'Add Prerequisite' : 'Edit Prerequisite' }}</CardTitle>
          <CardDescription>
            {{ showAddPrerequisite ? 'Add a prerequisite course' : 'Update prerequisite details' }}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form @submit.prevent="savePrerequisite" class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1">Prerequisite Course</label>
              <SelectInput
                v-model="prerequisiteForm.prerequisiteCourseId"
                :options="availableCourses"
                placeholder="Select a course"
                :disabled="!!showEditPrerequisite"
                required
              />
            </div>

            <div>
              <label class="block text-sm font-medium mb-1">Mandatory</label>
              <select
                v-model="prerequisiteForm.isMandatory"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
              >
                <option :value="true">Yes - Required</option>
                <option :value="false">No - Recommended</option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium mb-1">Minimum Grade</label>
              <Input
                v-model.number="prerequisiteForm.minimumGrade"
                type="number"
                min="0"
                max="100"
                step="0.1"
                placeholder="e.g., 70.0"
              />
            </div>

            <div>
              <label class="block text-sm font-medium mb-1">Notes</label>
              <textarea
                v-model="prerequisiteForm.notes"
                class="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                placeholder="Additional notes about this prerequisite"
                rows="3"
              ></textarea>
            </div>

            <div class="flex justify-end space-x-2">
              <Button type="button" variant="outline" @click="cancelPrerequisiteForm">
                Cancel
              </Button>
              <Button type="submit" :disabled="isSaving">
                {{ isSaving ? 'Saving...' : (showAddPrerequisite ? 'Add' : 'Update') }}
              </Button>
            </div>
          </form>
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
import PrerequisiteTree from './PrerequisiteTree.vue'
import { useToast } from 'vue-toastification'
import type { Course, CoursePrerequisite, CourseTreeNode } from '@/types/course'
import { courseService } from '@/services/courseService'
import {
  GitBranch,
  ChevronDown,
  ChevronRight,
  CheckCircle,
  AlertTriangle,
  Plus,
  Edit,
  Trash2,
  X
} from 'lucide-vue-next'

interface Props {
  course: Course
}

const props = defineProps<Props>()

const toast = useToast()

// State
const treeNodes = ref<CourseTreeNode[]>([])
const selectedNode = ref<CourseTreeNode | null>(null)
const selectedPrerequisite = ref<CoursePrerequisite | null>(null)
const showAddPrerequisite = ref(false)
const showEditPrerequisite = ref(false)
const isSaving = ref(false)
const availableCourses = ref<Array<{ value: number; label: string }>>([])

// Validation Results
const validationResults = reactive({
  isValid: true,
  issues: [] as string[],
  circularDependencies: [] as string[]
})

// Prerequisite Form
const prerequisiteForm = reactive({
  prerequisiteCourseId: 0,
  isMandatory: true,
  minimumGrade: null as number | null,
  notes: ''
})

// Computed
const currentCourse = computed(() => props.course)
const hasPrerequisites = computed(() => props.course.prerequisites && props.course.prerequisites.length > 0)

// Methods
const loadPrerequisiteTree = async () => {
  try {
    const treeData = await courseService.getPrerequisiteTree(props.course.id)
    treeNodes.value = buildTreeNodes(treeData)
  } catch (error) {
    toast.error('Failed to load prerequisite tree')
  }
}

const buildTreeNodes = (data: any): CourseTreeNode[] => {
  // Convert API response to tree nodes
  const nodes: CourseTreeNode[] = []

  if (data.prerequisites) {
    data.prerequisites.forEach((prereq: any) => {
      nodes.push({
        course: prereq.course,
        children: buildTreeNodes(prereq),
        depth: 0,
        isExpanded: true,
        isSelected: false
      })
    })
  }

  return nodes
}

const loadAvailableCourses = async () => {
  try {
    const courses = await courseService.getCoursesForPrerequisites()
    availableCourses.value = courses
      .filter(course => course.id !== props.course.id)
      .map(course => ({
        value: course.id,
        label: `${course.courseCode} - ${course.title}`
      }))
  } catch (error) {
    toast.error('Failed to load available courses')
  }
}

const validatePrerequisites = async () => {
  try {
    const results = await courseService.validatePrerequisites(props.course.id)
    Object.assign(validationResults, results)

    if (results.isValid) {
      toast.success('Prerequisites are valid')
    } else {
      toast.error('Prerequisite validation failed')
    }
  } catch (error) {
    toast.error('Failed to validate prerequisites')
  }
}

const expandAll = () => {
  const expand = (nodes: CourseTreeNode[]) => {
    nodes.forEach(node => {
      node.isExpanded = true
      if (node.children.length > 0) {
        expand(node.children)
      }
    })
  }
  expand(treeNodes.value)
}

const collapseAll = () => {
  const collapse = (nodes: CourseTreeNode[]) => {
    nodes.forEach(node => {
      node.isExpanded = false
      if (node.children.length > 0) {
        collapse(node.children)
      }
    })
  }
  collapse(treeNodes.value)
}

const handleNodeSelect = (node: CourseTreeNode) => {
  selectedNode.value = node
  // Find the corresponding prerequisite
  const prereq = props.course.prerequisites.find(p => p.prerequisiteCourse.id === node.course.id)
  if (prereq) {
    selectedPrerequisite.value = prereq
  }
}

const handleNodeToggle = (node: CourseTreeNode) => {
  node.isExpanded = !node.isExpanded
}

const handleNodeRemove = async (node: CourseTreeNode) => {
  const prereq = props.course.prerequisites.find(p => p.prerequisiteCourse.id === node.course.id)
  if (prereq) {
    await removePrerequisite(prereq)
  }
}

const editPrerequisite = (prereq: CoursePrerequisite) => {
  Object.assign(prerequisiteForm, {
    prerequisiteCourseId: prereq.prerequisiteCourse.id,
    isMandatory: prereq.isMandatory,
    minimumGrade: prereq.minimumGrade,
    notes: prereq.notes || ''
  })
  showEditPrerequisite.value = true
}

const removePrerequisite = async (prereq: CoursePrerequisite) => {
  try {
    // Remove from the course's prerequisites
    const index = props.course.prerequisites.findIndex(p => p.id === prereq.id)
    if (index > -1) {
      props.course.prerequisites.splice(index, 1)
    }

    // Update the course
    await courseService.updateCourse(props.course.id, {
      courseCode: props.course.courseCode,
      title: props.course.title,
      description: props.course.description,
      departmentId: props.course.department.id,
      credits: props.course.credits,
      contactHoursPerWeek: props.course.contactHoursPerWeek,
      theoryHours: props.course.theoryHours,
      labHours: props.course.labHours,
      level: props.course.level,
      maxStudents: props.course.maxStudents,
      minStudents: props.course.minStudents,
      requiresLab: props.course.requiresLab,
      prerequisites: props.course.prerequisites.map(p => ({
        prerequisiteCourseId: p.prerequisiteCourse.id,
        isMandatory: p.isMandatory,
        minimumGrade: p.minimumGrade,
        notes: p.notes
      }))
    })

    toast.success('Prerequisite removed successfully')
    selectedPrerequisite.value = null
    loadPrerequisiteTree()
  } catch (error) {
    toast.error('Failed to remove prerequisite')
  }
}

const savePrerequisite = async () => {
  try {
    isSaving.value = true

    if (showAddPrerequisite.value) {
      // Add new prerequisite
      const newPrereq = {
        prerequisiteCourseId: prerequisiteForm.prerequisiteCourseId,
        isMandatory: prerequisiteForm.isMandatory,
        minimumGrade: prerequisiteForm.minimumGrade,
        notes: prerequisiteForm.notes
      }

      props.course.prerequisites.push({
        id: Date.now(), // Temporary ID
        prerequisiteCourse: availableCourses.value.find(c => c.value === prerequisiteForm.prerequisiteCourseId)!.course,
        isMandatory: prerequisiteForm.isMandatory,
        minimumGrade: prerequisiteForm.minimumGrade,
        notes: prerequisiteForm.notes
      } as any)
    }

    // Update the course
    await courseService.updateCourse(props.course.id, {
      courseCode: props.course.courseCode,
      title: props.course.title,
      description: props.course.description,
      departmentId: props.course.department.id,
      credits: props.course.credits,
      contactHoursPerWeek: props.course.contactHoursPerWeek,
      theoryHours: props.course.theoryHours,
      labHours: props.course.labHours,
      level: props.course.level,
      maxStudents: props.course.maxStudents,
      minStudents: props.course.minStudents,
      requiresLab: props.course.requiresLab,
      prerequisites: props.course.prerequisites.map(p => ({
        prerequisiteCourseId: p.prerequisiteCourse.id,
        isMandatory: p.isMandatory,
        minimumGrade: p.minimumGrade,
        notes: p.notes
      }))
    })

    toast.success(`Prerequisite ${showAddPrerequisite.value ? 'added' : 'updated'} successfully`)
    cancelPrerequisiteForm()
    loadPrerequisiteTree()
  } catch (error) {
    toast.error(`Failed to ${showAddPrerequisite.value ? 'add' : 'update'} prerequisite`)
  } finally {
    isSaving.value = false
  }
}

const cancelPrerequisiteForm = () => {
  showAddPrerequisite.value = false
  showEditPrerequisite.value = false
  Object.assign(prerequisiteForm, {
    prerequisiteCourseId: 0,
    isMandatory: true,
    minimumGrade: null,
    notes: ''
  })
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
  loadPrerequisiteTree()
  loadAvailableCourses()
})
</script>