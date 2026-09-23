package first.util.hardwareID.can;

public class CANivoreBus {
	private final com.ctre.phoenix6.CANBus phoenixBus;

	private CANivoreBus(String name) {
		this.phoenixBus = new com.ctre.phoenix6.CANBus(name);
	}

	public static CANivoreBus name(String name) {
		return new CANivoreBus(name);
	}
}
