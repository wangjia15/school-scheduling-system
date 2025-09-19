package com.example.schoolscheduling.integration;

import com.example.schoolscheduling.model.Role;
import com.example.schoolscheduling.model.User;
import com.example.schoolscheduling.repository.UserRepository;
import com.example.schoolscheduling.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void testFullAuthenticationFlow() throws Exception {
        // Step 1: Register a new user
        String signupJson = "{\"username\":\"integrationuser\",\"email\":\"integration@example.com\",\"password\":\"password123\",\"firstName\":\"Integration\",\"lastName\":\"User\"}";

        mockMvc.perform(post("/api/auth/signup")
                .contentType("application/json")
                .content(signupJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Step 2: Login with the registered user
        String loginJson = "{\"username\":\"integrationuser\",\"password\":\"password123\"}";

        MvcResult loginResult = mockMvc.perform(post("/api/auth/signin")
                .contentType("application/json")
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("integrationuser"))
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andReturn();

        // Extract token from response
        String response = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();

        // Step 3: Use token to access protected endpoint
        mockMvc.perform(post("/api/auth/signin")
                .contentType("application/json")
                .content(loginJson))
                .andExpect(status().isOk());
    }

    @Test
    public void testUserRegistrationWithRoles() throws Exception {
        // Create admin user first
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFirstName("Admin");
        admin.setLastName("User");
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(Role.ADMIN);
        admin.setRoles(adminRoles);
        userRepository.save(admin);

        // Register user with specific roles
        String signupJson = "{\"username\":\"teacheruser\",\"email\":\"teacher@example.com\",\"password\":\"password123\",\"firstName\":\"Teacher\",\"lastName\":\"User\",\"roles\":[\"TEACHER\",\"STUDENT\"]}";

        mockMvc.perform(post("/api/auth/signup")
                .contentType("application/json")
                .content(signupJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Verify user was created in database
        User savedUser = userRepository.findByUsername("teacheruser").orElse(null);
        assertNotNull(savedUser);
        assertEquals(2, savedUser.getRoles().size());
        assertTrue(savedUser.getRoles().contains(Role.TEACHER));
        assertTrue(savedUser.getRoles().contains(Role.STUDENT));
    }

    @Test
    public void testDuplicateUsernameRegistration() throws Exception {
        // Register first user
        String signupJson1 = "{\"username\":\"duplicateuser\",\"email\":\"user1@example.com\",\"password\":\"password123\",\"firstName\":\"User\",\"lastName\":\"One\"}";

        mockMvc.perform(post("/api/auth/signup")
                .contentType("application/json")
                .content(signupJson1))
                .andExpect(status().isOk());

        // Try to register another user with same username
        String signupJson2 = "{\"username\":\"duplicateuser\",\"email\":\"user2@example.com\",\"password\":\"password123\",\"firstName\":\"User\",\"lastName\":\"Two\"}";

        mockMvc.perform(post("/api/auth/signup")
                .contentType("application/json")
                .content(signupJson2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    public void testDuplicateEmailRegistration() throws Exception {
        // Register first user
        String signupJson1 = "{\"username\":\"user1\",\"email\":\"duplicate@example.com\",\"password\":\"password123\",\"firstName\":\"User\",\"lastName\":\"One\"}";

        mockMvc.perform(post("/api/auth/signup")
                .contentType("application/json")
                .content(signupJson1))
                .andExpect(status().isOk());

        // Try to register another user with same email
        String signupJson2 = "{\"username\":\"user2\",\"email\":\"duplicate@example.com\",\"password\":\"password123\",\"firstName\":\"User\",\"LastName\":\"Two\"}";

        mockMvc.perform(post("/api/auth/signup")
                .contentType("application/json")
                .content(signupJson2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));
    }

    @Test
    public void testInvalidLogin() throws Exception {
        // Register a user
        String signupJson = "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"correctpassword\",\"firstName\":\"Test\",\"lastName\":\"User\"}";

        mockMvc.perform(post("/api/auth/signup")
                .contentType("application/json")
                .content(signupJson))
                .andExpect(status().isOk());

        // Try to login with wrong password
        String loginJson = "{\"username\":\"testuser\",\"password\":\"wrongpassword\"}";

        mockMvc.perform(post("/api/auth/signin")
                .contentType("application/json")
                .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testJwtTokenValidation() throws Exception {
        // Register and login a user
        String signupJson = "{\"username\":\"jwttuser\",\"email\":\"jwt@example.com\",\"password\":\"password123\",\"firstName\":\"JWT\",\"lastName\":\"User\"}";

        mockMvc.perform(post("/api/auth/signup")
                .contentType("application/json")
                .content(signupJson))
                .andExpect(status().isOk());

        String loginJson = "{\"username\":\"jwttuser\",\"password\":\"password123\"}";

        MvcResult loginResult = mockMvc.perform(post("/api/auth/signin")
                .contentType("application/json")
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();

        // Test token validation
        boolean isValid = jwtUtils.validateJwtToken(token);
        assertTrue(isValid);

        // Test username extraction
        String username = jwtUtils.getUserNameFromJwtToken(token);
        assertEquals("jwttuser", username);
    }

    @Test
    public void testUserDefaultRoleAssignment() throws Exception {
        // Register user without specifying roles
        String signupJson = "{\"username\":\"defaultuser\",\"email\":\"default@example.com\",\"password\":\"password123\",\"firstName\":\"Default\",\"lastName\":\"User\"}";

        mockMvc.perform(post("/api/auth/signup")
                .contentType("application/json")
                .content(signupJson))
                .andExpect(status().isOk());

        // Verify default role was assigned
        User savedUser = userRepository.findByUsername("defaultuser").orElse(null);
        assertNotNull(savedUser);
        assertEquals(1, savedUser.getRoles().size());
        assertTrue(savedUser.getRoles().contains(Role.STUDENT));
    }
}