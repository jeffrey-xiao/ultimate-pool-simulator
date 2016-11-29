#include <Wire.h>

void WireRequestArray(int address, uint8_t* buffer, uint8_t amount);
void WireWriteRegister(int address, uint8_t reg, uint8_t value);
void WireWriteByte(int address, uint8_t value);

static float const    SensorMaximumReading= 512.0;
static float const    SensorMaximumAccel  = 9.81 * 4.0;
static uint8_t const  SensorAccelerometer = 0x1D;
static float          avgAccel=0;

void accelInit()
{
  
  WireWriteRegister(SensorAccelerometer, 0x31, 1);
  WireWriteRegister(SensorAccelerometer, 0x2D, 1 << 3);
}
void accelTick()
{
  size_t const DataLength = 6;
  uint32_t data[DataLength] = { 0 };
  
  WireWriteByte(SensorAccelerometer, 0x32);   //measure x axis acceleration
  WireRequestArray(SensorAccelerometer, data, DataLength);
  avgAccel=0;
  for(int x=0; x<DataLength; x++){
    avgAccel+=data[x];
  }
  avgAccel/=DataLength;
  Serial.print(avgAccel);
}
