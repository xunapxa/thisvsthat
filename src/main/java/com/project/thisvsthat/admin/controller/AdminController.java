package com.project.thisvsthat.admin.controller;

import com.project.thisvsthat.admin.service.AdminService;
import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("admin")
public class AdminController {

    private final AdminService adminService;

    private  AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 신고 글, 신고 유저 조회
    @GetMapping("/")
    public String adminReport(Model model) {
        List<User> users = adminService.getReportUsers();
        List<Post> posts = adminService.getBlindedPosts();
        model.addAttribute("users", users);
        model.addAttribute("posts", posts);
        return "admin/admin";
    }

    // 신고 글 복구
    @PostMapping("/restore")
    public String restorePost(@RequestParam Long postId) {
        adminService.restorePost(postId);
        return "redirect:admin/";
    }

    //신고 글 삭제
    @PostMapping("/delete")
    public String deletePost(@RequestParam Long postId) {
        adminService.deletePost(postId);
        return  "redirect:admin/";
    }
}
