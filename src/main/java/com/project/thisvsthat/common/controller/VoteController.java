package com.project.thisvsthat.common.controller;

import com.project.thisvsthat.post.dto.VotePercentageDTO;
import com.project.thisvsthat.post.service.VoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    // 특정 게시글(postId)의 투표율 조회
    @GetMapping("/{postId}")
    public VotePercentageDTO getVoteResult(@PathVariable("postId") Long postId) {
        return voteService.getVotePercentage(postId);
    }
}

