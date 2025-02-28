package com.project.thisvsthat.auth.controller;

import com.project.thisvsthat.auth.dto.GoogleUserInfoDTO;
import com.project.thisvsthat.auth.dto.KakaoUserInfoDTO;
import com.project.thisvsthat.auth.dto.NaverUserInfoDTO;
import com.project.thisvsthat.auth.service.JwtService;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final Environment env;
    private final JwtService jwtService;

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage(HttpServletRequest request,
                            Model model,
                            @RequestParam(value = "redirect", required = false) String redirectUrl) {
        Optional<User> loggedInUser = jwtService.getUserFromRequest(request);

        if (loggedInUser.isPresent()) {
            User user = loggedInUser.get();

            // 탈퇴된 사용자(WITHDRAWN)나 차단된 사용자(BANNED)는 모델에 값 넣지 않음
            if (user.getUserStatus() == UserStatus.WITHDRAWN || user.getUserStatus() == UserStatus.BANNED) {
                return "auth/login";
            }

            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("socialType", user.getSocialType().name());
        }

        // 리디렉션 URL을 세션에 저장
        if (redirectUrl != null) {
            request.getSession().setAttribute("redirectUrl", redirectUrl);
        }

        return "auth/login";
    }

    /**
     * 로그인 오류 페이지
     */
    @GetMapping("/login/error/{error-type}")
    public String loginError(@PathVariable("error-type") String errorType,
                             @RequestParam(value = "provider", required = false) String provider,
                             Model model) {

        // 오류에 맞는 에러 타입 설정
        // URL 경로에서는 하이픈을 사용하고, 코드에서는 camelCase로 사용
        // 예를 들어, "social-mismatch"는 "socialMismatch"로 모델에 전달
        if ("banned".equals(errorType)) {
            model.addAttribute("errorType", "banned");
        } else if ("social-mismatch".equals(errorType)) {
            model.addAttribute("errorType", "socialMismatch");
            model.addAttribute("provider", provider);
        } else if ("social-failure".equals(errorType)) {
            model.addAttribute("errorType", "socialFailure");
        }

        return "auth/login-error";
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

        // 구글 DTO 처리
        if (socialUserInfo instanceof GoogleUserInfoDTO) {
            GoogleUserInfoDTO googleUser = (GoogleUserInfoDTO) socialUserInfo;
            email = googleUser.getEmail();
            profileImageUrl = googleUser.getPicture();
            nickname = googleUser.getName();
            socialId = googleUser.getId();
            socialType = "GOOGLE";

        // 카카오 DTO 처리
        } else if (socialUserInfo instanceof KakaoUserInfoDTO) {
            KakaoUserInfoDTO kakaoUser = (KakaoUserInfoDTO) socialUserInfo;
            email = kakaoUser.getEmail();
            profileImageUrl = kakaoUser.getProfileImage();
            nickname = kakaoUser.getNickname();
            socialId = kakaoUser.getId();
            socialType = "KAKAO";

        // 네이버 DTO 처리
        } else if (socialUserInfo instanceof NaverUserInfoDTO) {
            NaverUserInfoDTO naverUser = (NaverUserInfoDTO) socialUserInfo;
            email = naverUser.getEmail();
            profileImageUrl = naverUser.getProfileImage();
            nickname = naverUser.getNickname();
            socialId = naverUser.getId();
            socialType = "NAVER";
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
}
