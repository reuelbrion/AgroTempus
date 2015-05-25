"use strict";
var surrogateList = [];
//TODO: retrieve surrogates from data store instead of hardcoding
/*var surrogate = {
	"location" : "Amsterdam",
	"country" : "NL",
	"IP" : "196.239.52.212",
	"storageServerPort" : 11112,
	"requestServerPort" : 11113,
	"weight" : 1
};
surrogateList.push(surrogate);*/
var surrogate = {
	"location" : "Breda",
	"country" : "NL",
	"IP" : "195.240.53.133",
	"storageServerPort" : 11112,
	"requestServerPort" : 11113,
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
	if(serviceType == "store_weather_data"){
		port = surrogate.storageServerPort;
	}
	else if(serviceType == "retrieve_regional_data"){
		port = surrogate.requestServerPort;
	}
	else if(serviceType == "retrieve_computation_results"){
		port = surrogate.requestServerPort;
	}
	return port;
}