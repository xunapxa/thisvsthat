package com.project.thisvsthat.auth.config;

import com.project.thisvsthat.auth.service.JwtService;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.UserStatus;
import com.project.thisvsthat.common.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 정적 리소스 요청 제외
        if (path.matches(".+\\.(css|js|png|jpg|jpeg|gif|woff2|woff|ttf|svg|ico|map)$")) {
            return true; // 필터 제외
        }

        // JWT 검증이 필요 없는 URL 목록
        List<String> excludedUrls = List.of(
                "/login",
                "/signup",
                "/auth/google/callback", "/auth/kakao/callback"
        );

        return excludedUrls.stream().anyMatch(path::startsWith); // 해당 URL이면 필터 건너뜀
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 쿠키에서 JWT 가져오기
        String jwt = getJwtFromCookies(request);

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. JWT 검증
            Claims claims = jwtService.validateToken(jwt);
            String userEmail = claims.getSubject();

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<User> userOptional = userRepository.findByEmail(userEmail);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    // 3. 차단된 계정인지 확인
                    if (user.getUserStatus() == UserStatus.BANNED) {
                        System.out.println("🚨 [ERROR] 차단된 계정 접근 시도: " + userEmail);
                        deleteJwtCookie(response); // 쿠키 삭제
                        response.sendRedirect("/login/error/banned"); // 차단된 계정 페이지로 이동
                        return;
                    }

                    // 4. 탈퇴한 계정 처리
                    if (user.getUserStatus() == UserStatus.WITHDRAWN) {
                        System.out.println("🚨 [ERROR] 탈퇴한 계정 접근 시도: " + userEmail);
                        deleteJwtCookie(response); // 쿠키 삭제
                        response.sendRedirect("/logout"); // 로그아웃 처리 후 로그인 페이지로 이동
                        return;
                    }

                    // 5. 인증 객체 생성 후 SecurityContextHolder에 저장
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(user); // 인증 객체에 사용자 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("✅ [SUCCESS] SecurityContext에 사용자 인증 설정됨: " + authentication.getName());
                }
            }
        } catch (ExpiredJwtException e) {
            System.out.println("🚨 [ERROR] JWT 토큰 만료됨: " + e.getMessage());
            deleteJwtCookie(response); // 쿠키 삭제 (자동 로그아웃)
        } catch (Exception e) {
            System.out.println("🚨 [ERROR] JWT 검증 실패: " + e.getMessage());
            deleteJwtCookie(response); // 유효하지 않은 토큰이므로 쿠키 삭제
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 쿠키에서 JWT 토큰 가져오기
     */
    private String getJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwt".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        return null;
    }

    /**
     * 쿠키에서 JWT 삭제 (자동 로그아웃)
     */
    private void deleteJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(0); // 쿠키 즉시 삭제
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
        System.out.println("✅ JWT 쿠키 삭제 완료");
    }
}