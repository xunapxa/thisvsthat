package com.project.thisvsthat.auth.config;

import com.project.thisvsthat.auth.service.JwtService;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.UserStatus;
import com.project.thisvsthat.common.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 요청에서 JWT 토큰 추출
        String token = extractToken(request);

        if (token != null) {
            try {
                // JWT에서 사용자 정보 추출
                Claims claims = jwtService.getClaims(token);
                String email = claims.getSubject();
                System.out.println("📌 [DEBUG] 토큰에서 추출한 이메일: " + email);

                // DB에서 사용자 조회
                Optional<User> userOptional = userRepository.findByEmail(email);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    System.out.println("📌 [DEBUG] 데이터베이스에서 조회된 사용자: " + user.getEmail());

                    // [차단된 계정] 요청 차단
                    if (user.getUserStatus() == UserStatus.BANNED) {
                        System.out.println("🚨 [ERROR] 차단된 계정입니다.");
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "차단된 계정입니다.");
                        return;
                    }

                    // UserDetails 생성 후 인증 처리
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("✅ [SUCCESS] 인증 성공! 사용자: " + email);
                } else {
                    System.out.println("🚨 [ERROR] 데이터베이스에서 사용자 찾을 수 없음.");
                }
            } catch (Exception e) {
                System.out.println("🚨 [ERROR] JWT 검증 실패: " + e.getMessage());
            }
        }

        // 다음 필터로 요청 전달
        chain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 Bearer 토큰 추출
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
