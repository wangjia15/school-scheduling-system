<template>
  <div class="relative">
    <!-- Vertical line from parent to children -->
    <div class="absolute left-1/2 top-0 w-0.5 h-6 bg-border transform -translate-x-1/2"></div>

    <!-- Current level prerequisites -->
    <div class="flex space-x-6">
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
          <div class="text-xs text-muted-foreground mt-1">
            {{ prerequisite.prerequisiteCourse.department.name }} • {{ prerequisite.prerequisiteCourse.credits }}cr
          </div>
        </div>

        <!-- Sub-prerequisites (recursive) -->
        <div
          v-if="isNodeExpanded(prerequisite.prerequisiteCourse.id) && hasSubPrerequisites(prerequisite.prerequisiteCourse)"
          class="mt-6"
        >
          <PrerequisiteSubTree
            :course="prerequisite.prerequisiteCourse"
            :depth="depth + 1"
            :expanded-nodes="expandedNodes"
            :selected-node="selectedNode"
            @toggle-node="$emit('toggle-node', $event)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Badge from '@/components/ui/Badge.vue'
import { ChevronRight } from 'lucide-vue-next'
import type { Course } from '@/types/course'

interface Props {
  course: Course
  depth: number
  expandedNodes: Set<number>
  selectedNode?: number
}

interface Emits {
  (e: 'toggle-node', courseId: number): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

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
  } else {
    classes.push('border-border')
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
</script>