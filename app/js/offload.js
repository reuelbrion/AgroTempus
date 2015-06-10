"use strict";
var requestedOffloadCallback;

function requestOffload(offloadParams, serviceType, callback){
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
		console.info(offloadParams);
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


/*function pullDataCallback(surrogateSocket, args){
	if(surrogateSocket == null){
		//TODO: no surrogate found
		return;
	}
	//initialize new pull data request, if there is no inData object yet
	var inData = "";
	console.info("Ready to request regional data.\n");
	var request = new Object();
	request.start_time = args[0];
	request.end_time = args[1];
	var sendStr =  JSON.stringify(request) + "\n";
	console.info("Request sent:\n" + sendStr);
	surrogateSocket.send(sendStr.toString('utf-8'));
	
	surrogateSocket.ondata = function (event) {
		if (typeof event.data === 'string') {
			inData += event.data;
			if(event.data.substr(event.data.length - 1) == "\n" || event.data.substr(event.data.length - 1) == "]"){
				var response = new Object();
				response.response = "ok";
				var sendStr = JSON.stringify(response) + "\n";
				surrogateSocket.send(sendStr.toString('utf-8'));
				requestedDataCallback("ready", inData, args);
				surrogateSocket.onclose = function (event) {
					console.info("closed socket to surrogate");
				}
				surrogateSocket.close();
			} 
		} else {
			var response = new Object();
			response.response = "unknown";
			var sendStr = JSON.stringify(response) + "\n";
			surrogateSocket.send(sendStr.toString('utf-8'));
			requestedDataCallback("failed");
			surrogateSocket.onclose = function (event) {
				console.info("closed socket to surrogate");
			}
			surrogateSocket.close();
		}
	}	
}*/