package first.util.hardwareID.can;

public class CANBus {
	public final int index;
	private final com.ctre.phoenix6.CANBus phoenixBus;

	private CANBus(int index) {
		this.index = index;
		this.phoenixBus = com.ctre.phoenix6.CANBus.systemcore(index);
	}

	private CANBus(String name) {
		this.index = -1;
		this.phoenixBus = new com.ctre.phoenix6.CANBus(name);
	}

	public static CANBus systemcoreBus(int index) {
		return new CANBus(index);
	}

	public static CANBus canivore(String name) {
		return new CANBus(name);
	}

	public CANDevice id(int id) {
		return CANDevice.id(id, this);
	}

	public com.ctre.phoenix6.CANBus getPhoenix() {
		return this.phoenixBus;
	}
}
