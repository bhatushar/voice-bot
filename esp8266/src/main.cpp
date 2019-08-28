#include <Arduino.h>
#include <TransmissionControl.h>

void setup() {
    Serial.begin(115200);
    const char *ssid = "Zenfone M1", *password = "2030bf7cfed1";
    connect(ssid, password);
}

void loop() {
    server.handleClient();
    // TODO implement rest of the program
    clearData();
}