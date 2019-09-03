/**
 * TransmissionControl
 * This file provides different functionalities responsible for the server setup, processing of requests and
 * decoding of data on ESP8266.
 */

#ifndef BOT_TRANSMISSION_CONTROL_H
#define BOT_TRANSMISSION_CONTROL_H

#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

//Macros assigned to each transmission code
#define MOVE_FORWARD_STEP           22
#define MOVE_FORWARD_CM             26
#define MOVE_FORWARD_INCH           34
#define MOVE_BACKWARD_STEP          33
#define MOVE_BACKWARD_CM            39
#define MOVE_BACKWARD_INCH          51
#define TURN_LEFT_STEP              55
#define TURN_LEFT_DEG               95
#define TURN_LEFT_RAD               115
#define TURN_RIGHT_STEP             77
#define TURN_RIGHT_DEG              133
#define TURN_RIGHT_RAD              161
#define SET_SPEED                   29
#define STOP                        31

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
extern int request[];

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
