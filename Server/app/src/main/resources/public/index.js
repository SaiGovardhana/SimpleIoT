"use strict";
var options = {
    animateHistoryBrowsing: true,
    cache: false
};
//@ts-ignore
var sup = new Swup(options);
var myHost = location.host;
var devicesWebSocket = null;
var currentDeviceWebSocket=null;
var prevDevices = {};
let deviceInterval=null;
function onGadgetChanged(curObject)
{
    console.log("VALUED CHANGED");
    if(curObject.dataset.type=='DIG')
    {
        if(curObject.checked==true)
        {   console.log("Cur State "+curObject.checked)
            curObject.dataset.state="ON";
        }
        else
         {   console.log("Cur State "+curObject.checked)
            curObject.dataset.state="OFF";
        }
    }
    if(curObject.dataset.type=='LCD')
    {
        curObject.dataset.state=curObject.value;
    }
    let json={"messageType":"changeState",state:curObject.dataset.state,"name":curObject.dataset.name,"type":curObject.dataset.type};
    console.log(json);
    if(currentDeviceWebSocket!=null)
        currentDeviceWebSocket.send(JSON.stringify(json));
}
function parseDevices(message) {
    console.log("HERE");
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
        var htmlString = " <div id=\"D" + x + "\" class=\"device\">\n        <span>ID:" + x + "</span>\n        <span> Name:" + curDevices[x] + "</span>\n         <a href='/Device/"+x+"' class=\"link\">GO</a>\n     </div>";
        root.innerHTML += htmlString;
    }
    prevDevices=curDevices;
}

function updateState(newState)
{   let oldStates={}
    let devices=document.querySelectorAll('input');
    for(let i=0;i<devices.length;i++)
    {   
        let name=devices[i].dataset.name;
        let state=devices[i].dataset.state;
        oldStates[name]=state;
        
    }
    console.log("HRERE",oldStates)
    for(let i=0;i<newState.length;i++)
    {   let cur=newState[i];
        let name=cur["name"];
        let type=cur["type"];
        let state=cur["state"];
        console.log("cur ",cur);
        if(state!=oldStates[name])
        {
            if(type=="DIG")
            {   
                for(let z=0;z<devices.length;z++)
                    if(devices[z].dataset.name==name)
                        {
                            devices[z].dataset.state=state;
                            if(state=="OFF")
                                devices[z].checked=false;
                            else
                                devices[z].checked=true;
                        }
            }
            if(type=="LCD")
            {
                for(let z=0;z<devices.length;z++)
                if(devices[z].dataset.name==name)
                    {
                        devices[z].dataset.state=state;
                        devices[z].value=state;
                    }
            }
        }


    }


}

function init() {
    function resetConnection()
    {
        var root = document.querySelector("#swup");
        root.innerHTML="<h1 class='MainHeader'>OOPS!<br>Connection has Been Reset :(</h1><h1 class='MainHeader'>Please Refresh The Page </h1>"
        console.log("Reset Connection");
    }
    
    if(document.querySelector("#swup").classList.contains('Page2'))
    {   let deviceName=document.querySelector("#devicename");
        console.log("Entering Page2");
        currentDeviceWebSocket = new WebSocket("ws://" + myHost + "/Device/"+deviceName.dataset.name);
        currentDeviceWebSocket.onclose=resetConnection;
        
        let deviceInterval=setInterval(()=>{
            if(currentDeviceWebSocket!=null&&currentDeviceWebSocket.readyState==1)
            currentDeviceWebSocket.send("{messageType:Ping}");
            else
                clearInterval(deviceInterval);
        
        },2000);
        
        currentDeviceWebSocket.onmessage=(msgevent)=>
        {
                let json=JSON.parse(msgevent.data);
                console.log(json)
                if(json["messageType"]=="allStates")
                {
                    updateState(json["gadgets"]);
                    console.log("RECIEVED MESSAGE FOR STATE CHANGE",json);
                }

        }

        

    }
    

    if (document.querySelector("#swup").classList.contains('Page1')) {
        
        devicesWebSocket = new WebSocket("ws://" + myHost + "/Devices");
        devicesWebSocket.onmessage = parseDevices;
        devicesWebSocket.onopen=()=>{if(devicesWebSocket.readyState==WebSocket.OPEN)devicesWebSocket.send('{"messageType":"GETDEVICES"}')};
        devicesWebSocket.onclose=resetConnection;
        deviceInterval=setInterval(()=>{if(devicesWebSocket.readyState==WebSocket.OPEN)devicesWebSocket.send('{"messageType":"GETDEVICES"}')},2000);
        console.log("Entering Devices Page");
    }
    if (document.querySelector("#swup").classList.contains('HomePage')) 
    {
        console.log("Enter Home Page");
        if (devicesWebSocket != null) {
            if (devicesWebSocket.readyState == WebSocket.OPEN)
                devicesWebSocket.close();
        }
    }
}
function destroy() {
    if(document.querySelector("#swup").classList.contains('Page2'))
    {
        if(currentDeviceWebSocket!=null)
            {   console.log("LEAVING DEVICE PAGE");
                if(currentDeviceWebSocket.readyState==WebSocket.OPEN)
                    {    currentDeviceWebSocket.onclose=undefined;
                        currentDeviceWebSocket.close();
                    }
            }

    }

    if (document.querySelector("#swup").classList.contains('Page1')) {
        console.log("Leaving Device Page");
        prevDevices={}
        if(deviceInterval!=null)
            clearInterval(deviceInterval);
        if (devicesWebSocket != null) {

            if (devicesWebSocket.readyState == WebSocket.OPEN)
                {  devicesWebSocket.onclose=undefined;
                    devicesWebSocket.close();}
        }
    }
    if (document.querySelector("#swup").classList.contains('HomePage')) 
    {
        console.log("Leaving Home Page");
    }
}
sup.on('contentReplaced', init);
sup.on('willReplaceContent', destroy);
init()
