"use strict";
var TIMEOUT_SOCKET_IDLE = 10000; //ms

var stagingList = [];
var ticketList = [];
var dataPushTimeoutWaitTime = 5000; //ms
var ticketTimeoutWaitTime = 10000; //ms
var requestedDataCallback = null;
var requestedForecastCallback = null;

var dataPushTimeout = setTimeout(function(){pushStagedData()}, dataPushTimeoutWaitTime);
var ticketTimeout = setTimeout(function(){getOutstandingTickets()}, ticketTimeoutWaitTime);

function stageNewSubmit(stagingObject, UICallback){		
	stagingList.push(stagingObject);
	var status = "ok";
	//TODO: return success/failure messages
	UICallback(status);
}

function clearAllGlobalTimeouts(){
	clearTimeout(dataPushTimeout);
	clearTimeout(ticketTimeout);
}

function resumeAllGlobalTimeouts(){
	var tempTime = dataPushTimeoutWaitTime;
	dataPushTimeoutWaitTime = ticketTimeoutWaitTime;
	ticketTimeoutWaitTime = tempTime;
	dataPushTimeout = setTimeout(function(){pushStagedData()}, dataPushTimeoutWaitTime);
	ticketTimeout = setTimeout(function(){getOutstandingTickets()}, ticketTimeoutWaitTime);
}

function addTicket(ticket){
	ticketList.push(ticket);
	//TODO
	//in storage.js
	storeTicket();
}

function getOutstandingTickets(){
	console.log("gonna check for outstanding tickets");
	//turn periodical ticket pull off
	clearAllGlobalTimeouts();
	if(ticketList.length > 0){
		//in discovery.js
		getSurrogate(SERVICE_TYPE_RETRIEVE_COMPUTATION_RESULTS, null, getOutstandingTicketsCallback, null);
	} else {
		//turn periodical ticket pull back on
		resumeAllGlobalTimeouts();
	}
}

function getOutstandingTicketsCallback(surrogateSocket, surrogate){
	if(surrogateSocket == null){
		//turn periodical data push back on
		resumeAllGlobalTimeouts();
		console.info("Couldn't find surrogate for getting outstanding tickets.");
		return;
	}
	var timeout = setTimeout(function () {
		surrogateSocket.close();
	   	console.info("-> Tcp connection was idle for more than " + TIMEOUT_SOCKET_IDLE + " ms. Closing socket." + surrogate.IP + " - " + surrogate.location + " - " + surrogate.country);
	}, TIMEOUT_SOCKET_IDLE);
	var done = false;
	var inData = "";
	//setup: what happens when connection breaks during ticket requests or other socket problems
	surrogateSocket.onerror = function (event) {
		clearTimeout(timeout);
		resumeAllGlobalTimeouts();
		console.info("error during ticket retrieval: " + event.data.name);
	};
	//end setup connection breaks
	//setup: what happens when data comes in
	surrogateSocket.ondata = function (event) {
		clearTimeout(timeout);
		surrogateSocket.suspend;
		if (typeof event.data === 'string'){
			inData += event.data;
			if(event.data.substr(event.data.length - 1) == "\n"){
				done = true;
			}
		}
		//if done, response data for current ticket is complete
		if(done){
			var computationResults = JSON.parse(inData);
			if(computationResults.response == "success"){	
				//in storage.js
				storeComputationResults(computationResults, computationResultsCallback);
			}
			//turn periodical data push back on
			resumeAllGlobalTimeouts();
			//send confirmation to surrogate
			var response = new Object();
			response.response = "ok";
			if(ticketList > 1){
				response.moretickets = "yes";
			} else {
				response.moretickets = "no";
			}
			var sendStr = JSON.stringify(response) + "\n";
			surrogateSocket.send(sendStr.toString('utf-8'));
			//TODO: inform user of new data
			surrogateSocket.onclose = function (event) {
				console.info("closed socket to surrogate");
			}
			ticketList.shift();
			if(ticketList.length > 0){
				surrogateSocket.resume;
				getOutstandingTicketsCallback(surrogateSocket);
			} else {
				surrogateSocket.close();
			}
		}
	};
	//end data comes in
	if(ticketList.length > 0){
		var ticket = ticketList[0];
		var request = new Object();
		request.ticket = ticket;
		var sendStr = JSON.stringify(request) + "\n";
		surrogateSocket.send(sendStr.toString('utf-8'));
		console.info("Sent:\n" + sendStr);
	}
}	

function computationResultsCallback(){
	//in app.js
	newIncomingData();
}

function pushStagedData(){
	console.log("gonna check for outgoing data");
	//turn periodical data push off
	clearAllGlobalTimeouts();
	if(stagingList.length > 0){
		var stagingItem = stagingList[0];
		//in discovery.js
		getSurrogate(SERVICE_TYPE_STORE_WEATHER_DATA, null, getOutstandingTicketsCallback, null);
	} else {
		//turn periodical data push back on
		resumeAllGlobalTimeouts();
	}
}

function pushStagedDataCallback(surrogateSocket, surrogate){
	if(surrogateSocket == null){
		//turn periodical data push back on
		resumeAllGlobalTimeouts();
		console.info("Couldn't find surrogate for getting outstanding tickets.");
		return;
	}
	var timeout = setTimeout(function () {
		surrogateSocket.close();
	   	console.info("-> Tcp connection was idle for more than " + TIMEOUT_SOCKET_IDLE + " ms. Closing socket." + surrogate.IP + " - " + surrogate.location + " - " + surrogate.country);
	}, TIMEOUT_SOCKET_IDLE);
	//setup: what happens when connection breaks during ticket requests or other socket problems
	surrogateSocket.onerror = function (event) {
		clearTimeout(timeout);
		resumeAllGlobalTimeouts();
		console.info("error during data push: " + event.data.name);
	};
	//end setup connection breaks
	//setup: what happens when data comes in
	surrogateSocket.ondata = function (event) {
		clearTimeout(timeout);
		/*TODO: return the number of stored json objects from the surrogate, so that
		we know what has been saved.*/
		if (typeof event.data === 'string' && JSON.parse(event.data).response == "ok"){
			removeOldItemsFromStagingList();	
			alert("All weather data saved on surrogate.");			
		} else {
			restoreOldItemsFromStagingList();
			alert("Not all weather data saved on surrogate, will try again later.");
		}
			surrogateSocket.onclose = function (event) {
			console.info("socket closed");
		}
		surrogateSocket.close();
		//turn periodical data push back on
		resumeAllGlobalTimeouts();
	}
	//end data comes in
	var sendStr = "[";
	console.info("Ready to send weather data.");
	for(var i = 0; i < stagingList.length; i++){
		var currentItem = stagingList[i];
		if(i > 0){
			sendStr += ",";
		}
		sendStr += JSON.stringify(currentItem);	
		stagingList[i].old = true;
	}
	sendStr+="]\n";
	surrogateSocket.send(sendStr.toString('utf-8'));
	console.info("Sent items to surrogate.");
}

function removeOldItemsFromStagingList(){
	for(var i = stagingList.length -1; i >= 0 ; i--){
	    if(stagingList[i].old == true){
	    	stagingList.splice(i, 1);
	    }
	}
}

function restoreOldItemsFromStagingList(){
	for(var i = stagingList.length -1; i >= 0 ; i--){
		stagingList[i].old == false;
	}
}

function pullData(startDate, endDate, UICallback){
	//TODO: check for correct dates (time integers)
	if (!(typeof(callback) === "function")){
		//TODO: error handling
	}
	if (startDate > endDate){
		callback("wrongdate");
		return;
	}
	var args = [startDate, endDate];
	//callback to update UI
	UICallback("requesting");
	//and we need to callback to UI again when we get data so store this callback
	requestedDataCallback = UICallback;
	//look for surrogate in discovery.js
	getSurrogate(SERVICE_TYPE_RETRIEVE_REGIONAL_DATA, null, pullDataCallback, args);	
}

function pullDataCallback(surrogateSocket, surrogate, args){
	if(surrogateSocket == null){
		//TODO: no surrogate found
		requestedDataCallback("failed");
	} else {
		var timeout = setTimeout(function () {
			surrogateSocket.close();
		   	console.info("-> Tcp connection was idle for more than " + TIMEOUT_SOCKET_IDLE + " ms. Closing socket." + surrogate.IP + " - " + surrogate.location + " - " + surrogate.country);
		}, TIMEOUT_SOCKET_IDLE);
		//initialize new pull data request
		var inData = "";
		console.info("Ready to request regional data.\n");
		var request = new Object();
		request.start_time = args[0];
		request.end_time = args[1];
		var sendStr =  JSON.stringify(request) + "\n";
		console.info("Request sent:\n" + sendStr);
		surrogateSocket.send(sendStr.toString('utf-8'));
		//setup what happens on socket error
		surrogateSocket.onerror = function (event) {
			clearTimeout(timeout);
			surrogateSocket.close();
			console.info("Closing socket, error during data pull: " + event.data.name);
			requestedDataCallback("failed");
		};
		//end socket error
		//setup what happens on response
		surrogateSocket.ondata = function (event) {
			clearTimeout(timeout);
			if (typeof event.data === 'string') {
				inData += event.data;
				if(event.data.substr(event.data.length - 1) == "\n" || event.data.substr(event.data.length - 1) == "]"){
					var response = new Object();
					response.response = "ok";
					var sendStr = JSON.stringify(response) + "\n";
					surrogateSocket.send(sendStr.toString('utf-8'));
					requestedDataCallback("ready", inData, args);
				} 
			} else {
				var response = new Object();
				response.response = "unknown";
				var sendStr = JSON.stringify(response) + "\n";
				surrogateSocket.send(sendStr.toString('utf-8'));
				requestedDataCallback("failed");
			}
			surrogateSocket.onclose = function (event) {
				console.info("closed socket to surrogate");
			};
			surrogateSocket.close();
		}
		//end response handling
	}		
}

function pullForecasts(UICallback){
	if (!(typeof(UICallback) === "function")){
		//TODO: error handling
	}
	getSurrogate(SERVICE_TYPE_RETRIEVE_FORECASTS, null, pullForecastsCallback, null);
	UICallback("requesting");
	requestedForecastCallback = UICallback;
}

function pullForecastsCallback(surrogateSocket){
	if(surrogateSocket == null){
		requestedForecastCallback("failed");
	} else {
		var inData = "";
		console.info("Ready to receive forecasts.\n");
		//setup what happens on socket error
		surrogateSocket.onerror = function (event) {
			console.info("Closing socket, error during forecast pull: " + event.data.name);
			surrogateSocket.close();
		};
		//end socket error
		surrogateSocket.ondata = function (event) {
			if (typeof event.data === 'string') {
				inData += event.data;
				if(event.data.substr(event.data.length - 1) == "\n" || event.data.substr(event.data.length - 1) == "]"){
					var response = new Object();
					response.response = "ok";
					var sendStr = JSON.stringify(response) + "\n";
					surrogateSocket.send(sendStr.toString('utf-8'));
					requestedForecastCallback("ready", inData);
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
				requestedForecastCallback("failed");
				surrogateSocket.onclose = function (event) {
					console.info("closed socket to surrogate");
				};
				surrogateSocket.close();
			}
		}
	}
}