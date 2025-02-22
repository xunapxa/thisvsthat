package com.project.thisvsthat.myPage.service;

import com.project.thisvsthat.common.dto.PostDTO;
import com.project.thisvsthat.common.dto.UserDTO;
import com.project.thisvsthat.common.enums.UserStatus;
import com.project.thisvsthat.common.repository.PostRepository;
import com.project.thisvsthat.common.repository.UserRepository;
import com.project.thisvsthat.common.repository.VoteRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MyPageService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private VoteRepository voteRepository;

    // 기본 이미지 URL 설정
    private static final String DEFAULT_OPTION1_IMAGE = "/images.common/icon-blue";
    private static final String DEFAULT_OPTION2_IMAGE = "/images.common/icon-orange";

    @Autowired
    EntityManager em; //DB와 상호작용

    //사용자 ID 조회 --> findById()와 중복
//    public Long findUserId(Long userId) {
//        return userRepository.findByUserId(userId)
//                .map(User::getUserId) // User 엔티티에서 userId 추출
//                .orElse(null); // 없으면 null 반환
//    }

    //사용자 정보 조회
    public UserDTO findLoginUser(Long userId) {
        return userRepository.findById(userId)
                .map(UserDTO::fromEntity) // User -> UserDTO 변환
                .orElse(null);
    }

    //닉네임 수정
    @Transactional
    public boolean infoEdit(Long id, String newNickname) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setNickname(newNickname);
                    userRepository.saveAndFlush(user); // 즉시 반영
                    return true;
                })
                .orElse(false);
    }

    //내가 올린 게시물 조회
    public List<PostDTO> findMyPosts(Long userId) {
        return postRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PostDTO::fromEntity) // Post -> PostDTO 변환
                .collect(Collectors.toList());
    }

    //내가 투표한 게시물 조회
    public List<PostDTO> findVotedPosts(Long userId) {
        return voteRepository.findVotedPostsByUserId(userId)
                .stream()
                .map(PostDTO::fromEntity) // Post -> PostDTO 변환
                .collect(Collectors.toList());
    }

    //탈퇴
    @Transactional
    public boolean withdrawnUser(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setUserStatus(UserStatus.WITHDRAWN); // 유저 상태 변경
                    userRepository.save(user); // 변경 사항 저장
                    return true;
                })
                .orElse(false); // 유저가 없으면 false 반환
    }
}