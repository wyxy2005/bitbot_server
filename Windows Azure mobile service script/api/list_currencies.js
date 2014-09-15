exports.get = function(request, response) {
    // Get storage account settings from app settings. 
    //var appSettings = require('mobileservice-config').appSettings;
    //var pairs = appSettings.currencyPairs; // max length = 1000
    var pairs = 'btce-btc_usd---btce-btc_eur---btce-btc_rur---btce-btc_cnh---btce-btc_gbp---btce-ltc_usd---btce-ltc_btc---btce-ltc_eur---btce-ltc_rur---btce-ltc_cnh---btce-ltc_gbp---btce-ftc_btc---btce-nmc_usd---btce-nmc_btc---btce-usd_rur---btce-eur_usd---btce-usd_cnh---btce-gbp_usd---btce-nvc_usd---btce-nvc_btc---btce-trc_btc---btce-ppc_usd---btce-ppc_btc---btce-xpm_btc---bitstamp-btc_usd---okcoin-btc_cny---okcoin-ltc_cny---okcoininternational-btc_usd---okcoininternational-ltc_usd---huobi-btc_cny---huobi-ltc_cny---coinbase-btc_usd---btcchina-btc_cny---btcchina-ltc_cny---btcchina-ltc_btc---mtgox-btc_usd---campbx-btc_usd---itbit-xbt_usd---itbit-xbt_sgd---itbit-xbt_eur---bitfinex-btc_usd---bitfinex-ltc_usd---bitfinex-ltc_btc---bitfinex-drk_usd---bitfinex-drk_btc---kraken-xbt_usd---kraken-xbt_eur---cexio-ghs_btc---fybsg-btc_sgd---fybse-btc_sek---dgex-nxt_btc---cryptsy-btc_usd---cryptsy-doge_usd---cryptsy-drk_usd---cryptsy-ftc_usd---cryptsy-ltc_usd---cryptsy-rdd_usd---cryptsy-nxt_btc---cryptsy-ltc_btc---_796-btc Futures_usd---_796-ltc Futures_usd';
    
    response.send(statusCodes.OK, 
    { 
        message : pairs 
    });
};