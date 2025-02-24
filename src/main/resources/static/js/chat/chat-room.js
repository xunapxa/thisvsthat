$(document).ready(function() {
    let stompClient = null;
    let userId = $('#btn-send').data('user-id');
    let postId = $('#btn-send').data('post-id');
    console.log("userId : " + userId);

    // 제목 애니메이션 적용 함수 호출
    setTitleAnimation();

    // 웹소켓 연결
    connectWebSocket();

    function setTitleAnimation() {
        const chatTopic = $('#chat-topic');
        const wrapper = $('#chat-topic-wrapper');

        const textWidth = chatTopic[0]?.scrollWidth;  // 텍스트의 실제 너비
        const wrapperWidth = wrapper[0]?.offsetWidth;  // 슬라이드 영역의 너비
        const hiddenTextWidth = textWidth - wrapperWidth;  // 숨겨진 텍스트 길이

        // 텍스트 길이가 슬라이드 영역을 초과할 경우에만 애니메이션 적용
        if (textWidth > wrapperWidth) {
            const duration = (textWidth / 70) + 's';  // 텍스트 길이에 비례한 애니메이션 시간
            const keyframes = `
                @keyframes slide {
                    0% { transform: translateX(0); }
                    10% { transform: translateX(0); }  /* 시작 시 10% 동안 멈춤 */
                    90% { transform: translateX(-${hiddenTextWidth}px); }  /* 90% 구간까지 슬라이드 */
                    100% { transform: translateX(-${hiddenTextWidth}px); }  /* 끝나기 전에 10% 동안 멈춤 */
                }
            `;
            // 동적으로 생성된 keyframes를 head에 추가
            $('<style>').prop('type', 'text/css').html(keyframes).appendTo('head');
            // 애니메이션 시간 설정 및 슬라이드 애니메이션 클래스 추가
            chatTopic.css('animation-duration', duration).addClass('slide_animation');
        } else {
            // 텍스트가 영역을 초과하지 않으면 애니메이션 제거
            chatTopic.removeClass('slide_animation');
        }
    }

    // 채팅 입력창의 높이 자동 조정
    $('#message-input').on('input', function() {
        $(this).css('height', 'auto');  // 이전 높이 초기화
        $(this).css('height', this.scrollHeight + 'px');  // 내용에 맞게 높이 변경
    });

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
                profileImage: sessionStorage.getItem('profileImage'),
                nickname: sessionStorage.getItem('nickname'),
                selectedOption: sessionStorage.getItem('selectedOption'),
                content: message,
                sentTime: formattedTime
            };

            stompClient.send(`/pub/sendMessage/${postId}`, {}, JSON.stringify(chatMessage));
            $('#message-input').val('');  // 메시지 전송 후 입력 필드 초기화


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
            console.log("받은 메시지 : " + chatMessage)
            console.log("postId: ", postId);
            if (chatMessage.userId !== userId) {
                $('#message-list').append(`
                    <div class="other_message">
                        <div class="image_wrapper">
                            <img class="profile_image" src="${chatMessage.profileImage}"/>
                        </div>
                        <div class="message_wrapper">
                            <p class="chat_nickname">${chatMessage.nickname}</p>
                            <p class="message_box bg_${chatMessage.selectedOption}">
                                <span>${chatMessage.content}</span>
                            </p>
                        </div>
                    </div>
                `);
            }else{
                $('#message-list').append(`
                    <div class="my_message">
                        <p class="message_box bg_${chatMessage.selectedOption}">
                            <span>${chatMessage.content}</span>
                        </p>
                        <div class="image_wrapper">
                            <img class="profile_image" src="${chatMessage.profileImage}"/>
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
