package com.mohammadnuridin.todoapp.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mohammadnuridin.todoapp.core.security.JwtAuthEntryPoint;
import com.mohammadnuridin.todoapp.core.security.JwtAuthFilter;
import com.mohammadnuridin.todoapp.core.security.SecurityHeadersFilter;
import com.mohammadnuridin.todoapp.modules.auth.service.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final SecurityHeadersFilter securityHeadersFilter;
    private final UserDetailsServiceImpl userDetailsService;

    // ── Endpoint yang tidak butuh autentikasi ─────────────────
    private static final String[] PUBLIC_ENDPOINTS = {
            "/",
            "/welcome",
            "/hash", // TEMPORARY — hapus setelah dapat hash
            "/auth/login",
            "/auth/register",
            "/auth/refresh-token",
            "/internal/actuator/**", // Prometheus scrape
            "/v3/api-docs/**", // Swagger
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── Disable CSRF — REST API stateless tidak butuh CSRF ──
                .csrf(AbstractHttpConfigurer::disable)

                // ── Stateless session — JWT handle auth state ────────
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── Custom 401 response ───────────────────────────────
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint))

                // ── Authorization rules ───────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())

                // ── Filter chain: SecurityHeaders → JWT → UsernamePassword ──
                .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationManager menggunakan ProviderManager +
     * DaoAuthenticationProvider.
     *
     * FIX: DaoAuthenticationProvider TIDAK di-expose sebagai @Bean.
     * Dibuat inline di sini agar Spring Security tidak mendeteksi dua sumber
     * konfigurasi auth (UserDetailsService bean + AuthenticationProvider bean)
     * yang menyebabkan warning:
     * "UserDetailsService beans will not be used by Spring Security..."
     *
     * Dengan cara ini, Spring Security hanya melihat satu AuthenticationManager
     * bean dan tidak ambiguous.
     */
    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}