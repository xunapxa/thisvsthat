package com.project.thisvsthat.common.repository;

import com.project.thisvsthat.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 사용자 관련 추가 쿼리 메서드가 필요하면 여기에 작성

    @Query("SELECT DISTINCT p.user FROM Post p WHERE p.postStatus = 'BLINDED'")
    List<User> findUsersWithBlindedPosts();

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);

}