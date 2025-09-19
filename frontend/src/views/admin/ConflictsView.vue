<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">Conflict Resolution</h1>
          <p class="text-muted-foreground">
            Identify and resolve scheduling conflicts
          </p>
        </div>
        <div class="flex items-center gap-2">
          <Button variant="outline">
            <Filter class="h-4 w-4 mr-2" />
            Filter Conflicts
          </Button>
          <Button @click="resolveAllConflicts">
            <CheckCircle class="h-4 w-4 mr-2" />
            Resolve All
          </Button>
        </div>
      </div>

      <div class="grid gap-6 md:grid-cols-3">
        <Card>
          <CardHeader>
            <CardTitle>Active Conflicts</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-3xl font-bold text-red-600">{{ conflictStats.active }}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>In Progress</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-3xl font-bold text-yellow-600">{{ conflictStats.inProgress }}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Resolved Today</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-3xl font-bold text-green-600">{{ conflictStats.resolved }}</div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Real-Time Monitoring Dashboard</CardTitle>
          <CardDescription>
            Live conflict detection and system performance monitoring
          </CardDescription>
        </CardHeader>
        <CardContent>
          <RealTimeConflictDashboard />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Conflict Management</CardTitle>
          <CardDescription>
            Identify, analyze, and resolve scheduling conflicts
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ConflictList />
        </CardContent>
      </Card>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import { AlertTriangle, Filter, CheckCircle } from 'lucide-vue-next'
import ConflictList from '@/components/conflicts/ConflictList.vue'
import RealTimeConflictDashboard from '@/components/conflicts/RealTimeConflictDashboard.vue'

const conflictStats = ref({
  active: 12,
  inProgress: 5,
  resolved: 8
})

const resolveAllConflicts = () => {
  console.log('Resolve all conflicts clicked')
}
</script>