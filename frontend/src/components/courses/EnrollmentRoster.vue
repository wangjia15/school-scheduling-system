<template>
  <div class="space-y-4">
    <!-- Active Enrollments -->
    <Card>
      <CardHeader>
        <CardTitle>Active Enrollments ({{ activeEnrollments.length }})</CardTitle>
        <CardDescription>Students currently enrolled in the course</CardDescription>
      </CardHeader>
      <CardContent>
        <div v-if="activeEnrollments.length === 0" class="text-center py-8 text-muted-foreground">
          No active enrollments
        </div>
        <div v-else class="space-y-2">
          <div
            v-for="enrollment in activeEnrollments"
            :key="enrollment.id"
            class="flex items-center justify-between p-3 border rounded-lg hover:bg-muted/50"
          >
            <div class="flex items-center space-x-3">
              <div class="w-8 h-8 bg-primary/10 rounded-full flex items-center justify-center">
                <span class="text-sm font-medium text-primary">
                  {{ enrollment.studentName?.charAt(0).toUpperCase() }}
                </span>
              </div>
              <div>
                <div class="font-medium">{{ enrollment.studentName }}</div>
                <div class="text-sm text-muted-foreground">
                  ID: {{ enrollment.studentId }} • Enrolled: {{ formatDate(enrollment.enrolledAt) }}
                </div>
                <div v-if="enrollment.grade" class="text-sm">
                  Grade: <Badge :variant="getGradeVariant(enrollment.grade)">{{ enrollment.grade }}</Badge>
                </div>
              </div>
            </div>
            <div class="flex items-center space-x-2">
              <Badge :variant="enrollment.status === 'ACTIVE' ? 'default' : 'secondary'">
                {{ enrollment.status }}
              </Badge>
              <Button
                variant="ghost"
                size="sm"
                @click="$emit('remove-enrollment', enrollment)"
                class="text-destructive"
                title="Remove enrollment"
              >
                <X class="w-4 h-4" />
              </Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Waitlist -->
    <Card>
      <CardHeader>
        <CardTitle>Waitlist ({{ waitlist.length }})</CardTitle>
        <CardDescription>Students waiting to enroll in the course</CardDescription>
      </CardHeader>
      <CardContent>
        <div v-if="waitlist.length === 0" class="text-center py-8 text-muted-foreground">
          No students on waitlist
        </div>
        <div v-else class="space-y-2">
          <div
            v-for="(enrollment, index) in waitlist"
            :key="enrollment.id"
            class="flex items-center justify-between p-3 border rounded-lg hover:bg-muted/50"
          >
            <div class="flex items-center space-x-3">
              <div class="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
                <span class="text-sm font-medium text-yellow-600">{{ index + 1 }}</span>
              </div>
              <div>
                <div class="font-medium">{{ enrollment.studentName }}</div>
                <div class="text-sm text-muted-foreground">
                  ID: {{ enrollment.studentId }} • Added: {{ formatDate(enrollment.enrolledAt) }}
                </div>
              </div>
            </div>
            <div class="flex items-center space-x-2">
              <Badge variant="secondary">Position {{ index + 1 }}</Badge>
              <Button
                variant="ghost"
                size="sm"
                @click="$emit('promote-from-waitlist', enrollment)"
                :disabled="availableSlots === 0"
                title="Promote to active enrollment"
              >
                <ArrowUp class="w-4 h-4" />
              </Button>
              <Button
                variant="ghost"
                size="sm"
                @click="$emit('remove-enrollment', enrollment)"
                class="text-destructive"
                title="Remove from waitlist"
              >
                <X class="w-4 h-4" />
              </Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Export Actions -->
    <div class="flex justify-end space-x-2">
      <Button variant="outline" size="sm" @click="exportRoster">
        <Download class="w-4 h-4 mr-2" />
        Export Roster
      </Button>
      <Button variant="outline" size="sm" @click="exportWaitlist">
        <Download class="w-4 h-4 mr-2" />
        Export Waitlist
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import { X, ArrowUp, Download } from 'lucide-vue-next'

interface Props {
  course: any
  enrollments: any[]
  waitlist: any[]
  availableSlots?: number
}

interface Emits {
  (e: 'promote-from-waitlist', enrollment: any): void
  (e: 'remove-enrollment', enrollment: any): void
}

const props = defineProps<Props>()
defineEmits<Emits>()

// Computed
const activeEnrollments = computed(() => {
  return props.enrollments.filter(e => e.status === 'ACTIVE')
})

// Methods
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const getGradeVariant = (grade: string) => {
  const gradeNum = parseFloat(grade)
  if (gradeNum >= 90) return 'default'
  if (gradeNum >= 80) return 'secondary'
  if (gradeNum >= 70) return 'outline'
  return 'destructive'
}

const exportRoster = () => {
  // Export roster functionality
  console.log('Exporting roster...')
}

const exportWaitlist = () => {
  // Export waitlist functionality
  console.log('Exporting waitlist...')
}
</script>