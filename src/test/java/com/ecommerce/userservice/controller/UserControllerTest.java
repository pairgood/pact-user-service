package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.service.UserService;
import com.ecommerce.userservice.telemetry.TelemetryClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private TelemetryClient telemetryClient;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("encodedPassword");
        testUser.setAddress("123 Test St");
        testUser.setPhoneNumber("+1-555-0101");

        // Mock telemetry client to prevent null pointer exceptions
        when(telemetryClient.startTrace(anyString(), anyString(), anyString(), anyString())).thenReturn("trace-123");
        doNothing().when(telemetryClient).finishTrace(anyString(), anyInt(), anyString());
        doNothing().when(telemetryClient).logEvent(anyString(), anyString());
    }

    @Test
    void registerUser_ShouldReturnCreatedUser() throws Exception {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password123");

        when(userService.registerUser(any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).registerUser(any(User.class));
        verify(telemetryClient).startTrace(eq("register_user"), eq("POST"), anyString(), isNull());
        verify(telemetryClient).finishTrace(eq("register_user"), eq(200), isNull());
    }

    @Test
    void registerUser_ShouldHandleException() throws Exception {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");

        when(userService.registerUser(any(User.class)))
                .thenThrow(new RuntimeException("Registration failed"));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser))
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(telemetryClient).finishTrace(eq("register_user"), eq(500), eq("Registration failed"));
    }

    @Test
    void loginUser_ShouldReturnToken() throws Exception {
        // Given
        UserController.LoginRequest loginRequest = new UserController.LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        when(userService.authenticateUser("testuser", "password123")).thenReturn("jwt-token");

        // When & Then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token"));

        verify(userService).authenticateUser("testuser", "password123");
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).getUserById(1L);
        verify(telemetryClient).startTrace(eq("get_user"), eq("GET"), anyString(), eq("1"));
        verify(telemetryClient).finishTrace(eq("get_user"), eq(200), isNull());
    }

    @Test
    void getUserById_ShouldHandleNotFound() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isInternalServerError());

        verify(telemetryClient).finishTrace(eq("get_user"), eq(404), eq("User not found"));
    }

    @Test
    @WithMockUser
    void getAllUsers_ShouldReturnUserList() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("testuser"));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");

        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"));

        verify(userService).updateUser(eq(1L), any(User.class));
    }

    @Test
    @WithMockUser
    void validateToken_ShouldReturnValidationResult() throws Exception {
        // Given
        when(userService.validateToken("valid-token")).thenReturn(true);
        when(userService.validateToken("invalid-token")).thenReturn(false);

        // When & Then - Valid token
        mockMvc.perform(get("/api/users/validate/valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // When & Then - Invalid token
        mockMvc.perform(get("/api/users/validate/invalid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(userService).validateToken("valid-token");
        verify(userService).validateToken("invalid-token");
    }
}