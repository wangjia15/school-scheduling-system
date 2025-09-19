<template>
  <div class="space-y-6">
    <!-- Course Sections Header -->
    <div class="flex justify-between items-center">
      <div>
        <h3 class="text-lg font-semibold">Course Sections</h3>
        <p class="text-sm text-muted-foreground">
          {{ sections.length }} sections scheduled for {{ course.courseCode }}
        </p>
      </div>
      <Button @click="$emit('create-section')">
        <Plus class="w-4 h-4 mr-2" />
        Add Section
      </Button>
    </div>

    <!-- Sections List -->
    <div v-if="sections.length === 0" class="text-center py-8 text-muted-foreground">
      <Calendar class="w-12 h-12 mx-auto mb-4" />
      <p>No sections scheduled yet</p>
      <Button variant="outline" class="mt-4" @click="$emit('create-section')">
        Create First Section
      </Button>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="section in sections"
        :key="section.id"
        class="border rounded-lg p-4 hover:shadow-md transition-shadow"
      >
        <div class="flex justify-between items-start mb-3">
          <div>
            <div class="flex items-center space-x-2">
              <h4 class="font-semibold">{{ course.courseCode }}-{{ section.sectionNumber }}</h4>
              <Badge :variant="section.status === 'ACTIVE' ? 'default' : 'secondary'">
                {{ section.status }}
              </Badge>
            </div>
            <div class="text-sm text-muted-foreground">
              {{ section.semester }} {{ section.academicYear }}
            </div>
          </div>
          <div class="flex space-x-2">
            <Button variant="ghost" size="sm" @click="$emit('edit-section', section)">
              <Edit class="w-4 h-4" />
            </Button>
            <Button variant="ghost" size="sm" @click="$emit('delete-section', section)" class="text-destructive">
              <Trash2 class="w-4 h-4" />
            </Button>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 text-sm">
          <div>
            <div class="font-medium">Schedule</div>
            <div class="text-muted-foreground">
              {{ formatDays(section.days) }} {{ section.startTime }}-{{ section.endTime }}
            </div>
          </div>
          <div>
            <div class="font-medium">Instructor</div>
            <div class="text-muted-foreground">{{ section.instructorName }}</div>
          </div>
          <div>
            <div class="font-medium">Classroom</div>
            <div class="text-muted-foreground">{{ section.classroomName }}</div>
          </div>
          <div>
            <div class="font-medium">Capacity</div>
            <div class="text-muted-foreground">{{ section.enrolled }}/{{ section.capacity }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Card, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import { Calendar, Plus, Edit, Trash2 } from 'lucide-vue-next'

interface Props {
  course: any
  sections: any[]
  teachers: any[]
  classrooms: any[]
}

interface Emits {
  (e: 'create-section'): void
  (e: 'edit-section', section: any): void
  (e: 'delete-section', section: any): void
  (e: 'section-created', section: any): void
  (e: 'section-updated', section: any): void
  (e: 'section-deleted', sectionId: number): void
}

defineProps<Props>()
defineEmits<Emits>()

// Methods
const formatDays = (days: string[]) => {
  const dayMap = {
    MONDAY: 'Mon',
    TUESDAY: 'Tue',
    WEDNESDAY: 'Wed',
    THURSDAY: 'Thu',
    FRIDAY: 'Fri',
    SATURDAY: 'Sat',
    SUNDAY: 'Sun'
  }

  return days.map(day => dayMap[day] || day).join(', ')
}
</script>