package com.school.scheduling.domain.constraint;

import java.util.Objects;

/**
 * Represents a variable in the CSP - something that needs to be assigned a value.
 */
public class SchedulingVariable {
    private final String id;
    private final VariableType type;
    private final String entityId;
    private final String displayName;

    public SchedulingVariable(String id, VariableType type, String entityId, String displayName) {
        this.id = id;
        this.type = type;
        this.entityId = entityId;
        this.displayName = displayName;
    }

    public SchedulingVariable(VariableType type, String entityId, String displayName) {
        this.id = type.name() + "_" + entityId;
        this.type = type;
        this.entityId = entityId;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public VariableType getType() {
        return type;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingVariable that = (SchedulingVariable) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s[%s:%s]", displayName, type, entityId);
    }
}