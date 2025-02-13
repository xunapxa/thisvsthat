document.addEventListener("DOMContentLoaded", function () {

    const nameField = document.getElementById("nickname");
    const birthdateField = document.getElementById("birthdate");
    const signupForm = document.getElementById("signup-form");
    const submitButton = document.getElementById("submit-button");

    let nicknameCheckTimer = null; // 닉네임 중복 검사 타이머
    let isNicknameValid = false; // 닉네임 중복 검사 결과

    let defaultMessageNickname = "한글, 영문, 숫자, 언더바(_), 하이픈(-), 공백";
    let defaultMessageBirthdate = "예: 1995-07-24";

    // 한글 디코딩 처리 (닉네임)
    if (nameField) {
        nameField.value = decodeURIComponent(nameField.value);
    }

    // 닉네임 유효성 검사
    function validateNickname(nickname) {
        const nicknameRegex = /^[a-zA-Z0-9가-힣 _-]+$/;
        return nickname.length >= 2 && nickname.length <= 20 && nicknameRegex.test(nickname);
    }

    // 생년월일 유효성 검사
    function validateBirthdate(birthdate) {
        const birthdateRegex = /^\d{4}-\d{2}-\d{2}$/;
        return birthdateRegex.test(birthdate) && isValidDate(birthdate);
    }

    function isValidDate(dateString) {
        const [year, month, day] = dateString.split("-").map(Number);
        const date = new Date(year, month - 1, day);

        // 연도 범위 제한 (예: 1900년 ~ 현재 연도)
        const minYear = 1900;
        const maxYear = new Date().getFullYear();

        return (
            date.getFullYear() === year &&
            date.getMonth() === month - 1 &&
            date.getDate() === day &&
            year >= minYear &&
            year <= maxYear
        );
    }

    // 닉네임 중복 검사 (AJAX 요청)
    async function checkNicknameDuplicate(nickname) {
        console.log("📌 닉네임 중복 검사 시작:", nickname);
        try {
            const response = await fetch(`/auth/check-nickname?nickname=${encodeURIComponent(nickname)}`);
            const result = await response.json();
            console.log("📌 닉네임 중복 검사 결과(true면 중복): ", result.duplicate);
            return result.duplicate; // true: 중복된 닉네임
        } catch (error) {
            console.error("닉네임 중복 검사 요청 실패:", error);
            return false;
        }
    }

    // 오류 스타일 적용 함수
    function showError(inputField, message) {
        const inputGroup = inputField.closest(".input-group");
        const guide = inputGroup.querySelector(".input-guide");
        const underline = inputGroup.querySelector(".input-underline");

        guide.textContent = message;
        guide.classList.add("error");
        underline.classList.add("error");
    }

    // 오류 스타일 제거 함수
    function clearError(inputField, defaultMessage) {
        const inputGroup = inputField.closest(".input-group");
        const guide = inputGroup.querySelector(".input-guide");
        const underline = inputGroup.querySelector(".input-underline");

        guide.textContent = defaultMessage;
        guide.classList.remove("error");
        underline.classList.remove("error");
    }

    // 닉네임 입력 감지
    nameField.addEventListener("input", function () {
        const nickname = this.value.trim();
        clearTimeout(nicknameCheckTimer);

        // 닉네임 유효성 검사
        if (!validateNickname(nickname)) {
            showError(nameField, "닉네임은 2~20자, 한글/영문/숫자/_/- 만 사용 가능합니다.");
            isNicknameValid = false;
            toggleSubmitButton();
            return;
        } else {
            clearError(nameField, defaultMessageNickname);
        }

        // 닉네임 중복 검사 (디바운싱 적용)
        nicknameCheckTimer = setTimeout(async () => {
            const isDuplicate = await checkNicknameDuplicate(nickname);
            if (isDuplicate) {
                showError(nameField, "이미 사용 중인 닉네임입니다.");
                isNicknameValid = false;
            } else {
                clearError(nameField, defaultMessageNickname);
                isNicknameValid = true;
            }
            toggleSubmitButton();
        }, 500);
    });

    // 생년월일 입력 감지
    birthdateField.addEventListener("input", function () {
        if (!validateBirthdate(this.value.trim())) {
            showError(this, "생년월일은 YYYY-MM-DD 형식이며, 유효한 날짜여야 합니다.");
        } else {
            clearError(this, defaultMessageBirthdate);
        }
    });

    // 제출 버튼 활성화/비활성화
    function toggleSubmitButton() {
        submitButton.disabled = !isNicknameValid;
    }

    // 회원가입 폼 제출 이벤트
    if (signupForm) {
        signupForm.addEventListener("submit", async function(event) {
            event.preventDefault();

            const nicknameValue = nameField.value.trim();
            const birthdateValue = birthdateField.value.trim();

            let isValid = true;

            // 닉네임 유효성 검사
            if (!validateNickname(nicknameValue)) {
                showError(nameField, "닉네임은 2~20자, 한글/영문/숫자/_/- 만 사용 가능합니다.");
                nameField.focus();
                isValid = false;
            }

            // 생년월일 유효성 검사
            if (!validateBirthdate(birthdateValue)) {
                showError(birthdateField, "생년월일은 YYYY-MM-DD 형식이며, 유효한 날짜여야 합니다.");
                birthdateField.focus();
                isValid = false;
            }

            if (!isValid) return;

            // 닉네임 중복 검사 (최종 확인)
            const isDuplicate = await checkNicknameDuplicate(nicknameValue);
            if (isDuplicate) {
                showError(nameField, "이미 사용 중인 닉네임입니다.");
                nameField.focus();
                return;
            }

            // 회원가입 요청 전송
            const formData = new FormData(this);
            console.log("📌 회원가입 요청 데이터:", new URLSearchParams(formData).toString());

            submitButton.disabled = true; // 중복 요청 방지

            try {
                const response = await fetch("/auth/signup", {
                    method: "POST",
                    body: new URLSearchParams(formData),
                    headers: { "Content-Type": "application/x-www-form-urlencoded" }
                });

                const result = await response.json();
                console.log("📌 서버 응답:", result);

                if (response.ok) {
                    localStorage.setItem("token", result.token);
                    alert("회원가입 완료! 메인 페이지로 이동합니다.");
                    window.location.href = "/";
                } else {
                    alert(result.message || "회원가입 실패");
                    submitButton.disabled = false; // 실패 시 버튼 다시 활성화
                }
            } catch (error) {
                console.error("❌ 회원가입 요청 실패:", error);
                alert("서버 오류 발생. 다시 시도해주세요.");
                submitButton.disabled = false;
            }
        });
    }
});