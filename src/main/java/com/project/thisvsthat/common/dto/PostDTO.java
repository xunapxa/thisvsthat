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

    // entity -> dto
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

    // dto -> entity
    public static Post fromDto(PostDTO dto) {
        System.out.println("dto -> entity 전 받아온 dto =============== "+dto);
        Post post = new Post();
        post.setPostId(dto.getPostId());
        post.setUser(null); // 로그인 중인 사용자의 아이디로 찾은 User 정보를 저장하기 (DAO 에서)
        post.setCategory(dto.getCategory());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setHashtags(dto.getHashtags());
        post.setOption1(dto.getOption1());
        post.setOption2(dto.getOption2());
        post.setOption1ImageUrl(dto.getOption1ImageUrl());
        post.setOption2ImageUrl(dto.getOption2ImageUrl());
        return post;
    }
}