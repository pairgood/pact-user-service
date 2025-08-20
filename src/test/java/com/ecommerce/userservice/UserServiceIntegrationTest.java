package com.ecommerce.userservice;

import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
        assertThat(userService).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Test
    void fullUserLifecycle_ShouldWork() {
        // Create a new user
        User newUser = new User();
        newUser.setUsername("integrationuser");
        newUser.setEmail("integration@test.com");
        newUser.setPassword("password123");
        newUser.setFirstName("Integration");
        newUser.setLastName("Test");

        // Register user via service
        User createdUser = userService.registerUser(newUser);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo("integrationuser");

        // Verify user exists in database
        User foundUser = userRepository.findById(createdUser.getId()).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("integrationuser");

        // Test authentication
        String token = userService.authenticateUser("integrationuser", "password123");
        assertThat(token).isNotEmpty();

        // Test token validation
        assertThat(userService.validateToken(token)).isTrue();

        // Get user by ID
        User retrievedUser = userService.getUserById(createdUser.getId());
        assertThat(retrievedUser.getUsername()).isEqualTo("integrationuser");

        // Update user
        User updateData = new User();
        updateData.setUsername("integrationuser"); // Keep existing required fields
        updateData.setEmail("integration@test.com");
        updateData.setPassword("password123");
        updateData.setFirstName("Updated");
        updateData.setLastName("User");
        
        User updatedUser = userService.updateUser(createdUser.getId(), updateData);
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");

        // Get all users
        var allUsers = userService.getAllUsers();
        assertThat(allUsers).hasSize(1);
        assertThat(allUsers.get(0).getUsername()).isEqualTo("integrationuser");
    }

    @Test
    void userService_ShouldValidateTokensCorrectly() {
        // Test with valid user
        User user = new User();
        user.setUsername("tokenuser");
        user.setEmail("token@test.com");
        user.setPassword("password123");

        User savedUser = userService.registerUser(user);
        String token = userService.authenticateUser("tokenuser", "password123");

        // Valid token should return true
        assertThat(userService.validateToken(token)).isTrue();

        // Invalid token should return false
        assertThat(userService.validateToken("invalid.token")).isFalse();
        assertThat(userService.validateToken(null)).isFalse();
        assertThat(userService.validateToken("")).isFalse();
    }

    @Test
    void userRepository_ShouldSupportCustomQueries() {
        // Create test user
        User user = new User();
        user.setUsername("queryuser");
        user.setEmail("query@test.com");
        user.setPassword("password123");
        userRepository.save(user);

        // Test findByUsername
        assertThat(userRepository.findByUsername("queryuser")).isPresent();
        assertThat(userRepository.findByUsername("nonexistent")).isEmpty();

        // Test findByEmail
        assertThat(userRepository.findByEmail("query@test.com")).isPresent();
        assertThat(userRepository.findByEmail("nonexistent@test.com")).isEmpty();

        // Test existsByUsername
        assertThat(userRepository.existsByUsername("queryuser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();

        // Test existsByEmail
        assertThat(userRepository.existsByEmail("query@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@test.com")).isFalse();
    }
}