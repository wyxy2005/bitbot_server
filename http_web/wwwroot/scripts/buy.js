var exchangesList = [
    ["SINGAPORE",
        [
            "CoinHako", ["logo_coinhako.png", "https://coinhako.com"],
            "Bitcoin Exchange", ["sg_vendingmachine.jpg", "https://coinhako.com"],
            "ItBit", ["itBit-cover-logo.png", "https://www.itbit.com/"],
            "FybSG", ["fybsg.png", "http://fybsg.com"],
            "Local Bitcoins", ["logo_localbitcoin.png", "https://localbitcoins.com/country/SG"],
            "Coin Republic", ["logo_coinrepublic.jpg", "http://coinrepublic.com"]
        ]
    ],
    ["USA",
        [
            "Coinbase", ["logo_coinbase.jpg", "https://www.coinbase.com/"],
            "ItBit", ["itBit-cover-logo.png", "https://www.itbit.com/"],
            "Local Bitcoins", ["logo_localbitcoin.png", "https://localbitcoins.com/country/US"],
        ]
    ],
    ["UNITED KINGDOM",
        [
            "Bitstamp", ["logo_bitstamp.png", "https://www.bitstamp.net/"],
            "Local Bitcoins", ["logo_localbitcoin.png", "https://localbitcoins.com/country/UK"],
        ]
    ],
    ["CHINA",
        [
            "Okcoin", ["logo_okcoin.png", "https://www.okcoin.cn/"],
            "BTCChina", ["logo_btcchina.png", "https://www.btcchina.com/#/"],
            "Huobi", ["logo_huobi.jpg", "https://www.huobi.com/"],
            "Local Bitcoins", ["logo_localbitcoin.png", "https://localbitcoins.com/country/CN"],
        ]
    ]
];
var countries_exchangeMap = null;

// onReady
$(document).ready(function () {
    // Initialize
    //  countries_exchangeMap = new Map(exchangesList); // Doesnt work on IE11 :( Have to manually set
    countries_exchangeMap = new Map();
    for (var i = 0; i < exchangesList.length; i++) {
        var obj = exchangesList[i];
        countries_exchangeMap.set(obj[0], obj[1]);
    }

    // Fade in effect
    $("#title").fadeIn(1000);

    // 'Select' countries UI 
    populateCountries("country2");
});

function onCountrySelect() {
    var headerText = document.getElementById("h3_buyareaHeader");
    var selectItem = document.getElementById("country2");
    var selectedValue = selectItem.options[selectItem.selectedIndex].value;

    var list = document.getElementById('exchangeslist');
    // Clear available list first
    while (list.hasChildNodes()) {
        list.removeChild(list.firstChild);
    }

    var exchangesAvailable = countries_exchangeMap.get(selectedValue);
    if (exchangesAvailable != null && exchangesAvailable != "undefined") {
        // Add list item
        headerText.innerText = "BUY BITCOINS IN " + selectedValue + " WITH: ";

        for (var i = 0; i < exchangesAvailable.length; i++) {
            if (i % 2 == 0) {
                var exchangeName = exchangesAvailable[i];
                var exchangeImageLink = exchangesAvailable[i + 1][0];
                var exchangeLink = exchangesAvailable[i + 1][1];

                var entry = document.createElement('li');

                var logoImage = document.createElement("IMG");
                logoImage.setAttribute("src", "assets/icon/exchanges/" + exchangeImageLink);
                logoImage.setAttribute("width", "450");
                logoImage.setAttribute("width", "180");
                logoImage.setAttribute("class", "wow zoomIn");
                logoImage.setAttribute("alt", exchangeName);

                var linkObject = document.createElement("A");
                linkObject.setAttribute("target", "_blank");
                linkObject.setAttribute("href", exchangeLink);
                linkObject.appendChild(logoImage);

                // Append to the entry 'li' element
                entry.appendChild(linkObject);

                list.appendChild(entry);
                list.childNodes.shu
            }
        }
        // Shuffle nodes, to be fair :) 
        shuffleChildNodes('exchangeslist');
    } else {
        headerText.innerText = "No exchanges available.";

        var str = "Available countries for now: ";
        for (var i = 0; i < exchangesList.length; i++) {
            var obj = exchangesList[i];
            str += obj[0] + ", ";
        }
        var entry2 = document.createTextNode(str);

        list.appendChild(document.createElement("BR"));
        list.appendChild(entry2);
    }


    // Add the next 3 items in the list for some nice text effect
    document.getElementById("h2_country_fade1").innerText = "";
    document.getElementById("h2_country_fade2").innerText = "";
    document.getElementById("h2_country_fade3").innerText = "";
    for (var i = 1; i <= 3; i++) {
        if (selectItem.selectedIndex + i >= selectItem.options.length) {
            break;
        }
        var next1SelectedValue = selectItem.options[selectItem.selectedIndex + i].value;

        document.getElementById("h2_country_fade" + i).innerText = next1SelectedValue;

        $("#h2_country_fade" + i).fadeOut(100);
        $("#h2_country_fade" + i).fadeIn(500);
    }
}

function shuffleChildNodes(parent) {
    var container = document.getElementById(parent),
        children = container.children,
        length = children.length, i,
        tmparr = [];
    for (i = 0; i < length; i++)
        tmparr[i] = children[i];
    tmparr.sort(function () { return 0.5 - Math.random(); });
    for (i = 0; i < length; i++)
        container.appendChild(tmparr[i]);
}