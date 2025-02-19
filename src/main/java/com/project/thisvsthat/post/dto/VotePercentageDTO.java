package com.project.thisvsthat.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VotePercentageDTO {
    private long option1Votes;   // OPTION_1 투표 수
    private long option2Votes;   // OPTION_2 투표 수
    private double option1Percentage; // OPTION_1 투표율 (%)
    private double option2Percentage; // OPTION_2 투표율 (%)
    private long totalVotes;     // 총 투표 수
}