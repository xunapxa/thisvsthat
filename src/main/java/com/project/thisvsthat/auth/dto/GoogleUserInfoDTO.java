package com.project.thisvsthat.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleUserInfoDTO {
    private String id;         // Google 고유 ID
    private String email;      // Google 이메일
    private String picture;    // Google 프로필 이미지 URL
    private String name;       // Google 이름
}
