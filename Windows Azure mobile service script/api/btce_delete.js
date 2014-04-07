exports.post = function(request, response) {
    // Use "request.service" to access features of your mobile service, e.g.:
    //   var tables = request.service.tables;
    //   var push = request.service.push;
    
    var currencypair = escape(request.query.currencypair); // eg: btc_usd
    var uniqueid = escape(request.query.uniqueid); // the device's unique id. It also serve as an authentication against unauthorized modification of other user's data.
    var exchangesite = escape(request.query.exchangesite);
    var nonce = parseInt(request.query.nonce);
    
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
     
    if (verifiedCurrencyPair) {
         var tableName = exchangesite + '_push_' +currencypair;
         
         var tables = request.service.tables;
         var pushTable = tables.getTable('push_price');

        updateDatabase(tableName, uniqueid, exchangesite, currencypair);
    }
    response.send(statusCodes.OK, { message : 'ok' });
   
   
   function updateDatabase(tableName, uniqueid, exchangesite, currencypair) {
        var mssql = request.service.mssql;
        var query = "DELETE TOP (1) FROM push_price WHERE uniqueid = ? and exchange_pair = ?";

          mssql.query(query, [uniqueid, exchangesite + '_' + currencypair], {
               success: function(results) {            
                }, error:function(err) {
                    console.log("error updating current db: " + err); // ok doesn't really matter'
                }
          }
          ); 
    }
};

exports.get = function(request, response) {
    response.send(statusCodes.OK, { message : 'Hello World!' });
};

