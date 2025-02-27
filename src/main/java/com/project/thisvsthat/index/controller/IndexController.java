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

    // 메인(검색 조건, 전체 목록 갯수  담아가기)
    @GetMapping({"", "/"})
    public String index(Model model,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "search_by", required = false, defaultValue = "") String searchBy,
                        @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                        @RequestParam(value = "list_category", required = false, defaultValue = "") String listCategory,
                        @RequestParam(value = "list_desc", required = false, defaultValue = "createdAt") String listDesc,
                        @RequestParam(value = "vote_status", required = false, defaultValue = "") String voteStatus,
                        @RequestParam(value = "start_date", required = false, defaultValue = "") String startDate,
                        @RequestParam(value = "end_date", required = false, defaultValue = "") String endDate,
                        @RequestParam(value = "page_cnt", required = false, defaultValue = "1") int pageCnt) {
        PostListResponseDTO response = indexService.getFilteredPosts(page, searchBy, keyword, listCategory, listDesc, voteStatus,startDate, endDate,pageCnt);

        if(listDesc.equals("createdAt")){
            startDate = "";
            endDate = "";
        }

        model.addAttribute("searchBy", searchBy); // 검색 - 제목, 내용, 해시태그
        model.addAttribute("keyword", keyword); // 검색 - 검색어
        model.addAttribute("listCategory", listCategory); // 검색 - 카테고리(전체, 고민,토론, 자유)
        model.addAttribute("listDesc", listDesc); // 검색 - 정렬 기준(최신순, 인기순)
        model.addAttribute("voteStatus", voteStatus); // 검색 - 투표상태(전체, 진행, 종료)
        model.addAttribute("startDate", startDate); // 검색 - 정렬 인기순 일 때 시작날짜
        model.addAttribute("endDate", endDate); // 검색 - 정렬 인기순 일 때 종료날짜
        model.addAttribute("totalCount", response.getTotalCount()); // 목록 전체 개수

        return "index";
    }

    // 메인 목록 무한 스크롤 및 처음에 로드 시 목록 가져오기
    @GetMapping("/posts")
    @ResponseBody
    public PostListResponseDTO getMorePosts(@RequestParam(value = "page", defaultValue = "0") int page,
                                            @RequestParam(value = "search_by", required = false, defaultValue = "") String searchBy,
                                            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                                            @RequestParam(value = "list_category", required = false, defaultValue = "") String listCategory,
                                            @RequestParam(value = "list_desc", required = false, defaultValue = "createdAt") String listDesc,
                                            @RequestParam(value = "vote_status", required = false, defaultValue = "") String voteStatus,
                                            @RequestParam(value = "start_date", required = false, defaultValue = "") String startDate,
                                            @RequestParam(value = "end_date", required = false, defaultValue = "") String endDate,
                                            @RequestParam(value = "page_cnt", required = false, defaultValue = "1") int pageCnt) {
        return indexService.getFilteredPosts(page, searchBy, keyword, listCategory, listDesc, voteStatus,startDate, endDate,pageCnt);
    }
}
