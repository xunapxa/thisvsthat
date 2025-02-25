package com.project.thisvsthat.chat.controller;

import com.project.thisvsthat.chat.service.ChatService;
import com.project.thisvsthat.chat.dto.ChatMessage;
import com.project.thisvsthat.common.dto.UserDTO;
import com.project.thisvsthat.image.service.S3Service;
import com.project.thisvsthat.post.dto.VotePercentageDTO;
import com.project.thisvsthat.post.service.VoteService;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequiredArgsConstructor
@RequestMapping("chat")
public class ChatController {

    private final ChatService chatService;
    private final S3Service s3Service;
    private final SimpMessagingTemplate messagingTemplate;
    private final VoteService voteService;

    // 채팅방 별 사용자 수를 관리할 Map
    private final Map<String, AtomicInteger> roomUserCount = new ConcurrentHashMap<>();

    // 닉네임 및 프로필 사진 조회 API
    @GetMapping("/get-profile/{userId}")
    public ResponseEntity<UserDTO> getProfileData(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(chatService.getProfileByUserId(userId));
    }

    // 채팅 프로필 이미지 업로드
    @PostMapping("/upload-profile-img")
    public ResponseEntity<?> handleBase64ImageUpload(@RequestBody String profileImage) {
        try {
            if (profileImage == null || profileImage.isEmpty()) {
                return ResponseEntity.badRequest().body("이미지가 없습니다.");
            }

            // Base64 이미지 업로드 및 URL 반환
            String imageUrl = s3Service.uploadBase64Image(profileImage);

            return ResponseEntity.ok().body(imageUrl);
        } catch (Exception e) {
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

        // 투표 현황 가져오기
        VotePercentageDTO voteResult = voteService.getVotePercentage(postId);

        model.addAttribute("postId", postId);
        model.addAttribute("userId", userId);
        model.addAttribute("title", title);
        model.addAttribute("voteResult", voteResult);
        model.addAttribute("previousMessages", previousMessages);

        // 채팅방 페이지로 이동
        return "chat/chat-room";
    }

    // 채팅방에 접속할 때마다 호출되는 메서드
    @MessageMapping("/join/{postId}")
    public void joinChat(@DestinationVariable("postId") String postId) {
        roomUserCount.putIfAbsent(postId, new AtomicInteger(0));  // 방이 처음이라면 초기화
        int currentCount = roomUserCount.get(postId).incrementAndGet();  // 인원 수 증가

        // JSON 라이브러리를 사용해 메시지 생성
        JSONObject userCountMessage = new JSONObject();
        userCountMessage.put("userCount", "현재 채팅 인원: " + currentCount);

        // 현재 인원 수를 채팅방에 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chatroom/userCount/" + postId, userCountMessage.toString());
    }

    // 채팅방에서 나갈 때 호출되는 메서드
    @MessageMapping("/leave/{postId}")
    public void leaveChat(@DestinationVariable("postId") String postId) {
        if (roomUserCount.containsKey(postId)) {
            int currentCount = roomUserCount.get(postId).decrementAndGet();  // 인원 수 감소
            if (currentCount > 0) {
                // JSON 라이브러리를 사용해 메시지 생성
                JSONObject userCountMessage = new JSONObject();
                userCountMessage.put("userCount", "현재 채팅 인원: " + currentCount);
                messagingTemplate.convertAndSend("/sub/chatroom/userCount/" + postId, userCountMessage.toString());
            } else {
                roomUserCount.remove(postId);  // 채팅방에 더 이상 인원이 없다면 방을 제거
            }
        }
    }

    // 메시지 발송
    @MessageMapping("/sendMessage/{postId}")
    @SendTo("/sub/chatroom/{postId}")
    public ChatMessage sendMessage(ChatMessage message, @DestinationVariable("postId") String postId) {
        System.out.println("클라이언트가 보낸 메시지: " + message);
        chatService.saveMessageToRedis(message, Long.parseLong(postId));  // 메시지를 레디스에 저장 및 DB로 이전 작업

        return message;  // 메시지 반환 (구독자에게 전달)
    }
}