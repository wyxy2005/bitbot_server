exports.get = function(request, response) {
    // Get storage account settings from app settings. 
    //var appSettings = require('mobileservice-config').appSettings;
    //var pairs = appSettings.currencyPairs; // max length = 1000
    var pairs = 'dgex-nxt_btc---mtgox-btc_usd---btce-trc_btc---btce-xpm_btc---btce-ftc_btc---_796-btc Futures Quarterly_usd---btce-btc_gbp---btce-btc_cnh---btce-ltc_gbp---btce-ltc_cnh---btce-usd_cnh---btce-gbp_usd';
    
    response.send(statusCodes.OK, 
    { 
        message : pairs 
    });
};