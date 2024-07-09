package com.example.hyejulog.security;

import com.example.hyejulog.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {
    private final String username;
    private final String password;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(String username, String password, List<String> roles) {
        this.username = username;
        this.password = password;
        this.authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    // 권한 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // 아이디 반환
    @Override
    public String getUsername() {
        return username;
    }

    // 패스워드 반환
    @Override
    public String getPassword() {
        return password;
    }

    // 계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;    // true = 만료되지 않음
    }

    // 계정 잠김 여부
    @Override
    public boolean isAccountNonLocked() {
        return true;    // true = 잠기지 않음
    }

    // 인증 정보 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;    // true = 만료되지 않음
    }

    // 계정 사용 가능 여부
    @Override
    public boolean isEnabled() {
        return true;    // true = 사용 가능
    }

}