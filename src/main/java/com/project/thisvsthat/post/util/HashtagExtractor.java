package com.project.thisvsthat.post.util;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HashtagExtractor {
    public static String extractHashtags(String content) {
        if (content == null || content.isEmpty()) return "";

        // 정규식: '#'으로 시작하고, 공백 또는 특수문자가 나오기 전까지를 해시태그로 인식
        Pattern pattern = Pattern.compile("#[\\w가-힣]+");
        Matcher matcher = pattern.matcher(content);

        // 해시태그 목록 저장 (중복 제거)
        Set<String> hashtags = new LinkedHashSet<>();

        while (matcher.find()) {
            hashtags.add(matcher.group()); // 찾은 해시태그 추가
        }

        // 공백으로 구분된 문자열로 반환
        return String.join(" ", hashtags);
    }
}
