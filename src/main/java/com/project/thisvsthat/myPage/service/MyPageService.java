package com.project.thisvsthat.myPage.service;

import com.project.thisvsthat.common.dto.PostDTO;
import com.project.thisvsthat.common.dto.UserDTO;
import com.project.thisvsthat.common.entity.Post;
import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.repository.PostRepository;
import com.project.thisvsthat.myPage.DAO.MyPageDAO;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MyPageService {

    // 기본 이미지 URL 설정
    private static final String DEFAULT_OPTION1_IMAGE = "/static/images.common/icon-question-gradation-blue";
    private static final String DEFAULT_OPTION2_IMAGE = "/static/images.common/icon-question-gradation-orange";

    @Autowired
    MyPageDAO myPageDAO;

    @Autowired
    EntityManager em; //DB와 상호작용

    @Autowired
    PostRepository postRepository;

    //사용자 ID 조회
    public Long findUserId(String userId) {
        Long id = myPageDAO.findUserId(userId);
        return id;
    }

    //사용자 정보 조회
    public UserDTO findLoginUser(Long id) {
        User user = myPageDAO.getOneUser(id);
        return UserDTO.fromEntity(user);
    }

    //닉네임 수정
    public boolean infoEdit(Long id, String newNickname) {
        //DB에서 사용자 조회
        User user = myPageDAO.getOneUser(id);

        if(user != null) {
            user.setNickname(newNickname);
            myPageDAO.save(user);
            return true;
        }
        return false;
    }

    //내가 올린 게시물 조회
    public List<PostDTO> findMyPosts(Long userId) {
        List<Post> posts = myPageDAO.findMyPosts(userId);
        return posts.stream().map(PostDTO::fromEntity).collect(Collectors.toList());
    }

    //내가 투표한 게시물 조회
    public List<PostDTO> findVotedPosts(Long userId) {
        List<Post> posts = myPageDAO.findVotedPosts(userId);
        return posts.stream().map(PostDTO::fromEntity).collect(Collectors.toList());
    }
}