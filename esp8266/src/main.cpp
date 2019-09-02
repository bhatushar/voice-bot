#include <Arduino.h>
#include <TransmissionControl.h>
#include <MotorDriver.h>

void decode();

void setup() {
    Serial.begin(115200);
    const char *ssid = "Zenfone M1", *password = "2030bf7cfed1";
    connect(ssid, password);
    int motors[4];
    setMotors(motors, 0);
}

void loop() {
    server.handleClient();
    decode();
    clearData();
}

void decode() {
    switch (receivedData[0]) {
        case MOVE_FORWARD_STEPS:
            move(FORWARD, receivedData[1], STEP);
            break;
        case MOVE_BACKWARD_STEPS:
            move(BACKWARD, receivedData[1], STEP);
            break;
        case TURN_LEFT_STEPS:
            move(LEFT, receivedData[1], STEP);
            break;
        case TURN_RIGHT_STEPS:
            move(RIGHT, receivedData[1], STEP);
            break;
        case SET_SPEED:
            setSpeed(receivedData[1]);
            break;
        case STOP:
            stop();
            break;
        default:
            Serial.println("Cannot decode the request.");
    }
}