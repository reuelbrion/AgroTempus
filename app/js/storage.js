"use strict";

var db;
var request = indexedDB.open("ATStorage", 1);
var storageReady = false;

request.onerror = function (event) {
	console.error("Can't open indexedDB.", event);
	console.trace();
};

request.onsuccess = function (event) {
	db = event.target.result;
	storageReady = true;
	console.info("database opened successfully");
	//load locations in app.js
};

request.onupgradeneeded = function (event) {
    console.info("Running onUpgradeNeeded");
	request.onerror = function (event) {
		console.error("Error running onupgradeneeded @ indexedDB / storage.js", event);
		console.trace();
	};
    db = event.target.result;

    if (!db.objectStoreNames.contains("surrogates")) {
        console.info("Creating objectStore for application");
        var objectStore = db.createObjectStore("surrogates", {
            keyPath: "id",
            autoIncrement: true
        });
		objectStore.createIndex("location", "location", { unique: false });
        objectStore.createIndex("country", "country", { unique: false });

		//TODO: handle errors
        console.info("Adding sample surrogates");
        var sampleSurrogate1 = new Object();
        sampleSurrogate1.location = "Amsterdam";
        sampleSurrogate1.country = "NL";
        objectStore.add(sampleSurrogate1);
		var sampleSurrogate2 = new Object();
        sampleSurrogate2.location = "Breda";
        sampleSurrogate2.country = "NL";
        objectStore.add(sampleSurrogate2);
    }
	if (!db.objectStoreNames.contains("computationresults")) {
        console.info("Creating objectStore for computation results");
        var objectStore = db.createObjectStore("computationresults", {
            keyPath: "ticket",
            autoIncrement: false
        });
        objectStore.createIndex("graphimage", "graphimage", { unique: false });
		objectStore.createIndex("createtime", "createtime", { unique: false });
		objectStore.createIndex("lastviewed", "lastviewed", { unique: false });
    }
};

function loadLocations(callBack){
	if(storageReady){
		//TODO check callback is a function
		var objectStore = db.transaction("surrogates").objectStore("surrogates");
		objectStore.openCursor().onsuccess = function (event) {
			var cursor = event.target.result;
			var locations = [];
			if (cursor) {
				//console.log(cursor.value.location + " - " + cursor.value.country);
				locations.push(cursor.value.location + " - " + cursor.value.country);
				cursor.continue();
			}
			callBack(locations);
		};
	}
}

function storeComputationResults(computationResults, callback){
	if(storageReady){
		//TODO check callback is a function
		var objectStore = db.transaction("computationresults", "readwrite").objectStore("computationresults");
		var storageRequest = objectStore.add(computationResults);
		storageRequest.onsuccess = function(event) {
			callback();
		};
	}
}