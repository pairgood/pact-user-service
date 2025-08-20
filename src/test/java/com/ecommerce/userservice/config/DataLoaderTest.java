package com.ecommerce.userservice.config;

import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataLoaderTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataLoader dataLoader;


    @Test
    void run_WithEmptyDatabase_ShouldLoadSeedData() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // When
        dataLoader.run("arg1", "arg2");

        // Then
        verify(userRepository).count();
        verify(userRepository, times(8)).save(any(User.class));
    }

    @Test
    void run_WithExistingData_ShouldNotLoadSeedData() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(5L);

        // When
        dataLoader.run("arg1", "arg2");

        // Then
        verify(userRepository).count();
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void run_ShouldEncodePasswordsForAllUsers() throws Exception {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // When
        dataLoader.run();

        // Then
        verify(passwordEncoder, times(8)).encode("password123");
    }
}