package com.mohammadnuridin.todoapp.core.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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

        // ── CORS: inject dari application.properties / env var ────────────────
        @Value("${cors.allowed-origins}")
        private String allowedOriginsRaw;

        // ── Endpoint yang tidak butuh autentikasi ─────────────────
        private static final String[] PUBLIC_ENDPOINTS = {
                        "/",
                        "/welcome",
                        "/hash",
                        "/auth/login",
                        "/auth/register",
                        "/auth/refresh-token",
                        "/internal/actuator/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // ── Disable CSRF — REST API stateless tidak butuh CSRF ──
                                .csrf(AbstractHttpConfigurer::disable)

                                // ── CORS — wajib sebelum filter JWT ──────────────────
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // ── Stateless session — JWT handle auth state ────────
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // ── Custom 401 response ───────────────────────────────
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(jwtAuthEntryPoint))

                                // ── Authorization rules ───────────────────────────────
                                .authorizeHttpRequests(auth -> auth
                                                // Preflight OPTIONS request tidak membawa JWT —
                                                // harus permitAll agar tidak ditolak 401 sebelum CORS header balik
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())

                                // ── Filter chain: SecurityHeaders → JWT → UsernamePassword ──
                                .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        // ── CORS Configuration ─────────────────────────────────────────────────
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();

                // Origins dari env var — support multiple origin dipisah koma
                // contoh: CORS_ALLOWED_ORIGINS=http://localhost:3000,https://app.domain.com
                List<String> origins = Arrays.stream(allowedOriginsRaw.split(","))
                                .map(String::trim)
                                .toList();
                config.setAllowedOrigins(origins);

                // Method yang diizinkan — OPTIONS wajib ada untuk preflight
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

                // Header yang boleh dikirim client — Authorization untuk JWT
                config.setAllowedHeaders(List.of("*"));
                // config.setAllowedHeaders(List.of("Authorization", "Content-Type",
                // "X-Requested-With", "lang", "Accept-Language", "X-Client-Type"));

                // Header yang boleh dibaca frontend dari response
                config.setExposedHeaders(List.of("Authorization"));

                // Wajib true jika frontend kirim Authorization header atau cookies
                // TIDAK BISA pakai wildcard origin jika ini true
                config.setAllowCredentials(true);

                // Browser cache hasil preflight selama 1 jam — kurangi OPTIONS request
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/api/**", config);

                // Jika ada endpoint di luar /api/** yang diakses frontend, tambahkan:
                // source.registerCorsConfiguration("/auth/**", config);
                return source;
        }

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