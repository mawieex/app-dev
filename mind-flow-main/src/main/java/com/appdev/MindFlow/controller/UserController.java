package com.appdev.MindFlow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import com.appdev.MindFlow.model.User;
import com.appdev.MindFlow.model.VerificationToken;
import com.appdev.MindFlow.repository.VerificationTokenRepository;
import com.appdev.MindFlow.service.UserService;
import com.appdev.MindFlow.model.Post;
import com.appdev.MindFlow.repository.PostRepository;
import com.appdev.MindFlow.model.Comment;
import com.appdev.MindFlow.repository.CommentRepository;

import java.security.Principal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Optional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Controller
public class UserController {
    
    @Autowired
    private UserService userService;  
    
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @GetMapping("/user/new")
    public String showUserPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/user/save")
    public String saveUserForm(User user, RedirectAttributes redi) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            redi.addFlashAttribute("error", "Username is required!");
            return "redirect:/user/new";
        }
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
        System.out.println("\n=== Login Debug ===");
        System.out.println("Attempting login for email: " + email);
        
        User authenticatedUser = userService.authenticateUser(email, password);
        if (authenticatedUser != null) {
            System.out.println("Login successful for user: " + authenticatedUser.getActualUsername());
            System.out.println("User email: " + authenticatedUser.getEmail());
            System.out.println("Profile picture path: " + authenticatedUser.getProfilePicturePath());
            return "redirect:/journal";
        }
        System.out.println("Login failed for email: " + email);
        redi.addFlashAttribute("error", "Invalid email or password.");
        return "redirect:/user/login";
    }

    @GetMapping("/journal")
    public String showJournalPage(Model model, Principal principal, @AuthenticationPrincipal User currentUser) {
        if (currentUser != null) {
            model.addAttribute("greetingUsername", currentUser.getActualUsername());
        } else if (principal != null && principal instanceof User) {
            model.addAttribute("greetingUsername", ((User)principal).getActualUsername());
        }
        return "journal";
    }
  
     @GetMapping("/insights")
     public String showInsights() {
          return "insights";
    }
    
    @GetMapping("/community")
    public String showCommunity(Model model) {
        model.addAttribute("posts", postRepository.findAllByOrderByTimestampDesc());
        return "community";
    }
    
    @PostMapping("/community/post")
    public String createPost(@RequestParam String postContent, 
                           @AuthenticationPrincipal User currentUser,
                           RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to create a post.");
            return "redirect:/user/login";
        }
        
        Post post = new Post(postContent, currentUser);
        postRepository.save(post);
        redirectAttributes.addFlashAttribute("message", "Post created successfully!");
        return "redirect:/community";
    }
    
    @GetMapping("/profile")
    public String showProfile(Model model, @AuthenticationPrincipal User currentUser) {
        System.out.println("\n=== Profile Page Debug ===");
        System.out.println("Current User: " + (currentUser != null ? "exists" : "null"));
        if (currentUser != null) {
            System.out.println("Username: " + currentUser.getActualUsername());
            System.out.println("Profile Picture Path: " + currentUser.getProfilePicturePath());
        }
        model.addAttribute("currentUser", currentUser);
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
        } else {
            redi.addFlashAttribute("error", result);
        }
        return "redirect:/user/login";
    }
    
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        String result = userService.verifyEmail(token);
        if (result.contains("successfully")) {
            model.addAttribute("message", "Email verified successfully! You can now log in.");
        } else {
            model.addAttribute("error", result);
        }
        return "login";
    }
    
    @GetMapping("/user/edit-profile-picture")
    public String showEditProfilePicturePage(Model model, @AuthenticationPrincipal User currentUser) {
        model.addAttribute("currentUser", currentUser); // Pass current user to display existing pic path if any
        return "edit-profile-picture";
    }

    @PostMapping("/user/upload-profile-picture")
    public String uploadProfilePicture(@RequestParam("file") MultipartFile file, 
                                     @AuthenticationPrincipal User currentUser, 
                                     RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to upload a profile picture.");
            return "redirect:/user/login";
        }
        
        System.out.println("\n=== Upload Profile Picture Debug ===");
        System.out.println("Current User: " + (currentUser != null ? "exists" : "null"));
        if (currentUser != null) {
            System.out.println("Username: " + currentUser.getActualUsername());
            System.out.println("Email: " + currentUser.getEmail());
        }
        System.out.println("Original filename: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize() + " bytes");
        
        if (file.isEmpty()) {
            System.out.println("ERROR: No file selected");
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
            return "redirect:/user/edit-profile-picture";
        }

        try {
            String uploadDir = "uploads/user-profile-pictures/" + currentUser.getActualUsername();
            Path uploadDirPath = Paths.get(uploadDir);
            System.out.println("Creating directory at: " + uploadDirPath.toAbsolutePath());
            
            if (!Files.exists(uploadDirPath)) {
                Files.createDirectories(uploadDirPath);
                System.out.println("Directory created successfully");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                System.out.println("ERROR: Original filename is null");
                redirectAttributes.addFlashAttribute("error", "Invalid file name.");
                return "redirect:/user/edit-profile-picture";
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = System.currentTimeMillis() + fileExtension;
            Path filePath = uploadDirPath.resolve(filename);
            System.out.println("Saving file to: " + filePath.toAbsolutePath());

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File saved successfully");
            System.out.println("File exists after save: " + Files.exists(filePath));
            System.out.println("File size after save: " + Files.size(filePath) + " bytes");

            String relativePath = "/uploads/user-profile-pictures/" + currentUser.getActualUsername() + "/" + filename;
            System.out.println("Updating user profile with path: " + relativePath);
            userService.updateProfilePicturePath(currentUser.getActualUsername(), relativePath);
            
            System.out.println("Profile picture path updated in database");
            redirectAttributes.addFlashAttribute("message", "Profile picture uploaded successfully!");
        } catch (Exception e) {
            System.out.println("ERROR: Exception during upload: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Could not upload profile picture: " + e.getMessage());
        }

        return "redirect:/profile";
    }
    
    @GetMapping("/uploads/**")
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) {
        System.out.println("\n=== Serve File Debug ===");
        try {
            String filename = request.getRequestURI().split("/uploads/")[1];
            Path file = Paths.get("uploads", filename);
            System.out.println("Requested URI: " + request.getRequestURI());
            System.out.println("Looking for file at: " + file.toAbsolutePath());
            System.out.println("File exists? " + Files.exists(file));
            System.out.println("File is readable? " + Files.isReadable(file));
            
            Resource resource = new FileSystemResource(file.toFile());
            if (resource.exists() && resource.isReadable()) {
                String contentType = "image/jpeg"; // default
                String filenameLower = filename.toLowerCase();
                if (filenameLower.endsWith(".png")) {
                    contentType = "image/png";
                } else if (filenameLower.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (filenameLower.endsWith(".jpg") || filenameLower.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                }
                
                System.out.println("Serving file with content type: " + contentType);
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Cache-Control", "no-cache")
                    .body(resource);
            }
            System.out.println("File not found or not readable");
        } catch (Exception e) {
            System.out.println("Error serving file: " + e.getMessage());
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/community/like")
    @ResponseBody
    public ResponseEntity<?> toggleLike(@RequestParam Long postId, 
                                      @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body("You must be logged in to like posts");
        }
        
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
            
        if (post.isLikedBy(currentUser)) {
            post.removeLike(currentUser);
        } else {
            post.addLike(currentUser);
        }
        
        postRepository.save(post);
        return ResponseEntity.ok(post.getLikes().size());
    }
    
    @PostMapping("/community/comment")
    public String addComment(@RequestParam Long postId,
                           @RequestParam String commentContent,
                           @AuthenticationPrincipal User currentUser,
                           RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to comment");
            return "redirect:/user/login";
        }
        
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));
            
        Comment comment = new Comment(commentContent, currentUser, post);
        commentRepository.save(comment);
        
        redirectAttributes.addFlashAttribute("message", "Comment added successfully!");
        return "redirect:/community";
    }
    
}
