"use strict";
var SERVICE_TYPE_RETRIEVE_FORECASTS = "retrieve_forecasts";
var SERVICE_TYPE_RETRIEVE_REGIONAL_DATA = "retrieve_regional_data";
var SERVICE_TYPE_STORE_WEATHER_DATA = "store_weather_data";
var SERVICE_TYPE_RETRIEVE_COMPUTATION_RESULTS =  "retrieve_computation_results";
var SERVICE_TYPE_OFFLOAD_REGRESSION =  "offload_regression";
var SERVICE_TYPE_OFFLOAD_PREDICTION =  "offload_prediction";

var surrogateList = [];
//TODO: retrieve surrogates from data store instead of hardcoding
var surrogate = {
	"location" : "Amsterdam",
	"country" : "NL",
	"lat" : "52.379",
	"long" : "4.899",
	"IP" : "localhost",
	"storageServerPort" : 11112,
	"requestServerPort" : 11113,
	"offloadServerPort" : 11114,
	"weight" : 1
};
surrogateList.push(surrogate);
var surrogate = {
	"location" : "Breda",
	"country" : "NL",
	"lat" : "33.379",
	"long" : "1.899",
	"IP" : "195.240.53.133",
	"storageServerPort" : 11112,
	"requestServerPort" : 11113,
	"offloadServerPort" : 11114,
	"weight" : 0
};
surrogateList.push(surrogate);

function getSurrogate(serviceType, surrogateListClone, callback, args){
	//TODO check callback is function
	if(surrogateList.length < 1){
		//TODO: try to update surrogate list
		callback(null);
	}
	if(surrogateListClone == null || surrogateListClone === undefined){
		surrogateListClone = JSON.parse(JSON.stringify(surrogateList));
	}
	if(surrogateListClone.length < 1){
		callback(null);
	}
	
	var chosenSurrogate = getHighestWeightSurrogate(surrogateListClone);
	surrogateListClone.splice(surrogateListClone.indexOf(chosenSurrogate), 1);
	var surrogatePort = getSurrogatePort(serviceType, chosenSurrogate);
	var socket = navigator.mozTCPSocket.open(chosenSurrogate.IP, surrogatePort);
	
	//establishing connection fails
	socket.onerror = function(event){
		console.info("-> opening surrogate socket failed for: "  + socket.port + "\n" + socket.host + "\n" + event.data.name);
		getSurrogate(serviceType, surrogateListClone, callback);
	}
	//end establishing connection fails
	
	//start connection
	socket.onopen = function(event){
		console.info("-> connection to surrogate opened: " + socket.port + "\n" + socket.host + "\n");	
		//TODO: when connection breaks after a connection existed, we probably want to try again more than once with the same surrogate.
		socket.onerror = function(event){
			console.info("-> something went wrong during connection with surrogate, connection lost: "  + socket.port + "\n" + socket.host + "\n" + event.data.name);
			surrogateListClone.push(chosenSurrogate);
			getSurrogate(serviceType, surrogateListClone, callback, args);
		}
		chosenSurrogate.weight++;
		//TODO: weight algorithm
		var sendStr = "request-service\n" + serviceType + "\n";
		sendStr = sendStr.toString('utf-8');
		socket.send(sendStr);
		
		//check if surrogate provides this service
		socket.ondata = function (event) {
			if (typeof event.data === 'string' && event.data == "ok\n") {
				callback(socket, args);
			} else {
				getSurrogate(serviceType, surrogateListClone, callback, args);
			}
		}
	}
	//end connection
}

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
		port = surrogate.storageServerPort;
	}
	else if(serviceType == SERVICE_TYPE_RETRIEVE_REGIONAL_DATA){
		port = surrogate.requestServerPort;
	}
	else if(serviceType == SERVICE_TYPE_RETRIEVE_FORECASTS){
		port = surrogate.requestServerPort;
	}
	else if(serviceType == SERVICE_TYPE_RETRIEVE_COMPUTATION_RESULTS){
		port = surrogate.requestServerPort;
	}
	else if(serviceType == SERVICE_TYPE_OFFLOAD_REGRESSION){
		port = surrogate.offloadServerPort;
	}
	else if(serviceType == SERVICE_TYPE_OFFLOAD_PREDICTION){
		port = surrogate.offloadServerPort;
	}
	return port;
}