package com.appdev.MindFlow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "journal_entries", indexes = {
    @Index(name = "idx_journal_timestamp", columnList = "timestamp"),
    @Index(name = "idx_journal_user", columnList = "user_id")
})
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String mood; // Consider using an Enum for Mood if you want more type safety

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "journal_entry_tags", joinColumns = @JoinColumn(name = "journal_entry_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructors
    public JournalEntry() {
        this.timestamp = LocalDateTime.now(); // Default timestamp to now
    }

    public JournalEntry(String content, String mood, User user, List<String> tags) {
        this.content = content;
        this.mood = mood;
        this.user = user;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "JournalEntry{" +
               "id=" + id +
               ", mood='" + mood + "'" +
               ", timestamp=" + timestamp +
               ", userId=" + (user != null ? user.getId() : "null") +
               ", tags=" + tags +
               '}';
    }
} 