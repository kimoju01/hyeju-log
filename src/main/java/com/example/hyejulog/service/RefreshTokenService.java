package com.example.hyejulog.service;

import com.example.hyejulog.domain.RefreshToken;
import com.example.hyejulog.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = false)
    public RefreshToken addRefreshToken(RefreshToken refreshToken) {
        // 리프레시 토큰 저장
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findRefreshToken(String refreshToken) {
        // 리프레시 토큰 조회
        return refreshTokenRepository.findByValue(refreshToken);
    }

    public void deleteRefreshToken(String refreshToken) {
        // 리프레시 토큰이 존재하면 삭제
        refreshTokenRepository.findByValue(refreshToken).ifPresent(refreshTokenRepository :: delete);
    }

}
