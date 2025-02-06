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
     * @return 22자 길이의 Base64 인코딩된 UUID 문자열
     */
    public static String generateBase64UUIDFileName() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(byteBuffer.array());
    }
}