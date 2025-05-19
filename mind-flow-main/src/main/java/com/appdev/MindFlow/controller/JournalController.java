package com.appdev.MindFlow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.appdev.MindFlow.model.JournalEntry;
import com.appdev.MindFlow.model.User;
import com.appdev.MindFlow.repository.JournalEntryRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/journal")
public class JournalController {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @GetMapping
    public String showJournalPage(Model model, @AuthenticationPrincipal User currentUser,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String searchMood) {
        if (currentUser == null) {
            return "redirect:/user/login";
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        List<JournalEntry> entries;
        
        if (searchMood != null && !searchMood.trim().isEmpty()) {
            entries = journalEntryRepository.searchByMood(searchMood);
        } else {
            entries = journalEntryRepository.findByUserOrderByTimestampDesc(currentUser, pageable);
        }
        
        model.addAttribute("entries", entries);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("searchMood", searchMood);
        return "journal";
    }

    @PostMapping("/entry")
    public String createEntry(@RequestParam String content,
                            @RequestParam String mood,
                            @RequestParam(required = false) String tags,
                            @AuthenticationPrincipal User currentUser,
                            RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to create a journal entry.");
            return "redirect:/user/login";
        }

        JournalEntry entry = new JournalEntry();
        entry.setContent(content);
        entry.setMood(mood);
        entry.setUser(currentUser);
        entry.setTimestamp(LocalDateTime.now());
        
        if (tags != null && !tags.trim().isEmpty()) {
            entry.setTags(List.of(tags.split(",")));
        }

        journalEntryRepository.save(entry);
        redirectAttributes.addFlashAttribute("success", "Journal entry created successfully!");
        return "redirect:/journal";
    }

    @PostMapping("/entry/{id}/delete")
    public String deleteEntry(@PathVariable Long id,
                            @AuthenticationPrincipal User currentUser,
                            RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to delete entries.");
            return "redirect:/user/login";
        }

        JournalEntry entry = journalEntryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Entry not found"));

        if (!entry.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "You can only delete your own entries.");
            return "redirect:/journal";
        }

        journalEntryRepository.delete(entry);
        redirectAttributes.addFlashAttribute("success", "Entry deleted successfully!");
        return "redirect:/journal";
    }
} 