package com.project.thisvsthat.chat.dto;

import lombok.Data;

@Data
public class ChatMessage {
    private Long userId;
    private Long chatRoomId;
    private String nickname;
    private String profileImage;
    private String message;
    private String sentTime;
    private String selectedOption;
}
