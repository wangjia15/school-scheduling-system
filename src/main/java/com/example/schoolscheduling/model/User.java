package com.example.schoolscheduling.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Size(max = 100)
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @Column(name = "first_name")
    @NotBlank
    @Size(max = 50)
    private String firstName;

    @Column(name = "last_name")
    @NotBlank
    @Size(max = 50)
    private String lastName;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    @PreUpdate
    public void ensureDefaultRole() {
        if (roles == null || roles.isEmpty()) {
            roles.add(Role.STUDENT);
        }
    }
}