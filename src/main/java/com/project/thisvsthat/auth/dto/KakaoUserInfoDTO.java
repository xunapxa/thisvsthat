package com.project.thisvsthat.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoUserInfoDTO {
    private String id;           // 카카오 고유 ID
    private String email;        // 카카오 이메일
    private String nickname;     // 카카오 닉네임
    private String profileImage; // 카카오 프로필 이미지 URL
}
