"use strict";

var socket = navigator.mozTCPSocket.open('192.168.2.9', 2545);

var str = "Space shuttles!!!!! \n Pancake! \nEND"

uint = new Uint8Array(str.length);
for(var i=0,j=str.length;i<j;++i){
  uint[i]=str.charCodeAt(i);
}

function pushData() {
	var data = uint;
  while (data != null && socket.send(data));
}

// Each time the buffer is flushed
// we try to send data again.
socket.ondrain = pushData;

// Start sending data.
pushData();