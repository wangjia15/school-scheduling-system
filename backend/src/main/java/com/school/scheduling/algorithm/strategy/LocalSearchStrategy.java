package com.school.scheduling.algorithm.strategy;

import com.school.scheduling.algorithm.ConstraintSatisfactionProblem;
import com.school.scheduling.domain.constraint.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Local search algorithm implementation for school scheduling optimization.
 * Uses hill climbing and simulated annealing to find local optima.
 */
public class LocalSearchStrategy {

    private final int maxIterations;
    private final int maxNonImprovingIterations;
    private final double initialTemperature;
    private final double coolingRate;
    private final boolean useSimulatedAnnealing;

    public LocalSearchStrategy() {
        this(1000, 100, 100.0, 0.995, true);
    }

    public LocalSearchStrategy(int maxIterations, int maxNonImprovingIterations,
                             double initialTemperature, double coolingRate, boolean useSimulatedAnnealing) {
        this.maxIterations = maxIterations;
        this.maxNonImprovingIterations = maxNonImprovingIterations;
        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
        this.useSimulatedAnnealing = useSimulatedAnnealing;
    }

    /**
     * Solves CSP using local search algorithm.
     */
    public Optional<SchedulingAssignment> solve(ConstraintSatisfactionProblem csp) {
        // Generate initial solution
        SchedulingAssignment currentSolution = generateInitialSolution(csp);
        double currentFitness = calculateFitness(currentSolution, csp);

        SchedulingAssignment bestSolution = currentSolution.copy();
        double bestFitness = currentFitness;

        double temperature = initialTemperature;
        int nonImprovingIterations = 0;

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Generate neighbor solution
            SchedulingAssignment neighborSolution = generateNeighbor(currentSolution, csp);
            double neighborFitness = calculateFitness(neighborSolution, csp);

            // Acceptance criteria
            boolean acceptNeighbor = false;
            if (useSimulatedAnnealing) {
                acceptNeighbor = acceptWithProbability(currentFitness, neighborFitness, temperature);
            } else {
                acceptNeighbor = neighborFitness > currentFitness;
            }

            if (acceptNeighbor) {
                currentSolution = neighborSolution;
                currentFitness = neighborFitness;

                if (neighborFitness > bestFitness) {
                    bestSolution = neighborSolution.copy();
                    bestFitness = neighborFitness;
                    nonImprovingIterations = 0;
                } else {
                    nonImprovingIterations++;
                }
            } else {
                nonImprovingIterations++;
            }

            // Cool down temperature for simulated annealing
            if (useSimulatedAnnealing) {
                temperature *= coolingRate;
            }

            // Check termination conditions
            if (bestFitness >= 0.95 || nonImprovingIterations >= maxNonImprovingIterations) {
                break;
            }
        }

        return Optional.ofNullable(bestSolution);
    }

    private SchedulingAssignment generateInitialSolution(ConstraintSatisfactionProblem csp) {
        SchedulingAssignment assignment = new SchedulingAssignment();
        Map<SchedulingVariable, List<SchedulingValue>> domains = csp.getDomains();

        List<SchedulingVariable> variables = new ArrayList<>(domains.keySet());
        Collections.shuffle(variables);

        for (SchedulingVariable variable : variables) {
            List<SchedulingValue> possibleValues = domains.get(variable);
            if (!possibleValues.isEmpty()) {
                // Try to assign the best possible value first
                SchedulingValue bestValue = findBestValueForVariable(variable, possibleValues, assignment, csp);
                assignment.assign(variable, bestValue);
            }
        }

        return assignment;
    }

    private SchedulingValue findBestValueForVariable(SchedulingVariable variable, List<SchedulingValue> values,
                                                    SchedulingAssignment assignment, ConstraintSatisfactionProblem csp) {
        SchedulingValue bestValue = values.get(0);
        double bestScore = Double.NEGATIVE_INFINITY;

        for (SchedulingValue value : values) {
            assignment.assign(variable, value);
            double score = evaluatePartialAssignment(assignment, csp);
            assignment.unassign(variable);

            if (score > bestScore) {
                bestScore = score;
                bestValue = value;
            }
        }

        return bestValue;
    }

    private double evaluatePartialAssignment(SchedulingAssignment assignment, ConstraintSatisfactionProblem csp) {
        double score = 0.0;
        int constraintCount = 0;

        for (SchedulingConstraint constraint : csp.getConstraints()) {
            // Only evaluate constraints that involve assigned variables
            if (hasAssignedVariablesInScope(constraint, assignment)) {
                ConstraintResult result = constraint.validate(assignment);
                if (result.isSatisfied()) {
                    score += 1.0;
                }
                constraintCount++;
            }
        }

        return constraintCount > 0 ? score / constraintCount : 0.0;
    }

    private boolean hasAssignedVariablesInScope(SchedulingConstraint constraint, SchedulingAssignment assignment) {
        Set<SchedulingVariable> scope = constraint.getScope();
        for (SchedulingVariable variable : scope) {
            if (assignment.isAssigned(variable)) {
                return true;
            }
        }
        return false;
    }

    private SchedulingAssignment generateNeighbor(SchedulingAssignment current, ConstraintSatisfactionProblem csp) {
        SchedulingAssignment neighbor = current.copy();
        Map<SchedulingVariable, List<SchedulingValue>> domains = csp.getDomains();

        List<SchedulingVariable> assignedVariables = new ArrayList<>(current.getAssignments().keySet());
        if (assignedVariables.isEmpty()) {
            return neighbor;
        }

        // Select a random variable to modify
        SchedulingVariable variable = assignedVariables.get(ThreadLocalRandom.current().nextInt(assignedVariables.size()));
        List<SchedulingValue> possibleValues = new ArrayList<>(domains.get(variable));

        // Remove current value from options
        SchedulingValue currentValue = current.getValue(variable);
        possibleValues.remove(currentValue);

        if (!possibleValues.isEmpty()) {
            // Try a different value
            SchedulingValue newValue = possibleValues.get(ThreadLocalRandom.current().nextInt(possibleValues.size()));
            neighbor.assign(variable, newValue);
        }

        return neighbor;
    }

    private double calculateFitness(SchedulingAssignment assignment, ConstraintSatisfactionProblem csp) {
        double fitness = 0.0;

        // Primary: constraint satisfaction
        double constraintScore = calculateConstraintSatisfaction(assignment, csp);
        fitness += constraintScore * 0.6;

        // Secondary: solution completeness
        double completenessScore = calculateCompleteness(assignment, csp);
        fitness += completenessScore * 0.3;

        // Tertiary: resource efficiency
        double efficiencyScore = calculateResourceEfficiency(assignment, csp);
        fitness += efficiencyScore * 0.1;

        return Math.max(0, Math.min(1, fitness));
    }

    private double calculateConstraintSatisfaction(SchedulingAssignment assignment,
                                                 ConstraintSatisfactionProblem csp) {
        int satisfiedConstraints = 0;
        int applicableConstraints = 0;

        for (SchedulingConstraint constraint : csp.getConstraints()) {
            if (hasAssignedVariablesInScope(constraint, assignment)) {
                ConstraintResult result = constraint.validate(assignment);
                if (result.isSatisfied()) {
                    satisfiedConstraints++;
                }
                applicableConstraints++;
            }
        }

        return applicableConstraints > 0 ? (double) satisfiedConstraints / applicableConstraints : 1.0;
    }

    private double calculateCompleteness(SchedulingAssignment assignment, ConstraintSatisfactionProblem csp) {
        int assignedVariables = assignment.getAssignments().size();
        int totalVariables = csp.getVariables().size();

        return totalVariables > 0 ? (double) assignedVariables / totalVariables : 1.0;
    }

    private double calculateResourceEfficiency(SchedulingAssignment assignment, ConstraintSatisfactionProblem csp) {
        // Calculate efficiency based on preference scores and resource distribution
        Map<SchedulingVariable, SchedulingValue> assignments = assignment.getAssignments();

        if (assignments.isEmpty()) return 0.0;

        double totalPreference = assignments.values().stream()
            .mapToDouble(SchedulingValue::getPreferenceScore)
            .sum();

        // Calculate variance in resource usage (lower variance is better)
        Map<ValueType, Integer> resourceUsage = new EnumMap<>(ValueType.class);
        assignments.values().forEach(value ->
            resourceUsage.merge(value.getType(), 1, Integer::sum)
        );

        double variance = calculateVariance(resourceUsage.values());
        double efficiency = Math.max(0, 1.0 - variance / 100.0); // Normalize variance

        return (totalPreference / assignments.size() + efficiency) / 2.0;
    }

    private double calculateVariance(Collection<Integer> values) {
        if (values.size() < 2) return 0.0;

        double mean = values.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = values.stream()
            .mapToDouble(value -> Math.pow(value - mean, 2))
            .average()
            .orElse(0.0);

        return variance;
    }

    private boolean acceptWithProbability(double currentFitness, double neighborFitness, double temperature) {
        if (neighborFitness > currentFitness) {
            return true; // Always accept better solutions
        }

        // Accept worse solutions with probability based on temperature
        double delta = currentFitness - neighborFitness;
        double probability = Math.exp(-delta / temperature);

        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    // Tabu Search variant for additional local search capability
    public Optional<SchedulingAssignment> solveWithTabuSearch(ConstraintSatisfactionProblem csp) {
        SchedulingAssignment currentSolution = generateInitialSolution(csp);
        double currentFitness = calculateFitness(currentSolution, csp);

        SchedulingAssignment bestSolution = currentSolution.copy();
        double bestFitness = currentFitness;

        TabuList tabuList = new TabuList(100, 50); // 100 max size, 50 tenure

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Generate all neighbors
            List<SchedulingAssignment> neighbors = generateAllNeighbors(currentSolution, csp);

            // Find best non-tabu neighbor
            SchedulingAssignment bestNeighbor = null;
            double bestNeighborFitness = Double.NEGATIVE_INFINITY;
            Move bestMove = null;

            for (SchedulingAssignment neighbor : neighbors) {
                Move move = createMove(currentSolution, neighbor);
                if (!tabuList.isTabu(move)) {
                    double fitness = calculateFitness(neighbor, csp);
                    if (fitness > bestNeighborFitness ||
                        (fitness == bestNeighborFitness && fitness > bestFitness)) { // Aspiration criterion
                        bestNeighbor = neighbor;
                        bestNeighborFitness = fitness;
                        bestMove = move;
                    }
                }
            }

            if (bestNeighbor == null) {
                break; // No valid moves
            }

            currentSolution = bestNeighbor;
            currentFitness = bestNeighborFitness;

            if (bestNeighborFitness > bestFitness) {
                bestSolution = bestNeighbor.copy();
                bestFitness = bestNeighborFitness;
            }

            tabuList.addTabuMove(bestMove);
        }

        return Optional.ofNullable(bestSolution);
    }

    private List<SchedulingAssignment> generateAllNeighbors(SchedulingAssignment current,
                                                          ConstraintSatisfactionProblem csp) {
        List<SchedulingAssignment> neighbors = new ArrayList<>();
        Map<SchedulingVariable, List<SchedulingValue>> domains = csp.getDomains();

        // Generate neighbors by changing one variable assignment at a time
        for (Map.Entry<SchedulingVariable, SchedulingValue> entry : current.getAssignments().entrySet()) {
            SchedulingVariable variable = entry.getKey();
            SchedulingValue currentValue = entry.getValue();

            for (SchedulingValue newValue : domains.get(variable)) {
                if (!newValue.equals(currentValue)) {
                    SchedulingAssignment neighbor = current.copy();
                    neighbor.assign(variable, newValue);
                    neighbors.add(neighbor);
                }
            }
        }

        // Limit neighbor size for performance
        if (neighbors.size() > 50) {
            Collections.shuffle(neighbors);
            return neighbors.subList(0, 50);
        }

        return neighbors;
    }

    private Move createMove(SchedulingAssignment from, SchedulingAssignment to) {
        for (Map.Entry<SchedulingVariable, SchedulingValue> entry : from.getAssignments().entrySet()) {
            SchedulingVariable variable = entry.getKey();
            SchedulingValue fromValue = entry.getValue();
            SchedulingValue toValue = to.getValue(variable);

            if (!toValue.equals(fromValue)) {
                return new Move(variable, fromValue, toValue);
            }
        }
        return null;
    }

    // Helper classes
    private static class TabuList {
        private final Queue<Move> tabuMoves;
        private final Map<Move, Integer> tenures;
        private final int maxSize;
        private final int tenure;

        public TabuList(int maxSize, int tenure) {
            this.tabuMoves = new LinkedList<>();
            this.tenures = new HashMap<>();
            this.maxSize = maxSize;
            this.tenure = tenure;
        }

        public void addTabuMove(Move move) {
            if (move == null) return;

            tabuMoves.offer(move);
            tenures.put(move, tenure);

            // Update tenures and remove expired moves
            Iterator<Map.Entry<Move, Integer>> iterator = tenures.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Move, Integer> entry = iterator.next();
                entry.setValue(entry.getValue() - 1);
                if (entry.getValue() <= 0) {
                    iterator.remove();
                }
            }

            // Remove oldest if exceeds max size
            while (tabuMoves.size() > maxSize) {
                Move oldest = tabuMoves.poll();
                tenures.remove(oldest);
            }
        }

        public boolean isTabu(Move move) {
            return move != null && tenures.containsKey(move);
        }
    }

    private static class Move {
        private final SchedulingVariable variable;
        private final SchedulingValue fromValue;
        private final SchedulingValue toValue;

        public Move(SchedulingVariable variable, SchedulingValue fromValue, SchedulingValue toValue) {
            this.variable = variable;
            this.fromValue = fromValue;
            this.toValue = toValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Move move = (Move) obj;
            return Objects.equals(variable, move.variable) &&
                   Objects.equals(toValue, move.toValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variable, toValue);
        }
    }

    // Getters for configuration
    public int getMaxIterations() { return maxIterations; }
    public int getMaxNonImprovingIterations() { return maxNonImprovingIterations; }
    public double getInitialTemperature() { return initialTemperature; }
    public double getCoolingRate() { return coolingRate; }
    public boolean isUseSimulatedAnnealing() { return useSimulatedAnnealing; }
}