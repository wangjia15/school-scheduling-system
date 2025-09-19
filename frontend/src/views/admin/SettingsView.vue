<template>
  <AdminLayout>
    <div class="space-y-6">
      <!-- Page Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-3xl font-bold tracking-tight">System Settings</h1>
          <p class="text-muted-foreground">
            Configure system preferences and parameters
          </p>
        </div>
        <div class="flex items-center gap-2">
          <Button variant="outline" @click="resetSettings" :disabled="loading">
            <RotateCcw class="h-4 w-4 mr-2" />
            Reset to Defaults
          </Button>
          <Button @click="saveSettings" :disabled="loading || !hasChanges">
            <Save v-if="!loading" class="h-4 w-4 mr-2" />
            <Loader v-else class="h-4 w-4 mr-2 animate-spin" />
            Save Changes
          </Button>
        </div>
      </div>

      <!-- Settings Navigation Tabs -->
      <Card>
        <CardHeader>
          <div class="flex space-x-1 bg-muted p-1 rounded-lg">
            <Button
              variant="ghost"
              class="flex-1"
              :class="{ 'bg-background shadow-sm': activeTab === 'general' }"
              @click="activeTab = 'general'"
            >
              <Settings class="h-4 w-4 mr-2" />
              General
            </Button>
            <Button
              variant="ghost"
              class="flex-1"
              :class="{ 'bg-background shadow-sm': activeTab === 'scheduling' }"
              @click="activeTab = 'scheduling'"
            >
              <Calendar class="h-4 w-4 mr-2" />
              Scheduling
            </Button>
            <Button
              variant="ghost"
              class="flex-1"
              :class="{ 'bg-background shadow-sm': activeTab === 'notifications' }"
              @click="activeTab = 'notifications'"
            >
              <Bell class="h-4 w-4 mr-2" />
              Notifications
            </Button>
            <Button
              variant="ghost"
              class="flex-1"
              :class="{ 'bg-background shadow-sm': activeTab === 'advanced' }"
              @click="activeTab = 'advanced'"
            >
              <Shield class="h-4 w-4 mr-2" />
              Advanced
            </Button>
          </div>
        </CardHeader>
      </Card>

      <!-- General Settings -->
      <div v-if="activeTab === 'general'" class="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>School Information</CardTitle>
            <CardDescription>
              Basic school and institution settings
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="grid gap-4 md:grid-cols-2">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">School Name</label>
                <Input v-model="settings.general.schoolName" placeholder="Enter school name" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Academic Year</label>
                <Select v-model="settings.general.currentAcademicYear">
                  <option value="2024-2025">2024-2025</option>
                  <option value="2023-2024">2023-2024</option>
                  <option value="2022-2023">2022-2023</option>
                </Select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Current Semester</label>
                <Select v-model="settings.general.currentSemester">
                  <option value="FALL">Fall</option>
                  <option value="SPRING">Spring</option>
                  <option value="SUMMER">Summer</option>
                  <option value="WINTER">Winter</option>
                </Select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Time Zone</label>
                <Select v-model="settings.general.timeZone">
                  <option value="America/New_York">Eastern Time</option>
                  <option value="America/Chicago">Central Time</option>
                  <option value="America/Denver">Mountain Time</option>
                  <option value="America/Los_Angeles">Pacific Time</option>
                </Select>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Working Hours</CardTitle>
            <CardDescription>
              Set standard working hours for the institution
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="grid gap-4 md:grid-cols-2">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Week Start Day</label>
                <Select v-model="settings.general.weekStartDay">
                  <option value="MONDAY">Monday</option>
                  <option value="SUNDAY">Sunday</option>
                  <option value="SATURDAY">Saturday</option>
                </Select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Standard Day Start</label>
                <Input type="time" v-model="settings.general.dayStartTime" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Standard Day End</label>
                <Input type="time" v-model="settings.general.dayEndTime" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Class Duration (minutes)</label>
                <Input type="number" v-model.number="settings.general.classDuration" min="15" max="240" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- Scheduling Settings -->
      <div v-if="activeTab === 'scheduling'" class="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Scheduling Rules</CardTitle>
            <CardDescription>
              Configure scheduling constraints and preferences
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="space-y-4">
              <div class="grid gap-4 md:grid-cols-2">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Max Classes per Day</label>
                  <Input type="number" v-model.number="settings.scheduling.maxClassesPerDay" min="1" max="12" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Min Break Between Classes (min)</label>
                  <Input type="number" v-model.number="settings.scheduling.minBreakBetweenClasses" min="0" max="120" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Max Weekly Hours per Teacher</label>
                  <Input type="number" v-model.number="settings.scheduling.maxWeeklyHoursPerTeacher" min="1" max="80" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Max Courses per Teacher</label>
                  <Input type="number" v-model.number="settings.scheduling.maxCoursesPerTeacher" min="1" max="10" />
                </div>
              </div>

              <div class="space-y-3">
                <div class="flex items-center">
                  <input
                    v-model="settings.scheduling.allowDoubleBookings"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label class="ml-2 block text-sm text-gray-700">Allow teacher double bookings</label>
                </div>
                <div class="flex items-center">
                  <input
                    v-model="settings.scheduling.enforcePrerequisites"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label class="ml-2 block text-sm text-gray-700">Enforce course prerequisites</label>
                </div>
                <div class="flex items-center">
                  <input
                    v-model="settings.scheduling.preventRoomConflicts"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label class="ml-2 block text-sm text-gray-700">Prevent room conflicts</label>
                </div>
                <div class="flex items-center">
                  <input
                    v-model="settings.scheduling.autoResolveConflicts"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label class="ml-2 block text-sm text-gray-700">Auto-resolve minor conflicts</label>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Classroom Preferences</CardTitle>
            <CardDescription>
              Set classroom allocation preferences
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="grid gap-4 md:grid-cols-2">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Default Room Capacity</label>
                <Input type="number" v-model.number="settings.scheduling.defaultRoomCapacity" min="10" max="500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Min Room Utilization %</label>
                <Input type="number" v-model.number="settings.scheduling.minRoomUtilization" min="50" max="100" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Preferred Room Type</label>
                <Select v-model="settings.scheduling.preferredRoomType">
                  <option value="LECTURE_HALL">Lecture Hall</option>
                  <option value="CLASSROOM">Classroom</option>
                  <option value="LABORATORY">Laboratory</option>
                  <option value="SEMINAR_ROOM">Seminar Room</option>
                </Select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Room Assignment Priority</label>
                <Select v-model="settings.scheduling.roomAssignmentPriority">
                  <option value="CAPACITY">Capacity First</option>
                  <option value="EQUIPMENT">Equipment First</option>
                  <option value="LOCATION">Location First</option>
                  <option value="BALANCED">Balanced</option>
                </Select>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- Notification Settings -->
      <div v-if="activeTab === 'notifications'" class="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Email Notifications</CardTitle>
            <CardDescription>
              Configure email notification preferences
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="space-y-4">
              <div class="space-y-3">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium">Schedule Changes</p>
                    <p class="text-xs text-muted-foreground">Notify when schedules are modified</p>
                  </div>
                  <input
                    v-model="settings.notifications.emailScheduleChanges"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                </div>
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium">Conflict Alerts</p>
                    <p class="text-xs text-muted-foreground">Notify when conflicts occur</p>
                  </div>
                  <input
                    v-model="settings.notifications.emailConflictAlerts"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                </div>
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium">Daily Summary</p>
                    <p class="text-xs text-muted-foreground">Send daily schedule summary</p>
                  </div>
                  <input
                    v-model="settings.notifications.emailDailySummary"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                </div>
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium">Weekly Reports</p>
                    <p class="text-xs text-muted-foreground">Send weekly utilization reports</p>
                  </div>
                  <input
                    v-model="settings.notifications.emailWeeklyReports"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>In-App Notifications</CardTitle>
            <CardDescription>
              Configure in-app notification settings
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="space-y-4">
              <div class="space-y-3">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium">Real-time Updates</p>
                    <p class="text-xs text-muted-foreground">Show real-time schedule updates</p>
                  </div>
                  <input
                    v-model="settings.notifications.realTimeUpdates"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                </div>
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium">System Alerts</p>
                    <p class="text-xs text-muted-foreground">Show system maintenance alerts</p>
                  </div>
                  <input
                    v-model="settings.notifications.systemAlerts"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                </div>
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium">Achievement Notifications</p>
                    <p class="text-xs text-muted-foreground">Notify about scheduling achievements</p>
                  </div>
                  <input
                    v-model="settings.notifications.achievementNotifications"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- Advanced Settings -->
      <div v-if="activeTab === 'advanced'" class="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>System Configuration</CardTitle>
            <CardDescription>
              Advanced system configuration options
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="space-y-4">
              <div class="grid gap-4 md:grid-cols-2">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Session Timeout (minutes)</label>
                  <Input type="number" v-model.number="settings.advanced.sessionTimeout" min="5" max="480" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Max File Upload Size (MB)</label>
                  <Input type="number" v-model.number="settings.advanced.maxFileSize" min="1" max="100" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Log Retention Days</label>
                  <Input type="number" v-model.number="settings.advanced.logRetention" min="7" max="365" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">API Rate Limit (requests/min)</label>
                  <Input type="number" v-model.number="settings.advanced.apiRateLimit" min="10" max="1000" />
                </div>
              </div>

              <div class="space-y-3">
                <div class="flex items-center">
                  <input
                    v-model="settings.advanced.enableDebugMode"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label class="ml-2 block text-sm text-gray-700">Enable debug mode</label>
                </div>
                <div class="flex items-center">
                  <input
                    v-model="settings.advanced.enableAuditLogging"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label class="ml-2 block text-sm text-gray-700">Enable audit logging</label>
                </div>
                <div class="flex items-center">
                  <input
                    v-model="settings.advanced.enableAnalytics"
                    type="checkbox"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label class="ml-2 block text-sm text-gray-700">Enable usage analytics</label>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Integration Settings</CardTitle>
            <CardDescription>
              Configure third-party integrations
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="space-y-4">
              <div class="space-y-3">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium">Calendar Integration</p>
                    <p class="text-xs text-muted-foreground">Sync with external calendars</p>
                  </div>
                  <Button variant="outline" size="sm">Configure</Button>
                </div>
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium">Email Service</p>
                    <p class="text-xs text-muted-foreground">Configure email delivery settings</p>
                  </div>
                  <Button variant="outline" size="sm">Configure</Button>
                </div>
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-sm font-medium">File Storage</p>
                    <p class="text-xs text-muted-foreground">Configure file storage provider</p>
                  </div>
                  <Button variant="outline" size="sm">Configure</Button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- Save Status -->
      <Card v-if="hasChanges">
        <CardContent class="pt-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-orange-600">Unsaved Changes</p>
              <p class="text-xs text-muted-foreground">You have unsaved changes that will be lost if you navigate away</p>
            </div>
            <div class="flex items-center gap-2">
              <Button variant="outline" @click="discardChanges">Discard Changes</Button>
              <Button @click="saveSettings" :disabled="loading">
                <Save v-if="!loading" class="h-4 w-4 mr-2" />
                <Loader v-else class="h-4 w-4 mr-2 animate-spin" />
                Save Settings
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useToast } from 'vue-toastification'
import AdminLayout from '@/components/layout/AdminLayout.vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'
import {
  Settings,
  RotateCcw,
  Save,
  Calendar,
  Bell,
  Shield,
  Loader,
  Clock
} from 'lucide-vue-next'

const toast = useToast()

// State
const loading = ref(false)
const activeTab = ref('general')
const originalSettings = ref<any>(null)

// Settings data
const settings = ref({
  general: {
    schoolName: '',
    currentAcademicYear: '2024-2025',
    currentSemester: 'FALL',
    timeZone: 'America/New_York',
    weekStartDay: 'MONDAY',
    dayStartTime: '08:00',
    dayEndTime: '17:00',
    classDuration: 60
  },
  scheduling: {
    maxClassesPerDay: 6,
    minBreakBetweenClasses: 15,
    maxWeeklyHoursPerTeacher: 40,
    maxCoursesPerTeacher: 5,
    allowDoubleBookings: false,
    enforcePrerequisites: true,
    preventRoomConflicts: true,
    autoResolveConflicts: false,
    defaultRoomCapacity: 30,
    minRoomUtilization: 70,
    preferredRoomType: 'CLASSROOM',
    roomAssignmentPriority: 'BALANCED'
  },
  notifications: {
    emailScheduleChanges: true,
    emailConflictAlerts: true,
    emailDailySummary: false,
    emailWeeklyReports: true,
    realTimeUpdates: true,
    systemAlerts: true,
    achievementNotifications: true
  },
  advanced: {
    sessionTimeout: 30,
    maxFileSize: 10,
    logRetention: 30,
    apiRateLimit: 100,
    enableDebugMode: false,
    enableAuditLogging: true,
    enableAnalytics: true
  }
})

// Computed
const hasChanges = computed(() => {
  if (!originalSettings.value) return false
  return JSON.stringify(settings.value) !== JSON.stringify(originalSettings.value)
})

// Load settings
const loadSettings = async () => {
  loading.value = true
  try {
    // Simulate API call to load settings
    await new Promise(resolve => setTimeout(resolve, 1000))

    // Mock settings data
    const mockSettings = {
      ...settings.value,
      general: {
        ...settings.value.general,
        schoolName: 'University of Technology',
        classDuration: 50
      }
    }

    settings.value = mockSettings
    originalSettings.value = JSON.parse(JSON.stringify(mockSettings))
  } catch (error) {
    console.error('Failed to load settings:', error)
    toast.error('Failed to load settings')
  } finally {
    loading.value = false
  }
}

// Save settings
const saveSettings = async () => {
  loading.value = true
  try {
    // Simulate API call to save settings
    await new Promise(resolve => setTimeout(resolve, 2000))

    // Update original settings
    originalSettings.value = JSON.parse(JSON.stringify(settings.value))

    toast.success('Settings saved successfully')
  } catch (error) {
    console.error('Failed to save settings:', error)
    toast.error('Failed to save settings')
  } finally {
    loading.value = false
  }
}

// Reset settings
const resetSettings = async () => {
  if (!confirm('Are you sure you want to reset all settings to defaults?')) {
    return
  }

  loading.value = true
  try {
    // Reset to default values
    const defaultSettings = {
      general: {
        schoolName: '',
        currentAcademicYear: '2024-2025',
        currentSemester: 'FALL',
        timeZone: 'America/New_York',
        weekStartDay: 'MONDAY',
        dayStartTime: '08:00',
        dayEndTime: '17:00',
        classDuration: 60
      },
      scheduling: {
        maxClassesPerDay: 6,
        minBreakBetweenClasses: 15,
        maxWeeklyHoursPerTeacher: 40,
        maxCoursesPerTeacher: 5,
        allowDoubleBookings: false,
        enforcePrerequisites: true,
        preventRoomConflicts: true,
        autoResolveConflicts: false,
        defaultRoomCapacity: 30,
        minRoomUtilization: 70,
        preferredRoomType: 'CLASSROOM',
        roomAssignmentPriority: 'BALANCED'
      },
      notifications: {
        emailScheduleChanges: true,
        emailConflictAlerts: true,
        emailDailySummary: false,
        emailWeeklyReports: true,
        realTimeUpdates: true,
        systemAlerts: true,
        achievementNotifications: true
      },
      advanced: {
        sessionTimeout: 30,
        maxFileSize: 10,
        logRetention: 30,
        apiRateLimit: 100,
        enableDebugMode: false,
        enableAuditLogging: true,
        enableAnalytics: true
      }
    }

    settings.value = defaultSettings
    originalSettings.value = JSON.parse(JSON.stringify(defaultSettings))

    toast.success('Settings reset to defaults')
  } catch (error) {
    console.error('Failed to reset settings:', error)
    toast.error('Failed to reset settings')
  } finally {
    loading.value = false
  }
}

// Discard changes
const discardChanges = () => {
  if (!confirm('Are you sure you want to discard all unsaved changes?')) {
    return
  }

  if (originalSettings.value) {
    settings.value = JSON.parse(JSON.stringify(originalSettings.value))
    toast.info('Changes discarded')
  }
}

// Warn before leaving with unsaved changes
const beforeUnload = (event: BeforeUnloadEvent) => {
  if (hasChanges.value) {
    event.preventDefault()
    event.returnValue = ''
  }
}

onMounted(() => {
  loadSettings()
  window.addEventListener('beforeunload', beforeUnload)
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnload)
})
</script>