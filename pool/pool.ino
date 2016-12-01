#include <delay.h>
#include <FillPat.h>
#include <LaunchPad.h>
#include <OrbitBoosterPackDefs.h>
#include <OrbitOled.h>
#include <OrbitOledChar.h>
#include <OrbitOledGrph.h>
#include <Wire.h>
#include <string.h>
#include <stdbool.h>

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

void WireInit();
void accelInit();
void GameUIInit();
void SerialReaderInit();

void accelTick();
void posTick();
void SerialReaderTick();
void uiInputTick();

void setState();
void sendShot();
bool checkBtn1();
bool checkBtn2();

int isShooting();
bool isScratch();
void setScratch();
int getPlayer();

void setup() {
  WireInit();
  GameUIInit();
  OrbitOledInit();
  SerialReaderInit();
  OrbitOledClear();
  OrbitOledClearBuffer();
  OrbitOledSetFillPattern(OrbitOledGetStdPattern(iptnSolid));
  OrbitOledSetDrawMode(modOledSet);
  // initialize the LEDs pin as an output:

  Serial.begin(9600);
  delay(100);
  accelInit();

}
int gameState = 1;

bool RESET_CHECK = false;
bool readyToShoot = false;
bool unSet = true;


int const SCRATCH = 3;
void loop() {
  gameState=getPlayer();
  switch (gameState) {
    case 1:
      OrbitOledMoveTo(5, 10);
      OrbitOledDrawString("Player 1");
      OrbitOledUpdate();
      break;
    case 2:
      OrbitOledMoveTo(5, 10);
      OrbitOledDrawString("Player 2");
      OrbitOledUpdate();
      break;
  }
  // read the state of the pushbutton value:
  buttonState = digitalRead(buttonPin);

  // check if the pushbutton is pressed.
  // if it is, the buttonState is HIGH:

  if (RESET_CHECK) {
    if (buttonState == LOW)
      Serial.print(">RESET_GAME\n");
    delay(100);
  }
  if (buttonState == LOW) {
    // turn LED on:
    digitalWrite(Orbit_LD4, HIGH);
    RESET_CHECK = true;
    delay(2000);
  }
  else {
    // turn LED off:
    RESET_CHECK = false;
    digitalWrite(Orbit_LD4, LOW);
  }

  if (digitalRead(Orbit_SLIDE2) == LOW && !isScratch()) {     // changing angle
    digitalWrite(Orbit_LD1, HIGH);
    int potential = 0;
    potential = analogRead(POTENTIOMETER);
    
    Serial.print(">CHANGE_ANGLE ");
    Serial.print(potential);
    Serial.print("\n");
    
  } else {    // turn LED off:
    digitalWrite(Orbit_LD1, LOW);
  }

  if (digitalRead(Orbit_SLIDE2) == HIGH && digitalRead(Orbit_SLIDE1) == LOW && !isScratch()) {     // ready to shoot
    digitalWrite(Orbit_LD2, HIGH);

    if (unSet)
    {
      readyToShoot = true;
      accelTick();
      setState();
      unSet = false;
    }
  } else {    // turn LED off:
    digitalWrite(Orbit_LD2, LOW);
    readyToShoot = false;
    unSet = true;
  }
  if (readyToShoot)
  {
    accelTick();
    if (isShooting()) {
      //   Serial.println(isShooting());
      readyToShoot = false;
      sendShot();
    }
  }
  
  if(isScratch()==true)
  {
 //   Serial.println("CHECKING");
    posTick();
  }
  if (checkBtn1()) {     // ready to shoot
    Serial.println(">DROP\n");
    setScratch();
  }
  
  uiInputTick();
  
  delay(10);
  
  SerialReaderTick();

}
