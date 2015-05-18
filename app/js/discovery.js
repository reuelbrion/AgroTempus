"use strict";
var surrogateList = [];
//TODO: retrieve surrogates from data store instead of hardcoding
var surrogate = {
	"location" : "Amsterdam",
	"country" : "NL",
	"IP" : "195.240.53.111",
	"port" : 11112,
	"weight" : 1
};
surrogateList.push(surrogate);
surrogate = {
	"location" : "Breda",
	"country" : "NL",
	"IP" : "195.240.53.133",
	"port" : 11112,
	"weight" : 0
};
surrogateList.push(surrogate);

function getSurrogate(serviceType, surrogateListClone, callback){
	//TODO check callback is function
	if(surrogateList.length < 1){
		//TODO: try to update surrogate list
		callback(null);
	}
	if(surrogateListClone == null){
		var surrogateListClone = JSON.parse(JSON.stringify(surrogateList));
	}
	if(surrogateListClone.length < 1){
		callback(null);
	}
	var socket = null;
	
	var chosenSurrogate = getHighestWeightSurrogate(surrogateListClone);
	surrogateListClone.splice(surrogateListClone.indexOf(chosenSurrogate), 1);
	socket = navigator.mozTCPSocket.open(chosenSurrogate.IP, chosenSurrogate.port);
	
	socket.onerror = function(event){
		console.info("-> opening surrogate socket failed for: "  + socket.port + "\n" + socket.host + "\n" + event.data.name);
		getSurrogate(serviceType, surrogateListClone, callback);
	}
	
	socket.onopen = function(event){
		console.info("-> connection to surrogate opened: " + socket.port + "\n" + socket.host + "\n");	
		socket.onerror = function(event){
			console.info("-> something went wrong during connection with surrogate, connection lost: "  + socket.port + "\n" + socket.host + "\n" + event.data.name);
		}
		chosenSurrogate.weight++;
		//TODO: weight algorithm
		var sendStr = "request-service:" + serviceType + "\nEND\n";
		sendStr = sendStr.toString('utf-8');
		socket.send(sendStr);
		//callback(socket);
	}
	
	/*
		
		while(socket.readyState == "connecting"){console.log("blabla\n");}
		if(socket.readyState == "open"){
			
		}*/
	callback(null);
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



	// Each time the buffer is flushed
	// we try to send data again.
	//socket.ondrain = pushData;