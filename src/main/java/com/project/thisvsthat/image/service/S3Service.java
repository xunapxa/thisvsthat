package com.project.thisvsthat.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.project.thisvsthat.image.util.FileNameGenerator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class S3Service {
    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.default-profile-url}") // 기본 프로필 이미지 URL
    private String defaultProfileUrl;

    @Autowired
    public S3Service(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // 파일 확장자 추출
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        // Base64 UUID 기반 파일명 생성
        String fileName = FileNameGenerator.generateBase64UUIDFileName(extension);

        // MultipartFile을 InputStream으로 변환
        InputStream inputStream = file.getInputStream();

        // 파일 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        // S3에 파일 업로드
        s3Client.putObject(bucketName, fileName, inputStream, metadata);

        // 업로드된 파일의 S3 URL 반환
        return s3Client.getUrl(bucketName, fileName).toString();
    }

    // Google 프로필 이미지 URL을 S3에 업로드
    public String uploadProfileImage(String imageUrl, String socialId) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return defaultProfileUrl; // 빈 값일 경우 기본 이미지 반환
            }

            URL url = new URL(imageUrl);
            InputStream inputStream = url.openStream();

            // S3에 저장할 파일명 설정
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String fileName = "profile/" + socialId + "_" + timestamp + ".png";

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/png");

            s3Client.putObject(bucketName, fileName, inputStream, metadata);

            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            System.err.println("Google 프로필 이미지 다운로드 실패: " + e.getMessage());
            return defaultProfileUrl; // 실패 시 기본 이미지 반환
        }
    }
}
