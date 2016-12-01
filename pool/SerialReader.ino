#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

String inputString = ""; // a string to hold incoming data
boolean stringComplete = false; // whether the string is complete
char CURRENT_PLAYER = '0';
bool BALL_IN_HAND = false;
int GAME_WINNER =0;
void SerialReaderInit() {
  // initialize serial:
  Serial.begin(9600);
  // reserve 200 bytes for the inputString:
  inputString.reserve(300);
}
int getPlayer() {
  if (GAME_WINNER)
    return 2+GAME_WINNER;
  if (CURRENT_PLAYER == '0')
    return 1;
  if (CURRENT_PLAYER == '1')
    return 2;
}
bool stringCmp(String a, const char *b) {
  for (int x = 0; x < strlen(b); x++) {
    if (a[x] != b[x])
      return false;
  }
  return true;
}
bool isScratch() {
  return BALL_IN_HAND;
}
void setScratch() {
  BALL_IN_HAND = false;
}
void gameReset(){
  GAME_WINNER= 0;
  CURRENT_PLAYER= '0';
  BALL_IN_HAND = false;
}
void SerialReaderTick() {
  // print the string when a newline arrives:
  if (stringComplete) {
    /*
      Serial.print(inputString);
      Serial.print("\n");*/
    if (inputString[0] == '<') {

      String tokenString = "";
      String input = "";
      int counter = 1;
      while (inputString[counter] != ' ' && inputString[counter] != '\n') {
        tokenString += inputString[counter];
        counter++;
      }
      counter++;
      while (inputString[counter - 1] != '\n' && inputString[counter] != '\n') {
        input += inputString[counter];
        counter++;
      }
      if (stringCmp(tokenString, "CURRENT_PLAYER")) {
        CURRENT_PLAYER = input[0];
        //Serial.println(CURRENT_PLAYER);
      }
      if (stringCmp(tokenString, "BALL_IN_HAND")) {
        BALL_IN_HAND = true;
        /*
          if(isScratch())
          Serial.println(">CHECK");
        */
      }
      if (stringCmp(tokenString, "WINNER")) {
        if (input[0] == '0') {
           GAME_WINNER = 1;
        }
        else {
          GAME_WINNER = 2;
        }
      }
      //  Serial.println(tokenString);
    }
    // clear the string:
    inputString = "";
    stringComplete = false;
  }
}

/*
  SerialEvent occurs whenever a new data comes in the
  hardware serial RX. This routine is run between each
  time loop() runs, so using delay inside loop can delay
  response. Multiple bytes of data may be available.
*/
void serialEvent() {
  while (Serial.available() && !stringComplete) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    inputString += inChar;
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      stringComplete = true;
    }
  }
}
