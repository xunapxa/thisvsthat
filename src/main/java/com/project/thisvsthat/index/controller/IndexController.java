package com.project.thisvsthat.index.controller;

import com.project.thisvsthat.common.dto.PostDTO;
import com.project.thisvsthat.index.dto.PostListResponseDTO;
import com.project.thisvsthat.index.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping({"", "/"})
    public String index(Model model,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "search_by", required = false, defaultValue = "") String searchBy,
                        @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                        @RequestParam(value = "list_category", required = false, defaultValue = "") String listCategory,
                        @RequestParam(value = "list_desc", required = false, defaultValue = "createdAt") String listDesc,
                        @RequestParam(value = "vote_status", required = false, defaultValue = "") String voteStatus,
                        @RequestParam(value = "start_date", required = false, defaultValue = "") String startDate,
                        @RequestParam(value = "end_date", required = false, defaultValue = "") String endDate) {
        PostListResponseDTO response = indexService.getFilteredPosts(page, searchBy, keyword, listCategory, listDesc, voteStatus,startDate, endDate);

        if(listDesc.equals("createdAt")){
            startDate = "";
            endDate = "";
        }


        model.addAttribute("searchBy", searchBy);
        model.addAttribute("keyword", keyword);
        model.addAttribute("listCategory", listCategory);
        model.addAttribute("listDesc", listDesc);
        model.addAttribute("voteStatus", voteStatus);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("posts", response.getPosts());
        model.addAttribute("totalCount", response.getTotalCount()); // 전체 개수 추가
        return "index";
    }


    @GetMapping("/posts")
    @ResponseBody
    public PostListResponseDTO getMorePosts(@RequestParam(value = "page", defaultValue = "0") int page,
                                            @RequestParam(value = "search_by", required = false, defaultValue = "") String searchBy,
                                            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                                            @RequestParam(value = "list_category", required = false, defaultValue = "") String listCategory,
                                            @RequestParam(value = "list_desc", required = false, defaultValue = "createdAt") String listDesc,
                                            @RequestParam(value = "vote_status", required = false, defaultValue = "") String voteStatus,
                                            @RequestParam(value = "start_date", required = false, defaultValue = "") String startDate,
                                            @RequestParam(value = "end_date", required = false, defaultValue = "") String endDate) {
        System.out.println(startDate);
        return indexService.getFilteredPosts(page, searchBy, keyword, listCategory, listDesc, voteStatus,startDate, endDate);
    }
}
