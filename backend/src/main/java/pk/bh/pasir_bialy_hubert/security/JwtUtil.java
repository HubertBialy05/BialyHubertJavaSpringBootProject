package pk.bh.pasir_bialy_hubert.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pk.bh.pasir_bialy_hubert.model.User; // Dostosowane do Twojego pakietu

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final long EXPIRATION_MS = 3_600_000L;
    private final SecretKey key;

    // Konstruktor wstrzykujący sekret z właściwości i generujący bezpieczny klucz
    public JwtUtil(
            @Value("${JWT_SECRET}") String jwtSecret
    ) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured");
        }
        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < 64) {
            throw new IllegalStateException("JWT secret must be at least 64 bytes for HS512");
        }
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Generowanie tokenu na podstawie obiektu User wraz z dodatkowymi informacjami (id, email)
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    // Wyciąganie wszystkich informacji (claims) z tokenu
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Wyciąganie nazwy użytkownika (subject / email)
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Walidacja poprawności tokenu
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}