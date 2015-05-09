"use strict";

var stagingList = [];
var retryStagingInterval = 30000; //ms

setInterval(pushStagedData, retryStagingInterval);

function stageNewSubmit(stagingObject, callBack){		
	var stagingString = JSON.stringify(stagingObject);
	stagingList.push(stagingString);
	var status = "ok";
	//TODO: try to push one time
	//TODO: return success/failure messages
	callBack(status);
}

function pushStagedData(){
	//TODO: check for availability of surrogate
	//TODO: increase/decrease interval 
	while(stagingList.length > 0){
		//TODO: push data to surrogate
		alert("Sending: \n" + stagingList.shift());
	}
}

function pullData(startDate, endDate, callBack){
	if (!(startDate instanceof Date) || !(endDate instanceof Date)){
		//TODO: error handling
	}
	if (startDate > endDate){
		callBack("wrongdate");
	}
	//TODO: get Data from surrogate, this is dummy data
	var receivedObject = new Object();
	receivedObject.location = "Amsterdam";
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
	
	callBack(null, receivedItems, startDate, endDate);
}