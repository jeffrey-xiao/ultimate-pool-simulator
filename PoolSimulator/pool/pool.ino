#include <delay.h>
#include <FillPat.h>
#include <LaunchPad.h>
#include <OrbitBoosterPackDefs.h>
#include <OrbitOled.h>
#include <OrbitOledChar.h>
#include <OrbitOledGrph.h>
#include <Wire.h>

const int buttonPin = PUSH2;     // the number of the pushbutton pin (this is the bottom right button on the tiva and is inverted)
//PUSH1 doesn't work because of TIVA
const int ledPin =  GREEN_LED;      // the number of the LED pin
const int Orbit_LD1 = PC_6;    // Orbit LD1
const int Orbit_LD2 = PC_7;    // Orbit LD2
const int Orbit_LD3 = PD_6;    // Orbit LD3
const int Orbit_LD4 = PB_5;    // Orbit LD4

const int Orbit_SLIDE1 = PA_7;    // Orbit Slide Switch 1
const int Orbit_SLIDE2 = PA_6;    // Orbit Slide Switch 2

const int Orbit_BTN1 = PD_2;    // Orbit Button 1
const int Orbit_BTN2 = PE_0;    // Orbit Button 2

const uint32_t POTENTIOMETER = PE_3;  //potentiometer

// variables will change:
int buttonState = 0;         // variable for reading the pushbutton status

void setup() {
  // initialize the LEDs pin as an output:
  pinMode(RED_LED, OUTPUT);      
  pinMode(BLUE_LED, OUTPUT);      
  pinMode(GREEN_LED, OUTPUT);      

  // init Orbit LEDs
  pinMode(Orbit_LD1, OUTPUT);      
  pinMode(Orbit_LD2, OUTPUT);      
  pinMode(Orbit_LD3, OUTPUT);      
  pinMode(Orbit_LD4, OUTPUT);      
  
  // initialize the pushbutton pins as an input:
  pinMode(PUSH1, INPUT_PULLUP);     
  pinMode(PUSH2, INPUT_PULLUP);     
  
  pinMode(Orbit_BTN1, INPUT_PULLUP);     
  pinMode(Orbit_BTN2, INPUT_PULLUP);     
  pinMode(Orbit_SLIDE1, INPUT_PULLDOWN);   
  pinMode(Orbit_SLIDE2, INPUT_PULLDOWN);     
  Serial.begin(9600);
}
 int RESET_CHECK=0;
void loop(){
  // read the state of the pushbutton value:
  buttonState = digitalRead(buttonPin);

  // check if the pushbutton is pressed.
  // if it is, the buttonState is HIGH:
 
  bool PRINT_CHECK=false;
  if (buttonState == LOW) {     
    // turn LED on:    
    digitalWrite(Orbit_LD4, HIGH);  
    RESET_CHECK++;
    delay(300);
    Serial.print(">RESET_GAME_CHECK");
    PRINT_CHECK=true;
  } 
  else {
    // turn LED off:
    digitalWrite(Orbit_LD4, LOW); 
  }
  if(PRINT_CHECK){
    Serial.print(>);
    PRINT_CHECK=false;
  }
    if (digitalRead(Orbit_SLIDE1) == HIGH) {     // turn LED on:    
        digitalWrite(Orbit_LD1, HIGH);  
        int potential=0;
        potential = analogRead(POTENTIOMETER);
        Serial.print(">");
        Serial.println(potential);
    } else {    // turn LED off:
      digitalWrite(Orbit_LD1, LOW); 
    }
    if (digitalRead(Orbit_SLIDE2) == HIGH) {     // turn LED on:    
      digitalWrite(Orbit_LD2, HIGH);  
    } else {    // turn LED off:
      digitalWrite(Orbit_LD2, LOW); 
    }
    
    delay(10);
    
}
