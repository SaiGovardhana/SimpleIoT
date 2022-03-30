const options = {
    animateHistoryBrowsing: true
   };
let sup=new Swup(options);
let myHost=location.host;
let devicesWebSocket=null;
function init()
{
    if(document.querySelector("#swup").classList.contains('devices'))
    {
        console.log("Entering Devices Page");
    }
    else
    {
        if(devicesWebSocket!=null)
            {
                if(devicesWebSocket.readyState==)
            }
    }

}