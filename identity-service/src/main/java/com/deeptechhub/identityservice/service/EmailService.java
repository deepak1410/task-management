package com.deeptechhub.identityservice.service;

import com.deeptechhub.identityservice.domain.EmailToken;
import com.deeptechhub.identityservice.domain.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public void sendPasswordResetEmail(User user, EmailToken token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token.getToken();
        String subject = "Reset your password";
        String body = "Hi " + user.getName() + ",\n\nPlease reset your password by clicking the link below:\n"
                + resetUrl + "\n\nThis link will expire in 1 hour.";

        log.debug("Sending email for password reset");
        sendEmail(subject, body, user.getEmail());
    }

    public void sendVerificationEmail(User user, EmailToken token) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token.getToken();
        String subject = "Verify your email";
        String body = "Hi " + user.getName() + ",\n\nPlease verify your email by clicking the link below:\n"
                + verificationUrl + "\n\nThis link will expire in 24 hours.";

        log.debug("Sending email for password reset");
        sendEmail(subject, body, user.getEmail());
    }

    public void sendEmail(String subject, String body, String... to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

}
