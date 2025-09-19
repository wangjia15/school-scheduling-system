package com.school.scheduling.service;

import com.school.scheduling.domain.Schedule;
import com.school.scheduling.domain.ScheduleConflict;
import com.school.scheduling.domain.Teacher;
import com.school.scheduling.domain.Classroom;
import com.school.scheduling.domain.Course;
import com.school.scheduling.domain.Student;
import com.school.scheduling.domain.CourseOffering;
import com.school.scheduling.domain.TimeSlot;
import com.school.scheduling.mapper.ConflictMapper;
import com.school.scheduling.mapper.ScheduleMapper;
import com.school.scheduling.mapper.TeacherMapper;
import com.school.scheduling.mapper.ClassroomMapper;
import com.school.scheduling.mapper.CourseMapper;
import com.school.scheduling.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConflictDetectionService {

    private final ConflictMapper conflictMapper;
    private final ScheduleMapper scheduleMapper;
    private final TeacherMapper teacherMapper;
    private final ClassroomMapper classroomMapper;
    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;

    public List<ScheduleConflict> detectConflictsForSchedule(Schedule schedule) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        // Teacher conflicts
        conflicts.addAll(detectTeacherConflicts(schedule));

        // Classroom conflicts
        conflicts.addAll(detectClassroomConflicts(schedule));

        // Time slot conflicts
        conflicts.addAll(detectTimeSlotConflicts(schedule));

        // Student conflicts
        conflicts.addAll(detectStudentConflicts(schedule));

        // Capacity conflicts
        conflicts.addAll(detectCapacityConflicts(schedule));

        // Prerequisite conflicts
        conflicts.addAll(detectPrerequisiteConflicts(schedule));

        // Teacher workload conflicts
        conflicts.addAll(detectTeacherWorkloadConflicts(schedule));

        return conflicts;
    }

    public List<ScheduleConflict> detectAllSystemConflicts() {
        List<ScheduleConflict> allConflicts = new ArrayList<>();
        List<Schedule> allSchedules = scheduleMapper.findAllActiveSchedules();

        for (Schedule schedule : allSchedules) {
            allConflicts.addAll(detectConflictsForSchedule(schedule));
        }

        // Remove duplicates and persist
        List<ScheduleConflict> uniqueConflicts = deduplicateConflicts(allConflicts);
        persistConflicts(uniqueConflicts);

        return uniqueConflicts;
    }

    public CompletableFuture<List<ScheduleConflict>> detectConflictsAsync(Schedule schedule) {
        return CompletableFuture.supplyAsync(() -> detectConflictsForSchedule(schedule));
    }

    public List<ScheduleConflict> detectTeacherConflicts(Schedule schedule) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        if (schedule.getTeacher() == null) {
            return conflicts;
        }

        Teacher teacher = schedule.getTeacher();
        TimeSlot scheduleTimeSlot = schedule.getTimeSlot();

        // Find overlapping schedules for the same teacher
        List<Schedule> teacherSchedules = scheduleMapper.findSchedulesByTeacherAndTimeRange(
            teacher.getId(),
            scheduleTimeSlot.getStartTime(),
            scheduleTimeSlot.getEndTime()
        );

        for (Schedule existingSchedule : teacherSchedules) {
            if (!existingSchedule.getId().equals(schedule.getId()) &&
                hasTimeOverlap(existingSchedule.getTimeSlot(), scheduleTimeSlot)) {

                ScheduleConflict conflict = new ScheduleConflict();
                conflict.setConflictType(ScheduleConflict.ConflictType.TEACHER_DOUBLE_BOOKING);
                conflict.setSeverity(ScheduleConflict.Severity.HIGH);
                conflict.setDescription(String.format(
                    "Teacher %s is double-booked between %s and %s. " +
                    "Existing: %s in %s, New: %s in %s",
                    teacher.getFullName(),
                    scheduleTimeSlot.getStartTime(),
                    scheduleTimeSlot.getEndTime(),
                    existingSchedule.getCourseOffering().getCourse().getCourseCode(),
                    existingSchedule.getClassroom().getRoomCode(),
                    schedule.getCourseOffering().getCourse().getCourseCode(),
                    schedule.getClassroom().getRoomCode()
                ));
                conflict.setSchedule1(existingSchedule);
                conflict.setSchedule2(schedule);
                conflict.setEntityType(ScheduleConflict.EntityType.TEACHER);
                conflict.setEntityId(teacher.getId());
                conflict.setResolutionStatus(ScheduleConflict.ResolutionStatus.PENDING);
                conflict.setDetectedAt(LocalDateTime.now());

                conflicts.add(conflict);
            }
        }

        return conflicts;
    }

    public List<ScheduleConflict> detectClassroomConflicts(Schedule schedule) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        if (schedule.getClassroom() == null) {
            return conflicts;
        }

        Classroom classroom = schedule.getClassroom();
        TimeSlot scheduleTimeSlot = schedule.getTimeSlot();

        // Find overlapping schedules for the same classroom
        List<Schedule> classroomSchedules = scheduleMapper.findSchedulesByClassroomAndTimeRange(
            classroom.getId(),
            scheduleTimeSlot.getStartTime(),
            scheduleTimeSlot.getEndTime()
        );

        for (Schedule existingSchedule : classroomSchedules) {
            if (!existingSchedule.getId().equals(schedule.getId()) &&
                hasTimeOverlap(existingSchedule.getTimeSlot(), scheduleTimeSlot)) {

                ScheduleConflict conflict = new ScheduleConflict();
                conflict.setConflictType(ScheduleConflict.ConflictType.CLASSROOM_DOUBLE_BOOKING);
                conflict.setSeverity(ScheduleConflict.Severity.HIGH);
                conflict.setDescription(String.format(
                    "Classroom %s is double-booked between %s and %s. " +
                    "Existing: %s taught by %s, New: %s taught by %s",
                    classroom.getRoomCode(),
                    scheduleTimeSlot.getStartTime(),
                    scheduleTimeSlot.getEndTime(),
                    existingSchedule.getCourseOffering().getCourse().getCourseCode(),
                    existingSchedule.getTeacher().getFullName(),
                    schedule.getCourseOffering().getCourse().getCourseCode(),
                    schedule.getTeacher().getFullName()
                ));
                conflict.setSchedule1(existingSchedule);
                conflict.setSchedule2(schedule);
                conflict.setEntityType(ScheduleConflict.EntityType.CLASSROOM);
                conflict.setEntityId(classroom.getId());
                conflict.setResolutionStatus(ScheduleConflict.ResolutionStatus.PENDING);
                conflict.setDetectedAt(LocalDateTime.now());

                conflicts.add(conflict);
            }
        }

        return conflicts;
    }

    public List<ScheduleConflict> detectTimeSlotConflicts(Schedule schedule) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        if (schedule.getTimeSlot() == null) {
            return conflicts;
        }

        TimeSlot timeSlot = schedule.getTimeSlot();

        // Check for invalid time slots (outside allowed hours)
        if (isOutsideAllowedHours(timeSlot)) {
            ScheduleConflict conflict = new ScheduleConflict();
            conflict.setConflictType(ScheduleConflict.ConflictType.TIME_SLOT_CONFLICT);
            conflict.setSeverity(ScheduleConflict.Severity.MEDIUM);
            conflict.setDescription(String.format(
                "Invalid time slot: %s to %s is outside allowed scheduling hours (8:00 AM - 9:00 PM)",
                timeSlot.getStartTime(),
                timeSlot.getEndTime()
            ));
            conflict.setSchedule2(schedule);
            conflict.setEntityType(ScheduleConflict.EntityType.COURSE_OFFERING);
            conflict.setEntityId(schedule.getCourseOffering().getId());
            conflict.setResolutionStatus(ScheduleConflict.ResolutionStatus.PENDING);
            conflict.setDetectedAt(LocalDateTime.now());

            conflicts.add(conflict);
        }

        return conflicts;
    }

    public List<ScheduleConflict> detectStudentConflicts(Schedule schedule) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        if (schedule.getCourseOffering() == null) {
            return conflicts;
        }

        CourseOffering courseOffering = schedule.getCourseOffering();
        TimeSlot scheduleTimeSlot = schedule.getTimeSlot();

        // Get enrolled students
        List<Student> enrolledStudents = studentMapper.findStudentsByCourseOffering(courseOffering.getId());

        for (Student student : enrolledStudents) {
            // Find student's other schedules at the same time
            List<Schedule> studentSchedules = scheduleMapper.findSchedulesByStudentAndTimeRange(
                student.getId(),
                scheduleTimeSlot.getStartTime(),
                scheduleTimeSlot.getEndTime()
            );

            for (Schedule existingSchedule : studentSchedules) {
                if (!existingSchedule.getId().equals(schedule.getId()) &&
                    hasTimeOverlap(existingSchedule.getTimeSlot(), scheduleTimeSlot)) {

                    ScheduleConflict conflict = new ScheduleConflict();
                    conflict.setConflictType(ScheduleConflict.ConflictType.STUDENT_SCHEDULE_CONFLICT);
                    conflict.setSeverity(ScheduleConflict.Severity.MEDIUM);
                    conflict.setDescription(String.format(
                        "Student %s has conflicting schedules: %s at %s and %s at %s",
                        student.getFullName(),
                        existingSchedule.getCourseOffering().getCourse().getCourseCode(),
                        existingSchedule.getTimeSlot().getStartTime(),
                        courseOffering.getCourse().getCourseCode(),
                        scheduleTimeSlot.getStartTime()
                    ));
                    conflict.setSchedule1(existingSchedule);
                    conflict.setSchedule2(schedule);
                    conflict.setEntityType(ScheduleConflict.EntityType.STUDENT);
                    conflict.setEntityId(student.getId());
                    conflict.setResolutionStatus(ScheduleConflict.ResolutionStatus.PENDING);
                    conflict.setDetectedAt(LocalDateTime.now());

                    conflicts.add(conflict);
                }
            }
        }

        return conflicts;
    }

    public List<ScheduleConflict> detectCapacityConflicts(Schedule schedule) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        if (schedule.getClassroom() == null || schedule.getCourseOffering() == null) {
            return conflicts;
        }

        Classroom classroom = schedule.getClassroom();
        CourseOffering courseOffering = schedule.getCourseOffering();

        // Check if classroom capacity is sufficient
        if (courseOffering.getEnrolledCount() > classroom.getCapacity()) {
            ScheduleConflict conflict = new ScheduleConflict();
            conflict.setConflictType(ScheduleConflict.ConflictType.CAPACITY_EXCEEDED);
            conflict.setSeverity(ScheduleConflict.Severity.MEDIUM);
            conflict.setDescription(String.format(
                "Classroom %s (capacity: %d) cannot accommodate %s (enrolled: %d). " +
                "Exceeds capacity by %d students.",
                classroom.getRoomCode(),
                classroom.getCapacity(),
                courseOffering.getCourse().getCourseCode(),
                courseOffering.getEnrolledCount(),
                courseOffering.getEnrolledCount() - classroom.getCapacity()
            ));
            conflict.setSchedule2(schedule);
            conflict.setEntityType(ScheduleConflict.EntityType.CLASSROOM);
            conflict.setEntityId(classroom.getId());
            conflict.setResolutionStatus(ScheduleConflict.ResolutionStatus.PENDING);
            conflict.setDetectedAt(LocalDateTime.now());

            conflicts.add(conflict);
        }

        return conflicts;
    }

    public List<ScheduleConflict> detectPrerequisiteConflicts(Schedule schedule) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        if (schedule.getCourseOffering() == null) {
            return conflicts;
        }

        CourseOffering courseOffering = schedule.getCourseOffering();
        Course course = courseOffering.getCourse();

        // Get enrolled students
        List<Student> enrolledStudents = studentMapper.findStudentsByCourseOffering(courseOffering.getId());

        for (Student student : enrolledStudents) {
            // Check if student has completed prerequisites
            List<Course> missingPrerequisites = courseMapper.findMissingPrerequisitesForStudent(
                course.getId(), student.getId()
            );

            if (!missingPrerequisites.isEmpty()) {
                String prerequisiteList = missingPrerequisites.stream()
                    .map(Course::getCourseCode)
                    .collect(Collectors.joining(", "));

                ScheduleConflict conflict = new ScheduleConflict();
                conflict.setConflictType(ScheduleConflict.ConflictType.PREREQUISITE_NOT_MET);
                conflict.setSeverity(ScheduleConflict.Severity.LOW);
                conflict.setDescription(String.format(
                    "Student %s is enrolled in %s without completing prerequisites: %s",
                    student.getFullName(),
                    course.getCourseCode(),
                    prerequisiteList
                ));
                conflict.setSchedule2(schedule);
                conflict.setEntityType(ScheduleConflict.EntityType.STUDENT);
                conflict.setEntityId(student.getId());
                conflict.setResolutionStatus(ScheduleConflict.ResolutionStatus.PENDING);
                conflict.setDetectedAt(LocalDateTime.now());

                conflicts.add(conflict);
            }
        }

        return conflicts;
    }

    public List<ScheduleConflict> detectTeacherWorkloadConflicts(Schedule schedule) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        if (schedule.getTeacher() == null) {
            return conflicts;
        }

        Teacher teacher = schedule.getTeacher();

        // Calculate teacher's current workload
        int currentWorkload = scheduleMapper.calculateTeacherWorkload(teacher.getId());
        int maxWorkload = teacher.getMaxWorkloadHours() != null ? teacher.getMaxWorkloadHours() : 40;

        if (currentWorkload > maxWorkload) {
            ScheduleConflict conflict = new ScheduleConflict();
            conflict.setConflictType(ScheduleConflict.ConflictType.TEACHER_WORKLOAD_EXCEEDED);
            conflict.setSeverity(ScheduleConflict.Severity.MEDIUM);
            conflict.setDescription(String.format(
                "Teacher %s exceeds maximum workload: %d hours (max: %d hours)",
                teacher.getFullName(),
                currentWorkload,
                maxWorkload
            ));
            conflict.setSchedule2(schedule);
            conflict.setEntityType(ScheduleConflict.EntityType.TEACHER);
            conflict.setEntityId(teacher.getId());
            conflict.setResolutionStatus(ScheduleConflict.ResolutionStatus.PENDING);
            conflict.setDetectedAt(LocalDateTime.now());

            conflicts.add(conflict);
        }

        return conflicts;
    }

    public void processAndStoreConflicts(List<ScheduleConflict> conflicts) {
        List<ScheduleConflict> validConflicts = conflicts.stream()
            .filter(ScheduleConflict::isValidConflict)
            .collect(Collectors.toList());

        List<ScheduleConflict> uniqueConflicts = deduplicateConflicts(validConflicts);

        for (ScheduleConflict conflict : uniqueConflicts) {
            if (!isDuplicateConflict(conflict)) {
                conflictMapper.insert(conflict);
                log.info("Stored new conflict: {} for entity {}",
                    conflict.getConflictType(), conflict.getEntityId());
            }
        }
    }

    private List<ScheduleConflict> deduplicateConflicts(List<ScheduleConflict> conflicts) {
        return conflicts.stream()
            .distinct()
            .collect(Collectors.toList());
    }

    private boolean isDuplicateConflict(ScheduleConflict newConflict) {
        List<ScheduleConflict> recentConflicts = conflictMapper.findRecentConflicts(
            LocalDateTime.now().minusHours(1)
        );

        return recentConflicts.stream()
            .anyMatch(existing -> existing.isDuplicateOf(newConflict));
    }

    private void persistConflicts(List<ScheduleConflict> conflicts) {
        if (!conflicts.isEmpty()) {
            conflictMapper.batchInsert(conflicts);
            log.info("Persisted {} conflicts", conflicts.size());
        }
    }

    private boolean hasTimeOverlap(TimeSlot slot1, TimeSlot slot2) {
        return slot1.getStartTime().isBefore(slot2.getEndTime()) &&
               slot1.getEndTime().isAfter(slot2.getStartTime());
    }

    private boolean isOutsideAllowedHours(TimeSlot timeSlot) {
        // Define allowed hours (8:00 AM to 9:00 PM)
        int startHour = timeSlot.getStartTime().getHour();
        int endHour = timeSlot.getEndTime().getHour();

        return startHour < 8 || endHour > 21;
    }

    public List<ScheduleConflict> getPendingConflicts() {
        return conflictMapper.findPendingConflicts();
    }

    public List<ScheduleConflict> getConflictsBySeverity(ScheduleConflict.Severity severity) {
        return conflictMapper.findPendingBySeverity(severity.name());
    }

    public int getConflictCountByType(ScheduleConflict.ConflictType conflictType) {
        return conflictMapper.findByConflictType(conflictType.name()).size();
    }

    public void resolveConflict(Long conflictId, String resolutionNotes) {
        conflictMapper.markAsResolved(
            conflictId,
            resolutionNotes,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    public void ignoreConflict(Long conflictId, String reason) {
        conflictMapper.markAsIgnored(
            conflictId,
            reason,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    public List<ScheduleConflict> detectConflictsForTeacher(Long teacherId) {
        List<Schedule> teacherSchedules = scheduleMapper.findSchedulesByTeacher(teacherId);
        List<ScheduleConflict> conflicts = new ArrayList<>();

        for (Schedule schedule : teacherSchedules) {
            conflicts.addAll(detectTeacherConflicts(schedule));
            conflicts.addAll(detectTeacherWorkloadConflicts(schedule));
        }

        return conflicts;
    }

    public List<ScheduleConflict> detectConflictsForClassroom(Long classroomId) {
        List<Schedule> classroomSchedules = scheduleMapper.findSchedulesByClassroom(classroomId);
        List<ScheduleConflict> conflicts = new ArrayList<>();

        for (Schedule schedule : classroomSchedules) {
            conflicts.addAll(detectClassroomConflicts(schedule));
            conflicts.addAll(detectCapacityConflicts(schedule));
        }

        return conflicts;
    }

    public boolean hasConflicts(Long scheduleId) {
        List<ScheduleConflict> conflicts = conflictMapper.findByScheduleId(scheduleId);
        return conflicts.stream()
            .anyMatch(conflict -> conflict.getResolutionStatus() == ScheduleConflict.ResolutionStatus.PENDING);
    }

    public List<ScheduleConflict> getCriticalConflicts() {
        return conflictMapper.findPendingBySeverity(ScheduleConflict.Severity.CRITICAL.name());
    }

    public List<ScheduleConflict> getHighPriorityConflicts() {
        List<ScheduleConflict> conflicts = new ArrayList<>();
        conflicts.addAll(conflictMapper.findPendingBySeverity(ScheduleConflict.Severity.CRITICAL.name()));
        conflicts.addAll(conflictMapper.findPendingBySeverity(ScheduleConflict.Severity.HIGH.name()));
        return conflicts;
    }
}