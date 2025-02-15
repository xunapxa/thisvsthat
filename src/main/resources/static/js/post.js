// 팝업 활성화/비활성화
document.addEventListener("DOMContentLoaded", function () {
    var popup = document.querySelector(".popup_section");
    var openBtn = document.getElementById("postImgDescription");
    var closeBtn = document.getElementById("postCreateCloseBtn");
    var okBtn = document.getElementById("postCreateOkBtn");

    openBtn.addEventListener("click", function (event) {
        event.preventDefault(); // 기본 동작 방지 (페이지 이동 방지)
        popup.style.display = "block";
    });

    closeBtn.addEventListener("click", function (event) {
        event.preventDefault(); // 기본 동작 방지 (페이지 이동 방지)
        popup.style.display = "none";
    });

    okBtn.addEventListener("click", function (event) {
        event.preventDefault(); // 기본 동작 방지 (페이지 이동 방지)
        popup.style.display = "none";
    });
});

