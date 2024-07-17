package com.example.hyejulog.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final String name;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(String username, String password, String name, List<String> roles) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        // roles 리스트를 SimpleGrantedAuthority 객체로 변환하여 authorities 리스트를 초기화
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { // 사용자 권한을 반환
        return authorities; // 권한 목록 반환
    }

    @Override
    public String getPassword() { // 사용자 비밀번호를 반환
        return password; // 비밀번호 반환
    }

    @Override
    public String getUsername() { // 사용자 이름을 반환
        return username; // 사용자 이름 반환
    }

    @Override
    public boolean isAccountNonExpired() { // 계정이 만료되지 않았는지 여부를 반환
        return true; // 편의상 true로 반환
    }

    @Override
    public boolean isAccountNonLocked() { // 계정이 잠기지 않았는지 여부를 반환
        return true; // 편의상 true로 반환
    }

    @Override
    public boolean isCredentialsNonExpired() { // 자격 증명이 만료되지 않았는지 여부를 반환
        return true; // 편의상 true로 반환
    }

    @Override
    public boolean isEnabled() { // 계정이 활성화되었는지 여부를 반환
        return true; // 편의상 true로 반환
    }
}