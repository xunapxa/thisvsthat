package com.project.thisvsthat.post.controller;

import com.project.thisvsthat.common.dto.PostDTO;
import com.project.thisvsthat.common.dto.VoteDTO;
import com.project.thisvsthat.image.service.ImageService;
import com.project.thisvsthat.post.dto.VotePercentageDTO;
import com.project.thisvsthat.post.service.PostService;
import com.project.thisvsthat.post.service.VoteService;
import com.project.thisvsthat.post.util.HashtagExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("post")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private VoteService voteService;

    /* 상세 페이지 */
    @GetMapping("{id}")
    public String postDetail(@PathVariable("id") Long postId, Model model) {
        PostDTO dto = postService.findOnePost(postId);
        VotePercentageDTO votePercentage = voteService.getVotePercentage(postId);

        model.addAttribute("dto", dto);
        model.addAttribute("vote", new VoteDTO());
        model.addAttribute("votePercentage", votePercentage);
        return "post/postDetail";
    }

    /* 작성 페이지 */
    @GetMapping("create")
    public String createPost(Model model) {
        model.addAttribute("dto", new PostDTO());
        return "post/createPost";
    }

    /* 수정 페이지 */
    @GetMapping("{id}/update")
    public String updatePost(@PathVariable("id") Long postId, Model model) {
        PostDTO dto = postService.findOnePost(postId);
        model.addAttribute("dto", dto);
        return "post/updatePost";
    }

    /* 새 글 등록 */
    @PostMapping("create")
    public String insertPost(@ModelAttribute("dto") PostDTO dto,
                             @RequestParam("imageFile1") MultipartFile imageFile1,
                             @RequestParam("imageFile2") MultipartFile imageFile2,
                             Model model) {

        String imageUrl1 = null;
        String imageUrl2 = null;

        try {
            // 이미지 업로드 및 DB 저장
            imageUrl1 = imageService.uploadImage(imageFile1);
            imageUrl2 = imageService.uploadImage(imageFile2);
        } catch (Exception e) {
            model.addAttribute("message", "이미지 업로드 실패: " + e.getMessage());
        }

        System.out.println("화면단에서 받아온 dto 정보 ========== " + dto);
        dto.setOption1ImageUrl(imageUrl1);
        dto.setOption2ImageUrl(imageUrl2);

        // 해시태그 추출 및 설정
        String extractedHashtags = HashtagExtractor.extractHashtags(dto.getContent());
        System.out.println("추출한 해시태그 정보 ==========" + extractedHashtags);
        dto.setHashtags(extractedHashtags);

        postService.savePost(100002L, dto);
        return "redirect:/";
    }

    /* 글 수정 */
    @PostMapping("{id}/update")
    public String updatePost(@ModelAttribute("dto") PostDTO dto,
                             @PathVariable("id") Long postId,
                             @RequestParam("imageFile1") MultipartFile imageFile1,
                             @RequestParam("imageFile2") MultipartFile imageFile2,
                             Model model) {

        String imageUrl1 = null;
        String imageUrl2 = null;

        try {
            // 이미지 업로드 및 DB 저장
            imageUrl1 = imageService.uploadImage(imageFile1);
            imageUrl2 = imageService.uploadImage(imageFile2);
        } catch (Exception e) {
            model.addAttribute("message", "이미지 업로드 실패: " + e.getMessage());
        }

        System.out.println("화면단에서 받아온 dto 정보 ========== " + dto);
        dto.setOption1ImageUrl(imageUrl1);
        dto.setOption2ImageUrl(imageUrl2);

        // 해시태그 추출 및 설정
        String extractedHashtags = HashtagExtractor.extractHashtags(dto.getContent());
        System.out.println("추출한 해시태그 정보 ==========" + extractedHashtags);
        dto.setHashtags(extractedHashtags);

        postService.updatePost(postId, dto);

        String url = "redirect:/post/" + postId;
        return url;
    }

    /* 글 삭제 */
    @GetMapping("{id}/delete")
    public String deletePost(@PathVariable("id") Long postId) {
        postService.deletePost(postId);
        return "redirect:/";
    }

    /* 투표 저장 */
    @PostMapping("{id}/vote")
    public String saveVote(@PathVariable("id") Long postId, @ModelAttribute("dto") VoteDTO dto) {
        System.out.println("투표한 내용 ==========" + dto.getSelectedOption());
        voteService.saveVote(100002L,postId, dto.getSelectedOption());
        String url = "redirect:/post/" + postId;
        return url;
    }

    /* 투표 종료 */
    @GetMapping("{id}/voteFinished")
    public String voteFinished(@PathVariable("id") Long postId, @ModelAttribute("dto") VoteDTO dto) {
        voteService.voteFinished(postId);
        String url = "redirect:/post/" + postId;
        return url;
    }
}
