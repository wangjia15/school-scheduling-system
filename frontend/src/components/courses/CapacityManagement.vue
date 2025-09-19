<template>
  <div class="space-y-6">
    <!-- Capacity Settings -->
    <Card>
      <CardHeader>
        <CardTitle>Capacity Settings</CardTitle>
        <CardDescription>Configure course enrollment limits and capacity rules</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div>
            <label class="block text-sm font-medium mb-1">Maximum Students</label>
            <Input
              v-model.number="capacitySettings.maxStudents"
              type="number"
              min="1"
              max="500"
              @change="handleCapacityChange"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Minimum Students</label>
            <Input
              v-model.number="capacitySettings.minStudents"
              type="number"
              min="1"
              max="500"
              @change="handleCapacityChange"
            />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1">Waitlist Limit</label>
            <Input
              v-model.number="capacitySettings.waitlistLimit"
              type="number"
              min="0"
              max="100"
              @change="handleCapacityChange"
            />
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Current Capacity Status -->
    <Card>
      <CardHeader>
        <CardTitle>Current Status</CardTitle>
        <CardDescription>Real-time capacity and enrollment information</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div class="text-center">
            <div class="text-2xl font-bold text-blue-600">{{ enrolledCount }}</div>
            <div class="text-sm text-muted-foreground">Enrolled</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-green-600">{{ availableSlots }}</div>
            <div class="text-sm text-muted-foreground">Available</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-yellow-600">{{ waitlistCount }}</div>
            <div class="text-sm text-muted-foreground">Waitlisted</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold text-purple-600">{{ fillPercentage }}%</div>
            <div class="text-sm text-muted-foreground">Filled</div>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Waitlist Management -->
    <Card>
      <CardHeader>
        <CardTitle>Waitlist Management</CardTitle>
        <CardDescription>Configure automatic waitlist processing rules</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="space-y-4">
          <div class="flex items-center space-x-2">
            <input
              type="checkbox"
              id="autoPromote"
              v-model="waitlistSettings.autoPromote"
              class="rounded"
            />
            <label for="autoPromote" class="text-sm font-medium">
              Automatically promote from waitlist when slots become available
            </label>
          </div>
          <div v-if="waitlistSettings.autoPromote">
            <label class="block text-sm font-medium mb-1">Promotion Order</label>
            <SelectInput
              v-model="waitlistSettings.promotionOrder"
              :options="promotionOrderOptions"
            />
          </div>
          <div class="flex items-center space-x-2">
            <input
              type="checkbox"
              id="notifyWaitlist"
              v-model="waitlistSettings.notifyWaitlist"
              class="rounded"
            />
            <label for="notifyWaitlist" class="text-sm font-medium">
              Notify students when promoted from waitlist
            </label>
          </div>
        </div>
      </CardContent>
    </Card>

    <!-- Actions -->
    <div class="flex justify-end space-x-2">
      <Button variant="outline" @click="resetSettings">
        Reset to Defaults
      </Button>
      <Button @click="saveSettings">
        Save Settings
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import SelectInput from '@/components/ui/SelectInput.vue'

interface Props {
  course: any
  enrollments: any[]
  waitlist: any[]
}

interface Emits {
  (e: 'update-capacity', settings: any): void
  (e: 'manage-waitlist'): void
}

const props = defineProps<Props>()
defineEmits<Emits>()

// State
const capacitySettings = reactive({
  maxStudents: props.course.maxStudents,
  minStudents: props.course.minStudents,
  waitlistLimit: 20
})

const waitlistSettings = reactive({
  autoPromote: true,
  promotionOrder: 'CHRONOLOGICAL',
  notifyWaitlist: true
})

// Options
const promotionOrderOptions = [
  { value: 'CHRONOLOGICAL', label: 'Chronological (First Come, First Served)' },
  { value: 'GPA', label: 'By GPA (Highest First)' },
  { value: 'CREDITS', label: 'By Credits Completed' },
  { value: 'YEAR', label: 'By Academic Year (Seniors First)' }
]

// Computed
const enrolledCount = computed(() => {
  return props.enrollments.filter(e => e.status === 'ACTIVE').length
})

const availableSlots = computed(() => {
  return Math.max(0, capacitySettings.maxStudents - enrolledCount.value)
})

const waitlistCount = computed(() => {
  return props.waitlist.length
})

const fillPercentage = computed(() => {
  return capacitySettings.maxStudents > 0
    ? Math.round((enrolledCount.value / capacitySettings.maxStudents) * 100)
    : 0
})

// Methods
const handleCapacityChange = () => {
  // Validate min/max relationship
  if (capacitySettings.minStudents > capacitySettings.maxStudents) {
    capacitySettings.minStudents = capacitySettings.maxStudents
  }
}

const saveSettings = () => {
  const settings = {
    capacity: capacitySettings,
    waitlist: waitlistSettings
  }
  emit('update-capacity', settings)
}

const resetSettings = () => {
  Object.assign(capacitySettings, {
    maxStudents: props.course.maxStudents,
    minStudents: props.course.minStudents,
    waitlistLimit: 20
  })
  Object.assign(waitlistSettings, {
    autoPromote: true,
    promotionOrder: 'CHRONOLOGICAL',
    notifyWaitlist: true
  })
}
</script>