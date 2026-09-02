package first.util.hardwareID.can;

import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.CANdi;
import com.ctre.phoenix6.hardware.Pigeon2;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.hardware.TalonFXS;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;

public class CANDevice {
	public final int id;
	public final CANBus bus;

	private CANDevice(int id, CANBus bus) {
		this.id = id;
		this.bus = bus;
	}

	public static CANDevice id(int id, CANBus bus) {
		return new CANDevice(id, bus);
	}

	// CTRE
	// | Phoenix 6
	public TalonFX talonFX() {
		return new TalonFX(id, bus.getPhoenix());
	}
	public TalonFXS talonFXS() {
		return new TalonFXS(id, bus.getPhoenix());
	}
	public CANcoder cancoder() {
		return new CANcoder(id, bus.getPhoenix());
	}
	public Pigeon2 pigeon2() {
		return new Pigeon2(id, bus.getPhoenix());
	}
	public CANdi candi() {
		return new CANdi(id, bus.getPhoenix());
	}
	// REV
	public SparkMax sparkMax(MotorType motorType) {
		return new SparkMax(bus.index, id, motorType);
	}
}
