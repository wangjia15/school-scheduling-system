<template>
  <div class="container mx-auto px-4 py-8">
    <div class="card">
      <div class="card-header">
        <h1 class="card-title">Welcome to School Scheduling System</h1>
        <p class="card-description">Manage teachers, courses, and classrooms efficiently</p>
      </div>
      <div class="card-content">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div class="card">
            <div class="card-header">
              <h3 class="text-lg font-semibold">Teachers</h3>
            </div>
            <div class="card-content">
              <p class="text-2xl font-bold">0</p>
              <p class="text-sm text-muted-foreground">Total teachers</p>
            </div>
          </div>
          <div class="card">
            <div class="card-header">
              <h3 class="text-lg font-semibold">Courses</h3>
            </div>
            <div class="card-content">
              <p class="text-2xl font-bold">0</p>
              <p class="text-sm text-muted-foreground">Total courses</p>
            </div>
          </div>
          <div class="card">
            <div class="card-header">
              <h3 class="text-lg font-semibold">Classrooms</h3>
            </div>
            <div class="card-content">
              <p class="text-2xl font-bold">0</p>
              <p class="text-sm text-muted-foreground">Available rooms</p>
            </div>
          </div>
          <div class="card">
            <div class="card-header">
              <h3 class="text-lg font-semibold">Schedules</h3>
            </div>
            <div class="card-content">
              <p class="text-2xl font-bold">0</p>
              <p class="text-sm text-muted-foreground">Active schedules</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Home view component
</script>