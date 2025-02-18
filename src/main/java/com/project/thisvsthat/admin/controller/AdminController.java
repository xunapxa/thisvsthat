package com.project.thisvsthat.admin.controller;

import com.project.thisvsthat.admin.service.AdminService;
import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.repository.SpamFilterRepository;
import com.project.thisvsthat.common.service.SpamFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final SpamFilterRepository spamFilterRepository;
    private final SpamFilterService spamFilterService;

    // 금지 키워드 조회, 신고 글, 신고 유저 조회
    @GetMapping("/")
    public String adminReport(Model model) {
        List<User> users = adminService.getReportedUsers();
        List<Post> posts = adminService.getBlindedPosts();
        List<String> keywords = spamFilterService.getAllKeywords();
        model.addAttribute("users", users);
        model.addAttribute("posts", posts);
        model.addAttribute("keywords", keywords);
        return "admin/admin";
    }

    // 📌 금지 키워드 추가
    @PostMapping("/spam-filters/add")
    public ResponseEntity<String> addSpamFilter(@RequestParam("keyword") String keyword) {
        boolean isAdded = adminService.addKeyword(keyword);
        if (!isAdded) {
            return ResponseEntity.badRequest().body("이미 등록된 키워드가 있습니다.");
        }
        return ResponseEntity.ok("키워드가 추가되었습니다.");
    }

    // 📌 금지 키워드 삭제
    @PostMapping("/spam-filters/delete")
    public ResponseEntity<String> deleteSpamFilters(@RequestParam("keywords") List<String> keywords) {
        adminService.deleteKeywords(keywords);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    // 선택된 게시글을 일괄 복구 또는 삭제
    @PostMapping("/updateMultiplePostStatus")
    public String updateMultiplePostStatus(@RequestParam(value = "postIds") List<Long> postIds, @RequestParam(value = "postActionType") String postActionType) {
        if ("restore".equals(postActionType)) {
            adminService.restorePosts(postIds);
        } else if ("delete".equals(postActionType)) {
            adminService.deletePosts(postIds);
        }
        return "redirect:/admin/";  // 변경 후 새로고침
    }

    // 선택된 유저 복구 또는 차단
    @PostMapping("/updateUserStatus")
    public String updateUserStatus(@RequestParam(value = "userIds") List<Long> userIds, @RequestParam(value = "userActionType") String userActionType) {
        if ("restore".equals(userActionType)) {
            adminService.restoreUsers(userIds);
        } else if ("ban".equals(userActionType)) {
            adminService.banUsers(userIds);
        }
        return "redirect:/admin/";  // 변경 후 새로고침
    }

    // 유저가 쓴 신고된 글 + 삭제된 글 조회
    @GetMapping("/reported-posts")
    public String getReportedAndDeletedPosts(@RequestParam(name = "reportUserId") Long userId, Model model) {
        List<Post> reportedPosts = adminService.getBlindedAndDeletedPosts(userId);
        model.addAttribute("reportedPosts", reportedPosts);
        model.addAttribute("userId", userId);
        return "admin/admin";
    }

}
