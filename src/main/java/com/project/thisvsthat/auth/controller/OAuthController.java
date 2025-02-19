package com.project.thisvsthat.auth.controller;

import com.project.thisvsthat.auth.dto.*;
import com.project.thisvsthat.auth.service.JwtService;
import com.project.thisvsthat.auth.service.OAuthService;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.UserStatus;
import com.project.thisvsthat.common.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final Environment env;

    /**
     * Google 로그인 URL로 리디렉트
     */
    @GetMapping("/google/login")
    public void redirectToGoogleAuth(HttpServletResponse response) throws IOException {
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/auth"
                + "?client_id=" + env.getProperty("spring.security.oauth2.client.registration.google.client-id")
                + "&redirect_uri=" + env.getProperty("spring.security.oauth2.client.registration.google.redirect-uri")
                + "&response_type=code"
                + "&scope=email%20profile";

        response.sendRedirect(googleAuthUrl);
    }

    /**
     * Google OAuth Callback
     */
    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam("code") String code,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("📌 Received Google OAuth code: " + code);

        // 1. Google OAuth에서 받은 코드로 Access Token 요청
        String accessToken = oAuthService.getGoogleAccessToken(code);
        System.out.println("📌 Received Google Access Token: " + accessToken);

        // 2. Access Token을 사용하여 사용자 정보 가져오기
        GoogleUserInfoDTO userInfo = oAuthService.getGoogleUserInfo(accessToken);
        System.out.println("📌 Google User Info: " + userInfo);

        // 3. 기존 회원 여부 확인
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 차단된 계정인지 확인
            if (user.getUserStatus() == UserStatus.BANNED) {
                response.sendRedirect("/login/error/banned");
                return;
            }

            // 기존 회원 → JWT 발급 후 HTTP-Only 쿠키 저장
            String jwtToken = jwtService.generateToken(user);
            jwtService.setJwtCookie(response, jwtToken);
            System.out.println("📌 Generated JWT Token: " + jwtToken);

            // 로그인 전 URL 가져오기 (기본값은 홈 `/`)
            String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");
            request.getSession().removeAttribute("redirectUrl"); // 세션 값 삭제 (다시 로그인해도 남아있지 않게)
            response.sendRedirect((redirectUrl != null && !redirectUrl.isEmpty()) ? redirectUrl : "/");
            return;
        }

        // 4. 신규 회원 → 세션에 사용자 정보 저장 후 회원가입 페이지로 리디렉션
        request.getSession().setAttribute("signupUserInfo", userInfo);
        response.sendRedirect("/signup");
    }

    /**
     * Kakao 로그인 URL로 리디렉트
     */
    @GetMapping("/kakao/login")
    public void redirectToKakaoAuth(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + env.getProperty("spring.security.oauth2.client.registration.kakao.client-id")
                + "&redirect_uri=" + env.getProperty("spring.security.oauth2.client.registration.kakao.redirect-uri")
                + "&response_type=code";

        response.sendRedirect(kakaoAuthUrl);
    }

    /**
     * Kakao OAuth Callback
     */
    @GetMapping("/kakao/callback")
    public void kakaoCallback(@RequestParam("code") String code,
                              HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("📌 Received Kakao OAuth code: " + code);

        // 1. 카카오 액세스 토큰 요청
        String accessToken = oAuthService.getKakaoAccessToken(code);
        System.out.println("📌 Received Kakao Access Token: " + accessToken);

        // 2. Access Token을 사용하여 사용자 정보 가져오기
        KakaoUserInfoDTO userInfo = oAuthService.getKakaoUserInfo(accessToken);
        System.out.println("📌 Kakao User Info: " + userInfo);

        // 3. 기존 회원 여부 확인
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 차단된 계정인지 확인
            if (user.getUserStatus() == UserStatus.BANNED) {
                response.sendRedirect("/login/error/banned");
                return;
            }

            // 기존 회원 → JWT 발급 후 HTTP-Only 쿠키 저장
            String jwtToken = jwtService.generateToken(user);
            jwtService.setJwtCookie(response, jwtToken);
            System.out.println("📌 Generated JWT Token: " + jwtToken);

            // 로그인 전 URL 가져오기 (기본값은 홈 `/`)
            String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");
            request.getSession().removeAttribute("redirectUrl"); // 세션 값 삭제
            response.sendRedirect((redirectUrl != null && !redirectUrl.isEmpty()) ? redirectUrl : "/");
            return;
        }

        // 4. 신규 회원 → 세션에 사용자 정보 저장 후 회원가입 페이지로 리디렉션
        request.getSession().setAttribute("signupUserInfo", userInfo);
        response.sendRedirect("/signup");
    }

    /**
     * 네이버 로그인 URL로 리디렉트
     */
    @GetMapping("/naver/login")
    public void redirectToNaverAuth(HttpServletResponse response) throws IOException {
        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize"
                + "?client_id=" + env.getProperty("spring.security.oauth2.client.registration.naver.client-id")
                + "&redirect_uri=" + env.getProperty("spring.security.oauth2.client.registration.naver.redirect-uri")
                + "&response_type=code";

        response.sendRedirect(naverAuthUrl);
    }

    /**
     * 네이버 OAuth Callback
     */
    @GetMapping("/naver/callback")
    public void naverCallback(@RequestParam("code") String code,
                              HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("📌 Received Naver OAuth code: " + code);

        // 1. 네이버 액세스 토큰 요청
        String accessToken = oAuthService.getNaverAccessToken(code);
        System.out.println("📌 Received Naver Access Token: " + accessToken);

        // 2. Access Token을 사용하여 사용자 정보 가져오기
        NaverUserInfoDTO userInfo = oAuthService.getNaverUserInfo(accessToken);
        System.out.println("📌 Naver User Info: " + userInfo);

        // 3. 기존 회원 여부 확인
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 차단된 계정인지 확인
            if (user.getUserStatus() == UserStatus.BANNED) {
                response.sendRedirect("/login/error/banned");
                return;
            }

            // 기존 회원 → JWT 발급 후 HTTP-Only 쿠키 저장
            String jwtToken = jwtService.generateToken(user);
            jwtService.setJwtCookie(response, jwtToken);
            System.out.println("📌 Generated JWT Token: " + jwtToken);

            // 로그인 전 URL 가져오기 (기본값은 홈 `/`)
            String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");
            request.getSession().removeAttribute("redirectUrl"); // 세션 값 삭제
            response.sendRedirect((redirectUrl != null && !redirectUrl.isEmpty()) ? redirectUrl : "/");
            return;
        }

        // 4. 신규 회원 → 세션에 사용자 정보 저장 후 회원가입 페이지로 리디렉션
        request.getSession().setAttribute("signupUserInfo", userInfo);
        response.sendRedirect("/signup");
    }

    /**
     * 닉네임 중복 검사 API
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam("nickname") String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", true));
        }
        boolean isDuplicate = userRepository.existsByNickname(nickname);
        return ResponseEntity.ok(Collections.singletonMap("duplicate", isDuplicate));
    }

    /**
     * 회원가입 API
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDTO> signup(@RequestBody SignupRequestDTO signupRequest, HttpServletRequest request) {
        try {
            // 디버깅 로그 - 회원가입 정보 확인
            System.out.println("Received signup request: " + signupRequest);

            User newUser = oAuthService.registerUser(signupRequest); // 신규 회원 등록
            String jwtToken = jwtService.generateToken(newUser); // 회원가입 후 JWT 토큰 발급

            // 디버깅 로그 - 생성된 JWT 토큰 확인
            System.out.println("Generated JWT Token: " + jwtToken);

            // 회원가입 후 세션 정보 삭제
            request.getSession().removeAttribute("signupUserInfo");

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .body(new AuthResponseDTO(jwtToken, newUser.getNickname(), newUser.getEmail(), newUser.getProfileImageUrl(), signupRequest.getSocialType().name()));
        } catch (Exception e) {
            // 디버깅 로그 - 예외 메시지 확인
            System.out.println("Error during signup: " + e.getMessage());

            return ResponseEntity.badRequest().body(new AuthResponseDTO(null, e.getMessage(), "", "", ""));
        }
    }

    /**
     * JWT 토큰 기반 사용자 정보 조회 (쿠키 기반 인증 적용)
     */
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        Optional<User> userOptional = jwtService.getUserFromRequest(request);

        if (userOptional.isEmpty()) {
            System.out.println("🚨 [ERROR] JWT 쿠키 없음 또는 유효하지 않음");
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "로그인이 필요합니다."));
        }

        User user = userOptional.get();
        System.out.println("✅ [SUCCESS] 사용자 정보:");
        System.out.println("   - ID: " + user.getUserId());
        System.out.println("   - 닉네임: " + user.getNickname());
        System.out.println("   - 이메일: " + user.getEmail());
        System.out.println("   - 프로필 이미지: " + user.getProfileImageUrl());
        System.out.println("   - 가입 날짜: " + user.getCreatedAt());
        System.out.println("   - 상태: " + user.getUserStatus().name());
        System.out.println("   - 로그인 타입: " + user.getSocialType().name());

        Map<String, Object> response = Map.of(
                "userId", user.getUserId(),
                "nickname", user.getNickname(),
                "email", user.getEmail(),
                "profileImage", user.getProfileImageUrl(),
                "createdAt", user.getCreatedAt(),
                "userStatus", user.getUserStatus().name(),
                "socialType", user.getSocialType().name()
        );

        return ResponseEntity.ok(response);
    }
}
