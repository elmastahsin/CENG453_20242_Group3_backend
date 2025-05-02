package com.uno.controller;

import com.uno.dtos.LoginRequestDTO;
import com.uno.dtos.RegisterUserDTO;
import com.uno.dtos.responseDto.GeneralResponse;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.dtos.responseDto.Status;
import com.uno.entity.User;
import com.uno.security.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.times;

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
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("testuser");
        loginRequestDTO.setPassword("password");

        // Use raw ResponseEntity type
        ResponseEntity mockResponse = ResponseEntity.ok("Login successful");

        // Use raw type in when
        when(authenticationService.login(any(LoginRequestDTO.class)))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<?> response = authenticationController.authenticate(loginRequestDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify method was called with correct parameters
        verify(authenticationService).login(loginRequestDTO);
    }

    @Test
    @SuppressWarnings("unchecked") // Suppressing unchecked cast warnings
    void loginShouldHandleAuthenticationFailure() {
        // Arrange
        LoginRequestDTO invalidLogin = new LoginRequestDTO();
        invalidLogin.setUsername("wronguser");
        invalidLogin.setPassword("wrongpassword");

        // Create response using raw type to avoid generic type issues
        ResponseEntity errorResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid username or password");

        // Mock using raw types
        when(authenticationService.login(any())).thenReturn(errorResponse);

        // Act
        ResponseEntity response = authenticationController.authenticate(invalidLogin);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody());

        // Verify the service method was called
        verify(authenticationService).login(any());
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
    void getAllUsersShouldHandleEmptyList() {
        // Arrange
        List<User> emptyUsers = new ArrayList<>();
        GeneralResponseWithData<List<User>> responseData = new GeneralResponseWithData<>(null, emptyUsers);
        when(authenticationService.getAllUsers())
                .thenReturn(ResponseEntity.ok(responseData));

        // Act
        ResponseEntity<GeneralResponseWithData<List<User>>> response = authenticationController.getAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    void resetPasswordShouldHandleException() {
        // Arrange
        String username = "username";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        // Use raw ResponseEntity type
        ResponseEntity mockErrorResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password reset failed");

        when(authenticationService.resetPassword(anyString(), anyString(), anyString()))
                .thenReturn(mockErrorResponse);

        // Act
        ResponseEntity<?> response = authenticationController.resetPassword(username, oldPassword, newPassword);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Password reset failed", response.getBody());

        // Optional: Verify the method was called with the correct parameters
        verify(authenticationService).resetPassword(username, oldPassword, newPassword);
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
        when(authenticationService.setNewPasswordByToken(anyString(), anyString(), anyString()))
                .thenReturn((ResponseEntity) ResponseEntity.ok("Password updated successfully"));

        // Act
        ResponseEntity<?> response = authenticationController.setNewPassword("valid-token", "username", "new-password");

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