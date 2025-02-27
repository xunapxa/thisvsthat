package com.project.thisvsthat.chat.service;

import com.project.thisvsthat.chat.dto.ChatMessage;
import com.project.thisvsthat.common.entity.ChatLog;
import com.project.thisvsthat.common.entity.ChatRoom;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.repository.ChatLogRepository;
import com.project.thisvsthat.common.repository.ChatRoomRepository;
import com.project.thisvsthat.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final UserRepository userRepository;
    private final ChatLogRepository chatLogRepository;
    private final ChatRoomRepository chatRoomRepository;

    // 이전 메시지를 조회하는 메서드
    public List<ChatMessage> getPreviousMessages(Long chatRoomId) {
        String chatRoomKey = "chatroom:" + chatRoomId; // 채팅방 키

        // Redis에서 메시지 조회
        List<ChatMessage> messages = redisTemplate.opsForList().range(chatRoomKey, 0, -1);

        // 메시지를 ChatMessage 객체로 변환
        return messages;
    }

    // DB에 메시지 저장
    public boolean saveMessageToDB(ChatMessage message) {
        try {
            // User 객체 가져오기
            User user = userRepository.findById(message.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // ChatRoom 객체 가져오기
            ChatRoom chatRoom = chatRoomRepository.findByPost_PostId(message.getPostId())
                    .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found"));

            // ChatLog 엔티티 설정
            ChatLog chatLog = ChatLog.builder()
                    .chatRoom(chatRoom)
                    .user(user)
                    .createdAt(toLocalDateTime(message.getSentAt()))
                    .messageContent(message.getContent())
                    .build();

            // 메시지 DB에 저장
            ChatLog save = chatLogRepository.save(chatLog);
            return true; // 성공 시 true 반환
        } catch (Exception e) {
            // DB 저장 실패 시 예외 발생
            log.error("🚨 [ERROR] 메시지 DB 저장 실패: {}", e.getMessage(), e);
            return false; // 실패 시 false 반환
        }
    }

    private LocalDateTime toLocalDateTime(String dateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTime, formatter);
    }
}