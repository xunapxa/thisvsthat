package com.project.thisvsthat.common.dto;

import com.project.thisvsthat.common.entity.ChatLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatLogDTO {
    private Long messageId;
    private Long roomId;
    private Long userId;
    private String messageContent;
    private LocalDateTime createdAt;

    public static ChatLogDTO fromEntity(ChatLog chatLog) {
        return new ChatLogDTO(
                chatLog.getMessageId(),
                chatLog.getChatRoom().getRoomId(),
                chatLog.getUser().getUserId(),
                chatLog.getMessageContent(),
                chatLog.getCreatedAt()
        );
    }
}