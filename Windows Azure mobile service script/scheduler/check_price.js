﻿function check_price() {
    var SendGrid = require('sendgrid').SendGrid;
    var util = require('util');

    var date = new Date();
    var dateStr = date.toISOString().replace(/T/, ' ').replace(/\..+/, '');
    console.log("Scheduled price check task: " + dateStr);

    // start
    fetchItem('btc_usd', 'btce');
    fetchItem('btc_eur', 'btce');
    fetchItem('btc_rur', 'btce');
    //fetchItem('btc_cnh', 'btce');
    //fetchItem('btc_gbp', 'btce');
    fetchItem('ltc_btc', 'btce');
    fetchItem('ltc_usd', 'btce');
    fetchItem('ltc_rur', 'btce');
    fetchItem('ltc_eur', 'btce');
    //fetchItem('ltc_cnh', 'btce');
    //fetchItem('ltc_gbp', 'btce');
    fetchItem('nmc_usd', 'btce');
    fetchItem('nmc_btc', 'btce');
    fetchItem('usd_rur', 'btce');
    //fetchItem('usd_cnh', 'btce');
    //fetchItem('gbp_usd', 'btce');
    fetchItem('eur_usd', 'btce');
    fetchItem('nvc_usd', 'btce');
    fetchItem('nvc_btc', 'btce');
    //fetchItem('trc_btc', 'btce');
    fetchItem('ppc_usd', 'btce');
    fetchItem('ppc_btc', 'btce');
    //fetchItem('ftc_btc', 'btce');
    //fetchItem('xpm_btc', 'btce');

    fetchItem('btc_cny', 'btcchina');
    fetchItem('ltc_cny', 'btcchina');
    fetchItem('ltc_btc', 'btcchina');

    //fetchItem('btc_usd', 'mtgox');

    fetchItem('btc_cny', 'okcoin');
    fetchItem('ltc_cny', 'okcoin');
    fetchItem('btc_usd', 'okcoininternational');
    fetchItem('ltc_usd', 'okcoininternational');
    //fetchItem('btc_cny', 'huobi');
    //fetchItem('ltc_cny', 'huobi');

    fetchItem('btc_usd', 'coinbase');

    fetchItem('btc_usd', 'bitstamp');

    fetchItem('xbt_usd', 'itbit');
    fetchItem('xbt_sgd', 'itbit');
    fetchItem('xbt_eur', 'itbit');

    fetchItem('btc_usd', 'bitfinex');
    fetchItem('ltc_usd', 'bitfinex');
    fetchItem('ltc_btc', 'bitfinex');
    //fetchItem('drk_usd', 'bitfinex');
    //fetchItem('drk_btc', 'bitfinex');

    fetchItem('btc_usd', 'campbx');

    fetchItem('xbt_usd', 'kraken');
    fetchItem('xbt_eur', 'kraken');
    fetchItem('eth_xbt', 'kraken');

    fetchItem('ghs_btc', 'cexio');

    fetchItem('btc_sgd', 'fybsg');
    fetchItem('btc_sek', 'fybse');

    //fetchItem('nxt_btc', 'dgex');

    /*   fetchItem('btc_usd', 'cryptsy');
       fetchItem('doge_usd', 'cryptsy');
       fetchItem('drk_usd', 'cryptsy');
       fetchItem('ftc_usd', 'cryptsy');
       fetchItem('ltc_usd', 'cryptsy');
       fetchItem('rdd_usd', 'cryptsy');
       fetchItem('nxt_btc', 'cryptsy');
       fetchItem('ltc_btc', 'cryptsy');
       fetchItem('doge_btc', 'cryptsy');
       fetchItem('cann_btc', 'cryptsy');
       fetchItem('drk_btc', 'cryptsy');
       fetchItem('rdd_btc', 'cryptsy');
       fetchItem('uro_btc', 'cryptsy');
       fetchItem('bc_btc', 'cryptsy');
       fetchItem('btcd_btc', 'cryptsy');*/

    fetchItem('btc Futures_usd', '_796');
    fetchItem('btc Futures_cny', '_796');
    fetchItem('ltc Futures_usd', '_796');

    function fetchItem(currencypair, source) {
        try {
            if (source == 'btce') {
                // reduce traffic on a single IP due to BTCe cloudflare
                if (Math.floor((Math.random() * 1) + 1) == 1) {
                    fetchfromSource(currencypair, source, 'https://btc-e.com/api/2/' + currencypair + '/ticker');
                } else {
                    fetchfromSource(currencypair, source, 'http://maaapersonalppace.azurewebsites.net/httprelay.php?url=' + escape('https://btc-e.com/api/2/' + currencypair + '/ticker'));
                }
            } else if (source == 'btcchina') {
                fetchfromSource(currencypair, source,
                    currencypair == 'ltc_cny' ? 'https://data.btcchina.com/data/ticker?market=cnyltc' :
                        currencypair == 'btc_cny' ? 'https://data.btcchina.com/data/ticker?market=cnybtc' :
                            'https://data.btcchina.com/data/ticker?market=btcltc');
            } else if (source == 'mtgox') {
                fetchfromSource(currencypair, source, 'http://data.mtgox.com/api/2/BTCUSD/money/ticker');
            } else if (source == 'okcoin') {
                fetchfromSource(currencypair, source, 'https://www.okcoin.cn/api/ticker.do?symbol=' + currencypair);
            } else if (source == 'okcoininternational') {
                fetchfromSource(currencypair, source, 'https://www.okcoin.com/api/ticker.do?symbol=' + currencypair + '&ok=1');
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
                switch (currencypair) {
                    case 'btc':
                        {
                            fetchfromSource(currencypair, source, 'http://market.huobi.com/staticmarket/kline001.html');
                            break;
                        }
                    case 'ltc':
                        {
                            fetchfromSource(currencypair, source, 'http://market.huobi.com/staticmarket/kline_ltc001.html');
                            break;
                        }
                }
            } else if (source == '_796') {
                if (currencypair.indexOf('cny') > -1) {
                    fetchfromSource(currencypair, source, 'http://api.796.com/v3/futures/ticker.html?type=btccnyweeklyfutures');
                } else if (currencypair.indexOf('btc') > -1) {
                    fetchfromSource(currencypair, source, 'http://api.796.com/v3/futures/ticker.html?type=weekly');
                } else {
                    fetchfromSource(currencypair, source, 'http://api.796.com/v3/futures/ticker.html?type=ltc');
                }
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
            } else if (source == 'dgex') {
                fetchfromSource(currencypair, source, 'https://dgex.com/API/nxtprice.txt');
            } else if (source == 'cryptsy') {
                var marketid = GetCryptsyMarketId(currencypair);
                fetchfromSource(currencypair, source, 'http://pubapi.cryptsy.com/api.php?method=singlemarketdata&marketid=' + marketid);
            }
        } catch (ex) {
            console.log(ex);
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
            maxRedirects: 5,
            headers: {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.9600'
            }
        }, function (error, response, body) {
                if (!error && response.statusCode == 200) {
                    var returnJson = source === 'dgex' ? null : JSON.parse(body);

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
                            {
                                var isKraken = currencypair.indexOf('eth') > -1;
                                var pairName2 = "X" + currencypair.replace("_", isKraken ? "X" : "Z").toUpperCase();
                                var pairresult = returnJson.result[pairName2];

                                currentPrice = parseFloat(pairresult.b[0]);
                                average24Hrs = currentPrice / (parseFloat(pairresult.h[0]) + parseFloat(pairresult.l[0]) / 2) * 100 - 100;
                                break;
                            }
                        case 'dgex':
                            currentPrice = parseFloat(body);
                            average24Hrs = 0; // no data available
                            break;
                        case '_796':
                            {
                                var tickerJson = returnJson.ticker;

                                currentPrice = parseFloat(tickerJson.last);
                                average24Hrs = currentPrice / ((parseFloat(tickerJson.high) + parseFloat(tickerJson.low)) / 2) * 100 - 100;
                                break;
                            }
                        case 'cryptsy':
                            {
                                var success = parseInt(returnJson['success']);
                                if (success == 1) {
                                    var split = currencypair.split("_");

                                    var ret = returnJson['return'];
                                    var market = ret['markets'];
                                    var pairMarket = market[split[0].toUpperCase()];

                                    currentPrice = parseFloat(pairMarket["lasttradeprice"]);
                                    average24Hrs = 0; // not available
                                } else {
                                    return;
                                }
                                break;
                            }
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

                    var query = "SELECT * FROM push_price WHERE (low >= ? OR high <= ? OR (increment_percent_daily <= ? AND increment_percent_daily != 0)) AND __updatedAt < dateadd(ss, - (SELECT delay_between_notification), getdate()) AND exchange_pair = ? AND times > 0;";
                    mssql.query(query, [currentPrice, currentPrice, average24Hrs, source + '_' + currencypair], {
                        success: function (results) {
                            if (results.length > 0) {
                                // Permission record was found. Continue normal execution. 
                                console.log('%d items available for %s - %s %d', results.length, source, currencypair, average24Hrs);

                                for (var i = 0; i < results.length; i++) {
                                    var item = results[i];
                                    alertClient(item, currentPrice, average24Hrs, currencypair, currencyPair_str, sqlTable, date, source);
                                }
                            } else {
                                //console.log('%d items available for %s', results.length, currencypair);
                            }
                        }, error: function (err) {
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
        var email = client.email;

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

                (isFalling ? (currencypairStr + "↓ fell below " + clientLow + ". [" + source + "]") :
                    (currencypairStr + "↑ went above " + clientHigh + ". [" + source + "]"));
        var alertMsg_Tile = (((isFalling ? clientLow : clientHigh) / currentPrice * 100 - 100) * -1).toFixed(5) +
            "% - " + currencypairStr + "\r\n\r\nPrice: $" + currentPrice;

        var alertMsg_ToastNavigateURI = '/DetailedPricePage.xaml?QuoteDataSource=' + source + '&CurrencyPair=' + currencypair;

        // Sent email
        if (email != null && email !== '') {
            //var emailMsg = util.format('<b>%s</b>:<br>Current Price:%d<br>', source, currentPrice);
            var emailMsg = util.format('Exchange: %s - %s\r\nCurrent Price:%d', source, currencypairStr, currentPrice);
            sendEmail("Bitbot - " + alertMsg_Toast, email, emailMsg);
        }

        // Start sending
        if (platform == "Android") {
            var payload = {
                "data": {
                    "message": alertMsg_Toast,
                    "title": currentPrice.toString(),
                }
            };
            push.gcm.send(clientUniqueId, payload, {
                success: function (pushResponse) {
                    console.log("Sent push:", pushResponse, payload);
                },
                error: function (error) {
                    console.log("Error Sending push:", error);

                    if (error.statusCode == 410 || error.statusCode == 404 || error.statusCode == 412) {
                        removeExpiredChannel(sqlTable, client.pushuri, clientuniqueid);
                    }
                }
            });
        },
    } else if (platform == "WindowsPhone8" || platform == "WindowsPhone8.1") {
        push.mpns.sendToast(clientUniqueId,
            {
                text1: currentPrice.toString(),
                text2: alertMsg_Toast,
                param: alertMsg_ToastNavigateURI,
                Sound: 'Assets/CustomRingtone/Cat.wav'
            }, {
                success: function (response) {
                    // pinned tile
                    push.mpns.sendTile(clientUniqueId, {
                        count: 1,
                        id: '/DetailedPricePage.xaml?QuoteDataSource=' + source + '&CurrencyPair=' + currencypair,
                    }, {
                            success: function (pushResponse) {
                                //console.log("Sent push:", pushResponse);

                                updateNotificationCount(sqlTable, client.pushuri, client, source, currencypair, platform);
                            }
                        });
                    // sent another tile update to the main
                    push.mpns.sendTile(clientUniqueId, {
                        count: 1,
                        backContent: currentPrice.toString() + ':' + alertMsg_Toast,
                    }, {
                            success: function (pushResponse) {
                            }
                        });
                },
                error: function (error) {
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
    } else if (platform == "Windows8" || platform == "Windows8.1") {
        push.wns.sendToastImageAndText04(clientUniqueId, {
            text1: currentPrice.toString(),
            text2: alertMsg_Toast,
            text3: "",
            image: "ms-appx:///Assets/ToastImage.png"
        }, {
                launch: alertMsg_ToastNavigateURI,
                duration: 'long',
            }, {
                success: function (pushResponse) {
                    push.wns.sendBadge(clientUniqueId, {
                        value: 1,
                        text1: alertMsg_Toast
                    }, {
                            success: function (pushResponse) {
                                updateNotificationCount(sqlTable, client.pushuri, client, source, currencypair, platform);
                            }
                        });
                }, error: function (error) {
                    // expired channel
                    if (error.statusCode == 410 || error.statusCode == 404 || error.statusCode == 412) {
                        removeExpiredChannel(sqlTable, client.pushuri, clientuniqueid);
                    }
                    console.log(error);
                }
            }, {
                error: function (error) {
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
                success: function (results) {
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
            success: function (results) {
                console.log('Removed Expired Channel: ' + pushuri)
            }
        });
}

function insertTickerData(returnJson, currencypair, source) {
    /*  if (
          (source == 'kraken' && currencypair == 'xbt_usd') ||
          (source == 'kraken' && currencypair == 'xbt_eur')
          ) {
 
          // Insert only if we've yet to support these exchange to cache at second interval
          // via an external VPS.
      }*/
}

function sendEmail(subject, toEmail, context) {
    var sendgrid;
    if (Math.floor((Math.random() * 1) + 1) == 1) // double the amount of free shit.. lol
        sendgrid = new SendGrid('azure_d44639a993ad34c480950382904b1ba8@azure.com', '7WaCk2st2Zn7Pyu');
    else
        sendgrid = new SendGrid('azure_98436b19da666fc83288cb45033966f2@azure.com', 'cj9X26r1R15SN7x');

    sendgrid.send({
        to: toEmail,
        from: 'bitbotlive@outlook.com',
        subject: subject,
        text: context
    }, function (success, message) {
            // If the email failed to send, log it as an error so we can investigate
            if (!success) {
                console.error(message);
            }
        });
}

function GetCryptsyMarketId(pair) {
    switch (pair) {
        case "ltc_usd":
            return 1;
        case "btc_usd":
            return 2;
        case "ltc_btc":
            return 3;
        case "ftc_usd":
            return 6;
        case "doge_btc":
            return 132;
        case "drk_btc":
            return 155;
        case "nxt_btc":
            return 159;
        case "rdd_btc":
            return 169;
        case "bc_btc":
            return 179;
        case "doge_usd":
            return 182;
        case "drk_usd":
            return 213;
        case "uro_btc":
            return 247;
        case "btcd_btc":
            return 256;
        case "rdd_usd":
            return 262;
        case "cann_btc":
            return 300;
    }
    return -1;
}
}