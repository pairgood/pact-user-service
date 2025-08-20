package com.ecommerce.userservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    private Validator validator;
    private User user;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password123");
        user.setAddress("123 Test St");
        user.setPhoneNumber("+1-555-0101");
    }

    @Test
    void validUser_ShouldPassValidation() {
        // When
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        // Test all getters return expected values
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getFirstName()).isEqualTo("Test");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getAddress()).isEqualTo("123 Test St");
        assertThat(user.getPhoneNumber()).isEqualTo("+1-555-0101");
    }

    @Test
    void setters_ShouldUpdateValues() {
        // Given
        User newUser = new User();

        // When
        newUser.setId(2L);
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setPassword("newpassword");
        newUser.setAddress("456 New St");
        newUser.setPhoneNumber("+1-555-0102");

        // Then
        assertThat(newUser.getId()).isEqualTo(2L);
        assertThat(newUser.getUsername()).isEqualTo("newuser");
        assertThat(newUser.getEmail()).isEqualTo("new@example.com");
        assertThat(newUser.getFirstName()).isEqualTo("New");
        assertThat(newUser.getLastName()).isEqualTo("User");
        assertThat(newUser.getPassword()).isEqualTo("newpassword");
        assertThat(newUser.getAddress()).isEqualTo("456 New St");
        assertThat(newUser.getPhoneNumber()).isEqualTo("+1-555-0102");
    }

    @Test
    void equals_WithSameId_ShouldReturnTrue() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("user2");

        // When & Then
        assertThat(user1).isEqualTo(user2);
    }

    @Test
    void equals_WithDifferentId_ShouldReturnFalse() {
        // Given
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        // When & Then
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    void equals_WithNullId_ShouldCompareByReference() {
        // Given
        User user1 = new User();
        User user2 = new User();

        // When & Then
        assertThat(user1).isNotEqualTo(user2);
        assertThat(user1).isEqualTo(user1);
    }

    @Test
    void hashCode_WithSameId_ShouldReturnSameHashCode() {
        // Given
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        // When & Then
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void toString_ShouldContainKeyFields() {
        // When
        String toString = user.toString();

        // Then
        assertThat(toString).contains("User");
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("username='testuser'");
        assertThat(toString).contains("email='test@example.com'");
    }

    @Test
    void defaultConstructor_ShouldCreateEmptyUser() {
        // When
        User emptyUser = new User();

        // Then
        assertThat(emptyUser.getId()).isNull();
        assertThat(emptyUser.getUsername()).isNull();
        assertThat(emptyUser.getEmail()).isNull();
        assertThat(emptyUser.getFirstName()).isNull();
        assertThat(emptyUser.getLastName()).isNull();
        assertThat(emptyUser.getPassword()).isNull();
        assertThat(emptyUser.getAddress()).isNull();
        assertThat(emptyUser.getPhoneNumber()).isNull();
    }
}