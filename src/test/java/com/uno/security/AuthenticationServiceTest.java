package com.uno.security;

import com.uno.dtos.JwtResponse;
import com.uno.dtos.LoginRequestDTO;
import com.uno.dtos.RegisterUserDTO;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.entity.PasswordResetToken;
import com.uno.entity.User;
import com.uno.repository.UserRepository;
import com.uno.service.EmailService;
import com.uno.util.MapperUtil;
import org.checkerframework.common.aliasing.qual.MaybeAliased;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private MapperUtil mapper;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private RegisterUserDTO registerUserDTO;
    private LoginRequestDTO loginRequestDTO;
    private PasswordResetToken resetToken;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = Math.abs(new Random().nextLong()) % 1000000; // Generate a random userId
        testUser = new User();
        testUser.setUserId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setGameCount(0L);
        testUser.setVerified(false);
        testUser.setAvatar("https://www.gravatar.com/avatar/?d=mp");
        testUser.setLastLogin(LocalDateTime.now());

        registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setUsername("testuser");
        registerUserDTO.setEmail("test@example.com");
        registerUserDTO.setPassword("password");

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("testuser");
        loginRequestDTO.setPassword("password");

        resetToken = new PasswordResetToken();
        resetToken.setId(UUID.randomUUID());
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUser(testUser);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
    }

    @Test
    void login_WithValidCredentials_ReturnsJwtResponse() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(loginRequestDTO.getUsername())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(loginRequestDTO.getUsername())).thenReturn("refresh-token");
        when(userRepository.findByUsername(loginRequestDTO.getUsername())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authenticationService.login(loginRequestDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof JwtResponse);
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals("access-token", jwtResponse.getAccessToken());
        assertEquals("refresh-token", jwtResponse.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_WithInvalidCredentials_ReturnsBadRequest() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Act
        ResponseEntity<?> response = authenticationService.login(loginRequestDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody());
    }

    @Test
    void register_WithNewUser_ReturnsSuccessMessage() {
        // Arrange
        when(userRepository.findByUsername(registerUserDTO.getUsername())).thenReturn(Optional.empty());
        when(mapper.convert(any(RegisterUserDTO.class), any(User.class))).thenReturn(testUser);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");

        // Act
        ResponseEntity<?> response = authenticationService.register(registerUserDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user registered successfully!", response.getBody());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExistingUsername_ReturnsBadRequest() {
        // Arrange
        when(userRepository.findByUsername(registerUserDTO.getUsername())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authenticationService.register(registerUserDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username is already taken!", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_ReturnsUsersList() {
        // Arrange
        List<User> users = new ArrayList<>();
        users.add(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        ResponseEntity<GeneralResponseWithData<List<User>>> response = authenticationService.getAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(testUser, response.getBody().getData().get(0));
    }

    @Test
    void deleteUser_WithExistingId_ReturnsSuccessMessage() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authenticationService.deleteUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully!", response.getBody());
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_WithNonExistingId_ReturnsBadRequest() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = authenticationService.deleteUser(userId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not found!", response.getBody());
        verify(userRepository, never()).deleteById(any(Long.class));
    }

    @Test
    void resetPassword_WithValidCredentials_ReturnsSuccessMessage() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // Act
        ResponseEntity<?> response = authenticationService.resetPassword("testuser", "oldPassword", "newPassword");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successfully!", response.getBody());
        verify(userRepository).save(testUser);
    }

    @Test
    void resetPassword_WithIncorrectOldPassword_ReturnsBadRequest() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("incorrectPassword", testUser.getPassword())).thenReturn(false);

        // Act
        ResponseEntity<?> response = authenticationService.resetPassword("testuser", "incorrectPassword", "newPassword");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Incorrect password!", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createPasswordResetToken_WithValidEmail_SendsEmailAndReturnsSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(resetToken);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        // Act
        ResponseEntity<?> response = authenticationService.createPasswordResetToken("test@example.com");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset link has been sent to your email", response.getBody());
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    void createPasswordResetToken_WithEmailSendingFailure_ReturnsFallbackMessage() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(resetToken);
        doThrow(new MailException("Failed to send email") {}).when(emailService).sendPasswordResetEmail(anyString(), anyString());

        // Act
        ResponseEntity<?> response = authenticationService.createPasswordResetToken("test@example.com");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Please use this token to reset your password"));
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void validatePasswordResetToken_WithValidToken_ReturnsSuccess() {
        // Arrange
        String token = "valid-token";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // Act
        ResponseEntity<?> response = authenticationService.validatePasswordResetToken(token);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Token is valid", response.getBody());
    }

    @Test
    void validatePasswordResetToken_WithExpiredToken_ReturnsBadRequest() {
        // Arrange
        String token = "expired-token";
        resetToken.setExpiryDate(LocalDateTime.now().minusHours(1)); // Expired token
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // Act
        ResponseEntity<?> response = authenticationService.validatePasswordResetToken(token);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Password reset token has expired", response.getBody());
        verify(tokenRepository).delete(resetToken);
    }

    @Test
    void setNewPasswordByToken_WithValidToken_ReturnsSuccess() {
        // Arrange
        String token = "valid-token";
        String newPassword = "newPassword";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // Act
        ResponseEntity<?> response = authenticationService.setNewPasswordByToken(token, newPassword);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password has been reset successfully", response.getBody());
        verify(userRepository).save(testUser);
        verify(tokenRepository).delete(resetToken);
    }

    @Test
    void setNewPasswordByToken_WithExpiredToken_ReturnsBadRequest() {
        // Arrange
        String token = "expired-token";
        String newPassword = "newPassword";
        resetToken.setExpiryDate(LocalDateTime.now().minusHours(1)); // Expired token
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // Act
        ResponseEntity<?> response = authenticationService.setNewPasswordByToken(token, newPassword);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Password reset token has expired", response.getBody());
        verify(userRepository, never()).save(any(User.class));
        verify(tokenRepository).delete(resetToken);
    }
}