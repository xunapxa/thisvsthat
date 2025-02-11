$(document).ready(function() {
    function openPopup() {
        $('#popup-section').fadeIn();
        $('#toast-popup-box').css('transform', 'translateY(0)');
    }

    function closePopup() {
        $('#popup-section').fadeOut();
        $('#toast-popup-box').css('transform', 'translateY(100%)');
    }

    $('#open-chat-profile-popup').on('click', function() {
        openPopup();
    });

    $('#close-chat-profile-popup').on('click', function(e) {
        e.preventDefault();
        closePopup();
    });
});