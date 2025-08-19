package com.ecommerce.userservice.repository;

import com.ecommerce.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("encodedPassword");
        testUser.setAddress("123 Test St");
        testUser.setPhoneNumber("+1-555-0101");
    }

    @Test
    void findByUsername_WithExistingUser_ShouldReturnUser() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByUsername_WithNonExistentUser_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_WithExistingUser_ShouldReturnUser() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_WithNonExistentEmail_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUsername_WithExistingUser_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_WithNonExistentUser_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WithNonExistentEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void saveUser_ShouldPersistUser() {
        // When
        User saved = userRepository.save(testUser);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("testuser");
        assertThat(saved.getEmail()).isEqualTo("test@example.com");

        // Verify persistence
        User found = entityManager.find(User.class, saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("testuser");
    }

    @Test
    void updateUser_ShouldModifyExistingUser() {
        // Given
        User saved = entityManager.persistAndFlush(testUser);
        Long userId = saved.getId();

        // When
        saved.setUsername("updateduser");
        saved.setEmail("updated@example.com");
        User updated = userRepository.save(saved);

        // Then
        assertThat(updated.getId()).isEqualTo(userId);
        assertThat(updated.getUsername()).isEqualTo("updateduser");
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");

        // Verify persistence
        User found = entityManager.find(User.class, userId);
        assertThat(found.getUsername()).isEqualTo("updateduser");
        assertThat(found.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void deleteUser_ShouldRemoveUser() {
        // Given
        User saved = entityManager.persistAndFlush(testUser);
        Long userId = saved.getId();

        // When
        userRepository.delete(saved);

        // Then
        User found = entityManager.find(User.class, userId);
        assertThat(found).isNull();
    }
}