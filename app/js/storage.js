"use strict";

var db;
var request = indexedDB.open("storage", 1);
request.onerror = function (event) {
	alert("Can't open indexedDB! \n" + event);
};
request.onsuccess = function (event) {
	db = event.target.result;
};

/*request.onupgradeneeded = function (event) {
    alert("Running onUpgradeNeeded");
    db = event.target.result;

    if (!db.objectStoreNames.contains("weather data")) {
        alert("Creating objectStore for memos");
        var objectStore = db.createObjectStore("weather data", {
            keyPath: "id",
            autoIncrement: true
        });
        objectStore.createIndex("title", "title", {
            unique: false
        });

        console.log("Adding sample memo");
        var sampleMemo1 = new Memo();
        sampleMemo1.title = "Welcome Memo";
        sampleMemo1.content = "This is a note taking app. Use the plus sign in the topleft corner of the main screen to add a new memo. Click a memo to edit it. All your changes are automatically saved.";

        objectStore.add(sampleMemo1);
    }
};*/
