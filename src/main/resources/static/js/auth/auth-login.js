document.addEventListener("DOMContentLoaded", function () {

    const googleLoginBtn = document.getElementById("google-login");
    const kakaoLoginBtn = document.getElementById("kakao-login");
    const naverLoginBtn = document.getElementById("naver-login");

    // 로그인 버튼 이벤트
    if (googleLoginBtn) googleLoginBtn.addEventListener("click", () => window.location.href = "/auth/google/login");
    if (kakaoLoginBtn) kakaoLoginBtn.addEventListener("click", () => window.location.href = "/auth/kakao/login");
    if (naverLoginBtn) naverLoginBtn.addEventListener("click", () => window.location.href = "/auth/naver/login");
});