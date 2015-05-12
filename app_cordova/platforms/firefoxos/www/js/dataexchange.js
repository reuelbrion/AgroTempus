"use strict";

var stagingList = [];
var retryStagingInterval = 30000; //ms

setInterval(pushStagedData, retryStagingInterval);

function stageNewSubmit(stagingObject, callback){		
	var stagingString = JSON.stringify(stagingObject);
	stagingList.push(stagingString);
	var status = "ok";
	//TODO: try to push one time
	//TODO: return success/failure messages
	callback(status);
}

function pushStagedData(){
	//TODO: check for availability of surrogate
	//TODO: increase/decrease interval 
	while(stagingList.length > 0){
		//TODO: push data to surrogate
		alert("Sending: \n" + stagingList.shift());
	}
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
	//TODO: get Data from surrogate, this is dummy data
	var receivedObject = new Object();
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
	
	callback(null, receivedItems, startDate, endDate);
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