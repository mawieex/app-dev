package com.appdev.MindFlow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email; // Import for @Email
import jakarta.validation.constraints.NotBlank; // Import for @NotBlank

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(nullable = false, unique = true, length = 45)
    @Email // Validate that the email is in the correct format
    private String email;

    @Column(nullable = false, length = 50)
    @NotBlank // Ensure that the password is not blank
    private String password;

    private boolean emailVerified = false;

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean isEmailVerified() {
        return emailVerified;
    }
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    @Override
    public String toString() {
        return "User  {" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", emailVerified=" + emailVerified +
                '}';
    }
}