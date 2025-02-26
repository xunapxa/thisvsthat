package com.project.thisvsthat.chat.dto;

import lombok.Data;

@Data
public class ChatMessage {
    private Long userId;
    private Long postId;
    private String profileImage;
    private String nickname;
    private String selectedOption;
    private String content;
    private String sentAt;
}