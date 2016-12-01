#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

String inputString = ""; // a string to hold incoming data
boolean stringComplete = false; // whether the string is complete
/*
 * The following are variables used to store the game state
 */
char CURRENT_PLAYER = '0';
bool BALL_IN_HAND = false;
bool CALL_POCKET=false;
int GAME_WINNER =0;

void SerialReaderInit() {
  // initialize serial:
  Serial.begin(9600);
  // reserve 300 bytes for the inputString:
  inputString.reserve(300);
}

/*
 * Function is called when a pocket is properly called and changes the game state.
 */
void calledPocket(){ 
  CALL_POCKET=false;
}
/*
 * Function is called after a ball is placed due to a scratch and changes the game state.
 */
void setScratch() {
  BALL_IN_HAND = false;
}
/*
 * Function returns the current state of the game
 */
int getPlayer() {
  if (GAME_WINNER)
    return 2+GAME_WINNER;
  if (CURRENT_PLAYER == '0')
    return 1;
  if (CURRENT_PLAYER == '1')
    return 2;
}
/*
 * Compares String with const char *b (returns true or false)
 */
bool stringCmp(String a, const char *b) {
  for (int x = 0; x < strlen(b); x++) {
    if (a[x] != b[x])
      return false;
  }
  return true;
}
/*
 * Returns whether or not to prompt user to call a pocket
 */
bool pocket(){
  return CALL_POCKET;
}
/*
 * Returns whether or not to prompt user to place a ball
 */
bool isScratch() {
  return BALL_IN_HAND;
}
/*
 * Resets game to initial conditions
 */
void gameReset(){
  GAME_WINNER= 0;
  CURRENT_PLAYER= '0';
  BALL_IN_HAND = false;
  CALL_POCKET=false;
}
/*
 * The following waits and interprets Serial input from GUI.
 */
void SerialReaderTick() {
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
      if (stringCmp(tokenString, "CALL_POCKET")){
        CALL_POCKET=true;
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
  hardware serial RX.
  Adds chars to a string until it finds '\n'
*/
void serialEvent() {
  while (Serial.available() && !stringComplete) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    inputString += inChar;
    if (inChar == '\n') {
      stringComplete = true;
    }
  }
}
