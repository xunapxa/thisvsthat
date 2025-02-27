package com.project.thisvsthat.chat.redis.pubsub;

import com.project.thisvsthat.chat.dto.ChatMessage;
import com.project.thisvsthat.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisher {
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final ChatMessageService chatMessageService;

    private static final int MAX_SIZE = 50; // Redis가 유지할 최대 메시지 개수

    // 레디스에 메시지 저장 & 발행
    public void saveAndPublishMessage(ChatMessage message, String postId) {
        String chatRoomKey = "chatroom:" + postId;

        try {
            // 메시지를 Redis에 저장 (리스트의 오른쪽 끝에 추가)
            redisTemplate.opsForList().rightPush(chatRoomKey, message);

            // 메시지 발행
            redisTemplate.convertAndSend(chatRoomKey, message);

            // 레디스에서 메시지 수를 확인
            Long chatListSize = redisTemplate.opsForList().size(chatRoomKey);

            // 메시지 수가 MAX_SIZE 이상이면 오래된 메시지 삭제
            if (chatListSize != null && chatListSize > MAX_SIZE) {
                redisTemplate.opsForList().leftPop(chatRoomKey);
            }
        } catch (Exception e) {
            log.error("🚨 [ERROR] Redis 메시지 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
