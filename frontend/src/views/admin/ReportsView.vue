<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">Reports & Analytics</h1>
          <p class="text-muted-foreground">
            Generate reports and analyze scheduling data
          </p>
        </div>
        <div class="flex items-center gap-2">
          <Button variant="outline">
            <Filter class="h-4 w-4 mr-2" />
            Date Range
          </Button>
          <Button @click="generateReport">
            <BarChart3 class="h-4 w-4 mr-2" />
            Generate Report
          </Button>
        </div>
      </div>

      <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardHeader>
            <CardTitle>Schedule Utilization</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-center py-4">
              <BarChart3 class="h-12 w-12 mx-auto mb-2 text-muted-foreground" />
              <p class="text-sm text-muted-foreground">Charts coming soon</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Teacher Workload</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-center py-4">
              <Users class="h-12 w-12 mx-auto mb-2 text-muted-foreground" />
              <p class="text-sm text-muted-foreground">Analytics coming soon</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Room Usage</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-center py-4">
              <DoorOpen class="h-12 w-12 mx-auto mb-2 text-muted-foreground" />
              <p class="text-sm text-muted-foreground">Statistics coming soon</p>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Report Generation</CardTitle>
          <CardDescription>
            Create and download scheduling reports
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="text-center py-8">
            <FileText class="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
            <h3 class="text-lg font-medium mb-2">Report Generation Interface Coming Soon</h3>
            <p class="text-muted-foreground">
              Advanced reporting and analytics tools will be implemented here
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import AdminLayout from '@/components/layout/AdminLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import { BarChart3, Filter, Users, DoorOpen, FileText } from 'lucide-vue-next'

const generateReport = () => {
  console.log('Generate report clicked')
}
</script>