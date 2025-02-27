package com.project.thisvsthat.chat.redis.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.thisvsthat.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Redis에서 받은 메시지(JSON)를 ChatMessage 객체로 변환
            String jsonMessage = new String(message.getBody());
            ChatMessage chatMessage = objectMapper.readValue(jsonMessage, ChatMessage.class);

            log.info("📨 [SUCCESS] 레디스에서 메시지 전송 처리: ID({}) '{}'", chatMessage.getUserId(), chatMessage.getContent());
            // 웹소켓을 통해 클라이언트에게 메시지 전송
            messagingTemplate.convertAndSend("/sub/chatroom/" + chatMessage.getPostId(), chatMessage);
        } catch (Exception e) {
            log.error("🚨 [ERROR] 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
