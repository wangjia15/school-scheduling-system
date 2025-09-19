package com.school.scheduling.algorithm.optimization;

import com.school.scheduling.algorithm.ConstraintSatisfactionProblem;
import com.school.scheduling.domain.constraint.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Performance optimization utilities for large-scale scheduling problems.
 * Implements caching, parallelization, and algorithmic optimizations.
 */
public class PerformanceOptimizer {

    private final CacheManager cacheManager;
    private final ExecutorService executorService;
    private final int parallelismLevel;

    public PerformanceOptimizer() {
        this.cacheManager = new CacheManager();
        this.executorService = Executors.newWorkStealingPool();
        this.parallelismLevel = Runtime.getRuntime().availableProcessors();
    }

    /**
     * Optimizes CSP solving with caching and parallel processing.
     */
    public Optional<SchedulingAssignment> solveWithOptimizations(
        ConstraintSatisfactionProblem csp,
        OptimizationStrategy strategy) {

        // Check cache first
        String cacheKey = generateCacheKey(csp, strategy);
        Optional<SchedulingAssignment> cachedResult = cacheManager.get(cacheKey);
        if (cachedResult.isPresent()) {
            return cachedResult;
        }

        // Apply optimizations based on problem size
        if (isLargeScaleProblem(csp)) {
            return solveLargeScaleProblem(csp, strategy, cacheKey);
        } else {
            return solveStandardProblem(csp, strategy, cacheKey);
        }
    }

    /**
     * Solves large-scale problems with domain reduction and parallel processing.
     */
    private Optional<SchedulingAssignment> solveLargeScaleProblem(
        ConstraintSatisfactionProblem csp,
        OptimizationStrategy strategy,
        String cacheKey) {

        // Step 1: Domain reduction
        ReducedDomainResult reducedDomains = reduceDomains(csp);

        // Step 2: Problem decomposition
        List<ProblemSubproblem> subproblems = decomposeProblem(csp, reducedDomains);

        // Step 3: Parallel solving
        Optional<SchedulingAssignment> result = solveSubproblemsInParallel(subproblems, strategy);

        // Cache the result
        result.ifPresent(solution -> cacheManager.put(cacheKey, solution));

        return result;
    }

    /**
     * Solves standard problems with basic optimizations.
     */
    private Optional<SchedulingAssignment> solveStandardProblem(
        ConstraintSatisfactionProblem csp,
        OptimizationStrategy strategy,
        String cacheKey) {

        // Apply variable ordering optimization
        OptimizedCSP optimizedCsp = applyVariableOrdering(csp);

        // Solve with optimized CSP
        Optional<SchedulingAssignment> result = optimizedCsp.solveWithStrategy(
            mapStrategy(strategy)
        );

        // Cache the result
        result.ifPresent(solution -> cacheManager.put(cacheKey, solution));

        return result;
    }

    /**
     * Reduces domains using constraint propagation.
     */
    private ReducedDomainResult reduceDomains(ConstraintSatisfactionProblem csp) {
        Map<SchedulingVariable, List<SchedulingValue>> reducedDomains = new HashMap<>();

        for (SchedulingVariable variable : csp.getVariables()) {
            List<SchedulingValue> originalDomain = csp.getDomains().get(variable);
            List<SchedulingValue> reducedDomain = new ArrayList<>(originalDomain);

            // Apply constraint propagation
            for (SchedulingConstraint constraint : csp.getConstraints()) {
                if (constraint.getScope().contains(variable)) {
                    reducedDomain = constraint.getValidValues(
                        new SchedulingAssignment(),
                        Map.of(variable, reducedDomain)
                    );
                }
            }

            reducedDomains.put(variable, reducedDomain);
        }

        return new ReducedDomainResult(reducedDomains);
    }

    /**
     * Decomposes large problem into smaller subproblems.
     */
    private List<ProblemSubproblem> decomposeProblem(
        ConstraintSatisfactionProblem csp,
        ReducedDomainResult reducedDomains) {

        // Group variables by constraint groups
        Map<ConstraintGroup, Set<SchedulingVariable>> variableGroups = groupVariablesByConstraints(csp);

        // Create subproblems
        List<ProblemSubproblem> subproblems = new ArrayList<>();

        for (Map.Entry<ConstraintGroup, Set<SchedulingVariable>> entry : variableGroups.entrySet()) {
            ConstraintGroup group = entry.getKey();
            Set<SchedulingVariable> variables = entry.getValue();

            // Create subproblem constraints
            Set<SchedulingConstraint> subproblemConstraints = csp.getConstraints().stream()
                .filter(constraint -> constraint.getScope().stream().anyMatch(variables::contains))
                .collect(Collectors.toSet());

            // Create subproblem domains
            Map<SchedulingVariable, List<SchedulingValue>> subproblemDomains = variables.stream()
                .collect(Collectors.toMap(
                    Function.identity(),
                    v -> reducedDomains.getDomains().getOrDefault(v, new ArrayList<>())
                ));

            subproblems.add(new ProblemSubproblem(
                group.getId(),
                variables,
                subproblemConstraints,
                subproblemDomains
            ));
        }

        return subproblems;
    }

    /**
     * Solves subproblems in parallel.
     */
    private Optional<SchedulingAssignment> solveSubproblemsInParallel(
        List<ProblemSubproblem> subproblems,
        OptimizationStrategy strategy) {

        List<CompletableFuture<Optional<SchedulingAssignment>>> futures = subproblems.stream()
            .map(subproblem -> CompletableFuture.supplyAsync(
                () -> solveSubproblem(subproblem, strategy),
                executorService
            ))
            .collect(Collectors.toList());

        // Wait for all subproblems to complete
        List<Optional<SchedulingAssignment>> results = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());

        // Merge results
        return mergeSubproblemResults(results);
    }

    /**
     * Solves a single subproblem.
     */
    private Optional<SchedulingAssignment> solveSubproblem(
        ProblemSubproblem subproblem,
        OptimizationStrategy strategy) {

        ConstraintSatisfactionProblem subCsp = new ConstraintSatisfactionProblem(
            subproblem.getVariables(),
            subproblem.getConstraints(),
            subproblem.getDomains()
        );

        return subCsp.solveWithStrategy(mapStrategy(strategy));
    }

    /**
     * Merges results from multiple subproblems.
     */
    private Optional<SchedulingAssignment> mergeSubproblemResults(
        List<Optional<SchedulingAssignment>> results) {

        SchedulingAssignment merged = new SchedulingAssignment();

        for (Optional<SchedulingAssignment> result : results) {
            if (result.isPresent()) {
                merged.getAssignments().putAll(result.get().getAssignments());
            } else {
                return Optional.empty(); // If any subproblem fails, the whole solution fails
            }
        }

        return Optional.of(merged);
    }

    /**
     * Applies variable ordering optimization.
     */
    private OptimizedCSP applyVariableOrdering(ConstraintSatisfactionProblem csp) {
        // Calculate variable priorities
        Map<SchedulingVariable, Integer> variablePriorities = calculateVariablePriorities(csp);

        // Sort variables by priority
        List<SchedulingVariable> orderedVariables = csp.getVariables().stream()
            .sorted(Comparator.comparingInt(variablePriorities::get))
            .collect(Collectors.toList());

        return new OptimizedCSP(csp, orderedVariables);
    }

    /**
     * Calculates variable priorities for ordering.
     */
    private Map<SchedulingVariable, Integer> calculateVariablePriorities(ConstraintSatisfactionProblem csp) {
        Map<SchedulingVariable, Integer> priorities = new HashMap<>();

        for (SchedulingVariable variable : csp.getVariables()) {
            int priority = 0;

            // Priority based on domain size (smaller domains first)
            priority += (1000 / csp.getDomains().get(variable).size());

            // Priority based on constraint count (more constrained variables first)
            priority += csp.getConstraints().stream()
                .filter(constraint -> constraint.getScope().contains(variable))
                .count();

            // Priority based on constraint type (hard constraints first)
            priority += csp.getConstraints().stream()
                .filter(constraint -> constraint.getScope().contains(variable))
                .filter(SchedulingConstraint::isHardConstraint)
                .count() * 10;

            priorities.put(variable, priority);
        }

        return priorities;
    }

    /**
     * Groups variables by constraint relationships.
     */
    private Map<ConstraintGroup, Set<SchedulingVariable>> groupVariablesByConstraints(
        ConstraintSatisfactionProblem csp) {

        Map<ConstraintGroup, Set<SchedulingVariable>> groups = new HashMap<>();
        Map<SchedulingVariable, ConstraintGroup> variableToGroup = new HashMap<>();

        // Create initial groups based on direct constraint relationships
        for (SchedulingConstraint constraint : csp.getConstraints()) {
            Set<SchedulingVariable> constraintVariables = constraint.getScope();

            // Find or create group for these variables
            ConstraintGroup group = findOrCreateGroupForVariables(
                constraintVariables, variableToGroup, groups
            );

            // Add all variables to the group
            for (SchedulingVariable variable : constraintVariables) {
                group.addVariable(variable);
                variableToGroup.put(variable, group);
            }
        }

        return groups;
    }

    /**
     * Finds or creates a constraint group for variables.
     */
    private ConstraintGroup findOrCreateGroupForVariables(
        Set<SchedulingVariable> variables,
        Map<SchedulingVariable, ConstraintGroup> variableToGroup,
        Map<ConstraintGroup, Set<SchedulingVariable>> groups) {

        // Check if any variable already belongs to a group
        for (SchedulingVariable variable : variables) {
            ConstraintGroup existingGroup = variableToGroup.get(variable);
            if (existingGroup != null) {
                return existingGroup;
            }
        }

        // Create new group
        ConstraintGroup newGroup = new ConstraintGroup(UUID.randomUUID().toString());
        groups.put(newGroup, new HashSet<>());

        return newGroup;
    }

    /**
     * Checks if the problem is large-scale.
     */
    private boolean isLargeScaleProblem(ConstraintSatisfactionProblem csp) {
        return csp.getVariables().size() > 1000 ||
               csp.getConstraints().size() > 500 ||
               csp.getDomains().values().stream()
                   .mapToInt(List::size)
                   .average()
                   .orElse(0) > 50;
    }

    /**
     * Generates cache key for the problem.
     */
    private String generateCacheKey(ConstraintSatisfactionProblem csp, OptimizationStrategy strategy) {
        return String.format("csp_%d_%d_%s",
            csp.getVariables().size(),
            csp.getConstraints().size(),
            strategy.name()
        );
    }

    /**
     * Maps optimization strategy to CSP solving strategy.
     */
    private ConstraintSatisfactionProblem.SolvingStrategy mapStrategy(OptimizationStrategy strategy) {
        switch (strategy) {
            case PARALLEL_BACKTRACKING:
                return ConstraintSatisfactionProblem.SolvingStrategy.BACKTRACKING_FORWARD_CHECKING;
            case PARALLEL_AC3:
                return ConstraintSatisfactionProblem.SolvingStrategy.BACKTRACKING_AC3;
            case DISTRIBUTED_MIN_CONFLICTS:
                return ConstraintSatisfactionProblem.SolvingStrategy.MIN_CONFLICTS;
            default:
                return ConstraintSatisfactionProblem.SolvingStrategy.BACKTRACKING_FORWARD_CHECKING;
        }
    }

    // Helper classes

    public static class ReducedDomainResult {
        private final Map<SchedulingVariable, List<SchedulingValue>> domains;

        public ReducedDomainResult(Map<SchedulingVariable, List<SchedulingValue>> domains) {
            this.domains = new HashMap<>(domains);
        }

        public Map<SchedulingVariable, List<SchedulingValue>> getDomains() {
            return new HashMap<>(domains);
        }
    }

    public static class ProblemSubproblem {
        private final String id;
        private final Set<SchedulingVariable> variables;
        private final Set<SchedulingConstraint> constraints;
        private final Map<SchedulingVariable, List<SchedulingValue>> domains;

        public ProblemSubproblem(String id, Set<SchedulingVariable> variables,
                                Set<SchedulingConstraint> constraints,
                                Map<SchedulingVariable, List<SchedulingValue>> domains) {
            this.id = id;
            this.variables = new HashSet<>(variables);
            this.constraints = new HashSet<>(constraints);
            this.domains = new HashMap<>(domains);
        }

        // Getters
        public String getId() { return id; }
        public Set<SchedulingVariable> getVariables() { return new HashSet<>(variables); }
        public Set<SchedulingConstraint> getConstraints() { return new HashSet<>(constraints); }
        public Map<SchedulingVariable, List<SchedulingValue>> getDomains() { return new HashMap<>(domains); }
    }

    public static class ConstraintGroup {
        private final String id;
        private final Set<SchedulingVariable> variables;

        public ConstraintGroup(String id) {
            this.id = id;
            this.variables = new HashSet<>();
        }

        public void addVariable(SchedulingVariable variable) {
            variables.add(variable);
        }

        // Getters
        public String getId() { return id; }
        public Set<SchedulingVariable> getVariables() { return new HashSet<>(variables); }
    }

    public static class OptimizedCSP {
        private final ConstraintSatisfactionProblem originalCsp;
        private final List<SchedulingVariable> orderedVariables;

        public OptimizedCSP(ConstraintSatisfactionProblem originalCsp, List<SchedulingVariable> orderedVariables) {
            this.originalCsp = originalCsp;
            this.orderedVariables = new ArrayList<>(orderedVariables);
        }

        public Optional<SchedulingAssignment> solveWithStrategy(
            ConstraintSatisfactionProblem.SolvingStrategy strategy) {
            // This would use the ordered variables for solving
            return originalCsp.solveWithStrategy(strategy);
        }

        // Getters
        public ConstraintSatisfactionProblem getOriginalCsp() { return originalCsp; }
        public List<SchedulingVariable> getOrderedVariables() { return new ArrayList<>(orderedVariables); }
    }

    public enum OptimizationStrategy {
        PARALLEL_BACKTRACKING,
        PARALLEL_AC3,
        DISTRIBUTED_MIN_CONFLICTS,
        HYBRID_APPROACH
    }

    /**
     * Cache manager for storing and retrieving CSP solutions.
     */
    private static class CacheManager {
        private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
        private final int maxSize = 1000;

        public void put(String key, SchedulingAssignment solution) {
            if (cache.size() >= maxSize) {
                evictLeastRecentlyUsed();
            }
            cache.put(key, new CacheEntry(solution));
        }

        public Optional<SchedulingAssignment> get(String key) {
            CacheEntry entry = cache.get(key);
            if (entry != null && !entry.isExpired()) {
                return Optional.of(entry.getSolution());
            }
            return Optional.empty();
        }

        private void evictLeastRecentlyUsed() {
            cache.entrySet().stream()
                .min(Comparator.comparingLong(entry -> entry.getValue().getLastAccessTime()))
                .ifPresent(entry -> cache.remove(entry.getKey()));
        }
    }

    private static class CacheEntry {
        private final SchedulingAssignment solution;
        private final long creationTime;
        private long lastAccessTime;

        public CacheEntry(SchedulingAssignment solution) {
            this.solution = solution;
            this.creationTime = System.currentTimeMillis();
            this.lastAccessTime = creationTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - creationTime > TimeUnit.HOURS.toMillis(1);
        }

        public SchedulingAssignment getSolution() {
            this.lastAccessTime = System.currentTimeMillis();
            return solution;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}