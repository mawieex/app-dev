package com.appdev.MindFlow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.appdev.MindFlow.model.User;
import com.appdev.MindFlow.model.VerificationToken;
import com.appdev.MindFlow.repository.UserRepository;
import com.appdev.MindFlow.repository.VerificationTokenRepository;

import java.util.Optional;
import java.util.UUID;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationRepository;

    @Autowired
    private EmailService emailService;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public String generatePasswordResetToken(User user) {
        if (user != null) {
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken(token, user);
            verificationRepository.save(verificationToken);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            return "Password reset link sent to your email.";
        }
        return "User  not found.";
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    public String resetPassword(String token, String newPassword) {
        Optional<VerificationToken> verificationTokenOpt = verificationRepository.findByToken(token);
        if (verificationTokenOpt.isPresent()) {
            VerificationToken verificationToken = verificationTokenOpt.get();
            User user = verificationToken.getUser();

            // Check if the token is expired
            if (verificationToken.isExpired()) {
                return "Password reset token is expired. Please request a new one.";
            }

            // Update user's password
            user.setPassword(newPassword);
            userRepository.save(user);

            // Delete the used token
            verificationRepository.delete(verificationToken);

            return "Password has been reset successfully!";
        }
        return "Invalid password reset token.";
    }

    // New method to register a user and send a verification email
    @Transactional
    public void registerUser (User user) {
        user.setEmailVerified(false); // Set email as unverified by default
        userRepository.save(user); // Save user details
        String token = UUID.randomUUID().toString(); // Generate a unique token
        VerificationToken verificationToken = new VerificationToken(token, user); // Create a new VerificationToken
        verificationRepository.save(verificationToken); // Save the token to the database
        emailService.sendVerificationEmail(user.getEmail(), token); // Send verification email with the token
    }

    // New method to verify the email using the token
    public String verifyEmail(String token) {
        Optional<VerificationToken> verificationTokenOpt = verificationRepository.findByToken(token);
        if (verificationTokenOpt.isPresent()) {
            VerificationToken verificationToken = verificationTokenOpt.get();
            User user = verificationToken.getUser ();

            // Check if the token is expired
            if (verificationToken.isExpired()) {
                return "Verification token is expired.";
            }

            // Update user's email verification status
            user.setEmailVerified(true);
            userRepository.save(user); // Save the updated user

            // Optionally, delete the token after successful verification
            verificationRepository.delete(verificationToken);

            return "Email verified successfully!";
        }
        return "Invalid verification token.";
    }

    public boolean authenticateUser(String email, String password) {
        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isEmailVerified()) {
                return false;
            }
            return user.getPassword().equals(password);
        }
        return false;
    }
}