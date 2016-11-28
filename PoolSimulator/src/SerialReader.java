import jssc.*;

public class SerialReader implements SerialPortEventListener {
	static SerialPort serialPort;
	static StringBuilder msg = new StringBuilder();
	boolean receivingMessage = false;
	
	public SerialReader (String comName) {
		serialPort = new SerialPort("COM3");
		initializePort();
	}
	
	private void initializePort () {
		try {
			serialPort.openPort();
			serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
			serialPort.addEventListener(this, SerialPort.MASK_RXCHAR);
		} catch (SerialPortException ex) {
			System.out.println("There is an error with writing/reading to port " + serialPort.getPortName() + ": " + ex);
		}
	}
	
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
			}
		}
	}
}