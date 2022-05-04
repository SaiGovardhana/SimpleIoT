#include"ArduinoJson.h"
#include <ESP8266WiFi.h>
#include <ArduinoWebsockets.h>
using namespace websockets; 
String link="ws://192.168.216.11:4292/Register";
class Device
{
    public:
    String id;
    String name;
    StaticJsonDocument<1200> json;
    
    Device(String id,String name)
    {   
        
            this->id=id;
            this->name=name;

    }
    void addDevice(String name,String type,int pin,String state)
    {   StaticJsonDocument<512> curJson;
        pinMode(pin,OUTPUT);
        if(state=="ON")
          digitalWrite(pin,HIGH);
         else
          digitalWrite(pin,LOW);
        JsonObject obj=curJson.to<JsonObject>();
        obj["name"]=name;
        obj["type"]=type;
        obj["pin"]=pin;
        obj["state"]=state;
        json.add(curJson);
        curJson.clear();
        curJson.garbageCollect();
    }
    String getRegisterJSON()
    {   StaticJsonDocument<1024> curJson;
        JsonObject obj=curJson.to<JsonObject>();
        obj["id"]=id;
        obj["name"]=name;
        obj["messageType"]="REGISTER";
        obj["gadgets"]=json.as<JsonArray>();
        String result=curJson.as<String>();
        
        curJson.clear();
        return result;
    }
    String stateChange(String newState)
    {   StaticJsonDocument<512>newJson;
        deserializeJson(newJson,newState);
        
        
        if(newJson.isNull())
            return "";
       
        
      
        if(newJson.containsKey("name")==1)
            {     
                

                for(int i=0;i<json.size();i++)
                {   
                    if(json[i]["name"]==newJson["name"])
                    {   
                        json[i]["state"]=newJson["state"];
                        { Serial.println("Device State Changed");
                          newJson["messageType"]="stateChanged";
                          if(json[i]["type"]=="DIG")
                          { int pin=json[i]["pin"];
                            String state=json[i]["state"];
                            Serial.println("LIGHT State Changed");
                                    if(state=="ON")
                                      digitalWrite(pin,HIGH);
                                     else
                                       digitalWrite(pin,LOW);
                              
                          }
                          return newJson.as<String>();  
                        } 
                    }

                }
            }
            

        return "";
    }

};


WebsocketsClient clients;
Device d("2","Bed Room");
long startTime=millis();

void onMessageCall(WebsocketsMessage msg)
{ 
    String message=msg.data();
    String result=d.stateChange(message);
    if(result!="")
      clients.send(result);
     else
      Serial.println("Error");
    
}
void onEventsCallback(WebsocketsEvent event, String data) {
    if(event == WebsocketsEvent::ConnectionOpened) {
        Serial.println("Connnection Opened");
        clients.send(d.getRegisterJSON());
    } else if(event == WebsocketsEvent::ConnectionClosed) {
        Serial.println("Connnection Closed");
    } else if(event == WebsocketsEvent::GotPing) {
        Serial.println("Got a Ping!");
    } else if(event == WebsocketsEvent::GotPong) {
        Serial.println("Got a Pong!");
    }
}
void setup()
{ 
  d.addDevice("BLUE_LED","DIG",4,"ON");
  d.addDevice("BUZZER","DIG",5,"OFF");
  Serial.begin(115200);
  Serial.println();

  WiFi.begin("abcd", "12345678");

  Serial.print("Connecting");
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }
  Serial.println();

  Serial.print("Connected, IP address: ");
  Serial.println(WiFi.localIP());


  clients.onMessage(onMessageCall);
  clients.onEvent(onEventsCallback);
  clients.connect(link);
}

void loop() {
if(WiFi.status() == WL_CONNECTED){
  if(clients.available())
  {
  if((millis()-startTime)>=2000)
  {
    clients.send("{\"messageType\":\"Ping\"}");
    startTime=millis();
  }
  clients.poll();
  }
  else
  {   Serial.println("HERE");
      clients.connect(link);
  }
 
delay(1000);
}}
