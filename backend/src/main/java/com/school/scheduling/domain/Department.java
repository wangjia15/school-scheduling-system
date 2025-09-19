package com.school.scheduling.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "departments", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name"),
    @UniqueConstraint(columnNames = "code")
})
public class Department extends BaseEntity {

    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name must be less than 100 characters")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Department code is required")
    @Size(max = 20, message = "Department code must be less than 20 characters")
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "head_id", referencedColumnName = "id")
    private User head;

    public boolean hasHead() {
        return head != null;
    }

    public boolean canBeManagedBy(User user) {
        return user.isAdmin() ||
               user.isDepartmentHead() &&
               (head == null || head.getId().equals(user.getId()));
    }

    public String getDisplayName() {
        return code + " - " + name;
    }
}