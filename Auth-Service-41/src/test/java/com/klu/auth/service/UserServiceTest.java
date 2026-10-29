
package com.klu.auth.service;

import com.klu.auth.entity.User;
import com.klu.auth.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User createUser() {
        User user = new User();
        user.setUsername("Aravind");
        user.setPassword("Test@123");
        user.setRole("ADMIN");
        return user;
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        User user = createUser();

        when(userRepository.existsByUsername("Aravind"))
                .thenReturn(false);

        when(passwordEncoder.encode("Test@123"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.registerUser(user);

        assertNotNull(result);
        assertEquals("Aravind", result.getUsername());
        assertEquals("USER", result.getRole());
        assertEquals("hashed-password", result.getPassword());

        verify(passwordEncoder).encode("Test@123");
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectDuplicateUsername() {
        User user = createUser();

        when(userRepository.existsByUsername("Aravind"))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.registerUser(user)
        );

        assertEquals(
                "Username already exists",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldFindUserByUsername() {
        User user = createUser();

        when(userRepository.findByUsername("Aravind"))
                .thenReturn(Optional.of(user));

        User result = userService.findByUsername("Aravind");

        assertNotNull(result);
        assertEquals("Aravind", result.getUsername());

        verify(userRepository).findByUsername("Aravind");
    }

    @Test
    void shouldReturnNullWhenUserDoesNotExist() {
        when(userRepository.findByUsername("Unknown"))
                .thenReturn(Optional.empty());

        User result = userService.findByUsername("Unknown");

        assertNull(result);
    }

    @Test
    void shouldAcceptCorrectPassword() {
        when(passwordEncoder.matches(
                "Test@123",
                "hashed-password"
        )).thenReturn(true);

        boolean result = userService.checkPassword(
                "Test@123",
                "hashed-password"
        );

        assertTrue(result);
    }

    @Test
    void shouldRejectIncorrectPassword() {
        when(passwordEncoder.matches(
                "Wrong@123",
                "hashed-password"
        )).thenReturn(false);

        boolean result = userService.checkPassword(
                "Wrong@123",
                "hashed-password"
        );

        assertFalse(result);
    }
}
