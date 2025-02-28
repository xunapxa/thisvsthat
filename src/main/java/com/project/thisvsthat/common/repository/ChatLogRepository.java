package com.project.thisvsthat.common.repository;

import com.project.thisvsthat.common.entity.ChatLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    // 채팅 로그 관련 추가 쿼리 메서드가 필요하면 여기에 작성

    //사용자 id로 채팅 로그 조회
    Page<ChatLog> findByUser_UserId(Long userId, Pageable pageable);
}