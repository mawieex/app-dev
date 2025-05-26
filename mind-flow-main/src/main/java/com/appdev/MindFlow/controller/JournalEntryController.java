package com.appdev.MindFlow.controller;

import com.appdev.MindFlow.model.JournalEntry;
import com.appdev.MindFlow.service.JournalEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.appdev.MindFlow.model.User;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics/journals")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    @Autowired
    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    @GetMapping("/mood/{mood}")
    public ResponseEntity<List<JournalEntry>> getJournalsByMood(@PathVariable String mood) {
        List<JournalEntry> entries = journalEntryService.findJournalsByMood(mood);
        if (entries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<JournalEntry>> getJournalsByTag(@PathVariable String tag) {
        List<JournalEntry> entries = journalEntryService.findJournalsByTag(tag);
        if (entries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/search/mood")
    public ResponseEntity<List<JournalEntry>> searchJournalsByMood(@RequestParam String query) {
        List<JournalEntry> entries = journalEntryService.searchJournalsByMood(query);
        if (entries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(entries);
    }

    @GetMapping
    public ResponseEntity<List<JournalEntry>> getAllJournalsForCurrentUser(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        List<JournalEntry> entries = journalEntryService.findByUser(currentUser);
        return ResponseEntity.ok(entries);
    }

    // Example endpoint to create a journal entry (for testing purposes)
    @PostMapping
    public ResponseEntity<JournalEntry> createJournalEntry(@RequestBody JournalEntry journalEntry) {
        JournalEntry savedEntry = journalEntryService.saveJournalEntry(journalEntry);
        return ResponseEntity.ok(savedEntry);
    }
} 