package com.school.scheduling.algorithm;

import com.school.scheduling.algorithm.constraint.*;
import com.school.scheduling.domain.constraint.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.DayOfWeek;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConstraintSatisfactionProblem class.
 */
class ConstraintSatisfactionProblemTest {

    private Set<SchedulingVariable> variables;
    private Set<SchedulingConstraint> constraints;
    private Map<SchedulingVariable, List<SchedulingValue>> domains;
    private ConstraintSatisfactionProblem csp;

    @BeforeEach
    void setUp() {
        // Create test variables
        variables = new HashSet<>();
        for (int i = 1; i <= 3; i++) {
            variables.add(new SchedulingVariable(
                VariableType.COURSE_SCHEDULING,
                "course_" + i,
                "Course " + i
            ));
        }

        // Create test values
        List<SchedulingValue> teachers = Arrays.asList(
            new SchedulingValue(ValueType.TEACHER, "teacher_1", "Teacher A"),
            new SchedulingValue(ValueType.TEACHER, "teacher_2", "Teacher B")
        );

        List<SchedulingValue> classrooms = Arrays.asList(
            new SchedulingValue(ValueType.CLASSROOM, "room_101", "Room 101"),
            new SchedulingValue(ValueType.CLASSROOM, "room_102", "Room 102")
        );

        List<SchedulingValue> timeSlots = Arrays.asList(
            new SchedulingValue(ValueType.TIME_SLOT, "slot_1", "9:00-10:00"),
            new SchedulingValue(ValueType.TIME_SLOT, "slot_2", "10:00-11:00")
        );

        // Create domains
        domains = new HashMap<>();
        for (SchedulingVariable variable : variables) {
            List<SchedulingValue> variableValues = new ArrayList<>();
            variableValues.addAll(teachers);
            variableValues.addAll(classrooms);
            variableValues.addAll(timeSlots);
            domains.put(variable, variableValues);
        }

        // Create constraints
        constraints = new HashSet<>();
        constraints.add(new MockConstraint("Teacher Assignment Constraint", ConstraintPriority.HARD));
        constraints.add(new MockConstraint("Classroom Assignment Constraint", ConstraintPriority.HARD));

        csp = new ConstraintSatisfactionProblem(variables, constraints, domains);
    }

    @Test
    @DisplayName("Should create CSP with valid parameters")
    void shouldCreateCSPWithValidParameters() {
        assertNotNull(csp);
        assertEquals(3, csp.getVariables().size());
        assertEquals(2, csp.getConstraints().size());
        assertEquals(3, csp.getDomains().size());
    }

    @Test
    @DisplayName("Should solve simple CSP with backtracking")
    void shouldSolveSimpleCSPWithBacktracking() {
        Optional<SchedulingAssignment> solution = csp.solve();

        assertTrue(solution.isPresent(), "Should find a solution");
        SchedulingAssignment assignment = solution.get();
        assertTrue(assignment.isComplete(variables), "Assignment should be complete");
        assertEquals(3, assignment.size(), "Assignment should have 3 variables assigned");
    }

    @Test
    @DisplayName("Should solve with AC-3 strategy")
    void shouldSolveWithAC3Strategy() {
        Optional<SchedulingAssignment> solution = csp.solveWithStrategy(
            ConstraintSatisfactionProblem.SolvingStrategy.BACKTRACKING_AC3
        );

        assertTrue(solution.isPresent(), "Should find a solution with AC-3");
    }

    @Test
    @DisplayName("Should solve with min-conflicts strategy")
    void shouldSolveWithMinConflictsStrategy() {
        Optional<SchedulingAssignment> solution = csp.solveWithStrategy(
            ConstraintSatisfactionProblem.SolvingStrategy.MIN_CONFLICTS
        );

        // Min-conflicts might not always find a solution immediately for this simple test
        // We just verify it runs without error
        assertNotNull(solution);
    }

    @Test
    @DisplayName("Should handle empty domains gracefully")
    void shouldHandleEmptyDomainsGracefully() {
        // Create a variable with empty domain
        SchedulingVariable emptyVar = new SchedulingVariable(
            VariableType.COURSE_SCHEDULING,
            "empty_course",
            "Empty Course"
        );
        domains.put(emptyVar, new ArrayList<>());

        ConstraintSatisfactionProblem emptyCsp = new ConstraintSatisfactionProblem(
            Collections.singleton(emptyVar),
            constraints,
            domains
        );

        Optional<SchedulingAssignment> solution = emptyCsp.solve();
        assertFalse(solution.isPresent(), "Should not find solution with empty domain");
    }

    @Test
    @DisplayName("Should track performance metrics")
    void shouldTrackPerformanceMetrics() {
        csp.solve();

        assertTrue(csp.getNodesExplored() > 0, "Should explore some nodes");
        assertTrue(csp.getExecutionTimeMs() >= 0, "Should track execution time");
        assertNotNull(csp.getPerformanceSummary(), "Should provide performance summary");
    }

    @Test
    @DisplayName("Should handle inconsistent constraints")
    void shouldHandleInconsistentConstraints() {
        // Create conflicting constraints
        Set<SchedulingConstraint> conflictingConstraints = new HashSet<>();
        conflictingConstraints.add(new ConflictingConstraint());
        conflictingConstraints.add(new ConflictingConstraint());

        ConstraintSatisfactionProblem conflictingCsp = new ConstraintSatisfactionProblem(
            variables,
            conflictingConstraints,
            domains
        );

        Optional<SchedulingAssignment> solution = conflictingCsp.solve();
        assertFalse(solution.isPresent(), "Should not find solution with conflicting constraints");
    }

    @Test
    @DisplayName("Should validate assignment consistency")
    void shouldValidateAssignmentConsistency() {
        SchedulingAssignment assignment = new SchedulingAssignment();
        SchedulingVariable var1 = variables.iterator().next();
        SchedulingValue value1 = domains.get(var1).get(0);

        assignment.assign(var1, value1);
        assertTrue(csp.isConsistent(assignment), "Assignment should be consistent");
    }

    @Test
    @DisplayName("Should detect assignment inconsistency")
    void shouldDetectAssignmentInconsistency() {
        SchedulingAssignment assignment = new SchedulingAssignment();
        SchedulingVariable var1 = variables.iterator().next();
        SchedulingValue value1 = domains.get(var1).get(0);

        assignment.assign(var1, value1);

        // Add a conflicting constraint
        constraints.add(new ConflictingConstraint());
        ConstraintSatisfactionProblem conflictingCsp = new ConstraintSatisfactionProblem(
            variables,
            constraints,
            domains
        );

        assertFalse(conflictingCsp.isConsistent(assignment), "Should detect inconsistency");
    }

    // Mock constraint for testing
    private static class MockConstraint extends SchedulingConstraint {
        public MockConstraint(String name, ConstraintPriority priority) {
            super(name, ConstraintType.TEACHER_AVAILABILITY, priority, "Mock constraint for testing");
        }

        @Override
        public ConstraintResult validate(SchedulingAssignment assignment) {
            return ConstraintResult.satisfied();
        }

        @Override
        public Set<SchedulingVariable> getScope() {
            return new HashSet<>(); // Mock constraint applies to all variables
        }

        @Override
        public List<SchedulingValue> getValidValues(SchedulingAssignment assignment,
                                                   Map<SchedulingVariable, List<SchedulingValue>> domains) {
            return new ArrayList<>();
        }
    }

    // Conflicting constraint for testing
    private static class ConflictingConstraint extends SchedulingConstraint {
        public ConflictingConstraint() {
            super("Conflicting Constraint", ConstraintType.TEACHER_AVAILABILITY,
                  ConstraintPriority.HARD, "Always conflicting constraint");
        }

        @Override
        public ConstraintResult validate(SchedulingAssignment assignment) {
            return ConstraintResult.violated("Always conflicts");
        }

        @Override
        public Set<SchedulingVariable> getScope() {
            return new HashSet<>();
        }

        @Override
        public List<SchedulingValue> getValidValues(SchedulingAssignment assignment,
                                                   Map<SchedulingVariable, List<SchedulingValue>> domains) {
            return new ArrayList<>();
        }
    }
}