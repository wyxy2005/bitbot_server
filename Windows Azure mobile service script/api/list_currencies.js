exports.get = function(request, response) {
    // Get storage account settings from app settings. 
    //var appSettings = require('mobileservice-config').appSettings;
    //var pairs = appSettings.currencyPairs; // max length = 1000
    var pairs = 'gemini-btc_usd---btce-btc_usd---btce-btc_eur---btce-btc_rur---btce-ltc_usd---btce-ltc_btc---btce-ltc_eur---btce-ltc_rur---btce-nmc_usd---btce-nmc_btc---btce-usd_rur---btce-eur_usd---btce-nvc_usd---btce-nvc_btc---btce-ppc_usd---btce-ppc_btc---bitstamp-btc_usd---okcoin-btc_cny---okcoin-ltc_cny---okcoininternational-btc_usd---okcoininternational-ltc_usd---okcoininternational-btc Futures Weekly_usd---okcoininternational-btc Futures BiWeekly_usd---okcoininternational-btc Futures Quarterly_usd---okcoininternational-ltc Futures Weekly_usd---okcoininternational-ltc Futures BiWeekly_usd---okcoininternational-ltc Futures Quarterly_usd---huobi-btc_cny---huobi-ltc_cny---coinbase-btc_usd---coinbaseexchange-btc_usd---coinbaseexchange-btc_gbp---coinbaseexchange-btc_eur---coinbaseexchange-btc_cad---btcchina-btc_cny---btcchina-ltc_btc---btcchina-ltc_cny---campbx-btc_usd---itbit-xbt_usd---itbit-xbt_sgd---itbit-xbt_eur---bitfinex-btc_usd---bitfinex-ltc_usd---bitfinex-ltc_btc---bitfinex-eth_usd---bitfinex-eth-btc---kraken-xbt_usd---kraken-xbt_eur---kraken-eth_xbt---cexio-ghs_btc---fybsg-btc_sgd---fybse-btc_sek---_796-btc Futures_usd---_796-ltc Futures_usd---bitvc-btc Futures Weekly_cny---bitvc-btc Futures Quarterly_cny---bitvc-btc Futures BiWeekly_cny';
    
    response.send(statusCodes.OK, 
    { 
        message : pairs 
    });
};