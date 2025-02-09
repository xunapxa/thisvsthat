package com.project.thisvsthat.common.repository;

import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // 게시물 관련 추가 쿼리 메서드가 필요하면 여기에 작성

    // 블라인드 상태 글 조회
    List<Post> findByPostStatus(PostStatus postStatus);

    // 신고글 수정 기능
    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.postStatus = 'ACTIVE', p.reportCount = 0 WHERE p.postId = :postId AND p.postStatus = 'BLINDED'")
    int restorePost(@Param("postId") Long postId);

    // 신고 글 삭제 기능(상태 변경)
    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.postStatus = 'DELETED' WHERE p.postId = :postId AND p.postStatus = 'BLINDED'")
    int deletePost(@Param("postId") Long postId);
}