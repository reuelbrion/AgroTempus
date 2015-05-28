"use strict";
var requestedOffloadCallback;

function requestOffload(offloadParams, serviceType, callback){
	if (!(offloadParams.startDate instanceof Date)){
		//TODO: error handling
	}
	if (!(typeof(callback) === "function")){
		//TODO: error handling
	}
	getSurrogate(serviceType, null, requestOffloadCallback, offloadParams);
	callback("requesting");
	requestedOffloadCallback = callback;
}

function requestOffloadCallback(surrogateSocket, offloadParams){
	if(surrogateSocket == null){
		requestedOffloadCallback("failed");
	}
	else{
		console.info("Ready to request offload.");
		var sendStr = JSON.stringify(offloadParams);
		sendStr+="\n";
		surrogateSocket.send(sendStr.toString('utf-8'));
		console.info("Sent:\n" + sendStr);
		surrogateSocket.ondata = function (event) {
			if (typeof event.data === 'string') {
				//TODO: error checking
				var reply = JSON.parse(event.data);
				surrogateSocket.onclose = function (event) {
					alert(event.data);
					//requestedOffloadCallback();
				}
			} else {
				surrogateSocket.onclose = function (event) {
					alert("error");
				}
			}
			surrogateSocket.close();
		}
	}
}
