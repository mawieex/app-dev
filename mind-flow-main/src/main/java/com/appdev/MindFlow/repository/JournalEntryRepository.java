package com.appdev.MindFlow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.appdev.MindFlow.model.JournalEntry;
import com.appdev.MindFlow.model.User;

import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findByUserOrderByTimestampDesc(User user);
    List<JournalEntry> findByUserAndTimestampBetweenOrderByTimestampDesc(User user, java.time.LocalDateTime start, java.time.LocalDateTime end);
} 