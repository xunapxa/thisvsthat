package com.project.thisvsthat.common.repository;

import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    // 투표 관련 추가 쿼리 메서드가 필요하면 여기에 작성
    @Query("SELECT " +
            "SUM(CASE WHEN v.selectedOption = 'OPTION_1' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN v.selectedOption = 'OPTION_2' THEN 1 ELSE 0 END) " +
            "FROM Vote v WHERE v.post.id = :postId")
    Object[] countVotesByPost(@Param("postId") Long postId);

    @Query("SELECT v.post FROM Vote v WHERE v.user.userId = :userId")
    List<Post> findVotedPostsByUserId(@Param("userId") Long userId);

    @Query("SELECT v.selectedOption FROM Vote v WHERE v.user.userId = :userId AND v.post.postId = :postId")
    String findUserVoteForPost(@Param("userId") Long userId, @Param("postId") Long postId);

    @Query("SELECT COUNT(v) > 0 FROM Vote v WHERE v.user.userId = :userId AND v.post.postId = :postId")
    boolean existsByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Query("SELECT v.voteId FROM Vote v WHERE v.user.userId = :userId AND v.post.postId = :postId")
    Long findVoteIdByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
}