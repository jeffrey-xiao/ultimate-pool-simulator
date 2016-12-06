#include <Wire.h>

/**
 * You need to use your own Instance of the TwoWire Object (instead of the standard `Wire`)
 * because they communicate over the wrong pins!
 */
static TwoWire orbitWire(0);

void WireInit()
{
  orbitWire.begin();

}

void WireWriteByte(int address, uint8_t value)
{
  orbitWire.beginTransmission(address);
  orbitWire.write(value);
  orbitWire.endTransmission();
}

void WireWriteRegister(int address, uint8_t reg, uint8_t value)
{
  orbitWire.beginTransmission(address);
  orbitWire.write(reg);
  orbitWire.write(value);
  orbitWire.endTransmission();
}

/**
 * WireRequestArray(int address, uint32_t* buffer, uint8_t amount)
 * 
 * address - I2C Address of Chip
 * buffer - Buffer to store data
 * amount - Bytes of information to store!
 */
void WireRequestArray(int address, uint32_t* buffer, uint8_t amount)
{
  orbitWire.requestFrom(address, amount);
  do 
  {
    while(!orbitWire.available());
    *(buffer++) = orbitWire.read();
  } while(--amount > 0);
}
