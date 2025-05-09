package com.appdev.MindFlow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email; // Import for @Email
import jakarta.validation.constraints.NotBlank; // Import for @NotBlank
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User implements UserDetails { // Implement UserDetails
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(nullable = false, unique = true, length = 45)
    @Email // Validate that the email is in the correct format
    private String email;

    @Column(nullable = false, unique = true, length = 45) // Added username field
    @NotBlank // Ensure username is not blank
    private String username;

    @Column(nullable = false, length = 64) // Increased length for hashed password
    @NotBlank // Ensure that the password is not blank
    private String password;

    private boolean emailVerified = false;

    // For roles - simple approach using a Set of Strings
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

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

    public String getActualUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // If roles are stored like "ROLE_USER", "ROLE_ADMIN"
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        // For a single default role:
        // return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Or implement logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Or implement logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Or implement logic
    }

    @Override
    public boolean isEnabled() {
        return emailVerified; // Or return true if email verification is not strictly enforced for login
    }
    // End UserDetails methods

    public void setPassword(String password) {
        this.password = password;
    }
    public boolean isEmailVerified() {
        return emailVerified;
    }
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", emailVerified=" + emailVerified +
                ", roles=" + roles +
                '}';
    }
}