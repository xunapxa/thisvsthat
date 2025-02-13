package com.project.thisvsthat.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;  // JWT 토큰
    private String nickname;  // 사용자 닉네임
}
