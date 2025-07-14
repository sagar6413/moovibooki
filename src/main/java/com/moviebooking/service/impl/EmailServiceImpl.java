package com.moviebooking.service.impl;

import com.moviebooking.model.entity.User;
import com.moviebooking.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    // Common constants
    private static final String EMAIL_VERIFICATION_TEMPLATE = "email-verification";
    private static final String PASSWORD_RESET_TEMPLATE = "password-reset";
    private static final String EMAIL_VERIFICATION_SUBJECT = "Email Verification";
    private static final String PASSWORD_RESET_SUBJECT = "Password Reset Request";
    private static final long MINUTES_IN_MS = 60000L;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${app.email.verification.baseUrl}")
    private String verificationBaseUrl;
    @Value("${app.email.verification.expirationMs}")
    private long verificationExpirationMs;
    @Value("${app.email.passwordReset.baseUrl}")
    private String passwordResetBaseUrl;
    @Value("${app.email.passwordReset.expirationMs}")
    private long passwordResetExpirationMs;

    @Async
    @Override
    public CompletableFuture<Void> sendVerificationEmail(User user, String token) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> templateModel = createVerificationTemplateModel(user, token);
                String htmlContent = processTemplate(EMAIL_VERIFICATION_TEMPLATE, templateModel);
                sendEmail(user.getEmail(), EMAIL_VERIFICATION_SUBJECT, htmlContent);
                log.info("Verification email sent successfully to {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send verification email to {}", user.getEmail(), e);
                throw new RuntimeException("Failed to send verification email", e);
            }
        });
    }

    @Async
    @Override
    public CompletableFuture<Void> sendPasswordResetEmail(User user, String token) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> templateModel = createPasswordResetTemplateModel(user, token);
                String htmlContent = processTemplate(PASSWORD_RESET_TEMPLATE, templateModel);
                sendEmail(user.getEmail(), PASSWORD_RESET_SUBJECT, htmlContent);
                log.info("Password reset email sent successfully to {}", user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send password reset email to {}", user.getEmail(), e);
                throw new RuntimeException("Failed to send password reset email", e);
            }
        });
    }

    private Map<String, Object> createVerificationTemplateModel(User user, String token) {
        return Map.of(
                "firstName", user.getUsername(),
                "verificationUrl", verificationBaseUrl + "?token=" + token,
                "expirationTime", verificationExpirationMs / MINUTES_IN_MS + " minutes");
    }

    private Map<String, Object> createPasswordResetTemplateModel(User user, String token) {
        return Map.of(
                "firstName", user.getUsername(),
                "resetUrl", passwordResetBaseUrl + "?token=" + token,
                "expirationTime", passwordResetExpirationMs / MINUTES_IN_MS + " minutes");
    }

    private String processTemplate(String templateName, Map<String, Object> templateModel) {
        Context context = new Context();
        context.setVariables(templateModel);
        return templateEngine.process(templateName, context);
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}