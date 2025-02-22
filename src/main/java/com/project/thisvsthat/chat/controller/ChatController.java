package com.project.thisvsthat.chat.controller;

import com.project.thisvsthat.chat.service.ChatService;
import com.project.thisvsthat.chat.dto.ChatMessage;
import com.project.thisvsthat.common.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("chat")
public class ChatController {
    private final ChatService chatService;

    // 닉네임 및 프로필 사진 조회 API
    @GetMapping("/get-profile/{userId}")
    public ResponseEntity<UserDTO> getProfileData(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(chatService.getProfileByUserId(userId));
    }

//    // 채팅방 입장 및 이전 메시지 조회
//    @GetMapping("{post_id}")
//    public String chatRoom(@RequestParam String chatRoomId, Model model) {
//        // Redis에서 이전 메시지 50개 조회
//        List<ChatMessage> previousMessages = chatService.getPreviousMessages(chatRoomId);
//
//        model.addAttribute("chatRoomId", chatRoomId);
//        model.addAttribute("previousMessages", previousMessages);
////        model.addAttribute("voteResult", previousMessages);
//
//        // 채팅방 페이지로 이동
//        return "chat/chat-room";
//    }
}
