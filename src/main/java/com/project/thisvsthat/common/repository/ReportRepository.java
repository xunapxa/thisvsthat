package com.project.thisvsthat.common.repository;

import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.Report;
import com.project.thisvsthat.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // 특정 사용자가 특정 게시물을 신고한 적이 있는지 확인하는 메서드
    boolean existsByUser_UserIdAndPost_PostId(Long userId, Long postId);

    // 특정 게시물에 대한 모든 신고를 가져오는 메서드 (admin에서 했으면 필요없는 메서드!)
    List<Report> findByPost_PostId(Long postId);
}