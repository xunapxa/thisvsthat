$(document).ready(function() {
    // 채팅 팝업 열기
    $('#open-chat-profile-popup').on('click', function() {
        const token = getCookie("JWT_TOKEN"); // JWT 토큰을 쿠키에서 가져옴

        if (token) {
            // 토큰이 있으면 닉네임을 가져오려고 서버에 요청
            axios.get(`/chat/get-profile`, { headers: { "Authorization": `Bearer ${token}` } })
                .then(function(response) {
                    const user = response.data;

                    if (user.profileImageUrl) {
                        $("#profile-image").attr("src", user.profileImageUrl);
                    }
                    $('#chat-nickname').val(user.nickname);
                    $('#nickname-length').text(user.nickname.length);

                    // 팝업창 보이기
                    $('#popup-section').fadeIn();
                    $('#toast-popup-box').css('transform', 'translateY(0)');
                })
                .catch(function(error) {
                    console.error("프로필 로드 중 오류:", error);
                    alert("프로필을 불러오지 못했습니다.");
                });
        } else {
            alert("로그인이 필요합니다!");
            window.location.href = "/login?redirect=" + encodeURIComponent(window.location.href);
        }
    });

    // 채팅 팝업 닫기
    $('#close-chat-profile-popup').on('click', function(e) {
        e.preventDefault();
        $('#popup-section').fadeOut();
        $('#toast-popup-box').css('transform', 'translateY(100%)');
    });

    // 프로필 이미지 선택 시 이미지 선택 인풋 동작
    $('#profile-image, #edit-icon').on('click', function() {
        $('#profile-input').click();
    });

    // 이미지 선택하면 프로필 사진에 적용
    $("#profile-input").on('change', function(event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const img = new Image();
                img.src = e.target.result;

                img.onload = function() {
                    const canvas = document.createElement('canvas');
                    const ctx = canvas.getContext('2d');

                    // 이미지의 비율 맞추기
                    const size = Math.min(img.width, img.height);  // 가장 작은 값을 선택 (정사각형 만들기)
                    const x = (img.width - size) / 2;  // 이미지 중심에서 자르기 시작
                    const y = (img.height - size) / 2;  // 이미지 중심에서 자르기 시작

                    canvas.width = size;
                    canvas.height = size;

                    // 중앙 부분을 잘라서 1:1 비율로 캔버스에 그리기
                    ctx.drawImage(img, x, y, size, size, 0, 0, size, size);

                    // 결과적으로 잘린 이미지를 프로필에 적용
                    const dataURL = canvas.toDataURL("image/png");
                    $("#profile-image").attr("src", dataURL);
                };
            };
            reader.readAsDataURL(file);
        }
    });

    // 닉네임 길이
    $('#chat-nickname').on('input change', function() {
        // 텍스트 길이를 문자 단위로 제한 (한글 인코딩 문제로 maxlength+1까지 입력 가능한 것을 제한)
        if (this.value.length > 15) {
            this.value = this.value.substring(0, 15);
        }
        $('#nickname-length').text(this.value.length);
    });
});