exports.post = function(request, response) {
    // Use "request.service" to access features of your mobile service, e.g.:
    //   var tables = request.service.tables;
    //   var push = request.service.push;

    var currencypair = escape(request.query.currencypair).replace('%20', ' ');
    var exchangesite = escape(request.query.exchangesite);
    var pushuri = escape(request.query.pushuri);
    var uniqueid = escape(request.query.uniqueid); // the device's unique id. It also serve as an authentication against unauthorized modification of other user's data.
    var platform = escape(request.query.platform);

    // Input validation for currency pairs
    var pairs = 'gemini-btc_usd---gemini-eth_usd---gemini-eth_btc---btce-btc_usd---btce-btc_eur---btce-btc_rur---btce-ltc_usd---btce-ltc_btc---btce-ltc_eur---btce-ltc_rur---btce-nmc_usd---btce-nmc_btc---btce-usd_rur---btce-eur_usd---btce-nvc_usd---btce-nvc_btc---btce-ppc_usd---btce-ppc_btc---btce-eth_usd---btce-eth_btc---btce-dsh_btc---bitstamp-btc_usd---bitstamp-btc_eur---bitstamp-eur_usd---okcoin-btc_cny---okcoin-ltc_cny---okcoininternational-btc_usd---okcoininternational-ltc_usd---okcoininternational-btc Futures Weekly_usd---okcoininternational-btc Futures BiWeekly_usd---okcoininternational-btc Futures Quarterly_usd---okcoininternational-ltc Futures Weekly_usd---okcoininternational-ltc Futures BiWeekly_usd---okcoininternational-ltc Futures Quarterly_usd---huobi-btc_cny---huobi-ltc_cny---coinbase-btc_usd---coinbaseexchange-eth_btc---coinbaseexchange-eth_usd---coinbaseexchange-btc_usd---coinbaseexchange-btc_gbp---coinbaseexchange-btc_eur---coinbaseexchange-btc_cad---btcchina-btc_cny---btcchina-ltc_btc---btcchina-ltc_cny---campbx-btc_usd---itbit-xbt_usd---itbit-xbt_sgd---itbit-xbt_eur---bitfinex-etc_usd---bitfinex-etc_btc---bitfinex-etc_eth---bitfinex-btc_usd---bitfinex-ltc_usd---bitfinex-ltc_btc---bitfinex-eth_usd---bitfinex-eth_btc---kraken-xbt_usd---kraken-xbt_eur---kraken-eth_xbt---kraken-dao_xbt---kraken-dao_eth---kraken-dao_eur---kraken-dao_usd---kraken-dao_cad---kraken-dao_gbp---kraken-dao_jpy---kraken-etc_xbt---kraken-etc_eth---kraken-etc_usd---kraken-etc_eur---cexio-ghs_btc---fybsg-btc_sgd---fybse-btc_sek---_796-btc Futures_usd---_796-ltc Futures_usd---bitvc-btc Futures Weekly_cny---bitvc-btc Futures Quarterly_cny---bitvc-btc Futures BiWeekly_cny';
    
    var arr = pairs.split("---");
    var verifiedCurrencyPair = false;
    for (var i = 0; i < arr.length; i++) {
        var source_pair = arr[i].split("-");
        if (source_pair[0] == exchangesite && source_pair[1] == currencypair) {
            verifiedCurrencyPair = true;
            break;
        }
    }

    if (!verifiedCurrencyPair) {
        response.send(statusCodes.OK,
            {
                message: 'currency pair not supported',
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
                    for (var i = 0; i < results.length; i++) {
                        var item = results[i];

                        if (item.times > 0) {
                            response.send(statusCodes.OK,
                                {
                                    message: 'ok',
                                    high: item.high,
                                    low: item.low,
                                    times: item.times,
                                    delay_between_notification: (item.delay_between_notification + 1),
                                    increment_percent_daily: item.increment_percent_daily,
                                    email: item.email
                                });
                                break;
                        }
                    }
                     // Not available.
                    response.send(statusCodes.OK,
                        {
                            message: 'ok',
                            high: 0,
                            low: 0,
                            times: 0,
                            delay_between_notification: 60,
                            increment_percent_daily: 0,
                            email: ''
                        });
                } else {
                    // Not available.
                    response.send(statusCodes.OK,
                        {
                            message: 'ok',
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
                        message: 'Currency pair not supported or error connecting to database.',
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
            message: 'access denied',
            high: 0,
            low: 0,
            times: 0,
            delay_between_notification: 60,
            email: ''
        });
};