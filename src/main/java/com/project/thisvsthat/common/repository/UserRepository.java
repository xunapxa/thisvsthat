package com.project.thisvsthat.common.repository;

import com.project.thisvsthat.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 사용자 관련 추가 쿼리 메서드가 필요하면 여기에 작성

    // 신고된 유저 조회 (신고된 글이 존재하는 유저만)
    @Query("SELECT u FROM User u WHERE u.userStatus = 'REPORTED'")
    List<User> findReportedUsers();

    // 유저 복구 (REPORTED 상태를 ACTIVE 상태로 변경)
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.userStatus = 'ACTIVE' WHERE u.userStatus = 'REPORTED' AND u.id IN (:userIds)")
    int restoreUsers(@Param("userIds") List<Long> userIds);

    // 유저 차단 (REPORTED 상태를 BANNED 상태로 변경)
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.userStatus = 'BANNED' WHERE u.id IN (:userIds)")
    int banUsers(@Param("userIds") List<Long> userIds);

    // 자동 복구
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.userStatus = 'ACTIVE' " +
            "WHERE u.userStatus = 'REPORTED' " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM Post p " +
            "    WHERE p.user = u " +
            "    AND (p.postStatus = 'BLINDED' OR p.postStatus = 'DELETED') " +
            ")")
    int autoRestoreUsers();

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);

    User findByUserId(Long userId);
}