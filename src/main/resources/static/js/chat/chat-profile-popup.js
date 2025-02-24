$(document).ready(function() {
    // 채팅 팝업 열기
    $('#open-chat-profile-popup').on('click', function() {
        axios.post("/auth/check-token", {}, { withCredentials: true })
            .then(function(response) {
                if (response.data && response.data !== "JWT 쿠키 없음") {
                    // JWT 토큰이 있고 userId가 반환됨
                    const userId = response.data;

                    // userId를 이용해 닉네임 가져오기
                    axios.get(`/chat/get-profile/${userId}`)
                        .then(function(response) {
                            const user = response.data;

                            // 프로필 사진 변경
                            if (user.profileImageUrl) {
                                $("#profile-image").attr("src", user.profileImageUrl); // 프로필 이미지 URL로 변경
                            }
                            // 닉네임 변경
                            $('#chat-nickname').val(user.nickname);
                            $('#nickname-length').text(user.nickname.length);

                            // 팝업창 보이기
                            $('#popup-section').fadeIn();
                            $('#toast-popup-box').css('transform', 'translateY(0)');
                        })
                        .catch(function(error) {
                            console.error("닉네임 로드 중 오류:", error);
                            alert("닉네임을 불러오지 못했습니다.");
                        });
                } else {
                    // 토큰이 없으면 로그인 요청
                    alert("로그인이 필요합니다!");
                    window.location.href = "/login?redirect=" + encodeURIComponent(window.location.href); // 로그인 후 원래 페이지로 돌아올 수 있게
                }
            })
            .catch(function(error) {
                console.error("토큰 확인 중 오류:", error);
                alert("토큰 검증에 실패했습니다. 다시 시도해주세요.");
            });
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

    // 옵션 선택 시 프로필 이미지 변경
    $('input[name="selectOne"]').on('change', function() {
        let selectedOption = $(this).val(); // 선택된 값 가져오기
        let profileImage = $("#profile-image");

        // 프로필 이미지가 기본 이미지일 경우에만 변경
        if (profileImage.attr("src").startsWith("/images/common/profile-")) {
            if (selectedOption === "blue") {
                // '부먹' 선택 시 블루 로고
                profileImage.attr("src", "/images/common/profile-blue.png"); // 블루 로고로 변경
            } else if (selectedOption === "neutral") {
                // '중립' 선택 시 기본 로고
                profileImage.attr("src", "/images/common/profile-default.png"); // 기본 로고로 변경
            } else if (selectedOption === "orange") {
                // '찍먹' 선택 시 오렌지 로고
                profileImage.attr("src", "/images/common/profile-orange.png"); // 오렌지 로고로 변경
            }
        }
    });

    // 채팅방 입장하기
    $('#btn-join-chat').on('click', function(){
        let postId = $(this).data('post-id'); // 데이터 속성 가져오기
        console.log("게시글 ID:", postId);

        // 닉네임이 설정 되어있는지 검증
        if($('#nickname-length').text() != '0'){
            // 세션 스토리지에 채팅방에서 필요한 정보 저장
            let nickname = $('#chat-nickname').val();
            let selectedOption = $('input[name="selectOne"]:checked').val();
            let profileImage = $('#profile-image').attr('src');

            // 이미지가 Base64로 되어 있으면, S3로 저장
            if (profileImage.startsWith('data:image')) {
                fetch('/chat/upload-profile-img', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'text/plain'  // 순수 String으로 보내기
                    },
                    body: profileImage // JSON.stringify() 사용하지 않음
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('서버 응답 실패');
                    }
                    return response.text(); // 응답이 단순 문자열일 경우 text() 사용
                })
                .then(data => {
                    console.log("업로드 성공:", data);

                    sessionStorage.setItem('nickname', nickname);
                    sessionStorage.setItem('selectedOption', selectedOption);
                    sessionStorage.setItem('profileImage', data);

                    // 채팅방으로 이동
                    window.location.href = "/chat/" + postId;
                })
                .catch(error => console.error('이미지 업로드 실패:', error));
            }else{
                sessionStorage.setItem('nickname', nickname);
                sessionStorage.setItem('selectedOption', selectedOption);
                sessionStorage.setItem('profileImage', profileImage);

                // 채팅방으로 이동
                window.location.href = "/chat/" + postId;
            }
        } else {
            alert("닉네임을 입력해주세요.");
        }
    });
});