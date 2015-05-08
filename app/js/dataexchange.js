"use strict";

var stagingList = [];
var retryStagingInterval = 30000; //ms

setInterval(pushStagedData, retryStagingInterval);

function stageNewSubmit(){
	var submitForm = document.forms["submit-form"];
	var stagingObject = new Object();
	stagingObject.location = submitForm["location-select"].value;
	stagingObject.temp = submitForm["temp-input"].value;
	stagingObject.humidity = submitForm["humidity-input"].value;
	stagingObject.pressure = submitForm["pressure-input"].value;
	stagingObject.windspeed = submitForm["wind-speed-input"].value;
	stagingObject.winddegree = submitForm["wind-deg-input"].value;
	stagingObject.time = submitForm["time-input"].value;
	stagingObject.date = submitForm["date-input"].value;
	stagingObject.source = "app";
		
	var stagingString = JSON.stringify(stagingObject);
	stagingList.push(stagingString);
	//TODO: try to push one time
	//TODO: return success/failure messages
}

function pushStagedData(){
	//TODO: check for availability of surrogate
	//TODO: increase/decrease interval 
	while(stagingList.length > 0){
		//TODO: push data to surrogate
		alert("Sending: \n" + stagingList.shift());
	}
}

function pullData(startDate, endDate){
	if (!(startDate instanceof Date) || !(endDate instanceof Date)){
		//TODO: error handling
	}
	//TODO: get Data from surrogate	
}