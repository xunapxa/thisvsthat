document.addEventListener("DOMContentLoaded", function () {

    const googleLoginBtn = document.getElementById("google-login");
    const kakaoLoginBtn = document.getElementById("kakao-login");
    const naverLoginBtn = document.getElementById("naver-login");

    // 로그인 버튼 이벤트 리스너
    googleLoginBtn.addEventListener("click", () => window.location.href = "/auth/google/login");
    kakaoLoginBtn.addEventListener("click", () => window.location.href = "/auth/kakao/login");
    naverLoginBtn.addEventListener("click", () => window.location.href = "/auth/naver/login");

    // JWT 토큰 로컬 저장소에서 가져오기
    const storedToken = localStorage.getItem("token");
    console.log("📌 [DEBUG] 저장된 JWT 토큰:", storedToken);

    // JWT 토큰이 있을 경우, 사용자 정보를 가져오기
    if (storedToken) {
        console.log("📌 [DEBUG] JWT 토큰이 존재하여 사용자 정보를 요청합니다.");

        fetch("/auth/user-info", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${storedToken}`
            }
        })
        .then(response => {
            console.log("📌 [DEBUG] /auth/user-info 응답 상태 코드:", response.status);
            return response.json();
        })
        .then(data => {
            console.log("📌 [DEBUG] /auth/user-info 응답 데이터:", data);

            if (!data || data.error) {
                console.log("🚨 [ERROR] 사용자 정보 로딩 실패:", data.error);

                if (data.error === "토큰이 만료되었습니다") {
                    alert("세션이 만료되었습니다. 다시 로그인해주세요.");
                } else {
                    alert("로그인이 필요합니다.");
                }

                localStorage.removeItem("token"); // 토큰 삭제 후 다시 로그인 유도
                window.location.href = "/login";
            }
        })
        .catch(error => {
            console.error("🚨 [ERROR] 사용자 정보 요청 실패:", error);
        });
    } else {
        console.log("📌 [DEBUG] 저장된 JWT 토큰이 없습니다.");
    }

});