package com.example.schoolscheduling.controller;

import com.example.schoolscheduling.model.Role;
import com.example.schoolscheduling.model.User;
import com.example.schoolscheduling.payload.request.LoginRequest;
import com.example.schoolscheduling.payload.request.SignupRequest;
import com.example.schoolscheduling.payload.response.JwtResponse;
import com.example.schoolscheduling.payload.response.MessageResponse;
import com.example.schoolscheduling.security.JwtUtils;
import com.example.schoolscheduling.security.UserPrincipal;
import com.example.schoolscheduling.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        AuthController authController = new AuthController();
        ReflectionTestUtils.setField(authController, "authenticationManager", authenticationManager);
        ReflectionTestUtils.setField(authController, "userService", userService);
        ReflectionTestUtils.setField(authController, "jwtUtils", jwtUtils);

        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(1L);
        when(userPrincipal.getUsername()).thenReturn("testuser");
        when(userPrincipal.getEmail()).thenReturn("test@example.com");
        when(userPrincipal.getAuthorities()).thenReturn(Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_STUDENT")
        ));
    }

    @Test
    public void testAuthenticateUser_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("test-jwt-token");

        // Act
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_STUDENT"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateJwtToken(authentication);
    }

    @Test
    public void testAuthenticateUser_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("invaliduser");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});

        // Act
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRegisterUser_Success() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("new@example.com");
        signupRequest.setPassword("password");
        signupRequest.setFirstName("New");
        signupRequest.setLastName("User");

        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("new@example.com")).thenReturn(false);
        doNothing().when(userService).createUser(any(User.class));

        // Act
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        verify(userService, times(1)).existsByUsername("newuser");
        verify(userService, times(1)).existsByEmail("new@example.com");
        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    public void testRegisterUser_UsernameExists() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("existinguser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password");
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");

        when(userService.existsByUsername("existinguser")).thenReturn(true);

        // Act
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));

        verify(userService, times(1)).existsByUsername("existinguser");
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    public void testRegisterUser_EmailExists() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("existing@example.com");
        signupRequest.setPassword("password");
        signupRequest.setFirstName("New");
        signupRequest.setLastName("User");

        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("existing@example.com")).thenReturn(true);

        // Act
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));

        verify(userService, times(1)).existsByUsername("newuser");
        verify(userService, times(1)).existsByEmail("existing@example.com");
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    public void testRegisterUser_WithRoles() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("new@example.com");
        signupRequest.setPassword("password");
        signupRequest.setFirstName("New");
        signupRequest.setLastName("User");
        Set<Role> roles = new HashSet<>();
        roles.add(Role.TEACHER);
        roles.add(Role.STUDENT);
        signupRequest.setRoles(roles);

        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("new@example.com")).thenReturn(false);
        doNothing().when(userService).createUser(any(User.class));

        // Act
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        verify(userService, times(1)).existsByUsername("newuser");
        verify(userService, times(1)).existsByEmail("new@example.com");
        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    public void testRegisterUser_InvalidInput() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername(""); // Invalid username
        signupRequest.setEmail("invalid-email"); // Invalid email
        signupRequest.setPassword("123"); // Invalid password (too short)
        signupRequest.setFirstName("");
        signupRequest.setLastName("");

        // Act
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).existsByUsername(anyString());
        verify(userService, never()).existsByEmail(anyString());
        verify(userService, never()).createUser(any(User.class));
    }
}