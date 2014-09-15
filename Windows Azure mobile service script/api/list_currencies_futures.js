exports.get = function(request, response) {
    // Get storage account settings from app settings. 
    //var appSettings = require('mobileservice-config').appSettings;
    //var pairs = appSettings.currencyPairs; // max length = 1000
    var pairs = 'okcoininternational-btc Futures Week_cny---okcoininternational-btc Futures NextWeek_cny---okcoininternational-btc Futures Month_cny---okcoininternational-btc Futures Quarter_cny---okcoininternational-ltc Futures Week_cny---okcoininternational-ltc Futures NextWeek_cny---okcoininternational-ltc Futures Month_cny---okcoininternational-ltc Futures Quarter_cny';
    
    response.send(statusCodes.OK, 
    { 
        message : pairs 
    });
};