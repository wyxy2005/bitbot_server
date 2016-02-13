/*
 * The list of scripts that will be commonly used by every 
 * single page of the website.
 *
 */

// onReady
$(document).ready(function () {
    // Initialize WOW animation effect
    new WOW().init();
});

var _navigatePage = null;
function fadeOutPage(page) {
    // Set the page navigation
    _navigatePage = page;

    // Fade out effect
    $('body,html').fadeOut(500);

    // Schedule 1 second to navigate to the page
    var myVar;
    myVar = setInterval(function () {
        window.clearTimeout(myVar);
        if (_navigatePage != null) {
            window.location = _navigatePage;
            _navigatePage = null;
        }
    }, 500);
}

var isNavigationShown = false;
function onManburgerMenuTapped() {
    var navObject = document.getElementById("col0_hamburgerMenu");

    if (isNavigationShown) {
        navObject.style.display = "none";
    } else {
        navObject.style.display = "table";
    }
    isNavigationShown = !isNavigationShown;
}