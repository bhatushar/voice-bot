/**
 * TransmissionControl
 * This file provides different functionalities responsible for the server setup, processing of requests and
 * decoding of data on ESP8266.
 */

#ifndef BOT_TRANSMISSION_CONTROL_H
#define BOT_TRANSMISSION_CONTROL_H

#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>


/**
 * Macros assigned to each transmission code
 */
#define MOVE_FORWARD_STEPS 174
#define MOVE_BACKWARD_STEPS 290
#define TURN_LEFT_STEPS 2233
#define TURN_RIGHT_STEPS 2639
#define SET_SPEED 323
#define STOP 23

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
 * Resets the value of receivedData to 0.
 * Should be called at the end of each program cycle. This is to ensure that the same request is not
 * executed more than once.
 */
void clearData();

#endif //BOT_TRANSMISSION_CONTROL_H
