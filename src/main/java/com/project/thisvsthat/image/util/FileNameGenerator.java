package com.project.thisvsthat.image.util;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

/**
 * 파일명 생성을 위한 유틸리티 클래스
 */
public class FileNameGenerator {

    /**
     * Base64 URL Safe UUID 기반 파일명 생성
     * @param extension 원본 파일 확장자
     * @return 확장자가 포함된 파일명 (예: abcdef123456.png)
     */
    public static String generateBase64UUIDFileName(String extension) {
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        String base64UUID = Base64.getUrlEncoder().withoutPadding().encodeToString(byteBuffer.array());

        return (extension != null && !extension.isEmpty()) ? base64UUID + "." + extension : base64UUID;
    }
}