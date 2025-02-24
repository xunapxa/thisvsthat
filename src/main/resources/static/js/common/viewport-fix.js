function updateViewportHeight() {
    // 윈도우 높이를 기반으로 --vh 변수를 설정
    document.documentElement.style.setProperty('--vh', `${window.innerHeight * 0.01}px`);
}

// 페이지가 로드될 때 실행
document.addEventListener("DOMContentLoaded", updateViewportHeight);

// 창 크기가 변경될 때마다 업데이트
window.addEventListener("resize", updateViewportHeight);

// 초기 설정
updateViewportHeight();
