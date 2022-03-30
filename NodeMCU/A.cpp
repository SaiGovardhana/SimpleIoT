#include"ArduinoJson.h"
#include<iostream>
using namespace std;


class Device
{
    public:
    string id;
    string name;
    StaticJsonDocument<1200> json;
    
    Device(string id,string name)
    {   
        
            this->id=id;
            this->name=name;

    }
    void addDevice(string name,string type,int pin,string state)
    {   StaticJsonDocument<512> curJson;
        JsonObject obj=curJson.to<JsonObject>();
        obj["name"]=name;
        obj["type"]=type;
        obj["pin"]=pin;
        obj["state"]=state;
        json.add(curJson);
        curJson.clear();
        curJson.garbageCollect();
    }
    string getRegisterJSON()
    {   StaticJsonDocument<512> curJson;
        JsonObject obj=curJson.to<JsonObject>();
        obj["id"]=id;
        obj["name"]=name;
        obj["messageType"]="REGISTER";
        obj["gadgets"]=json.as<string>();
        string result=curJson.as<string>();
        
        curJson.clear();
        return result;
    }
    bool stateChange(string newState)
    {   StaticJsonDocument<512>newJson;
        deserializeJson(newJson,newState);
        
        
        if(newJson.isNull())
            return false;
       
        
      
        if(newJson.containsKey("name")==1)
            {     
                

                for(int i=0;i<json.size();i++)
                {   
                    if(json[i]["name"]==newJson["name"])
                    {   
                        json[i]["state"]=newJson["state"];
                        return true;  
                    }

                }
            }
            

        return false;
    }

};

/*
int main()
{
    Device d("102","Living Room");
    d.addDevice("LED_IMP_HALL","DIG",14,"O");
    cout<<"size  "<<d.json.memoryUsage()<<endl;
    d.addDevice("BLUE_LED","DIG",14,"O");

    d.addDevice("RED_LED","DIG",14,"O");
 
    string x;
    cin>>x;
    d.stateChange(x);
    cout<<d.getRegisterJSON()<<endl;
    cout<<"size  "<<d.json.memoryUsage()<<endl;
    
    return 0;
}*/