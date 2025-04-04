package com.uno.security;

import com.uno.dtos.JwtResponse;
import com.uno.dtos.LoginRequestDTO;
import com.uno.dtos.RegisterUserDTO;
import com.uno.entity.PasswordResetToken;
import com.uno.entity.User;
import com.uno.repository.UserRepository;
import com.uno.service.EmailService;
import com.uno.util.MapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

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

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private LoginRequestDTO loginRequest;
    private RegisterUserDTO registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");

        loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        registerRequest = new RegisterUserDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password");
    }

    @Test
    void loginShouldReturnTokensWhenCredentialsValid() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(anyString())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authenticationService.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof JwtResponse);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createPasswordResetTokenShouldSendEmail() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<?> response = authenticationService.createPasswordResetToken("test@example.com");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void validatePasswordResetTokenShouldReturnValidForActiveToken() {
        // Arrange
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid-token");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        // Act
        ResponseEntity<?> response = authenticationService.validatePasswordResetToken("valid-token");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Token is valid", response.getBody());
    }
}