
const options = {
    animateHistoryBrowsing: true
   };
//@ts-ignore
let sup=new Swup(options);
let myHost=location.host;
let devicesWebSocket:WebSocket|null=null;
let prevDevices:{[key:string]:string}={}

function parseDevices(message:MessageEvent)
{
    let curDevices:{[key:string]:string}=JSON.parse(message.data);
    let root=document.querySelector("#swup");
    for(let x in prevDevices)
    {
        if(x in curDevices)
            continue;
        let element=document.querySelector(`#D${x}`);
        element.remove();


    }
    for(let x in curDevices)
    {
        if(x in prevDevices)
            continue;
        let htmlString=` <div id="D${x}" class="device">
        <span>ID:${x}</span>
        <span> Name:${curDevices[x]}</span>
         <a class="link">GO</a>
     </div>`
     root.innerHTML+=htmlString;
        
        


    }

}
function init()
{
    if(document.querySelector("#swup").classList.contains('devices'))
    {   prevDevices={};
        devicesWebSocket=new WebSocket(`ws:/${myHost}/Devices`);
        devicesWebSocket.onmessage=parseDevices;
        console.log("Entering Devices Page");
    }
    else
    {   console.log("Enter Home Page")
        if(devicesWebSocket!=null)
            {
                if(devicesWebSocket.readyState==WebSocket.OPEN)
                    devicesWebSocket.close();
            }
    }

}
function destroy()
{
    if(document.querySelector("#swup").classList.contains('devices'))
    { 
        console.log("Leaving Device Page");
        if(devicesWebSocket!=null)
        {
            if(devicesWebSocket.readyState==WebSocket.OPEN)
                devicesWebSocket.close();
        }
    }
    else
    {   console.log("Leaving Home Page");

    }

}

sup.on('contentReplaced',init);
sup.on('willReplaceContent',destroy);