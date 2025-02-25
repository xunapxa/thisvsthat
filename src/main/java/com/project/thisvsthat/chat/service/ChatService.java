package com.project.thisvsthat.chat.service;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatLogRepository chatLogRepository;
    private final ChatRoomRepository chatRoomRepository;


    public UserDTO getProfileByUserId(Long userId) {
        return UserDTO.fromEntity(userRepository.findByUserId(userId));
    }

    // Redis나 DB에서 이전 메시지 50개를 조회하는 메서드
    public List<ChatMessage> getPreviousMessages(Long chatRoomId) {
        String chatRoomKey = "chatroom:" + chatRoomId; // 채팅방 키

        // Redis에서 가장 최근 50개의 메시지 조회 (리스트에서 끝에서부터 50개)
        List<ChatMessage> messages = redisTemplate.opsForList().range(chatRoomKey, 0, 49);

        // 메시지를 ChatMessage 객체로 변환
        return messages;
    }

    public String getTitleByPostId(Long postId) {
        Optional<Post> post = postRepository.findById(postId);
        return post.orElseThrow(() -> new IllegalArgumentException("Post not found")).getTitle();
    }

    // 채팅방 조회 및 생성
    public ChatRoom getOrCreateChatRoom(String postId) {
        // 채팅방이 이미 존재하는지 확인
        Optional<ChatRoom> chatRoom = chatRoomRepository.findByPost_PostId(Long.parseLong(postId));

        // 채팅방이 없으면 새로 생성
        if (chatRoom.isEmpty()) {
            return createNewChatRoom(Long.parseLong(postId));  // 새로운 채팅방 생성 후 반환
        }

        return chatRoom.get();  // 기존 채팅방 반환
    }

    // DB에 메시지 저장
    public boolean saveMessagesToDB(List<ChatMessage> messages, Long postId) {
        try {
            for (ChatMessage message : messages) {
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
                        .messageContent(message.getContent())
                        .build();

                // 메시지 DB에 저장
                chatLogRepository.save(chatLog);
            }
            return true; // 성공 시 true 반환
        } catch (Exception e) {
            // DB 저장 실패 시 예외 발생
            System.err.println("❌ DB 저장 실패: " + e.getMessage());
            return false; // 실패 시 false 반환
        }
    }

    // DB에 채팅방 생성
    public ChatRoom createNewChatRoom(Long postId) {
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

    public Long getUserIdByToken(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 유저를 찾을 수 없습니다: " + email))
                .getUserId();
    }
}
