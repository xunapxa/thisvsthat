document.addEventListener("DOMContentLoaded", function () {
        let errorMsgElement = document.getElementById("errorMsg");
        if (errorMsgElement && errorMsgElement.textContent.trim() !== "") {
            alert(errorMsgElement.textContent); // 알림 창 표시
            document.getElementById("keywordInput").value = ""; // 입력 필드 리셋
        }
    });

let selectedUserId = null;

function toggleSelection(section) {
    section.classList.toggle("admin_selected");
}

function toggleSelectionUser(sectionUsers) {
    const $section = $(sectionUsers);

    if ($section.hasClass("admin_selected")) {
        $section.removeClass("admin_selected");
        console.log("Removed admin_selected from:", $section);
        $("#adminPopup").hide();
        $("body").css("overflow", "auto");
        return;
    }

    $section.addClass("admin_selected");
    console.log("Added admin_selected to:", $section);

    selectedUserId = $section.attr("data-user-id");
    $("#reportUserIdInput").val(selectedUserId);
    $("#adminPopup").css("display", "flex");
    $("body").css("overflow", "hidden");

    $.ajax({
            url: "/admin/reported-posts",
            method: "GET",
            data: { reportUserId: selectedUserId },
            success: function (response) {
                const extractedHtml = $(response).find("#reportedPostsContainer").html();
                $("#reportedPostsContainer").html(extractedHtml);
                $("#adminPopup").css("display", "flex");
                $("body").css("overflow", "hidden");
            },
            error: function () {
                alert("데이터를 불러오는 중 오류가 발생했습니다.");
            }
        });
}

function closePopup() {
    const $popup = $("#adminPopup");
    const $userElement = $(`.bastard_user[data-user-id='${selectedUserId}']`);
    console.log("closePopup" + selectedUserId);

    $userElement.removeClass("admin_selected");

    $popup.hide();
    $("body").css("overflow", "auto");
    selectedUserId = null;
    return;
}

// 팝업 바깥 영역 클릭 시 닫기 기능 추가
$(document).on("click", ".dim_box", function () {
    closePopup();
});

function confirmSelection() {
    const $popup = $("#adminPopup");
    const $userElement = $(`.bastard_user[data-user-id='${selectedUserId}']`);

    if (!$userElement.hasClass("admin_selected")) {
        $userElement.addClass("admin_selected");
    }

    $popup.hide();
    $("body").css("overflow", "auto");
}

function submitPostForm(postActionType) {
    let $form = $("#postForm");
    let $selectedSections = $(".report_post.admin_selected");
    let $postIdsContainer = $("#postIdsContainer");

    $postIdsContainer.empty();

    if ($selectedSections.length === 0) {
        alert("선택된 게시글이 없습니다.");
        return;
    }

    let confirmMessage = (postActionType === "restore") ? "복구하시겠습니까?" : "삭제하시겠습니까?";
    if (!confirm(confirmMessage)) {
        return;
    }

    $selectedSections.each(function () {
        let postId = $(this).attr("data-post-id");
        let $input = $("<input>", {
            type: "hidden",
            name: "postIds",
            value: postId
        });
        $postIdsContainer.append($input);
    });

    $("#postActionTypeInput").val(postActionType);
    $form.submit();
}

function submitUserForm(userActionType) {
    let $form = $("#userForm");
    let $selectedUsers = $(".bastard_user.admin_selected");
    let $userIdsContainer = $("#userIdsContainer");

    $userIdsContainer.empty();

    if ($selectedUsers.length === 0) {
        alert("선택된 회원이 없습니다.");
        return;
    }

    let confirmMessage = (userActionType === "restoreUsers") ? "선택된 유저를 복구하시겠습니까?" : "선택된 유저를 차단하시겠습니까?";
    if (!confirm(confirmMessage)) {
        return;
    }

    $selectedUsers.each(function () {
        let userId = $(this).attr("data-user-id");
        let $input = $("<input>", {
            type: "hidden",
            name: "userIds",
            value: userId
        });
        $userIdsContainer.append($input);
    });

    $("#userActionTypeInput").val(userActionType);
    $form.submit();
}
