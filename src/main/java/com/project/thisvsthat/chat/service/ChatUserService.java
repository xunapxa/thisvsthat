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
        roomUserCount.putIfAbsent(postId, new AtomicInteger(0));  // 방이 처음이라면 초기화
        int currentCount = roomUserCount.get(postId).incrementAndGet();  // 인원 수 증가

        broadcastUserCount(postId, currentCount);
    }

    // 채팅방 퇴장 처리
    public void userLeave(String postId) {
        if (roomUserCount.containsKey(postId)) {
            int currentCount = roomUserCount.get(postId).decrementAndGet();  // 인원 수 감소
            if (currentCount > 0) {
                broadcastUserCount(postId, currentCount);
            } else {
                roomUserCount.remove(postId);  // 채팅방에 더 이상 인원이 없다면 방 제거
            }
        }
    }

    // 실시간 인원수
    private void broadcastUserCount(String postId, int currentCount) {
        JSONObject userCountMessage = new JSONObject();
        userCountMessage.put("userCount", "현재 채팅 인원: " + currentCount);
        messagingTemplate.convertAndSend("/sub/chatroom/user-count/" + postId, userCountMessage.toString());
    }
}
