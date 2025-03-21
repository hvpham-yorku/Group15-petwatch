package com.petwatch.petwatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.context.annotation.Primary;
import com.petwatch.petwatch.Controller.ApiController;
import com.petwatch.petwatch.Model.User.Role;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    

    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/", "/login-choice", "/login-owner", "/login-sitter", 
                                 "/signup-choice", "/signup-owner", "/signup-sitter",
                                 "/api/signup",
                                 "/js/**", "/css/**", "/images/**").permitAll()
                .requestMatchers("/dashboard-owner").hasRole("USER")
                .requestMatchers("/dashboard-sitter").hasRole("EMPLOYEE")
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/login-choice")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutUrl("/perform-logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll());
            
        // Disable CSRF for API calls
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
        
        return http.build();
    }
    
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                
                Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
                System.out.println("Login successful for: " + authentication.getName() + " with roles: " + roles);
                System.out.println("Authentication details: " + authentication.getDetails());
                System.out.println("Authentication principal: " + authentication.getPrincipal());
                
                // Check if this is coming from pet sitter login page
                String referer = request.getHeader("Referer");
                boolean comingFromSitterPage = (referer != null && referer.contains("login-sitter"));
                System.out.println("Coming from sitter page: " + comingFromSitterPage);
                
                // Get the user from the database again to double-check their role
                String username = authentication.getName();
                com.petwatch.petwatch.Model.User user = ApiController.getUserByEmail(username);
                System.out.println("User from database: " + (user != null ? user.getEmail() : "null") + 
                                  " with role: " + (user != null ? user.getRole() : "null"));
                
                // If coming from sitter page and we can find the user, force redirect to sitter dashboard
                if (comingFromSitterPage && user != null) {
                    System.out.println("Pet sitter login detected, redirecting to sitter dashboard");
                    response.sendRedirect("/dashboard-sitter");
                    return;
                }
                
                // Normal redirect logic based on role
                if (roles.contains("ROLE_USER")) {
                    System.out.println("Redirecting to owner dashboard based on ROLE_USER");
                    response.sendRedirect("/dashboard-owner");
                } else if (roles.contains("ROLE_EMPLOYEE")) {
                    System.out.println("Redirecting to sitter dashboard based on ROLE_EMPLOYEE");
                    response.sendRedirect("/dashboard-sitter");
                } else {
                    System.out.println("No recognized role, redirecting to login choice");
                    response.sendRedirect("/login-choice");
                }
            }
        };
    }
    
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            System.out.println("Login failed: " + exception.getMessage());
            System.out.println("Email used: " + request.getParameter("email"));
            System.out.println("Password used: [REDACTED]");
            
            String redirectUrl = "/login-owner?error=true";
            
            // Get the intended user type from the login URL
            String referer = request.getHeader("Referer");
            if (referer != null) {
                if (referer.contains("login-sitter")) {
                    redirectUrl = "/login-sitter?error=true";
                }
            }
            
            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        System.out.println("Creating database-backed UserDetailsService");
        System.out.println("- For testing: owner@example.com / password (ROLE_USER)");
        System.out.println("- For testing: sitter@example.com / password (ROLE_EMPLOYEE)");
        
        return username -> {
            System.out.println("Looking up user: " + username);
            
            // First, check the preloaded users for development/testing
            if ("owner@example.com".equals(username)) {
                System.out.println("Using hardcoded owner test account");
                return User.builder()
                    .username("owner@example.com")
                    .password("password")  // Using plain text with NoOpPasswordEncoder
                    .roles("USER")
                    .build();
            } else if ("sitter@example.com".equals(username)) {
                System.out.println("Using hardcoded sitter test account");
                return User.builder()
                    .username("sitter@example.com")
                    .password("password")  // Using plain text with NoOpPasswordEncoder
                    .roles("EMPLOYEE")
                    .build();
            }
            
            // Look up the user from the database through ApiController's helper method
            com.petwatch.petwatch.Model.User user = ApiController.getUserByEmail(username);
            
            if (user != null) {
                System.out.println("Found user in database: " + username + " with role: " + user.getRole());
                // Convert PetWatch User to Spring Security UserDetails
                Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                
                // More detailed role checking
                com.petwatch.petwatch.Model.User.Role userRole = user.getRole();
                System.out.println("User role from database (detailed): " + userRole);
                
                if (userRole == com.petwatch.petwatch.Model.User.Role.EMPLOYEE) {
                    System.out.println("Assigning EMPLOYEE role");
                    authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
                } else {
                    System.out.println("Assigning USER role");
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }
                
                return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),  // Using plain text with NoOpPasswordEncoder
                    authorities
                );
            }
            
            System.out.println("User not found: " + username);
            throw new UsernameNotFoundException("User not found: " + username);
        };
    }
} 