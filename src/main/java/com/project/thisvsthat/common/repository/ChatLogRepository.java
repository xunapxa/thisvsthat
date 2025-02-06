package com.project.thisvsthat.common.repository;

import com.project.thisvsthat.common.entity.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    // 채팅 로그 관련 추가 쿼리 메서드가 필요하면 여기에 작성
}