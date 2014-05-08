function check_price() {
    var date = new Date();
    var dateStr = date.toISOString().
        replace(/T/, ' ').      // replace T with a space
        replace(/\..+/, '');     // delete the dot and everything after

    console.log("Scheduled price check task: " + dateStr);

    // start
    fetchItem('btc_usd', 'btce');
    fetchItem('btc_eur', 'btce');
    fetchItem('btc_rur', 'btce');
    fetchItem('btc_cnh', 'btce');
    fetchItem('btc_gbp', 'btce');
    fetchItem('ltc_btc', 'btce');
    fetchItem('ltc_usd', 'btce');
    fetchItem('ltc_rur', 'btce');
    fetchItem('ltc_eur', 'btce');
    fetchItem('ltc_cnh', 'btce');
    fetchItem('ltc_gbp', 'btce');
    fetchItem('nmc_usd', 'btce');
    fetchItem('nmc_btc', 'btce');
    fetchItem('usd_rur', 'btce');
    fetchItem('usd_cnh', 'btce');
    fetchItem('gbp_usd', 'btce');
    fetchItem('eur_usd', 'btce');
    fetchItem('nvc_usd', 'btce');
    fetchItem('nvc_btc', 'btce');
    fetchItem('trc_btc', 'btce');
    fetchItem('ppc_usd', 'btce');
    fetchItem('ppc_btc', 'btce');
    fetchItem('ftc_btc', 'btce');
    fetchItem('xpm_btc', 'btce');

    fetchItem('btc_cny', 'btcchina');
    fetchItem('ltc_cny', 'btcchina');

    fetchItem('btc_usd', 'mtgox');

    fetchItem('btc_cny', 'okcoin');
    fetchItem('ltc_cny', 'okcoin');
    //fetchItem('btc_cny', 'huobi');

    fetchItem('btc_usd', 'coinbase');

    fetchItem('btc_usd', 'bitstamp');

    fetchItem('xbt_usd', 'itbit');
    fetchItem('xbt_sgd', 'itbit');
    fetchItem('xbt_eur', 'itbit');

    fetchItem('btc_usd', 'bitfinex');
    fetchItem('ltc_usd', 'bitfinex');
    fetchItem('ltc_btc', 'bitfinex');

    fetchItem('btc_usd', 'campbx');

    fetchItem('xbt_usd', 'kraken');
    fetchItem('xbt_eur', 'kraken');

    fetchItem('ghs_btc', 'cexio');

    fetchItem('btc_sgd', 'fybsg');
    fetchItem('btc_sek', 'fybse');

    function fetchItem(currencypair, source) {
        if (source == 'btce') {
            // reduce traffic on a single IP due to BTCe cloudflare
            if (Math.floor((Math.random() * 1) + 1) == 1) {
                fetchfromSource(currencypair, source, 'https://btc-e.com/api/2/' + currencypair + '/ticker');
            } else {
                fetchfromSource(currencypair, source, 'http://maaapersonalppace.azurewebsites.net/httprelay.php?url=' + escape('https://btc-e.com/api/2/' + currencypair + '/ticker'));
            }
        } else if (source == 'btcchina') {
            fetchfromSource(currencypair, source, currencypair == 'ltc_cny' ? 'https://data.btcchina.com/data/ticker?market=cnyltc' : 'https://data.btcchina.com/data/ticker?market=cnybtc');
        } else if (source == 'mtgox') {
            fetchfromSource(currencypair, source, 'http://data.mtgox.com/api/2/BTCUSD/money/ticker');
        } else if (source == 'okcoin') {
            fetchfromSource(currencypair, source, 'https://www.okcoin.com/api/ticker.do?symbol=' + currencypair);
        } else if (source == 'coinbase') {
            fetchfromSource(currencypair, source, 'https://coinbase.com/api/v1/prices/buy');
        } else if (source == 'bitstamp') {
            fetchfromSource(currencypair, source, 'https://www.bitstamp.net/api/ticker/');
        } else if (source == 'itbit') {
            fetchfromSource(currencypair, source, 'https://www.itbit.com/api/feeds/ticker/' + currencypair.toUpperCase().replace('_', ""));
        } else if (source == 'kraken') {
            var symbol = currencypair.toUpperCase().replace('_', "");
            fetchfromSource(currencypair, source, 'https://api.kraken.com/0/public/Ticker?pair=' + symbol);
        } else if (source == 'huobi') {
            fetchfromSource(currencypair, source, '');
        } else if (source == 'campbx') {
            fetchfromSource(currencypair, source, 'http://CampBX.com/api/xticker.php')
        } else if (source == 'bitfinex') {
             var symbol = currencypair.replace('_', "");
            fetchfromSource(currencypair, source, 'https://api.bitfinex.com/v1/ticker/' + symbol);
        } else if (source == 'cexio') {
            fetchfromSource(currencypair, source, 'https://cex.io/api/ticker/GHS/BTC');
        } else if (source == 'fybsg') {
            fetchfromSource(currencypair, source, 'https://www.fybsg.com/api/SGD/ticker.json');
        } else if (source == 'fybse') {
            fetchfromSource(currencypair, source, 'https://www.fybse.se/api/SEK/ticker.json');
        }
    }

    function fetchfromSource(currencypair, source, httpsource) {
        var request = require('request');
        var sqlTable = source + '_push_' + currencypair;

        var currencyPair_str = currencypair.replace("_", "/").toUpperCase();

        request({
            timeout: 12000,
            uri: httpsource,
            followRedirect: true,
            maxRedirects: 10,
            headers: {
                'User-Agent': 'Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko'
            }
        }, function(error, response, body) {
                if (!error && response.statusCode == 200) {
                    var returnJson = JSON.parse(body);

                    // acquire data here
                    var currentPrice = 0;
                    var average24Hrs = 0;  //  Buy / Average * 100 - 100
                    switch (source) {
                        case 'coinbase':
                            currentPrice = parseFloat(returnJson.amount);
                            average24Hrs = 0; // no data available
                            break;
                        case 'itbit':
                        case 'cexio':
                        case 'bitstamp':
                            currentPrice = parseFloat(returnJson.bid);
                            average24Hrs = currentPrice / (parseFloat(returnJson.high) + parseFloat(returnJson.low) / 2) * 100 - 100;
                            break;
                        case 'mtgox':
                            currentPrice = parseFloat(returnJson.data.buy.value);
                            average24Hrs = currentPrice / parseFloat(returnJson.data.avg.value) * 100 - 100;
                            break;
                        case 'fybsg':
                        case 'fybse':
                            currentPrice = parseFloat(returnJson.bid);
                            average24Hrs = 0; // no data available
                            break;
                        case 'campbx':
                            currentPrice = parseFloat(returnJson['Best Bid']);
                            average24Hrs = 0; // no data available
                            break;
                        case 'bitfinex':
                            currentPrice = parseFloat(returnJson['bid']);
                            average24Hrs = 0; // no data available
                            break;
                        case 'kraken':
                            var pairName2 = "X" + currencypair.replace("_", "Z").toUpperCase();
                            var pairresult = returnJson.result[pairName2];

                            currentPrice = parseFloat(pairresult.b[0]);
                            average24Hrs = currentPrice / (parseFloat(pairresult.h[0]) + parseFloat(pairresult.l[0]) / 2) * 100 - 100;
                            break;
                        default:
                            currentPrice = parseFloat(returnJson.ticker.buy);
                            average24Hrs = currentPrice / parseFloat(returnJson.ticker.avg) * 100 - 100;
                            break;
                    }
                    average24Hrs = Math.abs(average24Hrs);
                    if (isNaN(average24Hrs))
                        average24Hrs = 0;

                    // insert data for graph
                    insertTickerData(returnJson, currencypair, source);

                    var query = "SELECT * FROM push_price WHERE (low >= ? OR high <= ? OR (increment_percent_daily <= ? AND increment_percent_daily != 0)) AND __updatedAt < dateadd(ss, - (SELECT delay_between_notification), getdate()) AND exchange_pair = ?;";
                    mssql.query(query, [currentPrice, currentPrice, average24Hrs, source + '_' + currencypair], {
                        success: function(results) {
                            if (results.length > 0) {
                                // Permission record was found. Continue normal execution. 
                                console.log('%d items available for %s %d', results.length, currencypair, average24Hrs);

                                for (var i = 0; i < results.length; i++) {
                                    var item = results[i];
                                    alertClient(item, currentPrice, average24Hrs, currencypair, currencyPair_str, sqlTable, date, source);
                                }
                            } else {
                                //console.log('%d items available for %s', results.length, currencypair);
                            }
                        }, error: function(err) {
                            console.log("error is: " + err);
                        }
                    });
                }
                else {
                    console.log('Connection ' + response.statusCode + ', ' + error + ' to ' + httpsource + ' for ' + currencypair + ':' + source);
                }
            })
    }

    function alertClient(client, currentPrice, average24Hrs, currencypair, currencypairStr, sqlTable, date, source) {
        // client = client.pushuri, client.low, client.high
        var clientLow = parseFloat(client.low);
        var clientHigh = parseFloat(client.high);
        var client_increment_percent_daily = parseInt(client.increment_percent_daily);
        var clientuniqueid = client.uniqueid;
        var platform = client.operating_system;
        var hub_registrationid = client.hub_registrationid;
        var clientUniqueId = client.uniqueid;

        if (hub_registrationid == null)
            return;

        /*  var client_delay_between_notification = client.delay_between_notification;
          var client_updatedAt = client.__updatedAt;
          var timeDifference = parseInt(date - client_updatedAt);
        
          console.log((client_delay_between_notification * 1000));
          console.log( timeDifference);
        
           delay per notification
          if ( timeDifference < client_delay_between_notification * 1000)  {
             return;
          }*/

        var isFalling = clientLow >= currentPrice;
        var isPercentageNotification = average24Hrs >= client_increment_percent_daily && client_increment_percent_daily != 0;

        var alertMsg_Toast =
            isPercentageNotification ? (currencypairStr + "+- " + average24Hrs.toFixed(2) + "%! [" + source + "]") :

            (isFalling ? (currencypairStr + "? fell below " + clientLow + ". [" + source + "]") :
            (currencypairStr + "? went above " + clientHigh + ". [" + source + "]"));
        var alertMsg_Tile = (((isFalling ? clientLow : clientHigh) / currentPrice * 100 - 100) * -1).toFixed(5) +
            "% - " + currencypairStr + "\r\n\r\nPrice: $" + currentPrice;

        var alertMsg_ToastNavigateURI = '/DetailedPricePage.xaml?QuoteDataSource=' + source + '&CurrencyPair=' + currencypair;

        // Start sending
        if (platform == "WindowsPhone8") {
            push.mpns.sendToast(clientUniqueId,
                {
                    text1: currentPrice.toString(),
                    text2: alertMsg_Toast,
                    param: alertMsg_ToastNavigateURI,
                    Sound: 'Assets/CustomRingtone/Cat.wav'
                }, {
                    success: function(response) {
                        // pinned tile
                        push.mpns.sendTile(clientUniqueId, {
                            count: 1,
                            id: '/DetailedPricePage.xaml?QuoteDataSource=' + source + '&CurrencyPair=' + currencypair,
                        }, {
                                success: function(pushResponse) {
                                    //console.log("Sent push:", pushResponse);

                                    updateNotificationCount(sqlTable, client.pushuri, client, source, currencypair, platform);
                                }
                            });
                        // sent another tile update to the main
                        push.mpns.sendTile(clientUniqueId, {
                            count: 1,
                        }, {
                                success: function(pushResponse) {
                                }
                            });
                    },
                    error: function(error) {
                        // exired channel
                        if (error.statusCode == 410 || error.statusCode == 404 || error.statusCode == 412) {
                            removeExpiredChannel(sqlTable, client.pushuri, clientuniqueid);
                        }
                        console.log(error);
                    }
                });

            /*var toast = '<?xml version="1.0" encoding="utf-8"?>' +
                '<wp:Notification xmlns:wp="WPNotification">' +
                '<wp:Toast>' +
                '<wp:Text1>test</wp:Text1>' +
                '<wp:Text2>test2</wp:Text2>' +
                '<wp:Sound>Assets/CustomRingtone/Cat.wav</wp:Sound>' +
                '<wp:Param>test3</wp:Param>' +
                '</wp:Toast>' +
                '</wp:Notification>';

            console.log(toast);
            push.mpns.sendRaw(clientUniqueId, toast, {
                success: function(error) {
                    if (error) {
                        if (error.statusCode == 410 || error.statusCode == 404 || error.statusCode == 412)
                            removeExpiredChannel(sqlTable, client.pushuri, clientuniqueid);
                        console.log(error);
                    } else {

                    }
                }
            });*/
        } else if (platform == "Windows8") {
            push.wns.sendToastImageAndText04(clientUniqueId, {
                text1: currentPrice.toString(),
                text2: alertMsg_Toast,
                text3: "",
                image: "ms-appx:///Assets/ToastImage.png"
            }, {
                    launch: alertMsg_ToastNavigateURI,
                    duration: 'long',
                }, {
                    success: function(pushResponse) {
                        push.wns.sendBadge(clientUniqueId, {
                            value: 1,
                            text1: alertMsg_Toast
                        }, {
                                success: function(pushResponse) {
                                    updateNotificationCount(sqlTable, client.pushuri, client, source, currencypair, platform);
                                }
                            });
                    }, error: function(error) {
                        // expired channel
                        if (error.statusCode == 410 || error.statusCode == 404 || error.statusCode == 412) {
                            removeExpiredChannel(sqlTable, client.pushuri, clientuniqueid);
                        }
                        console.log(error);
                    }
                }, {
                    error: function(error) {
                        // expired channel
                        if (error.statusCode == 410 || error.statusCode == 404 || error.statusCode == 412) {
                            removeExpiredChannel(sqlTable, client.pushuri, clientuniqueid);
                        }
                        console.log(error);
                    }
                });
        }

    }

    function updateNotificationCount(sqlTable, pushuri, client, source, currencypair, platform) {
        var uniqueid = client.uniqueid;
        var notifyTimes = parseInt(client.times);
        notifyTimes--;

        if (notifyTimes <= 0) {
            removeExpiredChannel(sqlTable, client.pushuri, client.uniqueid, source, currencypair, platform);
        } else {
            var sql = "UPDATE TOP (1) push_price SET times = ? WHERE uniqueid = ? AND exchange_pair = ? AND operating_system = ?;";
            var params = [notifyTimes, uniqueid, source + '_' + currencypair, platform];

            mssql.query(sql, params,
                {
                    success: function(results) {
                        console.log('Updated push ID: ' + uniqueid)
                     }
                });
        }
    }

    function removeExpiredChannel(sqlTable, pushuri, clientuniqueid, source, currencypair, platform) {
        var sql = "DELETE TOP (1) FROM push_price WHERE uniqueid = ? AND exchange_pair = ? AND operating_system = ?;";
        var params = [clientuniqueid, source + '_' + currencypair, platform];
        mssql.query(sql, params,
            {
                success: function(results) {
                    console.log('Removed Expired Channel: ' + pushuri)
        }
            });
    }

    function insertTickerData(returnJson, currencypair, source) {
        if (
            //(source == 'btce' && currencypair == 'btc_usd') ||
            //(source == 'btce' && currencypair == 'btc_rur') ||
            //(source == 'btce' && currencypair == 'btc_eur') ||
            //(source == 'btce' && currencypair == 'btc_cnh') ||
            //(source == 'btce' && currencypair == 'btc_gbp') ||
            //(source == 'btce' && currencypair == 'ltc_usd') ||
            //(source == 'btce' && currencypair == 'ltc_rur') ||
            //(source == 'btce' && currencypair == 'ltc_cnh') ||
            //(source == 'btce' && currencypair == 'ltc_eur') ||
            //(source == 'btce' && currencypair == 'ltc_gbp') ||
            //(source == 'btce' && currencypair == 'nmc_usd') ||
            //(source == 'btce' && currencypair == 'ppc_usd') ||
            //(source == 'btce' && currencypair == 'nvc_usd') ||
            //(source == 'btce' && currencypair == 'trc_btc') ||
            //(source == 'btce' && currencypair == 'xpm_btc') ||
            //(source == 'btce' && currencypair == 'ftc_btc') ||
            //(source == 'mtgox' && currencypair == 'btc_usd') ||
            (source == 'btcchina' && currencypair == 'btc_cny') ||
            (source == 'btcchina' && currencypair == 'ltc_cny') ||
            //(source == 'okcoin' && currencypair == 'btc_cny') ||
            //(source == 'okcoin' && currencypair == 'ltc_cny') ||
            //(source == 'bitstamp' && currencypair == 'btc_usd') ||
            //(source == 'coinbase' && currencypair == 'btc_usd') ||
            //(source == 'huobi' && currencypair == 'btc_cny') ||
            //(source == 'itbit' && currencypair == 'xbt_usd') ||
            //(source == 'itbit' && currencypair == 'xbt_sgd') ||
            //(source == 'itbit' && currencypair == 'xbt_eur') ||
            (source == 'kraken' && currencypair == 'xbt_usd') ||
            (source == 'kraken' && currencypair == 'xbt_eur') ||
            (source == 'cexio' && currencypair == 'ghs_btc')
            ) {

            if (source == 'btce') {
                insertTickerData_btce(returnJson, currencypair, source);
            } else if (source == 'btcchina') {
                insertTickerData_btcChina(returnJson, currencypair, source);
            } else if (source == 'mtgox') {
                insertTickerData_MTGox(returnJson, currencypair, source);
            } else if (source == 'okcoin') {
                insertTickerData_Okcoin(returnJson, currencypair, source);
            } else if (source == 'huobi') {
            } else if (source == 'coinbase') {
                insertTickerData_Coinbase(returnJson, currencypair, source);
            } else if (source == 'bitstamp') {
                insertTickerData_Bitstamp(returnJson, currencypair, source);
            } else if (source == 'itbit') {
                insertTickerData_Itbit(returnJson, currencypair, source);
            } else if (source == 'kraken') {
                insertTickerData_Kraken(returnJson, currencypair, source);
            } else if (source == 'cexio') {
                insertTickerData_CexIO(returnJson, currencypair, source);
            }
        }
    }

    function insertTickerData_btce(returnJson, currencypair, source) {
        var pushTable = source + '_price_' + currencypair;

        var query_insert = 'INSERT INTO bitcoinbot.' + pushTable + ' ("high","low","open","vol","vol_cur","server_time","close") VALUES (?,?,?,?,?,?,?);';
        mssql.query(query_insert, [
            returnJson.ticker.high,
            returnJson.ticker.low,
            returnJson.ticker.buy,
            returnJson.ticker.vol,
            returnJson.ticker.vol_cur,
            returnJson.ticker.server_time,
            returnJson.ticker.buy // close
        ],
            {
                success: function(results) {
                }, error: function(err) {
                    console.log('Unable to log current currency ticker ' + currencypair + '' + err);
                }
            });
    }

    function insertTickerData_Kraken(returnJson, currencypair, source) {
        var pushTable = source + '_price_' + currencypair;

        var pairName2 = "X" + currencypair.replace("_", "Z").toUpperCase();
        var pairresult = returnJson.result[pairName2];

        var epochdate = new Date().valueOf() / 1000;

        var query_insert = 'INSERT INTO bitcoinbot.' + pushTable + ' ("high","low","open","vol","vol_cur","server_time","close") VALUES (?,?,?,?,?,?,?);';
        mssql.query(query_insert, [
            parseFloat(pairresult.h[0]),
            parseFloat(pairresult.l[0]),
            parseFloat(pairresult.b[0]), // buy
            parseFloat(pairresult.v[0]) * parseFloat(pairresult.b[0]),
            parseFloat(pairresult.v[0]),
            epochdate, // server time
            parseFloat(pairresult.b[0]) // close
        ],
            {
                success: function(results) {
                }, error: function(err) {
                    console.log('Unable to log current currency ticker ' + currencypair + ' for source:' + source + '. ' + err);
                }
            });
    }

    function insertTickerData_CexIO(returnJson, currencypair, source) {
        var pushTable = source + '_price_' + currencypair;

        var epochdate = parseInt(returnJson.timestamp);

        var query_insert = 'INSERT INTO bitcoinbot.' + pushTable + ' ("high","low","open","vol","vol_cur","server_time","close") VALUES (?,?,?,?,?,?,?);';
        mssql.query(query_insert, [
            parseFloat(returnJson.high),
            parseFloat(returnJson.low),
            parseFloat(returnJson.bid),
            parseFloat(returnJson.volume) * parseFloat(returnJson.bid),
            parseFloat(returnJson.volume),
            epochdate, // server time
            parseFloat(returnJson.bid)], // close
            {
                success: function(results) {
                }, error: function(err) {
                    console.log('Unable to log current currency ticker ' + currencypair + ' for source:' + source + '. ' + err);
                }
            });
    }

    function insertTickerData_Itbit(returnJson, currencypair, source) {
        var pushTable = source + '_price_' + currencypair;

        var epochdate = new Date().valueOf() / 1000;

        var query_insert = 'INSERT INTO bitcoinbot.' + pushTable + ' ("high","low","open","vol","vol_cur","server_time","close") VALUES (?,?,?,?,?,?,?);';
        mssql.query(query_insert, [
            parseFloat(returnJson.high),
            parseFloat(returnJson.low),
            parseFloat(returnJson.bid),
            parseFloat(returnJson.volume) * parseFloat(returnJson.bid),
            parseFloat(returnJson.volume),
            epochdate, // server time,
            parseFloat(returnJson.bid)], // close
            {
                success: function(results) {
                }, error: function(err) {
                    console.log('Unable to log current currency ticker ' + currencypair + ' for source:' + source + '. ' + err);
                }
            });
    }

    function insertTickerData_Bitstamp(returnJson, currencypair, source) {
        var pushTable = source + '_price_' + currencypair;

        var epochdate = parseInt(returnJson.timestamp);

        var query_insert = 'INSERT INTO bitcoinbot.' + pushTable + ' ("high","low","open","vol","vol_cur","server_time","close") VALUES (?,?,?,?,?,?,?);';
        mssql.query(query_insert, [
            parseFloat(returnJson.high),
            parseFloat(returnJson.low),
            parseFloat(returnJson.bid),
            parseFloat(returnJson.volume) * parseFloat(returnJson.bid),
            parseFloat(returnJson.volume),
            epochdate, // server time
            parseFloat(returnJson.bid)],
            {
                success: function(results) {
                }, error: function(err) {
                    console.log('Unable to log current currency ticker ' + currencypair + ' for source:' + source + '. ' + err);
                }
            });
    }

    function insertTickerData_Coinbase(returnJson, currencypair, source) {
        var pushTable = source + '_price_' + currencypair;

        var epochdate = new Date().valueOf() / 1000;


        var query_insert = 'INSERT INTO bitcoinbot.' + pushTable + ' ("high","low","open","vol","vol_cur","server_time","close") VALUES (?,?,?,?,?,?,?);';
        mssql.query(query_insert, [
            0,
            0,
            returnJson.amount, // buy
            0,
            0,
            epochdate,
            returnJson.amount], // buy
            {
                success: function(results) {
                }, error: function(err) {
                    console.log('Unable to log current currency ticker ' + currencypair + '' + err);
                }
            });
    }

    function insertTickerData_Okcoin(returnJson, currencypair, source) {
        var pushTable = source + '_price_' + currencypair;

        var epochdate = new Date().valueOf() / 1000;

        var query_insert = 'INSERT INTO bitcoinbot.' + pushTable + ' ("high","low","open","vol","vol_cur","server_time","close") VALUES (?,?,?,?,?,?,?);';
        mssql.query(query_insert, [
            returnJson.ticker.high,
            returnJson.ticker.low,
            returnJson.ticker.buy,
            parseFloat(returnJson.ticker.vol) * parseFloat(returnJson.ticker.buy),
            returnJson.ticker.vol,
            epochdate,
            returnJson.ticker.buy], // close
            {
                success: function(results) {
                }, error: function(err) {
                    console.log('Unable to log current currency ticker ' + currencypair + '' + err);
                }
            });
    }

    function insertTickerData_MTGox(returnJson, currencypair, source) {
        var pushTable = source + '_price_' + currencypair;

        var epochdate = parseInt(returnJson.data.now) / 1000000;

        var query_insert = 'INSERT INTO bitcoinbot.' + pushTable + ' ("high","low","open","vol","vol_cur","server_time","close") VALUES (?,?,?,?,?,?,?);';
        mssql.query(query_insert, [
            parseFloat(returnJson.data.high.value),
            parseFloat(returnJson.data.low.value),
            parseFloat(returnJson.data.buy.value),
            parseFloat(returnJson.data.vol.value) * parseFloat(returnJson.data.buy.value),
            parseFloat(returnJson.data.vol.value),
            epochdate, // server time],
            parseFloat(returnJson.data.buy.value)],
            {
                success: function(results) {
                }, error: function(err) {
                    console.log('Unable to log current currency ticker ' + currencypair + ' for source:' + source + '. ' + err);
                }
            });
    }

    function insertTickerData_btcChina(returnJson, currencypair, source) {
        var pushTable = source + '_price_' + currencypair;

        var epochdate = new Date().valueOf() / 1000;

        var query_insert = 'INSERT INTO bitcoinbot.' + pushTable + ' ("high","low","open","vol","vol_cur","server_time","close") VALUES (?,?,?,?,?,?,?);';
        mssql.query(query_insert, [
            returnJson.ticker.high,
            returnJson.ticker.low,
            returnJson.ticker.buy,
            parseFloat(returnJson.ticker.vol) * parseFloat(returnJson.ticker.buy),
            returnJson.ticker.vol,
            epochdate, // server time
            returnJson.ticker.buy],
            {
                success: function(results) {
                }, error: function(err) {
                    console.log('Unable to log current currency ticker ' + currencypair + '' + err);
                }
            });
    }
}