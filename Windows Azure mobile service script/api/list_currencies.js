exports.get = function(request, response) {
    // Get storage account settings from app settings. 
    //var appSettings = require('mobileservice-config').appSettings;
    //var pairs = appSettings.currencyPairs; // max length = 1000
    
    // everything must be in lower case here.
    var pairs = 'gemini-btc_usd---gemini-eth_usd---gemini-eth_btc---btce-btc_usd---btce-btc_eur---btce-btc_rur---btce-ltc_usd---btce-ltc_btc---btce-ltc_eur---btce-ltc_rur---btce-nmc_usd---btce-nmc_btc---btce-usd_rur---btce-eur_usd---btce-nvc_usd---btce-nvc_btc---btce-ppc_usd---btce-ppc_btc---btce-eth_usd---btce-eth_btc---btce-dsh_btc---bitstamp-btc_usd---bitstamp-btc_eur---bitstamp-eur_usd---okcoin-btc_cny---okcoin-ltc_cny---okcoininternational-btc_usd---okcoininternational-ltc_usd---okcoininternational-btc Futures Weekly_usd---okcoininternational-btc Futures BiWeekly_usd---okcoininternational-btc Futures Quarterly_usd---okcoininternational-ltc Futures Weekly_usd---okcoininternational-ltc Futures BiWeekly_usd---okcoininternational-ltc Futures Quarterly_usd---huobi-btc_cny---huobi-ltc_cny---coinbase-btc_usd---coinbaseexchange-btc_usd---coinbaseexchange-btc_gbp---coinbaseexchange-btc_eur---coinbaseexchange-btc_cad---coinbaseexchange-eth_btc---coinbaseexchange-eth_usd---btcchina-btc_cny---btcchina-ltc_btc---btcchina-ltc_cny---campbx-btc_usd---itbit-xbt_usd---itbit-xbt_sgd---itbit-xbt_eur---bitfinex-etc_btc---bitfinex-etc_usd---bitfinex-etc_usd---bitfinex-etc_btc---bitfinex-etc_eth---bitfinex-btc_usd---bitfinex-ltc_usd---bitfinex-ltc_btc---bitfinex-eth_usd---bitfinex-eth_btc---kraken-etc_xbt---kraken-etc_eth---kraken-etc_usd---kraken-etc_eur---kraken-xbt_usd---kraken-xbt_eur---kraken-eth_xbt---kraken-dao_xbt---kraken-dao_eth---kraken-dao_eur---kraken-dao_usd---kraken-dao_cad---kraken-dao_gbp---kraken-dao_jpy---cexio-ghs_btc---fybsg-btc_sgd---fybse-btc_sek---_796-btc Futures_usd---bitvc-btc Futures Weekly_cny---bitvc-btc Futures Quarterly_cny---bitvc-btc Futures BiWeekly_cny';
    
    response.send(statusCodes.OK, 
    { 
        message : pairs.toLowerCase() 
    });
};