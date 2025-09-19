<template>
  <div class="space-y-4">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h3 class="text-lg font-semibold">Course Comparison</h3>
        <p class="text-sm text-muted-foreground">Compare {{ courses.length }} selected courses</p>
      </div>
      <Button @click="$emit('close')" variant="outline">
        Close
      </Button>
    </div>

    <!-- Course Headers -->
    <div class="grid" :style="{ gridTemplateColumns: `200px repeat(${courses.length}, 1fr)` }">
      <div></div>
      <div v-for="course in courses" :key="course.id" class="p-4 border rounded-lg">
        <div class="text-center">
          <div class="font-semibold">{{ course.courseCode }}</div>
          <div class="text-sm text-muted-foreground">{{ course.title }}</div>
          <Button
            @click="$emit('remove-course', course)"
            variant="ghost"
            size="sm"
            class="mt-2"
          >
            <X class="w-4 h-4" />
          </Button>
        </div>
      </div>
    </div>

    <!-- Comparison Table -->
    <div class="border rounded-lg overflow-hidden">
      <div class="grid" :style="{ gridTemplateColumns: `200px repeat(${courses.length}, 1fr)` }">
        <!-- Basic Information -->
        <div class="bg-muted p-3 font-medium border-r">Department</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          {{ course.department.name }}
        </div>

        <div class="bg-muted p-3 font-medium border-r border-t">Level</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          <Badge :variant="getLevelVariant(course.level)">
            {{ formatLevel(course.level) }}
          </Badge>
        </div>

        <div class="bg-muted p-3 font-medium border-r border-t">Credits</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          <span class="font-semibold">{{ course.credits }}</span>
        </div>

        <div class="bg-muted p-3 font-medium border-r border-t">Status</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          <Badge :variant="course.isActive ? 'default' : 'secondary'">
            {{ course.isActive ? 'Active' : 'Inactive' }}
          </Badge>
        </div>

        <!-- Schedule Information -->
        <div class="bg-muted p-3 font-medium border-r border-t">Contact Hours/Week</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          {{ course.contactHoursPerWeek }}
        </div>

        <div class="bg-muted p-3 font-medium border-r border-t">Theory Hours</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          {{ course.theoryHours }}
        </div>

        <div class="bg-muted p-3 font-medium border-r border-t">Lab Hours</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          {{ course.labHours }}
        </div>

        <!-- Capacity Information -->
        <div class="bg-muted p-3 font-medium border-r border-t">Min Students</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          {{ course.minStudents }}
        </div>

        <div class="bg-muted p-3 font-medium border-r border-t">Max Students</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          {{ course.maxStudents }}
        </div>

        <div class="bg-muted p-3 font-medium border-r border-t">Course Type</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          <Badge variant="outline">
            {{ getCourseType(course) }}
          </Badge>
        </div>

        <!-- Requirements -->
        <div class="bg-muted p-3 font-medium border-r border-t">Lab Required</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          {{ course.requiresLab ? 'Yes' : 'No' }}
        </div>

        <div class="bg-muted p-3 font-medium border-r border-t">Has Prerequisites</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          {{ course.hasPrerequisites ? 'Yes' : 'No' }}
        </div>

        <div class="bg-muted p-3 font-medium border-r border-t">Difficulty Level</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          <div class="flex items-center space-x-2">
            <div class="flex">
              <Star
                v-for="i in 5"
                :key="i"
                :class="[
                  'w-4 h-4',
                  i <= getDifficultyLevel(course) ? 'text-yellow-400 fill-current' : 'text-gray-300'
                ]"
              />
            </div>
            <span class="text-sm text-muted-foreground">
              ({{ getDifficultyLevel(course) }}/5)
            </span>
          </div>
        </div>

        <!-- Additional Features -->
        <div class="bg-muted p-3 font-medium border-r border-t">Can be taken by Undergrads</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          {{ course.canBeTakenByUndergraduates() ? 'Yes' : 'No' }}
        </div>

        <div class="bg-muted p-3 font-medium border-r border-t">Requires Instructor Approval</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          {{ course.requiresInstructorApproval() ? 'Yes' : 'No' }}
        </div>

        <div class="bg-muted p-3 font-medium border-r border-t">Core Course</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          {{ course.isCoreCourse() ? 'Yes' : 'No' }}
        </div>

        <!-- Description -->
        <div class="bg-muted p-3 font-medium border-r border-t">Description</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          <p class="text-sm">{{ course.description || 'No description available' }}</p>
        </div>

        <!-- Prerequisites Summary -->
        <div class="bg-muted p-3 font-medium border-r border-t">Prerequisites</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          <div v-if="course.hasPrerequisites">
            <p class="text-sm">{{ course.getPrerequisiteSummary() }}</p>
          </div>
          <p v-else class="text-sm text-muted-foreground">No prerequisites</p>
        </div>

        <!-- Actions -->
        <div class="bg-muted p-3 font-medium border-r border-t">Actions</div>
        <div v-for="course in courses" :key="course.id" class="p-3 border-r border-b">
          <div class="flex space-x-2">
            <Button variant="outline" size="sm" @click="viewDetails(course)">
              View Details
            </Button>
            <Button variant="outline" size="sm" @click="checkConflicts(course)">
              Check Conflicts
            </Button>
          </div>
        </div>
      </div>
    </div>

    <!-- Summary Section -->
    <Card>
      <CardHeader>
        <CardTitle>Comparison Summary</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <h4 class="font-medium mb-2">Recommendations</h4>
            <ul class="text-sm space-y-1">
              <li v-for="recommendation in recommendations" :key="recommendation" class="flex items-start">
                <CheckCircle class="w-4 h-4 text-green-600 mr-2 mt-0.5 flex-shrink-0" />
                {{ recommendation }}
              </li>
            </ul>
          </div>
          <div>
            <h4 class="font-medium mb-2">Potential Conflicts</h4>
            <ul class="text-sm space-y-1">
              <li v-for="conflict in conflicts" :key="conflict" class="flex items-start">
                <AlertTriangle class="w-4 h-4 text-orange-600 mr-2 mt-0.5 flex-shrink-0" />
                {{ conflict }}
              </li>
            </ul>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import type { Course } from '@/types/course'
import { X, Star, CheckCircle, AlertTriangle } from 'lucide-vue-next'

interface Props {
  courses: Course[]
}

interface Emits {
  (e: 'close'): void
  (e: 'remove-course', course: Course): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// Computed
const recommendations = computed(() => {
  const recs: string[] = []

  if (props.courses.length >= 2) {
    const hasUndergrad = props.courses.some(c => c.isUndergraduate())
    const hasGrad = props.courses.some(c => c.isGraduate() || c.isPhD())

    if (hasUndergrad && hasGrad) {
      recs.push('Mix of undergraduate and graduate courses - ensure you meet prerequisites')
    }

    const totalCredits = props.courses.reduce((sum, c) => sum + c.credits, 0)
    if (totalCredits > 18) {
      recs.push(`Heavy course load (${totalCredits} credits) - consider reducing`)
    }

    const labCourses = props.courses.filter(c => c.requiresLab || c.hasLabComponent())
    if (labCourses.length > 2) {
      recs.push('Multiple lab courses - ensure schedule can accommodate lab times')
    }

    const difficultCourses = props.courses.filter(c => c.getDifficultyLevel() >= 4)
    if (difficultCourses.length > 1) {
      recs.push('Multiple difficult courses - consider balancing workload')
    }
  }

  return recs
})

const conflicts = computed(() => {
  const conflicts: string[] = []

  if (props.courses.length >= 2) {
    const courseCodes = props.courses.map(c => c.courseCode)

    // Check for similar course codes (might be different sections)
    const similarCodes = courseCodes.filter(code => {
      const baseCode = code.replace(/\d+$/, '')
      return courseCodes.filter(c => c.replace(/\d+$/, '') === baseCode).length > 1
    })

    if (similarCodes.length > 0) {
      conflicts.push('Similar course codes detected - verify these are different courses')
    }

    // Check for time conflicts (simplified check)
    const highContactHours = props.courses.filter(c => c.contactHoursPerWeek > 6)
    if (highContactHours.length > 1) {
      conflicts.push('Multiple courses with high contact hours - potential scheduling conflicts')
    }
  }

  return conflicts
})

// Methods
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

const getCourseType = (course: Course) => {
  if (course.isHybridCourse()) return 'Hybrid'
  if (course.isLabCourse()) return 'Lab'
  return 'Lecture'
}

const getDifficultyLevel = (course: Course) => {
  return course.getDifficultyLevel()
}

const viewDetails = (course: Course) => {
  // In real implementation, this would navigate to course details
  console.log('View details for:', course.courseCode)
}

const checkConflicts = (course: Course) => {
  // In real implementation, this would check for scheduling conflicts
  console.log('Check conflicts for:', course.courseCode)
}
</script>