package com.project.thisvsthat.auth.dto;

import com.project.thisvsthat.common.enums.SocialType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDTO {
    private String email;            // 이메일
    private String nickname;         // 닉네임
    private String birthdate;        // 생년월일
    private String gender;           // 성별
    private String profileImageUrl;  // 프로필 이미지 URL (S3 업로드 후 반환된 URL)
    private String socialId;         // 소셜 로그인 ID
    private SocialType socialType;   // 소셜 로그인 타입 (GOOGLE, KAKAO, NAVER)
}
