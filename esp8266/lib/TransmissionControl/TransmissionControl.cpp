#include "TransmissionControl.h"

// Defining the extern variables
ESP8266WebServer server(80); // port 80
int request[2];

/**
 * Accepts a numerical string and returns an equivalent integer.
 * If the string contains a non-numerical character or if the string numeral is greater than int range
 * the value returned is 0.
 *
 * @param s Numerical string
 * @return Integer equivalent of string
 */
int parseInt(const String& s) {
    int num = 0;
    for (char i : s) {
        if ('0' <= i && i <= '9')
            // i is a digit
            num = num * 10 + i - 48;
        else { num = 0; break; }
        if (num < 0) {
            // Integer overflow
            num = 0; break;
        }
    }
    return num;
}

/**
 * Handler for server requests.
 * It is invoked every time the server receives a request.
 * It extracts the required parameters and populates receivedData with it.
 * Request is of the form http://192.168.43.88:80/?code=XXX&value=XXX
 */
void argsHandler() {
    // Store data
    request[0] = parseInt(server.arg("code"));
    request[1] = parseInt(server.arg("value"));
    // Respond back to client
    String msg;
    if (!request[0])
        // No parameter passed.
        msg = "Connected";
    else {
        // Code passed in request
        msg = "Received: CODE";
        if (request[1])
            // Code and value passed.
            msg += ", VALUE";
    }
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
    request[0] = 0;
    request[1] = 0;
}
