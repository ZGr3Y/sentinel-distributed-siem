package com.sentinel.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdaworks.crypto.SCryptUtil;
import com.sentinel.api.dto.LoginRequest;
import com.sentinel.api.repository.UserRepository;
import com.sentinel.api.security.JwtUtils;
import com.sentinel.common.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters to isolate the controller
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    private User validUser;
    private LoginRequest loginRequest;
    private String scryptHash;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("securePassword");

        scryptHash = SCryptUtil.scrypt("securePassword", 16384, 8, 1);
        
        validUser = new User();
        ReflectionTestUtils.setField(validUser, "id", "usr-123");
        validUser.setUsername("admin");
        validUser.setPasswordHash(scryptHash);
        validUser.setRole("ADMIN");
    }

    @Test
    void testLogin_Success_ReturnsToken() throws Exception {
        Mockito.when(userRepository.findByUsername("admin")).thenReturn(Optional.of(validUser));
        Mockito.when(jwtUtils.generateToken("usr-123", "ADMIN")).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.userId").value("usr-123"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void testLogin_UserNotFound_ReturnsUnauthorized() throws Exception {
        Mockito.when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials."));
    }

    @Test
    void testLogin_WrongPassword_ReturnsUnauthorized() throws Exception {
        loginRequest.setPassword("wrongPassword");
        Mockito.when(userRepository.findByUsername("admin")).thenReturn(Optional.of(validUser));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials."));
    }
}
