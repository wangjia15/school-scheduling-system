<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h3 class="text-lg font-semibold">Conflict Resolution</h3>
      <Badge :variant="conflicts.length > 0 ? 'destructive' : 'default'">
        {{ conflicts.length }} Conflicts
      </Badge>
    </div>

    <div v-if="conflicts.length === 0" class="text-center py-8 text-muted-foreground">
      <CheckCircle class="w-12 h-12 mx-auto mb-4 text-green-500" />
      <p>No scheduling conflicts detected</p>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="conflict in conflicts"
        :key="conflict.id"
        class="border rounded-lg p-4"
      >
        <div class="flex justify-between items-start mb-3">
          <div>
            <h4 class="font-semibold">{{ conflict.type }}</h4>
            <p class="text-sm text-muted-foreground">{{ conflict.description }}</p>
          </div>
          <Badge :variant="getSeverityVariant(conflict.severity)">
            {{ conflict.severity }}
          </Badge>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <div>
            <div class="font-medium">Section 1</div>
            <div class="text-muted-foreground">{{ conflict.section1 }}</div>
          </div>
          <div>
            <div class="font-medium">Section 2</div>
            <div class="text-muted-foreground">{{ conflict.section2 }}</div>
          </div>
        </div>

        <div class="flex justify-end space-x-2 mt-4">
          <Button variant="outline" size="sm" @click="ignoreConflict(conflict)">
            Ignore
          </Button>
          <Button variant="outline" size="sm" @click="resolveConflict(conflict)">
            Resolve
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Badge } from '@/components/ui/Badge.vue'
import Button from '@/components/ui/Button.vue'
import { CheckCircle } from 'lucide-vue-next'

interface Props {
  course: any
  conflicts: any[]
}

interface Emits {
  (e: 'conflict-resolved', conflictId: number): void
}

defineProps<Props>()
defineEmits<Emits>()

// Methods
const getSeverityVariant = (severity: string) => {
  switch (severity) {
    case 'HIGH': return 'destructive'
    case 'MEDIUM': return 'default'
    case 'LOW': return 'secondary'
    default: return 'secondary'
  }
}

const ignoreConflict = (conflict: any) => {
  console.log('Ignoring conflict:', conflict.id)
}

const resolveConflict = (conflict: any) => {
  console.log('Resolving conflict:', conflict.id)
}
</script>