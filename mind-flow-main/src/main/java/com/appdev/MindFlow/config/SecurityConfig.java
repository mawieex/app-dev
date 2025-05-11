package com.appdev.MindFlow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/user/login")
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/*.css", "/uploads/**").permitAll() // Static resources
                .requestMatchers("/", "/home", "/user/new", "/user/save", "/user/login", "/forgot-password", "/user/forgot-password", "/user/reset-password", "/verify-email", "/error").permitAll() // Public pages
                .requestMatchers("/user/verify", "/user/resend-verification").permitAll() // Email verification links
                .requestMatchers("/insights", "/journal", "/community", "/profile").authenticated() // Protected pages that require authentication
                .anyRequest().authenticated() // All other requests need authentication
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/user/login") // Custom login page URL
                .loginProcessingUrl("/user/login") // URL to submit the username and password to
                .usernameParameter("email") // Matches the 'name' attribute in your login form for email
                .passwordParameter("password") // Matches the 'name' attribute for password
                .defaultSuccessUrl("/journal", true) // Redirect to /journal on successful login
                .failureUrl("/user/login?error=true") // Redirect to /user/login?error=true on login failure
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/perform_logout") // URL to trigger logout
                .logoutSuccessUrl("/user/login?logout=true") // Redirect to /user/login?logout=true after logout
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
} 