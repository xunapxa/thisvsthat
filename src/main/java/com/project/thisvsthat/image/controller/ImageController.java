package com.project.thisvsthat.image.controller;

import com.project.thisvsthat.image.service.S3Service;
import com.project.thisvsthat.common.entity.ImageEntity;
import com.project.thisvsthat.common.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ImageController {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private ImageRepository imageRepository;

    // 이미지 업로드 폼
    @GetMapping("/upload")
    public String showUploadForm() {
        return "image/uploadForm";  // 업로드 폼 HTML 페이지
    }

    // 이미지 업로드 처리
    @PostMapping("/upload")
    public String handleImageUpload(@RequestParam("imageFile") MultipartFile imageFile, Model model) {
        try {
            // S3에 이미지 업로드
            String imageUrl = s3Service.uploadFile(imageFile);

            // 이미지 URL을 DB에 저장
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setImageUrl(imageUrl);
            imageRepository.save(imageEntity);

            model.addAttribute("message", "이미지가 성공적으로 업로드되었습니다.");
            model.addAttribute("imageUrl", imageUrl);  // 업로드된 이미지 URL 반환
        } catch (Exception e) {
            model.addAttribute("message", "이미지 업로드 실패: " + e.getMessage());
        }
        return "image/uploadResult";  // 업로드 후 결과 페이지
    }
}