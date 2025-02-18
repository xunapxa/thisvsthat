package com.project.thisvsthat.common.repository;

import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    // 게시물 관련 추가 쿼리 메서드가 필요하면 여기에 작성

    // 블라인드 상태 글 조회
    List<Post> findByPostStatus(PostStatus postStatus);

    // 여러 개의 게시글을 ACTIVE 상태로 변경 (복구)
    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.postStatus = 'ACTIVE', p.reportCount = 0 WHERE p.postId IN :postIds AND p.postStatus = 'BLINDED'")
    int restoreMultiplePosts(@Param("postIds") List<Long> postIds);

    // 여러 개의 게시글을 DELETED 상태로 변경 (삭제)
    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.postStatus = 'DELETED' WHERE p.postId IN :postIds AND p.postStatus = 'BLINDED'")
    int deleteMultiplePosts(@Param("postIds") List<Long> postIds);


    // 특정 유저가 쓴 BLINDED 또는 DELETED 상태의 게시글 조회
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.postStatus IN ('BLINDED', 'DELETED')")
    List<Post> findBlindedAndDeletedPostsByUser(@Param("userId") Long userId);

}