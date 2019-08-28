#include "TransmissionControl.h"

// Defining the extern variables
ESP8266WebServer server(80); // port 80
int receivedData[2];

/**
 * Accepts a numerical string and returns an equivalent integer.
 * If the string contains a non-numerical character or if the string numeral is greater than int range
 * the value returned is 0.
 *
 * @param s Numerical string
 * @return Integer equivalent of string
 */
int parseInt(String s) {
    int num = 0;
    for (unsigned int i = 0; i < s.length(); i++) {
        if ('0' <= s[i] && s[i] <= '9')
            // s[i] is a digit
            num = num * 10 + s[i] - 48;
        else { num = 0; break; }
        if (num < 0) {
            // Integer overflow
            num = 0; break;
        }
    }
    return num;
}

void argsHandler() {
    // Store data
    receivedData[0] = parseInt(server.arg("code"));
    receivedData[1] = parseInt(server.arg("value"));
    // Respond back to client
    String msg = "Connected";
    server.send(200, "text/plain", msg);
}

void connect(const char *ssid, const char *password) {
    // Configuration settings
    IPAddress localIP(192, 168, 43, 88), 
            gateway(192, 168, 43, 1),
            subnet(255, 255, 255, 0);
    WiFi.config(localIP, gateway, subnet);
    // Start connection
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print('.');
    }
    Serial.print("\nConnected to: ");
    Serial.println(WiFi.localIP()); // used by client to send requests
    
    // Every request ending in / (eg. http://192.168.43.88:80/) is handled by argsHandler function
    server.on("/", argsHandler);
    // Start server
    server.begin();
}

void clearData() {
    receivedData[0] = 0;
    receivedData[1] = 0;
}
