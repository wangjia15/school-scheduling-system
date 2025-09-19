package com.school.scheduling.domain.constraint;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a value that can be assigned to a scheduling variable.
 */
public class SchedulingValue {
    private final String id;
    private final ValueType type;
    private final Object value;
    private final String displayName;
    private final double preferenceScore;

    public SchedulingValue(String id, ValueType type, Object value, String displayName, double preferenceScore) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.displayName = displayName;
        this.preferenceScore = preferenceScore;
    }

    public SchedulingValue(ValueType type, Object value, String displayName) {
        this(type.name() + "_" + value.toString(), type, value, displayName, 0.0);
    }

    public SchedulingValue(ValueType type, Object value, String displayName, double preferenceScore) {
        this(type.name() + "_" + value.toString(), type, value, displayName, preferenceScore);
    }

    public String getId() {
        return id;
    }

    public ValueType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPreferenceScore() {
        return preferenceScore;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValueAs(Class<T> type) {
        if (type.isInstance(value)) {
            return (T) value;
        }
        throw new ClassCastException("Cannot cast " + value.getClass() + " to " + type);
    }

    public boolean isTimeSlot() {
        return type == ValueType.TIME_SLOT;
    }

    public boolean isClassroom() {
        return type == ValueType.CLASSROOM;
    }

    public boolean isTeacher() {
        return type == ValueType.TEACHER;
    }

    public boolean isDate() {
        return type == ValueType.DATE;
    }

    public boolean isLocation() {
        return type == ValueType.LOCATION;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingValue that = (SchedulingValue) o;
        return Objects.equals(id, that.id) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    @Override
    public String toString() {
        return String.format("%s[%s:%s]", displayName, type, value);
    }
}