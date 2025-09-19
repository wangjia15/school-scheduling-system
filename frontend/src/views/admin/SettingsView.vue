<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">System Settings</h1>
          <p class="text-muted-foreground">
            Configure system preferences and parameters
          </p>
        </div>
        <div class="flex items-center gap-2">
          <Button variant="outline" @click="resetSettings">
            <RotateCcw class="h-4 w-4 mr-2" />
            Reset
          </Button>
          <Button @click="saveSettings">
            <Save class="h-4 w-4 mr-2" />
            Save Changes
          </Button>
        </div>
      </div>

      <div class="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>School Information</CardTitle>
            <CardDescription>
              Basic school and institution settings
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="text-center py-8">
              <Settings class="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
              <h3 class="text-lg font-medium mb-2">Settings Configuration Coming Soon</h3>
              <p class="text-muted-foreground">
                System configuration options will be implemented here
              </p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Scheduling Preferences</CardTitle>
            <CardDescription>
              Configure scheduling rules and constraints
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="text-center py-8">
              <Calendar class="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
              <h3 class="text-lg font-medium mb-2">Scheduling Settings Coming Soon</h3>
              <p class="text-muted-foreground">
                Advanced scheduling preferences will be implemented here
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
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
import { Settings, RotateCcw, Save, Calendar } from 'lucide-vue-next'

const resetSettings = () => {
  console.log('Reset settings clicked')
}

const saveSettings = () => {
  console.log('Save settings clicked')
}
</script>