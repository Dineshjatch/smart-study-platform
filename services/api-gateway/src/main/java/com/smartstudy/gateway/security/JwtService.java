package com.smartstudy.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private static final String JWT_SECRET_ENV = "JWT_SECRET";

    private final SecretKey signingKey;

    public JwtService() {
        String jwtSecret = System.getenv(JWT_SECRET_ENV);

        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET environment variable is not configured"
            );
        }

        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "JWT_SECRET must be a valid Base64-encoded secret",
                    e
            );
        }
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");

        if (userId == null) {
            throw new IllegalArgumentException("JWT does not contain userId");
        }

        if (userId instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(userId.toString());
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}