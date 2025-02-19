document.addEventListener("DOMContentLoaded", function () {

    // JWT 쿠키 존재 시 사용자 정보 요청
    fetch("/auth/user-info", {
        method: "GET",
        credentials: "include" // HTTP-Only 쿠키 자동 포함
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("🚨 [ERROR] 사용자 정보 요청 실패: " + response.statusText);
        }
        return response.json();
    })
    .then(data => {
        console.log("📌 [DEBUG] /auth/user-info 응답 데이터:", data);
    })
    .catch(error => {
        console.error("🚨 [ERROR] 사용자 정보 요청 실패:", error);
    });
});
