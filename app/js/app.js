"use strict";

function setSectionVisible(visibleSection){
	var sections = document.getElementsByTagName("section");
	for (var i = 0; i < sections.length; i++) {
		if(sections[i].getAttribute("id") != "title"){
			sections[i].setAttribute("class", "hidden");
		}
	}
	document.getElementById(visibleSection).setAttribute("class", "visible-section");
}

function makeBackButtonHeaderVisible(){
	document.getElementById("back-to-main-head-btn").setAttribute("class", "icon icon-back");
	document.getElementById("back-to-main-span").setAttribute("class", "");
}

function makeStoreButtonHeaderVisible(){
	document.getElementById("store-submit-btn").setAttribute("class", "icon icon-store");
	document.getElementById("store-submit-span").setAttribute("class", "");
}

function makeStoreButtonHeaderInvisible(){
	document.getElementById("store-submit-btn").setAttribute("class", "hidden");
	document.getElementById("store-submit-span").setAttribute("class", "hidden");
}

function makeBackButtonHeaderInvisible(){
	document.getElementById("back-to-main-head-btn").setAttribute("class", "hidden");
	document.getElementById("back-to-main-span").setAttribute("class", "hidden");
}

function submitDataClick(){
	setSectionVisible("submit");
	makeBackButtonHeaderVisible();
	makeStoreButtonHeaderVisible();
}

function getDataClick(){
	setSectionVisible("get-data");
	makeBackButtonHeaderVisible();
}

function forecastsClick(){
	setSectionVisible("forecasts");
	makeBackButtonHeaderVisible();
	pullForecasts(forecastsCallback);
}

function forecastsCallback(status, inData){
	if(status == null || status == "ok"){
		document.getElementById("forecasts-span").innerHTML = "retrieving data<br>";		
	}
	else if(status == "error"){
		//TODO: error handling
	}
	else if(status == "ready"){
		addForecastElements(inData);
	}
	else if(status == "failed"){
		document.getElementById("forecasts-span").innerHTML = "failed getting data<br>";
	}
}

function addForecastElements(receivedItems){
	var itemString = "Forecasts received: <br><br>" + receivedItems;
	/*while(receivedItems.length > 0){
		var receivedObject = JSON.parse(receivedItems.shift());
		itemString += "Location: " + receivedObject.location + "<br>";
		itemString += "Lat: " + receivedObject.lat + "<br>";
		itemString += "Long: " + receivedObject.long + "<br>";
		itemString += "Temperature: " + receivedObject.temp + "<br>";
		itemString += "Humidity: " + receivedObject.humidity + "<br>";
		itemString += "Pressure: " + receivedObject.pressure + "<br>";
		itemString += "Wind speed: " + receivedObject.windspeed + "<br>";
		itemString += "Wind degree: " + receivedObject.winddegree + "<br>";
		itemString += "Time: " + receivedObject.time + "<br>";
		itemString += "Date: " + receivedObject.date + "<br>";
		itemString += "Description: " + receivedObject.description + "<br><br>";
	}*/
	document.getElementById("forecasts-span").innerHTML = itemString;
}

function predictionClick(){
	setSectionVisible("prediction");
	makeBackButtonHeaderVisible();
}

function regressionClick(){
	setSectionVisible("regression");
	makeBackButtonHeaderVisible();
}

function editDataSubmitClick(){
var submitForm = document.forms["submit-form"];
	var stagingObject = new Object();
	stagingObject.location = submitForm["location-select"].value;
	stagingObject.temp = submitForm["temp-input"].value;
	stagingObject.humidity = submitForm["humidity-input"].value;
	stagingObject.pressure = submitForm["pressure-input"].value;
	stagingObject.windspeed = submitForm["wind-speed-input"].value;
	stagingObject.winddegree = submitForm["wind-deg-input"].value;
	if(submitForm["datetime-input"].value == ""){
		stagingObject.time = Date.now();
	} else {
		stagingObject.time = submitForm["datetime-input"].value;
	}
	stagingObject.source = "app";
	stageNewSubmit(stagingObject, submitCallBack);
}

function submitCallBack(status){
	if (status == "ok"){
		console.info("-> weather data added to outbound queue");
		//TODO: show that data has been submitted on app screen
		backToMainClick();
	}
}

function getDataSubmitClick(){
	setSectionVisible("get-data-results");
	var startDate = $("#get-date-input1").val();
	var endDate = $("#get-date-input2").val();
	startDate = new Date(startDate);
	endDate = new Date(endDate);
	//pull data in dataexchange.js
	pullData(startDate, endDate, getDataCallback);
}

function getDataCallback(status, inData, args){
	if(status == null || status == "requesting"){
		document.getElementById("get-results-span").innerHTML = "retrieving data<br>";
	}
	else if(status == "wrongdate"){
		alert("start date should be before end date!");
		getDataClick();
	}
	else if(status == "ready"){
		addGetDataElements(inData, args[0], args[1]);
	}
	else if(status == "failed"){
		document.getElementById("get-results-span").innerHTML = "failed getting data<br>";
	}
}

function addGetDataElements(receivedItems, startDate, endDate){
	var itemString = "Items received for time period " + startDate + " until " + endDate + "<br><br>";
	console.log(JSON.parse(receivedItems));
	/*while(receivedItems.length > 0){
		var receivedObject = JSON.parse(receivedItems.shift());
		itemString += "Location: " + receivedObject.location + "<br>";
		itemString += "Lat: " + receivedObject.lat + "<br>";
		itemString += "Long: " + receivedObject.long + "<br>";
		itemString += "Temperature: " + receivedObject.temp + "<br>";
		itemString += "Humidity: " + receivedObject.humidity + "<br>";
		itemString += "Pressure: " + receivedObject.pressure + "<br>";
		itemString += "Wind speed: " + receivedObject.windspeed + "<br>";
		itemString += "Wind degree: " + receivedObject.winddegree + "<br>";
		itemString += "Time: " + receivedObject.time + "<br>";
		itemString += "Date: " + receivedObject.date + "<br>";
		itemString += "Source: " + receivedObject.source + "<br><br>";
	}*/
	itemString += receivedItems;
	document.getElementById("get-results-span").innerHTML = itemString;
}

function editTimeClick(){
	var button = document.getElementById("edit-time-btn");
	if (button.getAttribute("value") == "no-edit"){
		button.setAttribute("value", "edit");
		button.innerHTML = "Use current time";
		document.getElementById("datetime-input-form").setAttribute("class", "");
	}	
	else{
		button.setAttribute("value", "no-edit");
		button.innerHTML = "Change time";
		document.getElementById("datetime-input-form").setAttribute("class", "hidden");
	}
}

function predictionSubmitClick(){
	setSectionVisible("prediction-results");
	document.getElementById("prediction-span").innerHTML = "sending request<br>";
	var offloadParams = [];
	offloadParams.variable = $("#prediction-select").val();
	offloadParams.startDate = $("#prediction-date-input").val();
	//offload request in offload.js
	requestOffload(offloadParams, predictionCallBack);
}

function predictionCallBack(status){
	if(status == "ok"){
		document.getElementById("prediction-span").innerHTML = "request has been sent<br> to surrogate<br>";
	}
	if(status == null){
		//TODO: error handling
	}
}

function regressionSubmitClick(){
	setSectionVisible("regression-results");
	document.getElementById("regression-span").innerHTML = "sending request<br>";
	var offloadParams = [];
	offloadParams.variable = $("#regression-variable-select").val();
	offloadParams.type = $("#regression-type-select").val();
	offloadParams.startDate = $("#regression-date-input").val();
	offloadParams.days = $("#regression-days-input").val();
	//offload request in offload.js
	requestOffload(offloadParams, regressionCallBack);
}

function regressionCallBack(status){
	if(status == "ok"){
		document.getElementById("regression-span").innerHTML = "request has been sent<br> to surrogate<br>";
	}
	if(status == null){
		//TODO: error handling
	}
}

function backToMainClick(){
	setSectionVisible("main");
	makeBackButtonHeaderInvisible();
	makeStoreButtonHeaderInvisible();
}

function backToMainClickResults(){
	setSectionVisible("main");
	makeBackButtonHeaderInvisible();
	document.getElementById("get-results-span").innerHTML = "";
	document.getElementById("forecasts-span").innerHTML = "";
	document.getElementById("prediction-span").innerHTML = "";
	document.getElementById("regression-span").innerHTML = "";
}

function addLocationElements(locations){
	var locationString = "";
	var len = locations.length;
	for (var i = 0; i < len; i++) {
		locationString += "<option>";
		locationString += locations[i];
		locationString += "</option>\n";
		document.getElementById("location-select").innerHTML += locationString;
	}
	
}

$(document).ready(function () {
	document.getElementById("back-to-main-head-btn").addEventListener("click", backToMainClick);
    document.getElementById("submit-data-btn").addEventListener("click", submitDataClick);
	document.getElementById("get-data-btn").addEventListener("click", getDataClick);
	document.getElementById("forecasts-btn").addEventListener("click", forecastsClick);
	document.getElementById("prediction-btn").addEventListener("click", predictionClick);
	document.getElementById("regression-btn").addEventListener("click", regressionClick);
	document.getElementById("submit-submit-btn").addEventListener("click", editDataSubmitClick);
	document.getElementById("edit-time-btn").addEventListener("click", editTimeClick);
	document.getElementById("get-submit-btn").addEventListener("click", getDataSubmitClick);
	document.getElementById("prediction-submit-btn").addEventListener("click", predictionSubmitClick);
	document.getElementById("regression-submit-btn").addEventListener("click", regressionSubmitClick);
	document.getElementById("back-to-main-btn1").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn2").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn3").addEventListener("click", backToMainClickResults);
	document.getElementById("back-to-main-btn4").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn5").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn6").addEventListener("click", backToMainClickResults);
	document.getElementById("back-to-main-btn7").addEventListener("click", backToMainClickResults);
	document.getElementById("back-to-main-btn8").addEventListener("click", backToMainClickResults);
	
	loadLocations(addLocationElements);
});

