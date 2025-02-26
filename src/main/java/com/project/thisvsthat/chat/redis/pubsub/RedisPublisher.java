package com.project.thisvsthat.chat.redis.pubsub;

import com.project.thisvsthat.chat.dto.ChatMessage;
import com.project.thisvsthat.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisPublisher {
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final ChatMessageService chatMessageService;

    private static final int MAX_SIZE = 50; // Redis가 유지할 최대 메시지 개수
    private static final int BATCH_SIZE = 10; // 한 번에 DB로 보낼 개수
    private static final int DELETE_THRESHOLD = MAX_SIZE + BATCH_SIZE; // 60개 이상이면 정리 시작

    // 레디스에 메시지 저장
    public void sendMessage(ChatMessage message, String postId) {
        String chatRoomKey = "chatroom:" + postId;

        try {
            // 메시지를 Redis에 저장 (리스트의 오른쪽 끝에 추가)
            redisTemplate.opsForList().rightPush(chatRoomKey, message);

            // 메시지 발행
            redisTemplate.convertAndSend(chatRoomKey, message);

            // 레디스에서 메시지 수를 확인
            Long chatListSize = redisTemplate.opsForList().size(chatRoomKey);

            // 메시지 수가 DELETE_THRESHOLD 이상이고, BATCH_SIZE 간격으로 저장
            if (chatListSize != null && chatListSize >= DELETE_THRESHOLD && chatListSize % BATCH_SIZE == 0) {
                // Redis에서 마지막 10개의 메시지를 가져옴
                List<ChatMessage> messagesToSave = redisTemplate.opsForList().range(chatRoomKey, MAX_SIZE, chatListSize - 1);

                // 메시지가 존재하면 DB로 저장
                if (messagesToSave != null && !messagesToSave.isEmpty()) {
                    boolean isSaved = chatMessageService.saveMessagesToDB(messagesToSave, Long.parseLong(postId)); // DB 저장 성공 여부 확인

                    if (isSaved) {
                        // Redis에서 오래된 메시지 삭제
                        redisTemplate.opsForList().trim(chatRoomKey, 0, MAX_SIZE - 1);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Redis 메시지 저장 중 오류 발생: " + e.getMessage());
        }
    }
}
