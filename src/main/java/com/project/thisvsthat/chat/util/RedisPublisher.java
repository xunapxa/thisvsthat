package com.project.thisvsthat.chat.util;

import com.project.thisvsthat.chat.dto.ChatMessage;
import com.project.thisvsthat.common.dto.ChatLogDTO;
import com.project.thisvsthat.common.entity.ChatLog;
import com.project.thisvsthat.common.entity.ChatRoom;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.repository.ChatLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisPublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatLogRepository chatLogRepository;

    private static final int MAX_SIZE = 50; // Redis가 유지할 최대 메시지 개수
    private static final int BATCH_SIZE = 10; // 한 번에 DB로 보낼 개수
    private static final int DELETE_THRESHOLD = MAX_SIZE + BATCH_SIZE; // 60개 이상이면 정리 시작
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 채팅 메시지를 Redis에 저장하고, 필요하면 DB로 이동
    public void sendMessage(ChatMessage message) {
        String chatRoomKey = "chatroom:" + message.getPostId(); // 채팅방별 키 생성

        // Redis List에 메시지 추가 (채팅방별 관리)
        redisTemplate.opsForList().leftPush(chatRoomKey, message);

        // 채팅 채널에 메시지 발행 (채팅방 ID에 맞는 채널)
        redisTemplate.convertAndSend(chatRoomKey, message);

        // 현재 채팅방의 Redis 리스트 크기 확인
        Long chatListSize = redisTemplate.opsForList().size(chatRoomKey);

        // 길이가 60개 이상이고, 10으로 나눴을 때 0이면 실행
        if (chatListSize != null && chatListSize >= DELETE_THRESHOLD && chatListSize % BATCH_SIZE == 0) {
            // 가장 오래된 10개 가져오기 (오른쪽에서 10개)
            List<Object> messagesToSave = redisTemplate.opsForList().range(chatRoomKey, MAX_SIZE, MAX_SIZE + BATCH_SIZE - 1);

            if (messagesToSave != null && !messagesToSave.isEmpty()) {
                saveChatsToDatabase(messagesToSave); // DB 저장
                redisTemplate.opsForList().trim(chatRoomKey, BATCH_SIZE, -1); // 가장 오래된 10개 삭제
            }
        }
    }

    private void saveChatsToDatabase(List<Object> msgList) {
        List<ChatLog> chatList = msgList.stream()
                .filter(msg -> msg instanceof ChatMessage) // ChatMessage 타입인지 확인
                .map(msg -> chatMessageToEntity((ChatMessage) msg)) // ChatLog로 변환
                .collect(Collectors.toList());

        chatLogRepository.saveAll(chatList); // 리스트를 한 번에 DB 저장
    }

    private ChatLog chatMessageToEntity(ChatMessage message) {
        return ChatLog.builder()
                .chatRoom(ChatRoom.builder().roomId(message.getPostId()).build())
                .user(User.builder().userId(message.getUserId()).build())
                .messageContent(message.getContent())
                .createdAt(parseSentTime(message.getSentTime()))
                .build();
    }

    // String -> LocalDateTime 변환
    private LocalDateTime parseSentTime(String sentTime) {
        return LocalDateTime.parse(sentTime, FORMATTER);
    }

}
