<template>
  <div class="container mx-auto px-4 py-8">
    <CourseList
      :departments="departments"
      @course-selected="handleCourseSelected"
      @prerequisite-managed="handlePrerequisiteManaged"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import CourseList from '@/components/courses/CourseList.vue'
import { useToast } from 'vue-toastification'
import type { Course } from '@/types/course'

const toast = useToast()
const departments = ref<Array<{ id: number; code: string; name: string }>>([])

// Load departments (mock data for now)
onMounted(async () => {
  try {
    // TODO: Replace with actual API call
    departments.value = [
      { id: 1, code: 'CS', name: 'Computer Science' },
      { id: 2, code: 'MATH', name: 'Mathematics' },
      { id: 3, code: 'PHYS', name: 'Physics' },
      { id: 4, code: 'CHEM', name: 'Chemistry' },
      { id: 5, code: 'ENGL', name: 'English' }
    ]
  } catch (error) {
    toast.error('Failed to load departments')
  }
})

const handleCourseSelected = (course: Course) => {
  console.log('Course selected:', course)
}

const handlePrerequisiteManaged = (course: Course) => {
  console.log('Prerequisite managed for:', course)
}
</script>