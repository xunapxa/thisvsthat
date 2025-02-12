document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("signup-form");

    form.addEventListener("submit", function (event) {
        event.preventDefault();

        let isValid = true;
        const nicknameInput = document.getElementById("nickname");
        const birthdateInput = document.getElementById("birthdate");

        // 닉네임 유효성 검사
        if (nicknameInput.value.trim() === "") {
            setError(nicknameInput, "닉네임을 입력해주세요.");
            isValid = false;
        } else {
            clearError(nicknameInput);
        }

        // 생년월일 유효성 검사
        if (birthdateInput.value.trim() === "") {
            setError(birthdateInput, "생년월일을 입력해주세요.");
            isValid = false;
        } else {
            clearError(birthdateInput);
        }

        if (isValid) {
            form.submit(); // 폼 제출
        }
    });

    function setError(input, message) {
        const group = input.parentElement;
        group.classList.add("error");
        group.querySelector(".validation-message").textContent = message;
    }

    function clearError(input) {
        const group = input.parentElement;
        group.classList.remove("error");
        group.querySelector(".validation-message").textContent = "";
    }
});