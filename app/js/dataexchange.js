"use strict";

var stagingList = [];
var retryStagingInterval = 10000; //ms

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
		var sendStr = "";
		console.info("Ready to send weather data.");
		for(var i = 0; i < stagingList.length; i++){
			sendStr += stagingList[i];
		}
		sendStr+="\nEND\n";
		console.info("Sent:\n" + sendStr);
		surrogateSocket.send(sendStr.toString('utf-8'));
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
	/*
	//TODO: get Data from surrogate, this is dummy data
	
	receivedObject.location = "Amsterdam - NL";
	receivedObject.lat = 52.379;
	receivedObject.long = 4.899;
	receivedObject.temp = 15.7;
	receivedObject.humidity = 20;
	receivedObject.pressure = 400;
	receivedObject.windspeed = 50;
	receivedObject.winddegree = 180;
	var nowDate = new Date();
	receivedObject.time = nowDate.toTimeString();
	receivedObject.date = nowDate.toDateString();
	receivedObject.source = "Open Weather Map";
	var receivedItems = [];
	receivedItems.push(JSON.stringify(receivedObject));
	*/
}

function pullDataCallback(surrogateSocket, args){
	if(surrogateSocket == null){
		//TODO: no surrogate found
	}
	else{
		console.info("Ready to request regional data.");
		var sendStr = args[0].getTime() + "\n" + args[1].getTime() + "\nEND\n";
		console.info("Request sent:\n" + sendStr);
		surrogateSocket.send(sendStr.toString('utf-8'));
	}
	var receivedObject = new Object();
	surrogateSocket.ondata = function (event) {
			if (typeof event.data === 'string') {
				console.log(JSON.parse(event.data));
				surrogateSocket.onclose = function (event) {
					
				}
			} else {
				surrogateSocket.onclose = function (event) {
					
				}
			}
			surrogateSocket.close();
		}
}

function pullForecasts(callback){
	if (!(typeof(callback) === "function")){
		//TODO: error handling
	}
	//TODO: get Forecasts from surrogate, this is dummy data
	var receivedObject = new Object();
	receivedObject.location = "Amsterdam - NL";
	receivedObject.lat = 52.379;
	receivedObject.long = 4.899;
	receivedObject.temp = 5;
	receivedObject.humidity = 60;
	receivedObject.pressure = 550;
	receivedObject.windspeed = 30;
	receivedObject.winddegree = 135;
	var nowDate = new Date();
	receivedObject.time = nowDate.toTimeString();
	receivedObject.date = nowDate.toDateString();
	receivedObject.description = "Sunny day";
	var receivedObject2 = new Object();
	receivedObject2.location = "Amsterdam - NL";
	receivedObject2.lat = 52.379;
	receivedObject2.long = 4.899;
	receivedObject2.temp = 6;
	receivedObject2.humidity = 67;
	receivedObject2.pressure = 551;
	receivedObject2.windspeed = 31;
	receivedObject2.winddegree = 120;
	var nowDate = new Date();
	receivedObject2.time = nowDate.toTimeString();
	receivedObject2.date = nowDate.toDateString();
	receivedObject2.description = "Rainy day";
	var receivedItems = [];
	receivedItems.push(JSON.stringify(receivedObject));
	receivedItems.push(JSON.stringify(receivedObject2));
	
	callback(null, receivedItems);
}