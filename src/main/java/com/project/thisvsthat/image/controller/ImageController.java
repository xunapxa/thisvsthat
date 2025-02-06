package com.project.thisvsthat.image.controller;

import com.project.thisvsthat.image.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ImageController {

    @Autowired
    private ImageService imageService;

    // 이미지 업로드 페이지
    @GetMapping("/upload")
    public String showUploadForm() {
        return "image/upload-form";  // 업로드 페이지
    }

    // 이미지 업로드 처리
    @PostMapping("/upload")
    public String handleImageUpload(@RequestParam("imageFile") MultipartFile imageFile, Model model) {
        try {
            // 이미지 업로드 및 DB 저장
            String imageUrl = imageService.uploadImage(imageFile);

            model.addAttribute("message", "이미지가 성공적으로 업로드되었습니다.");
            model.addAttribute("imageUrl", imageUrl);  // 업로드된 이미지 URL 반환
        } catch (Exception e) {
            model.addAttribute("message", "이미지 업로드 실패: " + e.getMessage());
        }
        return "image/upload-result";  // 업로드 후 결과 페이지
    }
}