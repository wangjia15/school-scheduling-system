package com.school.scheduling.algorithm.strategy;

import com.school.scheduling.algorithm.ConstraintSatisfactionProblem;
import com.school.scheduling.domain.constraint.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Genetic algorithm implementation for school scheduling optimization.
 * Uses population-based evolution to find optimal solutions.
 */
public class GeneticAlgorithmStrategy {

    private final int populationSize;
    private final int generations;
    private final double mutationRate;
    private final double crossoverRate;
    private final double elitismRate;

    public GeneticAlgorithmStrategy() {
        this(100, 50, 0.02, 0.8, 0.1);
    }

    public GeneticAlgorithmStrategy(int populationSize, int generations,
                                  double mutationRate, double crossoverRate, double elitismRate) {
        this.populationSize = populationSize;
        this.generations = generations;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.elitismRate = elitismRate;
    }

    /**
     * Solves CSP using genetic algorithm.
     */
    public Optional<SchedulingAssignment> solve(ConstraintSatisfactionProblem csp) {
        // Initialize population
        List<SchedulingAssignment> population = initializePopulation(csp);

        SchedulingAssignment bestSolution = null;
        double bestFitness = Double.NEGATIVE_INFINITY;

        // Evolution loop
        for (int generation = 0; generation < generations; generation++) {
            // Evaluate fitness
            List<Double> fitnessScores = evaluatePopulation(population, csp);

            // Track best solution
            for (int i = 0; i < population.size(); i++) {
                if (fitnessScores.get(i) > bestFitness) {
                    bestFitness = fitnessScores.get(i);
                    bestSolution = population.get(i).copy();
                }
            }

            // Check for convergence
            if (bestFitness >= 0.95) {
                break;
            }

            // Selection
            List<SchedulingAssignment> selected = selection(population, fitnessScores);

            // Crossover and mutation
            population = evolvePopulation(selected, csp);
        }

        return Optional.ofNullable(bestSolution);
    }

    private List<SchedulingAssignment> initializePopulation(ConstraintSatisfactionProblem csp) {
        List<SchedulingAssignment> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            population.add(generateRandomAssignment(csp));
        }

        return population;
    }

    private SchedulingAssignment generateRandomAssignment(ConstraintSatisfactionProblem csp) {
        SchedulingAssignment assignment = new SchedulingAssignment();
        Map<SchedulingVariable, List<SchedulingValue>> domains = csp.getDomains();

        List<SchedulingVariable> variables = new ArrayList<>(domains.keySet());
        Collections.shuffle(variables);

        for (SchedulingVariable variable : variables) {
            List<SchedulingValue> possibleValues = new ArrayList<>(domains.get(variable));
            Collections.shuffle(possibleValues);

            // Try to assign a value that doesn't violate constraints
            for (SchedulingValue value : possibleValues) {
                assignment.assign(variable, value);
                if (isValidAssignment(assignment, csp)) {
                    break;
                }
                assignment.unassign(variable);
            }
        }

        return assignment;
    }

    private List<Double> evaluatePopulation(List<SchedulingAssignment> population,
                                         ConstraintSatisfactionProblem csp) {
        return population.stream()
            .map(assignment -> calculateFitness(assignment, csp))
            .collect(Collectors.toList());
    }

    private double calculateFitness(SchedulingAssignment assignment, ConstraintSatisfactionProblem csp) {
        double fitness = 0.0;

        // Constraint satisfaction (primary factor)
        double constraintSatisfaction = calculateConstraintSatisfaction(assignment, csp);
        fitness += constraintSatisfaction * 0.7;

        // Resource utilization (secondary factor)
        double resourceUtilization = calculateResourceUtilization(assignment, csp);
        fitness += resourceUtilization * 0.2;

        // Solution quality (tertiary factor)
        double solutionQuality = calculateSolutionQuality(assignment, csp);
        fitness += solutionQuality * 0.1;

        return Math.max(0, Math.min(1, fitness));
    }

    private double calculateConstraintSatisfaction(SchedulingAssignment assignment,
                                                 ConstraintSatisfactionProblem csp) {
        int satisfiedConstraints = 0;
        int totalConstraints = csp.getConstraints().size();

        for (SchedulingConstraint constraint : csp.getConstraints()) {
            ConstraintResult result = constraint.validate(assignment);
            if (result.isSatisfied()) {
                satisfiedConstraints++;
            }
        }

        return totalConstraints > 0 ? (double) satisfiedConstraints / totalConstraints : 1.0;
    }

    private double calculateResourceUtilization(SchedulingAssignment assignment,
                                              ConstraintSatisfactionProblem csp) {
        // Calculate how efficiently resources are being used
        Map<SchedulingVariable, SchedulingValue> assignments = assignment.getAssignments();

        long teacherCount = assignments.values().stream()
            .filter(v -> v.getType() == ValueType.TEACHER)
            .distinct()
            .count();

        long classroomCount = assignments.values().stream()
            .filter(v -> v.getType() == ValueType.CLASSROOM)
            .distinct()
            .count();

        double teacherUtilization = Math.min(1.0, teacherCount / 10.0); // Assume 10 teachers available
        double classroomUtilization = Math.min(1.0, classroomCount / 15.0); // Assume 15 classrooms available

        return (teacherUtilization + classroomUtilization) / 2.0;
    }

    private double calculateSolutionQuality(SchedulingAssignment assignment,
                                         ConstraintSatisfactionProblem csp) {
        // Calculate solution quality metrics
        double preferenceScore = 0.0;
        int assignmentCount = assignment.getAssignments().size();

        if (assignmentCount > 0) {
            double totalPreference = assignment.getAssignments().values().stream()
                .mapToDouble(SchedulingValue::getPreferenceScore)
                .sum();
            preferenceScore = totalPreference / assignmentCount;
        }

        return preferenceScore;
    }

    private List<SchedulingAssignment> selection(List<SchedulingAssignment> population,
                                               List<Double> fitnessScores) {
        List<SchedulingAssignment> selected = new ArrayList<>();
        int eliteCount = (int) (populationSize * elitismRate);

        // Elite selection
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < fitnessScores.size(); i++) {
            indices.add(i);
        }

        indices.sort((i, j) -> Double.compare(fitnessScores.get(j), fitnessScores.get(i)));

        for (int i = 0; i < eliteCount && i < indices.size(); i++) {
            selected.add(population.get(indices.get(i)).copy());
        }

        // Tournament selection for remaining
        while (selected.size() < populationSize) {
            SchedulingAssignment winner = tournamentSelection(population, fitnessScores);
            selected.add(winner.copy());
        }

        return selected;
    }

    private SchedulingAssignment tournamentSelection(List<SchedulingAssignment> population,
                                                    List<Double> fitnessScores) {
        int tournamentSize = 3;
        SchedulingAssignment best = null;
        double bestFitness = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < tournamentSize; i++) {
            int index = ThreadLocalRandom.current().nextInt(population.size());
            if (fitnessScores.get(index) > bestFitness) {
                bestFitness = fitnessScores.get(index);
                best = population.get(index);
            }
        }

        return best;
    }

    private List<SchedulingAssignment> evolvePopulation(List<SchedulingAssignment> selected,
                                                      ConstraintSatisfactionProblem csp) {
        List<SchedulingAssignment> newPopulation = new ArrayList<>();
        Collections.shuffle(selected);

        for (int i = 0; i < selected.size(); i += 2) {
            SchedulingAssignment parent1 = selected.get(i);
            SchedulingAssignment parent2 = (i + 1 < selected.size()) ? selected.get(i + 1) : selected.get(0);

            if (ThreadLocalRandom.current().nextDouble() < crossoverRate) {
                SchedulingAssignment[] offspring = crossover(parent1, parent2, csp);
                newPopulation.add(offspring[0]);
                if (offspring[1] != null) {
                    newPopulation.add(offspring[1]);
                }
            } else {
                newPopulation.add(parent1.copy());
                newPopulation.add(parent2.copy());
            }
        }

        // Mutation
        for (SchedulingAssignment individual : newPopulation) {
            if (ThreadLocalRandom.current().nextDouble() < mutationRate) {
                mutate(individual, csp);
            }
        }

        return newPopulation;
    }

    private SchedulingAssignment[] crossover(SchedulingAssignment parent1, SchedulingAssignment parent2,
                                          ConstraintSatisfactionProblem csp) {
        Map<SchedulingVariable, SchedulingValue> assignments1 = parent1.getAssignments();
        Map<SchedulingVariable, SchedulingValue> assignments2 = parent2.getAssignments();

        List<SchedulingVariable> variables = new ArrayList<>(assignments1.keySet());
        Collections.shuffle(variables);

        int crossoverPoint = ThreadLocalRandom.current().nextInt(1, variables.size());

        SchedulingAssignment offspring1 = new SchedulingAssignment();
        SchedulingAssignment offspring2 = new SchedulingAssignment();

        for (int i = 0; i < variables.size(); i++) {
            SchedulingVariable variable = variables.get(i);
            if (i < crossoverPoint) {
                offspring1.assign(variable, assignments1.get(variable));
                offspring2.assign(variable, assignments2.get(variable));
            } else {
                offspring1.assign(variable, assignments2.get(variable));
                offspring2.assign(variable, assignments1.get(variable));
            }
        }

        return new SchedulingAssignment[]{offspring1, offspring2};
    }

    private void mutate(SchedulingAssignment individual, ConstraintSatisfactionProblem csp) {
        Map<SchedulingVariable, SchedulingValue> assignments = individual.getAssignments();
        Map<SchedulingVariable, List<SchedulingValue>> domains = csp.getDomains();

        List<SchedulingVariable> variables = new ArrayList<>(assignments.keySet());
        if (variables.isEmpty()) return;

        SchedulingVariable variable = variables.get(ThreadLocalRandom.current().nextInt(variables.size()));
        List<SchedulingValue> possibleValues = new ArrayList<>(domains.get(variable));
        Collections.shuffle(possibleValues);

        individual.unassign(variable);

        for (SchedulingValue value : possibleValues) {
            individual.assign(variable, value);
            if (isValidAssignment(individual, csp)) {
                break;
            }
            individual.unassign(variable);
        }

        // If no valid value found, assign back the original
        if (individual.getValue(variable) == null) {
            individual.assign(variable, assignments.get(variable));
        }
    }

    private boolean isValidAssignment(SchedulingAssignment assignment, ConstraintSatisfactionProblem csp) {
        for (SchedulingConstraint constraint : csp.getConstraints()) {
            ConstraintResult result = constraint.validate(assignment);
            if (!result.isSatisfied() && constraint.getType() == ConstraintType.TEACHER_CONFLICT) {
                return false;
            }
        }
        return true;
    }

    // Getters for configuration
    public int getPopulationSize() { return populationSize; }
    public int getGenerations() { return generations; }
    public double getMutationRate() { return mutationRate; }
    public double getCrossoverRate() { return crossoverRate; }
    public double getElitismRate() { return elitismRate; }
}