#include <stdio.h>
#include <stdlib.h>
#include <string.h>

String inputString = ""; // a string to hold incoming data
boolean stringComplete = false; // whether the string is complete

void SerialReaderInit() {
  // initialize serial:
  Serial.begin(9600);
  // reserve 200 bytes for the inputString:
  inputString.reserve(200);
}

void SerialReaderTick() {
  // print the string when a newline arrives:
  if (stringComplete) {
    Serial.println(inputString);
    if(inputString[0]=='<'){
      
      String tokenString = "";
      String input = "";
      int counter=1;
      while(inputString[counter]!=' ' && inputString[counter]!='\n'){
        
        tokenString+=inputString[counter];
        counter++;
       
      }
      counter++;
      while(inputString[counter]!='\n' && inputString[counter-1]!='\n'){
        input+=inputString[counter];
        counter++;
      }
      Serial.println(tokenString);
      Serial.println(input);
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
  while (Serial.available()) {
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
