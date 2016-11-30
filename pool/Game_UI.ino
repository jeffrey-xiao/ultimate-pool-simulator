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

void GameUIInit()
{
  for(int i = 0; i < ButtonCount; ++i )
    pinMode(Buttons[i], INPUT);
}
static void uiInputTick()
{
  for(int i = 0; i < ButtonCount; ++i )
  {
    // Only look for Rising Edge Signals.
    bool previousState = gameInputState.buttons[i].state;
    gameInputState.buttons[i].state = digitalRead(Buttons[i]);
    gameInputState.buttons[i].isRising = (!previousState && gameInputState.buttons[i].state);
  }
}
bool checkBtn1(){
  return gameInputState.buttons[0].isRising;
}
bool checkBtn2(){
  return gameInputState.buttons[1].isRising;
}

