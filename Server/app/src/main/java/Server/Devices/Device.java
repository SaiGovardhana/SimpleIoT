package Server.Devices;




import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;

public class Device 
{
   
        String id;
        String name;
        WsContext ws;
        
        public ConcurrentHashMap<String,Gadget>gadgets;
        public ConcurrentHashMap<WsContext,Long>clients;


    public Device(WsContext ws)
    {   
        this.ws=ws;
        gadgets=new ConcurrentHashMap<String,Gadget>();
        clients=new ConcurrentHashMap<WsContext,Long>();
        
    }
   
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setId(String id)
    {
        this.id=id;
    }

     public String getId()
    {

        return this.id;
    }
    public WsContext getWsContext()
    {
        return ws;
    }
    synchronized public void addGadget(Gadget d)
    {
        gadgets.put(d.getName(),d);
    }
    @Override
    public boolean equals(Object obj)
    {   if(obj==null)
            return false;
        if(obj instanceof Device)
            {
                Device that=(Device)obj;
                
                if(that.id.equals(this.id))
                    return true;
                else
                    return false;
            }
        else
            return false;

    }
    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
    public JSONArray getGadgetsState()
    {
        JSONArray states=new JSONArray();
        for(String x :gadgets.keySet())
            {
                Gadget cur=gadgets.get(x);
                JSONObject obj=new JSONObject();
                obj.put("name",cur.getName());
                obj.put("state",cur.getState());
                obj.put("type",cur.getType());
                states.put(obj);
            }
        


        return states;
    }
    public void onClientClose(WsCloseContext ws)
    {   System.out.println("REMOVED A CLIENT FROM DEVICE "+id);
        clients.remove(ws);

    } 
    public void onClientConnect(WsConnectContext ws)
    {   if(!this.ws.session.isOpen())
            {   System.out.println("Client added After Closing");
                ws.closeSession();
            }
        else
            clients.put(ws,0L);
    }

    public void onClientMessage(WsMessageContext ws)
    {
        if(!this.ws.session.isOpen())
            {   System.out.println("Client Message After Closing");
                ws.closeSession();
                return;
            }
        try
        {   
            String txt=ws.message();
            JSONObject json=new JSONObject(txt);
            //Client Requests for changing state
            if(json.getString("messageType").equals("changeState"))
            {   System.out.println("Forwarded request to device");
                this.ws.send(txt);
            }
            //Client Requests current state
            if(json.getString("messageType").equals("getStates"))
            {
                JSONArray states=getGadgetsState();
                JSONObject response=new JSONObject();
                response.put("gadgets",states);
                response.put("messageType","allStates");
            }   
        }
        catch(Exception e)
        {
            System.out.println("Error while parsing client message  "+e);
            ws.closeSession();

        }
    }

   synchronized public void onDeviceStatusChange(WsMessageContext ws)
    {   String txt=ws.message();
        try
        {   System.out.println("HWEWW");
            JSONObject json=new JSONObject(txt);
            if(json.getString("messageType").equals("stateChanged"))
            {   String nameOfGadget=json.getString("name");
                String state=json.getString("state");
                gadgets.get(nameOfGadget).setState(state);
                JSONArray states=getGadgetsState();
                JSONObject response=new JSONObject();
                response.put("gadgets",states);
                response.put("messageType","allStates");
                for(WsContext cur:clients.keySet())
                try{    

                    cur.send(response.toString());

                    }
                    catch(Exception e)
                    {   
                        System.out.println("Couldn't send message to client removing client");
                        

                    }
            }

        }
        catch(Exception e)
        {
            System.out.println("Error parsing device state change ");
        }
    }

}
