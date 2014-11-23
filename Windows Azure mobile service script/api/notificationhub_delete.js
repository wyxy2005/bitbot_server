exports.post = function(request, response) {
    // Use "request.service" to access features of your mobile service, e.g.:
    //   var tables = request.service.tables;
    //   var push = request.service.push;
    
    var currencypair = escape(request.query.currencypair).replace('%20', ' '); // eg: btc_usd
    var uniqueid = escape(request.query.uniqueid); // the device's unique id. It also serve as an authentication against unauthorized modification of other user's data.
    var exchangesite = escape(request.query.exchangesite);
    var nonce = parseInt(request.query.nonce);
    var platform = escape(request.query.platform);
    
    // Input validation for currency pairs
    var pairs = 'btce-btc_usd---btce-btc_eur---btce-btc_rur---btce-btc_cnh---btce-btc_gbp---btce-ltc_usd---btce-ltc_btc---btce-ltc_eur---btce-ltc_rur---btce-ltc_cnh---btce-ltc_gbp---btce-ftc_btc---btce-nmc_usd---btce-nmc_btc---btce-usd_rur---btce-usd_cnh---btce-eur_usd---btce-gbp_usd---btce-nvc_usd---btce-nvc_btc---btce-trc_btc---btce-ppc_usd---btce-ppc_btc---btce-xpm_btc---bitstamp-btc_usd---okcoin-btc_cny---okcoin-ltc_cny---okcoininternational-btc_usd---okcoininternational-ltc_usd---okcoininternational-btc Futures Weekly_usd---okcoininternational-btc Futures BiWeekly_usd---okcoininternational-btc Futures Monthly_usd---okcoininternational-btc Futures Quarterly_usd---okcoininternational-ltc Futures Weekly_usd---okcoininternational-ltc Futures BiWeekly_usd---okcoininternational-ltc Futures Monthly_usd---okcoininternational-ltc Futures Quarterly_usd---huobi-btc_cny---huobi-ltc_cny---coinbase-btc_usd---btcchina-btc_cny---btcchina-ltc_cny---mtgox-btc_usd---campbx-btc_usd---itbit-xbt_usd---itbit-xbt_sgd---itbit-xbt_eur---bitfinex-btc_usd---bitfinex-ltc_usd---bitfinex-ltc_btc---bitfinex-drk_usd---bitfinex-drk_btc---kraken-xbt_usd---kraken-xbt_eur---cexio-ghs_btc---fybsg-btc_sgd---fybse-btc_sek---dgex-nxt_btc---cryptsy-doge_btc---cryptsy-cann_btc---cryptsy-drk_btc---cryptsy-rdd_btc---cryptsy-uro_btc---cryptsy-bc_btc---cryptsy-btcd_btc---cryptsy-btc_usd---cryptsy-doge_usd---cryptsy-drk_usd---cryptsy-ftc_usd---cryptsy-ltc_usd---cryptsy-rdd_usd---cryptsy-nxt_btc---cryptsy-ltc_btc---_796-btc Futures_usd---_796-ltc Futures_usd';
    
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
         var tables = request.service.tables;
         var pushTable = tables.getTable('push_price');

        updateDatabase(uniqueid, exchangesite, currencypair, platform);
    }
    response.send(statusCodes.OK, { message : 'ok' });
   
   
   function updateDatabase(uniqueid, exchangesite, currencypair, platform) {
        var mssql = request.service.mssql;
        var query = "UPDATE push_price SET times = 0 WHERE uniqueid = ? and exchange_pair = ? AND operating_system = ?";

          mssql.query(query, [uniqueid, exchangesite + '_' + currencypair, platform], {
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
