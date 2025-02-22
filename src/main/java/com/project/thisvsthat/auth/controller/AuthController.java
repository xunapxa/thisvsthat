package com.project.thisvsthat.auth.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/auth")
public class AuthController {

    // JWT 비밀키
    @Value("${jwt.secret}")
    private String secretKey;

    @PostMapping("/check-token")
    public ResponseEntity<?> checkToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    String jwtToken = cookie.getValue(); // 쿠키에서 JWT 값 가져오기

                    try {
                        // JWT 토큰을 디코딩하여 Claims 추출
                        Claims claims = Jwts.parser()
                                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                                .parseClaimsJws(jwtToken)
                                .getBody();

                        // userId 추출
                        Long userId = claims.get("userId", Long.class); // userId 클레임에서 추출

                        return ResponseEntity.ok(userId); // userId 반환

                    } catch (SignatureException e) {
                        // 서명 검증 실패시 예외 처리
                        return ResponseEntity.status(401).body("토큰 검증 실패");
                    } catch (Exception e) {
                        // 다른 예외 처리
                        return ResponseEntity.status(400).body("잘못된 토큰");
                    }
                }
            }
        }
        return ResponseEntity.ok("JWT 쿠키 없음");
    }
}