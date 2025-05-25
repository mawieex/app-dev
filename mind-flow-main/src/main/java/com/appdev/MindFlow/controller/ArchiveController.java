package com.appdev.MindFlow.controller;

import com.appdev.MindFlow.model.JournalEntry;
import com.appdev.MindFlow.model.User;
import com.appdev.MindFlow.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@Controller
public class ArchiveController {
    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @GetMapping("/archive")
    public String archivePage(Model model, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return "redirect:/user/login";
        }
        Pageable pageable = PageRequest.of(0, 50, Sort.by("timestamp").descending());
        List<JournalEntry> entries = journalEntryRepository.findByUserOrderByTimestampDesc(currentUser, pageable);
        model.addAttribute("entries", entries);
        model.addAttribute("currentUser", currentUser);
        return "archive";
    }
} 