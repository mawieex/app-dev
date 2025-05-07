package com.appdev.MindFlow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.appdev.MindFlow.model.User;
import com.appdev.MindFlow.model.VerificationToken;
import com.appdev.MindFlow.repository.VerificationTokenRepository;
import com.appdev.MindFlow.service.UserService;

import java.util.Optional;

@Controller
public class UserController {
    
    @Autowired
    private UserService userService;  
    
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    
    @GetMapping("/user/new")
    public String showUserPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/user/save")
    public String saveUserForm(User user, RedirectAttributes redi) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            redi.addFlashAttribute("error", "Email is required!");
            return "redirect:/user/new"; 
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            redi.addFlashAttribute("error", "Password is required!");
            return "redirect:/user/new"; 
        }
        
        userService.registerUser(user, user.getPassword()); 
        redi.addFlashAttribute("message", "Registration successful! Please check your email to verify your account.");
        return "redirect:/user/login";
    }

    @GetMapping("/user/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/user/login")
    public String loginUser(@RequestParam String email, @RequestParam String password, RedirectAttributes redi, Model model) {
        // We'll need to implement or call an authentication method in UserService
        // For now, let's assume userService.authenticateUser(email, password) returns a User object or null
        User authenticatedUser = userService.authenticateUser(email, password); // This method needs to be implemented in UserService

        if (authenticatedUser != null) {
            // Successful login
            // You might want to add the user to the session here
            // model.addAttribute("currentUser", authenticatedUser); // Example
            return "redirect:/journal"; // Redirect to a dashboard or home page
        } else {
            // Failed login
            redi.addFlashAttribute("error", "Invalid email or password.");
            return "redirect:/user/login";
        }
    }

    @GetMapping("/journal")
    public String showJournalPage() {
        return "journal";
    }
  
     @GetMapping("/insights")
     public String showInsights() {
          return "insights";
    }
    
     @GetMapping("/community")
     public String showCommunity() {
          return "community";
    }
    
     @GetMapping("/profile")
     public String showProfile() {
          return "profile";
    }
    

    @GetMapping("/reset")
    public String welcome() {
        return "welcome";
    }
    
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password"; 
    }
    
    @PostMapping("/user/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();  
            Optional<VerificationToken> tokenOpt = verificationTokenRepository.findByUser(user);
            tokenOpt.ifPresent(verificationTokenRepository::delete);
            
            String message = userService.generatePasswordResetToken(user);
            model.addAttribute("message", message);
        } else {
            model.addAttribute("error", "No account found with that email.");
        }
        return "forgot-password";
    }

    @GetMapping("/user/reset-password")
    public String showResetPage(@RequestParam("token") String token, Model model, RedirectAttributes redi) {
        Optional<VerificationToken> optionalToken = verificationTokenRepository.findByToken(token);
        if (!optionalToken.isPresent() || optionalToken.get().isExpired()) {
            redi.addFlashAttribute("error", "Invalid or expired token.");
            return "redirect:/user/login";
        }
        model.addAttribute("token", token);
        return "reset-password"; 
    }
    
    @PostMapping("/user/reset-password")
    public String resetPassword(@RequestParam("token") String token, 
                                @RequestParam("newPassword") String newPassword, 
                                RedirectAttributes redi) {
        String result = userService.resetPassword(token, newPassword);
        if (result.contains("successfully")) {
            redi.addFlashAttribute("message", "Password reset successful! Please log in.");
            return "redirect:/user/login";
        } else {
            redi.addFlashAttribute("error", result);
            return "redirect:/user/login";
        }
    }
    
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        String result = userService.verifyEmail(token);
        if (result.contains("successfully")) {
            model.addAttribute("message", "Email verified successfully! You can now log in.");
            return "login";
        } else {
            model.addAttribute("error", result);
            return "login";
        }
    }
    
}
