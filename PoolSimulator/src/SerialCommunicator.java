/**
 * Object to communicates (reads and writes data) with the Serial Port.
 */

import java.util.function.Consumer;

import jssc.*;

public class SerialCommunicator implements SerialPortEventListener {
	private SerialPort serialPort;
	private StringBuilder msg = new StringBuilder();
	private boolean receivingMessage = false;
	private Consumer<String> func;
	
	/**
	 * 
	 * @param comName name of the COM port
	 * @param consumer the consumer to invoke when information is read
	 */
	public SerialCommunicator (String comName, Consumer<String> consumer) {
		this.serialPort = new SerialPort("COM3");
		this.func = consumer;
		initializePort();
	}
	
	/**
	 * Initializes the port for reading and writing.
	 */
	private void initializePort () {
		try {
			serialPort.openPort();
			serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
			serialPort.addEventListener(this, SerialPort.MASK_RXCHAR);
		} catch (SerialPortException ex) {
			System.err.println("There is an error with opening port " + serialPort.getPortName() + ": " + ex);
		}
	}
	
	/**
	 * 
	 * @param s string to print
	 */
	public void print (String s) {
		try {
			serialPort.writeString(s);
		} catch (SerialPortException ex) {
			System.err.println("There is an error with writing to port " + serialPort.getPortName() + ": " + ex);
		}
	}
	
	/**
	 * Appends a new line to s.
	 * @param s string to print
	 */
	public void println (String s) {
		try {
			serialPort.writeString(s + "\n");
		} catch (SerialPortException ex) {
			System.err.println("There is an error with writing to port " + serialPort.getPortName() + ": " + ex);
		}
	}
	
	/**
	 * Triggers everytime new information is outputted to the serial port.
	 */
	@Override
	public void serialEvent (SerialPortEvent event) {
		if(event.isRXCHAR() && event.getEventValue() > 0) {
			try {
				char[] buffer = serialPort.readString(event.getEventValue()).toCharArray();
				for (char c : buffer) {
					if (c == '>') {
						receivingMessage = true;
						msg.setLength(0);
					} else if (receivingMessage) {
						if (c == '\n') {
							receivingMessage = false;
						//	System.out.println(msg.toString());
							func.accept(msg.toString());
						} else {
							msg.append((char)c);
						}
					}
				}
			}
			catch (SerialPortException ex) {
				System.err.println("Error in receiving string from COM-port: " + ex);
			}
		}
	}
}