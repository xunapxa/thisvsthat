package com.project.thisvsthat.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.project.thisvsthat.image.util.FileNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class S3Service {
    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Autowired
    public S3Service(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // Base64 UUID 기반 파일명 생성
        String fileName = FileNameGenerator.generateBase64UUIDFileName();

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
}