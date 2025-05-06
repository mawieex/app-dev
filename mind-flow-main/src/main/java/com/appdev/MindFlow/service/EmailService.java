package com.appdev.MindFlow.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Inject JavaMailSender via constructor
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = "http://localhost:8080/user/reset-password?token=" + token;
        String subject = "Reset Your MindFlow Password";
        String body = "You requested to reset your password for your MindFlow account.\n\n" +
                     "Click the link below to reset your password:\n\n" +
                     resetUrl + "\n\n" +
                     "This link will expire in 24 hours.\n\n" +
                     "If you didn't request this password reset, please ignore this email.";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            System.out.println("Password reset email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Error sending password reset email: " + e.getMessage());
        }
    }
    
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = "http://localhost:8080/verify-email?token=" + token;
        String subject = "Verify your MindFlow Account";
        String body = "Welcome to MindFlow!\n\n" +
                     "Please verify your email address by clicking the link below:\n\n" +
                     verificationUrl + "\n\n" +
                     "This link will expire in 24 hours.\n\n" +
                     "If you didn't create an account with MindFlow, please ignore this email.";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            System.out.println("Verification email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Error sending verification email: " + e.getMessage());
        }
    }
}