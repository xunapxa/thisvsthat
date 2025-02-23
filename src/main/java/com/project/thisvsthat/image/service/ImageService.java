package com.project.thisvsthat.image.service;

import com.project.thisvsthat.common.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private ImageRepository imageRepository;

    // 이미지 업로드
    public String uploadImage(MultipartFile imageFile) throws Exception {
        // S3에 이미지 업로드 후 URL 받기
        String imageUrl = s3Service.uploadFile(imageFile);

        return imageUrl;
    }
}