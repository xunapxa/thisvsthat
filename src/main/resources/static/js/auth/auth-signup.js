document.addEventListener("DOMContentLoaded", function () {

    const profileImage = document.getElementById("profileImage");
    const editIcon = document.getElementById("editIcon");
    const profileImageInput = document.getElementById("profileImageInput");
    const profileImageUrlInput = document.getElementById("profileImageUrl");

    // 프로필 이미지 또는 편집 아이콘 클릭 시 파일 입력창 열기
    profileImage.addEventListener("click", () => profileImageInput.click());
    editIcon.addEventListener("click", () => profileImageInput.click());

    // 파일 선택 시 프로필 이미지 변경
    profileImageInput.addEventListener("change", function (event) {
        updateProfileImage(event);
    });

    // 선택한 파일을 프로필 이미지로 미리보기만 하고, 실제 회원가입 시 함께 전송
    function updateProfileImage(event) {
        const file = event.target.files[0]; // 선택한 파일 가져오기
        if (file) {
            const reader = new FileReader(); // FileReader 객체 생성
            reader.onload = function (e) {
                profileImage.src = e.target.result; // 이미지 미리보기
            };
            reader.readAsDataURL(file); // 파일을 Data URL로 변환하여 읽기
        }
    }

    const nameField = document.getElementById("nickname");
    const birthdateField = document.getElementById("birthdate");
    const signupForm = document.getElementById("signup-form");
    const submitButton = document.getElementById("submit-button");

    let nicknameCheckTimer = null; // 닉네임 중복 검사 타이머
    const nicknameCache = new Map(); // 닉네임 검사 결과 캐싱
    let isNicknameValid = false; // 닉네임 중복 검사 결과

    // 기본 메시지
    const defaultMessageNickname = "한글, 영문, 숫자, 언더바(_), 하이픈(-), 공백";
    const defaultMessageBirthdate = "예: 1995-07-24";

    // 유효성 검사 메시지
    const validationMessageNickname = "닉네임은 2~20자, 한글/영문/숫자/_/- 만 사용 가능합니다.";
    const validationMessageBirthdate = "생년월일은 YYYY-MM-DD 형식이며, 유효한 날짜여야 합니다.";

    // 에러 메시지
    const errorMessageNicknameUsed = "이미 사용 중인 닉네임입니다.";

    // 한글 디코딩 처리 (닉네임)
    if (nameField) {
        nameField.value = decodeURIComponent(nameField.value);
    }

    // 닉네임 유효성 검사
    function validateNickname(nickname) {
        const trimmedNickname = nickname.trim(); // 앞뒤 공백 제거
        const nicknameRegex = /^[a-zA-Z0-9가-힣 _-]+$/;
        return trimmedNickname.length >= 2 && trimmedNickname.length <= 20 && nicknameRegex.test(trimmedNickname);
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

    // 서버에 닉네임 중복 검사 요청 (캐싱 적용)
    async function checkNicknameDuplicate(nickname) {
        console.log("📌 닉네임 중복 검사 시작:", nickname);

        // 1. 캐시 확인 (이미 검사한 닉네임이면 API 요청 없이 결과 반환)
        if (nicknameCache.has(nickname)) {
            console.log("📌 캐시 사용: ", nickname, "→", nicknameCache.get(nickname));
            return nicknameCache.get(nickname);
        }

        try {
            const response = await fetch(`/auth/check-nickname?nickname=${encodeURIComponent(nickname)}`);
            const result = await response.json();
            console.log("📌 서버 응답 (중복 여부): ", result.duplicate);

            // 2. 검사 결과 캐싱
            nicknameCache.set(nickname, result.duplicate);

            return result.duplicate;
        } catch (error) {
            console.error("❌ 닉네임 중복 검사 실패:", error);
            return false;
        }
    }

    // 닉네임 입력 감지 (디바운싱 적용)
    nameField.addEventListener("input", async function () {
        const nickname = this.value.trim();
        clearTimeout(nicknameCheckTimer);

        // 1. 닉네임 유효성 검사
        if (!validateNickname(nickname)) {
            showError(nameField, validationMessageNickname);
            isNicknameValid = false;
            toggleSubmitButton();
            return;
        }

        // 닉네임이 유효하면 오류 제거 (UI 업데이트)
        clearError(nameField, defaultMessageNickname);

        // 2. 닉네임 중복 검사 (디바운싱 적용)
        nicknameCheckTimer = setTimeout(async () => {
            const isDuplicate = await checkNicknameDuplicate(nickname);
            if (isDuplicate) {
                showError(nameField, errorMessageNicknameUsed);
                isNicknameValid = false;
            } else {
                clearError(nameField, defaultMessageNickname);
                isNicknameValid = true;
            }
            toggleSubmitButton();
        }, 300);
    });

    // 생년월일 입력 감지
    birthdateField.addEventListener("input", function () {
        if (!validateBirthdate(this.value.trim())) {
            showError(this, validationMessageBirthdate);
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
                showError(nameField, validationMessageNickname);
                nameField.focus();
                isValid = false;
            }

            // 생년월일 유효성 검사
            if (!validateBirthdate(birthdateValue)) {
                showError(birthdateField, validationMessageBirthdate);
                birthdateField.focus();
                isValid = false;
            }

            if (!isValid) return;

            // 닉네임 중복 검사 (최종 확인)
            const isDuplicate = await checkNicknameDuplicate(nicknameValue);
            if (isDuplicate) {
                showError(nameField, errorMessageNicknameUsed);
                nameField.focus();
                return;
            }

            // FormData 객체 생성
            const formData = new FormData(this);

            // 프로필 이미지가 수정된 경우, 파일을 추가 (이미지 수정 시)
            const profileImageFile = profileImageInput.files[0];
            if (profileImageFile) {
                console.log(profileImageFile);  // 파일 정보 출력
                formData.append("profileImageFile", profileImageFile); // 이미지 파일 추가
            }

            // FormData를 순차적으로 출력하기
            const formDataObject = {};
            formData.forEach((value, key) => {
                formDataObject[key] = value;
                console.log(`📌 Key: ${key}, Value: ${value}`);
            });

            submitButton.disabled = true; // 중복 요청 방지

            try {
                const response = await fetch("/auth/signup", {
                    method: "POST",
                    body: formData, // FormData 객체 그대로 전송
                });

                const result = await response.json();
                console.log("📌 서버 응답:", result);

                if (response.ok) {
                    // 백엔드에서 제공한 redirectUrl로 이동
                    window.location.href = result.redirectUrl;
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
    }
});