package com.project.thisvsthat.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class NaverUserInfoDTO {
    private String id;           // 네이버 고유 ID
    private String email;        // 네이버 이메일
    private String nickname;     // 네이버 닉네임
    private String profileImage; // 네이버 프로필 이미지 URL
}
