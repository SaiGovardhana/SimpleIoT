package Server.Devices;

import java.util.Collection;

import java.util.concurrent.ConcurrentHashMap;


import org.json.JSONArray;
import org.json.JSONObject;

import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;



public class CommunicationManager 
{   
    ConcurrentHashMap<String,Device>deviceMap;
    ConcurrentHashMap<WsContext,String>deviceContextMap;
    ConcurrentHashMap<WsContext,Long>broadcastClients;
    public CommunicationManager()
    {  
        deviceMap=new ConcurrentHashMap<String,Device>();
        deviceContextMap=new ConcurrentHashMap<WsContext,String>();
        broadcastClients=new ConcurrentHashMap<WsContext,Long>();
        
    }
    synchronized public Device getDevice(String id)
    {   
        return deviceMap.get(id);
        
    }
   
   synchronized public void addDevice(Device cur)
    {
        if(deviceMap.containsKey(cur.getId()))
        {
            System.out.println("Device Already Exists Cant Add Device");
            
            try{
                cur.getWsContext().closeSession();
                deviceMap.get(cur.getId()).getWsContext().closeSession();
            }
            catch(Exception e)
            {
                System.out.println("Error In deleting previous");
            }
        
        }
        else
        {
            deviceMap.put(cur.getId(),cur);
            deviceContextMap.put(cur.getWsContext(), cur.getId());
            System.out.println("Succesfully added a Device with ID : "+cur.getId());
        }
        System.out.println("DEVICES LEFT "+deviceMap.size()+"   "+deviceContextMap.size());

    }
     synchronized void removeDevice(WsContext ws)
    {
            if(deviceContextMap.containsKey(ws))
            {  
                
                removeDevice(deviceContextMap.get(ws));
            }
            else
                System.out.println("Couldn't find corresponding key to Context");

    }
   synchronized  public void removeDevice(String id)
    {
        if(deviceMap.containsKey(id))
        {       System.out.println("Removing A Device with id "+id);
                Device cur=deviceMap.get(id);
                for(WsContext client:cur.clients.keySet())
                {
                    try
                    {
                            client.closeSession();
                    }
                    catch(Exception e)
                    {
                        System.out.println("Couldn't remove client "+e);
                    }
                }
                WsContext ws=cur.getWsContext();
                deviceContextMap.remove(ws);
                deviceMap.remove(id);
        }
        System.out.println("DEVICES LEFT "+deviceMap.size()+"   "+deviceContextMap.size());
    }



    public void onConnect(WsConnectContext ws)
    {
        System.out.println("A Device Connected");
    }
    public void onMessage(WsMessageContext ws)
    {       try{
            String message=ws.message();
            System.out.println("DEVICE/Message: "+message);
            JSONObject json=new JSONObject(message);
            if(!json.has("messageType"))
                ws.closeSession();
            else
                {  if(json.getString("messageType").equals("Ping"))
                    {

                        System.out.println("PING RECIEVED FROM DEVICE");
                    } 
                    if(json.getString("messageType").equals("REGISTER"))
                        {
                            String id=json.getString("id");
                            String name=json.getString("name");
                            JSONArray arr=new JSONArray(json.getJSONArray("gadgets"));
                            Device d=new Device(ws);
                            d.setId(id);
                            d.setName(name);
                            int totalGadgets=arr.length();
                            for (int i=0;i<totalGadgets;i++)
                            {   JSONObject curGadget= arr.getJSONObject(i);
                                String gadgetName=curGadget.getString("name");
                                String gadgetType=curGadget.getString("type");
                                String gadgetState=curGadget.getString("state");
                                Gadget cur=new Gadget(gadgetState,gadgetType,gadgetName);
                                System.out.println("Cur Gadget Name: "+gadgetName);
                                System.out.println("Cur Gadget Type: "+gadgetType);
                                System.out.println("Cur Gadget state: "+gadgetState);
                                d.addGadget(cur);
                            }
                            addDevice(d);    
                        }
                    if(json.getString("messageType").equals("stateChanged"))
                    {
                        Device cur=deviceMap.get(deviceContextMap.get(ws));
                        if(cur!=null)
                            cur.onDeviceStatusChange(ws);
                        else
                            System.out.println("Couldn't find Any Device for stateChange");
                    }
                    if(json.getString("messageType").equals("Ping"))
                        System.out.println("Ping recieved From Device ");
                 
                
                }

            }
            catch(Exception e)
            {
                System.out.println("Error While Parsing message for device "+e);
            }
    } 
    public void onError(WsErrorContext ws)
    {
        System.out.println("An Error For A  Device");
    }
    public void onClose(WsCloseContext ws)
    {
        System.out.println("Closing A Device");
        if(deviceContextMap.containsKey(ws))
            removeDevice(ws);
        else
            System.out.println("Removing a Devive with no ID");
    }

     public void addClientToBroadCast(WsContext ws)
    {           
                broadcastClients.put(ws,0L);
                System.out.println("New Client addToBroadcast");

    }

    public JSONObject getJSONDevices()
    {   JSONObject json=new JSONObject();
        Collection<Device>set=deviceMap.values();
        for(Device x:set)
          json.put(x.getId(), x.getName());
           
        
        return json;

    }

    
    public void onClientMessage(WsMessageContext ws)
    {   
        
        try{
            System.out.println("Message Recieved From Client HERE1");
            JSONObject json=new JSONObject(ws.message());
        
            String messageType=json.getString("messageType");
            if(messageType.equals("GETDEVICES"))
                ws.send(getJSONDevices().toString());
            if(messageType.equals("changeState"))
            {    System.out.println("Message Recieved From Client HERE1");
                Device d=deviceMap.get(ws.attribute("id").toString());
                if(d==null)
                {
                    System.out.println("Error While stateChange No Device Found");
                    ws.closeSession();
                }
                else
                {
                    d.onClientMessage(ws);
                }
                
            }

            }
            catch(Exception e)
            {

                System.out.println("Error While Parsing Client Message "+e);
            }   

    }
    public void onClientError(WsErrorContext ws)
    {
        broadcastClients.remove(ws);
    }

    public void onClientClose(WsCloseContext ws)
    {   System.out.println("Removing Client from list");
        
        broadcastClients.remove(ws);
    }

    public void onClientCloseDevice(WsCloseContext ws)
    {   try{
        Device d=deviceMap.get(ws.attribute("id").toString());
        if(d==null)
        {
            System.out.println("Client Not Linked To Device");
            
        }
        else
        {
            d.onClientClose(ws);
        }
        
    }

    
    catch(Exception e)
    {

        System.out.println("Error While Parsing Client Message "+e);
    }  
    }
    

    

}
