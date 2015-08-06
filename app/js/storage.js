"use strict";

var db;
var request = indexedDB.open("ATStorage", 1);

request.onerror = function (event) {
	console.error("Can't open indexedDB.", event);
	console.trace();
};

request.onsuccess = function (event) {
	db = event.target.result;
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
	};
}

function getReceivedItemsList(callback){
	//TODO check callback is a function
	var objectStore = db.transaction("computationresults", "readwrite").objectStore("computationresults");
	var oldItemsList = [];
	var newItemsList = [];
	objectStore.openCursor().onsuccess = function (event) {
		var cursor = event.target.result;
		if (cursor) {
			var newObject = new Object();			
			newObject.ticket = cursor.value.ticket;
			newObject.createtime = cursor.value.createtime;
			newObject.lastviewed = cursor.value.lastviewed;
			if(cursor.value.lastviewed == 0){
				newItemsList.push(newObject);
				cursor.value.lastviewed = new Date();
				var requestUpdate = objectStore.put(cursor.value);
				requestUpdate.onerror = function(event) {
					//TODO Do something with the error
				};
				requestUpdate.onsuccess = function(event) {
					//TODO
				};
			} else {
				oldItemsList.push(newObject);
			}
			cursor.continue();
		} else {
			callback(newItemsList.concat(oldItemsList));
		}
	};
	//TODO onerror
}

function getReceivedItem(requestedTicket, callback){
	//TODO check callback is a function
	var objectStore = db.transaction("computationresults").objectStore("computationresults");
	var request = objectStore.get(requestedTicket);
	request.onerror = function(event) {
		//TODO
	};
	request.onsuccess = function(event) {
		callback(request.result);
	};
}

function removeReceivedItem(requestedTicket, callback){
	//TODO check callback is a function
	var objectStore = db.transaction("computationresults", "readwrite").objectStore("computationresults");
	var request = objectStore.delete(requestedTicket);
	request.onerror = function(event) {
		callback("error");
	};
	request.onsuccess = function(event) {
		callback("success");
	};
}

function loadLocations(callBack){
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
	//TODO onerror
}


function storeComputationResults(computationResults, callback){
	//TODO check callback is a function, check object for correctness
	computationResults.lastviewed = 0;
	var objectStore = db.transaction("computationresults", "readwrite").objectStore("computationresults");
	var storageRequest = objectStore.add(computationResults);
	storageRequest.onsuccess = function(event) {
		callback();
	};
	storageRequest.onerror = function (event) {
		console.error("Can't save computation results.", event);
		console.trace();
		//TODO: callback with error
	};
}

