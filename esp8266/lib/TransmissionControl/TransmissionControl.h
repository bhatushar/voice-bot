/**
 * TransmissionControl
 * This file provides different functionalities responsible for the server setup, processing of requests and
 * decoding of data on ESP8266.
 */

#ifndef BOT_TRANSMISSION_CONTROL_H
#define BOT_TRANSMISSION_CONTROL_H

#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

/**
 * This is used to handle all incoming requests form clients.
 * Uses handleClient() method to get the requests.
 */
extern ESP8266WebServer server;

/**
 * Stores the data which is received by the server. The data consists of two parameters.
 * The first index element contains the "code" which is associated with a task.
 * The second index element contains the "value" which is an optional parameter.
 * The default value is 0, i.e., no data received by server.
 */
extern int receivedData[];

/**
 * Function configures the WiFi settings and connects to the provided network.
 * It also adds a server handle before starting the server.
 *
 * @param ssid Network Name
 * @param password Network password
 */
void connect(const char*, const char*);

/**
 * Handler for server requests.
 * It is invoked every time the server receives a request.
 * It extracts the required parameters and populates receivedData with it.
 * Request is of the form http://192.168.43.88:80/?code=XXX&value=XXX
 */
void argsHandler();

/**
 * Resets the value of receivedData to 0.
 * Should be called at the end of each program cycle. This is to ensure that the same request is not
 * executed more than once.
 */
void clearData();

#endif //BOT_TRANSMISSION_CONTROL_H
