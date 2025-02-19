package com.project.thisvsthat.auth.service;

import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    @Autowired
    private UserRepository userRepository;

    /**
     * JWT 토큰 생성
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail()) // Subject를 email로 설정
                .claim("userId", user.getUserId()) // userId claim 추가
                .setIssuedAt(new Date()) // 발급 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 만료 시간 설정
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256) // HMAC SHA256 서명
                .compact();
    }

    /**
     * JWT를 HTTP-Only 쿠키로 저장
     */
    public void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true); // JavaScript에서 접근 불가
        cookie.setSecure(false); // false: HTTP에서도 쿠키 전송, true: HTTPS에서만 사용
        cookie.setPath("/"); // 모든 경로에서 사용 가능
        cookie.setMaxAge(-1); // 세션 쿠키 (브라우저 종료 시 삭제)
        cookie.setAttribute("SameSite", "Strict"); // 크로스 사이트 요청 불가
        response.addCookie(cookie);
    }

    /**
     * JWT 토큰 유효성 검사
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))) // 시크릿 키 검증
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.out.println("🚨 [ERROR] JWT 토큰이 만료되었습니다.");
            throw e; // 필터에서 만료된 토큰 예외를 캐치할 수 있도록 던짐
        } catch (JwtException e) {
            System.out.println("🚨 [ERROR] 유효하지 않은 JWT 토큰: " + e.getMessage());
            throw new RuntimeException("유효하지 않은 JWT 토큰");
        }
    }

    /**
     * JWT 토큰에서 클레임(Claims) 가져오기
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))) // 시크릿 키로 서명 검증
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.out.println("❌ [ERROR] JWT 토큰이 만료되었습니다.");
            throw new RuntimeException("JWT 토큰이 만료되었습니다.");
        } catch (JwtException e) {
            System.out.println("❌ [ERROR] 유효하지 않은 JWT 토큰입니다.");
            throw new RuntimeException("유효하지 않은 JWT 토큰입니다.");
        }
    }

    /**
     * JWT 토큰에서 이메일 추출
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * JWT 토큰에서 userId 추출
     */
    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    /**
     * JWT 토큰에서 사용자 정보 가져오기 (기존 방식 - 토큰 직접 전달)
     */
    public Optional<User> getUserFromToken(String token) {
        try {
            Long userId = getUserIdFromToken(token); // 토큰에서 userId 추출
            return userRepository.findById(userId); // userId로 사용자 조회
        } catch (Exception e) {
            System.out.println("❌ [ERROR] JWT에서 사용자 정보를 가져오는 데 실패했습니다: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * HTTP-Only 쿠키에서 JWT를 가져와서 사용자 정보 조회
     */
    public Optional<User> getUserFromRequest(HttpServletRequest request) {
        String jwt = getJwtFromCookies(request);
        if (jwt == null) {
            System.out.println("🚨 [ERROR] 요청에서 JWT 쿠키 없음");
            return Optional.empty();
        }

        return getUserFromToken(jwt);
    }

    /**
     * 요청의 쿠키에서 JWT 가져오기
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
}