import java.io.*;
import java.util.*;
import jssc.*;

public class SerialReader {
	static SerialPort serialPort;
	public static void main (String[] args) {
		serialPort = new SerialPort("COM3");
		try {
			serialPort.openPort();

			serialPort.setParams(SerialPort.BAUDRATE_9600,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | 
					SerialPort.FLOWCONTROL_RTSCTS_OUT);

			serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);

		}
		catch (SerialPortException ex) {
			System.out.println("There are an error on writing string to port 3: " + ex);
		}
	}

	private static class PortReader implements SerialPortEventListener {
		StringBuilder msg = new StringBuilder();
		boolean receivingMessage = false;
		@Override
		public void serialEvent(SerialPortEvent event) {
			if(event.isRXCHAR() && event.getEventValue() > 0) {
				try {
					char[] buffer = serialPort.readString(event.getEventValue()).toCharArray();
					for (char c : buffer) {
						if (c == '>') {
							receivingMessage = true;
							msg.setLength(0);
						} else if (receivingMessage) {
							if (c == '\r') {
								receivingMessage = false;
								System.out.println("RECEIVED MESSAGE " + msg.toString());
							} else {
								msg.append((char)c);
							}
						}
					}
				}
				catch (SerialPortException ex) {
					System.out.println("Error in receiving string from COM-port: " + ex);
					System.out.println("hello i have a small penis");
				}
			}
		}

	}
}