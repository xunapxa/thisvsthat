//투표한 게시물 중 선택한 옵션 표시하기
const fieldsetLabels = document.querySelectorAll('fieldset label');

fieldsetLabels.forEach((label) => {
    const radioColor = label.getAttribute('data-radio-color');
    label.style.setProperty('--radio-border-color', `#${radioColor}`);
    label.style.setProperty('--radio-background-color', `#${radioColor}`);
});

// 뒤로 가기 버튼 클릭 시 뒤로 가기
document.addEventListener("DOMContentLoaded", function () {
    const backButton = document.getElementById("backButton");

    if (backButton) {
        backButton.addEventListener("click", function () {
            if (window.history.length > 1) {
                // 뒤로 갈 페이지가 있으면 뒤로 가기
                window.history.back();
            } else {
                // 뒤로 갈 페이지가 없으면 메인 페이지로 이동
                window.location.href = "/";
            }
        });
    }
});

$(document).ready(function() {
    const nicknameField = $("#nickname");
    let isNicknameValid = false;

    // 유효성 검사 메시지
    const validationMessageNickname = "닉네임은 2~20자, 한글/영문/숫자/_/- 만 사용 가능합니다.";
    const errorMessageNicknameUsed = "이미 사용 중인 닉네임입니다.";

    // 닉네임 유효성 검사 함수
    function validateNickname(nickname) {
        const trimmedNickname = nickname.trim(); // 앞뒤 공백 제거
        const nicknameRegex = /^[a-zA-Z0-9가-힣 _-]+$/;
        return trimmedNickname.length >= 2 && trimmedNickname.length <= 20 && nicknameRegex.test(trimmedNickname);
    }

    // 닉네임 중복 검사 (서버에 요청)
    async function checkNicknameDuplicate(nickname) {
        try {
            const encodedNickname = encodeURIComponent(nickname); // 닉네임 인코딩
            const response = await fetch(`/auth/check-nickname?nickname=${encodedNickname}`);
            const result = await response.json();
            return result.duplicate; // 중복이면 true 반환
        } catch (error) {
            // console.error("닉네임 중복 검사 실패:", error);
            return false;
        }
    }

    // 닉네임 폼 제출 이벤트
    $("#nicknameForm").submit(async function(event) {
        event.preventDefault(); // 폼 제출 시 페이지 리로드 방지

        const nickname = nicknameField.val().trim();

        // 닉네임 유효성 검사
        if (!validateNickname(nickname)) {
            showMessage(validationMessageNickname, false); // 유효성 실패 메시지 표시
            nicknameField.focus();
            return;
        }

        // 닉네임 중복 검사
        const isDuplicate = await checkNicknameDuplicate(nickname);
        if (isDuplicate) {
            showMessage(errorMessageNicknameUsed, false); // 중복 메시지 표시
            nicknameField.focus();
            return;
        }

        // 닉네임 변경 요청
        $.ajax({
            url: "/users", // PATCH 매핑에 맞는 URL
            type: "PATCH", // 요청 방식은 PATCH
            data: { nickname },
            success: function(response) {
               // 요청 성공 시
                if (response.success) {
                    showMessage("닉네임 변경이 완료되었습니다.", true); // 성공 메시지 표시
                    nicknameField.val(response.updatedNickname); // 수정된 닉네임 반영
                } else {
                    showMessage("닉네임 변경이 실패하였습니다.", false); // 실패 메시지 표시
                }
            },
            error: function(xhr, status, error) {
                // 오류 발생 시
                showMessage("서버 오류가 발생했습니다.", false); // 서버 오류 메시지 표시
            }
        });
    });

    // 메시지 표시 함수
    function showMessage(message, isSuccess) {
        const messageElement = $("#message");
        messageElement.text(message); // 메시지 설정

        // 클래스를 초기화하고, success 또는 error 클래스 추가
        messageElement.removeClass("success error").addClass(isSuccess ? "success" : "error");
    }

    // 탈퇴하기 버튼 클릭 시
    $("#withdrawnBtn").click(function () {
        if (confirm("정말 탈퇴하시겠습니까?")) {
            let $btn = $(this);
            let $message = $("#withdrawnMessage");

            // 로딩 상태 표시
            $btn.prop("disabled", true).text("탈퇴 처리 중...");

            $.ajax({
                url: "/users/withdrawn", // 탈퇴 API 경로
                type: "PATCH", // PATCH 요청
                success: function (response) {
                    if (response.success) {
                        $message.text("회원 탈퇴가 완료되었습니다. 잠시 후 로그아웃됩니다.").css("color", "#858585");
                        $btn.text("탈퇴 완료").prop("disabled", true);

                        // 예시: 3초 후 메인 페이지로 이동 (원하는 페이지로 수정 가능)
                        setTimeout(() => {
                            window.location.href = "/logout";
                        }, 3000);
                    } else {
                        $message.text("회원 탈퇴가 실패하였습니다.").css("color", "red");
                        $btn.prop("disabled", false).text("탈퇴하기"); // 버튼 복구
                    }
                },
                error: function () {
                    $message.text("서버 오류가 발생했습니다.").css("color", "red");
                    $btn.prop("disabled", false).text("탈퇴하기"); // 버튼 복구
                }
            });
        }
    });
});