package com.uno.service.impl;

import com.uno.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;
    
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Would have sent password reset to: {} with token: {}", to, token);
            logger.info("Password reset link would be: {}/reset-password?token={}", frontendUrl, token);
            return;
        }
        
        SimpleMailMessage message = new SimpleMailMessage();
        
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Password Reset Request");
        
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        
        message.setText("Hello,\n\n" +
                "You have requested to reset your password. " +
                "Please click on the link below to reset your password:\n\n" +
                resetUrl + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "Regards,\nThe UNO Game Team");
        
        try {
            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", to);
        } catch (MailException e) {
            logger.error("Failed to send password reset email to: {}", to, e);
            throw e;
        }
    }
}
