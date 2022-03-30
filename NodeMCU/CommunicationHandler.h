#include <ESP8266WiFi.h>
#include <ArduinoWebsockets.h>
using namespace websockets;
class CommunicationHandler{
public:
WebsocketsClient client;
string ssid;
string pass;
string ip;
Device* d;
CommunicationHandler(string ssid,string pass,string ip)
{
    this->ip=ip;
    this->pass=pass;
    this->ip=ip;
}
void onMessageCall(WebsocketsMessage msg)
{
Serial.println("Got Data:");
Serial.println(msg.data());
}
void setup()
{
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
  client.connect("ws://192.168.242.11:4292/device");
  client.onMessage(onMessageCall);
}

void loop() {
  if(client.available())
  {
  client.poll();
  }
  else
  {
      client.connect("ws://192.168.242.11:4292/device");
  }
  }

}