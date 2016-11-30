#include <Wire.h>

void WireRequestArray(int address, uint8_t* buffer, uint8_t amount);
void WireWriteRegister(int address, uint8_t reg, uint8_t value);
void WireWriteByte(int address, uint8_t value);

static float const    SensorMaximumReading= 512.0;
static float const    SensorMaximumAccel  = 9.81 * 4.0;
static uint8_t const  SensorAccelerometer = 0x1D;
static uint32_t const shotThreshold      = 50;
static  int const     shotSensitivity    = 15;
static float const    shotConstant        = 10;
static float const    posThreshold        = 40;
int total=0;
int n=0;
int SCALE=1000;
int avg=0;
float x, y, z;
float setY, setZ;
void accelInit()
{
  WireWriteRegister(SensorAccelerometer, 0x31, 1);
  WireWriteRegister(SensorAccelerometer, 0x2D, 1 << 3);
}
float shotTot=0;
int shotCounter=0;
int shotDist=0;
int isShooting()
{
   int temp=x-avg;
   if(abs(y-setY)<shotThreshold && abs(z-setZ)<shotThreshold){
      if(temp>=shotSensitivity){
        /*
        Serial.print(temp);
        Serial.print(" ");
        Serial.println(shotSensitivity);
        */
        
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
    delay(50);
   }
   /*
   Serial.print(shotDist);
   Serial.print(" ");
   Serial.println(shotTot/shotCounter/shotConstant);
   */
   if(shotDist>=75)
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
void setState(){
    setY=y;
    setZ=z;
}

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
  
  WireWriteByte(SensorAccelerometer, 0x34);
  WireRequestArray(SensorAccelerometer, data, DataLength);
  //checks 3d orientation of 
  uint16_t xi = (data[1] << 8) | data[0];
  uint16_t yi = (data[3] << 8) | data[2];
  uint16_t zi = (data[5] << 8) | data[4];
  x = *(int16_t*)(&xi) ;
  y = *(int16_t*)(&yi);
  z = *(int16_t*)(&zi);
  Serial.print(x);
  Serial.print(" ");
  Serial.print(y);
  Serial.print(" ");
  Serial.print(z);
  Serial.println(" ");
}

void posTick(){
  size_t const DataLength = 6;
  uint32_t data[DataLength] = { 0 };
  
  WireWriteByte(SensorAccelerometer, 0x32);
  WireRequestArray(SensorAccelerometer, data, DataLength);

  uint16_t xi = (data[1] << 8) | data[0];
  uint16_t yi = (data[3] << 8) | data[2];
  uint16_t zi = (data[5] << 8) | data[4];

  float x1 = *(int16_t*)(&xi);
  float y1 = *(int16_t*)(&yi);
  float z1 = *(int16_t*)(&zi);
  Serial.print(">SCRATCH ");
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
  /*
  Serial.print(x1);
  Serial.print(" ");
  Serial.print(y1);
  Serial.print(" ");
  Serial.print(z1);
  Serial.println(" ");
  */
}

