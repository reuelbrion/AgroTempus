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

function getSurrogate(serviceType){
	if(surrogateList.length < 1){
		//TODO: try to update surrogate list
		return null;
	}
	var surrogateListClone = JSON.parse(JSON.stringify(surrogateList));
	while(surrogateListClone.length > 0){
		var chosenSurrogate = getHighestWeightSurrogate(surrogateListClone);
		surrogateListClone.splice(surrogateListClone.indexOf(chosenSurrogate), 1);
		{console.log("blabla\nagjhdfsiughsdkgs\n\n\n\n");
		var socket = navigator.mozTCPSocket.open(chosenSurrogate.IP, chosenSurrogate.port);
		socket.onerror = function(event){
			console.info("-> opening surrogate socket failed for: "  + socket.port + "\n" + socket.host + "\n" + event.data.name);
		}
		socket.onopen = function onOpenForGetSurrogate(event){
			console.info("-> connection to surrogate opened: " + socket.port + "\n" + socket.host + "\n" + event.data.name);
			
		}
		while(socket.readyState == "connecting"){console.log("blabla\n");}
		if(socket.readyState == "open"){
			var sendStr = "request-service:" + serviceType + "\nEND\n";
			sendStr = data.toString('utf-8');
			socket.send(sendStr);
		}
	}
		
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