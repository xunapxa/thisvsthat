package com.project.thisvsthat.auth.service;

import com.project.thisvsthat.auth.dto.GoogleUserInfoDTO;
import com.project.thisvsthat.auth.dto.KakaoUserInfoDTO;
import com.project.thisvsthat.auth.dto.NaverUserInfoDTO;
import com.project.thisvsthat.auth.dto.SignupRequestDTO;
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
import java.util.Collections;
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

        // Access Token이 정상적으로 존재하는지 확인 후 반환
        return Optional.ofNullable(response.getBody())
                .map(body -> body.get("access_token"))
                .map(Object::toString)
                .orElseThrow(() -> new RuntimeException("Google OAuth2 Access Token 요청 실패"));
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
                Optional.ofNullable(attributes.get("picture")).map(Object::toString).orElse(""),
                Optional.ofNullable(attributes.get("name")).map(Object::toString).orElse("")
        );
    }

    /**
     * 카카오 OAuth2 Access Token 요청
     */
    public String getKakaoAccessToken(String code) {
        String tokenUri = env.getProperty("spring.security.oauth2.client.provider.kakao.token-uri");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(
                "grant_type=authorization_code" +
                        "&client_id=" + env.getProperty("spring.security.oauth2.client.registration.kakao.client-id") +
                        "&client_secret=" + env.getProperty("spring.security.oauth2.client.registration.kakao.client-secret") +
                        "&redirect_uri=" + env.getProperty("spring.security.oauth2.client.registration.kakao.redirect-uri") +
                        "&code=" + code, headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenUri, HttpMethod.POST, request, Map.class);

        return Optional.ofNullable(response.getBody())
                .map(body -> body.get("access_token"))
                .map(Object::toString)
                .orElseThrow(() -> new RuntimeException("카카오 OAuth2 Access Token 요청 실패"));
    }

    /**
     * 카카오 사용자 정보 가져오기
     */
    public KakaoUserInfoDTO getKakaoUserInfo(String accessToken) {
        String userInfoUri = env.getProperty("spring.security.oauth2.client.provider.kakao.user-info-uri");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, request, Map.class);

        Map<String, Object> attributes = response.getBody();

        if (attributes == null || !attributes.containsKey("id")) {
            throw new RuntimeException("카카오 사용자 정보 조회 실패");
        }

        Map<String, Object> kakaoAccount = Optional.ofNullable((Map<String, Object>) attributes.get("kakao_account"))
                .orElse(Collections.emptyMap());

        Map<String, Object> profile = Optional.ofNullable((Map<String, Object>) kakaoAccount.get("profile"))
                .orElse(Collections.emptyMap());

        return new KakaoUserInfoDTO(
                attributes.get("id").toString(),
                Optional.ofNullable(kakaoAccount.get("email")).map(Object::toString).orElse("user_" + attributes.get("id") + "@kakao.com"),
                Optional.ofNullable(profile.get("nickname")).map(Object::toString).orElse("사용자"),
                Optional.ofNullable(profile.get("profile_image_url")).map(Object::toString).orElse("")
        );
    }

    /**
     * 네이버 OAuth2 Access Token 요청
     */
    public String getNaverAccessToken(String code) {
        String tokenUri = env.getProperty("spring.security.oauth2.client.provider.naver.token-uri");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(
                "grant_type=authorization_code" +
                        "&client_id=" + env.getProperty("spring.security.oauth2.client.registration.naver.client-id") +
                        "&client_secret=" + env.getProperty("spring.security.oauth2.client.registration.naver.client-secret") +
                        "&redirect_uri=" + env.getProperty("spring.security.oauth2.client.registration.naver.redirect-uri") +
                        "&code=" + code, headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenUri, HttpMethod.POST, request, Map.class);

        return Optional.ofNullable(response.getBody())
                .map(body -> body.get("access_token"))
                .map(Object::toString)
                .orElseThrow(() -> new RuntimeException("네이버 OAuth2 Access Token 요청 실패"));
    }

    /**
     * 네이버 사용자 정보 가져오기
     */
    public NaverUserInfoDTO getNaverUserInfo(String accessToken) {
        String userInfoUri = env.getProperty("spring.security.oauth2.client.provider.naver.user-info-uri");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, request, Map.class);

        Map<String, Object> attributes = response.getBody();

        if (attributes == null || !attributes.containsKey("response")) {
            throw new RuntimeException("네이버 사용자 정보 조회 실패");
        }

        Map<String, Object> responseBody = (Map<String, Object>) attributes.get("response");

        return new NaverUserInfoDTO(
                responseBody.get("id").toString(),
                responseBody.get("email").toString(),
                Optional.ofNullable(responseBody.get("nickname")).map(Object::toString).orElse("사용자"),
                Optional.ofNullable(responseBody.get("profile_image")).map(Object::toString).orElse("")
        );
    }

    /**
     * 회원가입 처리 (기존 회원 조회 후 신규 가입)
     */
    public User registerUser(SignupRequestDTO signupRequest) {
        // 이메일로 기존 회원 조회
        Optional<User> existingUser = userRepository.findByEmail(signupRequest.getEmail());

        // 이미 존재하는 회원인지 확인
        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 차단된 계정인지 확인
            if (user.getUserStatus() == UserStatus.BANNED) {
                throw new RuntimeException("차단된 계정입니다. 가입이 불가능합니다.");
            }

            // 탈퇴한 계정이라면 기존 계정 재활성화
            if (user.getUserStatus() == UserStatus.WITHDRAWN) {
                user.setUserStatus(UserStatus.ACTIVE);
                user.setNickname(signupRequest.getNickname());
                user.setBirthDate(LocalDate.parse(signupRequest.getBirthdate()));
                user.setProfileImageUrl(signupRequest.getProfileImageUrl());
                return userRepository.save(user); // 기존 계정 재사용
            }
        }

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(signupRequest.getNickname())) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        // S3 프로필 이미지 업로드 후 URL 반환
        String s3ProfileImageUrl = s3Service.uploadProfileImage(signupRequest.getProfileImageUrl(), signupRequest.getSocialId());

        // 성별 변환 (MALE, FEMALE만 허용)
        Gender userGender = Optional.ofNullable(signupRequest.getGender())
                .map(String::toUpperCase)
                .map(Gender::valueOf)
                .orElseThrow(() -> new RuntimeException("잘못된 성별 값입니다: " + signupRequest.getGender()));

        // 생년월일 변환 (올바른 형식 확인)
        LocalDate parsedBirthdate = Optional.ofNullable(signupRequest.getBirthdate())
                .map(LocalDate::parse)
                .orElseThrow(() -> new RuntimeException("잘못된 생년월일 형식입니다: " + signupRequest.getBirthdate()));

        // 새 사용자 생성 및 저장
        User newUser = User.builder()
                .email(signupRequest.getEmail())
                .nickname(signupRequest.getNickname())
                .birthDate(parsedBirthdate)
                .gender(userGender)
                .profileImageUrl(s3ProfileImageUrl)
                .socialType(signupRequest.getSocialType())
                .socialId(signupRequest.getSocialId())
                .userStatus(UserStatus.ACTIVE)
                .build();

        return userRepository.save(newUser);
    }
}
