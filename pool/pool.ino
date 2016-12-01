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
const int GAME_SHOOT=1;
const int SCRATCH=2;
const int WINNER=3;
const int cPOCKET=4;
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
bool pocket();
void setScratch();
int getPlayer();
void gameReset();
void calledPocket();
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
int printState = 1;
int gameState =1;
bool RESET_CHECK = false;
bool readyToShoot = false;
bool unSet = true;

int prev=1;

void loop() {
  printState=getPlayer();
  if(printState!=prev){
    prev=printState;
    OrbitOledClear();
  }
  if(gameState==cPOCKET){
    digitalWrite(Orbit_LD1, HIGH);
    int potential = 0;
    potential = analogRead(POTENTIOMETER); 
    OrbitOledMoveTo(5, 10);
    OrbitOledDrawString("Call a pocket!");
    OrbitOledUpdate();
    Serial.print(">CHANGE_POCKET ");
    Serial.print(potential/685);
    Serial.print("\n");
    if(checkBtn2()){     // ready to shoot
      Serial.print(">SET_POCKET\n");
      calledPocket();
    } 
  }else{
    switch (printState) {
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
      case 3:
        OrbitOledMoveTo(5, 10);
        OrbitOledDrawString("Player 1 Wins!");
        OrbitOledUpdate();
        break;
      case 4:
        OrbitOledMoveTo(5, 10);
        OrbitOledDrawString("Player 2 Wins!");
        OrbitOledUpdate();
        break;    
    }
  }
  // read the state of the pushbutton value:
  buttonState = digitalRead(buttonPin);

  /*
   * Holding down the bottom right corner button resets the game
   */
  if (RESET_CHECK) {
    if (buttonState == LOW){
      Serial.print(">RESET_GAME\n");
      gameReset();
    }
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
  /*
   * Changes the angle of the Shot
   */

  if (digitalRead(Orbit_SLIDE2) == LOW && gameState==GAME_SHOOT) {    
    digitalWrite(Orbit_LD1, HIGH);
    int potential = 0;
    potential = analogRead(POTENTIOMETER);
    
    Serial.print(">CHANGE_ANGLE ");
    Serial.print(potential);
    Serial.print("\n");
    
  } else {    // turn LED off:
    digitalWrite(Orbit_LD1, LOW);
  }
  /*
   * Flipping the left switch "sets" the 2d-table and prompts user for a shot
   */
  if (digitalRead(Orbit_SLIDE2) == HIGH && digitalRead(Orbit_SLIDE1) == LOW && gameState==GAME_SHOOT) {     // ready to shoot
    digitalWrite(Orbit_LD2, HIGH);

    if (unSet)
    {
      readyToShoot = true;
      accelTick();
      setState();
      unSet = false;
    }
  } else if(gameState==GAME_SHOOT){    //:
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
  
  if(gameState==SCRATCH)
  {
    posTick();
  }
  if (checkBtn1() && gameState==SCRATCH){     // ready to shoot
    Serial.println(">DROP\n");
    setScratch();
  }
  
  if(printState>2)
     gameState=WINNER;
  else if(isScratch())
     gameState=SCRATCH;
  else if(pocket())
     gameState=cPOCKET;
  else
     gameState=GAME_SHOOT;
  uiInputTick();
  
  delay(10);
  
  SerialReaderTick();
  
}
