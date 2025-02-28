package com.project.thisvsthat.chat.service;

import com.project.thisvsthat.common.dto.UserDTO;
import com.project.thisvsthat.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatUserService {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final MultiValueMap<String, String> chatRoomUsers = new LinkedMultiValueMap<>();

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
    public void userJoin(String postId, String userId) {
        chatRoomUsers.compute(postId, (key, users) -> {
            if (users == null) {
                users = Collections.synchronizedList(new ArrayList<>()); // 동기화된 리스트 생성
            }
            if (!users.contains(userId)) { // 중복 방지
                users.add(userId);
            }

            int currentCount = users.size();
            log.info("👥 [UPDATE] 채팅방 입장: postId={}, userId={}, 현재 인원={}", postId, userId, currentCount);

            // 인원수 발송
            broadcastUserCount(postId, currentCount);
            return users;
        });
    }

    // 채팅방 퇴장 처리
    public void userLeave(String postId, String userId) {
        chatRoomUsers.computeIfPresent(postId, (key, users) -> {
            if (users.remove(userId)) { // 유저 제거
                int currentCount = users.size();
                log.info("👥 [UPDATE] 채팅방 퇴장: postId={}, userId={}, 남은 인원={}", postId, userId, currentCount);

                if (currentCount > 0) {
                    broadcastUserCount(postId, currentCount);
                    return users;
                } else {
                    return null; // 인원이 0이면 채팅방 삭제
                }
            }
            log.warn("⚠️ [WARN] 퇴장 처리 실패: postId={}, userId={} (이미 퇴장한 유저 또는 존재하지 않음)", postId, userId);
            return users;
        });
    }

    // 실시간 인원수 발행
    private void broadcastUserCount(String postId, int currentCount) {
        JSONObject userCountMessage = new JSONObject();
        userCountMessage.put("userCount", currentCount);
        log.info("📢 [SEND] 인원 수 발행: {} -> {}", postId, userCountMessage);
        messagingTemplate.convertAndSend("/sub/chatroom/user-count/" + postId, userCountMessage.toString());
    }
}