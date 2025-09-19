<template>
  <div v-if="open" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
    <Card class="w-full max-w-5xl max-h-[90vh] overflow-y-auto">
      <CardHeader>
        <div class="flex items-center justify-between">
          <CardTitle>Workload Optimization</CardTitle>
          <Button variant="ghost" size="icon" @click="$emit('update:open', false)">
            <X class="h-4 w-4" />
          </Button>
        </div>
        <CardDescription>
          AI-powered workload optimization for {{ teacher.user.firstName }} {{ teacher.user.lastName }}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <!-- Current Status -->
        <div class="mb-6 p-4 bg-blue-50 rounded-lg">
          <h4 class="font-semibold text-blue-900 mb-3">Current Workload Status</h4>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <span class="text-sm font-medium text-blue-700">Current Load:</span>
              <div class="text-blue-900 font-medium">{{ teacher.currentWorkload }}h / {{ teacher.maxWeeklyHours }}h</div>
            </div>
            <div>
              <span class="text-sm font-medium text-blue-700">Utilization:</span>
              <div class="text-blue-900 font-medium">{{ utilizationPercentage }}%</div>
            </div>
            <div>
              <span class="text-sm font-medium text-blue-700">Courses:</span>
              <div class="text-blue-900 font-medium">{{ teacher.assignedCourses?.length || 0 }}</div>
            </div>
            <div>
              <span class="text-sm font-medium text-blue-700">Conflicts:</span>
              <div class="text-blue-900 font-medium">{{ conflictsCount }}</div>
            </div>
          </div>
        </div>

        <!-- Optimization Strategy -->
        <div class="mb-6">
          <h4 class="text-lg font-medium mb-4">Optimization Strategy</h4>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Optimization Goal</label>
              <Select v-model="optimizationGoal">
                <option value="balance">Balance Workload</option>
                <option value="reduce">Reduce Workload</option>
                <option value="increase">Increase Workload</option>
                <option value="minimize_conflicts">Minimize Conflicts</option>
              </Select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Priority Level</label>
              <Select v-model="priorityLevel">
                <option value="high">High Priority</option>
                <option value="medium">Medium Priority</option>
                <option value="low">Low Priority</option>
              </Select>
            </div>
          </div>
        </div>

        <!-- Optimization Suggestions -->
        <div class="mb-6">
          <div class="flex items-center justify-between mb-4">
            <h4 class="text-lg font-medium">AI Optimization Suggestions</h4>
            <Button @click="generateOptimizations" :disabled="loading">
              <RefreshCw v-if="!loading" class="h-4 w-4 mr-2" />
              <Loader v-else class="h-4 w-4 mr-2 animate-spin" />
              Generate Suggestions
            </Button>
          </div>

          <div v-if="optimizations.length === 0 && !loading" class="text-center py-8 text-gray-500">
            Click "Generate Suggestions" to analyze workload and provide optimization recommendations
          </div>

          <div v-else class="space-y-4">
            <div
              v-for="optimization in optimizations"
              :key="optimization.id"
              class="border rounded-lg p-4"
              :class="getOptimizationClass(optimization.type)"
            >
              <div class="flex items-start justify-between mb-3">
                <div class="flex items-start gap-3">
                  <div :class="getOptimizationIconClass(optimization.type)">
                    <component :is="getOptimizationIcon(optimization.type)" class="h-5 w-5" />
                  </div>
                  <div class="flex-1">
                    <div class="flex items-center gap-2 mb-1">
                      <h5 class="font-medium">{{ optimization.title }}</h5>
                      <Badge :variant="getOptimizationVariant(optimization.priority)" class="text-xs">
                        {{ optimization.priority }}
                      </Badge>
                      <Badge variant="outline" class="text-xs">
                        {{ optimization.impact }} Impact
                      </Badge>
                    </div>
                    <p class="text-sm text-gray-600 mb-3">{{ optimization.description }}</p>

                    <!-- Optimization Details -->
                    <div class="space-y-2">
                      <div class="text-sm">
                        <span class="font-medium">Current:</span> {{ optimization.current }}
                      </div>
                      <div class="text-sm">
                        <span class="font-medium">Proposed:</span> {{ optimization.proposed }}
                      </div>
                      <div class="text-sm">
                        <span class="font-medium">Benefit:</span> {{ optimization.benefit }}
                      </div>
                    </div>

                    <!-- Affected Courses -->
                    <div v-if="optimization.affectedCourses.length > 0" class="mt-3">
                      <h6 class="text-sm font-medium text-gray-700 mb-2">Affected Courses:</h6>
                      <div class="space-y-1">
                        <div
                          v-for="course in optimization.affectedCourses"
                          :key="course.id"
                          class="text-sm text-gray-600 bg-gray-50 px-2 py-1 rounded"
                        >
                          {{ course.courseCode }} - {{ course.courseName }}
                        </div>
                      </div>
                    </div>

                    <!-- Implementation Steps -->
                    <div class="mt-3">
                      <h6 class="text-sm font-medium text-gray-700 mb-2">Implementation Steps:</h6>
                      <ol class="text-sm text-gray-600 space-y-1">
                        <li v-for="(step, index) in optimization.steps" :key="index" class="flex items-start gap-2">
                          <span class="font-medium">{{ index + 1 }}.</span>
                          <span>{{ step }}</span>
                        </li>
                      </ol>
                    </div>

                    <!-- Action Buttons -->
                    <div class="flex items-center gap-2 mt-4">
                      <Button
                        v-if="optimization.canApply"
                        variant="primary"
                        size="sm"
                        @click="applyOptimization(optimization)"
                        :disabled="applying"
                      >
                        <Check class="h-4 w-4 mr-2" />
                        Apply
                      </Button>
                      <Button variant="outline" size="sm" @click="viewOptimizationDetails(optimization)">
                        <Eye class="h-4 w-4 mr-2" />
                        View Details
                      </Button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Implementation Preview -->
        <div v-if="selectedOptimization" class="mb-6 p-4 bg-yellow-50 rounded-lg border border-yellow-200">
          <h4 class="font-semibold text-yellow-900 mb-3">Implementation Preview</h4>
          <div class="space-y-3">
            <div class="text-sm">
              <span class="font-medium text-yellow-700">Action:</span>
              <span class="text-yellow-900 ml-1">{{ selectedOptimization.title }}</span>
            </div>
            <div class="text-sm">
              <span class="font-medium text-yellow-700">Expected Result:</span>
              <span class="text-yellow-900 ml-1">{{ selectedOptimization.expectedResult }}</span>
            </div>
            <div class="text-sm">
              <span class="font-medium text-yellow-700">Risk Assessment:</span>
              <span class="text-yellow-900 ml-1">{{ selectedOptimization.riskLevel }}</span>
            </div>
          </div>
        </div>

        <!-- Form Actions -->
        <div class="flex items-center justify-between pt-4 border-t">
          <div class="text-sm text-gray-600">
            {{ optimizations.length }} optimization suggestions generated
          </div>
          <div class="flex items-center gap-3">
            <Button variant="outline" @click="$emit('update:open', false)">
              Cancel
            </Button>
            <Button
              v-if="hasApplicableOptimizations"
              @click="applyAllOptimizations"
              :disabled="applying"
            >
              <Zap class="h-4 w-4 mr-2" />
              Apply All
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Select from '@/components/ui/Select.vue'
import { X, RefreshCw, Loader, Check, Eye, Zap, TrendingUp, AlertTriangle, Target, Users } from 'lucide-vue-next'
import type { Teacher } from '@/services/teacherService'
import assignmentService from '@/services/assignmentService'

interface Props {
  open: boolean
  teacher: Teacher
}

interface Emits {
  'update:open': [value: boolean]
  'optimized': []
}

interface Optimization {
  id: string
  type: string
  title: string
  description: string
  priority: 'low' | 'medium' | 'high'
  impact: 'low' | 'medium' | 'high'
  current: string
  proposed: string
  benefit: string
  affectedCourses: any[]
  steps: string[]
  canApply: boolean
  expectedResult: string
  riskLevel: string
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const applying = ref(false)
const optimizationGoal = ref('balance')
const priorityLevel = ref('medium')
const optimizations = ref<Optimization[]>([])
const selectedOptimization = ref<Optimization | null>(null)

// Computed
const utilizationPercentage = computed(() => {
  return Math.round((props.teacher.currentWorkload / props.teacher.maxWeeklyHours) * 100)
})

const conflictsCount = computed(() => {
  return props.teacher.assignedCourses?.filter(course => course.hasConflicts).length || 0
})

const hasApplicableOptimizations = computed(() => {
  return optimizations.value.some(opt => opt.canApply)
})

// Methods
const generateOptimizations = async () => {
  loading.value = true
  try {
    // Simulate AI optimization analysis
    await new Promise(resolve => setTimeout(resolve, 2000))

    const newOptimizations: Optimization[] = []

    // Generate optimization suggestions based on current workload
    if (utilizationPercentage.value > 90) {
      newOptimizations.push({
        id: 'reduce-overload',
        type: 'workload_reduction',
        title: 'Reduce Workload Overload',
        description: 'Teacher is significantly overloaded. Redistribute some courses to other qualified teachers.',
        priority: 'high',
        impact: 'high',
        current: `${props.teacher.currentWorkload}h workload (${utilizationPercentage}%)`,
        proposed: `${Math.round(props.teacher.maxWeeklyHours * 0.8)}h workload (80%)`,
        benefit: 'Improved work-life balance and teaching quality',
        affectedCourses: props.teacher.assignedCourses || [],
        steps: [
          'Identify courses that can be reassigned',
          'Find qualified teachers with available capacity',
          'Reassign selected courses',
          'Update schedules and notify affected parties'
        ],
        canApply: true,
        expectedResult: 'Workload reduced to sustainable levels',
        riskLevel: 'Medium - requires coordination with other teachers'
      })
    }

    if (conflictsCount.value > 0) {
      newOptimizations.push({
        id: 'resolve-conflicts',
        type: 'conflict_resolution',
        title: 'Resolve Schedule Conflicts',
        description: `Resolve ${conflictsCount.value} scheduling conflicts to improve teacher availability.`,
        priority: 'high',
        impact: 'high',
        current: `${conflictsCount.value} unresolved conflicts`,
        proposed: 'All conflicts resolved with optimal scheduling',
        benefit: 'Eliminated scheduling conflicts and improved teacher availability',
        affectedCourses: props.teacher.assignedCourses?.filter(course => course.hasConflicts) || [],
        steps: [
          'Analyze conflict patterns and root causes',
          'Identify alternative time slots',
          'Resolve conflicts using AI-powered scheduling',
          'Verify all constraints are met'
        ],
        canApply: true,
        expectedResult: 'All scheduling conflicts resolved',
        riskLevel: 'Low - automated conflict resolution'
      })
    }

    if (utilizationPercentage.value < 50) {
      newOptimizations.push({
        id: 'increase-utilization',
        type: 'workload_increase',
        title: 'Increase Course Assignment',
        description: 'Teacher has capacity for additional courses. Assign more courses to optimize department resources.',
        priority: 'medium',
        impact: 'medium',
        current: `${props.teacher.currentWorkload}h workload (${utilizationPercentage}%)`,
        proposed: `${Math.round(props.teacher.maxWeeklyHours * 0.7)}h workload (70%)`,
        benefit: 'Better resource utilization and increased teaching opportunities',
        affectedCourses: [],
        steps: [
          'Identify unassigned courses matching teacher specializations',
          'Evaluate teacher availability and preferences',
          'Assign suitable courses',
          'Monitor workload and satisfaction'
        ],
        canApply: true,
        expectedResult: 'Optimized workload with better resource utilization',
        riskLevel: 'Low - gradual increase in workload'
      })
    }

    optimizations.value = newOptimizations
  } catch (error) {
    console.error('Failed to generate optimizations:', error)
  } finally {
    loading.value = false
  }
}

const getOptimizationClass = (type: string) => {
  switch (type) {
    case 'workload_reduction': return 'border-red-300 bg-red-50'
    case 'conflict_resolution': return 'border-orange-300 bg-orange-50'
    case 'workload_increase': return 'border-green-300 bg-green-50'
    case 'schedule_optimization': return 'border-blue-300 bg-blue-50'
    default: return 'border-gray-300 bg-gray-50'
  }
}

const getOptimizationIconClass = (type: string) => {
  switch (type) {
    case 'workload_reduction': return 'text-red-600 bg-red-100 rounded-full p-1'
    case 'conflict_resolution': return 'text-orange-600 bg-orange-100 rounded-full p-1'
    case 'workload_increase': return 'text-green-600 bg-green-100 rounded-full p-1'
    case 'schedule_optimization': return 'text-blue-600 bg-blue-100 rounded-full p-1'
    default: return 'text-gray-600 bg-gray-100 rounded-full p-1'
  }
}

const getOptimizationIcon = (type: string) => {
  switch (type) {
    case 'workload_reduction': return AlertTriangle
    case 'conflict_resolution': return Target
    case 'workload_increase': return TrendingUp
    case 'schedule_optimization': return Users
    default: return Info
  }
}

const getOptimizationVariant = (priority: string) => {
  switch (priority) {
    case 'high': return 'destructive'
    case 'medium': return 'outline'
    case 'low': return 'secondary'
    default: return 'outline'
  }
}

const applyOptimization = async (optimization: Optimization) => {
  applying.value = true
  try {
    // Apply the optimization
    console.log('Applying optimization:', optimization.title)

    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000))

    emit('optimized')
  } catch (error) {
    console.error('Failed to apply optimization:', error)
  } finally {
    applying.value = false
  }
}

const applyAllOptimizations = async () => {
  applying.value = true
  try {
    const applicableOptimizations = optimizations.value.filter(opt => opt.canApply)

    for (const optimization of applicableOptimizations) {
      await applyOptimization(optimization)
    }
  } catch (error) {
    console.error('Failed to apply optimizations:', error)
  } finally {
    applying.value = false
  }
}

const viewOptimizationDetails = (optimization: Optimization) => {
  selectedOptimization.value = optimization
}
</script>