
exports.post = function (request, response) {
    // Use "request.service" to access features of your mobile service, e.g.:
    //   var tables = request.service.tables;
    //   var push = request.service.push;

    //response.send(statusCodes.OK, { message: 'Hello World!' });
};

exports.get = function (request, response) {
    var operating_system = request.query.os;

    var message = 'Bitfinex ETH/USD & ETH/BTC pairs are now available.';
    switch (operating_system) {
        case 'Android':
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