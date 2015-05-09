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
	stagingObject.time = submitForm["datetime-input"].value;
	stagingObject.source = "app";
	stageNewSubmit(stagingObject, submitCallBack);
}

function submitCallBack(status){
	if (status == "ok"){
		alert("data submitted");
	}
}

function getDataSubmitClick(){
	setSectionVisible("get-data-results");
	document.getElementById("get-results-span").innerHTML = "fetching data<br>";
	var startDate = $("#get-date-input1").val();
	var endDate = $("#get-date-input2").val();
	//pull data in dataexchange.js
	pullData(startDate, endDate, getDataCallBack);
}

function getDataCallBack(error, receivedItems, startDate, endDate){
	if(error == null){
		addGetDataElements(receivedItems, startDate, endDate);
	}
	else if(error == "wrongdate"){
		alert("start date should be before end date!");
		getDataClick();
	}
}

function addGetDataElements(receivedItems, startDate, endDate){
	var itemString = "Items received for time period " + startDate + " until " + endDate + "<br><br>";
	while(receivedItems.length > 0){
		var receivedObject = JSON.parse(receivedItems.shift());
		itemString += "Location: " + receivedObject.location + "<br>";
		itemString += "Temperature: " + receivedObject.temp + "<br>";
		itemString += "Humidity: " + receivedObject.humidity + "<br>";
		itemString += "Pressure: " + receivedObject.pressure + "<br>";
		itemString += "Wind speed: " + receivedObject.windspeed + "<br>";
		itemString += "Wind degree: " + receivedObject.winddegree + "<br>";
		itemString += "Time: " + receivedObject.time + "<br>";
		itemString += "Date: " + receivedObject.date + "<br>";
		itemString += "Source: " + receivedObject.source + "<br><br>";
		
		console.log(itemString);
	}
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

function backToMainClick(){
	setSectionVisible("main");
	makeBackButtonHeaderInvisible();
	makeStoreButtonHeaderInvisible();
}

function backToMainClickResults(){
	setSectionVisible("main");
	makeBackButtonHeaderInvisible();
	document.getElementById("get-results-span").innerHTML = "";
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
		
	document.getElementById("back-to-main-btn1").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn2").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn3").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn4").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn5").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn-results").addEventListener("click", backToMainClickResults);
	
	loadLocations(addLocationElements);
});

