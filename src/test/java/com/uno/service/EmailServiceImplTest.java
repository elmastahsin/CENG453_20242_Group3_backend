package com.uno.service;

import com.uno.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://test.com");
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
    }

    @Test
    void shouldSendPasswordResetEmail() {
        // Arrange
        String email = "user@example.com";
        String username = "testUser";
        String token = "test-token";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendPasswordResetEmail(email, username, token);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldNotSendEmailWhenDisabled() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        String email = "user@example.com";
        String username = "testUser";
        String token = "test-token";

        // Act
        emailService.sendPasswordResetEmail(email, username, token);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}