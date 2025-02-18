package com.project.thisvsthat.common.service;

import com.project.thisvsthat.common.entity.SpamFilter;
import com.project.thisvsthat.common.repository.SpamFilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpamFilterService {

    private final SpamFilterRepository spamFilterRepository;

    // 금지 키워드 조회
    public List<String> getAllKeywords() {
        List<String> keywords = spamFilterRepository.findAllFilterValues();
        return keywords;
    }

    // 금지 키워드 추가 (중복 검사 후 저장)
    public void addKeyword(String filterValue) {
        if (isFilterValueDuplicate(filterValue)) {
            throw new IllegalArgumentException("이미 등록된 키워드입니다.");
        }
        SpamFilter newKeyword = new SpamFilter();
        newKeyword.setFilterValue(filterValue);
        spamFilterRepository.save(newKeyword);
    }

    public boolean isFilterValueDuplicate(String filterValue) {
        return spamFilterRepository.existsByFilterValue(filterValue);
    }

    // 금지 키워드 삭제
    public void deleteKeywords(List<String> keywords) {
        spamFilterRepository.deleteByFilterValueIn(keywords);
    }

}
