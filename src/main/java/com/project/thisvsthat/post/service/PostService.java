package com.project.thisvsthat.post.service;

import com.project.thisvsthat.common.dto.PostDTO;
import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.Report;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.Category;
import com.project.thisvsthat.common.enums.PostStatus;
import com.project.thisvsthat.common.enums.UserStatus;
import com.project.thisvsthat.common.enums.VoteStatus;
import com.project.thisvsthat.common.repository.PostRepository;
import com.project.thisvsthat.common.repository.ReportRepository;
import com.project.thisvsthat.common.repository.UserRepository;
import com.project.thisvsthat.common.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ReportService reportService;

    public void savePost(Long userId, PostDTO dto) {
        if (dto.getCategory() == null) {
            dto.setCategory(Category.자유);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. ID: " + userId));
        Post post = PostDTO.fromDto(dto);
        post.setCreatedAt(LocalDateTime.now());
        post.setUser(user);
        user.getPosts().add(post);

        System.out.println("저장 전 post 결과 (유저아이디, 제목, 카테고리, 해시태그) ========== " +
                post.getUser().getUserId() + post.getTitle() + post.getCategory() + post.getHashtags());

        postRepository.save(post);
    }

    public PostDTO findOnePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. ID: " + postId));
        return PostDTO.fromEntity(post);
    }

    public void updatePost(Long postId, PostDTO dto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. ID: " + postId));

        post.setUpdatedAt(LocalDateTime.now());
        post.setCategory(dto.getCategory());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setHashtags(dto.getHashtags());
        post.setOption1(dto.getOption1());
        post.setOption2(dto.getOption2());
        post.setOption1ImageUrl(dto.getOption1ImageUrl());
        post.setOption2ImageUrl(dto.getOption2ImageUrl());

        System.out.println("updatePost 후 post 결과 (제목, 카테고리) " + post.getTitle() + post.getCategory());
        postRepository.save(post);
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. ID: " + postId));
        post.setPostStatus(PostStatus.DELETED);
        post.setVoteStatus(VoteStatus.FINISHED);
        postRepository.save(post);
    }

    // 신고 기능 추가
    public void reportPost(Long postId, Long userId) {
        // ReportService의 신고 처리 메서드 호출
        reportService.createReport(postId, userId);

        // 신고 후 게시물 상태 변경
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. ID: " + postId));

        // 신고수가 3이상일 때 포스트 상태를 "Blinded"로 변경
        if (post.getReportCount() >= 3) {
            post.setPostStatus(PostStatus.BLINDED);
        }

        // 업데이트된 게시물 저장
        postRepository.save(post);
    }

}
