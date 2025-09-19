<template>
  <div class="space-y-4">
    <!-- Filter and Search -->
    <div class="flex items-center justify-between gap-4">
      <div class="flex items-center gap-2 flex-1">
        <div class="relative flex-1 max-w-sm">
          <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            v-model="searchQuery"
            placeholder="Search conflicts..."
            class="pl-10"
          />
        </div>
        <Select v-model="severityFilter" class="w-40">
          <option value="">All Severities</option>
          <option value="high">High</option>
          <option value="medium">Medium</option>
          <option value="low">Low</option>
        </Select>
        <Select v-model="statusFilter" class="w-40">
          <option value="">All Status</option>
          <option value="open">Open</option>
          <option value="in-progress">In Progress</option>
          <option value="resolved">Resolved</option>
        </Select>
      </div>
      <div class="flex items-center gap-2">
        <Button variant="outline" size="sm" @click="refreshConflicts">
          <RefreshCw class="h-4 w-4 mr-2" />
          Refresh
        </Button>
        <Button size="sm" @click="resolveSelected" :disabled="!selectedConflicts.length">
          <CheckCircle class="h-4 w-4 mr-2" />
          Resolve Selected ({{ selectedConflicts.length }})
        </Button>
      </div>
    </div>

    <!-- Conflicts Table -->
    <Card>
      <CardContent class="p-0">
        <Table
          :headers="tableHeaders"
          :data="filteredConflicts"
          striped
          hoverable
        >
          <template #cell-select="{ row }">
            <input
              type="checkbox"
              :checked="selectedConflicts.includes(row.id)"
              @change="toggleConflictSelection(row.id)"
              class="rounded border-gray-300"
            />
          </template>
          <template #cell-severity="{ row }">
            <Badge :variant="severityVariant(row.severity)">
              {{ row.severity }}
            </Badge>
          </template>
          <template #cell-status="{ row }">
            <Badge :variant="statusVariant(row.status)">
              {{ row.status }}
            </Badge>
          </template>
          <template #cell-actions="{ row }">
            <div class="flex items-center gap-2">
              <Button variant="ghost" size="sm" @click="viewConflict(row)">
                <Eye class="h-4 w-4" />
              </Button>
              <Button
                v-if="row.status !== 'resolved'"
                variant="ghost"
                size="sm"
                @click="resolveConflict(row)"
              >
                <CheckCircle class="h-4 w-4" />
              </Button>
            </div>
          </template>
        </Table>
      </CardContent>
    </Card>

    <!-- Conflict Details Modal -->
    <div v-if="selectedConflict" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <Card class="w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <CardHeader>
          <div class="flex items-center justify-between">
            <CardTitle>Conflict Details</CardTitle>
            <Button variant="ghost" size="icon" @click="closeConflictDetails">
              <X class="h-4 w-4" />
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-sm font-medium text-muted-foreground">Type</label>
                <p class="font-medium">{{ selectedConflict.type }}</p>
              </div>
              <div>
                <label class="text-sm font-medium text-muted-foreground">Severity</label>
                <Badge :variant="severityVariant(selectedConflict.severity)">
                  {{ selectedConflict.severity }}
                </Badge>
              </div>
            </div>

            <div>
              <label class="text-sm font-medium text-muted-foreground">Description</label>
              <p class="text-sm">{{ selectedConflict.description }}</p>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-sm font-medium text-muted-foreground">Affected Teacher</label>
                <p class="font-medium">{{ selectedConflict.teacher }}</p>
              </div>
              <div>
                <label class="text-sm font-medium text-muted-foreground">Time Slot</label>
                <p class="font-medium">{{ selectedConflict.time }}</p>
              </div>
            </div>

            <div>
              <label class="text-sm font-medium text-muted-foreground">Suggested Resolution</label>
              <p class="text-sm">{{ selectedConflict.resolution }}</p>
            </div>

            <div class="flex items-center justify-between pt-4 border-t">
              <div>
                <label class="text-sm font-medium text-muted-foreground">Status</label>
                <Select v-model="selectedConflict.status" class="w-32">
                  <option value="open">Open</option>
                  <option value="in-progress">In Progress</option>
                  <option value="resolved">Resolved</option>
                </Select>
              </div>
              <div class="flex items-center gap-2">
                <Button variant="outline" @click="closeConflictDetails">
                  Cancel
                </Button>
                <Button @click="saveConflictResolution">
                  Save Changes
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Table from '@/components/ui/Table.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import Badge from '@/components/ui/Badge.vue'
import { useToast } from 'vue-toastification'
import {
  Search,
  RefreshCw,
  CheckCircle,
  Eye,
  X
} from 'lucide-vue-next'
import { websocketService, type ConflictDetectedMessage, type ConflictStatsMessage } from '@/services/websocketService'

interface Conflict {
  id: string
  type: string
  severity: 'high' | 'medium' | 'low'
  status: 'open' | 'in-progress' | 'resolved'
  description: string
  teacher: string
  time: string
  resolution: string
}

const toast = useToast()
const searchQuery = ref('')
const severityFilter = ref('')
const statusFilter = ref('')
const selectedConflicts = ref<string[]>([])
const selectedConflict = ref<Conflict | null>(null)
const isLoading = ref(false)
const realTimeUpdates = ref(true)

const tableHeaders = [
  { key: 'select', label: '', width: '50px' },
  { key: 'type', label: 'Type' },
  { key: 'severity', label: 'Severity' },
  { key: 'status', label: 'Status' },
  { key: 'teacher', label: 'Teacher' },
  { key: 'time', label: 'Time' },
  { key: 'actions', label: 'Actions', width: '100px' }
]

const conflicts = ref<Conflict[]>([
  {
    id: '1',
    type: 'Teacher Double Booking',
    severity: 'high',
    status: 'open',
    description: 'Dr. Smith is scheduled to teach both CS101 and CS201 at the same time on Monday 10:00 AM',
    teacher: 'Dr. Smith',
    time: 'Monday 10:00 AM - 11:00 AM',
    resolution: 'Reschedule CS201 to Tuesday 10:00 AM or assign a different teacher'
  },
  {
    id: '2',
    type: 'Room Capacity Exceeded',
    severity: 'medium',
    status: 'in-progress',
    description: 'CS101 has 45 students enrolled but Room 101 only has capacity for 30',
    teacher: 'Dr. Smith',
    time: 'Monday 9:00 AM - 10:00 AM',
    resolution: 'Move to larger classroom (Room 301) or split into two sections'
  },
  {
    id: '3',
    type: 'Prerequisite Conflict',
    severity: 'low',
    status: 'open',
    description: 'Student John Doe is enrolled in CS201 without completing CS101',
    teacher: 'Prof. Johnson',
    time: 'Tuesday 2:00 PM - 3:00 PM',
    resolution: 'Remove student from course or ensure prerequisite completion'
  }
])

const filteredConflicts = computed(() => {
  return conflicts.value.filter(conflict => {
    const matchesSearch = !searchQuery.value ||
      conflict.type.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      conflict.teacher.toLowerCase().includes(searchQuery.value.toLowerCase())

    const matchesSeverity = !severityFilter.value || conflict.severity === severityFilter.value
    const matchesStatus = !statusFilter.value || conflict.status === statusFilter.value

    return matchesSearch && matchesSeverity && matchesStatus
  })
})

const toggleConflictSelection = (conflictId: string) => {
  const index = selectedConflicts.value.indexOf(conflictId)
  if (index > -1) {
    selectedConflicts.value.splice(index, 1)
  } else {
    selectedConflicts.value.push(conflictId)
  }
}

const refreshConflicts = () => {
  console.log('Refreshing conflicts...')
}

const resolveSelected = () => {
  console.log('Resolving selected conflicts:', selectedConflicts.value)
  selectedConflicts.value = []
}

const viewConflict = (conflict: Conflict) => {
  selectedConflict.value = { ...conflict }
}

const resolveConflict = (conflict: Conflict) => {
  conflict.status = 'resolved'
  console.log('Conflict resolved:', conflict.id)
}

const closeConflictDetails = () => {
  selectedConflict.value = null
}

const saveConflictResolution = () => {
  if (selectedConflict.value) {
    const index = conflicts.value.findIndex(c => c.id === selectedConflict.value!.id)
    if (index > -1) {
      conflicts.value[index] = { ...selectedConflict.value }
    }
    closeConflictDetails()
  }
}

const severityVariant = (severity: string) => {
  switch (severity) {
    case 'high':
      return 'destructive'
    case 'medium':
      return 'secondary'
    case 'low':
      return 'outline'
    default:
      return 'outline'
  }
}

const statusVariant = (status: string) => {
  switch (status) {
    case 'open':
      return 'destructive'
    case 'in-progress':
      return 'secondary'
    case 'resolved':
      return 'default'
    default:
      return 'outline'
  }
}
</script>