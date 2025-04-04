package com.uno.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    private final String TO_EMAIL = "test@example.com";
    private final String TOKEN = "reset-token-123";
    private final String FROM_EMAIL = "noreply@unoapp.com";
    private final String FRONTEND_URL = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        // Set required properties using reflection
        ReflectionTestUtils.setField(emailService, "fromEmail", FROM_EMAIL);
        ReflectionTestUtils.setField(emailService, "frontendUrl", FRONTEND_URL);
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
    }

    @Test
    void testSendPasswordResetEmail_Success() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendPasswordResetEmail(TO_EMAIL, TOKEN);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
        assertEquals(FROM_EMAIL, sentMessage.getFrom());
        assertEquals(TO_EMAIL, sentMessage.getTo()[0]);
        assertEquals("Password Reset Request", sentMessage.getSubject());
        
        String messageText = sentMessage.getText();
        assertTrue(messageText.contains(FRONTEND_URL + "/reset-password?token=" + TOKEN));
        assertTrue(messageText.contains("You have requested to reset your password"));
        assertTrue(messageText.contains("This link will expire in 24 hours"));
    }

    @Test
    void testSendPasswordResetEmail_WhenDisabled() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);

        // Act
        emailService.sendPasswordResetEmail(TO_EMAIL, TOKEN);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendPasswordResetEmail_MailException() {
        // Arrange
        doThrow(new MailSendException("Failed to send mail"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(MailException.class, () -> 
            emailService.sendPasswordResetEmail(TO_EMAIL, TOKEN));
        
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
