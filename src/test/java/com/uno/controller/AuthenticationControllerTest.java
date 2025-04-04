package com.uno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uno.dtos.JwtResponse;
import com.uno.dtos.LoginRequestDTO;
import com.uno.dtos.RegisterUserDTO;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.dtos.responseDto.Status;
import com.uno.entity.User;
import com.uno.security.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private RegisterUserDTO registerUserDTO;
    private LoginRequestDTO loginRequestDTO;
    private Long userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
        objectMapper = new ObjectMapper();

        userId = Math.abs(new Random().nextLong() %1000000000);
        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setUsername("testuser");
        registerUserDTO.setEmail("test@example.com");
        registerUserDTO.setPassword("password");

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("testuser");
        loginRequestDTO.setPassword("password");
    }

    @Test
    void register_ShouldReturnSuccess() throws Exception {
        // Arrange
        ResponseEntity<String> mockResponse = ResponseEntity.ok("user registered successfully!");
        doReturn(mockResponse).when(authenticationService).register(any(RegisterUserDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"body\":\"user registered successfully!\"}"));
    }

    @Test
    void authenticate_ShouldReturnJwtResponse() throws Exception {
        // Arrange
        JwtResponse jwtResponse = new JwtResponse("access-token", "refresh-token");
        ResponseEntity<JwtResponse> mockResponse = ResponseEntity.ok(jwtResponse);
        doReturn(mockResponse).when(authenticationService).login(any(LoginRequestDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.accessToken").value("access-token"))
                .andExpect(jsonPath("$.body.refreshToken").value("refresh-token"));
    }

    @Test
    void refreshToken_ShouldReturnRefreshedTokens() throws Exception {
        // Arrange
        JwtResponse jwtResponse = new JwtResponse("new-access-token", "new-refresh-token");
        ResponseEntity<JwtResponse> mockResponse = ResponseEntity.ok(jwtResponse);
        doReturn(mockResponse).when(authenticationService).refreshToken(any(MockHttpServletRequest.class), any(MockHttpServletResponse.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer refresh-token"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        // Arrange
        List<User> users = new ArrayList<>();
        User user = new User();
        user.setUserId(userId);
        user.setUsername("testuser");
        users.add(user);

        Status status = new Status();
        status.setCode("200");
        status.setDescription("No error");

        GeneralResponseWithData<List<User>> responseData = new GeneralResponseWithData<>(status, users);
        ResponseEntity<GeneralResponseWithData<List<User>>> mockResponse = ResponseEntity.ok(responseData);
        doReturn(mockResponse).when(authenticationService).getAllUsers();

        // Act & Assert
        mockMvc.perform(get("/api/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.status.code").value("200"))
                .andExpect(jsonPath("$.body.data[0].username").value("testuser"));
    }

    @Test
    void deleteUser_ShouldReturnSuccess() throws Exception {
        // Arrange
        ResponseEntity<String> mockResponse = ResponseEntity.ok("User deleted successfully!");
        doReturn(mockResponse).when(authenticationService).deleteUser(any(Long.class));

        // Act & Assert
        mockMvc.perform(delete("/api/auth/delete")
                        .param("id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"body\":\"User deleted successfully!\"}"));
    }

    @Test
    void resetPassword_ShouldReturnSuccess() throws Exception {
        // Arrange
        ResponseEntity<String> mockResponse = ResponseEntity.ok("Password reset successfully!");
        doReturn(mockResponse).when(authenticationService).resetPassword(anyString(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                        .param("username", "testuser")
                        .param("password", "oldPassword")
                        .param("newPassword", "newPassword"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"body\":\"Password reset successfully!\"}"));
    }

    @Test
    void forgotPassword_ShouldReturnSuccess() throws Exception {
        // Arrange
        ResponseEntity<String> mockResponse = ResponseEntity.ok("Password reset link has been sent to your email");
        doReturn(mockResponse).when(authenticationService).createPasswordResetToken(anyString());

        // Act & Assert
        mockMvc.perform(post("/api/auth/forgot-password")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"body\":\"Password reset link has been sent to your email\"}"));
    }

    @Test
    void validateResetToken_ShouldReturnValid() throws Exception {
        // Arrange
        ResponseEntity<String> mockResponse = ResponseEntity.ok("Token is valid");
        doReturn(mockResponse).when(authenticationService).validatePasswordResetToken(anyString());

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate-reset-token")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"body\":\"Token is valid\"}"));
    }

    @Test
    void setNewPassword_ShouldReturnSuccess() throws Exception {
        // Arrange
        ResponseEntity<String> mockResponse = ResponseEntity.ok("Password has been reset successfully");
        doReturn(mockResponse).when(authenticationService).setNewPasswordByToken(anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/api/auth/set-new-password")
                        .param("token", "valid-token")
                        .param("newPassword", "newPassword"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"body\":\"Password has been reset successfully\"}"));
    }
}