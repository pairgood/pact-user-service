package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.service.UserService;
import com.ecommerce.userservice.telemetry.TelemetryClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "API for user management operations including registration, authentication, and profile management")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TelemetryClient telemetryClient;
    
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account with the provided user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user data or username already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> registerUser(@RequestBody User user, HttpServletRequest request) {
        String traceId = telemetryClient.startTrace("register_user", "POST", request.getRequestURL().toString(), null);
        
        try {
            telemetryClient.logEvent("User registration started for: " + user.getUsername(), "INFO");
            User savedUser = userService.registerUser(user);
            telemetryClient.finishTrace("register_user", 200, null);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            telemetryClient.finishTrace("register_user", 500, e.getMessage());
            throw e;
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token for accessing protected resources")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, token returned"),
        @ApiResponse(responseCode = "401", description = "Invalid username or password"),
        @ApiResponse(responseCode = "400", description = "Invalid login request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        telemetryClient.startTrace("login_user", "POST", request.getRequestURL().toString(), null);
        
        try {
            String token = userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
            telemetryClient.finishTrace("login_user", 200, null);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            telemetryClient.finishTrace("login_user", 401, e.getMessage());
            throw e;
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user's information using their unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found and returned successfully"),
        @ApiResponse(responseCode = "404", description = "User not found with the provided ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> getUserById(
        @Parameter(description = "Unique identifier of the user", required = true, example = "1")
        @PathVariable Long id, HttpServletRequest request) {
        String traceId = telemetryClient.startTrace("get_user", "GET", request.getRequestURL().toString(), id.toString());
        
        try {
            User user = userService.getUserById(id);
            telemetryClient.finishTrace("get_user", 200, null);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            telemetryClient.finishTrace("get_user", 404, e.getMessage());
            throw e;
        }
    }
    
    @GetMapping
    @Operation(summary = "Retrieve all users", description = "Returns a list of all registered users (admin access typically required)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<User>> getAllUsers(HttpServletRequest request) {
        telemetryClient.startTrace("get_all_users", "GET", request.getRequestURL().toString(), null);
        
        try {
            List<User> users = userService.getAllUsers();
            telemetryClient.finishTrace("get_all_users", 200, null);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            telemetryClient.finishTrace("get_all_users", 500, e.getMessage());
            throw e;
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user's information with the provided data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user data provided"),
        @ApiResponse(responseCode = "404", description = "User not found with the provided ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> updateUser(
        @Parameter(description = "Unique identifier of the user to update", required = true, example = "1")
        @PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        telemetryClient.startTrace("update_user", "PUT", request.getRequestURL().toString(), id.toString());
        
        try {
            User updatedUser = userService.updateUser(id, user);
            telemetryClient.finishTrace("update_user", 200, null);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            telemetryClient.finishTrace("update_user", 500, e.getMessage());
            throw e;
        }
    }
    
    @GetMapping("/validate/{token}")
    @Operation(summary = "Validate JWT token", description = "Validates a JWT token and returns whether it is still valid and not expired")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token validation result returned"),
        @ApiResponse(responseCode = "400", description = "Invalid token format"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Boolean> validateToken(
        @Parameter(description = "JWT token to validate", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @PathVariable String token, HttpServletRequest request) {
        telemetryClient.startTrace("validate_token", "GET", request.getRequestURL().toString(), null);
        
        try {
            boolean isValid = userService.validateToken(token);
            telemetryClient.finishTrace("validate_token", 200, null);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            telemetryClient.finishTrace("validate_token", 500, e.getMessage());
            throw e;
        }
    }
    
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}