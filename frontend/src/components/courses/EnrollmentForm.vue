<template>
  <form @submit.prevent="handleSubmit" class="space-y-4">
    <!-- Student Selection -->
    <div>
      <label class="block text-sm font-medium mb-1">Student</label>
      <SelectInput
        v-model="formData.studentId"
        :options="studentOptions"
        placeholder="Select a student"
        :error="errors.studentId"
        required
      />
      <p v-if="errors.studentId" class="text-red-500 text-sm mt-1">{{ errors.studentId }}</p>
    </div>

    <!-- Student Info Display -->
    <div v-if="selectedStudent" class="p-3 bg-muted rounded-lg">
      <div class="font-medium">{{ selectedStudent.name }}</div>
      <div class="text-sm text-muted-foreground">
        ID: {{ selectedStudent.id }} • {{ selectedStudent.email }}
      </div>
      <div class="text-sm text-muted-foreground">
        Major: {{ selectedStudent.major }} • Year: {{ selectedStudent.year }}
      </div>
    </div>

    <!-- Enrollment Type -->
    <div>
      <label class="block text-sm font-medium mb-1">Enrollment Type</label>
      <SelectInput
        v-model="formData.enrollmentType"
        :options="enrollmentTypeOptions"
        placeholder="Select enrollment type"
        :error="errors.enrollmentType"
        required
      />
      <p v-if="errors.enrollmentType" class="text-red-500 text-sm mt-1">{{ errors.enrollmentType }}</p>
    </div>

    <!-- Academic Standing -->
    <div v-if="selectedStudent">
      <label class="block text-sm font-medium mb-1">Academic Standing</label>
      <div class="p-3 bg-muted rounded-lg">
        <div class="grid grid-cols-2 gap-4 text-sm">
          <div>
            <span class="font-medium">GPA:</span> {{ selectedStudent.gpa }}
          </div>
          <div>
            <span class="font-medium">Credits:</span> {{ selectedStudent.totalCredits }}
          </div>
          <div>
            <span class="font-medium">Standing:</span>
            <Badge :variant="getStandingVariant(selectedStudent.academicStanding)">
              {{ selectedStudent.academicStanding }}
            </Badge>
          </div>
          <div>
            <span class="font-medium">Status:</span>
            <Badge :variant="selectedStudent.isActive ? 'default' : 'secondary'">
              {{ selectedStudent.isActive ? 'Active' : 'Inactive' }}
            </Badge>
          </div>
        </div>
      </div>
    </div>

    <!-- Prerequisite Check -->
    <div v-if="prerequisiteCheck">
      <label class="block text-sm font-medium mb-1">Prerequisite Requirements</label>
      <div :class="[
        'p-3 rounded-lg',
        prerequisiteCheck.met ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'
      ]">
        <div class="flex items-center space-x-2 mb-2">
          <CheckCircle v-if="prerequisiteCheck.met" class="w-5 h-5 text-green-600" />
          <XCircle v-else class="w-5 h-5 text-red-600" />
          <span class="font-medium">
            {{ prerequisiteCheck.met ? 'Prerequisites Met' : 'Prerequisites Not Met' }}
          </span>
        </div>
        <ul v-if="prerequisiteCheck.issues.length > 0" class="text-sm space-y-1">
          <li v-for="issue in prerequisiteCheck.issues" :key="issue" class="text-red-700">
            • {{ issue }}
          </li>
        </ul>
      </div>
    </div>

    <!-- Enrollment Notes -->
    <div>
      <label class="block text-sm font-medium mb-1">Notes (Optional)</label>
      <textarea
        v-model="formData.notes"
        class="w-full p-2 border rounded-md text-sm"
        rows="3"
        placeholder="Add any additional notes or special requirements..."
      ></textarea>
    </div>

    <!-- Available Slots Warning -->
    <div v-if="availableSlots === 0" class="p-3 bg-yellow-50 border-yellow-200 rounded-lg">
      <div class="flex items-center space-x-2">
        <AlertTriangle class="w-5 h-5 text-yellow-600" />
        <span class="font-medium text-yellow-800">Course is Full</span>
      </div>
      <p class="text-sm text-yellow-700 mt-1">
        Student will be added to the waitlist. Current position: {{ waitlistPosition }}
      </p>
    </div>

    <!-- Actions -->
    <div class="flex justify-end space-x-2 pt-4">
      <Button type="button" variant="outline" @click="$emit('cancel')">
        Cancel
      </Button>
      <Button
        type="submit"
        :disabled="isSubmitting || !prerequisiteCheck?.met"
        :variant="availableSlots === 0 ? 'secondary' : 'default'"
      >
        {{ isSubmitting ? 'Enrolling...' : (availableSlots === 0 ? 'Add to Waitlist' : 'Enroll Student') }}
      </Button>
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import Button from '@/components/ui/Button.vue'
import SelectInput from '@/components/ui/SelectInput.vue'
import Badge from '@/components/ui/Badge.vue'
import { useToast } from 'vue-toastification'
import { CheckCircle, XCircle, AlertTriangle } from 'lucide-vue-next'

interface Props {
  course: any
  availableSlots: number
}

interface Emits {
  (e: 'submit', data: any): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const toast = useToast()

// State
const isSubmitting = ref(false)
const students = ref([]) // Mock student data
const waitlistPosition = ref(1) // Mock waitlist position

const formData = reactive({
  studentId: '',
  enrollmentType: '',
  notes: ''
})

const errors = reactive<Record<string, string>>({})

// Computed
const selectedStudent = computed(() => {
  return students.value.find(s => s.id === formData.studentId)
})

const studentOptions = computed(() =>
  students.value.map(student => ({
    value: student.id.toString(),
    label: `${student.name} (${student.id})`
  }))
)

const enrollmentTypeOptions = [
  { value: 'REGULAR', label: 'Regular Enrollment' },
  { value: 'AUDIT', label: 'Audit' },
  { value: 'PASS_FAIL', label: 'Pass/Fail' },
  { value: 'INDEPENDENT_STUDY', label: 'Independent Study' }
]

const prerequisiteCheck = computed(() => {
  if (!selectedStudent.value || !props.course.prerequisites) {
    return { met: true, issues: [] }
  }

  // Mock prerequisite check
  const issues = []
  if (props.course.prerequisites.length > 0 && selectedStudent.value.gpa < 2.0) {
    issues.push('Minimum GPA requirement not met')
  }

  return {
    met: issues.length === 0,
    issues
  }
})

// Methods
const validateForm = (): boolean => {
  errors.studentId = formData.studentId ? '' : 'Student selection is required'
  errors.enrollmentType = formData.enrollmentType ? '' : 'Enrollment type is required'

  return Object.values(errors).every(error => !error)
}

const handleSubmit = async () => {
  if (!validateForm()) {
    toast.error('Please fix the errors in the form')
    return
  }

  if (!prerequisiteCheck.value.met) {
    toast.error('Student does not meet prerequisites')
    return
  }

  try {
    isSubmitting.value = true
    const enrollmentData = {
      ...formData,
      courseId: props.course.id,
      studentName: selectedStudent.value?.name,
      status: props.availableSlots > 0 ? 'ACTIVE' : 'WAITLISTED'
    }

    emit('submit', enrollmentData)
  } catch (error) {
    toast.error('Failed to enroll student')
  } finally {
    isSubmitting.value = false
  }
}

const getStandingVariant = (standing: string) => {
  switch (standing) {
    case 'GOOD': return 'default'
    case 'PROBATION': return 'destructive'
    case 'SUSPENDED': return 'destructive'
    default: return 'secondary'
  }
}

// Mock data initialization
const loadMockStudents = () => {
  students.value = [
    {
      id: 'S001',
      name: 'Alice Johnson',
      email: 'alice.johnson@university.edu',
      major: 'Computer Science',
      year: 'Junior',
      gpa: 3.8,
      totalCredits: 89,
      academicStanding: 'GOOD',
      isActive: true
    },
    {
      id: 'S002',
      name: 'Bob Smith',
      email: 'bob.smith@university.edu',
      major: 'Mathematics',
      year: 'Sophomore',
      gpa: 3.2,
      totalCredits: 62,
      academicStanding: 'GOOD',
      isActive: true
    },
    {
      id: 'S003',
      name: 'Carol Davis',
      email: 'carol.davis@university.edu',
      major: 'Physics',
      year: 'Senior',
      gpa: 2.9,
      totalCredits: 105,
      academicStanding: 'GOOD',
      isActive: true
    }
  ]
}

// Watch for student selection changes
watch(() => formData.studentId, (newStudentId) => {
  if (newStudentId) {
    // Reset errors when student is selected
    errors.studentId = ''
  }
})

// Lifecycle
loadMockStudents()
</script>