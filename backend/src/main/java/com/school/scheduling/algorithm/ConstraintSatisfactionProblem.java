package com.school.scheduling.algorithm;

import com.school.scheduling.domain.constraint.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Core Constraint Satisfaction Problem (CSP) solver for school scheduling.
 * Implements backtracking with forward checking and various heuristics.
 */
public class ConstraintSatisfactionProblem {

    private final Set<SchedulingVariable> variables;
    private final Set<SchedulingConstraint> constraints;
    private final Map<SchedulingVariable, List<SchedulingValue>> domains;
    private final Map<SchedulingVariable, Set<SchedulingConstraint>> variableConstraints;

    // Performance tracking
    private int nodesExplored = 0;
    private int backtracks = 0;
    private long startTime = 0;
    private long endTime = 0;

    public ConstraintSatisfactionProblem(Set<SchedulingVariable> variables, Set<SchedulingConstraint> constraints,
                                        Map<SchedulingVariable, List<SchedulingValue>> domains) {
        this.variables = new HashSet<>(variables);
        this.constraints = new HashSet<>(constraints);
        this.domains = new ConcurrentHashMap<>(domains);
        this.variableConstraints = buildVariableConstraints();
    }

    /**
     * Solves the CSP using backtracking with forward checking.
     */
    public Optional<SchedulingAssignment> solve() {
        return solveWithStrategy(SolvingStrategy.BACKTRACKING_FORWARD_CHECKING);
    }

    /**
     * Solves the CSP using a specific strategy.
     */
    public Optional<SchedulingAssignment> solveWithStrategy(SolvingStrategy strategy) {
        resetPerformanceTracking();
        startTime = System.currentTimeMillis();

        try {
            switch (strategy) {
                case BACKTRACKING_FORWARD_CHECKING:
                    return backtrackWithForwardChecking(new SchedulingAssignment());
                case BACKTRACKING_AC3:
                    return backtrackWithAC3(new SchedulingAssignment());
                case MIN_CONFLICTS:
                    return minConflictsSearch();
                default:
                    return backtrackWithForwardChecking(new SchedulingAssignment());
            }
        } finally {
            endTime = System.currentTimeMillis();
        }
    }

    /**
     * Backtracking algorithm with forward checking.
     */
    private Optional<SchedulingAssignment> backtrackWithForwardChecking(SchedulingAssignment assignment) {
        nodesExplored++;

        // Check if assignment is complete
        if (assignment.isComplete(variables)) {
            return Optional.of(assignment);
        }

        // Select unassigned variable using MRV heuristic
        SchedulingVariable variable = selectUnassignedVariableMRV(assignment);
        if (variable == null) {
            return Optional.empty();
        }

        // Try values in order of least constraining value
        List<SchedulingValue> orderedValues = orderDomainValuesLCV(assignment, variable, domains.get(variable));

        for (SchedulingValue value : orderedValues) {
            // Check if value is consistent with current assignment
            if (isConsistent(assignment, variable, value)) {
                // Assign value
                SchedulingAssignment newAssignment = assignment.copy();
                newAssignment.assign(variable, value);

                // Forward checking
                Map<SchedulingVariable, List<SchedulingValue>> newDomains = forwardChecking(newAssignment, variable, value);

                if (newDomains != null) { // No domain wipeout
                    // Recurse
                    Optional<SchedulingAssignment> result = backtrackWithForwardChecking(newAssignment);
                    if (result.isPresent()) {
                        return result;
                    }
                }

                backtracks++;
            }
        }

        return Optional.empty();
    }

    /**
     * Backtracking algorithm with AC-3 preprocessing.
     */
    private Optional<SchedulingAssignment> backtrackWithAC3(SchedulingAssignment assignment) {
        nodesExplored++;

        if (assignment.isComplete(variables)) {
            return Optional.of(assignment);
        }

        // AC-3 preprocessing
        Map<SchedulingVariable, List<SchedulingValue>> currentDomains = getCurrentDomains(assignment);
        if (!ac3(assignment, currentDomains)) {
            return Optional.empty();
        }

        SchedulingVariable variable = selectUnassignedVariableMRV(assignment);
        if (variable == null) {
            return Optional.empty();
        }

        List<SchedulingValue> orderedValues = orderDomainValuesLCV(assignment, variable, currentDomains.get(variable));

        for (SchedulingValue value : orderedValues) {
            if (isConsistent(assignment, variable, value)) {
                SchedulingAssignment newAssignment = assignment.copy();
                newAssignment.assign(variable, value);

                Optional<SchedulingAssignment> result = backtrackWithAC3(newAssignment);
                if (result.isPresent()) {
                    return result;
                }

                backtracks++;
            }
        }

        return Optional.empty();
    }

    /**
     * Min-conflicts local search algorithm.
     */
    private Optional<SchedulingAssignment> minConflictsSearch() {
        // Start with a random complete assignment
        SchedulingAssignment current = generateRandomAssignment();
        int maxSteps = variables.size() * 100; // Limit steps to prevent infinite loop

        for (int i = 0; i < maxSteps; i++) {
            nodesExplored++;

            if (isSolution(current)) {
                return Optional.of(current);
            }

            // Select variable with most conflicts
            SchedulingVariable conflictedVar = selectConflictedVariable(current);
            if (conflictedVar == null) {
                break;
            }

            // Assign value that minimizes conflicts
            SchedulingValue bestValue = selectMinConflictsValue(current, conflictedVar);
            if (bestValue != null) {
                current.assign(conflictedVar, bestValue);
            }
        }

        return Optional.empty();
    }

    /**
     * AC-3 arc consistency algorithm.
     */
    private boolean ac3(SchedulingAssignment assignment, Map<SchedulingVariable, List<SchedulingValue>> currentDomains) {
        Queue<Arc> queue = new LinkedList<>();

        // Initialize queue with all arcs
        for (SchedulingConstraint constraint : constraints) {
            Set<SchedulingVariable> scope = constraint.getScope();
            List<SchedulingVariable> scopeList = new ArrayList<>(scope);
            for (int i = 0; i < scopeList.size(); i++) {
                for (int j = 0; j < scopeList.size(); j++) {
                    if (i != j) {
                        queue.add(new Arc(scopeList.get(i), scopeList.get(j)));
                    }
                }
            }
        }

        while (!queue.isEmpty()) {
            Arc arc = queue.poll();
            if (revise(assignment, arc.xi, arc.xj, currentDomains)) {
                if (currentDomains.get(arc.xi).isEmpty()) {
                    return false; // Domain wipeout
                }
                // Add all related arcs back to queue
                for (SchedulingVariable xk : variables) {
                    if (!xk.equals(arc.xj)) {
                        queue.add(new Arc(xk, arc.xi));
                    }
                }
            }
        }

        return true;
    }

    /**
     * Revise domain for arc consistency.
     */
    private boolean revise(SchedulingAssignment assignment, SchedulingVariable xi, SchedulingVariable xj,
                          Map<SchedulingVariable, List<SchedulingValue>> currentDomains) {
        boolean revised = false;
        List<SchedulingValue> xiDomain = new ArrayList<>(currentDomains.get(xi));

        for (SchedulingValue x : xiDomain) {
            boolean hasSupport = false;
            for (SchedulingValue y : currentDomains.get(xj)) {
                SchedulingAssignment testAssignment = assignment.copy();
                testAssignment.assign(xi, x);
                testAssignment.assign(xj, y);

                if (isConsistent(testAssignment)) {
                    hasSupport = true;
                    break;
                }
            }

            if (!hasSupport) {
                currentDomains.get(xi).remove(x);
                revised = true;
            }
        }

        return revised;
    }

    /**
     * Forward checking: reduces domains of unassigned variables.
     */
    private Map<SchedulingVariable, List<SchedulingValue>> forwardChecking(SchedulingAssignment assignment,
                                                                         SchedulingVariable assignedVar,
                                                                         SchedulingValue assignedValue) {
        Map<SchedulingVariable, List<SchedulingValue>> newDomains = new HashMap<>();

        // Copy current domains
        for (Map.Entry<SchedulingVariable, List<SchedulingValue>> entry : domains.entrySet()) {
            newDomains.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        // Remove assigned value from domain
        newDomains.get(assignedVar).removeIf(v -> v.equals(assignedValue));

        // Check constraints for unassigned variables
        for (SchedulingVariable unassignedVar : variables) {
            if (!assignment.isAssigned(unassignedVar) && !unassignedVar.equals(assignedVar)) {
                Set<SchedulingConstraint> sharedConstraints = new HashSet<>(variableConstraints.get(assignedVar));
                sharedConstraints.retainAll(variableConstraints.get(unassignedVar));

                for (SchedulingConstraint constraint : sharedConstraints) {
                    List<SchedulingValue> validValues = new ArrayList<>();
                    for (SchedulingValue value : newDomains.get(unassignedVar)) {
                        SchedulingAssignment testAssignment = assignment.copy();
                        testAssignment.assign(assignedVar, assignedValue);
                        testAssignment.assign(unassignedVar, value);

                        if (isConsistent(testAssignment, constraint)) {
                            validValues.add(value);
                        }
                    }

                    if (validValues.isEmpty()) {
                        return null; // Domain wipeout
                    }

                    newDomains.put(unassignedVar, validValues);
                }
            }
        }

        return newDomains;
    }

    /**
     * Selects unassigned variable using Minimum Remaining Values (MRV) heuristic.
     */
    private SchedulingVariable selectUnassignedVariableMRV(SchedulingAssignment assignment) {
        return variables.stream()
                .filter(v -> !assignment.isAssigned(v))
                .min((v1, v2) -> {
                    int domain1Size = getCurrentDomainSize(assignment, v1);
                    int domain2Size = getCurrentDomainSize(assignment, v2);
                    return Integer.compare(domain1Size, domain2Size);
                })
                .orElse(null);
    }

    /**
     * Orders domain values using Least Constraining Value (LCV) heuristic.
     */
    private List<SchedulingValue> orderDomainValuesLCV(SchedulingAssignment assignment,
                                                      SchedulingVariable variable,
                                                      List<SchedulingValue> domain) {
        Map<SchedulingValue, Integer> constraintCount = new HashMap<>();

        for (SchedulingValue value : domain) {
            int count = 0;
            for (SchedulingVariable otherVar : variables) {
                if (!otherVar.equals(variable) && !assignment.isAssigned(otherVar)) {
                    if (shareConstraints(variable, otherVar)) {
                        count++;
                    }
                }
            }
            constraintCount.put(value, count);
        }

        return domain.stream()
                .sorted(Comparator.comparingInt(v -> constraintCount.get(v)))
                .collect(Collectors.toList());
    }

    /**
     * Checks if assignment is consistent with all constraints.
     */
    private boolean isConsistent(SchedulingAssignment assignment) {
        for (SchedulingConstraint constraint : constraints) {
            if (!isConsistent(assignment, constraint)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if assignment is consistent with a specific constraint.
     */
    private boolean isConsistent(SchedulingAssignment assignment, SchedulingConstraint constraint) {
        // Only check constraints where all variables in scope are assigned
        Set<SchedulingVariable> scope = constraint.getScope();
        for (SchedulingVariable var : scope) {
            if (!assignment.isAssigned(var)) {
                return true; // Not all variables assigned yet
            }
        }

        ConstraintResult result = constraint.validate(assignment);
        return constraint.isHardConstraint() ? result.isSatisfied() : true;
    }

    /**
     * Checks if assigning a value to a variable is consistent.
     */
    private boolean isConsistent(SchedulingAssignment assignment, SchedulingVariable variable, SchedulingValue value) {
        SchedulingAssignment testAssignment = assignment.copy();
        testAssignment.assign(variable, value);
        return isConsistent(testAssignment);
    }

    /**
     * Checks if the assignment is a complete solution.
     */
    private boolean isSolution(SchedulingAssignment assignment) {
        if (!assignment.isComplete(variables)) {
            return false;
        }

        // Check all constraints
        for (SchedulingConstraint constraint : constraints) {
            ConstraintResult result = constraint.validate(assignment);
            if (constraint.isHardConstraint() && !result.isSatisfied()) {
                return false;
            }
        }

        return true;
    }

    // Helper methods
    private Map<SchedulingVariable, Set<SchedulingConstraint>> buildVariableConstraints() {
        Map<SchedulingVariable, Set<SchedulingConstraint>> map = new HashMap<>();
        variables.forEach(v -> map.put(v, new HashSet<>()));

        for (SchedulingConstraint constraint : constraints) {
            for (SchedulingVariable variable : constraint.getScope()) {
                map.get(variable).add(constraint);
            }
        }

        return map;
    }

    private int getCurrentDomainSize(SchedulingAssignment assignment, SchedulingVariable variable) {
        if (assignment.isAssigned(variable)) {
            return 1;
        }
        return domains.get(variable).size();
    }

    private Map<SchedulingVariable, List<SchedulingValue>> getCurrentDomains(SchedulingAssignment assignment) {
        Map<SchedulingVariable, List<SchedulingValue>> currentDomains = new HashMap<>();
        for (SchedulingVariable variable : variables) {
            if (assignment.isAssigned(variable)) {
                currentDomains.put(variable, Collections.singletonList(assignment.getValue(variable)));
            } else {
                currentDomains.put(variable, new ArrayList<>(domains.get(variable)));
            }
        }
        return currentDomains;
    }

    private boolean shareConstraints(SchedulingVariable v1, SchedulingVariable v2) {
        Set<SchedulingConstraint> constraints1 = variableConstraints.get(v1);
        Set<SchedulingConstraint> constraints2 = variableConstraints.get(v2);
        constraints1.retainAll(constraints2);
        return !constraints1.isEmpty();
    }

    private SchedulingVariable selectConflictedVariable(SchedulingAssignment assignment) {
        return variables.stream()
                .filter(v -> hasConflicts(assignment, v))
                .findFirst()
                .orElse(null);
    }

    private boolean hasConflicts(SchedulingAssignment assignment, SchedulingVariable variable) {
        SchedulingValue currentValue = assignment.getValue(variable);
        if (currentValue == null) return false;

        for (SchedulingValue alternativeValue : domains.get(variable)) {
            if (!alternativeValue.equals(currentValue)) {
                SchedulingAssignment testAssignment = assignment.copy();
                testAssignment.assign(variable, alternativeValue);
                if (countConflicts(testAssignment) < countConflicts(assignment)) {
                    return true;
                }
            }
        }

        return false;
    }

    private int countConflicts(SchedulingAssignment assignment) {
        int conflicts = 0;
        for (SchedulingConstraint constraint : constraints) {
            ConstraintResult result = constraint.validate(assignment);
            if (!result.isSatisfied()) {
                conflicts += result.getViolationScore();
            }
        }
        return conflicts;
    }

    private SchedulingValue selectMinConflictsValue(SchedulingAssignment assignment, SchedulingVariable variable) {
        return domains.get(variable).stream()
                .min(Comparator.comparingDouble(value -> {
                    SchedulingAssignment testAssignment = assignment.copy();
                    testAssignment.assign(variable, value);
                    return countConflicts(testAssignment);
                }))
                .orElse(null);
    }

    private SchedulingAssignment generateRandomAssignment() {
        SchedulingAssignment assignment = new SchedulingAssignment();
        Random random = new Random();

        for (SchedulingVariable variable : variables) {
            List<SchedulingValue> domain = domains.get(variable);
            if (!domain.isEmpty()) {
                SchedulingValue value = domain.get(random.nextInt(domain.size()));
                assignment.assign(variable, value);
            }
        }

        return assignment;
    }

    private void resetPerformanceTracking() {
        nodesExplored = 0;
        backtracks = 0;
        startTime = 0;
        endTime = 0;
    }

    // Performance metrics
    public int getNodesExplored() {
        return nodesExplored;
    }

    public int getBacktracks() {
        return backtracks;
    }

    public long getExecutionTimeMs() {
        return endTime - startTime;
    }

    public String getPerformanceSummary() {
        return String.format("Nodes: %d, Backtracks: %d, Time: %dms",
                nodesExplored, backtracks, getExecutionTimeMs());
    }

    // Helper classes
    private static class Arc {
        final SchedulingVariable xi;
        final SchedulingVariable xj;

        Arc(SchedulingVariable xi, SchedulingVariable xj) {
            this.xi = xi;
            this.xj = xj;
        }
    }

    public enum SolvingStrategy {
        BACKTRACKING_FORWARD_CHECKING,
        BACKTRACKING_AC3,
        MIN_CONFLICTS
    }
}