package com.example.hyejulog.jwt.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    // AbstarctAuthenticationToken은 시큐리티 컨텍스트의 Authentication 인터페이스를 구현하고 있는 추상 클래스

    private String token;
    private Object principal;
    private Object credentials;

    // 생성자 - 권한(authorities), 주체(principal), 자격 증명(credentials)을 매개변수로 받아 인증된 상태로 설정되도록 초기화
    public JwtAuthenticationToken(Collection<? extends GrantedAuthority> authorities, Object principal, Object credentials) {
        super(authorities);             // 부모 클래스(AbstractAuthenticationToken)의 생성자 호출해 권한 설정
        this.principal = principal;     // 주체 설정
        this.credentials = credentials; // 자격 증명 설정
        this.setAuthenticated(true);    // 인증 여부 설정
    }

    // 생성자 - 토큰(token)을 매개변수로 받아 인증되지 않은 상태로 설정되도록 초기화
    public JwtAuthenticationToken(String token) {
        super(null); // 권한(authorities)이 null인 경우 부모 클래스의 생성자 호출
        this.token = token; // 토큰 설정
        this.setAuthenticated(false); // 인증 여부 설정 (토큰의 유효성 확인 전이기 떄문에 인증X 상태로 설정 -> 유효성 검증되면 위에 있는 생성자 사용해서 인증 상태로 전환)
    }

    // 자격 증명(credentials) 반환
    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    // 주체(principal) 반환
    @Override
    public Object getPrincipal() {
        return this.principal;
    }

}
