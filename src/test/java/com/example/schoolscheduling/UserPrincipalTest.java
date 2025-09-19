package com.example.schoolscheduling;

import com.example.schoolscheduling.model.Role;
import com.example.schoolscheduling.model.User;
import com.example.schoolscheduling.security.UserPrincipal;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserPrincipalTest {

    @Test
    public void testCreateUserPrincipal() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");

        Set<Role> roles = new HashSet<>();
        roles.add(Role.STUDENT);
        roles.add(Role.TEACHER);
        user.setRoles(roles);

        // Act
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        // Assert
        assertNotNull(userPrincipal);
        assertEquals(user.getId(), userPrincipal.getId());
        assertEquals(user.getUsername(), userPrincipal.getUsername());
        assertEquals(user.getEmail(), userPrincipal.getEmail());
        assertEquals(user.getPassword(), userPrincipal.getPassword());
        assertEquals(2, userPrincipal.getAuthorities().size());
        assertTrue(userPrincipal.isAccountNonExpired());
        assertTrue(userPrincipal.isAccountNonLocked());
        assertTrue(userPrincipal.isCredentialsNonExpired());
        assertTrue(userPrincipal.isEnabled());
    }

    @Test
    public void testUserPrincipalWithSingleRole() {
        // Arrange
        User user = new User();
        user.setId(2L);
        user.setUsername("teacher");
        user.setEmail("teacher@example.com");
        user.setPassword("password");

        Set<Role> roles = new HashSet<>();
        roles.add(Role.TEACHER);
        user.setRoles(roles);

        // Act
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        // Assert
        assertNotNull(userPrincipal);
        assertEquals(1, userPrincipal.getAuthorities().size());
        assertTrue(userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER")));
    }

    @Test
    public void testUserPrincipalWithAdminRole() {
        // Arrange
        User user = new User();
        user.setId(3L);
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPassword("password");

        Set<Role> roles = new HashSet<>();
        roles.add(Role.ADMIN);
        user.setRoles(roles);

        // Act
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        // Assert
        assertNotNull(userPrincipal);
        assertEquals(1, userPrincipal.getAuthorities().size());
        assertTrue(userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    public void testUserPrincipalConstructor() {
        // Arrange
        Set<Role> roles = new HashSet<>();
        roles.add(Role.STUDENT);

        // Act
        UserPrincipal userPrincipal = new UserPrincipal(
            1L, "testuser", "test@example.com", "password",
            roles.stream().map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.name())).collect(java.util.stream.Collectors.toList())
        );

        // Assert
        assertNotNull(userPrincipal);
        assertEquals(1L, userPrincipal.getId());
        assertEquals("testuser", userPrincipal.getUsername());
        assertEquals("test@example.com", userPrincipal.getEmail());
        assertEquals("password", userPrincipal.getPassword());
        assertEquals(1, userPrincipal.getAuthorities().size());
    }
}