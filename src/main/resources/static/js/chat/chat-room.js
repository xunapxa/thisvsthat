$(document).ready(function() {
    let stompClient = null;
    let userId = $('#btn-send').data('user-id');
    let postId = $('#btn-send').data('post-id');
    console.log("userId : " + userId);

    // 채팅 입력창의 높이 자동 조정
    $('#message-input').on('input', function() {
        $(this).css('height', 'auto');  // 이전 높이 초기화
        $(this).css('height', this.scrollHeight + 'px');  // 내용에 맞게 높이 변경
    });

    // 웹소켓 연결
    connectWebSocket();

    // 채팅방 입장 시 기존의 채팅 50개 리스트로 출력
    function showMessage(message) {
        $('#messageList').append('<div>' + message + '</div>');
    }

    // 채팅 메시지 전송
    $('#btn-send').click(function() {
        let message = $('#message-input').val();
        if (message && stompClient) {
            let now = new Date();
            let formattedTime = now.getHours().toString().padStart(2, '0') + ':' +
                                now.getMinutes().toString().padStart(2, '0') + ':' +
                                now.getSeconds().toString().padStart(2, '0');  // HH:mm:ss 형식

            let chatMessage = {
                userId: userId,
                postId: postId,
                profileImg: sessionStorage.getItem('profileImg'),
                nickname: sessionStorage.getItem('nickname'),
                selectedOption: sessionStorage.getItem('selectedOption'),
                content: message,
                sentTime: formattedTime
            };

            stompClient.send(`/pub/sendMessage/${postId}`, {}, JSON.stringify(chatMessage));
            $('#message-input').val('');  // 메시지 전송 후 입력 필드 초기화

            $('#message-list').append(`
                <div class="my_message">
                    <p class="message_box bg_${chatMessage.selectedOption}">${message}</p>
                    <div class="image_wrapper">
                        <img class="profile_image" src="${chatMessage.profileImg}">
                    </div>
                </div>
            `);
        }
    });

    // 웹소켓 연결
    function connectWebSocket() {
        let socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function(frame) {
            console.log('🔗연결 성공: ' + frame);
            // 채팅방 구독
            subscribeToChatRoom();
            // 채팅방 입장 시 인원 수 알림
            stompClient.send(`/pub/join/${postId}`);
        }, function(error) {
            console.error('⛓️‍💥서버 연결 실패: ', error);
        });
    }

    // 채팅방 구독
    function subscribeToChatRoom() {
        stompClient.subscribe(`/sub/chatroom/${postId}`, function(response) {
            let chatMessage = JSON.parse(response.body);
            if (chatMessage.userId !== sessionStorage.getItem('userId')) {
                $('#message-list').append(`
                    <div class="other_message">
                        <div class="image_wrapper">
                            <img class="profile_image" src="${chatMessage.profileImg}" />
                        </div>
                        <div class="message_wrapper">
                            <p class="chat_nickname">${chatMessage.nickname}</p>
                            <p class="message_box bg_${chatMessage.selectedOption}">
                                <span th:text="${chatMessage.content}">${chatMessage.content}</span>
                            </p>
                        </div>
                    </div>
                `);
            }
            scrollToBottom();
        }, function(error) {
            console.error('구독 오류:', error);
        });

        // 인원 수 업데이트 메시지 구독
        stompClient.subscribe(`/sub/chatroom/userCount/${postId}`, function(response) {
            let data = JSON.parse(response.body);
            let userCount = data.userCount;  // "현재 채팅 인원: x" 형태
            userCount = userCount.replace(/\D/g, '');  // 숫자만 추출 (숫자 이외의 문자 제거)
            console.log("인원수: " + userCount);
            $('#user-count').text(userCount);  // 인원 수 표시
        });
    }

    // 퇴장 시 서버에 퇴장 메시지 보내기
    window.onbeforeunload = function() {
        stompClient.send(`/pub/leave/${postId}`, {}, "");
    };

    // 스크롤을 채팅 창 맨 아래로 이동
    function scrollToBottom() {
        $('#message-list').scrollTop($('#message-list')[0].scrollHeight);
    }
});
