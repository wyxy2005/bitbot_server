exports.post = function(request, response) {
    // Use "request.service" to access features of your mobile service, e.g.:
    //   var tables = request.service.tables;
    //   var push = request.service.push;
    
    var key = request.query.clientkey;
    var toEncrypt = request.query.clienttext;
    var nonce =  parseInt(request.query.nonce);

    var crypto = require('crypto');
    
    var keybuffer = new Buffer(key, 'utf-8');
    var signer = crypto.createHmac('sha512', keybuffer);
    var byteresult = signer.update(toEncrypt).digest('hex');

//console.log(byteresult);
//console.log("Text: " + toEncrypt);
//console.log("Buffer: " + keybuffer);

    var result = byteresult.replace("-", "").toLowerCase();

    console.log(result);
    
    response.send(statusCodes.OK, 
    { 
        message: 'ok',
        hash : result 
    });
};

exports.get = function(request, response) {
    response.send(statusCodes.OK, { hash : 'error' });
};