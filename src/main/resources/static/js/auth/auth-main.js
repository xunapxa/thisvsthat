document.addEventListener("DOMContentLoaded", function () {
    console.log("📌 [DEBUG] 페이지 로드됨");

    // 로컬스토리지에서 JWT 토큰 확인
    const storedToken = localStorage.getItem("token");

    if (storedToken) {
        console.log("📌 [DEBUG] 로컬스토리지에서 JWT 토큰 확인:", storedToken);
    } else {
        console.log("🚨 [ERROR] JWT 토큰이 localStorage에 없음");
    }
});
