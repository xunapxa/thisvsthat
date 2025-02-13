package com.project.thisvsthat.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.core.env.Environment;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final Environment env;

    @GetMapping("/login")
    public String loginPage(Model model) {
        // 환경 변수에서 값 가져오기
        String googleClientId = env.getProperty("spring.security.oauth2.client.registration.google.client-id");
        String googleRedirectUri = env.getProperty("spring.security.oauth2.client.registration.google.redirect-uri");

        // 잘 가져오는지 확인
        if (googleClientId == null || googleRedirectUri == null) {
            throw new IllegalStateException("Google OAuth 설정이 누락되었습니다.");
        }

        // Google OAuth 로그인 URL 생성
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/auth" +
                "?client_id=" + googleClientId +
                "&redirect_uri=" + googleRedirectUri +
                "&response_type=code" +
                "&scope=email%20profile";

        model.addAttribute("googleAuthUrl", googleAuthUrl);
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signupPage(
            @RequestParam(name = "email", required = false, defaultValue = "") String email,
            @RequestParam(name = "profile", required = false) String profile,
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "socialId", required = false, defaultValue = "") String socialId,
            Model model) {

        // 환경 변수에서 기본 프로필 이미지 가져오기
        String defaultProfileImage = env.getProperty("aws.s3.default-profile-url");

        // 프로필 이미지가 없으면 기본 이미지 사용
        if (profile == null || profile.isEmpty()) {
            profile = defaultProfileImage;
        }

        model.addAttribute("email", email);
        model.addAttribute("profile", profile);
        model.addAttribute("name", name);
        model.addAttribute("socialId", socialId);

        return "auth/signup";
    }

    @GetMapping("/social-mismatch")
    public String socialMismatchPage(){
        return "auth/social-mismatch";
    }
}
