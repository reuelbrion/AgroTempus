"use strict";

var stagingList = [];
var ticketList = [];
var dataPushIntervalWaitTime = 9000; //ms
var ticketIntervalWaitTime = 10000; //ms
var receivingPullData = null;
var requestedDataCallback = null;
var requestedForecastCallback = null;

var dataPushInterval = setInterval(function(){pushStagedData()}, dataPushIntervalWaitTime);
var ticketInterval = setInterval(function(){getOutstandingTickets()}, ticketIntervalWaitTime);

function stageNewSubmit(stagingObject, UICallback){		
	var stagingString = JSON.stringify(stagingObject);
	stagingList.push(stagingString);
	pushStagedData();
	var status = "ok";
	//TODO: return success/failure messages
	UICallback(status);
}

function clearAllIntervals(){
	clearInterval(dataPushInterval);
	clearInterval(ticketIntervalWaitTime);
}

function resumeAllIntervals(){
	dataPushInterval = setInterval(function(){pushStagedData()}, dataPushIntervalWaitTime);
	ticketInterval = setInterval(function(){getOutstandingTickets()}, ticketIntervalWaitTime);
}

function addTicket(ticket){
	ticketList.push(ticket);
}

function getOutstandingTickets(){
	console.log("gonna try to get tickets");
	//turn periodical ticket pull off
	clearInterval(ticketInterval);
	if(ticketList.length > 0){
		//in discovery.js
		getSurrogate(SERVICE_TYPE_RETRIEVE_COMPUTATION_RESULTS, null, getOutstandingTicketsCallback, null);
	} else {
		//turn periodical ticket pull back on
		ticketInterval = setInterval(function(){getOutstandingTickets()}, ticketIntervalWaitTime);
	}
}

function getOutstandingTicketsCallback(surrogateSocket){
	if(surrogateSocket == null){
		//TODO: no surrogate found
		console.info("Couldn't find surrogate for getting outstanding tickets.");
	} else {
		//TODO: timeout
		var done = false;
		var inData = "";
		surrogateSocket.onerror = function (event) {
			console.info("error during ticket retrieval: " + event.data.name);
		}
		surrogateSocket.ondata = function (event) {
			surrogateSocket.suspend;
			if (typeof event.data === 'string'){
				inData += event.data;
				if(event.data.substr(event.data.length - 1) == "\n"){
					done = true;
				}
			} 
			if(done){
				var response = JSON.parse(inData);
				if(response.response == "success"){
					var computationResults = new Object();					
					computationResults.ticket = response.ticket;
					computationResults.graphimage = response.graphimage;	
					computationResults.createtime = response.createtime;	
					//in storage.js
					storeComputationResults(computationResults, computationResultsCallback);
				}
				//turn periodical data push back on
				ticketInterval = setInterval(function(){getOutstandingTickets()}, ticketIntervalWaitTime);
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
			surrogateSocket.resume;
		}
		if(ticketList.length > 0){
			var ticket = ticketList[0];
			var request = new Object();
			request.ticket = ticket;
			var sendStr = JSON.stringify(request) + "\n";
			surrogateSocket.send(sendStr.toString('utf-8'));
			console.info("Sent:\n" + sendStr);
			var receiveStr = "";
		}
	}
}	

function computationResultsCallback(){
	//in app.js
	newIncomingData();
}

function pushStagedData(){
	console.log("gonna try to send data");
	//turn periodical data push off
	clearInterval(dataPushInterval);
	if(stagingList.length > 0){
		//in discovery.js
		getSurrogate(SERVICE_TYPE_STORE_WEATHER_DATA, null, pushStagedDataCallback, null);
	} else {
		//turn periodical data push back on
		dataPushInterval = setInterval(function(){pushStagedData()}, dataPushIntervalWaitTime);
	}
}

function pushStagedDataCallback(surrogateSocket, args){
	if(surrogateSocket == null){
	 //TODO: no surrogate found
	}
	else{
		var sendStr = "[";
		console.info("Ready to send weather data.");
		for(var i = 0; i < stagingList.length; i++){
			if(i > 0){
				sendStr += ",";
			}
			sendStr += stagingList[i];
		}
		sendStr+="]\n";
		surrogateSocket.send(sendStr.toString('utf-8'));
		console.info("Sent:\n" + sendStr);
		surrogateSocket.ondata = function (event) {
			/*TODO: return the number of stored json objects from the surrogate, so that
			we know what has been saved.*/
			if (typeof event.data === 'string' && JSON.parse(event.data).response == "ok"){
				alert("All weather data saved on surrogate.");
				stagingList = [];
				surrogateSocket.onclose = function (event) {
					console.info("socket closed");
				}
			} else {
				alert("Not all weather data saved on surrogate, will try again later.");
				surrogateSocket.onclose = function (event) {
					console.info("socket closed");
				}
			}
			surrogateSocket.close();
			//turn periodical data push back on
			dataPushInterval = setInterval(function(){pushStagedData()}, dataPushIntervalWaitTime);
		}
	}	
}

function pullData(startDate, endDate, UICallback){
	//TODO: check for correct dates (time integers)
	if (!(typeof(callback) === "function")){
		//TODO: error handling
	}
	if (startDate > endDate){
		callback("wrongdate");
	}
	var args = [startDate, endDate];
	//look for surrogate in discovery.js
	getSurrogate(SERVICE_TYPE_RETRIEVE_REGIONAL_DATA, null, pullDataCallback, args);
	//callback to update UI
	UICallback("requesting");
	//and we need to callback to UI again when we get data so store this callback
	requestedDataCallback = UICallback;
}

function pullDataCallback(surrogateSocket, args){
	if(surrogateSocket == null){
		//TODO: no surrogate found
		requestedDataCallback("failed");
	} else {
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
				}
				surrogateSocket.close();
			}
		}
	}
}