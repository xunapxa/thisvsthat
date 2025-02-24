// 팝업 활성화 / 비활성화
document.addEventListener("DOMContentLoaded", function () {
    var popup = document.querySelector(".popup_section");
    var openBtn = document.getElementById("postImgDescription");
    var closeBtn = document.getElementById("postCreateCloseBtn");
    var okBtn = document.getElementById("postCreateOkBtn");

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

    if (okBtn) {
        okBtn.addEventListener("click", function (event) {
            event.preventDefault();
            popup.style.display = "none";
        });
    }
});

// 폼 제출 유효성 검사
function validateForm() {
    let title = document.getElementById("title").value.trim();
    let option1 = document.getElementById("option1").value.trim();
    let option2 = document.getElementById("option2").value.trim();

    if (title === "") {
        alert("글의 제목을 작성하세요.");
        document.getElementById("title").focus();
        return false;
    }
    if (option1 === "") {
        alert("첫번째 선택지의 설명을 작성하세요.");
        document.getElementById("option1").focus();
        return false;
    }
    if (option2 === "") {
        alert("두번째 선택지의 설명을 작성하세요.");
        document.getElementById("option2").focus();
        return false;
    }

    return true;
}

// 삭제 confirm
//document.addEventListener("DOMContentLoaded", function () {
//    document.getElementById("postDeleteBtn").addEventListener("click", function (event) {
//        event.preventDefault();
//        let deleteUrl = this.getAttribute("href");
//
//        if (confirm("삭제하시겠습니까?")) {
//            window.location.href = deleteUrl;
//        }
//    });
//});

// 투표종료 confirm
document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("voteFinished").addEventListener("click", function (event) {
        event.preventDefault();
        let url = this.getAttribute("href");

        if (confirm("투표를 종료 하시겠습니까?\n투표종료 시 게시글을 삭제할 수 없습니다")) {
            window.location.href = url;
        }
    });
});

// 해시태그 변환
document.addEventListener("DOMContentLoaded", function () {
    let postContent = document.getElementById("postContent");

    if (postContent) {
        let text = postContent.innerHTML;

        let formattedText = text.replace(/#([ㄱ-ㅎ|가-힣|a-zA-Z0-9_]+)/g, "<a href='/?search_by=hashtags&keyword=$1' class='hashtag'>#$1</a>");

        postContent.innerHTML = formattedText;
    }
});

// 공유 버튼 클릭 시 URL 복사
document.addEventListener("DOMContentLoaded", function () {
    const shareBtn = document.getElementById("shareButton");

    if (shareBtn) {
        shareBtn.addEventListener("click", function () {
            const url = window.location.href;
            const textarea = document.createElement("textarea");

            textarea.value = url;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand("copy");
            document.body.removeChild(textarea);

            alert("URL이 클립보드에 복사되었습니다.");
        });
    }
});

// 뒤로 가기 버튼 클릭 시 뒤로 가기
document.addEventListener("DOMContentLoaded", function () {
    const backButton = document.getElementById("backButton");

    if (backButton) {
        backButton.addEventListener("click", function () {
            if (window.history.length > 1) {
                // 뒤로 갈 페이지가 있으면 뒤로 가기
                window.history.back();
            } else {
                // 뒤로 갈 페이지가 없으면 메인 페이지로 이동
                window.location.href = "/";
            }
        });
    }
});
