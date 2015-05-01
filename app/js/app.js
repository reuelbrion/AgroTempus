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

function submitDataClick(){
	setSectionVisible("submit");
}

function getDataClick(){
	setSectionVisible("get-data");
}

function forecastsClick(){
	setSectionVisible("forecasts");
}

function predictionClick(){
	setSectionVisible("prediction");
}

function regressionClick(){
	setSectionVisible("regression");
}

function backToMainClick(){
	setSectionVisible("main");
}

window.onload = function () {
    document.getElementById("submit-data-btn").addEventListener("click", submitDataClick);
	document.getElementById("get-data-btn").addEventListener("click", getDataClick);
	document.getElementById("forecasts-btn").addEventListener("click", forecastsClick);
	document.getElementById("prediction-btn").addEventListener("click", predictionClick);
	document.getElementById("regression-btn").addEventListener("click", regressionClick);
	document.getElementById("back-to-main-btn1").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn2").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn3").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn4").addEventListener("click", backToMainClick);
	document.getElementById("back-to-main-btn5").addEventListener("click", backToMainClick);
};

