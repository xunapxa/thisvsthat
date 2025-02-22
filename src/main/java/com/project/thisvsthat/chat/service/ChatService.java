package com.project.thisvsthat.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.thisvsthat.chat.dto.ChatMessage;
import com.project.thisvsthat.common.dto.UserDTO;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public UserDTO getProfileByUserId(Long userId) {
        return UserDTO.fromEntity(userRepository.findByUserId(userId));
    }

    // Redis나 DB에서 이전 메시지 50개를 조회하는 메서드
    public List<ChatMessage> getPreviousMessages(String chatRoomId) {
        String chatRoomKey = "chatroom:" + chatRoomId; // 채팅방 키

        // Redis에서 가장 최근 50개의 메시지 조회 (리스트에서 끝에서부터 50개)
        List<Object> messages = redisTemplate.opsForList().range(chatRoomKey, 0, 49);

        // 메시지를 ChatMessage 객체로 변환
        return messages.stream()
                .map(message -> {
                    try {
                        return objectMapper.readValue(message.toString(), ChatMessage.class);
                    } catch (Exception e) {
                        throw new RuntimeException("이전 메시지 조회 오류", e);
                    }
                })
                .collect(Collectors.toList());
    }
}
