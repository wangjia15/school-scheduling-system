package com.example.schoolscheduling;

import com.example.schoolscheduling.model.Role;
import com.example.schoolscheduling.model.User;
import com.example.schoolscheduling.repository.UserRepository;
import com.example.schoolscheduling.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRoles(new HashSet<>(Arrays.asList(Role.STUDENT)));
    }

    @Test
    public void testCreateUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");
        newUser.setFirstName("New");
        newUser.setLastName("User");

        // Act
        User result = userService.createUser(newUser);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testCreateUser_UsernameExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        User user = new User();
        user.setUsername("existinguser");
        user.setEmail("test@example.com");
        user.setPassword("password");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(user);
        });

        assertEquals("Username is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testCreateUser_EmailExists() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        User user = new User();
        user.setUsername("newuser");
        user.setEmail("existing@example.com");
        user.setPassword("password");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.createUser(user);
        });

        assertEquals("Email is already in use!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testCreateUser_DefaultRole() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User capturedUser = new User();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            capturedUser = invocation.getArgument(0);
            return capturedUser;
        });

        User user = new User();
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setPassword("password");
        user.setFirstName("New");
        user.setLastName("User");
        user.setRoles(null);

        // Act
        User result = userService.createUser(user);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.STUDENT));
    }

    @Test
    public void testFindByUsername() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    public void testFindByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void testAddRoleToUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.addRoleToUser(1L, Role.TEACHER);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.STUDENT));
        assertTrue(result.getRoles().contains(Role.TEACHER));
    }

    @Test
    public void testAddRoleToUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.addRoleToUser(999L, Role.TEACHER);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testRemoveRoleFromUser() {
        // Arrange
        testUser.getRoles().add(Role.TEACHER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.removeRoleFromUser(1L, Role.TEACHER);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.STUDENT));
        assertFalse(result.getRoles().contains(Role.TEACHER));
    }
}