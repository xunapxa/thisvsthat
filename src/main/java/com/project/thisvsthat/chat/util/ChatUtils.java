package com.project.thisvsthat.chat.util;

import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatUtils {
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅 오류 메시지 전송
    public void sendErrorMessage(String postId, String errorMessage) {
        JSONObject errorMessageJson = new JSONObject();
        errorMessageJson.put("error", errorMessage);
        messagingTemplate.convertAndSend("/sub/chatroom/" + postId, errorMessageJson.toString());
    }
}