
exports.post = function (request, response) {
    // Use "request.service" to access features of your mobile service, e.g.:
    //   var tables = request.service.tables;
    //   var push = request.service.push;

    //response.send(statusCodes.OK, { message: 'Hello World!' });
};

exports.get = function (request, response) {
    var operating_system = request.query.os;

    var message = '';
    switch (operating_system) {
        case 'Android':
            message = '[12/01/2015]Added XMR/BTC and XMR/USD pairs for Bitfinex\r\n\r\n[08/26/2015] Added Litecoin pairs for GDAX (Coinbase Exchange) \r\n\r\n[07/25/2015] Added Ethereum Classic pairs for Bitfinex and Kraken \r\n\r\n[05/27/2016] Kraken DAO/XBT, DAO/ETH, DAO/EUR, DAO/USD, DAO/CAD, DAO/GBP, & DAO/JPY pairs are now supported\r\n\r\n[05/16/2016] GDAX (Coinbase) ETH/BTC and ETH/USD pairs are now supported\r\n\r\n';
            break;
        case 'Windows':
            break;
        case 'iOS':
            break;
    }
    
    response.send(statusCodes.OK,
        {
            message: message
        });
};