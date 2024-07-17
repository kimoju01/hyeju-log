package com.example.hyejulog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponseDto {
    // 로그인 응답 시 담을 객체

    private String accessToken;

    private String refreshToken;

    private Long userId;

    private String name;

}
