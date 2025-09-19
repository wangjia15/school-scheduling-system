package com.example.schoolscheduling;

import com.example.schoolscheduling.security.JwtUtils;
import com.example.schoolscheduling.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    @BeforeEach
    public void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "testSecretKeyForJwtTokenGenerationAndValidation123456");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000); // 1 hour
    }

    @Test
    public void testGenerateJwtToken() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getUsername()).thenReturn("testuser");

        // Act
        String token = jwtUtils.generateJwtToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    public void testGetUserNameFromJwtToken() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getUsername()).thenReturn("testuser");
        String token = jwtUtils.generateJwtToken(authentication);

        // Act
        String username = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    public void testValidateJwtToken() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getUsername()).thenReturn("testuser");
        String token = jwtUtils.generateJwtToken(authentication);

        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    public void testValidateInvalidJwtToken() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateEmptyJwtToken() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    public void testValidateNullJwtToken() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken(null);

        // Assert
        assertFalse(isValid);
    }
}