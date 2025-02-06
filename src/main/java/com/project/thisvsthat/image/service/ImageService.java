package com.project.thisvsthat.image.service;

import com.project.thisvsthat.common.entity.Image;
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

    // 이미지 업로드 및 DB 저장 처리
    public String uploadImage(MultipartFile imageFile) throws Exception {
        // S3에 이미지 업로드
        String imageUrl = s3Service.uploadFile(imageFile);

        // 이미지 URL을 DB에 저장
        Image image = new Image();
        image.setImageUrl(imageUrl);
        imageRepository.save(image);

        return imageUrl;
    }
}