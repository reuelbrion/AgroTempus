"use strict";

var db;
var request = indexedDB.open("surrogates", 1);

request.onerror = function (event) {
	console.error("Can't open indexedDB!!!", event);
	console.trace();
};
request.onsuccess = function (event) {
	db = event.target.result;
	console.info("database opened successfully");
	//load locations in app.js
};

request.onupgradeneeded = function (event) {
    console.info("Running onUpgradeNeeded");
    db = event.target.result;

    if (!db.objectStoreNames.contains("surrogates")) {
        console.info("Creating objectStore for surrogates");
        var objectStore = db.createObjectStore("surrogates", {
            keyPath: "name",
            autoIncrement: false
        });
        objectStore.createIndex("country", "country", {
            unique: false
        });

		//TODO: handle errors
        console.info("Adding sample surrogates");
        var sampleSurrogate1 = new Object();
        sampleSurrogate1.name = "Amsterdam";
        sampleSurrogate1.country = "NL";
        objectStore.add(sampleSurrogate1);
		var sampleSurrogate2 = new Object();
        sampleSurrogate2.name = "Breda";
        sampleSurrogate2.country = "NL";
        objectStore.add(sampleSurrogate2);
    }
};

function loadLocations(callBack){
	//TODO check callback is a function
	var objectStore = db.transaction("surrogates").objectStore("surrogates");
	objectStore.openCursor().onsuccess = function (event) {
		var cursor = event.target.result;
		var locations = [];
		if (cursor) {
			console.info("Found surrogate #" + cursor.value.name + " - " + cursor.value.country);
			locations.push(cursor.value.name + " - " + cursor.value.country);
			cursor.continue();
		}
		callBack(locations);
    };
}