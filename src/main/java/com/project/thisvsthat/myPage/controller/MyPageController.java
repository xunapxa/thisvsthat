package com.project.thisvsthat.myPage.controller;

import com.project.thisvsthat.auth.service.JwtService;
import com.project.thisvsthat.common.dto.PostDTO;
import com.project.thisvsthat.common.dto.UserDTO;
import com.project.thisvsthat.common.repository.UserRepository;
import com.project.thisvsthat.myPage.service.MyPageService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class MyPageController {
    @Autowired
    MyPageService myPageService;

    @Autowired
    JwtService jwtService; // JwtService를 주입 받아 JWT 처리

    //사용자 상세 정보 조회
    @GetMapping("")
    public String myPageMain(Model model, HttpServletRequest request) {
        //쿠키에서 JWT 토큰 추출
        String token = jwtService.getJwtFromCookies(request);
        if (token == null) {
            return "redirect:/login";  // 토큰이 없으면 로그인 페이지로 리다이렉트
        }

        Long userId = jwtService.getUserIdFromToken(token);  // JWT에서 사용자 ID 추출
        if (userId == null) {
            return "redirect:/login";  // 잘못된 토큰인 경우 로그인 페이지로 리다이렉트
        }

        // 참여했던 채팅방의 PostDTO 목록 조회
        List<PostDTO> participatedPosts = myPageService.getUserParticipatedPosts(userId);

        UserDTO dto = myPageService.findLoginUser(userId);
        if (dto != null) {
            // 연령대 계산
            String ageGroup = dto.getAgeGroup();

            // 내가 올린 게시물과 내가 투표한 게시물 가져오기
            List<PostDTO> myPosts = myPageService.findMyPosts(userId);
            List<PostDTO> votedPosts = myPageService.findVotedPosts(userId);

            model.addAttribute("dto", dto);
            model.addAttribute("ageGroup", ageGroup);
            model.addAttribute("myPosts", myPosts);
            model.addAttribute("votedPosts", votedPosts);
            model.addAttribute("participatedPosts", participatedPosts);
        } else {
            return "redirect:/login";  // 유저 정보가 없으면 로그인 페이지로 리다이렉트
        }

        return "myPage/myPage";
    }

    //정보 수정(닉네임) 처리
    @PatchMapping("")
    public ResponseEntity<Map<String, Object>> editNickname(@RequestParam(name = "nickname") String nickname, HttpServletRequest request) {
        String token = jwtService.getJwtFromCookies(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Long userId = jwtService.getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        boolean editSuccess = myPageService.infoEdit(userId, nickname);

        Map<String, Object> response = new HashMap<>();
        response.put("success", editSuccess);
        if (editSuccess) {
            response.put("updatedNickname", nickname);
        }

        return ResponseEntity.ok(response);
    }


    //탈퇴하기 (status = withdrawn)
    @PatchMapping("/withdrawn")
    public ResponseEntity<Map<String, Object>> withdrawnUser(HttpServletRequest request) {
        String token = jwtService.getJwtFromCookies(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Long userId = jwtService.getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Unauthorized"));
        }

        boolean withdrawnSuccess = myPageService.withdrawnUser(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", withdrawnSuccess);

        return ResponseEntity.ok(response);
    }
}