"use strict";


	/*var imageString = '<img src="data:image/png;base64,'+response.graph-image+'">';
	document.getElementById("regression-confirmation-span").innerHTML = imageString;*/

function newIncomingData(){
	toggleNewDataIcon(true);
}

function toggleNewDataIcon(switchOn){
	if(switchOn){
		document.getElementById("newdata-head-btn").setAttribute("class", "icon icon-newdata");
	} else {
		document.getElementById("newdata-head-btn").setAttribute("class", "hidden");
	}
}

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
	if(status == null || status == "requesting"){
		document.getElementById("forecasts-span").innerHTML = "retrieving data<br><br>";	
	}
	else if(status == "error"){
		//TODO: error handling
	}
	else if(status == "ready"){
		addForecastElements(inData);
	}
	else if(status == "failed"){
		document.getElementById("forecasts-span").innerHTML = "failed getting data<br><br>";
	}
}

function addForecastElements(receivedItems){
	var itemString = "Forecasts received: <br><br>";
	receivedItems = JSON.parse(receivedItems);
	while(receivedItems.length > 0){
	//TODO: format time into human readable date and time
		var receivedObject = receivedItems.shift();
		itemString += "Location: " + receivedObject.location + "<br>";
		itemString += "Lat: " + receivedObject.lat + "<br>";
		itemString += "Long: " + receivedObject.long + "<br>";
		itemString += "Temperature: " + receivedObject.temp + "<br>";
		itemString += "Humidity: " + receivedObject.humidity + "<br>";
		itemString += "Pressure: " + receivedObject.pressure + "<br>";
		itemString += "Wind speed: " + receivedObject.windspeed + "<br>";
		itemString += "Wind degree: " + receivedObject.winddegree + "<br>";
		itemString += "Time: " + receivedObject.time + "<br>";
		//itemString += "Date: " + receivedObject.date + "<br>";
		itemString += "Description: " + receivedObject.description + "<br><br>";
	}
	document.getElementById("forecasts-span").innerHTML = itemString;
}

function receivedItemsClick(){
	setSectionVisible("received-overview");	
	makeBackButtonHeaderVisible();
	toggleNewDataIcon(false);
	//in storage.js
	getReceivedItemsList(displayReceivedItemsCallback);
}

function displayReceivedItemsCallback(receivedItemsList){
	//TODO: check for correct data
	var itemString = "";
	var counter = 1;
	var ticketList = [];
	while(receivedItemsList.length > 0){
		var receivedObject = receivedItemsList.shift();
		itemString += counter + ". Ticket: <a href=\"#\" class=\"ticket-link\" id=\"" + receivedObject.ticket + "\">" + receivedObject.ticket + "</a><br>";
		if(receivedObject.lastviewed == 0){
			itemString += "!NEW! - <br>";
		} else {
			itemString += "Received: " + receivedObject.lastviewed.toString() + "<br>";
		}
		var createDate = new Date(receivedObject.createtime);
		itemString += "Created: " + createDate.toString() + "<br>";
		itemString += "VIEW DELETE <br>";
		ticketList.push(receivedObject.ticket);
		counter++;
	}
	document.getElementById("received-overview-span").innerHTML = itemString;
	while(ticketList.length > 0){
		var nextTicket = ticketList.shift();
		document.getElementById(nextTicket).addEventListener("click", function(){goToTicketPage(nextTicket);});
	}
}

function goToTicketPage(ticket){
	setSectionVisible("received-item-details");
	makeBackButtonHeaderVisible();
	document.getElementById("received-item-details-span").innerHTML = ticket;
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
	//TODO: get surrogate lat and long and name correctly
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
	startDate = new Date(startDate).getTime();
	endDate = new Date(endDate).getTime();
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
	receivedItems = JSON.parse(receivedItems);
	while(receivedItems.length > 0){
	//TODO: format time into human readable date and time
		var receivedObject = receivedItems.shift();
		itemString += "Location: " + receivedObject.location + "<br>";
		itemString += "Lat: " + receivedObject.lat + "<br>";
		itemString += "Long: " + receivedObject.long + "<br>";
		itemString += "Temperature: " + receivedObject.temp + "<br>";
		itemString += "Humidity: " + receivedObject.humidity + "<br>";
		itemString += "Pressure: " + receivedObject.pressure + "<br>";
		itemString += "Wind speed: " + receivedObject.windspeed + "<br>";
		itemString += "Wind degree: " + receivedObject.winddegree + "<br>";
		itemString += "Time: " + receivedObject.time + "<br>";
		//itemString += "Date: " + receivedObject.date + "<br>";
		itemString += "Source: " + receivedObject.source + "<br><br>";
	}
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
	setSectionVisible("prediction-confirm");
	document.getElementById("prediction-span").innerHTML = "sending request<br>";
	var offloadParams = new Object();
	offloadParams.variable = $("#prediction-select").val();
	offloadParams.startdate = $("#prediction-date-input").val();
	offloadParams.startdate = new Date(offloadParams.startDate);
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
	setSectionVisible("regression-confirm");
	document.getElementById("regression-confirmation-span").innerHTML = "locating surrogate<br>";
	var offloadParams = new Object();
	offloadParams.type = SERVICE_TYPE_OFFLOAD_REGRESSION;
	offloadParams.variable = selectRegressionVariable($("#regression-variable-select").val());
	offloadParams.regressiontype = $("#regression-type-select").val();
	offloadParams.startdate = $("#regression-date-input").val();
	offloadParams.startdate = Date.parse(offloadParams.startdate);
	offloadParams.extrapolatedays = $("#regression-days-input").val();
	if(typeof offloadParams.extrapolatedays === 'string'){
		offloadParams.extrapolatedays = parseInt(offloadParams.extrapolatedays);
	}
	//offload request in offload.js
	requestOffload(offloadParams, SERVICE_TYPE_OFFLOAD_REGRESSION, regressionCallBack);
}

function selectRegressionVariable(value){
	if(value == "Temperature"){
		return "temp";
	} else if (value == "Humidity"){
		return "humidity";
	} else if (value == "Pressure"){
		return "pressure";
	} else {
		return "unknown";
	}
}

function regressionCallBack(status, inData, args){
	if(status == "requesting"){
		document.getElementById("regression-confirmation-span").innerHTML = "trying to send request<br> to surrogate<br>";
	}
	if(status == "unknown"){
		document.getElementById("regression-confirmation-span").innerHTML = "unknown response from surrogate<br>";
	}
	if(status == "success"){
		document.getElementById("regression-confirmation-span").innerHTML = "request submitted succesfully<br>ticket id:" + inData;
	}
	if(status == "failed"){
		document.getElementById("regression-confirmation-span").innerHTML = "failed submitting regression request<br>";
	}
}

function backToMainClick(){
	setSectionVisible("main");
	makeBackButtonHeaderInvisible();
}

function backToMainClickResults(){
	setSectionVisible("main");
	makeBackButtonHeaderInvisible();
	document.getElementById("get-results-span").innerHTML = "";
	document.getElementById("forecasts-span").innerHTML = "";
	document.getElementById("prediction-span").innerHTML = "";
	document.getElementById("regression-confirmation-span").innerHTML = "";
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
	document.getElementById("received-items-btn").addEventListener("click", receivedItemsClick);	
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
	document.getElementById("back-to-main-btn9").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn10").addEventListener("click", backToMainClick);
	document.getElementById("newdata-head-btn").addEventListener("click", receivedItemsClick);
	loadLocations(addLocationElements);
});

