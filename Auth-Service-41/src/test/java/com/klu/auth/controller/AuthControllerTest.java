
package com.klu.auth.controller;

import com.klu.auth.entity.User;
import com.klu.auth.service.JWTService;
import com.klu.auth.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private AuthController authController;

    private User createUser(
            String username,
            String password,
            String role) {

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        return user;
    }

    // Test 1: Successful user registration
    @Test
    void shouldRegisterUserSuccessfully() {

        User inputUser =
                createUser("Aravind", "Test@123", "USER");

        User savedUser =
                createUser("Aravind", "hashed-password", "USER");

        when(userService.registerUser(any(User.class)))
                .thenReturn(savedUser);

        ResponseEntity<?> response =
                authController.register(inputUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertNotNull(body);
        assertEquals(
                "User registered successfully",
                body.get("message")
        );
        assertEquals("Aravind", body.get("username"));
        assertEquals("USER", body.get("role"));

        verify(userService).registerUser(any(User.class));
    }

    // Test 2: Registration rejected when username is blank
    @Test
    void shouldRejectRegistrationWithBlankUsername() {

        User user =
                createUser(" ", "Test@123", "USER");

        ResponseEntity<?> response =
                authController.register(user);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                "Username is required",
                response.getBody()
        );

        verifyNoInteractions(userService);
    }

    // Test 3: Registration rejected when password is blank
    @Test
    void shouldRejectRegistrationWithBlankPassword() {

        User user =
                createUser("Aravind", " ", "USER");

        ResponseEntity<?> response =
                authController.register(user);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertEquals(
                "Password is required",
                response.getBody()
        );

        verifyNoInteractions(userService);
    }

    // Test 4: Duplicate username returns conflict
    @Test
    void shouldRejectDuplicateUsername() {

        User user =
                createUser("Aravind", "Test@123", "USER");

        when(userService.registerUser(any(User.class)))
                .thenThrow(
                        new RuntimeException(
                                "Username already exists"
                        )
                );

        ResponseEntity<?> response =
                authController.register(user);

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertEquals(
                "Username already exists",
                response.getBody()
        );
    }

    // Test 5: Successful login returns a JWT
    @Test
    void shouldLoginSuccessfullyAndGenerateToken() {

        User user =
                createUser("Aravind", "hashed-password", "USER");

        when(userService.findByUsername("Aravind"))
                .thenReturn(user);

        when(userService.checkPassword(
                "Test@123",
                "hashed-password"
        )).thenReturn(true);

        when(jwtService.generateToken("Aravind", "USER"))
                .thenReturn("mock-jwt-token");

        Map<String, String> request = Map.of(
                "username", "Aravind",
                "password", "Test@123"
        );

        ResponseEntity<?> response =
                authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) response.getBody();

        assertNotNull(body);
        assertEquals("Login successful", body.get("message"));
        assertEquals("Aravind", body.get("username"));
        assertEquals("USER", body.get("role"));
        assertEquals("mock-jwt-token", body.get("token"));

        verify(jwtService).generateToken("Aravind", "USER");
    }

    // Test 6: Unknown username is rejected
    @Test
    void shouldRejectUnknownUsername() {

        when(userService.findByUsername("Unknown"))
                .thenReturn(null);

        Map<String, String> request = Map.of(
                "username", "Unknown",
                "password", "Test@123"
        );

        ResponseEntity<?> response =
                authController.login(request);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );

        assertEquals(
                "Invalid credentials",
                response.getBody()
        );

        verify(userService, never())
                .checkPassword(any(), any());

        verifyNoInteractions(jwtService);
    }

    // Test 7: Incorrect password is rejected
    @Test
    void shouldRejectIncorrectPassword() {

        User user =
                createUser("Aravind", "hashed-password", "USER");

        when(userService.findByUsername("Aravind"))
                .thenReturn(user);

        when(userService.checkPassword(
                "Wrong@123",
                "hashed-password"
        )).thenReturn(false);

        Map<String, String> request = Map.of(
                "username", "Aravind",
                "password", "Wrong@123"
        );

        ResponseEntity<?> response =
                authController.login(request);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );

        assertEquals(
                "Invalid credentials",
                response.getBody()
        );

        verify(jwtService, never())
                .generateToken(anyString(), anyString());
    }
}
