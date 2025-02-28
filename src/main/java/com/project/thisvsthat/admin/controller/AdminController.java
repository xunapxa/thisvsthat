package com.project.thisvsthat.admin.controller;

import com.project.thisvsthat.admin.service.AdminService;
import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.SpamFilter;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.repository.SpamFilterRepository;
import com.project.thisvsthat.common.service.SpamFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final SpamFilterRepository spamFilterRepository;
    @Autowired
    private final SpamFilterService spamFilterService;

    // 금지 키워드 조회, 신고 글, 신고 유저 조회
    @GetMapping("/")
    public String adminReport(Model model) {
        List<User> users = adminService.getReportedUsers();
        List<Post> posts = adminService.getBlindedPosts();
        List<SpamFilter> keywords = spamFilterRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("posts", posts);
        model.addAttribute("keywords", keywords);
        return "admin/admin";
    }

    // 금지 키워드 추가
    @PostMapping("/")
    public String addKeyword(@RequestParam("keyword") String keyword, RedirectAttributes redirectAttributes) {
        try {
            spamFilterService.addKeyword(keyword);
            return "redirect:/admin/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "중복되는 키워드가 이미 있습니다.");
            return "redirect:/admin/";
        }
    }

    // 금지 키워드 삭제
    @PostMapping("/delete-keywords")
    public String deleteKeywords(@RequestParam("keywordIds") List<Long> keywordIds, RedirectAttributes redirectAttributes) {
        try {
            if (keywordIds == null || keywordIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "삭제할 키워드가 없습니다.");
            } else {
                spamFilterService.deleteKeywords(keywordIds);
                redirectAttributes.addFlashAttribute("success", "삭제 완료되었습니다.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "삭제 실패: " + e.getMessage());
        }
        return "redirect:/admin/";
    }

    // 선택된 게시글을 일괄 복구 또는 삭제
    @PostMapping("/updatePostStatus")
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
