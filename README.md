# VoiceBot

## Instructions

### File: esp8266/src/main.cpp
Line 61: Set the SSID and Password for WiFi.
Line 64: Set the motor pins, first left then right.
Line 65: Set the encoder A pin for any one motor.

### File: esp8266/lib/MotorDriver/MotorDriver.cpp
Line 20: Set the constant value using the formula `2*PI*R/360` (R in cm).
Line 26: Set the contant value equal to R (in cm).
