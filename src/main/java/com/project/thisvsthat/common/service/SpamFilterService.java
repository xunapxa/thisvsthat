package com.project.thisvsthat.common.service;

import com.project.thisvsthat.common.entity.SpamFilter;
import com.project.thisvsthat.common.repository.SpamFilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Transactional
    public void deleteKeywords(List<Long> filterIds) {
        if (filterIds == null || filterIds.isEmpty()) {
            throw new IllegalArgumentException("삭제할 키워드가 없습니다.");
        }
        spamFilterRepository.deleteByFilterIdIn(filterIds);
    }


    // 메시지에 금지된 키워드가 포함되어 있는지 체크하고 포함된 단어를 반환
    public List<String> findSpamWords(String message) {
        List<String> keywords = getAllKeywords();
        List<String> foundSpamWords = new ArrayList<>();

        // 메시지에 금지된 키워드가 포함되어 있는지 체크
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                foundSpamWords.add(keyword);  // 부적절한 단어를 목록에 추가
            }
        }
        return foundSpamWords;  // 부적절한 단어 목록 반환
    }
}
