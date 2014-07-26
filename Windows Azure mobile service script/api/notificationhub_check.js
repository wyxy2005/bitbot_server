exports.post = function(request, response) {
    // Use "request.service" to access features of your mobile service, e.g.:
    //   var tables = request.service.tables;
    //   var push = request.service.push;
    
    var currencypair = escape(request.query.currencypair);
    var exchangesite = escape(request.query.exchangesite);
    var pushuri = escape(request.query.pushuri);
    var uniqueid = escape(request.query.uniqueid); // the device's unique id. It also serve as an authentication against unauthorized modification of other user's data.
    var platform = escape(request.query.platform);
    
    // Input validation for currency pairs
    var appSettings = require('mobileservice-config').appSettings;
    var pairs = appSettings.currencyPairs;
    
    var arr = pairs.split("---");
    var verifiedCurrencyPair = false;
    for(var i=0;i<arr.length;i++) {
        var source_pair = arr[i].split("-");
        if (source_pair[0] == exchangesite && source_pair[1] == currencypair) {
            verifiedCurrencyPair = true;
            break;
        }
     }
     
     if (!verifiedCurrencyPair) {
             response.send(statusCodes.OK, 
             { 
                 message : 'currency pair not supported',
                 high: 0,
                 low: 0,
                 times: 0,
                 delay_between_notification: 60,
                 increment_percent_daily: 0,
                 email: ''
              });
         return;
     }
    
    // check against current ones that exist
    var tables = request.service.tables;
    var pushTable = tables.getTable('push_price');

        pushTable.where({
                    uniqueid: uniqueid,
                    exchange_pair: exchangesite + '_' + currencypair,
                    operating_system: platform
                }).read({
                    success: function(results) {
                        if (results.length != 0) {
                            // a copy available
                            for (var i = 0; i < results.length; i++) 
                             {
                                 var item = results[i];
                                 
                                 if (pushuri != item.pushuri) { 
                                     // The app might have been reinstalled.
                                     // Same uniqueID but different push channel
                                     response.send(statusCodes.OK, 
                                    { 
                                        message : 'ok',
                                        high: 0,
                                        low: 0,
                                        times: 0,
                                        delay_between_notification: 60,
                                        increment_percent_daily: 0,
                                        email: ''
                                    });
                                 } else {
                                     response.send(statusCodes.OK, 
                                    { 
                                        message : 'ok',
                                        high: item.high,
                                        low: item.low,
                                        times: item.times,
                                        delay_between_notification: (item.delay_between_notification + 1),
                                        increment_percent_daily: item.increment_percent_daily,
                                        email: item.email
                                    });
                                 }
                             }
                        } else {
                            // Not available.
                            response.send(statusCodes.OK, 
                            { 
                                message : 'ok',
                                high: 0,
                                low: 0,
                                times: 0,
                                delay_between_notification: 60,
                                increment_percent_daily: 0,
                                email: ''
                            });
                        }
                    }, error: function(err) {
                        console.log("error checking current db: " + err);
                        
                        response.send(statusCodes.OK, 
                        { 
                            message : 'Currency pair not supported or error connecting to database.',
                            high: 0,
                            low: 0,
                            times: 0,
                            delay_between_notification: 60,
                            increment_percent_daily: 0,
                            email: ''
                        });
                    }   
                });
};

exports.get = function(request, response) {
    response.send(statusCodes.OK, 
    { 
        message : 'access denied',
        high: 0,
        low: 0,
        times: 0,
        delay_between_notification: 60,
        email: ''
     });
};