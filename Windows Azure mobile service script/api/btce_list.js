exports.post = function(request, response) {
    // Use "request.service" to access features of your mobile service, e.g.:
    //   var tables = request.service.tables;
    //   var push = request.service.push;

    response.send(statusCodes.OK, { message : 'Hello World!' });
};

exports.get = function(request, response) {
    // Get storage account settings from app settings. 
    var appSettings = require('mobileservice-config').appSettings;
    var pairs = appSettings.currencyPairs;
    
    response.send(statusCodes.OK, 
    { 
        message : pairs 
    });
};