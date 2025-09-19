<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h3 class="text-lg font-semibold">Resource Allocation</h3>
      <Button variant="outline" size="sm" @click="optimizeAllocation">
        <Settings class="w-4 h-4 mr-2" />
        Optimize
      </Button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Teacher Utilization -->
      <Card>
        <CardHeader>
          <CardTitle>Teacher Utilization</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="space-y-3">
            <div
              v-for="teacher in teachers"
              :key="teacher.id"
              class="flex justify-between items-center"
            >
              <div>
                <div class="font-medium">{{ teacher.name }}</div>
                <div class="text-sm text-muted-foreground">{{ teacher.department }}</div>
              </div>
              <div class="text-right">
                <div class="text-sm font-medium">{{ teacher.utilization }}%</div>
                <div class="text-xs text-muted-foreground">
                  {{ teacher.assignedHours }}/{{ teacher.maxHours }}h
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Classroom Utilization -->
      <Card>
        <CardHeader>
          <CardTitle>Classroom Utilization</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="space-y-3">
            <div
              v-for="classroom in classrooms"
              :key="classroom.id"
              class="flex justify-between items-center"
            >
              <div>
                <div class="font-medium">{{ classroom.building }} {{ classroom.roomNumber }}</div>
                <div class="text-sm text-muted-foreground">Capacity: {{ classroom.capacity }}</div>
              </div>
              <div class="text-right">
                <div class="text-sm font-medium">{{ classroom.utilization }}%</div>
                <div class="text-xs text-muted-foreground">
                  {{ classroom.assignedSections }} sections
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Allocation Summary -->
    <Card>
      <CardHeader>
        <CardTitle>Allocation Summary</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4 text-center">
          <div>
            <div class="text-2xl font-bold text-blue-600">{{ teacherEfficiency }}%</div>
            <div class="text-sm text-muted-foreground">Teacher Efficiency</div>
          </div>
          <div>
            <div class="text-2xl font-bold text-green-600">{{ classroomEfficiency }}%</div>
            <div class="text-sm text-muted-foreground">Classroom Efficiency</div>
          </div>
          <div>
            <div class="text-2xl font-bold text-purple-600">{{ totalUtilization }}%</div>
            <div class="text-sm text-muted-foreground">Total Utilization</div>
          </div>
          <div>
            <div class="text-2xl font-bold text-yellow-600">{{ conflictsResolved }}</div>
            <div class="text-sm text-muted-foreground">Conflicts Resolved</div>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import { Settings } from 'lucide-vue-next'

interface Props {
  course: any
  sections: any[]
  teachers: any[]
  classrooms: any[]
}

interface Emits {
  (e: 'resource-allocated', allocation: any): void
}

defineProps<Props>()
defineEmits<Emits>()

// Mock data
const teacherEfficiency = ref(85)
const classroomEfficiency = ref(78)
const totalUtilization = ref(82)
const conflictsResolved = ref(12)

// Methods
const optimizeAllocation = () => {
  console.log('Optimizing resource allocation...')
}
</script>