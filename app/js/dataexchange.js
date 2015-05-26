"use strict";

var stagingList = [];
var retryStagingInterval = 10000; //ms
var requestedDataCallback = null;
var requestedForecastsCallback = null;

//TODO: setInterval doesnt seem to work
var interval = setInterval(function(){pushStagedData()}, retryStagingInterval);

function stageNewSubmit(stagingObject, callback){		
	var stagingString = JSON.stringify(stagingObject);
	stagingList.push(stagingString);
	pushStagedData();
	var status = "ok";
	//TODO: return success/failure messages
	callback(status);
}

function pushStagedData(){
	clearInterval(interval);
	if(stagingList.length > 0){
		//in discovery.js
		getSurrogate("store_weather_data", null, pushStagedDataCallback, null);
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
			if (typeof event.data === 'string' && event.data == "ok\n") {
				stagingList = [];
				surrogateSocket.onclose = function (event) {
					alert("All weather data saved on surrogate.");
				}
			} else {
				surrogateSocket.onclose = function (event) {
					alert("Not all weather data saved on surrogate, will try again later.");
				}
			}
			surrogateSocket.close();
			interval = setInterval(function(){pushStagedData()}, retryStagingInterval);
		}
	}	
	/*
	//TODO: increase/decrease interval 
	while(stagingList.length > 0){
		//TODO: push data to surrogate
	}
	*/
}

function pullData(startDate, endDate, callback){
	if (!(startDate instanceof Date) || !(endDate instanceof Date)){
		//TODO: error handling
	}
	if (!(typeof(callback) === "function")){
		//TODO: error handling
	}
	if (startDate > endDate){
		callback("wrongdate");
	}
	var args = [startDate, endDate];
	//in discovery.js
	getSurrogate("retrieve_regional_data", null, pullDataCallback, args);
	callback("requesting");
	requestedDataCallback = callback;
}

function pullDataCallback(surrogateSocket, args){
	if(surrogateSocket == null){
		//TODO: no surrogate found
	}
	else{
		console.info("Ready to request regional data.");
		var sendStr = args[0].getTime() + "\n" + args[1].getTime() + "\n";
		console.info("Request sent:\n" + sendStr);
		surrogateSocket.send(sendStr.toString('utf-8'));
	}
	surrogateSocket.ondata = function (event) {
			if (typeof event.data === 'string') {
				var sendStr = "ok\n";
				surrogateSocket.send(sendStr.toString('utf-8'));
				var inData = event.data;
				surrogateSocket.onclose = function (event) {
					requestedDataCallback("ready", inData, args);
				}
			} else {
				var sendStr = "unknown\n";
				surrogateSocket.send(sendStr.toString('utf-8'));
				surrogateSocket.onclose = function (event) {
					requestedDataCallback("failed");
				}
			}
			surrogateSocket.close();
		}
}

function pullForecasts(callback){
	if (!(typeof(callback) === "function")){
		//TODO: error handling
	}
	getSurrogate("retrieve_forecasts", null, pullForecastsCallback, null);
	callback("requesting");
	requestedForecastsCallback = callback;
}

function pullForecastsCallback(surrogateSocket){
	if(surrogateSocket == null){
		//TODO: no surrogate found
	}
	else{
		console.info("Ready to request forecast data.");
	}
	surrogateSocket.ondata = function (event) {
			if (typeof event.data === 'string') {
				var sendStr = "ok\n";
				surrogateSocket.send(sendStr.toString('utf-8'));
				var inData = event.data;
				surrogateSocket.onclose = function (event) {
					requestedForecastCallback("ready", inData);
				}
			} else {
				var sendStr = "unknown\n";
				surrogateSocket.send(sendStr.toString('utf-8'));
				surrogateSocket.onclose = function (event) {
					requestedForecastCallback("failed");
				}
			}
			surrogateSocket.close();
		}
}