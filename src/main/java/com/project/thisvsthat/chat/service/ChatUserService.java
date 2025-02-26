package com.project.thisvsthat.chat.service;

import com.project.thisvsthat.common.dto.UserDTO;
import com.project.thisvsthat.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChatUserService {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final Map<String, AtomicInteger> roomUserCount = new ConcurrentHashMap<>(); // 채팅방 별 사용자 수를 관리할 Map

    // 사용자 프로필 조회
    public UserDTO getProfileByUserId(Long userId) {
        return UserDTO.fromEntity(userRepository.findByUserId(userId));
    }

    // 토큰에서 유저 ID 조회
    public Long getUserIdByToken(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 유저를 찾을 수 없습니다: " + email))
                .getUserId();
    }

    // 채팅방 입장 처리
    public void userJoin(String postId) {
        roomUserCount.computeIfAbsent(postId, key -> new AtomicInteger(0)).incrementAndGet();
        int currentCount = roomUserCount.get(postId).get();  // 증가된 인원 확인

        broadcastUserCount(postId, currentCount);
    }

    // 채팅방 퇴장 처리
    public void userLeave(String postId) {
        roomUserCount.computeIfPresent(postId, (key, count) -> {
            int currentCount = count.decrementAndGet();
            if (currentCount > 0) {
                broadcastUserCount(postId, currentCount);
                return count;
            } else {
                return null; // 인원 0명이면 방 제거
            }
        });
    }

    // 실시간 인원수 발행
    private void broadcastUserCount(String postId, int currentCount) {
        JSONObject userCountMessage = new JSONObject();
        userCountMessage.put("userCount", "현재 채팅 인원: " + currentCount);
        messagingTemplate.convertAndSend("/sub/chatroom/user-count/" + postId, userCountMessage.toString());
    }
}
