// 게시물 무한 스크롤 start
let page = 1;
let loading = false;

// 검색 조건 가져오기
const searchBy = new URLSearchParams(window.location.search).get("search_by") || "";
const keyword = new URLSearchParams(window.location.search).get("keyword") || "";
const listCategory = new URLSearchParams(window.location.search).get("list_category") || "";
const listDesc = new URLSearchParams(window.location.search).get("list_desc") || "createdAt";
const startDate = new URLSearchParams(window.location.search).get("start_date");
const endDate = new URLSearchParams(window.location.search).get("end_date");


// 페이지 로드 시, 게시물 개수가 3개 이하라면 "게시물 완료" 표시
document.addEventListener("DOMContentLoaded", function () {
    const totalCount = parseInt(document.getElementById("total-count").value, 10);
    const pageSize = 3;

    if (totalCount <= pageSize) {
        document.getElementById("end_message").style.display = "block";
    }

    if (totalCount == 0 && totalCount <= pageSize) {
        document.getElementById("no_posts_message").style.display = "block";
        document.getElementById("end_message").style.display = "none";
    }
});

// 무한스크롤 이벤트
$(window).scroll(function() {
    if (loading) return;

    if ($(window).scrollTop() + $(window).height() >= $(document).height() - 50) {
        loadMorePosts();
    }
});

function loadMorePosts() {
    loading = true;

    $.ajax({
        url: "/posts",
        type: "GET",
        data: { page: page, search_by: searchBy, keyword: keyword, list_category: listCategory, list_desc: listDesc, start_date: startDate, end_date: endDate },
        success: function(data) {
            if (data.posts.length === 0) { // ✅ posts 배열을 가져와서 확인
                $("#end_message").show(); // 게시물 완료 메시지 표시
            } else {
                data.posts.forEach(post => { // ✅ data.posts로 변경
                    let option1ImageUrl = post.option1ImageUrl && post.option1ImageUrl.trim() !== ""
                        ? post.option1ImageUrl
                        : "/images/common/icon-letter-o.png";

                    let option1Class = post.option1ImageUrl && post.option1ImageUrl.trim() !== ""
                        ? "choose_img is_img"
                        : "choose_img"; // 이미지가 없으면 no_img 클래스 추가

                    let option2ImageUrl = post.option2ImageUrl && post.option2ImageUrl.trim() !== ""
                        ? post.option2ImageUrl
                        : "/images/common/icon-letter-x.png";

                    let option2Class = post.option2ImageUrl && post.option2ImageUrl.trim() !== ""
                        ? "choose_img is_img"
                        : "choose_img"; // 이미지가 없으면 no_img 클래스 추가

                    $("#list_wrap").append(`
                        <div class="choose_section margin_bottom10 position_relative">
                            <span class="vote_status margin_bottom10 ${post.voteStatus === 'ACTIVE' ? 'vote_ing' : (post.voteStatus === 'FINISHED' ? 'vote_finished' : '')}" >
                                ${post.voteStatus === 'ACTIVE' ? '진행' : (post.voteStatus === 'FINISHED' ? '종료' : '')}
                            </span>

                            <a href="/posts/${post.postId}" class="choose_total">
                                <div class="choose_top_box">
                                    <div class="choose_img_wrap ${post.option1ImageUrl && post.option1ImageUrl.trim() !== '' ? 'is_img_wrap' : ''}">
                                        <img class="${option1Class} " src="${option1ImageUrl}" alt="" onerror="this.classList.remove('is_img');this.onerror=null; this.src='/images/common/icon-letter-o.png';"/>
                                    </div>
                                    <div class="choose_img_wrap ${post.option2ImageUrl && post.option2ImageUrl.trim() !== '' ? 'is_img_wrap' : ''}">
                                        <img class="${option2Class} " src="${option2ImageUrl}" alt="" onerror="this.classList.remove('is_img');this.onerror=null; this.src='/images/common/icon-letter-x.png';" />
                                    </div>
                                </div>
                                <div class="choose_bottom_box">
                                    <p class="shorten">${post.title}</p>
                                </div>
                            </a>
                        </div>
                    `);

                });

                page++; // 다음 페이지 증가
            }

            // ✅ 전체 개수 체크해서 더 이상 불러올 게시물이 없으면 "게시물 완료" 표시
            if ((page * 3) >= data.totalCount) {
                $("#end_message").show();
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
// 게시물 무한 스크롤 end

// 모달 start
const modal = document.querySelector('.modal');
const modalOpen = document.querySelector('#modal_btn');
const modalClose = document.querySelector('#modal_close_btn');
const dateSearchBtn = document.querySelector('.date_search_btn');

//열기 버튼을 눌렀을 때 모달팝업이 열림
modalOpen.addEventListener('click',function(){
  	//'on' class 추가
  	event.preventDefault();
    modal.classList.add('on');
});

//닫기 버튼을 눌렀을 때 모달팝업이 닫힘
modalClose.addEventListener('click',function(){
    //'on' class 제거
    modal.classList.remove('on');
});
// 모달 end

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
