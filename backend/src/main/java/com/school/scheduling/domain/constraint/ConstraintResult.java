package com.school.scheduling.domain.constraint;

/**
 * Result of constraint validation.
 */
public class ConstraintResult {
    private final boolean satisfied;
    private final String message;
    private final double violationScore;
    private final List<String> affectedEntities;

    public ConstraintResult(boolean satisfied, String message, double violationScore) {
        this(satisfied, message, violationScore, new ArrayList<>());
    }

    public ConstraintResult(boolean satisfied, String message, double violationScore, List<String> affectedEntities) {
        this.satisfied = satisfied;
        this.message = message;
        this.violationScore = violationScore;
        this.affectedEntities = new ArrayList<>(affectedEntities);
    }

    public static ConstraintResult satisfied() {
        return new ConstraintResult(true, "Constraint satisfied", 0.0);
    }

    public static ConstraintResult violated(String message) {
        return new ConstraintResult(false, message, 1.0);
    }

    public static ConstraintResult violated(String message, double violationScore) {
        return new ConstraintResult(false, message, violationScore);
    }

    public static ConstraintResult violated(String message, double violationScore, List<String> affectedEntities) {
        return new ConstraintResult(false, message, violationScore, affectedEntities);
    }

    public boolean isSatisfied() {
        return satisfied;
    }

    public boolean isViolated() {
        return !satisfied;
    }

    public String getMessage() {
        return message;
    }

    public double getViolationScore() {
        return violationScore;
    }

    public List<String> getAffectedEntities() {
        return new ArrayList<>(affectedEntities);
    }

    public ConstraintResult withAffectedEntity(String entity) {
        List<String> newEntities = new ArrayList<>(affectedEntities);
        newEntities.add(entity);
        return new ConstraintResult(satisfied, message, violationScore, newEntities);
    }

    public ConstraintResult withHigherScore(double newScore) {
        return new ConstraintResult(satisfied, message, Math.max(violationScore, newScore), affectedEntities);
    }

    @Override
    public String toString() {
        return String.format("ConstraintResult[%s:%s:%.2f]", satisfied ? "SATISFIED" : "VIOLATED", message, violationScore);
    }
}