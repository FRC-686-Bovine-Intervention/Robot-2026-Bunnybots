package first.util.hardwareID.systemcorePorts;

import org.wpilib.hardware.led.AddressableLED;

public class PWMPort {
	public final int port;

	public PWMPort(int port) {
		this.port = port;
	}

	public static PWMPort of(int port) {
		return new PWMPort(port);
	}

	public AddressableLED addressableLED() {
		return new AddressableLED(port);
	}
}
