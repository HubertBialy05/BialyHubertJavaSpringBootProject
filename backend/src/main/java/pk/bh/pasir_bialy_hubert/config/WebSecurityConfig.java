package pk.bh.pasir_bialy_hubert.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    // Konstruktor wstrzykujący Twój filtr JWT (Listing 4.12)
    public WebSecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults()) // Włączenie domyślnych ustawień CORS
                .csrf(AbstractHttpConfigurer::disable) // Wyłączenie CSRF dla API
                .authorizeHttpRequests(auth -> auth
                       // .requestMatchers("/api/auth/**").permitAll() // Rejestracja i logowanie są dostępne dla każdego
                        .requestMatchers("/api/auth/**", "/graphql").permitAll() // Rejestracja i logowanie są dostępne dla każdego
                        .anyRequest().authenticated() // Wszystkie inne zapytania (np. transakcje) wymagają tokena
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Aplikacja nie tworzy sesji (używamy JWT)
                )
                // Dodanie filtra JWT przed standardowym filtrem logowania (Listing 4.12)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}