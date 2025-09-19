package com.school.scheduling.domain.constraint;

import java.util.*;

/**
 * Represents a constraint in the Constraint Satisfaction Problem (CSP) for school scheduling.
 */
public abstract class SchedulingConstraint {

    private final String name;
    private final ConstraintType type;
    private final ConstraintPriority priority;
    private final String description;

    public SchedulingConstraint(String name, ConstraintType type, ConstraintPriority priority, String description) {
        this.name = name;
        this.type = type;
        this.priority = priority;
        this.description = description;
    }

    /**
     * Validates if the constraint is satisfied for the given scheduling assignment.
     */
    public abstract ConstraintResult validate(SchedulingAssignment assignment);

    /**
     * Returns the scope of this constraint (what entities it affects).
     */
    public abstract Set<SchedulingVariable> getScope();

    /**
     * Returns the domain values that are still valid after constraint propagation.
     */
    public abstract List<SchedulingValue> getValidValues(SchedulingAssignment assignment,
                                                         Map<SchedulingVariable, List<SchedulingValue>> domains);

    public String getName() {
        return name;
    }

    public ConstraintType getType() {
        return type;
    }

    public ConstraintPriority getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public boolean isHardConstraint() {
        return priority == ConstraintPriority.HARD;
    }

    public boolean isSoftConstraint() {
        return priority == ConstraintPriority.SOFT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingConstraint that = (SchedulingConstraint) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return String.format("%s[%s:%s]", name, type, priority);
    }
}