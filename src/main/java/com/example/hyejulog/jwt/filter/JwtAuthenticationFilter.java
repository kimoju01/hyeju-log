package com.example.hyejulog.jwt.filter;

import com.example.hyejulog.jwt.exception.JwtExceptionCode;
import com.example.hyejulog.jwt.token.JwtAuthenticationToken;
import com.example.hyejulog.jwt.util.JwtTokenizer;
import com.example.hyejulog.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // 각 요청마다 doFilterInternal 호출 -> 필터 실행 -> 요청 헤더나 쿠키에서 JWT 토큰 추출, 유효성 검증 후 인증 정보 설정

    private final JwtTokenizer jwtTokenizer;

    // 필터 동작 메서드. 각 요청마다 호출됨
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getToken(request);   // request로부터 토큰을 꺼내옴

        // 토큰 검사
        if (StringUtils.hasText(token)) {   // 토큰이 존재할 경우
            try {
                getAuthentication(token);   // 토큰 유효성 검사 및 인증 설정
            } catch (ExpiredJwtException e) {   // 토큰 만료 예외 처리
                request.setAttribute("exception", JwtExceptionCode.EXPIRED_TOKEN.getCode());
                log.error("Expired Token : {}", token, e);
                throw new BadCredentialsException("Expired token exception", e);
            } catch (UnsupportedJwtException e) {   // 지원하지 않는 토큰 예외 처리
                request.setAttribute("exception", JwtExceptionCode.UNSUPPORTED_TOKEN.getCode());
                log.error("Unsupported Token: {}", token, e);
                throw new BadCredentialsException("Unsupported token exception", e);
            } catch (MalformedJwtException e) {     // 토큰 변조 예외 처리
                request.setAttribute("exception", JwtExceptionCode.INVALID_TOKEN.getCode());
                log.error("Invalid Token: {}", token, e);
                throw new BadCredentialsException("Invalid token exception", e);
            } catch (IllegalArgumentException e) {  // 토큰 없음 예외 처리
                request.setAttribute("exception", JwtExceptionCode.NOT_FOUND_TOKEN.getCode());
                log.error("Token not found: {}", token, e);
                throw new BadCredentialsException("Token not found exception", e);
            } catch (Exception e) {     // 기타 예외 처리
                log.error("JWT Filter - Internal Error: {}", token, e);
                throw new BadCredentialsException("JWT filter internal exception", e);
            }
        }
        filterChain.doFilter(request, response);    // 다음 필터로 요청 전달
    }

    // 요청에서 토큰 추출
    private String getToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");  // HTTP 헤더에서 Authorization 값 가져옴
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {    // Bearer 토큰인지 확인
            return authorization.substring(7);  // "Bearer " 부분을 제외한 토큰 값 반환
        }

        // 쿠키에서 토큰 값 추출
        Cookie[] cookies = request.getCookies();    // 쿠키 배열 가져옴
        if (cookies != null) {  // 쿠키가 존재할 경우
            for (Cookie cookie : cookies) { // 모든 쿠키를 검사
                if ("accessToken".equals(cookie.getName())) {   // accessToken 쿠키가 존재하면
                    return cookie.getValue();   // 해당 쿠키의 값 반환
                }
            }
        }

        return null;    // 토큰을 찾지 못한 경우 null 반환
    }

    // 토큰을 기반으로 인증 객체 생성
    private void getAuthentication(String token) {
        Claims claims = jwtTokenizer.parseAccessToken(token);   // 토큰 파싱하여 Claims 객체 생성
        String email = claims.getSubject();                 // Claims에서 이메일 추출
        Long userId = claims.get("userId", Long.class); // Claims에서 사용자 ID 추출
        String name = claims.get("name", String.class); // Claims에서 이름 추출
        String username = claims.get("username", String.class); // Claims에서 사용자 로그인 아이디(username) 추출
        List<GrantedAuthority> authorities = getGrantedAuthorities(claims); // 권한 목록 추출

        // 사용자 정보와 권한으로 CustomUserDetails 객체 생성
        CustomUserDetails userDetails = new CustomUserDetails(username, "", name, authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

        // Authentication 객체 생성하여 SecurityContextHolder에 설정
        Authentication authentication = new JwtAuthenticationToken(authorities, userDetails, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Claims에서 권한 목록 추출
    private List<GrantedAuthority> getGrantedAuthorities(Claims claims) {
        List<String> roles = (List<String>) claims.get("roles"); // Claims에서 roles 추출
        List<GrantedAuthority> authorities = new ArrayList<>(); // GrantedAuthority 객체(권한)를 저장할 리스트 생성
        for (String role : roles) { // roles 목록을 순회하며 권한 객체 생성 및 추가
            authorities.add(() -> role); // GrantedAuthority 인터페이스를 람다식으로 구현
        }

        // 람다식 풀어서 쓰면
//        for (String role : roles) {
//            // 익명 클래스 방식으로 GrantedAuthority 인터페이스 구현
//            GrantedAuthority authority = new GrantedAuthority() {
//                @Override
//                public String getAuthority() {
//                    return role; // role을 반환
//                }
//            };
//            authorities.add(authority); // authorities 목록에 추가
//        }

        return authorities; // 권한 목록 반환
    }

}
