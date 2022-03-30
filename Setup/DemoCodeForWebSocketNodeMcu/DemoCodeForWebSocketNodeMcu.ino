#include <ESP8266WiFi.h>
#include <ArduinoWebsockets.h>
using namespace websockets;
WebsocketsClient client;


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
  client.onEvent([](WebsocketsClient& client, WebsocketsEvent event, std::string payload) {
  switch(event) {
    case WebsocketsEvent::ConnectionOpened:
      
      break;
    case WebsocketsEvent::GotPing:
      // Dispatched when a ping frame arrives
      break;
    case WebsocketsEvent::GotPong:
      // Dispatched when a pong frame arrives
      break;
    case WebsocketsEvent::ConnectionClosed:
      // Dispatched when the connection is closed (either 
      // by the user or after some error or event)
      break;
  }
});
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
