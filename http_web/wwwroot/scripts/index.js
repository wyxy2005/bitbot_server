// Constants
var features = new Array(
    "Imagine payments without a midleman",
    "Investments without a broker",
    "Loans without a bank",
    "Insurance without an underwriter",
    "Charity without a trustee",
    "Escrow without an agent",
    "Betting without a bookie",
    "Record-keeping without an accountant.",
    "Decentralized & pseudonymous transaction like cash."
    );

// onReady
$(document).ready(function () {
    // Fade in effect
    // $("#title").fadeIn(1000);
    //$("#meetbitcoin").fadeIn(3000);

    // Schedule timer to update text
    setInterval(function () {
        onUpdateFeatureSlider();
    }, 5000);

    // Check for redirects from contact form submission
    var status = getQueryParam("status");
    if (status != null && status == "ok") {
        alert("Hello! Ms Leong");
    }
});

function getQueryParam(param) {
    var found;
    window.location.search.substr(1).split("&").forEach(function(item) {
        if (param ==  item.split("=")[0]) {
            found = item.split("=")[1];
        }
    });
    return found;
};

function onUpdateFeatureSlider() {
    // alert(this.features[Math.round(Math.random() * features.length)]);
    var jQueryElem = $("#h3_scrollingMessage");

    jQueryElem.fadeOut(500);
    jQueryElem.fadeIn(1000);

    document.getElementById("h3_scrollingMessage").innerText =
        features[Math.floor(Math.random() * features.length)];
}

function validateInputFields() {
    var contactForm = document.getElementById("contact-form");
    var name = contactForm["name"].value;
    var desc = contactForm["description"].value;
    var email = contactForm["email"].value;
  
    if (!validateInputFieldsInternal("Name", name) || !validateInputFieldsInternal("Desc", desc) || !validateInputFieldsInternal("Email", email)) {
        return false;
    } else if (!validateEmailFormat(email)) {
        alert("You must enter a valid email address.");
        return false;
    } else if (desc.length < 20) {
        alert("Please enter just a little more information regarding the feedback!");
        return false;
    }
    return true;
}

// Validates the basic input format.
function validateInputFieldsInternal(fieldName, field) {
    if (field != null && field != "") {
        return true;
    }
    alert(fieldName + " must be filled out");
    return false;
}

// Validates email format in client side 
function validateEmailFormat(emailAddress) {
    var sQtext = '[^\\x0d\\x22\\x5c\\x80-\\xff]';
    var sDtext = '[^\\x0d\\x5b-\\x5d\\x80-\\xff]';
    var sAtom = '[^\\x00-\\x20\\x22\\x28\\x29\\x2c\\x2e\\x3a-\\x3c\\x3e\\x40\\x5b-\\x5d\\x7f-\\xff]+';
    var sQuotedPair = '\\x5c[\\x00-\\x7f]';
    var sDomainLiteral = '\\x5b(' + sDtext + '|' + sQuotedPair + ')*\\x5d';
    var sQuotedString = '\\x22(' + sQtext + '|' + sQuotedPair + ')*\\x22';
    var sDomain_ref = sAtom;
    var sSubDomain = '(' + sDomain_ref + '|' + sDomainLiteral + ')';
    var sWord = '(' + sAtom + '|' + sQuotedString + ')';
    var sDomain = sSubDomain + '(\\x2e' + sSubDomain + ')*';
    var sLocalPart = sWord + '(\\x2e' + sWord + ')*';
    var sAddrSpec = sLocalPart + '\\x40' + sDomain; // complete RFC822 email address spec
    var sValidEmail = '^' + sAddrSpec + '$'; // as whole string

    var reValidEmail = new RegExp(sValidEmail);

    return reValidEmail.test(emailAddress);
}