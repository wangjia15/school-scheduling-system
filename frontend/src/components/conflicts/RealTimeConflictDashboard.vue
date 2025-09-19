<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold">Real-Time Conflict Monitoring</h2>
        <p class="text-muted-foreground">
          Live monitoring of scheduling conflicts and system performance
        </p>
      </div>
      <div class="flex items-center gap-2">
        <div class="flex items-center gap-2 text-sm">
          <div class="w-2 h-2 rounded-full" :class="isConnected ? 'bg-green-500' : 'bg-red-500'"></div>
          <span :class="isConnected ? 'text-green-600' : 'text-red-600'">
            {{ isConnected ? 'Connected' : 'Disconnected' }}
          </span>
        </div>
        <Button variant="outline" size="sm" @click="refreshData">
          <RefreshCw class="h-4 w-4 mr-2" />
          Refresh
        </Button>
      </div>
    </div>

    <!-- Stats Overview -->
    <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <Card>
        <CardContent class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-muted-foreground">Total Conflicts</p>
              <p class="text-2xl font-bold">{{ conflictStats.totalConflicts }}</p>
            </div>
            <AlertTriangle class="h-8 w-8 text-yellow-500" />
          </div>
          <div class="mt-2 text-xs text-muted-foreground">
            {{ getConflictTrend('total') }}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-muted-foreground">Critical</p>
              <p class="text-2xl font-bold text-red-600">{{ conflictStats.criticalConflicts }}</p>
            </div>
            <XCircle class="h-8 w-8 text-red-500" />
          </div>
          <div class="mt-2 text-xs text-muted-foreground">
            {{ getConflictTrend('critical') }}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-muted-foreground">Resolved Today</p>
              <p class="text-2xl font-bold text-green-600">{{ conflictStats.resolvedToday }}</p>
            </div>
            <CheckCircle class="h-8 w-8 text-green-500" />
          </div>
          <div class="mt-2 text-xs text-muted-foreground">
            {{ conflictStats.resolutionRate }}% resolution rate
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-muted-foreground">Avg Resolution Time</p>
              <p class="text-2xl font-bold">{{ conflictStats.avgResolutionTime }}h</p>
            </div>
            <Clock class="h-8 w-8 text-blue-500" />
          </div>
          <div class="mt-2 text-xs text-muted-foreground">
            {{ getPerformanceStatus('resolution') }}
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Charts Row -->
    <div class="grid gap-6 lg:grid-cols-2">
      <!-- Conflict Trend Chart -->
      <Card>
        <CardHeader>
          <CardTitle class="text-lg">Conflict Trends</CardTitle>
          <CardDescription>24-hour conflict detection timeline</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="h-64">
            <canvas ref="trendChart"></canvas>
          </div>
        </CardContent>
      </Card>

      <!-- Severity Distribution -->
      <Card>
        <CardHeader>
          <CardTitle class="text-lg">Severity Distribution</CardTitle>
          <CardDescription>Current conflicts by severity level</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="h-64">
            <canvas ref="severityChart"></canvas>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- Recent Conflicts & Performance -->
    <div class="grid gap-6 lg:grid-cols-3">
      <!-- Recent Conflicts -->
      <Card class="lg:col-span-2">
        <CardHeader>
          <CardTitle class="text-lg">Recent Conflicts</CardTitle>
          <CardDescription>Latest detected conflicts requiring attention</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-3 max-h-96 overflow-y-auto">
            <div
              v-for="conflict in recentConflicts"
              :key="conflict.id"
              class="flex items-center justify-between p-3 border rounded-lg"
              :class="getConflictBorderClass(conflict.severity)"
            >
              <div class="flex-1">
                <div class="flex items-center gap-2">
                  <Badge :variant="getSeverityVariant(conflict.severity)">
                    {{ conflict.severity }}
                  </Badge>
                  <span class="font-medium">{{ conflict.type }}</span>
                </div>
                <p class="text-sm text-muted-foreground mt-1">{{ conflict.description }}</p>
                <p class="text-xs text-muted-foreground mt-1">
                  {{ formatTime(conflict.detectedAt) }} • {{ getEntityLabel(conflict) }}
                </p>
              </div>
              <div class="flex items-center gap-2">
                <Button
                  variant="ghost"
                  size="sm"
                  @click="viewConflict(conflict)"
                >
                  <Eye class="h-4 w-4" />
                </Button>
                <Button
                  v-if="conflict.status !== 'resolved'"
                  variant="ghost"
                  size="sm"
                  @click="resolveConflict(conflict)"
                >
                  <CheckCircle class="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Performance Metrics -->
      <Card>
        <CardHeader>
          <CardTitle class="text-lg">System Performance</CardTitle>
          <CardDescription>Real-time system metrics</CardDescription>
        </CardHeader>
        <CardContent class="space-y-4">
          <div class="space-y-2">
            <div class="flex items-center justify-between text-sm">
              <span>Detection Accuracy</span>
              <span class="font-medium">{{ performanceMetrics.detectionAccuracy }}%</span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-2">
              <div
                class="bg-green-500 h-2 rounded-full"
                :style="{ width: performanceMetrics.detectionAccuracy + '%' }"
              ></div>
            </div>
          </div>

          <div class="space-y-2">
            <div class="flex items-center justify-between text-sm">
              <span>System Load</span>
              <span class="font-medium">{{ performanceMetrics.systemLoad }}%</span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-2">
              <div
                class="bg-blue-500 h-2 rounded-full"
                :style="{ width: performanceMetrics.systemLoad + '%' }"
              ></div>
            </div>
          </div>

          <div class="space-y-2">
            <div class="flex items-center justify-between text-sm">
              <span>Response Time</span>
              <span class="font-medium">{{ performanceMetrics.responseTime }}ms</span>
            </div>
            <div class="w-full bg-gray-200 rounded-full h-2">
              <div
                class="bg-yellow-500 h-2 rounded-full"
                :style="{ width: Math.min(performanceMetrics.responseTime / 5, 100) + '%' }"
              ></div>
            </div>
          </div>

          <div class="pt-4 border-t">
            <div class="flex items-center justify-between text-sm">
              <span>Active Connections</span>
              <span class="font-medium">{{ performanceMetrics.activeConnections }}</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

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
                <Badge :variant="getSeverityVariant(selectedConflict.severity)">
                  {{ selectedConflict.severity }}
                </Badge>
              </div>
            </div>

            <div>
              <label class="text-sm font-medium text-muted-foreground">Description</label>
              <p class="text-sm">{{ selectedConflict.description }}</p>
            </div>

            <div>
              <label class="text-sm font-medium text-muted-foreground">Detected At</label>
              <p class="text-sm">{{ formatDateTime(selectedConflict.detectedAt) }}</p>
            </div>

            <div v-if="selectedConflict.resolutionNotes">
              <label class="text-sm font-medium text-muted-foreground">Resolution Notes</label>
              <p class="text-sm">{{ selectedConflict.resolutionNotes }}</p>
            </div>

            <div class="flex items-center justify-between pt-4 border-t">
              <Button variant="outline" @click="closeConflictDetails">
                Close
              </Button>
              <Button
                v-if="selectedConflict.status !== 'resolved'"
                @click="markAsResolved"
              >
                Mark as Resolved
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useToast } from 'vue-toastification'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import {
  AlertTriangle,
  XCircle,
  CheckCircle,
  Clock,
  RefreshCw,
  Eye,
  X
} from 'lucide-vue-next'
import { websocketService, type ConflictDetectedMessage, type ConflictStatsMessage } from '@/services/websocketService'

// Chart.js would need to be imported and configured
// import { Chart, registerables } from 'chart.js'
// Chart.register(...registerables)

const toast = useToast()

// State
const isConnected = ref(false)
const selectedConflict = ref<any>(null)
const recentConflicts = ref<any[]>([])
const conflictStats = ref({
  totalConflicts: 0,
  criticalConflicts: 0,
  highSeverityConflicts: 0,
  mediumSeverityConflicts: 0,
  lowSeverityConflicts: 0,
  resolvedToday: 0,
  resolutionRate: 0,
  avgResolutionTime: 0
})

const performanceMetrics = ref({
  detectionAccuracy: 95,
  systemLoad: 45,
  responseTime: 250,
  activeConnections: 0
})

// Chart references
const trendChart = ref<HTMLCanvasElement | null>(null)
const severityChart = ref<HTMLCanvasElement | null>(null)

// WebSocket handlers
const handleConflictDetected = (message: ConflictDetectedMessage) => {
  console.log('New conflict detected:', message)

  // Add to recent conflicts
  const conflict = {
    id: message.id,
    type: message.conflictType.replace(/_/g, ' '),
    severity: message.severity,
    description: message.description,
    detectedAt: message.detectedAt,
    status: message.resolutionStatus,
    entityType: message.entityType,
    entityId: message.entityId,
    requiresImmediateAttention: message.requiresImmediateAttention
  }

  recentConflicts.value.unshift(conflict)

  // Keep only last 10 conflicts
  if (recentConflicts.value.length > 10) {
    recentConflicts.value = recentConflicts.value.slice(0, 10)
  }

  // Update stats
  updateConflictStats()

  // Show notification for critical conflicts
  if (message.severity === 'CRITICAL' || message.requiresImmediateAttention) {
    toast.error(`Critical conflict detected: ${message.conflictType.replace(/_/g, ' ')}`)
  }
}

const handleConflictStats = (message: ConflictStatsMessage) => {
  console.log('Conflict stats updated:', message)
  updateStatsFromMessage(message)
}

const handleConnectionStatus = (status: boolean) => {
  isConnected.value = status
  if (!status) {
    toast.warning('WebSocket connection lost')
  } else {
    toast.success('WebSocket connection restored')
  }
}

// Helper functions
const updateConflictStats = () => {
  // This would typically fetch from API
  // For demo, we'll calculate from recent conflicts
  const total = recentConflicts.value.length
  const critical = recentConflicts.value.filter(c => c.severity === 'CRITICAL').length
  const high = recentConflicts.value.filter(c => c.severity === 'HIGH').length

  conflictStats.value = {
    ...conflictStats.value,
    totalConflicts: total,
    criticalConflicts: critical,
    highSeverityConflicts: high,
    resolvedToday: Math.floor(total * 0.3), // Simulated
    resolutionRate: total > 0 ? Math.round((total * 0.3) / total * 100) : 0,
    avgResolutionTime: 2.5
  }
}

const updateStatsFromMessage = (message: ConflictStatsMessage) => {
  conflictStats.value = {
    totalConflicts: message.totalConflicts,
    criticalConflicts: message.criticalConflicts,
    highSeverityConflicts: message.highSeverityConflicts,
    mediumSeverityConflicts: message.mediumSeverityConflicts,
    lowSeverityConflicts: message.lowSeverityConflicts,
    resolvedToday: Math.floor(message.totalConflicts * 0.3),
    resolutionRate: message.totalConflicts > 0 ?
      Math.round((message.totalConflicts * 0.3) / message.totalConflicts * 100) : 0,
    avgResolutionTime: 2.5
  }
}

const getConflictTrend = (type: string) => {
  // Simulated trend data
  if (type === 'total') {
    return conflictStats.value.totalConflicts > 10 ? '↑ 15% from yesterday' : '↓ 5% from yesterday'
  } else if (type === 'critical') {
    return conflictStats.value.criticalConflicts > 2 ? '↑ Requires attention' : 'Stable'
  }
  return 'No data'
}

const getPerformanceStatus = (metric: string) => {
  if (metric === 'resolution') {
    return conflictStats.value.avgResolutionTime < 3 ? 'Good' : 'Needs improvement'
  }
  return 'Unknown'
}

const getConflictBorderClass = (severity: string) => {
  switch (severity) {
    case 'CRITICAL':
      return 'border-red-200 bg-red-50'
    case 'HIGH':
      return 'border-orange-200 bg-orange-50'
    case 'MEDIUM':
      return 'border-yellow-200 bg-yellow-50'
    default:
      return 'border-gray-200'
  }
}

const getSeverityVariant = (severity: string) => {
  switch (severity) {
    case 'CRITICAL':
      return 'destructive'
    case 'HIGH':
      return 'default'
    case 'MEDIUM':
      return 'secondary'
    case 'LOW':
      return 'outline'
    default:
      return 'outline'
  }
}

const getEntityLabel = (conflict: any) => {
  if (conflict.entityType && conflict.entityId) {
    return `${conflict.entityType} #${conflict.entityId}`
  }
  return 'System'
}

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString()
}

const formatDateTime = (timestamp: string) => {
  const date = new Date(timestamp)
  return date.toLocaleString()
}

const viewConflict = (conflict: any) => {
  selectedConflict.value = conflict
}

const resolveConflict = (conflict: any) => {
  selectedConflict.value = conflict
}

const closeConflictDetails = () => {
  selectedConflict.value = null
}

const markAsResolved = () => {
  if (selectedConflict.value) {
    // This would call API to resolve conflict
    toast.success(`Conflict ${selectedConflict.value.id} marked as resolved`)

    // Update local state
    const index = recentConflicts.value.findIndex(c => c.id === selectedConflict.value.id)
    if (index !== -1) {
      recentConflicts.value[index].status = 'resolved'
    }

    closeConflictDetails()
    updateConflictStats()
  }
}

const refreshData = () => {
  // This would refresh data from API
  updateConflictStats()
  toast.info('Data refreshed')
}

// Initialize charts (placeholder - would need Chart.js)
const initializeCharts = () => {
  nextTick(() => {
    // Chart.js initialization would go here
    console.log('Charts would be initialized here')
  })
}

// Lifecycle hooks
onMounted(() => {
  // Subscribe to WebSocket events
  websocketService.subscribeToConflictDetected(handleConflictDetected)
  websocketService.subscribeToConflictStats(handleConflictStats)

  // Request initial data
  websocketService.requestConflictUpdates()

  // Set up connection status monitoring
  const checkConnection = () => {
    isConnected.value = websocketService.connected
  }

  setInterval(checkConnection, 5000)

  // Initialize with demo data
  updateConflictStats()
  initializeCharts()

  console.log('RealTimeConflictDashboard mounted')
})

onUnmounted(() => {
  // Cleanup would be handled by WebSocket service
  console.log('RealTimeConflictDashboard unmounted')
})
</script>