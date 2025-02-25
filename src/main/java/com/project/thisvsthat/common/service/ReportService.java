package com.project.thisvsthat.common.service;

import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.Report;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.UserStatus;
import com.project.thisvsthat.common.repository.PostRepository;
import com.project.thisvsthat.common.repository.ReportRepository;
import com.project.thisvsthat.common.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // 게시물 신고 처리
    public void reportPost(Long postId, Long userId) {
        // 이미 신고된 게시물인지 체크
        boolean isReported = reportRepository.existsByUser_UserIdAndPost_PostId(userId, postId);

        if (isReported) {
            // 중복 신고의 경우 예외 처리
            throw new IllegalArgumentException("이미 신고한 게시물입니다.");
        }

        // 게시물과 사용자 정보 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 신고 처리 로직
        Report report = Report.builder()
                .post(post)  // 게시물 정보 설정
                .user(user)  // 사용자 정보 설정
                .build();

        reportRepository.save(report);
    }

    // 신고 이력 생성
    public void createReport(Long postId, Long userId) {
        // 사용자가 이미 해당 게시물을 신고한 적이 있는지 확인 (중복 신고 방지)
        if (reportRepository.existsByUser_UserIdAndPost_PostId(userId, postId)) {
            throw new IllegalStateException("이미 신고한 게시물입니다.");
        }

        // 게시물과 사용자가 존재하는지 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 새로운 신고 생성
        Report report = Report.builder()
                .post(post)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        // 신고 저장
        reportRepository.save(report);

        // 게시물의 신고 횟수 증가
        post.setReportCount(post.getReportCount() + 1);
        postRepository.save(post);

        // 포스트 작성자의 상태를 REPORTED로 변경
        user.setUserStatus(UserStatus.REPORTED);
        userRepository.save(user);
    }
}


