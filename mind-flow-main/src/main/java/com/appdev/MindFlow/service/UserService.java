package com.appdev.MindFlow.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.appdev.MindFlow.model.User;
import com.appdev.MindFlow.model.VerificationToken;
import com.appdev.MindFlow.repository.UserRepository;
import com.appdev.MindFlow.repository.VerificationTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, 
                       VerificationTokenRepository verificationRepository, 
                       EmailService emailService, 
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return user;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void updateProfilePicturePath(String username, String filePath) {
        User user = findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        user.setProfilePicturePath(filePath);
        userRepository.save(user);
    }

    public String generatePasswordResetToken(User user) {
        if (user != null) {
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken(token, user);
            verificationRepository.save(verificationToken);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            return "Password reset link sent to your email.";
        }
        return "User not found.";
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

            if (verificationToken.isExpired()) {
                verificationRepository.delete(verificationToken);
                return "Password reset token is expired. Please request a new one.";
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            verificationRepository.delete(verificationToken);

            return "Password has been reset successfully!";
        }
        return "Invalid password reset token.";
    }

    @Transactional
    public void registerUser(User user, String rawPassword) {
        user.setUsername(user.getDisplayUsername());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmailVerified(false);
        user.setActive(true);
        user.getRoles().clear();
        user.addRole("ROLE_USER");
        userRepository.save(user);
        
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationRepository.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    public String verifyEmail(String token) {
        Optional<VerificationToken> verificationTokenOpt = verificationRepository.findByToken(token);
        if (verificationTokenOpt.isPresent()) {
            VerificationToken verificationToken = verificationTokenOpt.get();
            User user = verificationToken.getUser();

            if (verificationToken.isExpired()) {
                verificationRepository.delete(verificationToken);
                return "Verification token is expired.";
            }
            
            user.setEmailVerified(true);
            userRepository.save(user);
            verificationRepository.delete(verificationToken);

            return "Email verified successfully!";
        }
        return "Invalid verification token.";
    }

    public User authenticateUser(String email, String password) {
        System.out.println("\n=== Authentication Debug ===");
        System.out.println("Attempting to authenticate user with email: " + email);
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("User found in database");
            System.out.println("Email verified: " + user.isEmailVerified());
            System.out.println("Account active: " + user.isActive());
            System.out.println("Account deleted: " + (user.getDeletedAt() != null));
            
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("Password matches: " + passwordMatches);
            
            if (passwordMatches && user.isEnabled()) {
                System.out.println("Authentication successful");
                return user;
            }
            System.out.println("Authentication failed - Password match: " + passwordMatches + 
                             ", Email verified: " + user.isEmailVerified() + 
                             ", Account active: " + user.isActive() + 
                             ", Account deleted: " + (user.getDeletedAt() != null));
        } else {
            System.out.println("No user found with email: " + email);
        }
        return null;
    }

    @Transactional
    public void deactivateAccount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        user.setDeletedAt(LocalDateTime.now());
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void reactivateAccount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        user.setActive(true);
        user.setDeletedAt(null);
        userRepository.save(user);
    }
}