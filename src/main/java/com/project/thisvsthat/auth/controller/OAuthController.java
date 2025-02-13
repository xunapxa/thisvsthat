package com.project.thisvsthat.auth.controller;

import com.project.thisvsthat.auth.dto.AuthResponseDTO;
import com.project.thisvsthat.auth.dto.GoogleUserInfoDTO;
import com.project.thisvsthat.auth.service.JwtService;
import com.project.thisvsthat.auth.service.OAuthService;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.UserStatus;
import com.project.thisvsthat.common.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final Environment env;

    @GetMapping("/google/login")
    public void redirectToGoogleAuth(HttpServletResponse response) throws IOException {
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/auth"
                + "?client_id=" + env.getProperty("spring.security.oauth2.client.registration.google.client-id")
                + "&redirect_uri=" + env.getProperty("spring.security.oauth2.client.registration.google.redirect-uri")
                + "&response_type=code"
                + "&scope=email%20profile";

        response.sendRedirect(googleAuthUrl);
    }

    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        // Google에서 받은 인증 코드로 Access Token 요청
        String accessToken = oAuthService.getGoogleAccessToken(code);

        // Access Token을 이용해 사용자 정보 가져오기
        GoogleUserInfoDTO userInfo = oAuthService.getGoogleUserInfo(accessToken);

        // 유저 정보 확인
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // [차단된 계정] 로그인 시도 차단
            if (user.getUserStatus() == UserStatus.BANNED) {
                response.sendRedirect("/login?error=banned");
                return;
            }

            // [정상 계정] 로그인 진행 (JWT 발급 후 메인으로 이동)
            String jwtToken = jwtService.generateToken(user);
            response.sendRedirect("/?token=" + jwtToken);
            return;
        }

        // 신규 사용자 → 회원가입 페이지로 리디렉트
        response.sendRedirect(
                "/signup?email=" + URLEncoder.encode(userInfo.getEmail(), StandardCharsets.UTF_8) +
                        "&profile=" + URLEncoder.encode(userInfo.getPicture(), StandardCharsets.UTF_8) +
                        "&name=" + URLEncoder.encode(userInfo.getName(), StandardCharsets.UTF_8) +
                        "&socialId=" + URLEncoder.encode(userInfo.getId(), StandardCharsets.UTF_8));
    }

    // 닉네임 중복 검사
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam("nickname") String nickname) {
        boolean isDuplicate = userRepository.existsByNickname(nickname);
        return ResponseEntity.ok(Collections.singletonMap("duplicate", isDuplicate));
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDTO> signup(
            @RequestParam("email") String email,
            @RequestParam("nickname") String nickname,
            @RequestParam("birthdate") String birthdate,
            @RequestParam("gender") String gender,
            @RequestParam(value = "profileImageUrl", required = false, defaultValue = "/images/default-profile.png") String profileImageUrl,
            @RequestParam("socialId") String socialId) {
        try {
            User newUser = oAuthService.registerUser(email, nickname, birthdate, gender, profileImageUrl, socialId);
            String jwtToken = jwtService.generateToken(newUser);
            return ResponseEntity.ok(new AuthResponseDTO(jwtToken, newUser.getNickname()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new AuthResponseDTO(null, e.getMessage()));
        }
    }
}
