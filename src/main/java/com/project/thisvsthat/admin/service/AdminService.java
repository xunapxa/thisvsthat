package com.project.thisvsthat.admin.service;

import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.PostStatus;
import com.project.thisvsthat.common.repository.PostRepository;
import com.project.thisvsthat.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

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

    // 신고된 유저 조회
    public List<User> getReportedUsers() {
        autoRestoreUsers(); // 자동 복구 실행
        return userRepository.findReportedUsers();
    }

    // 선택된 유저 복구
    @Transactional
    public void restoreUsers(List<Long> userIds) {
        userRepository.restoreUsers(userIds);
    }

    // 선택된 유저 차단
    @Transactional
    public void banUsers(List<Long> userIds) {
        userRepository.banUsers(userIds);
    }

    // 자동 복구
    @Transactional
    public void autoRestoreUsers() {
        userRepository.autoRestoreUsers();
    }

    // 특정 유저가 작성한 BLINDED, DELETED 게시글 조회
    public List<Post> getBlindedAndDeletedPosts(Long userId) {
        return postRepository.findBlindedAndDeletedPostsByUser(userId);
    }
}
