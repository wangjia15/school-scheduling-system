<template>
  <div v-if="open" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
    <Card class="w-full max-w-4xl max-h-[90vh] overflow-y-auto">
      <CardHeader>
        <div class="flex items-center justify-between">
          <CardTitle>Teacher Profile Management</CardTitle>
          <Button variant="ghost" size="icon" @click="$emit('update:open', false)">
            <X class="h-4 w-4" />
          </Button>
        </div>
        <CardDescription>
          Manage teacher profile, qualifications, and specializations
        </CardDescription>
      </CardHeader>
      <CardContent>
        <!-- Profile Form -->
        <form @submit.prevent="saveProfile" class="space-y-6">
          <!-- Basic Information -->
          <div class="border rounded-lg p-4">
            <h4 class="text-lg font-medium mb-4">Basic Information</h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">First Name</label>
                <input
                  v-model="profileData.user.firstName"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Last Name</label>
                <input
                  v-model="profileData.user.lastName"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input
                  v-model="profileData.user.email"
                  type="email"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Employee ID</label>
                <input
                  v-model="profileData.employeeId"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Title</label>
                <Select v-model="profileData.title">
                  <option value="PROFESSOR">Professor</option>
                  <option value="ASSOCIATE_PROFESSOR">Associate Professor</option>
                  <option value="ASSISTANT_PROFESSOR">Assistant Professor</option>
                  <option value="INSTRUCTOR">Instructor</option>
                  <option value="ADJUNCT">Adjunct</option>
                </Select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Department</label>
                <Select v-model="profileData.departmentId">
                  <option v-for="dept in departments" :key="dept.id" :value="dept.id">
                    {{ dept.name }}
                  </option>
                </Select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
                <input
                  v-model="profileData.phone"
                  type="tel"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Office Location</label>
                <input
                  v-model="profileData.officeLocation"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
            </div>
          </div>

          <!-- Workload Settings -->
          <div class="border rounded-lg p-4">
            <h4 class="text-lg font-medium mb-4">Workload Settings</h4>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Max Weekly Hours</label>
                <input
                  v-model.number="profileData.maxWeeklyHours"
                  type="number"
                  min="1"
                  max="80"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Max Courses per Semester</label>
                <input
                  v-model.number="profileData.maxCoursesPerSemester"
                  type="number"
                  min="1"
                  max="12"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
            </div>
          </div>

          <!-- Specializations -->
          <div class="border rounded-lg p-4">
            <div class="flex items-center justify-between mb-4">
              <h4 class="text-lg font-medium">Teaching Specializations</h4>
              <Button type="button" variant="outline" size="sm" @click="addSpecialization">
                <Plus class="h-4 w-4 mr-2" />
                Add Specialization
              </Button>
            </div>
            <div class="space-y-4">
              <div
                v-for="(spec, index) in profileData.specializations"
                :key="index"
                class="border rounded-lg p-4 bg-gray-50"
              >
                <div class="flex items-center justify-between mb-3">
                  <h5 class="font-medium">Specialization {{ index + 1 }}</h5>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    @click="removeSpecialization(index)"
                    class="text-red-600 hover:text-red-800"
                  >
                    <Trash2 class="h-4 w-4" />
                  </Button>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Subject Code</label>
                    <input
                      v-model="spec.subjectCode"
                      type="text"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      placeholder="e.g., CS101, MATH201"
                      required
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Subject Name</label>
                    <input
                      v-model="spec.subjectName"
                      type="text"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      placeholder="e.g., Introduction to Computer Science"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Proficiency Level</label>
                    <Select v-model="spec.proficiencyLevel">
                      <option value="BEGINNER">Beginner</option>
                      <option value="INTERMEDIATE">Intermediate</option>
                      <option value="ADVANCED">Advanced</option>
                      <option value="EXPERT">Expert</option>
                    </Select>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Years of Experience</label>
                    <input
                      v-model.number="spec.yearsExperience"
                      type="number"
                      min="0"
                      max="50"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      required
                    />
                  </div>
                </div>
                <div class="mt-4 space-y-3">
                  <label class="flex items-center">
                    <input
                      v-model="spec.certified"
                      type="checkbox"
                      class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    />
                    <span class="ml-2 text-sm text-gray-700">Certified in this subject</span>
                  </label>
                  <div v-if="spec.certified">
                    <label class="block text-sm font-medium text-gray-700 mb-1">Certification Details</label>
                    <textarea
                      v-model="spec.certificationDetails"
                      rows="2"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      placeholder="Describe certification, issuing authority, date obtained, etc."
                    />
                  </div>
                </div>
              </div>
            </div>
            <div v-if="profileData.specializations.length === 0" class="text-center py-4 text-gray-500">
              No specializations added. Add teaching specializations to improve course matching.
            </div>
          </div>

          <!-- Qualifications -->
          <div class="border rounded-lg p-4">
            <div class="flex items-center justify-between mb-4">
              <h4 class="text-lg font-medium">Qualifications & Credentials</h4>
              <Button type="button" variant="outline" size="sm" @click="addQualification">
                <Plus class="h-4 w-4 mr-2" />
                Add Qualification
              </Button>
            </div>
            <div class="space-y-4">
              <div
                v-for="(qual, index) in profileData.qualifications"
                :key="index"
                class="border rounded-lg p-4 bg-gray-50"
              >
                <div class="flex items-center justify-between mb-3">
                  <h5 class="font-medium">Qualification {{ index + 1 }}</h5>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    @click="removeQualification(index)"
                    class="text-red-600 hover:text-red-800"
                  >
                    <Trash2 class="h-4 w-4" />
                  </Button>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Degree/Certification</label>
                    <input
                      v-model="qual.degree"
                      type="text"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      placeholder="e.g., PhD in Computer Science, MBA, PMP"
                      required
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Institution</label>
                    <input
                      v-model="qual.institution"
                      type="text"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      placeholder="e.g., Stanford University, Microsoft"
                      required
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Year Obtained</label>
                    <input
                      v-model.number="qual.year"
                      type="number"
                      min="1900"
                      :max="new Date().getFullYear()"
                      class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      required
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 mb-1">Type</label>
                    <Select v-model="qual.type">
                      <option value="DEGREE">Academic Degree</option>
                      <option value="CERTIFICATION">Professional Certification</option>
                      <option value="LICENSE">Professional License</option>
                      <option value="AWARD">Award/Honor</option>
                      <option value="OTHER">Other</option>
                    </Select>
                  </div>
                </div>
                <div class="mt-4">
                  <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
                  <textarea
                    v-model="qual.description"
                    rows="2"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Additional details about the qualification..."
                  />
                </div>
              </div>
            </div>
            <div v-if="profileData.qualifications.length === 0" class="text-center py-4 text-gray-500">
              No qualifications added. Add academic degrees, certifications, and other credentials.
            </div>
          </div>

          <!-- Form Actions -->
          <div class="flex items-center justify-between pt-4 border-t">
            <div class="text-sm text-gray-600">
              Profile updated: {{ formatDate(profileData.updatedAt) }}
            </div>
            <div class="flex items-center gap-3">
              <Button type="button" variant="outline" @click="$emit('update:open', false)">
                Cancel
              </Button>
              <Button type="submit" :disabled="saving">
                <Save v-if="!saving" class="h-4 w-4 mr-2" />
                <Loader v-else class="h-4 w-4 mr-2 animate-spin" />
                {{ isNewProfile ? 'Create Profile' : 'Update Profile' }}
              </Button>
            </div>
          </div>
        </form>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Button from '@/components/ui/Button.vue'
import Select from '@/components/ui/Select.vue'
import { X, Plus, Trash2, Save, Loader } from 'lucide-vue-next'
import teacherService, { type Teacher, type TeacherSpecializationRequest } from '@/services/teacherService'

interface Props {
  open: boolean
  teacher?: Teacher
  departments: Array<{ id: number; name: string }>
}

interface Emits {
  'update:open': [value: boolean]
  'save': []
}

interface Qualification {
  degree: string
  institution: string
  year: number
  type: 'DEGREE' | 'CERTIFICATION' | 'LICENSE' | 'AWARD' | 'OTHER'
  description?: string
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const saving = ref(false)

const profileData = reactive({
  id: 0,
  employeeId: '',
  userId: 0,
  departmentId: 0,
  title: 'PROFESSOR',
  maxWeeklyHours: 40,
  maxCoursesPerSemester: 4,
  officeLocation: '',
  phone: '',
  user: {
    id: 0,
    firstName: '',
    lastName: '',
    email: ''
  },
  specializations: [] as TeacherSpecializationRequest[],
  qualifications: [] as Qualification[],
  createdAt: '',
  updatedAt: ''
})

// Computed
const isNewProfile = computed(() => !props.teacher)

// Methods
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString()
}

const addSpecialization = () => {
  profileData.specializations.push({
    subjectCode: '',
    subjectName: '',
    proficiencyLevel: 'INTERMEDIATE',
    yearsExperience: 0,
    certified: false,
    certificationDetails: ''
  })
}

const removeSpecialization = (index: number) => {
  profileData.specializations.splice(index, 1)
}

const addQualification = () => {
  profileData.qualifications.push({
    degree: '',
    institution: '',
    year: new Date().getFullYear(),
    type: 'DEGREE',
    description: ''
  })
}

const removeQualification = (index: number) => {
  profileData.qualifications.splice(index, 1)
}

const saveProfile = async () => {
  saving.value = true
  try {
    const teacherData = {
      employeeId: profileData.employeeId,
      userId: profileData.userId,
      departmentId: profileData.departmentId,
      title: profileData.title,
      maxWeeklyHours: profileData.maxWeeklyHours,
      maxCoursesPerSemester: profileData.maxCoursesPerSemester,
      officeLocation: profileData.officeLocation,
      phone: profileData.phone,
      specializations: profileData.specializations
    }

    if (isNewProfile.value) {
      await teacherService.createTeacher(teacherData)
    } else {
      await teacherService.updateTeacher(profileData.id, teacherData)
    }

    emit('save')
  } catch (error) {
    console.error('Failed to save profile:', error)
  } finally {
    saving.value = false
  }
}

// Reset form when modal opens
watch(() => props.open, (newOpen) => {
  if (newOpen && props.teacher) {
    // Load existing teacher data
    Object.assign(profileData, {
      ...props.teacher,
      qualifications: [] // Initialize empty qualifications array
    })
  } else if (newOpen) {
    // Reset form for new teacher
    Object.assign(profileData, {
      id: 0,
      employeeId: '',
      userId: 0,
      departmentId: props.departments[0]?.id || 0,
      title: 'PROFESSOR',
      maxWeeklyHours: 40,
      maxCoursesPerSemester: 4,
      officeLocation: '',
      phone: '',
      user: {
        id: 0,
        firstName: '',
        lastName: '',
        email: ''
      },
      specializations: [],
      qualifications: [],
      createdAt: '',
      updatedAt: ''
    })
  }
})
</script>