package com.project.thisvsthat.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;       // JWT 토큰
    private String nickname;    // 사용자 닉네임
    private String email;       // 사용자 이메일
    private String profileImageUrl; // 프로필 이미지 URL
    private String socialType;  // 소셜 로그인 타입 (GOOGLE, KAKAO, NAVER)
}

