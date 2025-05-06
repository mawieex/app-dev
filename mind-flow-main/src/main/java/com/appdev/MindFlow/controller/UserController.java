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
        
        userService.registerUser(user); 
        redi.addFlashAttribute("message", "Registration successful! Please check your email to verify your account.");
        return "redirect:/user/login";
    }

    @GetMapping("/user/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/user/authenticate") 
    public String loginUser(@RequestParam("email") String email, 
                            @RequestParam("password") String password, 
                            RedirectAttributes redi) {
        if (userService.authenticateUser(email, password)) {
            return "redirect:/journal";
        }
        
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent() && !userOpt.get().isEmailVerified()) {
            redi.addFlashAttribute("error", "Please verify your email before logging in. Check your inbox for the verification link.");
        } else {
            redi.addFlashAttribute("error", "Invalid email or password.");
        }
        return "redirect:/user/login";
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
