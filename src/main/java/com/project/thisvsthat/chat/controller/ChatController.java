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
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<String> handleBase64ImageUpload(@RequestBody String profileImage) throws Exception {
        if (profileImage == null || profileImage.isEmpty()) {
            return ResponseEntity.badRequest().body("이미지가 없습니다.");
        }
        return ResponseEntity.ok(s3Service.uploadBase64Image(profileImage));
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

    // 메시지 스팸 필터 API
    @PostMapping("/spam-filter")
    public ResponseEntity<String> checkSpam(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        List<String> spamWords = spamFilterService.findSpamWords(content);

        // 스팸 단어가 없으면 "검증 완료" 반환
        if (spamWords.isEmpty()) {
            return ResponseEntity.ok("검증 완료");
        }

        // 스팸 단어가 포함된 경우 에러 메시지 반환
        String errorMessage = "🚫부적절한 단어가 포함되어 있습니다🚫\n[ '" + String.join("', '", spamWords) + "' ]\n확인 후 전송해주세요.";
        return ResponseEntity.ok(errorMessage);
    }

    // 채팅방에 접속할 때
    @MessageMapping("/join/{postId}")
    public void joinChat(@DestinationVariable("postId") String postId,
                         @Payload Map<String, Object> payload,
                         Principal principal) {
        if (!isAuthenticated(principal, postId)) return;

        String userId = String.valueOf(payload.get("userId"));
        chatUserService.userJoin(postId, userId);
        redisSubscriptionService.subscribeToChatRoom(postId, userId); // 레디스 채팅방 구독
    }

    // 채팅방에서 나갈 때
    @MessageMapping("/leave/{postId}")
    public void leaveChat(@DestinationVariable("postId") String postId,
                          @Payload Map<String, Object> payload,
                          Principal principal) {
        if (!isAuthenticated(principal, postId)) return;

        String userId = String.valueOf(payload.get("userId"));
        chatUserService.userLeave(postId, userId);
        redisSubscriptionService.unsubscribeFromChatRoom(postId, userId); // 레디스 채팅방 구독 해제
    }

    // 메시지 전송
    @MessageMapping("/sendMessage/{postId}")
    public void sendMessage(@DestinationVariable("postId") String postId, ChatMessage message, Principal principal) {
        if (!isAuthenticated(principal, postId)) return;

        // 채팅방 존재 여부 확인 및 생성 처리
        ChatRoom chatRoom = chatRoomService.getOrCreateChatRoom(postId);

        redisPublisher.saveAndPublishMessage(message, postId); // 레디스 저장 & 발행
        saveMessageToDBAsync(message); // DB에 저장
    }

    @Async
    public void saveMessageToDBAsync(ChatMessage message) {
        chatMessageService.saveMessageToDB(message);
    }
}