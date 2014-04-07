exports.post = function(request, response) {
    var appSettings = require('mobileservice-config').appSettings;
    
    // check source
    var header = request.header('Server');
    if (header != appSettings.BitBotServer_UserAgentHost) {
        response.send(statusCodes.BAD_REQUEST, { message : 'lulz' });
        return;
    }

    var nonce =  parseInt(request.query.nonce);
    var currencypair = escape(request.query.currencypair); // eg: btc_usd
    var depth = parseInt(request.query.depth);
    var hours = parseInt(request.query.hours);
    var start_server_time = parseInt(request.query.start_server_time);
    var exchangesite = escape(request.query.exchangesite);

    // Input validation for currency pairs
    var pairs = appSettings.currencyPairs;
    
    var arr = pairs.split("---");
    var verifiedCurrencyPair = false;
    for(var i=0;i<arr.length;i++) {
        var source_pair = arr[i].split("-");
        if (source_pair[1] == currencypair && source_pair[0] == exchangesite) {
            verifiedCurrencyPair = true;
            break;
        }
     }
    
    if (depth <= 0 || depth > 1000000 || !verifiedCurrencyPair || hours <= 0 || hours > 24 || start_server_time < 0)
    {
       response.send(statusCodes.BAD_REQUEST, 
       { 
           message : 'lulz' 
       });
       return;
    }
    selectTop(depth, currencypair, hours, exchangesite);


    function selectTop(depth, currencypair, hours, exchangesite) {
        var mssql = request.service.mssql;
        // btce_price_btc_usd
        
        var tableName = exchangesite + "_price_"+currencypair;
        var query = "SELECT TOP "+depth+"  high, low, avg, buy, vol, vol_cur, server_time, sell, last, updated FROM "+tableName+" WHERE __createdAt < dateadd(hh, + "+hours+", getdate()) AND server_time > ? ORDER BY server_time ASC;";

          mssql.query(query, [start_server_time], 
          {
              success: function(result) {
                  // Cache result first
                 /* var query2 = "UPDATE cache_price SET cached_string = '?' WHERE exchange_currency_pair = '?'" +
                  " IF @@ROWCOUNT=0" +
                  "  BEGIN" +
                  "    INSERT INTO cache_price (cached_string, exchange_currency_pair, trendtime_hours, depth) VALUES ('?','?', ?, ?);" +
                  "  END";
                  
                  var jsonstr = JSON.stringify(result);// new Buffer(JSON.stringify(result)).toString('base64');
                  console.log(jsonstr.length);
console.log(jsonstr);
console.log(query2);
//new Buffer("SGVsbG8gV29ybGQ=", 'base64').toString('ascii'))

                  mssql.query(query2, [jsonstr, tableName, jsonstr, tableName, hoursSelection, depthSelection], 
                  {
                      success: function(result) {
                          console.log("Successfully cached 24 hour trend for: " + tableName);
                      },
                      error: function (err) {
                               console.log("error acquiring cached 24 hour trend: " + err);
                      }
                  });*/
                  // ok, sent result to user
                  response.send(statusCodes.OK, result);
                },
                error: function (err) {
                     console.log("error acquiring current db for graph: " + err);
                     response.send(statusCodes.OK, { message : 'error connecting to database' });
                }
           });
    }
    
   /* function selectCachedResult(depthSelection, currencypair, hoursSelection) {
        var tables = request.service.tables;
        var table = tables.getTable('cache_price');
        var tableName = "btce_price_"+currencypair;
        
        table.where({
            exchange_currency_pair: tableName
            }).read({
                success: function(results) 
                {
                    if (results.length !== 0) {
                        var item = results[0];
                        
                        response.send(statusCodes.OK, new Buffer(item.cached_string, 'base64').toString('ascii'));
                    } 
                    else 
                    {
                        selectTop(depthSelection, currencypair, hoursSelection);
                    }
                }, 
                error: function(err) 
                {
                    response.send(statusCodes.OK, { message : 'error connecting to database' });
                }
            });
    }*/
   
};

exports.get = function(request, response) {
    response.send(statusCodes.OK, { message : 'Hello World!' });
};