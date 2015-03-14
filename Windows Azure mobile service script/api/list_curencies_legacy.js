exports.get = function(request, response) {
    // Get storage account settings from app settings. 
    //var appSettings = require('mobileservice-config').appSettings;
    //var pairs = appSettings.currencyPairs; // max length = 1000
    var pairs = 'dgex-nxt_btc---mtgox-btc_usd---btce-trc_btc---btce-xpm_btc---btce-ftc_btc';
    
    response.send(statusCodes.OK, 
    { 
        message : pairs 
    });
};