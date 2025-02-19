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
document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("postDeleteBtn").addEventListener("click", function (event) {
        event.preventDefault();
        let deleteUrl = this.getAttribute("href");

        if (confirm("삭제하시겠습니까?")) {
            window.location.href = deleteUrl;
        }
    });
});

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

        let formattedText = text.replace(/#([ㄱ-ㅎ|가-힣|a-zA-Z0-9_]+)/g, "<a href='?search_by=hashtags&keyword=$1' class='hashtag'>#$1</a>");

        postContent.innerHTML = formattedText;
    }
});

