package com.Car.Dealership.Inventory.System.service.impl;

import com.Car.Dealership.Inventory.System.entity.Role;
import com.Car.Dealership.Inventory.System.entity.User;
import com.Car.Dealership.Inventory.System.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_ValidUser_ReturnsSavedUser() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User savedUser = authService.register(testUser);

        // Assert
        assertNotNull(savedUser);
        assertEquals(testUser.getUsername(), savedUser.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(testUser));
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_UsernameAlreadyExists_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(testUser));
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ValidCredentials_ReturnsToken() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        String token = authService.login(testUser.getEmail(), testUser.getPassword());

        // Assert
        assertNotNull(token);
        assertTrue(token.contains("dummy-jwt-token-for-" + testUser.getEmail()));
    }

    @Test
    void login_InvalidEmail_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login("wrong@example.com", "password123"));
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(testUser.getEmail(), "wrongpassword"));
        assertEquals("Invalid email or password", exception.getMessage());
    }
}
