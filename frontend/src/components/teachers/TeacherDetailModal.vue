<template>
  <div class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl mx-4 max-h-[90vh] overflow-y-auto">
      <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
        <h3 class="text-lg font-medium text-gray-900">Teacher Details</h3>
        <Button variant="ghost" @click="$emit('close')">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </Button>
      </div>

      <div v-if="teacher" class="px-6 py-4 space-y-6">
        <!-- Header Information -->
        <div class="flex items-center space-x-4">
          <div class="h-16 w-16 rounded-full bg-blue-100 flex items-center justify-center">
            <span class="text-blue-800 text-xl font-bold">
              {{ teacher.user.firstName.charAt(0) }}{{ teacher.user.lastName.charAt(0) }}
            </span>
          </div>
          <div>
            <h2 class="text-xl font-semibold text-gray-900">
              {{ teacher.user.firstName }} {{ teacher.user.lastName }}
            </h2>
            <p class="text-gray-600">{{ teacher.title }} • {{ teacher.department.name }}</p>
            <div class="flex items-center gap-4 mt-1 text-sm text-gray-500">
              <span>{{ teacher.employeeId }}</span>
              <span>•</span>
              <span>{{ teacher.user.email }}</span>
            </div>
          </div>
        </div>

        <!-- Contact Information -->
        <Card>
          <CardHeader>
            <CardTitle class="text-base">Contact Information</CardTitle>
          </CardHeader>
          <CardContent class="space-y-3">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-sm font-medium text-gray-500">Email</label>
                <p class="text-gray-900">{{ teacher.user.email }}</p>
              </div>
              <div>
                <label class="text-sm font-medium text-gray-500">Phone</label>
                <p class="text-gray-900">{{ teacher.phone || 'Not provided' }}</p>
              </div>
              <div>
                <label class="text-sm font-medium text-gray-500">Office Location</label>
                <p class="text-gray-900">{{ teacher.officeLocation || 'Not provided' }}</p>
              </div>
              <div>
                <label class="text-sm font-medium text-gray-500">Employee ID</label>
                <p class="text-gray-900">{{ teacher.employeeId }}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- Workload Information -->
        <Card>
          <CardHeader>
            <CardTitle class="text-base">Workload Limits</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="grid grid-cols-2 gap-4">
              <div class="text-center p-4 bg-blue-50 rounded-lg">
                <div class="text-2xl font-bold text-blue-700">{{ teacher.maxWeeklyHours }}h</div>
                <div class="text-sm text-blue-600">Max Weekly Hours</div>
              </div>
              <div class="text-center p-4 bg-green-50 rounded-lg">
                <div class="text-2xl font-bold text-green-700">{{ teacher.maxCoursesPerSemester }}</div>
                <div class="text-sm text-green-600">Max Courses/Semester</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- Specializations -->
        <Card>
          <CardHeader>
            <CardTitle class="text-base">Specializations</CardTitle>
          </CardHeader>
          <CardContent>
            <div v-if="teacher.specializations.length === 0" class="text-center py-4 text-gray-500">
              No specializations added yet
            </div>
            <div v-else class="space-y-3">
              <div v-for="spec in teacher.specializations" :key="spec.id" class="border rounded-lg p-4">
                <div class="flex justify-between items-start">
                  <div>
                    <h4 class="font-medium text-gray-900">{{ spec.subjectCode }}</h4>
                    <p v-if="spec.subjectName" class="text-sm text-gray-600">{{ spec.subjectName }}</p>
                    <div class="flex items-center gap-3 mt-2 text-sm">
                      <Badge :variant="getProficiencyBadgeVariant(spec.proficiencyLevel)">
                        {{ spec.proficiencyLevel.toLowerCase() }}
                      </Badge>
                      <span class="text-gray-500">{{ spec.yearsExperience }} years experience</span>
                      <Badge v-if="spec.certified" variant="outline">Certified</Badge>
                    </div>
                    <p v-if="spec.certificationDetails" class="text-xs text-gray-500 mt-1">
                      {{ spec.certificationDetails }}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- Quick Stats -->
        <Card>
          <CardHeader>
            <CardTitle class="text-base">Quick Stats</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="grid grid-cols-3 gap-4">
              <div class="text-center p-3 bg-purple-50 rounded-lg">
                <div class="text-xl font-bold text-purple-700">{{ teacher.specializations.length }}</div>
                <div class="text-xs text-purple-600">Specializations</div>
              </div>
              <div class="text-center p-3 bg-orange-50 rounded-lg">
                <div class="text-xl font-bold text-orange-700">{{ getExpertSpecializations() }}</div>
                <div class="text-xs text-orange-600">Expert Level</div>
              </div>
              <div class="text-center p-3 bg-teal-50 rounded-lg">
                <div class="text-xl font-bold text-teal-700">{{ getAverageExperience() }}</div>
                <div class="text-xs text-teal-600">Avg Experience</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- Actions -->
        <div class="flex justify-end gap-3 pt-4 border-t">
          <Button variant="outline" @click="$emit('manage-availability', teacher)">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            Manage Availability
          </Button>
          <Button @click="$emit('edit', teacher)">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
            Edit Teacher
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Card, { CardHeader, CardTitle, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import teacherService, { type Teacher } from '@/services/teacherService'

interface Props {
  teacher?: Teacher | null
}

interface Emits {
  (e: 'close'): void
  (e: 'edit', teacher: Teacher): void
  (e: 'manage-availability', teacher: Teacher): void
}

defineProps<Props>()
defineEmits<Emits>()

const getProficiencyBadgeVariant = (level: string) => {
  switch (level) {
    case 'EXPERT':
      return 'default'
    case 'ADVANCED':
      return 'secondary'
    case 'INTERMEDIATE':
      return 'outline'
    case 'BEGINNER':
      return 'destructive'
    default:
      return 'outline'
  }
}

const getExpertSpecializations = () => {
  if (!props.teacher) return 0
  return props.teacher.specializations.filter(spec => spec.proficiencyLevel === 'EXPERT').length
}

const getAverageExperience = () => {
  if (!props.teacher || props.teacher.specializations.length === 0) return 0
  const total = props.teacher.specializations.reduce((sum, spec) => sum + spec.yearsExperience, 0)
  return Math.round(total / props.teacher.specializations.length)
}
</script>