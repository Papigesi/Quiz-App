package com.example.quizapp.quizappdemo.config;

import com.example.quizapp.quizappdemo.security.QuizUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Minimal Spring Security yapılandırması:
 * - HTTP oturumu (session cookie) ile REST koruması
 * - BCrypt şifre hash
 * - ADMIN / USER rol kuralları
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final QuizUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public SecurityConfig(QuizUserDetailsService userDetailsService, ObjectMapper objectMapper) {
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider(passwordEncoder()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // Statik frontend
                        .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        // Kayıt / giriş
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // Admin yazma işlemleri
                        .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                        .requestMatchers("/api/questions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/quizzes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/**").hasRole("ADMIN")
                        .requestMatchers("/api/quizzes/*/questions/admin").hasRole("ADMIN")
                        .requestMatchers("/api/quizzes/*/available-questions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/*/questions/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/*/questions/*").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/results").hasRole("ADMIN")
                        // Diğer API uçları: giriş zorunlu
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(this::writeAccessDenied)
                );

        return http.build();
    }

    private void writeAccessDenied(HttpServletRequest request,
                                   HttpServletResponse response,
                                   org.springframework.security.access.AccessDeniedException ex)
            throws java.io.IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 403,
                "error", "Forbidden",
                "message", "Bu işlem için yetkiniz yok"
        ));
    }
}
