package com.uno.service.impl;

import com.uno.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String to, String token, String userName) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Would have sent password reset to: {} with token: {}", to, token);
            logger.info("Password reset link would be: {}/reset-password?token={}", frontendUrl, token);
            return;
        }

        String resetLink = frontendUrl + "/reset-password?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Şifrenizi Sıfırlayın");

            // HTML içeriği oluştur
            String htmlContent = "<!DOCTYPE html>\n" +
                    "<html lang=\"tr\">\n" +
                    "<head>\n" +
                    "  <meta charset=\"UTF-8\">\n" +
                    "  <title>Şifrenizi Sıfırlayın</title>\n" +
                    "  <style>\n" +
                    "    body { font-family: Arial, sans-serif; color: #333; }\n" +
                    "    .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                    "    a.button {\n" +
                    "      display: inline-block;\n" +
                    "      padding: 10px 20px;\n" +
                    "      background-color: #1e88e5;\n" +
                    "      color: #fff;\n" +
                    "      text-decoration: none;\n" +
                    "      border-radius: 4px;\n" +
                    "    }\n" +
                    "    .footer { font-size: 0.85em; color: #777; margin-top: 30px; }\n" +
                    "    .token { background: #f4f4f4; padding: 10px; border-radius: 4px; word-break: break-all; }\n" +
                    "  </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "  <div class=\"container\">\n" +
                    "    <h2>Merhaba " + userName + ",</h2>\n" +
                    "    <p>Şifrenizi sıfırlamak için aşağıdaki butona tıklayın:</p>\n" +
                    "    <p><a href=\"" + resetLink + "\" class=\"button\">Şifreyi Sıfırla</a></p>\n" +
                    "    <p><strong>Not:</strong> Bu link 1 saat için geçerlidir.</p>\n" +
                    "    <hr>\n" +
                    "    <p>Eğer buton çalışmazsa, aşağıdaki token’ı kopyalayıp uygulamamızın “Token ile şifre sıfırla” alanına yapıştırabilirsiniz:</p>\n" +
                    "    <p class=\"token\">" + token + "</p>\n" +
                    "    <div class=\"footer\">\n" +
                    "      <p>Eğer bu isteği siz yapmadıysanız, bu e-postayı görmezden gelin.</p>\n" +
                    "      <p>– UnoGame Ekibi</p>\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "</body>\n" +
                    "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            logger.info("Password reset email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to: {}", to, e);
            throw new IllegalStateException("Mail gönderilemedi", e);
        }
    }

}
