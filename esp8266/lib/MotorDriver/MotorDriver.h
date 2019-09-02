#ifndef BOT_MOTOR_DRIVER_H
#define BOT_MOTOR_DRIVER_H

// Macros to be used as parameters for move() function
#define FORWARD     0
#define RIGHT       1
#define BACKWARD    2
#define LEFT        3
#define STEP        0
#define CM          1
#define INCH        2
#define DEGREE      1
#define RADIAN      2

/**
 * The function stores the pins of the motors connected to the bot. There are two types of pins - the motor terminals
 * and the encoder output. There are two motors onboard, which constitutes to 4 terminal pins in total. However, I'm
 * only using single encoder pin instead of the traditional two-output setup because I only need to calculate distance
 * in one direction.
 *
 * The function also attaches an interrupt to the encoder pin for recording the distance.
 *
 * @param motors Terminal pins for left and right motors respectively
 * @param encoder Pin connected to the encoder output
 */
void setMotors(const int[], int);

/**
 * Moves the bot in the desired manner. The bot is capable of two types of motions - linear and rotatory.
 * In linear motion, the bot can move either forward or backward by a specified distance. The unit of the distance
 * can be CM, INCH or STEP (a step is equal to one revolution of the wheel).
 * In rotatory motion, the bot can be rotated either left or right around it center of axis by a specified angle. The
 * unit of the angle can be DEGREE, RADIAN or STEP.
 *
 * @param direction FORWARD, BACKWARD, LEFT or RIGHT
 * @param value Linear or angular distance
 * @param unit The unit in which the distance is specified
 */
void move(int, float, int);

/**
 * The function sets the default voltage which is applied to the motors. The range of the value is 0 to 255.
 * 0 is same as 0 volts, while 255 is equivalent to 12V.
 *
 * @param volt The voltage to be applied
 */
void setSpeed(int);

/**
 * Stops both motors. Simple as that.
 */
void stop();

#endif //BOT_MOTOR_DRIVER_H
