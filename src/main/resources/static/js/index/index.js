// 게시물 출력 무한 스크롤 start
let page = 0;
let loading = false;
const pageSize = 3;

// 현재 URL의 쿼리스트링 가져오기
const currentQueryString = window.location.search;

// 검색 조건 가져오기
const searchParams = new URLSearchParams(currentQueryString);
const searchBy = searchParams.get("search_by") || "";
const keyword = searchParams.get("keyword") || "";
const listCategory = searchParams.get("list_category") || "";
const listDesc = searchParams.get("list_desc") || "createdAt";
const startDate = searchParams.get("start_date");
const endDate = searchParams.get("end_date");
const voteStatus = searchParams.get("vote_status");

// 이전에 저장된 쿼리스트링 가져오기
const savedQueryString = sessionStorage.getItem("savedQueryString");

// 전체 목록 갯수
const totalCount = parseInt(document.getElementById("total-count").value, 10);
document.addEventListener("DOMContentLoaded", function () {


    if (totalCount <= pageSize) {
        document.getElementById("end_message").style.display = "block";
    }

    if (totalCount === 0 && totalCount <= pageSize) {
        document.getElementById("no_posts_message").style.display = "block";
        document.getElementById("end_message").style.display = "none";
    }

    // 쿼리스트링이 변경되었을 경우 저장된 데이터 초기화
    if (savedQueryString !== currentQueryString) {
        sessionStorage.removeItem("savedPosts");
        sessionStorage.removeItem("savedPage");
        sessionStorage.removeItem("scrollPosition");
        sessionStorage.setItem("savedQueryString", currentQueryString);
        window.scrollTo(0, 0);
        return;
    }

    // 뒤로 가기 시 데이터 복원 (쿼리스트링이 동일할 때만)
    if (sessionStorage.getItem("savedPosts")) {
        $("#list_wrap").html(sessionStorage.getItem("savedPosts"));
        page = Number(sessionStorage.getItem("savedPage"));
        window.scrollTo(0, Number(sessionStorage.getItem("scrollPosition")));
    }
});

// 처음 로드 됐을 때 main에 보여줄 목록 갯수 체크해서 목록 가져오기
// 세로 높이 여부에 따라 처음에 가져올 갯수를 정함
document.addEventListener("DOMContentLoaded", function () {

    const listWrap = document.getElementById("list_wrap");
    const rect = listWrap.getBoundingClientRect();
    const viewportHeight = window.innerHeight; // 브라우저의 세로 길이(뷰포트 높이)
    const list_wrap_heght = viewportHeight-rect.top; // 브라우저에서 list_wrap id를 가진 div의 높이
    const list_page = list_wrap_heght/752; // 처음에 가져와야할 list_page 갯수, 752는 1페이지당 높이
    const default_page_cnt = Math.floor(list_page)+1; // 1이면 페이지 1개만 가져오고 2이 이상이면 페이지를 여러 개 가져와야함.

    loadMorePosts(default_page_cnt);
});

// 무한스크롤 이벤트 발생 했을 때 loadMorePosts() 이벤트로 list 가져오기
$(window).scroll(function() {
    if (loading) return;

    if ($(window).scrollTop() + $(window).height() >= $(document).height() - 50) {
        loadMorePosts(1);
    }
});

// list의 게시글 가져오기
// pageCnt는 가져와야할 페이지 갯수 (1이면 현재 페이지 한 페이지만 가져오고 2이면 0,1 이렇게 2페이지 목록을 가져와야함)
function loadMorePosts(pageCnt) {
    loading = true;

    $.ajax({
        url: "/posts",
        type: "GET",
        data: { page, search_by: searchBy, keyword, list_category: listCategory, list_desc: listDesc, start_date: startDate, end_date: endDate,  vote_status: voteStatus,page_cnt : pageCnt},
        success: function(data) {
            if (data.posts.length === 0) {
                $("#end_message").show();
            } else {
                data.posts.forEach(post => {
                    let option1ImageUrl = post.option1ImageUrl?.trim() || "/images/common/icon-letter-o.png";
                    let option1Class = post.option1ImageUrl?.trim() ? "choose_img is_img" : "choose_img";

                    let option2ImageUrl = post.option2ImageUrl?.trim() || "/images/common/icon-letter-x.png";
                    let option2Class = post.option2ImageUrl?.trim() ? "choose_img is_img" : "choose_img";

                    $("#list_wrap").append(`
                        <div class="choose_section margin_bottom10 position_relative">
                            <span class="vote_status margin_bottom10 ${post.voteStatus === 'ACTIVE' ? 'vote_ing' : (post.voteStatus === 'FINISHED' ? 'vote_finished' : '')}">
                                ${post.voteStatus === 'ACTIVE' ? '진행' : (post.voteStatus === 'FINISHED' ? '종료' : '')}
                            </span>

                            <a href="/post/${post.postId}" class="choose_total">
                                <div class="choose_top_box">
                                    <div class="choose_img_wrap ${post.option1ImageUrl?.trim() ? 'is_img_wrap' : ''}">
                                        <img class="${option1Class}" src="${option1ImageUrl}" alt="" onerror="this.classList.remove('is_img'); this.onerror=null; this.src='/images/common/icon-letter-o.png';"/>
                                    </div>
                                    <div class="choose_img_wrap ${post.option2ImageUrl?.trim() ? 'is_img_wrap' : ''}">
                                        <img class="${option2Class}" src="${option2ImageUrl}" alt="" onerror="this.classList.remove('is_img'); this.onerror=null; this.src='/images/common/icon-letter-x.png';"/>
                                    </div>
                                </div>
                                <div class="choose_bottom_box">
                                    <p class="shorten">${post.title}</p>
                                </div>
                            </a>
                        </div>
                    `);
                });

                if(pageCnt==1){
                    page++;
                }
                else{
                    page = pageCnt;
                }

            }

            // 저장된 데이터 업데이트 (뒤로 가기 시 복원 가능하도록)
            sessionStorage.setItem("savedPosts", $("#list_wrap").html());
            sessionStorage.setItem("savedPage", page);
            sessionStorage.setItem("scrollPosition", window.scrollY);

            if (totalCount === 0 && totalCount <= pageSize) {
                document.getElementById("no_posts_message").style.display = "block";
                document.getElementById("end_message").style.display = "none";
            }else if ((page * pageSize) >= data.totalCount) {
                document.getElementById("end_message").style.display = "block";
            }

        },
        error: function() {
            alert("게시물을 불러오는 중 오류가 발생했습니다.");
        },
        complete: function() {
            loading = false;
        }
    });
}
// 게시물 출력 무한 스크롤 end

// 팝업 활성화 / 비활성화 start
document.addEventListener("DOMContentLoaded", function () {
    var popup = document.querySelector(".popup_section");
    var openBtn = document.getElementById("modal_btn");
    var closeBtn = document.getElementById("postCreateCloseBtn");
    var okBtn = document.getElementById("date_search_btn");

    if (openBtn) {
        openBtn.addEventListener("click", function (event) {
            event.preventDefault();
            popup.style.display = "block";
        });
    }

    if (closeBtn) {
        closeBtn.addEventListener("click", function (event) {
            event.preventDefault();
            popup.style.display = "none";
        });
    }

});
// 팝업 활성화 / 비활성화 end


// 기간 설정 시 시작날짜, 종료 날짜 입력 후 기간 설정 완료 눌렀을 때 이동 링크 start
// 링크 이동
document.querySelector('#date_search_btn').addEventListener('click', function(event) {
    event.preventDefault(); // 기본 a 태그 동작 막기

    let startDateValue = document.querySelector("#start_date").value;
    let endDateValue = document.querySelector("#end_date").value;

    // 기존 <a> 태그의 href 속성 가져오기
    let baseUrl = this.getAttribute("href");
    let url = new URL(baseUrl, window.location.origin); // 절대 URL 생성

    // 날짜 값이 있을 경우 URL에 추가
    if (startDateValue&&endDateValue) {
        url.searchParams.set("start_date", startDateValue);
        url.searchParams.set("end_date", endDateValue);

        // URL 이동
        window.location.href = url;
    }

    if(!startDateValue || !endDateValue){
        alert("기간을 입력해주세요.");
    }

});

// 기간 설정 시 시작날짜, 종료 날짜 입력 후 기간 설정 완료 눌렀을 때 이동 링크 end

// top 버튼 start
document.querySelector('.top_btn').addEventListener('click', function(e) {
    e.preventDefault();  // a 태그 기본 동작 막기
    window.scrollTo({ top: 0, behavior: 'smooth' }); // 부드럽게 맨 위로 이동
});
// top 버튼 end





































