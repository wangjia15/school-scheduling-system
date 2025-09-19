<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">Classroom Management</h1>
          <p class="text-muted-foreground">
            Manage classroom inventory, capacity, and equipment
          </p>
        </div>
        <div class="flex items-center gap-2">
          <Button variant="outline">
            <Filter class="h-4 w-4 mr-2" />
            Filter
          </Button>
          <Button @click="addClassroom">
            <Plus class="h-4 w-4 mr-2" />
            Add Classroom
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Classroom Directory</CardTitle>
          <CardDescription>
            View and manage all classrooms in the system
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="text-center py-8">
            <DoorOpen class="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
            <h3 class="text-lg font-medium mb-2">Classroom Management Interface Coming Soon</h3>
            <p class="text-muted-foreground">
              Comprehensive classroom inventory management will be implemented here
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
import { DoorOpen, Filter, Plus } from 'lucide-vue-next'

const addClassroom = () => {
  console.log('Add classroom clicked')
}
</script>