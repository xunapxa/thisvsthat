package com.project.thisvsthat.common.dto;

import com.project.thisvsthat.common.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private Long roomId;
    private Long postId;
    private LocalDateTime createdAt;

    public static ChatRoomDTO fromEntity(ChatRoom chatRoom) {
        return new ChatRoomDTO(
                chatRoom.getRoomId(),
                chatRoom.getPost().getPostId(),
                chatRoom.getCreatedAt()
        );
    }
}