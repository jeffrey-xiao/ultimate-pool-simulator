#include <stdbool.h>
#include <string.h>

const uint32_t ButtonCount = 2;
const uint32_t Buttons[ButtonCount] = { PD_2, PE_0 };

struct ButtonState
{ 
  bool state;
  bool isRising;
};

static struct InputState
{
  struct ButtonState  buttons[2];
} gameInputState;

/*
 * Initializes all the input/output from the orbit booster pack
 */
 
void GameUIInit()
{
  OrbitOledClear();
  OrbitOledClearBuffer();
  OrbitOledSetFillPattern(OrbitOledGetStdPattern(iptnSolid));
  OrbitOledSetDrawMode(modOledSet);
  
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
  for(int i = 0; i < ButtonCount; ++i )
    pinMode(Buttons[i], INPUT);
}
void uiInputTick()
{
  for(int i = 0; i < ButtonCount; ++i )
  {
    // Only look for Rising Edge Signals.
    bool previousState = gameInputState.buttons[i].state;
    gameInputState.buttons[i].state = digitalRead(Buttons[i]);
    gameInputState.buttons[i].isRising = (!previousState && gameInputState.buttons[i].state);
  }
}
/*
 * Determines whether or not a button is pressed
 */
bool checkBtn1(){
  return gameInputState.buttons[0].isRising;
}
bool checkBtn2(){
  return gameInputState.buttons[1].isRising;
}

