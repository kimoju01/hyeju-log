package com.example.hyejulog.config;

import com.example.hyejulog.jwt.exception.CustomAuthenticationEntryPoint;
import com.example.hyejulog.jwt.filter.JwtAuthenticationFilter;
import com.example.hyejulog.jwt.util.JwtTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtTokenizer jwtTokenizer; // JWT 토큰 생성 및 파싱을 위한 유틸 클래스
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint; // 인증 실패 시 커스텀 예외 처리를 위한 클래스

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
        // SecurityFilterChain을 리턴하도록 빈으로 등록
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/assets/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/", "/api/users/**", "/api/posts", "/userregform", "/userreg", "/loginform", "/login", "/refreshToken", "/welcome").permitAll() // 특정 경로에 대한 접근 허용
                        .anyRequest().authenticated())  // 그 외 모든 요청은 인증 필요
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenizer),
                        UsernamePasswordAuthenticationFilter.class) // JWT 인증 필터 추가
                .formLogin(form -> form
                        .disable())  // 기본 폼 로그인을 비활성화
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션을 사용하지 않도록 설정 (JWT 사용)
                .csrf(csrf -> csrf
                        .disable()) // CSRF 보호 비활성화
                .httpBasic(httpBasic -> httpBasic
                        .disable()) // HTTP Basic 인증 비활성화
                .cors(cors -> cors
                        .configurationSource(configurationSource())) // CORS 설정 적용
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)); // 인증 실패 시 예외 처리에 CustomAuthenticationEntryPoint 사용
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public CorsConfigurationSource configurationSource() {
        // CORS 설정 정의
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*"); // 모든 도메인에 대한 접근 허용
        config.addAllowedHeader("*"); // 모든 헤더 허용
        config.addAllowedMethod("*"); // 모든 HTTP 메서드 허용

        config.setAllowedMethods(List.of("GET", "POST", "DELETE")); // 허용할 HTTP 메서드 리스트

        source.registerCorsConfiguration("/**", config); // 모든 경로에 대해 위 설정 적용
        return source;
    }

}
