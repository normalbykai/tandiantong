package com.tandiantong.security.auth;

import com.tandiantong.security.context.AccessDomain;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

public class TokenService {

    private final SecretKey secretKey;
    private final long ttlSeconds;

    public TokenService(String secret, long ttlSeconds) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("令牌密钥长度不能少于 32 字节");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(Long userId, AccessDomain domain, int tokenVersion) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ttlSeconds);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("domain", domain.name())
                .claim("tokenVersion", tokenVersion)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public AuthTokenClaims parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new AuthTokenClaims(
                Long.valueOf(claims.getSubject()),
                AccessDomain.valueOf(claims.get("domain", String.class)),
                claims.get("tokenVersion", Integer.class),
                claims.getExpiration().toInstant()
        );
    }

    public AuthTokenClaims parseForDomain(String token, AccessDomain expectedDomain) {
        AuthTokenClaims claims = parse(token);
        if (claims.domain() != expectedDomain) {
            throw new IllegalArgumentException("令牌权限域不匹配");
        }
        return claims;
    }
}
