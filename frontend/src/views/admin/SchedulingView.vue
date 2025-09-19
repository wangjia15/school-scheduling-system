<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">Scheduling</h1>
          <p class="text-muted-foreground">
            Manage and generate course schedules
          </p>
        </div>
        <div class="flex items-center gap-2">
          <Button variant="outline">
            <Filter class="h-4 w-4 mr-2" />
            Filters
          </Button>
          <Button @click="generateSchedule">
            <Calendar class="h-4 w-4 mr-2" />
            Generate Schedule
          </Button>
        </div>
      </div>

      <!-- Weekly Calendar -->
      <Card>
        <CardHeader>
          <CardTitle>Weekly Schedule</CardTitle>
          <CardDescription>
            View and manage the weekly schedule calendar
          </CardDescription>
        </CardHeader>
        <CardContent>
          <WeeklyCalendar
            @slot-click="handleSlotClick"
            @schedule-click="handleScheduleClick"
          />
        </CardContent>
      </Card>

      <!-- Timeline View -->
      <Card>
        <CardHeader>
          <CardTitle>Schedule Timeline</CardTitle>
          <CardDescription>
            View schedules by teacher, classroom, or course
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ScheduleTimeline @schedule-click="handleScheduleClick" />
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
import { Calendar, Filter } from 'lucide-vue-next'
import WeeklyCalendar from '@/components/scheduling/WeeklyCalendar.vue'
import ScheduleTimeline from '@/components/scheduling/ScheduleTimeline.vue'

const generateSchedule = () => {
  console.log('Generate schedule clicked')
}

const handleSlotClick = (day: string, time: string) => {
  console.log('Slot clicked:', day, time)
}

const handleScheduleClick = (schedule: any) => {
  console.log('Schedule clicked:', schedule)
}
</script>