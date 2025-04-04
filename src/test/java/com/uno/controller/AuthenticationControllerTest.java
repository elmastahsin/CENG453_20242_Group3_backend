package com.uno.controller;

import com.uno.dtos.LoginRequestDTO;
import com.uno.dtos.RegisterUserDTO;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.entity.User;
import com.uno.security.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthenticationController authenticationController;

    private LoginRequestDTO loginRequestDTO;
    private RegisterUserDTO registerUserDTO;

    @BeforeEach
    void setUp() {
        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("testuser");
        loginRequestDTO.setPassword("password");

        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setUsername("newuser");
        registerUserDTO.setPassword("password");
    }

    @Test
    void loginShouldReturnAuthenticationResponse() {
        // Arrange
        when(authenticationService.login(any(LoginRequestDTO.class)))
                .thenReturn((ResponseEntity) ResponseEntity.ok("Login successful"));

        // Act
        ResponseEntity<?> response = authenticationController.authenticate(loginRequestDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void registerShouldReturnSuccessResponse() {
        // Arrange
        when(authenticationService.register(any(RegisterUserDTO.class)))
                .thenReturn((ResponseEntity) ResponseEntity.ok("Registration successful"));

        // Act
        ResponseEntity<?> response = authenticationController.register(registerUserDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllUsersShouldReturnUsersList() {
        // Arrange
        List<User> users = new ArrayList<>();
        GeneralResponseWithData<List<User>> responseData = new GeneralResponseWithData<>(null, users);
        when(authenticationService.getAllUsers())
                .thenReturn(ResponseEntity.ok(responseData));

        // Act
        ResponseEntity<GeneralResponseWithData<List<User>>> response = authenticationController.getAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void forgotPasswordShouldProcessEmailRequest() {
        // Arrange
        when(authenticationService.createPasswordResetToken(anyString()))
                .thenReturn((ResponseEntity) ResponseEntity.ok("Password reset link sent"));

        // Act
        ResponseEntity<?> response = authenticationController.forgotPassword("test@example.com");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void validateResetTokenShouldCheckToken() {
        // Arrange
        when(authenticationService.validatePasswordResetToken(anyString()))
                .thenReturn((ResponseEntity) ResponseEntity.ok("Token is valid"));

        // Act
        ResponseEntity<?> response = authenticationController.validateResetToken("valid-token");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void setNewPasswordShouldUpdatePassword() {
        // Arrange
        when(authenticationService.setNewPasswordByToken(anyString(), anyString()))
                .thenReturn((ResponseEntity) ResponseEntity.ok("Password updated"));

        // Act
        ResponseEntity<?> response = authenticationController.setNewPassword("valid-token", "newPassword");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void refreshTokenShouldProcessRefreshRequest() throws IOException {
        // Arrange
        when(authenticationService.refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn((ResponseEntity) ResponseEntity.ok("Token refreshed"));

        // Act
        ResponseEntity<?> result = authenticationController.refreshToken(request, response);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
    @Test
    void deleteUserShouldRemoveUser() {
        // Arrange
        when(authenticationService.deleteUser(anyLong()))
                .thenReturn((ResponseEntity) ResponseEntity.ok("User deleted successfully!"));

        // Act
        ResponseEntity<?> response = authenticationController.deleteUser(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void resetPasswordShouldUpdatePassword() {
        // Arrange
        when(authenticationService.resetPassword(anyString(), anyString(), anyString()))
                .thenReturn((ResponseEntity) ResponseEntity.ok("Password reset successfully!"));

        // Act
        ResponseEntity<?> response = authenticationController.resetPassword("username", "oldPassword", "newPassword");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}