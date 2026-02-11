package com.example.usermanagement.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // 🔐 Secret key (must be 32+ characters)
    private static final String SECRET_KEY =
            "mysecretkeymysecretkeymysecretkey123456";

    // ⏱ Token validity: 1 hour
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;

    // 🔑 Create signing key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ✅ Generate JWT with EMAIL + ROLE
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)              // email
                .claim("role", role)            // USER / ADMIN
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Validate JWT
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ✅ Extract EMAIL from JWT
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // ✅ Extract ROLE from JWT
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // 🔁 Common method to read claims
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
