package com.club;

import com.club.security.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void generateToken_thenParseUserIdAndUsername() {
        String token = jwtUtils.generateToken(1L, "admin", "ADMIN", Set.of("*:*:*"));
        assertNotNull(token);

        Long userId = jwtUtils.getUserId(token);
        String username = jwtUtils.getUsername(token);

        assertEquals(1L, userId);
        assertEquals("admin", username);
    }

    @Test
    void tamperedToken_thenInvalid() {
        String token = jwtUtils.generateToken(1L, "admin", "ADMIN", Set.of("*:*:*"));
        String tampered = token + "tampered";
        assertFalse(jwtUtils.validateToken(tampered));
    }

    @Test
    void expiredToken_thenInvalid() {
        // 手动生成一个已过期的 token
        SecretKey key = Keys.hmacShaKeyFor("clubflow2024secretkey1234567890abcdef".getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .claim("userId", 1L)
                .claim("username", "admin")
                .issuedAt(new Date(System.currentTimeMillis() - 100000))
                .expiration(new Date(System.currentTimeMillis() - 50000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertFalse(jwtUtils.validateToken(expiredToken));
    }
}
