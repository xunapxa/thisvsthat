$(document).ready(function() {
    $('.chat_input').on('input', function() {
        $(this).css('height', 'auto');  // 이전 높이 초기화
        $(this).css('height', this.scrollHeight + 'px');  // 내용에 맞게 높이 변경
    });
});
