$(document).ready(function() {
    $("choose_section").on("click", function(section){
        section.classList.toggle("admin_selected");
    });
    function submitForm(actionType) {
                let form = document.getElementById("postForm");
                let selectedSections = document.querySelectorAll(".choose_section.admin_selected");

                // 기존 hidden input 초기화
                document.getElementById("postIdsContainer").innerHTML = "";

                if (selectedSections.length === 0) {
                    alert("선택된 게시글이 없습니다.");
                    return;
                }

                selectedSections.forEach(section => {
                    let postId = section.getAttribute("data-post-id");
                    let input = document.createElement("input");
                    input.type = "hidden";
                    input.name = "postIds";
                    input.value = postId;
                    document.getElementById("postIdsContainer").appendChild(input);
                });

                // 액션 타입 설정 (복구 또는 삭제)
                document.getElementById("actionTypeInput").value = actionType;

                form.submit();
            };
})