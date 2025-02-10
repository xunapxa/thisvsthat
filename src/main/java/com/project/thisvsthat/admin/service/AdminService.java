package com.project.thisvsthat.admin.service;

import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.PostStatus;
import com.project.thisvsthat.common.repository.PostRepository;
import com.project.thisvsthat.common.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public AdminService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // 신고 유저 조회
    public List<User> getReportUsers() {
        return userRepository.findUsersWithBlindedPosts();
    }

    // 신고 글 조회
    public List<Post> getBlindedPosts() {
        return postRepository.findByPostStatus(PostStatus.BLINDED);
    }

    // 선택된 게시글 일괄 복구
    @Transactional
    public void restorePosts(List<Long> postIds) {
        postRepository.restoreMultiplePosts(postIds);
    }

    // 선택된 게시글 일괄 삭제
    @Transactional
    public void deletePosts(List<Long> postIds) {
        postRepository.deleteMultiplePosts(postIds);
    }
}
