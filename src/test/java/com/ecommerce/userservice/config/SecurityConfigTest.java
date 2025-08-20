package com.ecommerce.userservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
        // Test that SecurityConfig can be loaded
        assertThat(securityConfig).isNotNull();
    }

    @Test
    void passwordEncoder_ShouldBeConfigured() {
        // Test that PasswordEncoder bean is created
        assertThat(passwordEncoder).isNotNull();
        
        // Test password encoding functionality
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
        assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
    }

    @Test
    void filterChain_ShouldBeConfigured() throws Exception {
        // Test that SecurityFilterChain can be created
        SecurityFilterChain filterChain = securityConfig.filterChain(null);
        assertThat(filterChain).isNotNull();
    }
}