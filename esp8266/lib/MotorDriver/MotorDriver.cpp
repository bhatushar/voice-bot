#include <Arduino.h>
#include "MotorDriver.h"

/**
 * It is the ratio between the distance covered by the wheel in one state change of encoder signal (or a tick) and the
 * radius of the wheel. This value is a constant and is calculated as: 2*PI/N
 * Here, N is the the number of ticks in one complete revolution of the motor shaft.
 * For the encoder used in this project, N = 180
 */
#define DISTANCE_RATIO 0.034920

#define WHEEL_RADIUS_CM 6.7
#define WHEEL_RADIUS_IN 2.6378
// TODO find bot radius
/**
 * Calculates the length of the arc formed when angle is in degrees.
 * Formula: (2*PI*R/360)*theta
 * R, radius of bot: 15cm
 */
#define ARC_LENGTH_DEG(theta) 0.2618*theta
/**
 * Calculates the length of the arc formed when angle is in radians.
 * Formula: R*theta
 * R, radius of bot: 15cm
 */
#define ARC_LENGTH_RAD(theta) 15*theta

/**
 * The voltage which is applied to the motors to move them.
 * The range is between 0 to 255 (inclusive).
 */
int speed = 150;
// Left and right motor pins
int motorLeft[2], motorRight[2];

float distanceCovered, // Distance covered by the bot at any given time
    distancePerTick, // Distance covered by the bot in one state change of encoder output
    relativeRadius, // Radius of the wheel relative to the unit in which it is calculated
    totalDistance; // The total distance that is to be covered by the bot


/* -------------------------------
 //     Function prototypes     //
--------------------------------- */
void calcDistance();
void motorWrite(int, int);
void configure(int, float, int);


/* -------------------------------
 // Global function definitions //
--------------------------------- */
void setMotors(const int motors[4], int encoder) {
    motorLeft[0] = motors[0]; // Left, positive
    motorLeft[1] = motors[1]; // Left, negative
    motorRight[0] = motors[2]; // Right, positive
    motorRight[1] = motors[3]; // Right, negative
    // call calcDistance() every time the output of encoder pin changes
    attachInterrupt(digitalPinToInterrupt(encoder), calcDistance, CHANGE);
}

void move(int direction, float value, int unit) {
    // Configure global settings
    configure(direction, value, unit);

    // The following rotation description is in reference to the center of axis
    switch (direction) {
        case FORWARD:
            // Left motor clockwise
            motorWrite(motorLeft[0], motorLeft[1]);
            // Right motor counter-clockwise
            motorWrite(motorRight[0], motorRight[1]);
            break;
        case BACKWARD:
            // Left motor counter-clockwise
            motorWrite(motorLeft[1], motorLeft[0]);
            // Right motor clockwise
            motorWrite(motorRight[1], motorRight[0]);
            break;
        case LEFT:
            // Left motor counter-clockwise
            motorWrite(motorLeft[1], motorLeft[0]);
            // Right motor counter-clockwise
            motorWrite(motorRight[0], motorRight[1]);
            break;
        case RIGHT:
            // Left motor clockwise
            motorWrite(motorLeft[0], motorLeft[1]);
            // Right motor clockwise
            motorWrite(motorRight[1], motorRight[0]);
            break;
    }
}

void setSpeed(int volt) {
    if (volt < 0) speed = 0;
    if (volt > 255) speed = 255;
    else speed = volt;
}

void stop() {
    analogWrite(motorLeft[0], 0);
    analogWrite(motorLeft[1], 0);
    analogWrite(motorRight[0], 0);
    analogWrite(motorRight[1], 0);
}

/* -------------------------------
  // Local function definitions //
--------------------------------- */

/**
 * Writes high voltage to the first pin and low voltage to the second pin.
 * The voltage applied to the first pin is the value of speed.
 *
 * @param pos High potential terminal
 * @param neg Low potential terminal
 */
void motorWrite(int pos, int neg) {
    analogWrite(pos, speed);
    analogWrite(neg, 0);
}

/**
 * Invoked whenever there is a state change in the output signal of the encoder pin.
 * It adds the distance travelled by bot in one tick (state change) to the total distance travelled
 * by the bot.
 * It stops the bot once it has covered the desired distance.
 */
void calcDistance() {
    distanceCovered += distancePerTick;
    if (distanceCovered >= totalDistance)
        // Bot has travelled the required distance
        stop();
}

/**
 * The function is responsible for setting appropriate parameters which are used in distance
 * calculation. This function also resets the value of distanceCovered and the total distance.
 * It must be called before the execution of every new move command.
 * The parameters are the same as the ones passed to move().
 *
 * @param direction
 * @param value
 * @param unit
 */
void configure(int direction, float value, int unit) {
    // Resetting values
    distanceCovered = 0;
    totalDistance = 0;
    relativeRadius = 0;

    if (unit == STEP) {
        /*
         * One step is equal to one revolution of the wheel.
         * Therefore, the distance covered in one tick is equal to 1/N (unitless).
         */
        distancePerTick = 1.0/180.0;
        totalDistance = value;
    } else {
        if (direction == FORWARD || direction == BACKWARD) {
            // In linear motion, the magnitude of displacement is the same as the value provided
            // but the relativeRadius changes as per the unit
            totalDistance = value;
            switch (unit) {
                case CM: relativeRadius = WHEEL_RADIUS_CM; break;
                case INCH: relativeRadius = WHEEL_RADIUS_IN; break;
            }
        } else if (direction == LEFT || direction == RIGHT) {
            // In rotatory motion, the relativeRadius is the same as the wheel radius in cm
            // but the length of the arc to be covered by bot is calculated using the value of the angle (also in cm)
            relativeRadius = WHEEL_RADIUS_CM;
            switch (unit) {
                case DEGREE: totalDistance = ARC_LENGTH_DEG(value);  break;
                case RADIAN: totalDistance = ARC_LENGTH_RAD(value); break;
            }
        }
        // distancePerTick = 2*PI*R/N = Circumference of wheel/Ticks per revolution
        distancePerTick = DISTANCE_RATIO * relativeRadius;
    }
}