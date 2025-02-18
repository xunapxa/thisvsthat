package com.project.thisvsthat.common.service;

import com.project.thisvsthat.common.entity.SpamFilter;
import com.project.thisvsthat.common.enums.FilterType;
import com.project.thisvsthat.common.repository.SpamFilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpamFilterService {

    private final SpamFilterRepository spamFilterRepository;

    // 📌 금지 키워드 조회
    public List<String> getAllKeywords() {
        List<String> keywords = spamFilterRepository.findAllFilterValues();
        return keywords;
    }

    // 📌 금지 키워드 추가 (중복 검사 후 저장)
    public boolean addKeyword(String keyword) {
        if (spamFilterRepository.existsByFilterValue(keyword)) {
            return false; // 이미 존재하는 키워드
        }
        spamFilterRepository.save(new SpamFilter(FilterType.WORD, keyword)); // 저장
        return true;
    }

    // 📌 금지 키워드 삭제
    public void deleteKeywords(List<String> keywords) {
        spamFilterRepository.deleteByFilterValueIn(keywords);
    }

}
