package com.project.thisvsthat.common.entity;

import com.project.thisvsthat.common.enums.Category;
import com.project.thisvsthat.common.enums.PostStatus;
import com.project.thisvsthat.common.enums.VoteStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Category category = Category.자유;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String hashtags;

    @Column(nullable = false, length = 255)
    private String option1;

    @Column(nullable = false, length = 255)
    private String option2;

    @Column(length = 1000)
    private String option1ImageUrl;

    @Column(length = 1000)
    private String option2ImageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Integer option1Count = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer option2Count = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private VoteStatus voteStatus = VoteStatus.ACTIVE;

    @Builder.Default
    @Column(nullable = false)
    private Integer reportCount = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PostStatus postStatus = PostStatus.ACTIVE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(insertable = false)
    private LocalDateTime updatedAt;
}
