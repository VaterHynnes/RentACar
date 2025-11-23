package de.rentacar.shared.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Konfiguration für RBAC (NFR3, NFR4)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Für REST API, in Produktion sollte CSRF aktiviert sein
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(httpBasic -> {}) // Basic Auth aktivieren
            .authorizeHttpRequests(authz -> authz
                // Öffentliche Endpunkte
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                
                // Kunden-Endpunkte
                .requestMatchers("/api/customers/register").permitAll()
                .requestMatchers("/api/customers/**").hasAnyRole("CUSTOMER", "EMPLOYEE", "ADMIN")
                
                // Buchungs-Endpunkte
                .requestMatchers("/api/bookings/search").hasAnyRole("CUSTOMER", "EMPLOYEE", "ADMIN")
                .requestMatchers("/api/bookings/**").hasAnyRole("CUSTOMER", "EMPLOYEE", "ADMIN")
                
                // Fahrzeug-Endpunkte (nur Mitarbeiter und Admin)
                .requestMatchers("/api/vehicles/**").hasAnyRole("EMPLOYEE", "ADMIN")
                
                // Vermietungs-Endpunkte (nur Mitarbeiter und Admin)
                .requestMatchers("/api/rentals/**").hasAnyRole("EMPLOYEE", "ADMIN")
                
                // Admin-Endpunkte
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Alle anderen Anfragen erfordern Authentifizierung
                .anyRequest().authenticated()
            )
            .userDetailsService(userDetailsService)
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // Für H2 Console

        return http.build();
    }
}

