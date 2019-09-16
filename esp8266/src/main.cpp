#include <Arduino.h>
#include <TransmissionControl.h>
#include <MotorDriver.h>

/**
 * Function decodes the request received and calls the respective function in MotorDriver library.
 * A better implementation might be possible. However, this is simple and straightforward, even if a little redundant.
 */
void decode() {
    switch (request[0]) {
        case MOVE_FORWARD_STEP:
            move(FORWARD, request[1], STEP);
            break;
        case MOVE_FORWARD_CM:
            move(FORWARD, request[1], CM);
            break;
        case MOVE_FORWARD_INCH:
            move(FORWARD, request[1], INCH);
            break;
        case MOVE_BACKWARD_STEP:
            move(BACKWARD, request[1], STEP);
            break;
        case MOVE_BACKWARD_CM:
            move(BACKWARD, request[1], CM);
            break;
        case MOVE_BACKWARD_INCH:
            move(BACKWARD, request[1], INCH);
            break;
        case TURN_LEFT_STEP:
            move(LEFT, request[1], STEP);
            break;
        case TURN_LEFT_DEG:
            move(LEFT, request[1], DEGREE);
            break;
        case TURN_LEFT_RAD:
            move(LEFT, request[1], RADIAN);
            break;
        case TURN_RIGHT_STEP:
            move(RIGHT, request[1], STEP);
            break;
        case TURN_RIGHT_DEG:
            move(RIGHT, request[1], DEGREE);
            break;
        case TURN_RIGHT_RAD:
            move(RIGHT, request[1], RADIAN);
            break;
        case SET_SPEED:
            setSpeed(request[1]);
            break;
        case STOP:
            stop();
            break;
        default:
            Serial.println("Cannot decode the request.");
    }
}

void setup() {
    Serial.begin(115200);
    // Connect to the network
    const char *ssid = "****", *password = "****";
    connect(ssid, password);
    // Set motor and encoder pins
    int motors[] = {0, 0, 0, 0},
        encoderPin = 0;
    setMotors(motors, encoderPin);
}

void loop() {
    server.handleClient();
    if (request[0]) {
        decode();
        clearData();
    }
}
