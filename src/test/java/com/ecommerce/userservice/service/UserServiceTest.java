package com.ecommerce.userservice.service;

import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("encodedPassword");

        // Set up JWT secret key
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        ReflectionTestUtils.setField(userService, "jwtSecret", secretKey.getEncoded());
        ReflectionTestUtils.setField(userService, "jwtExpirationMs", 86400000); // 24 hours
    }

    @Test
    void registerUser_ShouldEncodePasswordAndSaveUser() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.registerUser(newUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnJWT() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);

        // When
        String token = userService.authenticateUser("testuser", "plainPassword");

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // Verify JWT content
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("userId")).isEqualTo(1);
        
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("plainPassword", "encodedPassword");
    }

    @Test
    void authenticateUser_WithInvalidCredentials_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            userService.authenticateUser("testuser", "wrongPassword"));
        
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
    }

    @Test
    void authenticateUser_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            userService.authenticateUser("nonexistent", "password"));
        
        verify(userRepository).findByUsername("nonexistent");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void getUserById_WithExistingUser_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.getUserById(999L));
        verify(userRepository).findById(999L);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        
        List<User> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        assertThat(result.get(1).getUsername()).isEqualTo("user2");
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_WithExistingUser_ShouldReturnUpdatedUser() {
        // Given
        User updateData = new User();
        updateData.setUsername("updateduser");
        updateData.setEmail("updated@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        User result = userService.updateUser(1L, updateData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("updateduser");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WithNonExistentUser_ShouldThrowException() {
        // Given
        User updateData = new User();
        updateData.setUsername("updateduser");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.updateUser(999L, updateData));
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String validToken = Jwts.builder()
                .setSubject("testuser")
                .claim("userId", 1)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        // When
        boolean result = userService.validateToken(validToken);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean result = userService.validateToken(invalidToken);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Given
        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .claim("userId", 1)
                .setIssuedAt(new Date(System.currentTimeMillis() - 86400000))
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // Expired 1 hour ago
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        // When
        boolean result = userService.validateToken(expiredToken);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // When
        boolean result = userService.validateToken(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        // When
        boolean result = userService.validateToken("");

        // Then
        assertThat(result).isFalse();
    }
}