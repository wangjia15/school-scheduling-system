package com.school.scheduling.domain.constraint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a partial or complete assignment in the CSP.
 */
public class SchedulingAssignment {
    private final Map<SchedulingVariable, SchedulingValue> assignments;
    private final Map<String, Object> metadata;

    public SchedulingAssignment() {
        this.assignments = new HashMap<>();
        this.metadata = new HashMap<>();
    }

    public SchedulingAssignment(Map<SchedulingVariable, SchedulingValue> assignments) {
        this.assignments = new HashMap<>(assignments);
        this.metadata = new HashMap<>();
    }

    public void assign(SchedulingVariable variable, SchedulingValue value) {
        assignments.put(variable, value);
    }

    public void unassign(SchedulingVariable variable) {
        assignments.remove(variable);
    }

    public SchedulingValue getValue(SchedulingVariable variable) {
        return assignments.get(variable);
    }

    public boolean isAssigned(SchedulingVariable variable) {
        return assignments.containsKey(variable);
    }

    public boolean isComplete(Set<SchedulingVariable> allVariables) {
        return allVariables.stream().allMatch(this::isAssigned);
    }

    public boolean isEmpty() {
        return assignments.isEmpty();
    }

    public int size() {
        return assignments.size();
    }

    public Map<SchedulingVariable, SchedulingValue> getAssignments() {
        return new HashMap<>(assignments);
    }

    public Set<SchedulingVariable> getAssignedVariables() {
        return assignments.keySet();
    }

    public Set<SchedulingValue> getAssignedValues() {
        return new HashSet<>(assignments.values());
    }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }

    public SchedulingAssignment copy() {
        SchedulingAssignment copy = new SchedulingAssignment(this.assignments);
        copy.metadata.putAll(this.metadata);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingAssignment that = (SchedulingAssignment) o;
        return assignments.equals(that.assignments);
    }

    @Override
    public int hashCode() {
        return assignments.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SchedulingAssignment{");
        sb.append("size=").append(assignments.size());
        sb.append(", assignments={");

        assignments.forEach((variable, value) -> {
            sb.append(variable.getDisplayName()).append("=").append(value.getDisplayName());
            sb.append(", ");
        });

        if (!assignments.isEmpty()) {
            sb.setLength(sb.length() - 2); // Remove last ", "
        }

        sb.append("}");
        sb.append("}");
        return sb.toString();
    }
}