"use strict";

var db;
var request = indexedDB.open("ATStorage", 1);

request.onerror = function (event) {
	console.error("Can't open indexedDB.", event);
	console.trace();
};

request.onsuccess = function (event) {
	db = event.target.result;	
	//in discovery.js
	updateSurrogateList();
	console.info("database opened successfully");
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
		objectStore.createIndex("lat", "lat", { unique: false });
		objectStore.createIndex("long", "long", { unique: false });
		objectStore.createIndex("IP", "IP", { unique: false });
		objectStore.createIndex("storageserverport", "storageserverport", { unique: false });
		objectStore.createIndex("requestserverport", "requestserverport", { unique: false });
		objectStore.createIndex("offloadserverport", "offloadserverport", { unique: false });
		objectStore.createIndex("weight", "weight", { unique: false });
		
		//SURROGATE CONNECTION DATA		
        console.info("Adding sample surrogates");
        //edit this for other computers
        var sampleSurrogate1 = new Object();
        sampleSurrogate1.location = "ReuelHome";
        sampleSurrogate1.country = "NL";
		sampleSurrogate1.lat = "52.379";
		sampleSurrogate1.long = "4.899";
		sampleSurrogate1.IP = "195.240.53.133";
		sampleSurrogate1.storageserverport = 11112;
		sampleSurrogate1.requestserverport = 11113;
		sampleSurrogate1.offloadserverport = 11114;
		sampleSurrogate1.weight = 2;
        objectStore.add(sampleSurrogate1);
        /*
        //for testing on the firefoxos emulator
		var sampleSurrogate2 = new Object();
        sampleSurrogate2.location = "FirefoxOs";
        sampleSurrogate2.country = "FF";
		sampleSurrogate2.lat = "33.379";
		sampleSurrogate2.long = "1.899";
		sampleSurrogate2.IP = "localhost";
		sampleSurrogate2.storageserverport = 11112;
		sampleSurrogate2.requestserverport = 11113;
		sampleSurrogate2.offloadserverport = 11114;
		sampleSurrogate2.weight = 1;
        objectStore.add(sampleSurrogate2);*/
        //the raspberry pi surrogate
		var sampleSurrogate3 = new Object();
        sampleSurrogate3.location = "Raspberry";
        sampleSurrogate3.country = "PI";
		sampleSurrogate3.lat = "33.379";
		sampleSurrogate3.long = "1.899";
		sampleSurrogate3.IP = "192.168.42.254";
		sampleSurrogate3.storageserverport = 11112;
		sampleSurrogate3.requestserverport = 11113;
		sampleSurrogate3.offloadserverport = 11114;
		sampleSurrogate3.weight = 3;
        objectStore.add(sampleSurrogate3);
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
	
	request = indexedDB.open("ATStorage", 1);
}

function getReceivedItemsList(callback){
	//TODO check callback is a function
	var objectStore = db.transaction("computationresults", "readwrite").objectStore("computationresults");
	var oldItemsList = [];
	var newItemsList = [];
	objectStore.openCursor().onsuccess = function (event) {
		var cursor = event.target.result;
		if (cursor) {
			if(cursor.value.lastviewed == 0){
				newItemsList.push(cursor.value);
				cursor.value.lastviewed = new Date();
				var requestUpdate = objectStore.put(cursor.value);
				requestUpdate.onerror = function(event) {
					//TODO Do something with the error
				};
				requestUpdate.onsuccess = function(event) {
					//TODO
				};
			} else {
				oldItemsList.push(cursor.value);
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

function getSurrogateList(callback){
	//TODO check callback is a function
	var objectStore = db.transaction("surrogates").objectStore("surrogates");
	var surrogateList = [];
	objectStore.openCursor().onsuccess = function (event) {
		var cursor = event.target.result;
		if (cursor) {
			surrogateList.push(cursor.value);
			cursor.continue();
		} else {
			callback(surrogateList);
		};
	//TODO onerror
	}
}

function storeComputationResults(computationResults, callback){
	//TODO check callback is a function, check object for correctness
	computationResults.lastviewed = 0;
	var objectStore = db.transaction("computationresults", "readwrite").objectStore("computationresults");
	var storageRequest = objectStore.add(computationResults);
	//console.log(JSON.stringify(computationResults));
	storageRequest.onsuccess = function(event) {
		callback();
	};
	storageRequest.onerror = function (event) {
		console.error("Can't save computation results.", event);
		console.trace();
		//TODO: callback with error
	};
}

function storeTicket(ticket, callback){
	//TODO
}