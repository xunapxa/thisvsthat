$(document).ready(function() {
    let stompClient = null;
    let userId = $('#btn-send').data('user-id');
    let postId = $('#btn-send').data('post-id');
    console.log("userId : " + userId);

    const maxLength = 500; // 제한할 글자 수
    const messageInput = $('#message-input'); // 입력 필드
    const messageContainer = $('#message-input-container'); // 입력 필드 부모
    const chatContainer = $('#chat-container'); // 주고 받은 메시지 컨테이너

    setTitleAnimation();
    connectWebSocket();
    keepScrollAtBottom();

    // 뒤로가기
    $("#btn-back").click(function() {
        window.history.back();
    });

    // 제목 애니메이션 적용 함수
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

    let lastReloadTime = new Date(); // 마지막 투표 데이터 갱신 시간

    function getTimeDifference(startTime) {
        const now = new Date();
        const diffInSeconds = Math.floor((now - startTime) / 1000);

        const minutes = Math.floor(diffInSeconds / 60);
        const seconds = diffInSeconds % 60;

        if (minutes === 0) {
            return `${seconds}초 전`;
        } else {
            return `${minutes}분 전`;
        }
    }

    function updateTime() {
        const timeText = getTimeDifference(lastReloadTime);
        $('#last-reload-time').text(timeText);

        // 자동 리로드: 마지막 갱신 이후 10분(600초)이 지나면 자동으로 갱신
        if (Math.floor((new Date() - lastReloadTime) / 1000) >= 600) {
            reloadVoteData();
        }
    }

    // 1초마다 시간 업데이트
    setInterval(updateTime, 1000);

    // 버튼 회전 애니메이션 함수
    function rotateButton(button) {
        $(button).addClass('rotate');  // 버튼에 회전 클래스 추가

        // 회전 애니메이션 끝난 후 클래스 제거 (다시 회전할 수 있도록)
        setTimeout(() => {
            $(button).removeClass('rotate');
        }, 1000);  // 애니메이션 시간이 1초이므로 1초 후에 클래스 제거
    }

    // 투표 데이터 갱신 함수
    function reloadVoteData() {
        axios.get(`/api/votes/${postId}`)
            .then(function (response) {
                const voteResult = response.data;
                const option1Percentage = voteResult.option1Percentage;
                const option2Percentage = voteResult.option2Percentage;
                const totalVotes = voteResult.totalVotes;

                rotateButton($("#btn-vote-reload"));
                $("#vote-count").text(totalVotes);
                $("#blue_bar").css("width", option1Percentage + "%");
                $("#orange-bar").css("width", option2Percentage + "%");

                if (option1Percentage > 6) {
                    $("#blue_bar").html(`<span>${option1Percentage.toFixed(0)}%</span>`);
                } else {
                    $("#blue_bar").html("");
                }

                if (option2Percentage > 6) {
                    $("#orange-bar").html(`<span>${option2Percentage.toFixed(0)}%</span>`);
                } else {
                    $("#orange-bar").html("");
                }

                // 리로드 후 마지막 갱신 시간 업데이트
                lastReloadTime = new Date();
                $("#last-reload-time").text("방금 전");
            })
            .catch(function (error) {
                console.error("투표율 리로드 실패:", error);
            });
    }

    // 리로드 버튼 클릭 시 수동 갱신
    $("#btn-vote-reload").click(function () {
        reloadVoteData();
    });

    // 접어두기 버튼 클릭 시
    $("#collapse-btn").click(function () {
        var voteRate = $(this).closest('#vote-rate');
        var messageList = $('#message-list'); // 메시지 리스트 선택

        // collapsed 클래스 추가/제거
        $(this).toggleClass('collapsed');
        voteRate.toggleClass('collapsed');

        if (voteRate.hasClass('collapsed')) {
            messageList.css('padding-top', '0'); // 접혀 있으면 없애기
        } else {
            messageList.css('padding-top', '65px'); // 펼쳐져 있으면 설정
        }

        // hidden 클래스 추가/제거
        voteRate.find('#vote-reload-wrapper, #vote-bar').toggleClass('hidden');
    });

    // 가장 최근 메시지가 보이게 스크롤 하단 유지
    function keepScrollAtBottom() {
        chatContainer.scrollTop(chatContainer[0].scrollHeight);
    }

    // 입력 필드 높이 조절 함수
    function adjustInputHeight() {
        let inputHeight = messageInput.height();

        if (inputHeight <= 150) {
            messageContainer.css('height', `${inputHeight + 30}px`);
            let newChatContainerHeight = `calc(100vh - var(--height-header) - var(--height-chat-header) - ${inputHeight + 30}px)`;
            chatContainer.css('height', newChatContainerHeight);
        } else {
            messageInput.css('overflow-y', 'scroll');
        }
    }

    // 500자 제한 함수
    function limitTextLength() {
        let text = messageInput.text();

        if (text.length > maxLength) {
            messageInput.text(text.substring(0, maxLength)); // 500자까지만 유지

            // 커서를 맨 뒤로 이동
            let range = document.createRange();
            let sel = window.getSelection();
            range.selectNodeContents(messageInput[0]);
            range.collapse(false);
            sel.removeAllRanges();
            sel.addRange(range);
        }
    }

messageInput.on('input change', function () {
    limitTextLength(); // 500자 제한
    adjustInputHeight(); // 입력 필드 높이

    // 내용이 비었으면, empty 클래스를 추가
    if ($(this).text().trim() === "") {
        $(this).addClass('empty');
    } else {
        $(this).removeClass('empty');
    }
});

    // 입력 필드 초기화 및 높이 복원 함수
    function resetInputField() {
        $('#message-input').text('');  // 메시지 전송 후 입력 필드 초기화

        // 전송 후 입력창 높이를 기본값으로 복원
        $('#message-input').css('height', '30px');

        // 부모 요소 높이 기본값으로 복원
        $('#message-input-container').css('height', 'var(--height-chat-input)');

        // message-container 높이 복원
        $('#message-container').css('height', `calc(100vh - var(--height-header) - var(--height-chat-header) - var(--height-chat-input))`);
    }

    // 채팅 메시지 전송
    $('#btn-send').click(function(e) {
        let message = $('#message-input').text();
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
        console.log("postId: ", postId);
        stompClient.subscribe(`/sub/chatroom/${postId}`, function(response) {
            let chatMessage = JSON.parse(response.body);
            console.log("받은 메시지 : ", chatMessage);

            // 에러 메시지가 포함된 경우 처리
            if (chatMessage.error) {
                // 로그인 정보가 없을 때
                if (chatMessage.error.includes("로그인 정보가 없습니다")) {
                    alert(chatMessage.error);
                    window.location.href = "/login?redirect=" + encodeURIComponent(window.location.href);
                }
                // 스팸 메시지 처리
                else if (chatMessage.error.includes("부적절한 단어가 포함되어 있습니다")) {
                    alert(chatMessage.error);
                }
                return;  // 에러 처리 후 더 이상 진행하지 않음
            }else{
                resetInputField(); // 입력 필드 초기화
            }

            // 정상 메시지 처리
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
            } else {
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
            keepScrollAtBottom();
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
});