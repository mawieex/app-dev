package com.appdev.MindFlow.service;

import com.appdev.MindFlow.model.JournalEntry;
import com.appdev.MindFlow.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    @Autowired
    public JournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    public List<JournalEntry> findJournalsByMood(String mood) {
        return journalEntryRepository.findByMoodIgnoreCase(mood);
    }

    public List<JournalEntry> findJournalsByTag(String tag) {
        return journalEntryRepository.findByTag(tag);
    }

    public List<JournalEntry> searchJournalsByMood(String searchTerm) {
        return journalEntryRepository.searchByMood(searchTerm);
    }

    // You can add other service methods here, e.g., for creating, updating, deleting entries.
    public JournalEntry saveJournalEntry(JournalEntry journalEntry) {
        return journalEntryRepository.save(journalEntry);
    }
} 