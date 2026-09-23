package first.util.hardwareID.can;

import org.wpilib.hardware.bus.CANPort;

public class CANBus {
	public final CANPort port;
	private final com.ctre.phoenix6.CANBus phoenixBus;

	private CANBus(CANPort port) {
		this.port = port;
		this.phoenixBus = new com.ctre.phoenix6.CANBus(port);
	}

	public static CANBus port(CANPort port) {
		return new CANBus(port);
	}

	public CANDevice id(int id) {
		return CANDevice.id(id, this);
	}

	public com.ctre.phoenix6.CANBus getPhoenix() {
		return this.phoenixBus;
	}
}
