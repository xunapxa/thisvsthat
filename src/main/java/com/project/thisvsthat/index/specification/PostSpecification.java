package com.project.thisvsthat.index.specification;

import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.enums.Category;
import com.project.thisvsthat.common.enums.PostStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 메안 검색조건에 따른 동적쿼리를 만들기위한 스펙
public class PostSpecification {

    // 게시물상태 (ACTIVE인 것을 기준) 필터링
    public static Specification<Post> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("postStatus"), PostStatus.ACTIVE);
    }

    // 제목, 내용, 해시태그 중 포함되어있는 검색어 필터링
    public static Specification<Post> containsKeyword(String searchBy, String keyword) {
        return (root, query, criteriaBuilder) -> {
            if ("title".equals(searchBy)) {
                return criteriaBuilder.like(root.get("title"), "%" + keyword + "%");
            } else if ("content".equals(searchBy)) {
                return criteriaBuilder.like(root.get("content"), "%" + keyword + "%");
            } else if ("hashtags".equals(searchBy)) {
                return criteriaBuilder.like(root.get("hashtags"), "%" + keyword + "%");
            }
            return null;
        };
    }

    // 카테고리(전체,고민,토론,자유) 필터링
    public static Specification<Post> hasCategory(String category) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category"), Category.valueOf(category));
    }

    // 인기순 정렬 (option1Count + option2Count 기준) 필터링
    public static Specification<Post> orderByPopularity() {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(
                    criteriaBuilder.sum(root.get("option1Count"), root.get("option2Count"))
            ));
            return null;
        };
    }

    // 투표 상태 필터링
    public static Specification<Post> hasVoteStatus(String voteStatus) {
        return (root, query, criteriaBuilder) -> {
            if ("ACTIVE".equalsIgnoreCase(voteStatus)) {
                return criteriaBuilder.equal(root.get("voteStatus"), "ACTIVE");
            } else if ("FINISHED".equalsIgnoreCase(voteStatus)) {
                return criteriaBuilder.equal(root.get("voteStatus"), "FINISHED");
            }
            return criteriaBuilder.conjunction();
        };
    }

    // 특정 기간이 startDate ~ endDate 사이에 있는 게시물만 필터링(단, 정렬 기준이 인기순)
    public static Specification<Post> isCreatedBetween(String startDate, String endDate) {
        return (root, query, criteriaBuilder) -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDateTime start = LocalDate.parse(startDate, formatter).atStartOfDay(); // 00:00:00
                LocalDateTime end = LocalDate.parse(endDate, formatter).atTime(23, 59, 59); // 23:59:59

                return criteriaBuilder.between(root.get("createdAt"), start, end);
            } catch (Exception e) {
                return criteriaBuilder.conjunction(); // 날짜 변환 실패 시 필터링 적용 안 함
            }
        };
    }

}


