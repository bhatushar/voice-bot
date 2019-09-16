# VoiceBot

This repository contains the code for a project based on voice controlled bot. The code is split into two parts: Android application and ESP8266 sketch. The android application is reponsible for conversion of audio speech into text command and send it over to the ESP8266 module for execution. The bot, which is controlled by ESP8266 (NodeMCU), simply executes the command recieved.

## Android application workflow
The application is build for Android P (API 28) and above. It is not tested on earlier versions. 

The core task of the application is to listen to a user input, covert it to text and transfer the equivalent code to the bot. The speech-to-text conversion is handled by the `SpeechRecognizer` module. Once the text result is obtained, it is passed to a set of regex rules which helps in identifying the command type and later generates and equivalent transmission code. More details on this can be found in `com.bhatushar.voicebot.TransmissionControl`.
<br>
For actual transmission to take place, the ESP8266 has to be connected to the same network as the Android device (preferably, the mobile hotspot).

A valid command is of the form `<VERB><NOUN><MAGNUTIDE><UNIT>`. Everything except MAGNITUDE (which is numeral) is a word. The length of the command may vary but the order is preserved. The following commands can be processed by the application:

| Verb | Noun              | Unit                 |
| ---- | ----------------- | -------------------- |
| move | forward, backward | step, cm, inch       |
| turn | left, right       | step, degree, radian |
| set  | speed             |                      |
| stop |                   |                      |

*One step is equal to one complete rotation of the wheel.

## ESP8266 sketch workflow
The sketch is written in Arduino framework, and follows the project structure as proposed by [PlatformIO](https://docs.platformio.org/en/latest/).

The bot connects to the specified Wi-Fi network and listens to any requests made at the IP address defined for it. When it recieves the data, it looks up the recieved code in the data table and executes the corresponsing operation. Most of the operations are precision based, i.e., they require the bot to move a particular distance or rotate by a specified angle. For that, I'm using an encoder which keeps track of the distance covered by the bot.

## Instructions for project setup

### File: esp8266/src/main.cpp
Line 61: Set the SSID and Password for WiFi.
<br>
Line 64: Set the motor pins, first left then right.
<br>
Line 65: Set the encoder A pin.

### File: esp8266/lib/MotorDriver/MotorDriver.cpp
Line 10: Define DISTANCE_RATIO as `2*PI/N`.
<br>
Line 12 and 13: Set wheel radius.
<br>
Line 20: Set the constant value using the formula `2*PI*R/360` (R in cm).
<br>
Line 26: Set the contant value equal to R (in cm).
