package com.project.thisvsthat.post.controller;

import com.project.thisvsthat.auth.service.JwtService;
import com.project.thisvsthat.common.dto.PostDTO;
import com.project.thisvsthat.common.dto.VoteDTO;
import com.project.thisvsthat.common.enums.Category;
import com.project.thisvsthat.common.service.ReportService;
import com.project.thisvsthat.image.service.ImageService;
import com.project.thisvsthat.post.dto.VotePercentageDTO;
import com.project.thisvsthat.post.service.PostService;
import com.project.thisvsthat.post.service.VoteService;
import com.project.thisvsthat.post.util.HashtagExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
@RequestMapping("post")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ReportService reportService;

    /* 상세 페이지 */
    @GetMapping("{id}")
    public String postDetail(@PathVariable("id") Long postId, Model model, HttpServletRequest request) {

        Long userId = null;

        //쿠키에서 JWT 토큰 추출
        String token = jwtService.getJwtFromCookies(request);
        if (token == null) {
            userId = null;
        } else {
            userId = jwtService.getUserIdFromToken(token);  // JWT에서 사용자 ID 추출
            if (userId == null) {
                return "redirect:/login";  // 잘못된 토큰인 경우 로그인 페이지로 리다이렉트
            }
        }

        PostDTO dto = postService.findOnePost(postId);
        VotePercentageDTO votePercentage = voteService.getVotePercentage(postId);

        // 글쓴이인지 확인
        if (dto.getUserId() == userId) {
            model.addAttribute("userId", userId);
        } else {
            model.addAttribute("userId", null);
        }

        model.addAttribute("dto", dto);
        model.addAttribute("vote", new VoteDTO());
        model.addAttribute("votePercentage", votePercentage);
        return "post/postDetail";
    }

    /* 작성 페이지 */
    @GetMapping("create")
    public String createPost(Model model) {
        model.addAttribute("dto", new PostDTO());
        return "post/createPost";
    }

    /* 수정 페이지 */
    @GetMapping("{id}/update")
    public String updatePost(@PathVariable("id") Long postId, Model model) {
        PostDTO dto = postService.findOnePost(postId);
        model.addAttribute("dto", dto);
        return "post/updatePost";
    }

    /* 새 글 등록 */
    @PostMapping("create")
    public String insertPost(@ModelAttribute("dto") PostDTO dto,
                             @RequestParam("imageFile1") MultipartFile imageFile1,
                             @RequestParam("imageFile2") MultipartFile imageFile2,
                             Model model, HttpServletRequest request) {

        //쿠키에서 JWT 토큰 추출
        String token = jwtService.getJwtFromCookies(request);
        if (token == null) {
            return "redirect:/login";  // 토큰이 없으면 로그인 페이지로 리다이렉트
        }

        Long userId = jwtService.getUserIdFromToken(token);  // JWT에서 사용자 ID 추출
        if (userId == null) {
            return "redirect:/login";  // 잘못된 토큰인 경우 로그인 페이지로 리다이렉트
        }

        String imageUrl1 = null;
        String imageUrl2 = null;

        try {
            // 이미지 업로드 및 DB 저장
            imageUrl1 = imageService.uploadImage(imageFile1);
            imageUrl2 = imageService.uploadImage(imageFile2);
        } catch (Exception e) {
            model.addAttribute("message", "이미지 업로드 실패: " + e.getMessage());
        }

        System.out.println("화면단에서 받아온 dto 정보 ========== " + dto);

        dto.setOption1ImageUrl(imageUrl1);
        dto.setOption2ImageUrl(imageUrl2);

        // 해시태그 추출 및 설정
        String extractedHashtags = HashtagExtractor.extractHashtags(dto.getContent());
        System.out.println("추출한 해시태그 정보 ==========" + extractedHashtags);
        dto.setHashtags(extractedHashtags);

        postService.savePost(userId, dto);
        return "redirect:/";
    }

    /* 글 수정 */
    @PostMapping("{id}/update")
    public String updatePost(@ModelAttribute("dto") PostDTO dto,
                             @PathVariable("id") Long postId,
                             @RequestParam("imageFile1") MultipartFile imageFile1,
                             @RequestParam("imageFile2") MultipartFile imageFile2,
                             Model model) {

        String imageUrl1 = null;
        String imageUrl2 = null;

        try {
            // 이미지 업로드 및 DB 저장
            imageUrl1 = imageService.uploadImage(imageFile1);
            imageUrl2 = imageService.uploadImage(imageFile2);
        } catch (Exception e) {
            model.addAttribute("message", "이미지 업로드 실패: " + e.getMessage());
        }

        System.out.println("화면단에서 받아온 dto 정보 ========== " + dto);
        dto.setOption1ImageUrl(imageUrl1);
        dto.setOption2ImageUrl(imageUrl2);

        // 해시태그 추출 및 설정
        String extractedHashtags = HashtagExtractor.extractHashtags(dto.getContent());
        System.out.println("추출한 해시태그 정보 ==========" + extractedHashtags);
        dto.setHashtags(extractedHashtags);

        postService.updatePost(postId, dto);

        String url = "redirect:/post/" + postId;
        return url;
    }

    /* 글 삭제 */
    @GetMapping("{id}/delete")
    public String deletePost(@PathVariable("id") Long postId) {
        postService.deletePost(postId);
        return "redirect:/";
    }

    /* 투표 저장 */
    @PostMapping("{id}/vote")
    public String saveVote(@PathVariable("id") Long postId, @ModelAttribute("dto") VoteDTO dto,
                           HttpServletRequest request) {

        //쿠키에서 JWT 토큰 추출
        String token = jwtService.getJwtFromCookies(request);
        if (token == null) {
            return "redirect:/login";  // 토큰이 없으면 로그인 페이지로 리다이렉트
        }

        Long userId = jwtService.getUserIdFromToken(token);  // JWT에서 사용자 ID 추출
        if (userId == null) {
            return "redirect:/login";  // 잘못된 토큰인 경우 로그인 페이지로 리다이렉트
        }

        System.out.println("투표한 내용 ==========" + dto.getSelectedOption());
        voteService.saveVote(userId,postId, dto.getSelectedOption());
        String url = "redirect:/post/" + postId;
        return url;
    }

    /* 투표 종료 */
    @GetMapping("{id}/voteFinished")
    public String voteFinished(@PathVariable("id") Long postId, @ModelAttribute("dto") VoteDTO dto) {
        voteService.voteFinished(postId);
        String url = "redirect:/post/" + postId;
        return url;
    }

    // 게시물 신고 처리 (AJAX)
    @PostMapping("{id}/report")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reportPost(@PathVariable("id") Long postId, HttpServletRequest request) {
        // 쿠키에서 JWT 토큰 추출
        String token = jwtService.getJwtFromCookies(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "로그인 정보가 없습니다."));
        }

        Long userId = jwtService.getUserIdFromToken(token);  // JWT에서 사용자 ID 추출
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "잘못된 사용자입니다."));
        }

        try {
            // 신고 처리 서비스 호출
            reportService.reportPost(postId, userId);  // 서비스에서 처리

            // 신고 처리 성공
            return ResponseEntity.ok(Map.of("success", true, "message", "신고하였습니다."));
        } catch (IllegalArgumentException e) {
            // 중복 신고 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "이미 신고한 게시물입니다."));
        } catch (Exception e) {
            // 신고 처리 중 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
