package com.project.thisvsthat.chat.controller;

import com.project.thisvsthat.chat.service.ChatMessageService;
import com.project.thisvsthat.chat.dto.ChatMessage;
import com.project.thisvsthat.chat.redis.pubsub.RedisSubscriptionService;
import com.project.thisvsthat.chat.service.ChatRoomService;
import com.project.thisvsthat.chat.service.ChatUserService;
import com.project.thisvsthat.chat.util.ChatUtils;
import com.project.thisvsthat.chat.redis.pubsub.RedisPublisher;
import com.project.thisvsthat.common.dto.UserDTO;
import com.project.thisvsthat.common.entity.ChatRoom;
import com.project.thisvsthat.common.service.SpamFilterService;
import com.project.thisvsthat.image.service.S3Service;
import com.project.thisvsthat.post.dto.VotePercentageDTO;
import com.project.thisvsthat.post.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("chat")
public class ChatController {
    private final ChatUserService chatUserService;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final SpamFilterService spamFilterService;
    private final RedisSubscriptionService redisSubscriptionService;
    private final RedisPublisher redisPublisher;
    private final S3Service s3Service;
    private final VoteService voteService;
    private final ChatUtils chatUtils;

    // 로그인 체크 메서드
    private boolean isAuthenticated(Principal principal, String postId) {
        if (principal == null) {
            chatUtils.sendErrorMessage(postId, "로그인 정보가 없습니다.\n로그인 후 이용해주세요.");
            return false;
        }
        return true;
    }

    // 닉네임 및 프로필 사진 조회 API
    @GetMapping("/get-profile/{userId}")
    public ResponseEntity<UserDTO> getProfileData(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(chatUserService.getProfileByUserId(userId));
    }

    // 채팅 프로필 이미지 업로드
    @PostMapping("/upload-profile-img")
    public ResponseEntity<?> handleBase64ImageUpload(@RequestBody String profileImage) {
        try {
            if (profileImage == null || profileImage.isEmpty()) {
                return ResponseEntity.badRequest().body("이미지가 없습니다.");
            }
            String imageUrl = s3Service.uploadBase64Image(profileImage);
            return ResponseEntity.ok().body(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("이미지 업로드 실패: " + e.getMessage());
        }
    }

    // 채팅방 입장 및 이전 메시지 조회
    @GetMapping("{postId}")
    public String chatRoom(@PathVariable("postId") Long postId, Model model, Principal principal) {
        List<ChatMessage> previousMessages = chatMessageService.getPreviousMessages(postId);
        Long userId = chatUserService.getUserIdByToken(principal.getName());
        String title = chatRoomService.getTitleByPostId(postId);
        VotePercentageDTO voteResult = voteService.getVotePercentage(postId);

        model.addAttribute("postId", postId);
        model.addAttribute("userId", userId);
        model.addAttribute("title", title);
        model.addAttribute("voteResult", voteResult);
        model.addAttribute("previousMessages", previousMessages);
        return "chat/chat-room";
    }

    // 채팅방에 접속할 때마다 호출되는 메서드
    @MessageMapping("/join/{postId}")
    public void joinChat(@DestinationVariable("postId") String postId, Principal principal) {
        if (!isAuthenticated(principal, postId)) return;

        chatUserService.userJoin(postId);
        redisSubscriptionService.subscribeToChatRoom(postId); // 레디스 채팅방 구독
    }

    // 채팅방에서 나갈 때 호출되는 메서드
    @MessageMapping("/leave/{postId}")
    public void leaveChat(@DestinationVariable("postId") String postId, Principal principal) {
        if (!isAuthenticated(principal, postId)) return;

        chatUserService.userLeave(postId);
        redisSubscriptionService.unsubscribeFromChatRoom(postId); // 레디스 채팅방 구독 해제
    }

    // 메시지 전송 메서드
    @MessageMapping("/sendMessage/{postId}")
    public void sendMessage(@DestinationVariable("postId") String postId, ChatMessage message, Principal principal) {
        if (!isAuthenticated(principal, postId)) return;

        // 스팸 필터링
        List<String> spamWords = spamFilterService.findSpamWords(message.getContent());
        if (!spamWords.isEmpty()) {
            String errorMessage = "부적절한 단어가 포함되어 있습니다\n[ '" + String.join("', '", spamWords) + "' ]\n다시 확인 후 전송해주세요.";
            chatUtils.sendErrorMessage(postId, errorMessage);
            return;
        }

        // 채팅방 존재 여부 확인 및 생성 처리
        ChatRoom chatRoom = chatRoomService.getOrCreateChatRoom(postId);

        // 레디스로 저장
        redisPublisher.sendMessage(message, postId);
    }
}