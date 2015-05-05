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
	stageNewSubmit();
}

function editTimeClick(){
	var button = document.getElementById("edit-time-btn");
	if (button.getAttribute("value") == "no-edit"){
		button.setAttribute("value", "edit");
		button.innerHTML = "Use current time";
		document.getElementById("time-input-form").setAttribute("class", "");
		document.getElementById("date-input-form").setAttribute("class", "");
	}	
	else{
		button.setAttribute("value", "no-edit");
		button.innerHTML = "Change time";
		document.getElementById("time-input-form").setAttribute("class", "hidden");
		document.getElementById("date-input-form").setAttribute("class", "hidden");
	}
}

function backToMainClick(){
	setSectionVisible("main");
	makeBackButtonHeaderInvisible();
	makeStoreButtonHeaderInvisible();
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
		
	document.getElementById("back-to-main-btn1").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn2").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn3").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn4").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn5").addEventListener("click", backToMainClick);
});

