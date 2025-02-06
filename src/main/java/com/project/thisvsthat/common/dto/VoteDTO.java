package com.project.thisvsthat.common.dto;

import com.project.thisvsthat.common.entity.Vote;
import com.project.thisvsthat.common.enums.SelectedOption;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteDTO {
    private Long voteId;
    private Long postId;
    private Long userId;
    private SelectedOption selectedOption;
    private LocalDateTime createdAt;

    public static VoteDTO fromEntity(Vote vote) {
        return new VoteDTO(
                vote.getVoteId(),
                vote.getPost().getPostId(),
                vote.getUser().getUserId(),
                vote.getSelectedOption(),
                vote.getCreatedAt()
        );
    }
}