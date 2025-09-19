<template>
  <div class="space-y-4">
    <!-- Waitlist Overview -->
    <div class="flex justify-between items-center">
      <div>
        <h3 class="text-lg font-semibold">Waitlist Management</h3>
        <p class="text-sm text-muted-foreground">
          {{ waitlist.length }} students on waitlist • {{ availableSlots }} available slots
        </p>
      </div>
      <div class="flex space-x-2">
        <Button
          variant="outline"
          size="sm"
          @click="autoPromoteAll"
          :disabled="availableSlots === 0 || waitlist.length === 0"
        >
          Promote All
        </Button>
        <Button
          variant="outline"
          size="sm"
          @click="exportWaitlist"
        >
          <Download class="w-4 h-4 mr-2" />
          Export
        </Button>
      </div>
    </div>

    <!-- Waitlist Settings -->
    <Card>
      <CardContent class="p-4">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="flex items-center space-x-2">
            <input
              type="checkbox"
              id="autoNotify"
              v-model="settings.autoNotify"
              class="rounded"
            />
            <label for="autoNotify" class="text-sm font-medium">
              Auto-notify when promoted
            </label>
          </div>
          <div class="flex items-center space-x-2">
            <input
              type="checkbox"
              id="autoExpire"
              v-model="settings.autoExpire"
              class="rounded"
            />
            <label for="autoExpire" class="text-sm font-medium">
              Auto-expire after 48 hours
            </label>
          </div>
          <div class="flex items-center space-x-2">
            <input
              type="checkbox"
              id="priorityEnrollment"
              v-model="settings.priorityEnrollment"
              class="rounded"
            />
            <label for="priorityEnrollment" class="text-sm font-medium">
              Priority enrollment for seniors
            </label>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Waitlist Table -->
    <div class="border rounded-lg">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="border-b bg-muted">
              <th class="text-left p-3 font-medium">Position</th>
              <th class="text-left p-3 font-medium">Student</th>
              <th class="text-left p-3 font-medium">Added</th>
              <th class="text-left p-3 font-medium">Priority</th>
              <th class="text-left p-3 font-medium">Status</th>
              <th class="text-left p-3 font-medium">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(student, index) in waitlist"
              :key="student.id"
              class="border-b hover:bg-muted/50"
            >
              <td class="p-3">
                <Badge variant="secondary">{{ index + 1 }}</Badge>
              </td>
              <td class="p-3">
                <div>
                  <div class="font-medium">{{ student.studentName }}</div>
                  <div class="text-sm text-muted-foreground">{{ student.studentId }}</div>
                </div>
              </td>
              <td class="p-3">
                <div class="text-sm">{{ formatDate(student.addedAt) }}</div>
                <div class="text-xs text-muted-foreground">{{ getTimeAgo(student.addedAt) }}</div>
              </td>
              <td class="p-3">
                <Badge :variant="getPriorityVariant(student.priority)">
                  {{ student.priority }}
                </Badge>
              </td>
              <td class="p-3">
                <Badge :variant="student.status === 'PENDING' ? 'secondary' : 'default'">
                  {{ student.status }}
                </Badge>
              </td>
              <td class="p-3">
                <div class="flex space-x-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    @click="$emit('promote-student', student)"
                    :disabled="availableSlots === 0"
                    title="Promote to enrollment"
                  >
                    <ArrowUp class="w-4 h-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    @click="contactStudent(student)"
                    title="Contact student"
                  >
                    <Mail class="w-4 h-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    @click="$emit('remove-from-waitlist', student)"
                    class="text-destructive"
                    title="Remove from waitlist"
                  >
                    <X class="w-4 h-4" />
                  </Button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="waitlist.length === 0" class="text-center py-8 text-muted-foreground">
        No students on waitlist
      </div>
    </div>

    <!-- Bulk Actions -->
    <div v-if="waitlist.length > 0" class="flex justify-between items-center">
      <div class="text-sm text-muted-foreground">
        {{ selectedStudents.length }} students selected
      </div>
      <div class="flex space-x-2">
        <Button
          variant="outline"
          size="sm"
          @click="contactSelected"
          :disabled="selectedStudents.length === 0"
        >
          <Mail class="w-4 h-4 mr-2" />
          Contact Selected
        </Button>
        <Button
          variant="outline"
          size="sm"
          @click="removeSelected"
          :disabled="selectedStudents.length === 0"
          class="text-destructive"
        >
          <X class="w-4 h-4 mr-2" />
          Remove Selected
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Card, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import { ArrowUp, Mail, X, Download } from 'lucide-vue-next'

interface Props {
  course: any
  waitlist: any[]
  availableSlots: number
}

interface Emits {
  (e: 'promote-student', student: any): void
  (e: 'remove-from-waitlist', student: any): void
  (e: 'close'): void
}

const props = defineProps<Props>()
defineEmits<Emits>()

// State
const selectedStudents = ref([])
const settings = reactive({
  autoNotify: true,
  autoExpire: false,
  priorityEnrollment: true
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

const getTimeAgo = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (hours < 1) return 'Just now'
  if (hours < 24) return `${hours}h ago`
  return `${days}d ago`
}

const getPriorityVariant = (priority: string) => {
  switch (priority) {
    case 'HIGH': return 'destructive'
    case 'MEDIUM': return 'default'
    case 'LOW': return 'secondary'
    default: return 'secondary'
  }
}

const autoPromoteAll = () => {
  // Promote all eligible students from waitlist
  console.log('Auto-promoting all eligible students...')
}

const exportWaitlist = () => {
  // Export waitlist data
  console.log('Exporting waitlist...')
}

const contactStudent = (student: any) => {
  // Contact individual student
  console.log('Contacting student:', student.studentName)
}

const contactSelected = () => {
  // Contact selected students
  console.log('Contacting selected students...')
}

const removeSelected = () => {
  // Remove selected students from waitlist
  console.log('Removing selected students...')
}
</script>