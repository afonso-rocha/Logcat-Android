module.exports = {
	sendLogs:function(successCB,failureCB){
		cordova.exec(successCB(data), failureCB(data), "LogCat","sendLogs", []);
	}
};

