package com.example.hyejulog.jwt.util;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class JwtTokenizerTest {

    @Autowired
    JwtTokenizer jwtTokenizer;

    @Test
    void createAccessToken() {
        String accessToken = jwtTokenizer.createAccessToken(
                1L,
                "test@exam.com",
                "testName",
                "testUsername",
                Arrays.asList("ROLE_USER")
        );
        log.info("AccessToken {}", accessToken);

        Claims claims = jwtTokenizer.parseAccessToken(accessToken);
        assertEquals("test@exam.com", claims.getSubject());
        assertEquals("testName", claims.get("name"));
        assertEquals("testUsername", claims.get("username"));
        assertEquals(1L, jwtTokenizer.getUserIdFromToken("Bearer " + accessToken));
    }

    @Test
    void createRefreshToken() {
        String refreshToken = jwtTokenizer.createRefreshToken(
                1L,
                "test@exam.com",
                "testName",
                "testUsername",
                Arrays.asList("ROLE_USER")
        );
        log.info("RefreshToken {}", refreshToken);

        Claims claims = jwtTokenizer.parseRefreshToken(refreshToken);
        assertEquals("test@exam.com", claims.getSubject());
        assertEquals("testName", claims.get("name"));
        assertEquals("testUsername", claims.get("username"));
    }
  
}