package com.project.thisvsthat.post.service;

import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.entity.Vote;
import com.project.thisvsthat.common.enums.SelectedOption;
import com.project.thisvsthat.common.enums.VoteStatus;
import com.project.thisvsthat.common.repository.PostRepository;
import com.project.thisvsthat.common.repository.UserRepository;
import com.project.thisvsthat.common.repository.VoteRepository;
import com.project.thisvsthat.post.dto.VotePercentageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public VotePercentageDTO getVotePercentage(Long postId) {
        Object[] result = voteRepository.countVotesByPost(postId);


        if (result == null || result.length == 0 || result[0] == null) {
            log.error("데이터가 없습니다. postId: {}", postId);
            return new VotePercentageDTO(0, 0, 0.0, 0.0, 0);
        }

        long option1Votes = 0, option2Votes = 0;

        Object[] data = (Object[]) result[0];

        if (data[0] != null) {
            option1Votes = ((Number) data[0]).longValue();
        }
        if (data[1] != null) {
            option2Votes = ((Number) data[1]).longValue();
        }

        long totalVotes = option1Votes + option2Votes;
        double option1Percentage = (totalVotes > 0) ? (option1Votes * 100.0 / totalVotes) : 0;
        double option2Percentage = (totalVotes > 0) ? (option2Votes * 100.0 / totalVotes) : 0;

        log.info("투표 결과: OPTION_1 = {}표, OPTION_2 = {}표, 총 투표 수 = {}", option1Votes, option2Votes, totalVotes);

        return new VotePercentageDTO(option1Votes, option2Votes, option1Percentage, option2Percentage, totalVotes);
    }

    public void saveVote(Long userId, Long postId, SelectedOption selectedOption) {

        // 이미 투표를 했는지 확인
        boolean exists = voteRepository.existsByUserIdAndPostId(userId, postId);
        
        // 투표 데이터 존재 여부에 따라 create, update
        if (exists == false) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. ID: " + userId));
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 없습니다. ID: " + postId));

            Vote vote = new Vote();
            vote.setPost(post);
            vote.setUser(user);
            vote.setSelectedOption(selectedOption);
            vote.setCreatedAt(LocalDateTime.now());
            user.getVotes().add(vote);
            post.getVotes().add(vote);

            System.out.println("저장 전 vote 결과 (선택옵션) ========== " + vote.getSelectedOption());
            voteRepository.save(vote);
        } else if (exists == true) {

            Long voteId = voteRepository.findVoteIdByUserIdAndPostId(userId, postId);

            Vote vote = voteRepository.findById(voteId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 투표기록이 없습니다. ID: " + voteId));

            vote.setSelectedOption(selectedOption);
            vote.setCreatedAt(LocalDateTime.now());

            System.out.println("변경 전 vote 결과 (선택옵션) ========== " + vote.getSelectedOption());
            voteRepository.save(vote);
        }


    }

    public void voteFinished(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다. ID: " + postId));
        post.setVoteStatus(VoteStatus.FINISHED);
        postRepository.save(post);
    }
}