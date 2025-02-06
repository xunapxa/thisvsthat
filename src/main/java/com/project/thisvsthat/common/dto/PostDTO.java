package com.project.thisvsthat.common.dto;

import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.enums.Category;
import com.project.thisvsthat.common.enums.PostStatus;
import com.project.thisvsthat.common.enums.VoteStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long postId;
    private Long userId;
    private Category category;
    private String title;
    private String content;
    private String hashtags;
    private String option1;
    private String option2;
    private String option1ImageUrl;
    private String option2ImageUrl;
    private Integer option1Count;
    private Integer option2Count;
    private VoteStatus voteStatus;
    private Integer reportCount;
    private PostStatus postStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostDTO fromEntity(Post post) {
        return new PostDTO(
                post.getPostId(),
                post.getUser().getUserId(),
                post.getCategory(),
                post.getTitle(),
                post.getContent(),
                post.getHashtags(),
                post.getOption1(),
                post.getOption2(),
                post.getOption1ImageUrl(),
                post.getOption2ImageUrl(),
                post.getOption1Count(),
                post.getOption2Count(),
                post.getVoteStatus(),
                post.getReportCount(),
                post.getPostStatus(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}