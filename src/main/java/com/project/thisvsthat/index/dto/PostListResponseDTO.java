package com.project.thisvsthat.index.dto;

import com.project.thisvsthat.common.dto.PostDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostListResponseDTO {
    private List<PostDTO> posts; // 현재 페이지의 게시글 리스트
    private long totalCount; // 전체 개수
}