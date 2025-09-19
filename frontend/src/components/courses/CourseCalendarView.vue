<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h3 class="text-lg font-semibold">Calendar View</h3>
      <div class="flex space-x-2">
        <Button variant="outline" size="sm">
          Week
        </Button>
        <Button variant="outline" size="sm">
          Month
        </Button>
      </div>
    </div>

    <div class="bg-muted rounded-lg p-8 text-center">
      <Calendar class="w-16 h-16 mx-auto mb-4 text-muted-foreground" />
      <p class="text-muted-foreground">Calendar view would be implemented here</p>
      <p class="text-sm text-muted-foreground mt-2">
        This would show course sections in a calendar format
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Calendar } from 'lucide-vue-next'

interface Props {
  course: any
  sections: any[]
}

interface Emits {
  (e: 'section-selected', section: any): void
}

defineProps<Props>()
defineEmits<Emits>()
</script>