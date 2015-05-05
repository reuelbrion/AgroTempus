"use strict";

var stagingList = [];

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
		
	var stagingString = JSON.stringify(stagingObject);
	stagingList.push(stagingString);
}

function pushStagedData(){
	while(stagingList.length > 0){
		alert(stagingList.shift());
	}
}
	
/*
$(document).ready(function () {
});
*/