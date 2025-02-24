package com.project.thisvsthat.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.thisvsthat.chat.dto.ChatMessage;
import com.project.thisvsthat.common.dto.UserDTO;
import com.project.thisvsthat.common.entity.ChatLog;
import com.project.thisvsthat.common.entity.ChatRoom;
import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.repository.ChatLogRepository;
import com.project.thisvsthat.common.repository.ChatRoomRepository;
import com.project.thisvsthat.common.repository.PostRepository;
import com.project.thisvsthat.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatLogRepository chatLogRepository;
    private final ChatRoomRepository chatRoomRepository;

    private static final String MESSAGE_KEY_PREFIX = "chatroom:";

    public UserDTO getProfileByUserId(Long userId) {
        return UserDTO.fromEntity(userRepository.findByUserId(userId));
    }

    // Redis나 DB에서 이전 메시지 50개를 조회하는 메서드
    public List<ChatMessage> getPreviousMessages(Long chatRoomId) {
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

    public String getTitleByPostId(Long postId) {
        Optional<Post> post = postRepository.findById(postId);
        return post.orElseThrow(() -> new IllegalArgumentException("Post not found")).getTitle();
    }

    // 전송된 메시지 레디스에 저장
    public void saveMessageToRedis(ChatMessage message, Long postId) {
        String key = MESSAGE_KEY_PREFIX + postId;

        // 채팅방이 존재하는지 확인하고, 없으면 새로 생성
        ChatRoom chatRoom = chatRoomRepository.findByPost_PostId(postId)
                .orElseGet(() -> createNewChatRoom(postId)); // 채팅방이 없으면 새로 생성

        // 메시지를 Redis에 추가
        try {
            redisTemplate.opsForList().leftPush(key, message);

            // 레디스에서 메시지 수를 확인하고, 60개가 되면 DB로 저장
            Long size = redisTemplate.opsForList().size(key);
            if (size == 60) { // 60개가 되면 DB에 저장
                List<Object> oldMessages = redisTemplate.opsForList().range(key, 50, size - 1);
                List<ChatMessage> chatMessages = convertToChatMessages(oldMessages);
                chatMessages.forEach(msg -> saveMessageToDB(msg, postId));  // DB로 저장

                // 오래된 메시지 10개 삭제
                redisTemplate.opsForList().trim(key, 0, 49);  // Redis 리스트를 50개로 유지
            }
        } catch (Exception e) {
            // 예외 처리
            log.error("Error saving message to Redis for postId: {}", postId, e);
            throw new RuntimeException("레디스 메시지 저장 중 오류 발생", e);
        }
    }

    // 레디스에서 조회한 메시지를 ChatMessage로 변환
    private List<ChatMessage> convertToChatMessages(List<Object> oldMessages) {
        return oldMessages.stream()
                .map(obj -> objectMapper.convertValue(obj, ChatMessage.class))
                .collect(Collectors.toList());
    }

    // DB에 메시지 저장
    private void saveMessageToDB(ChatMessage message, Long postId) {
        // User 객체 가져오기
        User user = userRepository.findById(message.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ChatRoom 객체 가져오기
        ChatRoom chatRoom = chatRoomRepository.findByPost_PostId(postId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found"));

        // ChatLog 엔티티 설정
        ChatLog chatLog = ChatLog.builder()
                .chatRoom(chatRoom)
                .user(user)
                .messageContent(message.getMessage())
                .build();

        // 메시지 DB에 저장
        chatLogRepository.save(chatLog);
    }

    // DB에 채팅방 생성
    private ChatRoom createNewChatRoom(Long postId) {
        // ChatRoom 생성 로직
        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new IllegalArgumentException("Post not found");
        }

        ChatRoom newChatRoom = new ChatRoom();
        newChatRoom.setPost(post.get());
        chatRoomRepository.save(newChatRoom); // 새 채팅방을 DB에 저장
        return newChatRoom;
    }

    // 게시글 삭제시 레디스에 남은 메시지 DB로 저장
    public void handlePostDeletion(Long postId) {
        // 레디스에서 채팅방 메시지 조회
        String key = MESSAGE_KEY_PREFIX + postId;
        Long size = redisTemplate.opsForList().size(key);

        // 레디스에 메시지가 남아있다면 DB로 저장하고 삭제
        if (size > 0) {
            List<Object> messages = redisTemplate.opsForList().range(key, 0, size - 1);
            List<ChatMessage> chatMessages = convertToChatMessages(messages);
            chatMessages.forEach(msg -> saveMessageToDB(msg, postId));  // DB로 저장

            // 레디스에서 메시지 삭제
            redisTemplate.delete(key);
        }
    }

    public Long getUserIdByToken(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 유저를 찾을 수 없습니다: " + email))
                .getUserId();
    }
}
