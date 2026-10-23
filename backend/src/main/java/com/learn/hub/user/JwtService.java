package com.learn.hub.user;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * JWT 签发与解析：HS256，密钥和有效期来自配置。
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long ttlMillis;

    public JwtService(
            @Value("${learn-hub.jwt-secret}") String secret,
            @Value("${learn-hub.jwt-ttl-hours:168}") long ttlHours
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMillis = ttlHours * 60L * 60L * 1000L;
    }

    /**
     * 签发访问令牌：sub 存用户 ID，附带用户名，默认有效期由配置决定。
     */
    public String issue(Long userId, String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttlMillis)))
                .signWith(key)
                .compact();
    }

    /**
     * 校验签名与有效期并取出用户 ID；任何异常都视为未认证，交由调用方返回 401。
     */
    public Optional<Long> parseUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(Long.valueOf(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
