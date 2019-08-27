#ifndef BOT_TRANSMISSION_CONTROL_H
#define BOT_TRANSMISSION_CONTROL_H

#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

extern ESP8266WebServer server;
extern String data[];

void connect(const char*, const char*);
void argsHandler();
inline void clearData();

#endif //BOT_TRANSMISSION_CONTROL_H
