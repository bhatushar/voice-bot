//
// Created by tbhat on 27-08-2019.
//

#include "TransmissionControl.h"

ESP8266WebServer server(80);
String data[2];

void argsHandler() {
    data[0] = server.arg(0);
    data[1] = server.arg(1);
    String msg = "Connected";
    server.send(200, "text/plain", msg);
}

void connect(const char *ssid, const char *password) {
    IPAddress localIP(192, 168, 43, 88),
            gateway(192, 168, 43, 1),
            subnet(255, 255, 255, 0);
    WiFi.config(localIP, gateway, subnet);
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print('.');
    }
    Serial.print("\nConnected to: ");
    Serial.println(WiFi.localIP());
    server.on("/", argsHandler);
    server.begin();
}

inline void clearData() {
    data[0] = "";
    data[1] = "";
}
