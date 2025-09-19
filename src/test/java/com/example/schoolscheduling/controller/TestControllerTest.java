package com.example.schoolscheduling.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAllAccess() throws Exception {
        mockMvc.perform(get("/api/test/all"))
                .andExpect(status().isOk())
                .andExpect(content().string("Public Content."));
    }

    @Test
    public void testUserAccess_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/test/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testTeacherAccess_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/test/teacher"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAdminAccess_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isUnauthorized());
    }
}