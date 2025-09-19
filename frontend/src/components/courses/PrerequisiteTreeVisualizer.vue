<template>
  <div class="space-y-4">
    <!-- Tree Visualization -->
    <div class="flex justify-center">
      <div class="relative">
        <!-- Root Course -->
        <div class="flex flex-col items-center">
          <div
            class="relative bg-primary text-primary-foreground px-4 py-2 rounded-lg text-sm font-medium shadow-md"
            :class="{ 'ring-2 ring-blue-500': isRootCourse }"
          >
            {{ course.courseCode }}
            <div class="text-xs opacity-90">{{ course.title }}</div>
          </div>

          <!-- Prerequisites Tree -->
          <div v-if="hasPrerequisites" class="mt-6">
            <div class="relative">
              <!-- Vertical line from root to prerequisites -->
              <div class="absolute left-1/2 top-0 w-0.5 h-6 bg-border transform -translate-x-1/2"></div>

              <!-- Prerequisites Level -->
              <div class="flex space-x-8">
                <div
                  v-for="(prerequisite, index) in course.prerequisites"
                  :key="prerequisite.id"
                  class="flex flex-col items-center relative"
                >
                  <!-- Horizontal line -->
                  <div class="absolute top-0 left-0 right-1/2 h-0.5 bg-border"></div>

                  <!-- Prerequisite Node -->
                  <div
                    class="mt-6 bg-card border-2 px-3 py-2 rounded-lg text-xs font-medium shadow-sm cursor-pointer hover:shadow-md transition-shadow"
                    :class="[
                      getNodeClasses(prerequisite),
                      { 'ring-2 ring-blue-500': isSelected(prerequisite.prerequisiteCourse.id) }
                    ]"
                    @click="toggleNode(prerequisite.prerequisiteCourse.id)"
                  >
                    <div class="flex items-center space-x-1">
                      <ChevronRight
                        v-if="hasSubPrerequisites(prerequisite.prerequisiteCourse)"
                        class="w-3 h-3 transform transition-transform"
                        :class="{ 'rotate-90': isNodeExpanded(prerequisite.prerequisiteCourse.id) }"
                      />
                      <span>{{ prerequisite.prerequisiteCourse.courseCode }}</span>
                      <Badge
                        v-if="prerequisite.isMandatory"
                        variant="default"
                        size="sm"
                        class="ml-1"
                      >
                        M
                      </Badge>
                    </div>
                    <div class="text-xs text-muted-foreground mt-1">
                      {{ prerequisite.prerequisiteCourse.title }}
                    </div>
                    <div v-if="prerequisite.minimumGrade" class="text-xs text-blue-600 mt-1">
                      Min: {{ prerequisite.minimumGrade }}%
                    </div>
                  </div>

                  <!-- Sub-prerequisites (recursive) -->
                  <div
                    v-if="isNodeExpanded(prerequisite.prerequisiteCourse.id) && hasSubPrerequisites(prerequisite.prerequisiteCourse)"
                    class="mt-6"
                  >
                    <PrerequisiteSubTree
                      :course="prerequisite.prerequisiteCourse"
                      :depth="1"
                      :expanded-nodes="expandedNodes"
                      :selected-node="selectedNode"
                      @toggle-node="$emit('toggle-node', $event)"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tree Info -->
    <div class="flex justify-center space-x-6 text-xs text-muted-foreground">
      <div class="flex items-center space-x-2">
        <div class="w-3 h-3 bg-primary rounded"></div>
        <span>Current Course</span>
      </div>
      <div class="flex items-center space-x-2">
        <div class="w-3 h-3 bg-card border-2 border-blue-500 rounded"></div>
        <span>Selected</span>
      </div>
      <div class="flex items-center space-x-2">
        <div class="w-3 h-3 bg-card border rounded"></div>
        <span>Prerequisite</span>
      </div>
    </div>

    <!-- Tree Statistics -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
      <div class="p-3 bg-muted rounded-lg">
        <div class="text-2xl font-bold text-primary">{{ totalPrerequisites }}</div>
        <div class="text-xs text-muted-foreground">Total Prerequisites</div>
      </div>
      <div class="p-3 bg-muted rounded-lg">
        <div class="text-2xl font-bold text-green-600">{{ mandatoryPrerequisites }}</div>
        <div class="text-xs text-muted-foreground">Mandatory</div>
      </div>
      <div class="p-3 bg-muted rounded-lg">
        <div class="text-2xl font-bold text-blue-600">{{ optionalPrerequisites }}</div>
        <div class="text-xs text-muted-foreground">Optional</div>
      </div>
      <div class="p-3 bg-muted rounded-lg">
        <div class="text-2xl font-bold text-purple-600">{{ maxDepth }}</div>
        <div class="text-xs text-muted-foreground">Max Depth</div>
      </div>
    </div>

    <!-- Legend -->
    <div class="p-4 bg-muted rounded-lg">
      <h4 class="font-medium mb-2">Legend</h4>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-2 text-xs">
        <div class="flex items-center space-x-2">
          <Badge variant="default" size="sm">M</Badge>
          <span>Mandatory prerequisite</span>
        </div>
        <div class="flex items-center space-x-2">
          <div class="w-3 h-3 bg-blue-500 rounded"></div>
          <span>Has minimum grade requirement</span>
        </div>
        <div class="flex items-center space-x-2">
          <ChevronRight class="w-3 h-3" />
          <span>Click to expand/collapse sub-prerequisites</span>
        </div>
        <div class="flex items-center space-x-2">
          <div class="w-3 h-3 bg-red-500 rounded"></div>
          <span>Circular dependency warning</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Badge from '@/components/ui/Badge.vue'
import PrerequisiteSubTree from './PrerequisiteSubTree.vue'
import { ChevronRight } from 'lucide-vue-next'
import type { Course } from '@/types/course'

interface Props {
  course: Course
  expandedNodes: Set<number>
  selectedNode?: number
  isRootCourse?: boolean
}

interface Emits {
  (e: 'toggle-node', courseId: number): void
}

const props = withDefaults(defineProps<Props>(), {
  isRootCourse: false
})

const emit = defineEmits<Emits>()

// Computed
const hasPrerequisites = computed(() => {
  return props.course.prerequisites && props.course.prerequisites.length > 0
})

const totalPrerequisites = computed(() => {
  return countPrerequisites(props.course)
})

const mandatoryPrerequisites = computed(() => {
  return countMandatoryPrerequisites(props.course)
})

const optionalPrerequisites = computed(() => {
  return totalPrerequisites.value - mandatoryPrerequisites.value
})

const maxDepth = computed(() => {
  return calculateMaxDepth(props.course)
})

// Methods
const isNodeExpanded = (courseId: number) => {
  return props.expandedNodes.has(courseId)
}

const isSelected = (courseId: number) => {
  return props.selectedNode === courseId
}

const toggleNode = (courseId: number) => {
  emit('toggle-node', courseId)
}

const getNodeClasses = (prerequisite: any) => {
  const classes = []

  if (prerequisite.minimumGrade) {
    classes.push('border-blue-500')
  }

  if (prerequisite.isMandatory) {
    classes.push('font-semibold')
  } else {
    classes.push('opacity-75')
  }

  return classes.join(' ')
}

const hasSubPrerequisites = (course: Course) => {
  return course.prerequisites && course.prerequisites.length > 0
}

const countPrerequisites = (course: Course): number => {
  if (!course.prerequisites) return 0

  let count = course.prerequisites.length
  for (const prereq of course.prerequisites) {
    count += countPrerequisites(prereq.prerequisiteCourse)
  }

  return count
}

const countMandatoryPrerequisites = (course: Course): number => {
  if (!course.prerequisites) return 0

  let count = course.prerequisites.filter(p => p.isMandatory).length
  for (const prereq of course.prerequisites) {
    count += countMandatoryPrerequisites(prereq.prerequisiteCourse)
  }

  return count
}

const calculateMaxDepth = (course: Course, currentDepth: number = 0): number => {
  if (!course.prerequisites || course.prerequisites.length === 0) {
    return currentDepth
  }

  let maxChildDepth = currentDepth
  for (const prereq of course.prerequisites) {
    const childDepth = calculateMaxDepth(prereq.prerequisiteCourse, currentDepth + 1)
    maxChildDepth = Math.max(maxChildDepth, childDepth)
  }

  return maxChildDepth
}
</script>