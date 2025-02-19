document.addEventListener("DOMContentLoaded", function () {
    const googleLoginBtn = document.getElementById("google-login");
    const kakaoLoginBtn = document.getElementById("kakao-login");
    const naverLoginBtn = document.getElementById("naver-login");

    // 로그인 버튼 이벤트 리스너
    googleLoginBtn.addEventListener("click", () => window.location.href = "/auth/google/login");
    kakaoLoginBtn.addEventListener("click", () => window.location.href = "/auth/kakao/login");
    naverLoginBtn.addEventListener("click", () => window.location.href = "/auth/naver/login");

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
