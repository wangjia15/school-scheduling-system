package com.school.scheduling.domain.constraint;

/**
 * Priority levels for constraints.
 */
public enum ConstraintPriority {
    HARD(1, "Must be satisfied"),
    HIGH(2, "Strong preference"),
    MEDIUM(3, "Moderate preference"),
    LOW(4, "Weak preference");

    private final int level;
    private final String description;

    ConstraintPriority(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public boolean isHigherPriorityThan(ConstraintPriority other) {
        return this.level < other.level;
    }

    public boolean isLowerPriorityThan(ConstraintPriority other) {
        return this.level > other.level;
    }

    public boolean isHardConstraint() {
        return this == HARD;
    }

    public boolean isSoftConstraint() {
        return this != HARD;
    }

    @Override
    public String toString() {
        return name();
    }
}