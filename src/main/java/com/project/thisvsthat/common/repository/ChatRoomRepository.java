package com.project.thisvsthat.common.repository;

import com.project.thisvsthat.common.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 채팅방 관련 추가 쿼리 메서드가 필요하면 여기에 작성
    Optional<ChatRoom> findByPost_PostId(Long postId);
}