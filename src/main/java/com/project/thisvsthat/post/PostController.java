package com.project.thisvsthat.post;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("post")
public class PostController {

    /* 상세 페이지 */
    @GetMapping("")
    public String postDetail() {
        return "post/postDetail";
    }

    /* 작성 페이지 */
    @GetMapping("create")
    public String createPost() {
        return "post/createPost";
    }

    /* 수정 페이지 */
    @GetMapping("update")
    public String updatePost() {
        return "post/updatePost";
    }

}
