package com.project.thisvsthat.myPage.controller;

import com.project.thisvsthat.common.dto.PostDTO;
import com.project.thisvsthat.common.dto.UserDTO;
import com.project.thisvsthat.myPage.service.MyPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class MyPageController {
    @Autowired
    MyPageService myPageService;

    private static final String TEST_USER_ID = "100001";

    //사용자 상세 정보 조회
//    @GetMapping("")
//    public String myPageMain(Model model,
//                              @PathVariable("userId") String userId) {
//        Long id = myPageService.findUserId(userId);
    @GetMapping("")
    public String myPageMain(Model model) {
        Long id = myPageService.findUserId(TEST_USER_ID);
        UserDTO dto = myPageService.findLoginUser(id);

        String ageGroup = dto.getAgeGroup();

        model.addAttribute("dto", dto);
        model.addAttribute("ageGroup", ageGroup);
        //System.out.print("---------------" + dto);
        return "myPage/myPage";
    }

    //정보 수정(닉네임) 처리
//    @PatchMapping("/{userId}/edit")
//    public String infoEdit(Model model,
//                           @PathVariable("userId") String userId,
//                           @RequestParam("nickname") String nickname) {
//        Long id = myPageService.findUserId(userId);
    @PatchMapping("")
    public ResponseEntity<Map<String, Object>> editNickname(@RequestParam String nickname) {
        Long id = myPageService.findUserId(TEST_USER_ID);
        boolean editSuccess = myPageService.infoEdit(id, nickname);

        Map<String, Object> response = new HashMap<>();
        if (editSuccess) {
            response.put("success", true);
            response.put("updatedNickname", nickname); // 수정된 닉네임
        } else {
            response.put("success", false);
        }

        return ResponseEntity.ok(response);
    }

    //내가 올린 게시물 조회
//    @GetMapping("/{userId}/posted")
//    public String myPagePosts(Model model,
//                              @PathVariable("userId") String userId) {
    @GetMapping("/posts")
    public String myPagePosts(Model model) {
        Long id = myPageService.findUserId(TEST_USER_ID);
        UserDTO dto = myPageService.findLoginUser(id);
        List<PostDTO> myPosts = myPageService.findMyPosts(id);

        model.addAttribute("dto", dto);
        model.addAttribute("myPosts", myPosts);
        return "myPage/myPage";
    }

    //내가 투표한 글 조회
//    @GetMapping("/{userId}/voted")
//    public String myPageVotes(Model model,
//                              @PathVariable("userId") String userId) {
    @GetMapping("/votes")
    public String myPageVotes(Model model) {
        Long id = myPageService.findUserId(TEST_USER_ID);
        UserDTO dto = myPageService.findLoginUser(id);
        List<PostDTO> myPosts = myPageService.findMyPosts(id);
        List<PostDTO> votedPosts = myPageService.findVotedPosts(id);

        model.addAttribute("dto", dto);
        model.addAttribute("myPosts", myPosts);
        model.addAttribute("votedPosts", votedPosts);

        return "myPage/myPage";
    }

    //탈퇴하기 (status = withdrawn)
//    @PatchMapping("/users/withdrawn")
//    public ResponseEntity<Map<String, Object>> withdrawnUser() {
//        Long id = myPageService.findUserId(TEST_USER_ID);
//        boolean withdrawnSuccess = userService.withdrawnUser(id);  // 실제 탈퇴 처리 메서드 호출
//
//        Map<String, Object> response = new HashMap<>();
//        if (withdrawnSuccess) {
//            response.put("success", true);
//        } else {
//            response.put("success", false);
//        }
//
//        return ResponseEntity.ok(response);
//    }
}
