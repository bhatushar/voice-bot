#include <Arduino.h>
#include <TransmissionControl.h>

void setup() {
    Serial.begin(115200);
    const char *ssid = "Zenfone M1", *password = "2030bf7cfed1";
    connect(ssid, password);
}

void loop() {
    server.handleClient();

    if (!data[0].length()) return;
    Serial.print("Code: ");
    Serial.println(data[0]);
    Serial.print("Value: ");
    Serial.println(data[1]);

    clearData();
}