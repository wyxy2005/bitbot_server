exports.post = function(request, response) {
    // Use "request.service" to access features of your mobile service, e.g.:
    //   var tables = request.service.tables;
    //   var push = request.service.push;
    var uniqueid = escape(request.query.uniqueid);
    
    var result = deleteItemFromDB(uniqueid);
    
    response.send(statusCodes.OK, { message : (result ? 'ok' : 'fail') });
    
    function deleteItemFromDB(uniqueid) {
        var mssql = request.service.mssql;
        var query = "DELETE from push_price WHERE uniqueid = ?";
         
        // console.log(query);
         
          mssql.query(query, [uniqueid], {
               success: function(results) {            
                }, error:function(err) {
                    console.log("error updating current db: " + err); // ok doesn't really matter'
                    return false;
                }
          }
          );
          return true;
    }
};

exports.get = function(request, response) {
    response.send(statusCodes.OK, { message : 'Hello World!' });
};