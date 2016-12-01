#include <Wire.h>

void WireRequestArray(int address, uint8_t* buffer, uint8_t amount);
void WireWriteRegister(int address, uint8_t reg, uint8_t value);
void WireWriteByte(int address, uint8_t value);

/*
 * The following are constants used to determine the sensitivity of the accelerometer. 
 */
static uint8_t const  ShotAccelerometer = 0x1D;
static uint8_t const  posAccelerometer = 0x1D;
static uint32_t const shotThreshold      = 50;
static  int const     shotSensitivity    = 12;
static float const    shotConstant        = 10;
static float const    posThreshold        = 30;
static int            distThreshold       = 50;
int SCALE=10;

/*
 * The following are used as the "shooting conditions".
 * Variables act as a reference point for shooting.
 * Determines speed of the shot
 */
int total=0;
int n=0;
int avg=0;
float x, y, z;
float setY, setZ;
float shotTot=0;
int shotCounter=0;
int shotDist=0;

/*
 * Initializes reading from accelerometer
 */
 
void accelInit()
{
  WireWriteRegister(ShotAccelerometer, 0x31, 1);
  WireWriteRegister(ShotAccelerometer, 0x2D, 1 << 3);
  WireWriteRegister(posAccelerometer, 0x31, 1);
  WireWriteRegister(posAccelerometer, 0x2D, 1 << 3);
}

/*
 * If shot conditions pass threshold then it sends shot properties to GUI
 */
 
int isShooting()
{
   int temp=x-avg;
   if(abs(y-setY)<shotThreshold && abs(z-setZ)<shotThreshold){
      if(temp>=shotSensitivity){

        shotCounter++;
        shotTot+=temp;
        shotDist+=temp*0.1;
      }
      else{
        shotCounter=0;
        shotTot=0;
        shotDist=0;
      }
   }
   else{
    delay(30);
   }

   if(shotDist>=distThreshold)
    return 1;
   return 0;
}
void sendShot(){
  Serial.print(">SHOT ");
  Serial.print(shotTot/shotCounter/shotConstant);
  Serial.print ("\n");
  shotCounter=0; 
  shotTot=0;
  n=0;
  total=0;
  avg=0;
}

/*
 * "Sets" the 2d table when the left switch is flipped
 */
 
void setState(){
    setY=y;
    setZ=z;
}

/*
 * Tracks acceleration of Tiva in order to determine shot
 */
 
void accelTick(){
  if(abs(y-setY)<shotThreshold && abs(z-setZ)<shotThreshold){
  total+=x;
  n++;
  avg=total/n;
  }
  if(total>10000000){
    n=0;
    total=0;
  }
  size_t const DataLength = 6;
  uint32_t data[DataLength] = { 0 };
  
  WireWriteByte(ShotAccelerometer, 0x34);
  WireRequestArray(ShotAccelerometer, data, DataLength);
  //checks 3d orientation of 
  uint16_t xi = (data[1] << 8) | data[0];
  uint16_t yi = (data[3] << 8) | data[2];
  uint16_t zi = (data[5] << 8) | data[4];
  x = *(int16_t*)(&xi) ;
  y = *(int16_t*)(&yi);
  z = *(int16_t*)(&zi);
}

/*
 * Tracks orientation of Tiva for ball placement during a scratch
 */
 
void posTick(){
  size_t const DataLength = 6;
  uint32_t data[DataLength] = { 0 };
  
  WireWriteByte(posAccelerometer, 0x32);
  WireRequestArray(posAccelerometer, data, DataLength);

  uint16_t xi = (data[1] << 8) | data[0];
  uint16_t yi = (data[3] << 8) | data[2];
  uint16_t zi = (data[5] << 8) | data[4];

  float x1 = *(int16_t*)(&xi);
  float y1 = *(int16_t*)(&yi);
  float z1 = *(int16_t*)(&zi);
  Serial.print(">CHANGE_POSITION ");
  if(abs(x1)>posThreshold){
    Serial.print((-1)*x1/SCALE);
    Serial.print(" ");
  }
  else{
    Serial.print("0 ");
  }
  if(abs(y1)>posThreshold){
    Serial.print(y1/SCALE);
    Serial.println();
  }
  else{
    Serial.println("0");
  }

}

