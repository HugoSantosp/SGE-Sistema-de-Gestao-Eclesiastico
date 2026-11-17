package com.sg.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(String email, String nivelAcesso) {
        return generateToken(email, nivelAcesso, 0L);
    }

    public String generateToken(String email, String nivelAcesso, long tokenVersion) {
        return Jwts.builder()
                .subject(email)
                .claim("nivel", nivelAcesso)
                .claim("ver", tokenVersion)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Versão do token gravada no claim {@code ver}. Tokens antigos (sem o claim)
     * são tratados como versão 0.
     */
    public long extractTokenVersion(String token) {
        Object ver = extractClaims(token).get("ver");
        if (ver instanceof Number numero) {
            return numero.longValue();
        }
        if (ver instanceof String texto) {
            try {
                return Long.parseLong(texto);
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractNivel(String token) {
        return extractClaims(token).get("nivel", String.class);
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
