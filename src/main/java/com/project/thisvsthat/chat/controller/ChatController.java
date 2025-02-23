package com.project.thisvsthat.chat.controller;

import com.project.thisvsthat.chat.service.ChatService;
import com.project.thisvsthat.chat.dto.ChatMessage;
import com.project.thisvsthat.common.dto.UserDTO;
import com.project.thisvsthat.image.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("chat")
public class ChatController {
    private final ChatService chatService;
    private final S3Service s3Service;

    // 닉네임 및 프로필 사진 조회 API
    @GetMapping("/get-profile/{userId}")
    public ResponseEntity<UserDTO> getProfileData(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(chatService.getProfileByUserId(userId));
    }

    // 채팅 프로필 이미지 업로드
    @PostMapping("/upload-profile-img")
    public ResponseEntity<?> handleBase64ImageUpload(@RequestParam("profileImage") String profileImage) {
        try {
            // Base64 이미지 업로드 및 URL 반환
            String imageUrl = s3Service.uploadBase64Image(profileImage);

            // URL을 반환
            return ResponseEntity.ok().body(imageUrl);
        } catch (Exception e) {
            // 오류 처리
            return ResponseEntity.status(500).body("이미지 업로드 실패: " + e.getMessage());
        }
    }

    // 채팅방 입장 및 이전 메시지 조회
    @GetMapping("{postId}")
    public String chatRoom(@PathVariable("postId") Long postId, Model model, Principal principal) {
        // Redis에서 이전 메시지 50개 조회
        List<ChatMessage> previousMessages = chatService.getPreviousMessages(postId);

        // 사용자 아이디 가져오기
        Long userId = chatService.getUserIdByToken(principal.getName());

        // 게시글 주제 가져오기
        String title = chatService.getTitleByPostId(postId);

        model.addAttribute("postId", postId);
        model.addAttribute("userId", userId);
        model.addAttribute("title", title);
        model.addAttribute("previousMessages", previousMessages);

        // 채팅방 페이지로 이동
        return "chat/chat-room";
    }

    @MessageMapping("/sendMessage/{roomId}")
    @SendTo("/sub/chatroom/{roomId}")
    public ChatMessage sendMessage(ChatMessage message, @DestinationVariable String roomId) {
        Long postId = Long.parseLong(roomId);  // roomId를 postId로 변환 (혹은 다른 방식으로 매핑)
        chatService.saveMessageToRedis(message, postId);  // 메시지를 레디스에 저장 및 DB로 이전 작업

        return message;  // 메시지 반환 (구독자에게 전달)
    }
}
