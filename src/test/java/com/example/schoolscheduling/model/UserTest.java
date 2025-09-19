package com.example.schoolscheduling.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testUserBuilder() {
        // Act
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .roles(new HashSet<>(Set.of(Role.STUDENT)))
                .build();

        // Assert
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertTrue(user.getIsActive());
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(Role.STUDENT));
    }

    @Test
    public void testUserDefaultValues() {
        // Act
        User user = new User();

        // Assert
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
        assertNull(user.getIsActive());
    }

    @Test
    public void testUserWithAllArgsConstructor() {
        // Act
        User user = new User(1L, "testuser", "test@example.com", "password", "Test", "User", true, new HashSet<>(Set.of(Role.ADMIN)));

        // Assert
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertTrue(user.getIsActive());
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(Role.ADMIN));
    }

    @Test
    public void testUserSettersAndGetters() {
        // Arrange
        User user = new User();

        // Act
        user.setId(2L);
        user.setUsername("updateduser");
        user.setEmail("updated@example.com");
        user.setPassword("newpassword");
        user.setFirstName("Updated");
        user.setLastName("User");
        user.setIsActive(false);

        Set<Role> roles = new HashSet<>();
        roles.add(Role.TEACHER);
        user.setRoles(roles);

        // Assert
        assertEquals(2L, user.getId());
        assertEquals("updateduser", user.getUsername());
        assertEquals("updated@example.com", user.getEmail());
        assertEquals("newpassword", user.getPassword());
        assertEquals("Updated", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertFalse(user.getIsActive());
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(Role.TEACHER));
    }

    @Test
    public void testUserEquality() {
        // Arrange
        User user1 = new User(1L, "testuser", "test@example.com", "password", "Test", "User", true, new HashSet<>(Set.of(Role.STUDENT)));
        User user2 = new User(1L, "testuser", "test@example.com", "password", "Test", "User", true, new HashSet<>(Set.of(Role.STUDENT)));

        // Assert
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getUsername(), user2.getUsername());
        assertEquals(user1.getEmail(), user2.getEmail());
    }

    @Test
    public void testUserWithMultipleRoles() {
        // Arrange
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ADMIN);
        roles.add(Role.TEACHER);
        roles.add(Role.STUDENT);

        User user = User.builder()
                .id(1L)
                .username("multiroleuser")
                .email("multi@example.com")
                .password("password")
                .firstName("Multi")
                .lastName("Role")
                .roles(roles)
                .build();

        // Assert
        assertEquals(3, user.getRoles().size());
        assertTrue(user.getRoles().contains(Role.ADMIN));
        assertTrue(user.getRoles().contains(Role.TEACHER));
        assertTrue(user.getRoles().contains(Role.STUDENT));
    }
}