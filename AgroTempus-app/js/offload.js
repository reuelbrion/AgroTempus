"use strict";
var requestedOffloadCallback;

function requestOffload(offloadParams, serviceType, callback){
	if (!(typeof(callback) === "function")){
		//TODO: error handling
	}
	console.info(JSON.stringify(offloadParams));
	//discovery.js
	requestedOffloadCallback = callback;
	callback("requesting");
	getSurrogate(serviceType, null, requestOffloadCallback, offloadParams);	
}

function requestOffloadCallback(surrogateSocket, surrogate, offloadParams){
	if(surrogateSocket == null){
		requestedOffloadCallback("failed");
	}
	else{
		console.info("Ready to request offload.");
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
					//in dataexchange.js
					addTicket(inData);
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