/*module.exports = {
	sendLogs:function(successCB,failureCB){
		cordova.exec(successCB(), failureCB(), "LogCat","sendLogs", []);
	}
};*/

var exec = require('cordova/exec');

exports.sendLogs = function (arg0, success, error) {
    exec(success, error, 'LogCat', 'sendLogs', [arg0]);
};
