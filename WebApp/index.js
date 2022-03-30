"use strict";
var options = {
    animateHistoryBrowsing: true
};
//@ts-ignore
var sup = new Swup(options);
var myHost = location.host;
var devicesWebSocket = null;
var prevDevices = {};
function parseDevices(message) {
    var curDevices = JSON.parse(message.data);
    var root = document.querySelector("#swup");
    for (var x in prevDevices) {
        if (x in curDevices)
            continue;
        var element = document.querySelector("#D" + x);
        element.remove();
    }
    for (var x in curDevices) {
        if (x in prevDevices)
            continue;
        var htmlString = " <div id=\"D" + x + "\" class=\"device\">\n        <span>ID:" + x + "</span>\n        <span> Name:" + curDevices[x] + "</span>\n         <a class=\"link\">GO</a>\n     </div>";
        root.innerHTML += htmlString;
    }
}
function init() {
    if (document.querySelector("#swup").classList.contains('devices')) {
        prevDevices = {};
        devicesWebSocket = new WebSocket("ws:/" + myHost + "/Devices");
        devicesWebSocket.onmessage = parseDevices;
        console.log("Entering Devices Page");
    }
    else {
        console.log("Enter Home Page");
        if (devicesWebSocket != null) {
            if (devicesWebSocket.readyState == WebSocket.OPEN)
                devicesWebSocket.close();
        }
    }
}
function destroy() {
    if (document.querySelector("#swup").classList.contains('devices')) {
        console.log("Leaving Device Page");
        if (devicesWebSocket != null) {
            if (devicesWebSocket.readyState == WebSocket.OPEN)
                devicesWebSocket.close();
        }
    }
    else {
        console.log("Leaving Home Page");
    }
}
sup.on('contentReplaced', init);
sup.on('willReplaceContent', destroy);
