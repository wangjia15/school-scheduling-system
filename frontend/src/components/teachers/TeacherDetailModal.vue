<template>
  <div v-if="open" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
    <Card class="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
      <CardHeader>
        <div class="flex items-center justify-between">
          <CardTitle>Teacher Details</CardTitle>
          <Button variant="ghost" size="icon" @click="$emit('update:open', false)">
            <X class="h-4 w-4" />
          </Button>
        </div>
        <CardDescription>
          Detailed information about the teacher
        </CardDescription>
      </CardHeader>
      <CardContent>

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
        <div class="flex items-center justify-end gap-3 pt-4 border-t">
          <Button variant="outline" @click="$emit('close')">
            Close
          </Button>
          <Button @click="$emit('edit', teacher)">
            <Pencil class="h-4 w-4 mr-2" />
            Edit Teacher
          </Button>
        </div>
      </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import { X, Pencil } from 'lucide-vue-next'
import { type Teacher } from '@/services/teacherService'

interface Props {
  open: boolean
  teacher?: Teacher | null
}

interface Emits {
  'update:open': [value: boolean]
  'edit': [teacher: Teacher]: void
  'close': []
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

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