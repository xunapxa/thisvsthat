package com.project.thisvsthat.index.service;

import com.project.thisvsthat.common.dto.PostDTO;
import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.repository.PostRepository;
import com.project.thisvsthat.index.dto.PostListResponseDTO;
import com.project.thisvsthat.index.specification.PostSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class IndexService {

    @Autowired
    private PostRepository postRepository;

    private static final int PAGE_SIZE = 3;  // 한 번에 보여줄 게시물 개수

    // 메인페이지(검색조건에 따른 목록 출력)
    public PostListResponseDTO getFilteredPosts(int page, String searchBy, String keyword, String listCategory, String listDesc, String voteStatus, String startDate, String endDate, int pageCnt) {
        Specification<Post> spec = Specification.where(PostSpecification.isActive());

        // 검색어 필터링
        if (!keyword.isBlank()) {
            spec = spec.and(PostSpecification.containsKeyword(searchBy, keyword));
        }

        // 카테고리 필터링
        if (!listCategory.isBlank()) {
            spec = spec.and(PostSpecification.hasCategory(listCategory));
        }

        // 정렬(인기순) 필터링
        if ("popularity".equals(listDesc)) {
            spec = spec.and(PostSpecification.orderByPopularity()); // 인기순 정렬
        }

        // 투표 상태 필터링
        if (!voteStatus.isBlank()) {
            spec = spec.and(PostSpecification.hasVoteStatus(voteStatus));
        }

        // 날짜 필터링
        if ("popularity".equals(listDesc) && !startDate.isBlank() && !endDate.isBlank()) {
            spec = spec.and(PostSpecification.isCreatedBetween(startDate, endDate));
        }

        // pageCnt == 1 일 때 => 무한 스크롤 
        if (pageCnt == 1) {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            List<PostDTO> posts = postRepository.findAll(spec, pageable)
                    .stream()
                    .map(PostDTO::fromEntity)
                    .collect(Collectors.toList());

            long totalCount = postRepository.count(spec);
            return new PostListResponseDTO(posts, totalCount);
        }

        // pageCnt != 1 => 메인에 처음 로드될 때 세로높이에 따른 목록가져오기(세로높이가 길면 페이지를 여러 개 보여줌)
        List<PostDTO> allPosts = new ArrayList<>();

        for (int i = 0; i < pageCnt; i++) { // 0 ~ pageCnt 까지 반복
            Pageable pageable = PageRequest.of(i, PAGE_SIZE);
            List<PostDTO> posts = postRepository.findAll(spec, pageable)
                    .stream()
                    .map(PostDTO::fromEntity)
                    .collect(Collectors.toList());

            allPosts.addAll(posts);
        }

        long totalCount = postRepository.count(spec);
        return new PostListResponseDTO(allPosts, totalCount);
    }
}




