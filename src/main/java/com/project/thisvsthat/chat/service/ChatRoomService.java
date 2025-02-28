package com.project.thisvsthat.chat.service;

import com.project.thisvsthat.common.entity.ChatLog;
import com.project.thisvsthat.common.entity.ChatRoom;
import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.repository.ChatLogRepository;
import com.project.thisvsthat.common.repository.ChatRoomRepository;
import com.project.thisvsthat.common.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatLogRepository chatLogRepository;

    // 게시글 제목 조회
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

    // 채팅방 통해 postId 조회
    public List<Long> getPostIdsForUser(Long userId) {
        //해당 사용자가 보낸 메시지 찾기 (데이터가 많을 때 대비해 페이징 처리)
        Page<ChatLog> chatLogsPage = chatLogRepository.findByUser_UserId(userId, PageRequest.of(0, 50));
        List<ChatLog> chatLogs = chatLogsPage.getContent(); // Page에서 List 추출

        //System.out.println("ChatRoomService++++++++++++++ chatLogs" + chatLogs);

        //메시지에서 roomId(chatRoom)를 통해 postId 추출 (null 값 방지)
        return chatLogs.stream()
                .filter(chatLog -> chatLog.getChatRoom() != null && chatLog.getChatRoom().getPost() != null) // Null 체크 추가
                .map(chatLog -> chatLog.getChatRoom().getPost().getPostId())
                .distinct()
                .collect(Collectors.toList());

    }
}
