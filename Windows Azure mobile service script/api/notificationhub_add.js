exports.post = function(request, response) {
    // Use "request.service" to access features of your mobile service, e.g.:
    //   var tables = request.service.tables;
    //   var push = request.service.push;

    var currencypair = escape(request.query.currencypair); // eg: btc_usd
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

    // Input validation for currency pairs
    var appSettings = require('mobileservice-config').appSettings;
    var pairs = appSettings.currencyPairs;

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
        increment_percent_daily >= 0 && increment_percent_daily <= 20) {

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
                success: function(results) {
                    if (results.length != 0) {
                        // a copy available
                        updateDatabase(tableName, high, low, times, pushuri, delay_between_notification, hub_registrationid, uniqueid, increment_percent_daily, exchange_pair, platform);
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
                                hub_registrationid: hub_registrationid
                            }, {
                                success: function() {
                                    // insert the transaction
                                    response.send(statusCodes.OK,
                                        {
                                            message: 'ok',
                                        });
                                }, error: function(err) {
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
                }, error: function(err) {
                    console.log("error checking current db: " + err);

                    response.send(statusCodes.OK, { message: 'Currency pair not supported.' });
                }
            });
    } else {
        response.send(statusCodes.BAD_REQUEST, { message: 'bad guy :(' });
    }

    function updateDatabase(tableName, high, low, times, pushuri, delay_between_notification, hub_registrationid, uniqueid, increment_percent_daily, exchange_pair, platform) {
        var mssql = request.service.mssql;
        var query = "UPDATE TOP (1) push_price SET pushuri = ?, high = ?, low = ?, times = ?, delay_between_notification = ?, increment_percent_daily = ?, hub_registrationid = ? WHERE uniqueid = ? AND exchange_pair = ? AND operating_system = ?";

        // console.log(query)

        mssql.query(query, [pushuri, high, low, times, delay_between_notification, increment_percent_daily, hub_registrationid, uniqueid, exchange_pair, platform], {
            success: function(results) {
            }, error: function(err) {
                console.log("error updating current db: " + err); // ok doesn't really matter'
            }
        }
            );
        response.send(statusCodes.OK, { message: 'ok' });
    }
};

exports.get = function(request, response) {
    response.send(statusCodes.OK, { message: 'Hello World!' });
};