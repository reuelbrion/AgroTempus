"use strict";
var requestedOffloadCallback;

function requestOffload(offloadParams, serviceType, callback){
	if (!(typeof(callback) === "function")){
		//TODO: error handling
	}
	//discovery.js
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
		console.info(offloadParams);
		var sendStr = JSON.stringify(offloadParams);
		sendStr+="\n";
		surrogateSocket.send(sendStr.toString('utf-8'));
		console.info("Sent:\n" + sendStr);
		//TODO: add timeout
		surrogateSocket.ondata = function (event) {
			var status = "failed";
			var inData = "";
			if (typeof event.data === 'string') {
				//TODO: error checking
				var reply = JSON.parse(event.data);
				if(reply.response == "success"){
					status = "success";
					inData = reply.ticket;
				} else {
					status = "unknown";
				}
			} 
			surrogateSocket.onclose = function (event) {
					requestedOffloadCallback(status, inData);
			}
			surrogateSocket.close();
		}
	}
}