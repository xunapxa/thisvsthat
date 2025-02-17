package com.project.thisvsthat.auth.controller;

import com.project.thisvsthat.auth.dto.GoogleUserInfoDTO;
import com.project.thisvsthat.auth.dto.KakaoUserInfoDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.core.env.Environment;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final Environment env;

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage(Model model) {

        // 디버깅 로그 - Google OAuth 로그인 URL 확인
        System.out.println("Redirecting to Google OAuth login URL");

        // Google OAuth 로그인 URL을 `/auth/google/login`을 통해 리디렉트하도록 변경
        String googleAuthUrl = "/auth/google/login";

        model.addAttribute("googleAuthUrl", googleAuthUrl);
        return "auth/login";
    }

    /**
     * 로그인 오류 페이지
     */
    @GetMapping("/login/error/{errorType}")
    public String loginError(@PathVariable("errorType") String errorType, Model model) {

        // 디버깅 로그 - 오류 타입 확인
        System.out.println("Login error: " + errorType);

        if ("banned".equals(errorType)) {
            model.addAttribute("error", "banned");
        }
        return "auth/login";
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping("/signup")
    public String signupPage(HttpServletRequest request, Model model) {

        // 세션에서 저장된 소셜 회원가입 정보를 가져옴
        Object socialUserInfo = request.getSession().getAttribute("signupUserInfo");

        if (socialUserInfo == null) {
            // 세션 정보가 없으면 로그인 페이지로 리디렉션
            return "redirect:/login";
        }

        String email = null;
        String profileImageUrl = null;
        String nickname = null;
        String socialId = null;
        String socialType = null;

        // 구글 & 카카오 DTO 처리
        if (socialUserInfo instanceof GoogleUserInfoDTO) {
            GoogleUserInfoDTO googleUser = (GoogleUserInfoDTO) socialUserInfo;
            email = googleUser.getEmail();
            profileImageUrl = googleUser.getPicture();
            nickname = googleUser.getName();
            socialId = googleUser.getId();
            socialType = "GOOGLE";
        } else if (socialUserInfo instanceof KakaoUserInfoDTO) {
            KakaoUserInfoDTO kakaoUser = (KakaoUserInfoDTO) socialUserInfo;
            email = kakaoUser.getEmail();
            profileImageUrl = kakaoUser.getProfileImage();
            nickname = kakaoUser.getNickname();
            socialId = kakaoUser.getId();
            socialType = "KAKAO";
        }

        // 기본 프로필 이미지 설정 (없을 경우 환경 변수에서 가져오기)
        if (profileImageUrl == null || profileImageUrl.isEmpty()) {
            profileImageUrl = env.getProperty("aws.s3.default-profile-url");
        }

        // Model에 데이터 추가
        model.addAttribute("email", email);
        model.addAttribute("profileImageUrl", profileImageUrl);
        model.addAttribute("nickname", nickname);
        model.addAttribute("socialId", socialId);
        model.addAttribute("socialType", socialType);

        return "auth/signup";
    }

    /**
     * 소셜 로그인 정보 불일치 시 안내 페이지
     */
    @GetMapping("/social-mismatch")
    public String socialMismatchPage() {

        // 디버깅 로그 - 소셜 로그인 정보 불일치 확인
        System.out.println("User's social login information mismatch detected.");

        return "auth/social-mismatch";
    }
}
