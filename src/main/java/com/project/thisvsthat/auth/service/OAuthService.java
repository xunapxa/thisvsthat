package com.project.thisvsthat.auth.service;

import com.project.thisvsthat.auth.dto.GoogleUserInfoDTO;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.Gender;
import com.project.thisvsthat.common.enums.SocialType;
import com.project.thisvsthat.common.enums.UserStatus;
import com.project.thisvsthat.common.repository.UserRepository;
import com.project.thisvsthat.image.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final Environment env;
    private final S3Service s3Service;

    /**
     * Google OAuth2 Access Token 요청
     */
    public String getGoogleAccessToken(String code) {
        String tokenUri = env.getProperty("spring.security.oauth2.client.provider.google.token-uri");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(
                "code=" + code +
                        "&client_id=" + env.getProperty("spring.security.oauth2.client.registration.google.client-id") +
                        "&client_secret=" + env.getProperty("spring.security.oauth2.client.registration.google.client-secret") +
                        "&redirect_uri=" + env.getProperty("spring.security.oauth2.client.registration.google.redirect-uri") +
                        "&grant_type=authorization_code", headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenUri, HttpMethod.POST, request, Map.class);

        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            throw new RuntimeException("Google OAuth2 Access Token 요청 실패");
        }

        return response.getBody().get("access_token").toString();
    }

    /**
     * Google 사용자 정보 가져오기
     */
    public GoogleUserInfoDTO getGoogleUserInfo(String accessToken) {
        String userInfoUri = env.getProperty("spring.security.oauth2.client.provider.google.user-info-uri");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, request, Map.class);

        Map<String, Object> attributes = response.getBody();

        if (attributes == null || !attributes.containsKey("sub") || !attributes.containsKey("email")) {
            throw new RuntimeException("Google 사용자 정보 조회 실패");
        }

        return new GoogleUserInfoDTO(
                attributes.get("sub").toString(),
                attributes.get("email").toString(),
                attributes.getOrDefault("picture", "").toString(),  // 값이 없을 경우 빈 문자열 반환
                attributes.getOrDefault("name", "").toString()  // 값이 없을 경우 빈 문자열 반환
        );
    }

    /**
     * 회원가입 처리 (기존 회원 조회 후 신규 가입)
     */
    public User registerUser(String email, String nickname, String birthdate, String gender, String profileImageUrl, String socialId) {
        // [차단된 계정] 가입 차단
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent() && existingUser.get().getUserStatus() == UserStatus.BANNED) {
            throw new RuntimeException("차단된 계정입니다. 가입이 불가능합니다.");
        }

        // ✅ [탈퇴한 계정] 기존 데이터 복구 후 재가입 처리 <-- 이후 수정 예정
        if (existingUser.isPresent() && existingUser.get().getUserStatus() == UserStatus.WITHDRAWN) {
            User withdrawnUser = existingUser.get();
            withdrawnUser.setUserStatus(UserStatus.ACTIVE);
            withdrawnUser.setNickname(nickname);
            withdrawnUser.setBirthDate(LocalDate.parse(birthdate));
            withdrawnUser.setProfileImageUrl(profileImageUrl);
            return userRepository.save(withdrawnUser);
        }

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(nickname)) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        // Google 프로필 이미지를 S3에 업로드 후 URL 반환
        String s3ProfileImageUrl = s3Service.uploadProfileImage(profileImageUrl, socialId);

        // Gender 변환 예외 처리
        Gender userGender;
        try {
            userGender = Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RuntimeException("잘못된 성별 값입니다: " + gender);
        }

        // 생년월일 변환 예외 처리
        LocalDate parsedBirthdate;
        try {
            parsedBirthdate = LocalDate.parse(birthdate);
        } catch (Exception e) {
            throw new RuntimeException("잘못된 생년월일 형식입니다: " + birthdate);
        }

        // 새 사용자 생성 및 저장
        User newUser = User.builder()
                .email(email)
                .nickname(nickname)
                .birthDate(LocalDate.parse(birthdate))
                .gender(userGender)
                .profileImageUrl(s3ProfileImageUrl)
                .socialType(SocialType.GOOGLE)
                .socialId(socialId)
                .userStatus(UserStatus.ACTIVE)
                .build();

        return userRepository.save(newUser);
    }
}
