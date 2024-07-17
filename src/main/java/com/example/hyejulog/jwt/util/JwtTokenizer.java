package com.example.hyejulog.jwt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtTokenizer {
    // JWT 토큰 생성, 서명, 검증 + 필요한 정보 파싱하는 유틸리티 클래스

    private final byte[] accessSecret;  // 액세스 토큰 비밀 키 저장할 배열
    private final byte[] refreshSecret; // 리프레시 토큰 비밀 키 저장할 배열

    public final static Long ACCESS_TOKEN_EXPIRE_COUNT = 1 * 60 * 1000L;   // 액세스 토큰 만료 시간 1분 (테스트용)
//    public final static Long ACCESS_TOKEN_EXPIRE_COUNT = 30 * 60 * 1000L;   // 액세스 토큰 만료 시간 30분
    public final static Long REFRESH_TOKEN_EXPIRE_COUNT = 7 * 24 * 60 * 60 * 1000L; // 리프레시 토큰 만료 시간 7일

    // 1. 비밀키 설정
    // 생성자에서 application.properties에 있는 값 받아서 byte 배열로 변환 후 초기화
    public JwtTokenizer(@Value("${jwt.secretKey}") String accessSecret, @Value("${jwt.refreshKey}") String refreshSecret) {
        this.accessSecret = accessSecret.getBytes(StandardCharsets.UTF_8);
        this.refreshSecret = refreshSecret.getBytes(StandardCharsets.UTF_8);
    }

    // 2. 토큰 생성
    // JWT 토큰 생성 메서드
    private String createToken(Long id, String email, String name, String username,
                               List<String> roles, Long expire, byte[] secretKey) {
        Claims claims = Jwts.claims().setSubject(email);    // JWT의 subject를 email로 설정

        // 추가 정보들 claims에 저장
        claims.put("username", username);
        claims.put("name", name);
        claims.put("userId", id);
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)          // 위에서 설정한 claims를 JWT에 포함
                .setIssuedAt(new Date())    // 발행 시간을 현재 시간으로 설정
                .setExpiration(new Date(new Date().getTime() + expire)) // Date 객체에서 현재 시간을 가져와서 현재시간 + 유효기간(expire) 더해서 Date 객체에 넣어줌
                .signWith(getSigningKey(secretKey))     // 비밀키 사용해서 서명
                .compact();                 // JWT 문자열로 직렬화
    }

    // ACCESS TOKEN 생성 메서드
    public String createAccessToken(Long id, String email, String name, String username, List<String> roles) {
        return createToken(id, email, name, username, roles, ACCESS_TOKEN_EXPIRE_COUNT, accessSecret);
    }

    // REFRESH TOKEN 생성 메서드
    public String createRefreshToken(Long id, String email, String name, String username, List<String> roles) {
        return createToken(id, email, name, username, roles, REFRESH_TOKEN_EXPIRE_COUNT, refreshSecret);
    }

    // 3. 서명 키 생성
    // secretKey로 서명 키 생성 메서드 (@param secretKey - byte 형식 / @return Key 형식 시크릿 키)
    public static Key getSigningKey(byte[] secretKey) {
        return Keys.hmacShaKeyFor(secretKey);
    }

    // 4. 토큰 파싱
    // JWT 토큰에서 userId 추출 메서드
    public Long getUserIdFromToken(String token){
        String[] tokenArr = token.split(" ");       // 토큰 문자열 분리
        token = tokenArr[1];                                // 분리된 토큰의 두 번째 부분 사용
        Claims claims = parseToken(token, accessSecret);     // 토큰 파싱해서 claims를 얻음
        return Long.valueOf((Integer)claims.get("userId")); // claims에서 userId 추출해서 Long으로 변환
    }

    // secretKey로 JWT 토큰 파싱 메서드
    public Claims parseToken(String token, byte[] secretKey){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(secretKey))    // 서명 키 설정
                .build()
                .parseClaimsJws(token)  // 토큰 파싱
                .getBody();             // 파싱된 claims 반환
    }

    // 액세스 토큰 파싱 메서드
    public Claims parseAccessToken(String accessToken) {
        return parseToken(accessToken, accessSecret);
    }

    // 리프레시 토큰 파싱 메서드
    public Claims parseRefreshToken(String refreshToken) {
        return parseToken(refreshToken, refreshSecret);
    }

}
