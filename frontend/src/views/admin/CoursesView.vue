<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">Course Management</h1>
          <p class="text-muted-foreground">
            Manage course catalog, prerequisites, and requirements
          </p>
        </div>
        <div class="flex items-center gap-2">
          <Button variant="outline">
            <Filter class="h-4 w-4 mr-2" />
            Filter
          </Button>
          <Button @click="addCourse">
            <Plus class="h-4 w-4 mr-2" />
            Add Course
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Course Catalog</CardTitle>
          <CardDescription>
            View and manage all courses in the system
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="text-center py-8">
            <BookOpen class="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
            <h3 class="text-lg font-medium mb-2">Course Management Interface Coming Soon</h3>
            <p class="text-muted-foreground">
              Comprehensive course catalog management will be implemented here
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
import { BookOpen, Filter, Plus } from 'lucide-vue-next'

const addCourse = () => {
  console.log('Add course clicked')
}
</script>