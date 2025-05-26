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
import com.appdev.MindFlow.model.JournalEntry;
import com.appdev.MindFlow.repository.VerificationTokenRepository;
import com.appdev.MindFlow.repository.JournalEntryRepository;
import com.appdev.MindFlow.service.UserService;
import com.appdev.MindFlow.model.Post;
import com.appdev.MindFlow.repository.PostRepository;
import com.appdev.MindFlow.model.Comment;
import com.appdev.MindFlow.repository.CommentRepository;
import com.appdev.MindFlow.repository.UserRepository;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JournalEntryRepository journalEntryRepository;
    
    @GetMapping("/user/new")
    public String showUserPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/user/save")
    public String saveUserForm(User user, RedirectAttributes redi) {
        if (user.getDisplayUsername() == null || user.getDisplayUsername().trim().isEmpty()) {
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
            System.out.println("Login successful for user: " + authenticatedUser.getDisplayUsername());
            System.out.println("User email: " + authenticatedUser.getEmail());
            System.out.println("Profile picture path: " + authenticatedUser.getProfilePicturePath());
            return "redirect:/journal";
        }
        
        // Get user to check specific error conditions
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isEmailVerified()) {
                redi.addAttribute("emailVerified", "false");
            }
            if (!user.isActive()) {
                redi.addAttribute("accountDisabled", "true");
            }
            if (user.getDeletedAt() != null) {
                redi.addAttribute("accountDeleted", "true");
            }
        }
        
        redi.addAttribute("email", email);
        redi.addAttribute("error", "true");
        return "redirect:/user/login";
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
            System.out.println("Username: " + currentUser.getDisplayUsername());
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
            System.out.println("Username: " + currentUser.getDisplayUsername());
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
            String uploadDir = "uploads/user-profile-pictures/" + currentUser.getDisplayUsername();
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

            String relativePath = "/uploads/user-profile-pictures/" + currentUser.getDisplayUsername() + "/" + filename;
            System.out.println("Updating user profile with path: " + relativePath);
            userService.updateProfilePicturePath(currentUser.getDisplayUsername(), relativePath);
            
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
    
    @PostMapping("/user/update-username")
    public String updateUsername(@RequestParam String newUsername, 
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check if username is already taken
            if (userRepository.findByUsername(newUsername).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Username is already taken");
                return "redirect:/profile";
            }
            
            // Update username
            currentUser.setDisplayUsername(newUsername);
            userRepository.save(currentUser);
            
            redirectAttributes.addFlashAttribute("success", "Username updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update username");
        }
        return "redirect:/profile";
    }
    
    @PostMapping("/user/deactivate-account")
    public String deactivateAccount(@AuthenticationPrincipal User currentUser,
                                  RedirectAttributes redirectAttributes) {
        try {
            userService.deactivateAccount(currentUser.getEmail());
            redirectAttributes.addFlashAttribute("message", "Your account has been deactivated. You can reactivate it by logging in again.");
            return "redirect:/perform_logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to deactivate account: " + e.getMessage());
            return "redirect:/profile";
        }
    }

    @PostMapping("/user/delete-account")
    public String deleteAccount(@AuthenticationPrincipal User currentUser,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.deleteAccount(currentUser.getEmail());
            redirectAttributes.addFlashAttribute("message", "Your account has been deleted. We're sorry to see you go!");
            return "redirect:/perform_logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete account: " + e.getMessage());
            return "redirect:/profile";
        }
    }
    
    @GetMapping("/insights")
    public String showInsights(@AuthenticationPrincipal User currentUser, Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "30") int size) {
        System.out.println("\n=== Insights Debug ===");
        try {
            if (currentUser == null) {
                return "redirect:/user/login";
            }

            // Get current month's entries with pagination
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
            
            // Get only the last 30 entries for mood chart
            Pageable pageable = PageRequest.of(0, 30, Sort.by("timestamp").descending());
            List<JournalEntry> recentEntries = journalEntryRepository.findByUserOrderByTimestampDesc(currentUser, pageable);
            
            // Get monthly entries for charts
            List<JournalEntry> monthlyEntries = journalEntryRepository.findByUserAndTimestampBetweenOrderByTimestampDesc(
                currentUser, startOfMonth, endOfMonth);

            // Calculate insights using database queries
            List<JournalEntry> allEntries = journalEntryRepository.findByUserOrderByTimestampDesc(currentUser, org.springframework.data.domain.Pageable.unpaged());
            System.out.println("All entries for user:");
            for (JournalEntry entry : allEntries) {
                System.out.println("Entry ID: " + entry.getId() + ", Timestamp: " + entry.getTimestamp());
            }
            System.out.println("Total entries counted: " + allEntries.size());
            int totalEntries = allEntries.size();
            int currentStreak = calculateCurrentStreak(recentEntries);
            int longestStreak = calculateLongestStreak(recentEntries);
            int completionRate = calculateCompletionRate(recentEntries);
            
            // Prepare data for charts
            Map<String, Long> moodDistribution = monthlyEntries.stream()
                .map(entry -> {
                    // Convert old mood values to new categories
                    String newMood;
                    switch (entry.getMood()) {
                        case "Happy":
                        case "Calm":
                            newMood = "Positive";
                            break;
                        case "Neutral":
                            newMood = "Neutral";
                            break;
                        case "Anxious":
                        case "Sad":
                            newMood = "Negative";
                            break;
                        default:
                            newMood = "Neutral";
                    }
                    return newMood;
                })
                .collect(Collectors.groupingBy(mood -> mood, Collectors.counting()));
            
            Map<String, Long> activityPatterns = monthlyEntries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getTimestamp().getDayOfWeek().toString(), Collectors.counting()));
            
            // Add data to model
            model.addAttribute("totalEntries", totalEntries);
            model.addAttribute("currentStreak", currentStreak);
            model.addAttribute("longestStreak", Math.max(longestStreak, 1));
            model.addAttribute("completionRate", completionRate);
            model.addAttribute("moodDistribution", moodDistribution);
            model.addAttribute("activityPatterns", activityPatterns);

            // Create a simpler data structure for the mood chart
            List<Map<String, Object>> moodChartData = recentEntries.stream()
                .map(entry -> {
                    Map<String, Object> dataPoint = new java.util.HashMap<>();
                    dataPoint.put("timestamp", entry.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
                    dataPoint.put("mood", entry.getMood());
                    return dataPoint;
                })
                .collect(Collectors.toList());
            model.addAttribute("moodChartData", moodChartData);

            model.addAttribute("currentUser", currentUser);
            
            return "insights";
        } catch (Exception e) {
            System.out.println("ERROR in showInsights: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/journal";
        }
    }

    private int calculateCurrentStreak(List<JournalEntry> entries) {
        if (entries.isEmpty()) return 0;
        
        int streak = 0;
        LocalDateTime lastDate = entries.get(0).getTimestamp().toLocalDate().atStartOfDay();
        LocalDateTime currentDate = LocalDateTime.now().toLocalDate().atStartOfDay();
        
        // If last entry was not today, streak is broken
        if (lastDate.toLocalDate().isBefore(currentDate.toLocalDate())) {
            return 0;
        }
        
        for (JournalEntry entry : entries) {
            LocalDateTime entryDate = entry.getTimestamp().toLocalDate().atStartOfDay();
            if (entryDate.equals(lastDate)) {
                continue;
            }
            if (entryDate.equals(lastDate.minusDays(1))) {
                streak++;
                lastDate = entryDate;
            } else {
                break;
            }
        }
        
        return streak + 1; // +1 for today
    }

    private int calculateLongestStreak(List<JournalEntry> entries) {
        if (entries.isEmpty()) return 0;
        
        int longestStreak = 0;
        int currentStreak = 1;
        LocalDateTime lastDate = entries.get(0).getTimestamp().toLocalDate().atStartOfDay();
        
        for (int i = 1; i < entries.size(); i++) {
            LocalDateTime currentDate = entries.get(i).getTimestamp().toLocalDate().atStartOfDay();
            if (currentDate.equals(lastDate)) {
                continue;
            }
            if (currentDate.equals(lastDate.minusDays(1))) {
                currentStreak++;
            } else {
                longestStreak = Math.max(longestStreak, currentStreak);
                currentStreak = 1;
            }
            lastDate = currentDate;
        }
        
        return Math.max(longestStreak, currentStreak);
    }

    private int calculateCompletionRate(List<JournalEntry> entries) {
        if (entries.isEmpty()) return 0;
        
        LocalDateTime firstEntryDate = entries.get(entries.size() - 1).getTimestamp();
        long daysSinceFirstEntry = java.time.Duration.between(firstEntryDate, LocalDateTime.now()).toDays();
        long uniqueDays = entries.stream()
            .map(entry -> entry.getTimestamp().toLocalDate())
            .distinct()
            .count();
        
        return (int) ((uniqueDays * 100) / (daysSinceFirstEntry + 1));
    }
    
    @PostMapping("/user/reactivate")
    public String reactivateAccount(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            userService.reactivateAccount(email);
            redirectAttributes.addFlashAttribute("message", "Your account has been reactivated. You can now log in.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reactivate account: " + e.getMessage());
        }
        return "redirect:/user/login";
    }

    @PostMapping("/user/fix-account")
    public String fixAccount(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setActive(true);
                user.setDeletedAt(null);
                userService.save(user);
                redirectAttributes.addFlashAttribute("message", "Your account has been fixed. Please try logging in again.");
            } else {
                redirectAttributes.addFlashAttribute("error", "No account found with that email.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to fix account: " + e.getMessage());
        }
        return "redirect:/user/login";
    }
}
