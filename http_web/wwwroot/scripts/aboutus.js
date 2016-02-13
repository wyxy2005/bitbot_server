// onReady
$(document).ready(function () {
    // Fade in effect
    $("#title").fadeIn(1000);
});

function showAppScreenshotGallery() {
    // Initialize photoswipe
    var pswpElement = document.querySelectorAll('.pswp')[0];

    // build items array
    var items = [];
    for (var i = 1; i <= 13; i++) {
        items.push({
            src: 'assets/img/wp_ss' + i + '.png',
            w: 1080,
            h: 1920
        });
    }


    // define options (if needed)
    var options = {
        // optionName: 'option value'
        // for example:
        index: 0 // start at first slide
    };

    // Initializes and opens PhotoSwipe
    var gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);
    gallery.init();
}