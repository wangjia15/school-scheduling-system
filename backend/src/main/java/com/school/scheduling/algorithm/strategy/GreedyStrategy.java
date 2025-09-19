package com.school.scheduling.algorithm.strategy;

import com.school.scheduling.algorithm.ConstraintSatisfactionProblem;
import com.school.scheduling.domain.constraint.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Greedy algorithm implementation for school scheduling optimization.
 * Uses various heuristics to make locally optimal choices at each step.
 */
public class GreedyStrategy {

    private final HeuristicType heuristicType;
    private final boolean useForwardChecking;
    private final int maxAttemptsPerVariable;

    public GreedyStrategy() {
        this(HeuristicType.MRV_DEGREE, true, 10);
    }

    public GreedyStrategy(HeuristicType heuristicType, boolean useForwardChecking, int maxAttemptsPerVariable) {
        this.heuristicType = heuristicType;
        this.useForwardChecking = useForwardChecking;
        this.maxAttemptsPerVariable = maxAttemptsPerVariable;
    }

    /**
     * Solves CSP using greedy algorithm with specified heuristic.
     */
    public Optional<SchedulingAssignment> solve(ConstraintSatisfactionProblem csp) {
        SchedulingAssignment assignment = new SchedulingAssignment();
        Map<SchedulingVariable, List<SchedulingValue>> currentDomains = new HashMap<>();

        // Initialize domains
        for (Map.Entry<SchedulingVariable, List<SchedulingValue>> entry : csp.getDomains().entrySet()) {
            currentDomains.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        return greedyAssignment(assignment, currentDomains, csp);
    }

    private Optional<SchedulingAssignment> greedyAssignment(SchedulingAssignment assignment,
                                                         Map<SchedulingVariable, List<SchedulingValue>> domains,
                                                         ConstraintSatisfactionProblem csp) {
        // Order variables using selected heuristic
        List<SchedulingVariable> orderedVariables = orderVariablesByHeuristic(assignment, domains, csp);

        for (SchedulingVariable variable : orderedVariables) {
            if (assignment.isAssigned(variable)) {
                continue;
            }

            // Order values by preference
            List<SchedulingValue> orderedValues = orderValuesByPreference(assignment, variable, domains.get(variable), csp);

            boolean assigned = false;
            int attempts = 0;

            for (SchedulingValue value : orderedValues) {
                if (attempts >= maxAttemptsPerVariable) {
                    break;
                }

                // Check consistency
                if (isConsistent(assignment, variable, value, csp)) {
                    // Assign value
                    assignment.assign(variable, value);

                    // Forward checking if enabled
                    Map<SchedulingVariable, List<SchedulingValue>> newDomains = null;
                    if (useForwardChecking) {
                        newDomains = forwardChecking(assignment, variable, value, domains);
                    }

                    if (newDomains != null) { // No domain wipeout
                        assigned = true;
                        break;
                    } else {
                        // Backtrack assignment
                        assignment.unassign(variable);
                    }
                }

                attempts++;
            }

            if (!assigned) {
                // Try alternative approaches for this variable
                assigned = handleAssignmentFailure(variable, assignment, domains, csp);
                if (!assigned) {
                    return Optional.empty(); // No valid assignment found
                }
            }
        }

        return Optional.of(assignment);
    }

    private List<SchedulingVariable> orderVariablesByHeuristic(SchedulingAssignment assignment,
                                                               Map<SchedulingVariable, List<SchedulingValue>> domains,
                                                               ConstraintSatisfactionProblem csp) {
        List<SchedulingVariable> variables = new ArrayList<>(domains.keySet());

        switch (heuristicType) {
            case MRV:
                return orderVariablesByMRV(assignment, domains);
            case DEGREE:
                return orderVariablesByDegree(assignment, csp);
            case MRV_DEGREE:
                return orderVariablesByMRVDegree(assignment, domains, csp);
            case LEAST_CONSTRAINING_VALUE:
                return orderVariablesByLeastConstraining(assignment, domains, csp);
            case DOM_DEG:
                return orderVariablesByDomDeg(assignment, domains, csp);
            default:
                return variables;
        }
    }

    private List<SchedulingVariable> orderVariablesByMRV(SchedulingAssignment assignment,
                                                        Map<SchedulingVariable, List<SchedulingValue>> domains) {
        return domains.entrySet().stream()
            .filter(entry -> !assignment.isAssigned(entry.getKey()))
            .sorted((e1, e2) -> {
                int size1 = e1.getValue().size();
                int size2 = e2.getValue().size();
                return Integer.compare(size1, size2);
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private List<SchedulingVariable> orderVariablesByDegree(SchedulingAssignment assignment,
                                                           ConstraintSatisfactionProblem csp) {
        return csp.getVariables().stream()
            .filter(v -> !assignment.isAssigned(v))
            .sorted((v1, v2) -> {
                int degree1 = calculateVariableDegree(v1, csp);
                int degree2 = calculateVariableDegree(v2, csp);
                return Integer.compare(degree2, degree1); // Higher degree first
            })
            .collect(Collectors.toList());
    }

    private List<SchedulingVariable> orderVariablesByMRVDegree(SchedulingAssignment assignment,
                                                               Map<SchedulingVariable, List<SchedulingValue>> domains,
                                                               ConstraintSatisfactionProblem csp) {
        return domains.entrySet().stream()
            .filter(entry -> !assignment.isAssigned(entry.getKey()))
            .sorted((e1, e2) -> {
                SchedulingVariable v1 = e1.getKey();
                SchedulingVariable v2 = e2.getKey();

                int mrv1 = e1.getValue().size();
                int mrv2 = e2.getValue().size();
                int degree1 = calculateVariableDegree(v1, csp);
                int degree2 = calculateVariableDegree(v2, csp);

                if (mrv1 != mrv2) {
                    return Integer.compare(mrv1, mrv2);
                } else {
                    return Integer.compare(degree2, degree1);
                }
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private List<SchedulingVariable> orderVariablesByLeastConstraining(SchedulingAssignment assignment,
                                                                     Map<SchedulingVariable, List<SchedulingValue>> domains,
                                                                     ConstraintSatisfactionProblem csp) {
        return domains.entrySet().stream()
            .filter(entry -> !assignment.isAssigned(entry.getKey()))
            .sorted((e1, e2) -> {
                double score1 = calculateLeastConstrainingScore(e1.getKey(), assignment, csp);
                double score2 = calculateLeastConstrainingScore(e2.getKey(), assignment, csp);
                return Double.compare(score2, score1); // Higher score first
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private List<SchedulingVariable> orderVariablesByDomDeg(SchedulingAssignment assignment,
                                                            Map<SchedulingVariable, List<SchedulingValue>> domains,
                                                            ConstraintSatisfactionProblem csp) {
        return domains.entrySet().stream()
            .filter(entry -> !assignment.isAssigned(entry.getKey()))
            .sorted((e1, e2) -> {
                SchedulingVariable v1 = e1.getKey();
                SchedulingVariable v2 = e2.getKey();

                double ratio1 = calculateDomDegRatio(v1, e1.getValue().size(), csp);
                double ratio2 = calculateDomDegRatio(v2, e2.getValue().size(), csp);

                return Double.compare(ratio1, ratio2);
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private int calculateVariableDegree(SchedulingVariable variable, ConstraintSatisfactionProblem csp) {
        Set<SchedulingVariable> connectedVariables = new HashSet<>();

        for (SchedulingConstraint constraint : csp.getConstraints()) {
            Set<SchedulingVariable> scope = constraint.getScope();
            if (scope.contains(variable)) {
                connectedVariables.addAll(scope);
            }
        }

        connectedVariables.remove(variable);
        return connectedVariables.size();
    }

    private double calculateLeastConstrainingScore(SchedulingVariable variable,
                                                   SchedulingAssignment assignment,
                                                   ConstraintSatisfactionProblem csp) {
        double totalImpact = 0.0;
        int count = 0;

        // For each possible value, estimate its impact on other variables
        for (SchedulingConstraint constraint : csp.getConstraints()) {
            if (constraint.getScope().contains(variable)) {
                Set<SchedulingVariable> otherVariables = new HashSet<>(constraint.getScope());
                otherVariables.remove(variable);

                if (!otherVariables.isEmpty()) {
                    // Estimate how many choices this constraint removes from other variables
                    double impact = estimateConstraintImpact(variable, otherVariables, constraint, assignment, csp);
                    totalImpact += impact;
                    count++;
                }
            }
        }

        return count > 0 ? totalImpact / count : 0.0;
    }

    private double estimateConstraintImpact(SchedulingVariable variable, Set<SchedulingVariable> otherVariables,
                                         SchedulingConstraint constraint, SchedulingAssignment assignment,
                                         ConstraintSatisfactionProblem csp) {
        // This is a heuristic estimation of the constraint's impact
        // Lower impact is better (less constraining)
        double impact = 0.0;

        switch (constraint.getType()) {
            case TEACHER_CONFLICT:
                impact = 2.0; // High impact - affects teacher schedules
                break;
            case CLASSROOM_CAPACITY:
                impact = 1.5; // Medium impact - affects classroom usage
                break;
            case STUDENT_CONFLICT:
                impact = 1.8; // High impact - affects student schedules
                break;
            default:
                impact = 1.0; // Low impact
        }

        // Adjust based on number of affected variables
        impact *= Math.sqrt(otherVariables.size());

        return impact;
    }

    private double calculateDomDegRatio(SchedulingVariable variable, int domainSize,
                                      ConstraintSatisfactionProblem csp) {
        int degree = calculateVariableDegree(variable, csp);
        return degree > 0 ? (double) domainSize / degree : Double.MAX_VALUE;
    }

    private List<SchedulingValue> orderValuesByPreference(SchedulingAssignment assignment,
                                                          SchedulingVariable variable,
                                                          List<SchedulingValue> values,
                                                          ConstraintSatisfactionProblem csp) {
        return values.stream()
            .sorted((v1, v2) -> {
                double score1 = calculateValuePreferenceScore(assignment, variable, v1, csp);
                double score2 = calculateValuePreferenceScore(assignment, variable, v2, csp);
                return Double.compare(score2, score1); // Higher score first
            })
            .collect(Collectors.toList());
    }

    private double calculateValuePreferenceScore(SchedulingAssignment assignment,
                                               SchedulingVariable variable,
                                               SchedulingValue value,
                                               ConstraintSatisfactionProblem csp) {
        double score = value.getPreferenceScore(); // Base preference score

        // Add constraint satisfaction bonus
        assignment.assign(variable, value);
        double constraintScore = evaluateConstraintSatisfaction(assignment, csp);
        assignment.unassign(variable);

        // Combine scores
        return score * 0.7 + constraintScore * 0.3;
    }

    private double evaluateConstraintSatisfaction(SchedulingAssignment assignment, ConstraintSatisfactionProblem csp) {
        int satisfied = 0;
        int total = 0;

        for (SchedulingConstraint constraint : csp.getConstraints()) {
            ConstraintResult result = constraint.validate(assignment);
            if (result.isSatisfied()) {
                satisfied++;
            }
            total++;
        }

        return total > 0 ? (double) satisfied / total : 1.0;
    }

    private boolean isConsistent(SchedulingAssignment assignment, SchedulingVariable variable,
                                SchedulingValue value, ConstraintSatisfactionProblem csp) {
        // Create temporary assignment
        SchedulingAssignment testAssignment = assignment.copy();
        testAssignment.assign(variable, value);

        // Check all constraints
        for (SchedulingConstraint constraint : csp.getConstraints()) {
            ConstraintResult result = constraint.validate(testAssignment);
            if (!result.isSatisfied()) {
                return false;
            }
        }

        return true;
    }

    private Map<SchedulingVariable, List<SchedulingValue>> forwardChecking(SchedulingAssignment assignment,
                                                                         SchedulingVariable variable,
                                                                         SchedulingValue value,
                                                                         Map<SchedulingVariable, List<SchedulingValue>> currentDomains) {
        Map<SchedulingVariable, List<SchedulingValue>> newDomains = new HashMap<>();

        // Copy current domains
        for (Map.Entry<SchedulingVariable, List<SchedulingValue>> entry : currentDomains.entrySet()) {
            newDomains.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        // Remove inconsistent values from neighboring variables
        Set<SchedulingVariable> connectedVariables = findConnectedVariables(variable, csp);

        for (SchedulingVariable connectedVar : connectedVariables) {
            if (!assignment.isAssigned(connectedVar)) {
                List<SchedulingValue> consistentValues = new ArrayList<>();

                for (SchedulingValue connectedValue : newDomains.get(connectedVar)) {
                    SchedulingAssignment testAssignment = assignment.copy();
                    testAssignment.assign(variable, value);
                    testAssignment.assign(connectedVar, connectedValue);

                    if (isConsistent(testAssignment, connectedVar, connectedValue, csp)) {
                        consistentValues.add(connectedValue);
                    }
                }

                if (consistentValues.isEmpty()) {
                    return null; // Domain wipeout
                }

                newDomains.put(connectedVar, consistentValues);
            }
        }

        return newDomains;
    }

    private Set<SchedulingVariable> findConnectedVariables(SchedulingVariable variable,
                                                           ConstraintSatisfactionProblem csp) {
        Set<SchedulingVariable> connected = new HashSet<>();

        for (SchedulingConstraint constraint : csp.getConstraints()) {
            if (constraint.getScope().contains(variable)) {
                connected.addAll(constraint.getScope());
            }
        }

        connected.remove(variable);
        return connected;
    }

    private boolean handleAssignmentFailure(SchedulingVariable variable, SchedulingAssignment assignment,
                                         Map<SchedulingVariable, List<SchedulingValue>> domains,
                                         ConstraintSatisfactionProblem csp) {
        // Try relaxation strategies
        return tryAssignmentRelaxation(variable, assignment, domains, csp);
    }

    private boolean tryAssignmentRelaxation(SchedulingVariable variable, SchedulingAssignment assignment,
                                          Map<SchedulingVariable, List<SchedulingValue>> domains,
                                          ConstraintSatisfactionProblem csp) {
        // Find least constraining constraint to temporarily relax
        List<SchedulingConstraint> softConstraints = findSoftConstraints(csp);

        for (SchedulingConstraint constraint : softConstraints) {
            if (constraint.getScope().contains(variable)) {
                // Temporarily relax this constraint and try assignment again
                List<SchedulingValue> values = new ArrayList<>(domains.get(variable));
                Collections.shuffle(values);

                for (SchedulingValue value : values) {
                    assignment.assign(variable, value);
                    if (isConsistentExcept(assignment, variable, value, csp, constraint)) {
                        return true;
                    }
                    assignment.unassign(variable);
                }
            }
        }

        return false;
    }

    private List<SchedulingConstraint> findSoftConstraints(ConstraintSatisfactionProblem csp) {
        return csp.getConstraints().stream()
            .filter(c -> c.getPriority() == ConstraintPriority.SOFT)
            .collect(Collectors.toList());
    }

    private boolean isConsistentExcept(SchedulingAssignment assignment, SchedulingVariable variable,
                                      SchedulingValue value, ConstraintSatisfactionProblem csp,
                                      SchedulingConstraint ignoredConstraint) {
        SchedulingAssignment testAssignment = assignment.copy();
        testAssignment.assign(variable, value);

        for (SchedulingConstraint constraint : csp.getConstraints()) {
            if (constraint != ignoredConstraint) {
                ConstraintResult result = constraint.validate(testAssignment);
                if (!result.isSatisfied()) {
                    return false;
                }
            }
        }

        return true;
    }

    // Heuristic types
    public enum HeuristicType {
        MRV,                    // Minimum Remaining Values
        DEGREE,                 // Degree heuristic
        MRV_DEGREE,             // Combined MRV and Degree
        LEAST_CONSTRAINING_VALUE, // Least Constraining Value for variable ordering
        DOM_DEG                 // Domain/Degree ratio
    }

    // Getters for configuration
    public HeuristicType getHeuristicType() { return heuristicType; }
    public boolean isUseForwardChecking() { return useForwardChecking; }
    public int getMaxAttemptsPerVariable() { return maxAttemptsPerVariable; }
}