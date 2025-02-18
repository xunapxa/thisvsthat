package com.project.thisvsthat.auth.controller;

import com.project.thisvsthat.auth.dto.*;
import com.project.thisvsthat.auth.service.JwtService;
import com.project.thisvsthat.auth.service.OAuthService;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.UserStatus;
import com.project.thisvsthat.common.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
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
     * JWT를 Authorization 헤더에 설정하고 JSON 응답 반환
     */
    @GetMapping("/google/callback")
    public ResponseEntity<Void> googleCallback(@RequestParam("code") String code, HttpServletRequest request) {
        // 디버깅 로그 - Google OAuth에서 받은 코드 확인
        System.out.println("Received Google OAuth code: " + code);

        // 1. Google OAuth에서 받은 코드로 Access Token 요청
        String accessToken = oAuthService.getGoogleAccessToken(code);

        // 디버깅 로그 - Access Token 확인
        System.out.println("Received Google Access Token: " + accessToken);

        // 2. Access Token을 사용하여 사용자 정보 가져오기
        GoogleUserInfoDTO userInfo = oAuthService.getGoogleUserInfo(accessToken);

        // 디버깅 로그 - 사용자 정보 확인
        System.out.println("Google User Info: " + userInfo);

        // 3. 기존 회원 여부 확인
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 차단된 계정인지 확인
            if (user.getUserStatus() == UserStatus.BANNED) {
                return ResponseEntity.status(302)
                        .header("Location", "/login/error/banned")
                        .build();  // 로그인 에러 페이지로 리디렉션 처리
            }

            // 기존 회원 → JWT 발급 후 메인 페이지 리디렉션
            String jwtToken = jwtService.generateToken(user);

            // 디버깅 로그 - 생성된 JWT 토큰 확인
            System.out.println("Generated JWT Token: " + jwtToken);

            return ResponseEntity.status(302)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .header("Location", "/")  // 메인 페이지로 리디렉션
                    .build();
        }

        // 4. 신규 회원이면 세션에 사용자 정보 저장 후 회원가입 페이지로 리디렉션
        request.getSession().setAttribute("signupUserInfo", userInfo);

        return ResponseEntity.status(302)
                .header("Location", "/signup")  // 회원가입 페이지로 리디렉션
                .build();
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
     * JWT를 Authorization 헤더에 설정하고 JSON 응답 반환
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<Void> kakaoCallback(@RequestParam("code") String code, HttpServletRequest request) {

        // 디버깅 로그 - 카카오에서 받은 코드 확인
        System.out.println("Received Kakao OAuth code: " + code);

        // 1. 카카오 액세스 토큰 요청
        String accessToken = oAuthService.getKakaoAccessToken(code);

        // 디버깅 로그 - Access Token 확인
        System.out.println("Received Kakao Access Token: " + accessToken);

        // 2. 카카오 사용자 정보 가져오기
        KakaoUserInfoDTO userInfo = oAuthService.getKakaoUserInfo(accessToken);

        // 디버깅 로그 - 사용자 정보 확인
        System.out.println("Kakao User Info: " + userInfo);

        // 3. 기존 회원 확인
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 차단된 계정인지 확인
            if (user.getUserStatus() == UserStatus.BANNED) {
                return ResponseEntity.status(302)
                        .header("Location", "/login/error/banned")
                        .build();  // 로그인 에러 페이지로 리디렉션 처리
            }

            // 기존 회원 → JWT 발급 후 메인 페이지 리디렉션
            String jwtToken = jwtService.generateToken(user);

            // 디버깅 로그 - 생성된 JWT 토큰 확인
            System.out.println("Generated JWT Token: " + jwtToken);

            return ResponseEntity.status(302)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .header("Location", "/")  // 메인 페이지로 리디렉션
                    .build();
        }

        // 4. 신규 회원이면, 세션에 사용자 정보 저장 후 회원가입 페이지로 리디렉션
        request.getSession().setAttribute("signupUserInfo", userInfo);

        return ResponseEntity.status(302)
                .header("Location", "/signup")  // 회원가입 페이지로 리디렉션
                .build();
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
    public ResponseEntity<Void> naverCallback(@RequestParam("code") String code, HttpServletRequest request) {

        // 1. 네이버 Access Token 요청
        String accessToken = oAuthService.getNaverAccessToken(code);

        // 2. Access Token을 사용해서 사용자 정보 가져오기
        NaverUserInfoDTO userInfo = oAuthService.getNaverUserInfo(accessToken);

        // 3. 기존 회원 확인
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 차단된 계정인지 확인
            if (user.getUserStatus() == UserStatus.BANNED) {
                return ResponseEntity.status(302)
                        .header("Location", "/login/error/banned")
                        .build();  // 로그인 에러 페이지로 리디렉션 처리
            }

            // 기존 회원 → JWT 발급 후 메인 페이지 리디렉션
            String jwtToken = jwtService.generateToken(user);

            return ResponseEntity.status(302)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                    .header("Location", "/")  // 메인 페이지로 리디렉션
                    .build();
        }

        // 4. 신규 회원이면 세션에 사용자 정보 저장 후 회원가입 페이지로 리디렉션
        request.getSession().setAttribute("signupUserInfo", userInfo);

        return ResponseEntity.status(302)
                .header("Location", "/signup")  // 회원가입 페이지로 리디렉션
                .build();
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
     * JWT 토큰 기반 사용자 정보 조회 (테스트용)
     */
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            System.out.println("🚨 [ERROR] 유효하지 않은 인증 헤더");
            return ResponseEntity.status(401)
                    .body(Collections.singletonMap("error", "유효하지 않은 인증 헤더"));
        }

        String token = authorizationHeader.substring(7);
        try {
            Optional<User> userOptional = jwtService.getUserFromToken(token);
            if (userOptional.isEmpty()) {
                System.out.println("🚨 [ERROR] 유효하지 않은 JWT 토큰");
                return ResponseEntity.status(401)
                        .body(Collections.singletonMap("error", "유효하지 않은 토큰"));
            }

            User user = userOptional.get();
            System.out.println("✅ [SUCCESS] 사용자 정보: " + user.getNickname());

            // 사용자 정보 반환
            Map<String, Object> response = Map.of(
                    "userId", user.getUserId(),
                    "nickname", user.getNickname(),
                    "email", user.getEmail(),
                    "profileImage", user.getProfileImageUrl(),
                    "createdAt", user.getCreatedAt(),  // 계정 생성일
                    "userStatus", user.getUserStatus().name(),  // 활성/차단 상태
                    "socialType", user.getSocialType().name()  // 로그인한 소셜 타입 (Google, Kakao 등)
            );

            return ResponseEntity.ok(response);

        } catch (ExpiredJwtException e) {
            System.out.println("🚨 [ERROR] JWT 토큰이 만료되었습니다.");
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "토큰이 만료되었습니다."));
        } catch (JwtException e) {
            System.out.println("🚨 [ERROR] JWT 파싱 실패: " + e.getMessage());
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "유효하지 않은 토큰"));
        } catch (Exception e) {
            System.out.println("🚨 [ERROR] 알 수 없는 오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "서버 내부 오류"));
        }
    }
}
