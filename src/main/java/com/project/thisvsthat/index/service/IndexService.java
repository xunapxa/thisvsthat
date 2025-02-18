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
import java.util.List;
import java.util.stream.Collectors;
@Service
public class IndexService {

    @Autowired
    private PostRepository postRepository;

    private static final int PAGE_SIZE = 3;  // 한 번에 보여줄 게시물 개수

    // 메인페이지(검색조건에 따른 목록 출력)
    public PostListResponseDTO getFilteredPosts(int page, String searchBy, String keyword, String listCategory, String listDesc, String voteStatus, String startDate, String endDate) {
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

        Pageable pageable = PageRequest.of(page, PAGE_SIZE); // 정렬 기준이 Specification 내부로 이동

        // 조건에 맞는 전체 게시글 개수 가져오기
        long totalCount = postRepository.count(spec);

        // 현재 페이지의 게시글 리스트 가져오기
        List<PostDTO> posts = postRepository.findAll(spec, pageable)
                .stream()
                .map(PostDTO::fromEntity)
                .collect(Collectors.toList());

        return new PostListResponseDTO(posts, totalCount);
    }
}



