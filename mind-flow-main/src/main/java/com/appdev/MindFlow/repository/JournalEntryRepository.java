package com.appdev.MindFlow.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.appdev.MindFlow.model.JournalEntry;
import com.appdev.MindFlow.model.User;

import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findByUserOrderByTimestampDesc(User user, Pageable pageable);
    List<JournalEntry> findByUserAndTimestampBetweenOrderByTimestampDesc(User user, java.time.LocalDateTime start, java.time.LocalDateTime end);
    
    @Query("SELECT COUNT(e) FROM JournalEntry e WHERE e.user = ?1")
    int countByUser(User user);
    
    @Query("SELECT COUNT(DISTINCT DATE(e.timestamp)) FROM JournalEntry e WHERE e.user = ?1")
    int countUniqueDaysByUser(User user);

    List<JournalEntry> findByMoodIgnoreCase(String mood);
    
    @Query("SELECT e FROM JournalEntry e JOIN e.tags t WHERE t = ?1")
    List<JournalEntry> findByTag(String tag);

    @Query("SELECT e FROM JournalEntry e WHERE LOWER(e.mood) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<JournalEntry> searchByMood(String searchTerm);
} 