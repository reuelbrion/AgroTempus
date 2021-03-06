"use strict";

var SERVICE_TYPE_RETRIEVE_FORECASTS = "retrieve_forecasts";
var SERVICE_TYPE_RETRIEVE_REGIONAL_DATA = "retrieve_regional_data";
var SERVICE_TYPE_STORE_WEATHER_DATA = "store_weather_data";
var SERVICE_TYPE_RETRIEVE_COMPUTATION_RESULTS =  "retrieve_computation_results";
var SERVICE_TYPE_OFFLOAD_REGRESSION =  "offload_regression";
var SERVICE_TYPE_OFFLOAD_PREDICTION =  "offload_prediction";
var TIMEOUT_SOCKET_CONNECT = 5000; //ms

var surrogateList = [];

function updateSurrogateList(){
	//in storage.js
	getSurrogateList(loadSurrogateListCallback);
}

function loadSurrogateListCallback(inList){
	surrogateList = inList;
	//in app.js
	addLocationElements(surrogateList);
}

function getSurrogate(serviceType, surrogateListClone, callback, args){
	//TODO check callback is function
	if(surrogateList.length < 1){
		//TODO: try to update surrogate list
		callback(null);
		return;
	}
	if(surrogateListClone == null || surrogateListClone === undefined){
		//deep copy hack
		surrogateListClone = JSON.parse(JSON.stringify(surrogateList));
	}
	if(surrogateListClone.length < 1 || surrogateListClone == null){
		callback(null);
		return;
	}
	var chosenSurrogate = getHighestWeightSurrogate(surrogateListClone);
	surrogateListClone.splice(surrogateListClone.indexOf(chosenSurrogate), 1);
	var surrogatePort = getSurrogatePort(serviceType, chosenSurrogate);
	if(surrogatePort == "unknown"){
		//TODO
	}
	console.log("trying to connect to surrogate " + chosenSurrogate.location + " - " 
		+ chosenSurrogate.country + " at " + chosenSurrogate.IP + " - "  + surrogatePort 
		+ " for " + serviceType);
	var socket = navigator.mozTCPSocket.open(chosenSurrogate.IP, surrogatePort);
	var timeout = setTimeout(function () {
	    	socket.close();
	    	console.info("-> connection attempt to socket timed out: "  + surrogatePort + " - " + chosenSurrogate.IP + " -> ");
		}, TIMEOUT_SOCKET_CONNECT);
	//establishing connection fails
	socket.onerror = function(event){
		clearTimeout(timeout);	
		console.info("-> opening surrogate socket failed for: "  + socket.port + " - " + socket.host + " -> " + event.data.name);
		//in app.js
		connectionBroken();	
		getSurrogate(serviceType, surrogateListClone, callback, args);
	};
	//end establishing connection fails
	
	//connection succeeds
	socket.onopen = function(event){
		clearTimeout(timeout);
		console.info("-> connection to surrogate opened: " + socket.port + " - " + socket.host + "\n");	
		//in app.js
		connectionEstablished();
		socket.onerror = function(event){			
			console.info("-> something went wrong during connection with surrogate, connection lost: "  + socket.port + " - " + socket.host + " - " + event.data.name);
			//in app.js
			connectionBroken();
			surrogateListClone.push(chosenSurrogate);
			getSurrogate(serviceType, surrogateListClone, callback, args);
		};
		chosenSurrogate.weight++;
		//TODO: weight algorithm
		var serviceRequest = new Object();
		serviceRequest.type = serviceType;
		var sendStr = JSON.stringify(serviceRequest);
		sendStr+="\n";
		sendStr = sendStr.toString('utf-8');
		socket.send(sendStr);
		
		//check if surrogate provides this service
		socket.ondata = function (event) {
			if (typeof event.data === 'string') {
				var response = JSON.parse(event.data);
				if(response.response == "ok"){
					callback(socket, chosenSurrogate, args);
					return;
				}
			} 
			//TODO: define socket.onclose
			socket.close();
			getSurrogate(serviceType, surrogateListClone, callback, args);
		};
	};
	//end connection succeeds
}

//Not sure if needed
//function getSpecificSurrogate(location, serviceType, callback){
//	for(var i = 0; i < surrogateList.length; i++){
//		if (surrogateList[i].location + " - " + surrogateList[i].country == location){
//			connectToSurrogate(surrogateList[i], serviceType, callback);
//			return;
//		}
//	}
//	callback(null);
//}
//
//function connectToSurrogate(surrogate, serviceType, callback){
//	var surrogatePort = getSurrogatePort(serviceType, surrogate);
//
//	if(surrogatePort == "unknown"){
//		//TODO
//	}
//	console.log("trying to connect to surrogate " + surrogate.location + " - " 
//		+ surrogate.country + " at " + surrogate.IP + " - "  + surrogatePort 
//		+ " for " + serviceType);
//	var socket = navigator.mozTCPSocket.open(surrogate.IP, surrogatePort);
//	
//	//establishing connection fails
//	socket.onerror = function(event){
//		//in app.js
//		connectionBroken();
//		console.info("-> opening surrogate socket failed for: "  + socket.port + " - " + socket.host + " -> " + event.data.name);
//		callback(null);
//	}
//	//end establishing connection fails
//	
//	//connection succeeds
//	socket.onopen = function(event){
//		console.info("-> connection to surrogate opened: " + socket.port + " - " + socket.host + "\n");	
//		socket.onerror = function(event){			
//			console.info("-> something went wrong during connection with surrogate, connection lost: "  + socket.port + " - " + socket.host + " - " + event.data.name);
//			//in app.js
//			connectionBroken();
//			alert("connection broken, surrogate: " + surrogate.location + " - " + surrogate.country);
//		}
//		surrogate.weight++;
//		//TODO: weight algorithm
//		var serviceRequest = new Object();
//		serviceRequest.type = serviceType;
//		var sendStr = JSON.stringify(serviceRequest);
//		sendStr+="\n";
//		sendStr = sendStr.toString('utf-8');
//		socket.send(sendStr);
//		//in app.js
//		connectionEstablished();
//		//check if surrogate provides this service
//		socket.ondata = function (event) {
//			var hasService = false;
//			if (typeof event.data === 'string') {
//				var response = JSON.parse(event.data);
//				if(response.response == "ok"){
//					callback(socket, surrogate);
//					hasService = true;
//				}
//			} else {
//				callback(null);
//			}
//			if(!hasService){
//				callback(null);
//			}
//		}
//	}
//	//end connection succeeds
//}

function getHighestWeightSurrogate(inList){
	var chosenSurrogate = inList[0];
	var highestWeight = chosenSurrogate.weight;
	for(var i = 0; i < inList.length; i++){
		if (inList[i].weight > highestWeight){
			chosenSurrogate = inList[i];
		}
	}
	return chosenSurrogate;
}

function getSurrogatePort(serviceType, surrogate){
	//TODO: checking for correct input
	var port = "unknown";
	if(serviceType == SERVICE_TYPE_STORE_WEATHER_DATA){
		port = surrogate.storageserverport;
	}
	else if(serviceType == SERVICE_TYPE_RETRIEVE_REGIONAL_DATA){
		port = surrogate.requestserverport;
	}
	else if(serviceType == SERVICE_TYPE_RETRIEVE_FORECASTS){
		port = surrogate.requestserverport;
	}
	else if(serviceType == SERVICE_TYPE_RETRIEVE_COMPUTATION_RESULTS){
		port = surrogate.requestserverport;
	}
	else if(serviceType == SERVICE_TYPE_OFFLOAD_REGRESSION){
		port = surrogate.offloadserverport;
	}
	else if(serviceType == SERVICE_TYPE_OFFLOAD_PREDICTION){
		port = surrogate.offloadserverport;
	}
	return port;
}