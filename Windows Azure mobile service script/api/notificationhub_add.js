exports.post = function (request, response) {
    // Use "request.service" to access features of your mobile service, e.g.:
    //   var tables = request.service.tables;
    //   var push = request.service.push;

    var currencypair = escape(request.query.currencypair).replace('%20', ' '); // eg: btc_usd
    var exchangesite = escape(request.query.exchangesite);
    var pushuri = escape(request.query.pushuri); // push notification URI to client
    var hub_registrationid = escape(request.query.hub_registrationid); // Eg: 2944783739930209553-6013460519289270908-2
    var hub_etag = escape(request.query.hub_ETag);  // 
    var high = parseFloat(request.query.high); // The upper limit
    var low = parseFloat(request.query.low); // The lower limit
    var uniqueid = escape(request.query.uniqueid); // the device's unique id. It also serve as an authentication against unauthorized modification of other user's data.
    var times = parseInt(request.query.times); // number of times to be notified
    var delay_between_notification = parseInt(request.query.delay_between_notification) - 1;
    var increment_percent_daily = parseInt(request.query.increment_percent_daily);
    var platform = escape(request.query.platform);
    var email = request.query.email;

    // Input validation for currency pairs
    var pairs = 'btce-btc_usd---btce-btc_eur---btce-btc_rur---btce-btc_cnh---btce-btc_gbp---btce-ltc_usd---btce-ltc_btc---btce-ltc_eur---btce-ltc_rur---btce-ltc_cnh---btce-ltc_gbp---btce-ftc_btc---btce-nmc_usd---btce-nmc_btc---btce-usd_rur---btce-eur_usd---btce-usd_cnh---btce-gbp_usd---btce-nvc_usd---btce-nvc_btc---btce-trc_btc---btce-ppc_usd---btce-ppc_btc---btce-xpm_btc---bitstamp-btc_usd---okcoin-btc_cny---okcoin-ltc_cny---okcoininternational-btc_usd---okcoininternational-ltc_usd---huobi-btc_cny---huobi-ltc_cny---coinbase-btc_usd---btcchina-btc_cny---btcchina-ltc_cny---btcchina-ltc_btc---mtgox-btc_usd---campbx-btc_usd---itbit-xbt_usd---itbit-xbt_sgd---itbit-xbt_eur---bitfinex-btc_usd---bitfinex-ltc_usd---bitfinex-ltc_btc---bitfinex-drk_usd---bitfinex-drk_btc---kraken-xbt_usd---kraken-xbt_eur---cexio-ghs_btc---fybsg-btc_sgd---fybse-btc_sek---dgex-nxt_btc---cryptsy-btc_usd---cryptsy-doge_usd---cryptsy-drk_usd---cryptsy-ftc_usd---cryptsy-ltc_usd---cryptsy-rdd_usd---cryptsy-nxt_btc---cryptsy-ltc_btc---_796-btc Futures_usd---_796-ltc Futures_usd'; 

    var arr = pairs.split("---");
    var verifiedCurrencyPair = false;
    for (var i = 0; i < arr.length; i++) {
        var source_pair = arr[i].split("-");
        if (source_pair[0] == exchangesite && source_pair[1] == currencypair) {
            verifiedCurrencyPair = true;
            break;
        }
    }


    //console.log('low: '+low+', high: '+high+', unique: ' + uniqueid + ', times: ' + times + ', tablename: ' + tableName);
    //console.log(' ' +(low > 0)+ ' ' +(high > 0)+ ' ' +(high > low)+ ' ' +(times > 0)+ ' ' +(times <= 10));

    if (verifiedCurrencyPair &&
        low > 0 && high > 0 && high > low &&
        times > 0 && times <= 50 &&
        (delay_between_notification >= 59) &&
        (delay_between_notification <= 36000) &&
        increment_percent_daily >= 0 && increment_percent_daily <= 20 &&
        (email && email !== '' ? validateEmail(email) : true)) {

        var tableName = 'push_price';
        var tables = request.service.tables;
        var pushTable = tables.getTable(tableName);

        var exchange_pair = exchangesite + '_' + currencypair;

        pushTable.where({
            //pushuri: pushuri,
            uniqueid: uniqueid,
            exchange_pair: exchange_pair,
            operating_system: platform
        }).read({
            success: function (results) {
                if (results.length != 0) {
                    // a copy available
                    updateDatabase(tableName, high, low, times, pushuri, delay_between_notification, hub_registrationid, uniqueid, increment_percent_daily, exchange_pair, platform, email);
                } else {
                    // insert
                    pushTable.insert(
                        {
                            high: high,
                            low: low,
                            times: times,
                            pushuri: pushuri,
                            uniqueid: uniqueid,
                            delay_between_notification: delay_between_notification, // compensate for the fast running script. -1 second
                            increment_percent_daily: increment_percent_daily,
                            exchange_pair: exchange_pair,
                            operating_system: platform,
                            hub_registrationid: hub_registrationid,
                            email: !email ? null : email
                        }, {
                            success: function () {
                                // insert the transaction
                                response.send(statusCodes.OK,
                                    {
                                        message: 'ok',
                                    });
                            }, error: function (err) {
                                response.send(statusCodes.OK,
                                    {
                                        message: 'couldnt connect to database server',
                                    });
                                console.log(err);
                            }
                        });

                    response.send(statusCodes.OK,
                        {
                            message: 'ok',
                        });
                }
            }, error: function (err) {
                console.log("error checking current db: " + err);

                response.send(statusCodes.OK, { message: 'Currency pair not supported.' });
            }
        });
    } else {
        response.send(statusCodes.BAD_REQUEST, { message: 'bad guy :(' });
    }

    function updateDatabase(tableName, high, low, times, pushuri, delay_between_notification, hub_registrationid, uniqueid, increment_percent_daily, exchange_pair, platform, email) {
        var mssql = request.service.mssql;
        var query = "UPDATE TOP (1) push_price SET pushuri = ?, high = ?, low = ?, times = ?, delay_between_notification = ?, increment_percent_daily = ?, hub_registrationid = ?, email = ? WHERE uniqueid = ? AND exchange_pair = ? AND operating_system = ?";

        // console.log(query)

        mssql.query(query, [pushuri, high, low, times, delay_between_notification, increment_percent_daily, hub_registrationid, email, uniqueid, exchange_pair, platform], {
            success: function (results) {
            }, error: function (err) {
                console.log("error updating current db: " + err); // ok doesn't really matter'
            }
        }
            );
        response.send(statusCodes.OK, { message: 'ok' });
    }
};

exports.get = function (request, response) {
    response.send(statusCodes.OK, { message: 'Hello World!' });
};

function validateEmail(email) { 
    var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
} 