"use strict";

var db;
var request = indexedDB.open("storage", 1);
request.onerror = function (event) {
	alert("Can't open indexedDB! \n");
};
request.onsuccess = function (event) {
	db = event.target.result;
};

request.onupgradeneeded = function (event) {
    alert("Running onUpgradeNeeded");
    db = event.target.result;

    if (!db.objectStoreNames.contains("registration data")) {
        alert("Creating objectStore for registration");
        var objectStore = db.createObjectStore("locations", {
            keyPath: "name",
            autoIncrement: false
        });
        objectStore.createIndex("country", "country", {
            unique: false
        });

        alert("Adding sample surrogates");
        var sampleSurrogate1 = new Object();
        sampleSurrogate1.name = "Amsterdam";
        sampleSurrogate1.country = "NL";
        objectStore.add(sampleSurrogate1);
		var sampleSurrogate2 = new Object();
        sampleSurrogate2.name = "Breda";
        sampleSurrogate2.country = "NL";
        objectStore.add(sampleSurrogate1);
    }
};

function getLocations(){
	var transaction = db.transaction(["registration data"]);
	var objectStore = transaction.objectStore("registration data");
	var req = objectStore.get("locations");
	req.onerror = function(event) {
		alert("getLocations error");
	  //TODO: Handle errors!
	};
	
		alert("im here though");
	/*request.onsuccess = function(event) {
	      var cursorRequest = store.openCursor();

 

    cursorRequest.onerror = function(error) {

        console.log(error);

    };

 

    cursorRequest.onsuccess = function(evt) {                    

        var cursor = evt.target.result;

        if (cursor) {

            items.push(cursor.value);

            cursor.continue();

        }

    };
	};
	return */
}
