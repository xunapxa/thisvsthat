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

    // 신고 글 복구
    @Transactional
    public void restorePost(Long postId) {
        postRepository.restorePost(postId);
    }

    // 신고 글 삭제
    @Transactional
    public void deletePost(Long postId) {
        postRepository.deletePost(postId);
    }
}
