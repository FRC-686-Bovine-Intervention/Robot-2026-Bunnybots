package first.util.loggerUtil.inputs;

import java.nio.ByteBuffer;

import org.wpilib.simulation.DCMotorSim;
import org.wpilib.simulation.FlywheelSim;
import org.wpilib.simulation.SingleJointedArmSim;
import org.wpilib.system.RobotController;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Temperature;
import org.wpilib.units.measure.Voltage;
import org.wpilib.util.struct.Struct;
import org.wpilib.util.struct.StructSerializable;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.hardware.TalonFXS;
import com.revrobotics.spark.SparkMax;

public class LoggedMotor implements StructSerializable {
	private double appliedVolts = 0.0;
	private double statorCurrentAmps = 0.0;
	private double supplyCurrentAmps = 0.0;
	private double torqueCurrentAmps = 0.0;
	private double deviceTempCel = 0.0;

	public LoggedMotor() {}

	private LoggedMotor(double appliedVolts, double statorCurrentAmps, double supplyCurrentAmps, double torqueCurrentAmps, double deviceTempCel) {
		this.appliedVolts = appliedVolts;
		this.statorCurrentAmps = statorCurrentAmps;
		this.supplyCurrentAmps = supplyCurrentAmps;
		this.torqueCurrentAmps = torqueCurrentAmps;
		this.deviceTempCel = deviceTempCel;
	}

	/**
	 * @return The applied voltage of the motor in {@link #edu.wpi.first.units.Units.Volts volts}
	 */
	public double getAppliedVolts() {
		return this.appliedVolts;
	}
	/**
	 * @return The stator current of the motor in {@link #edu.wpi.first.units.Units.Amps amps}
	 */
	public double getStatorCurrentAmps() {
		return this.statorCurrentAmps;
	}
	/**
	 * @return The applied voltage of the motor in {@link #edu.wpi.first.units.Units.Volts volts}
	 */
	public double getSupplyCurrentAmps() {
		return this.supplyCurrentAmps;
	}
	/**
	 * @return The applied voltage of the motor in {@link #edu.wpi.first.units.Units.Volts volts}
	 */
	public double getTorqueCurrentAmps() {
		return this.torqueCurrentAmps;
	}
	/**
	 * @return The temperature of the motor in {@link #edu.wpi.first.units.Units.Celsius celsius}
	 */
	public double getDeviceTempCel() {
		return this.deviceTempCel;
	}

	public void setAppliedVolts(double appliedVolts) {
		this.appliedVolts = appliedVolts;
	}

	public void setStatorCurrentAmps(double statorCurrentAmps) {
		this.statorCurrentAmps = statorCurrentAmps;
	}

	public void setSupplyCurrentAmps(double supplyCurrentAmps) {
		this.supplyCurrentAmps = supplyCurrentAmps;
	}

	public void setTorqueCurrentAmps(double torqueCurrentAmps) {
		this.torqueCurrentAmps = torqueCurrentAmps;
	}

	public void setDeviceTempCel(double deviceTempCel) {
		this.deviceTempCel = deviceTempCel;
	}

	public static record MotorStatusSignalCache(
		StatusSignal<Voltage> appliedVoltage,
		StatusSignal<Current> statorCurrent,
		StatusSignal<Current> supplyCurrent,
		StatusSignal<Current> torqueCurrent,
		StatusSignal<Temperature> deviceTemperature
	) {
		public static MotorStatusSignalCache from(TalonFX talonFX) {
			return new MotorStatusSignalCache(talonFX.getMotorVoltage(), talonFX.getStatorCurrent(), talonFX.getSupplyCurrent(), talonFX.getTorqueCurrent(), talonFX.getDeviceTemp());
		}
		public static MotorStatusSignalCache from(TalonFXS talonFXS) {
			return new MotorStatusSignalCache(talonFXS.getMotorVoltage(), talonFXS.getStatorCurrent(), talonFXS.getSupplyCurrent(), talonFXS.getTorqueCurrent(), talonFXS.getDeviceTemp());
		}

		public BaseStatusSignal[] getStatusSignals() {
			return new BaseStatusSignal[] {
				this.appliedVoltage(),
				this.statorCurrent(),
				this.supplyCurrent(),
				this.torqueCurrent(),
				this.deviceTemperature(),
			};
		}
	}

	public void updateFrom(MotorStatusSignalCache statusSignals) {
		this.setAppliedVolts(statusSignals.appliedVoltage().getValueAsDouble());
		this.setStatorCurrentAmps(statusSignals.statorCurrent().getValueAsDouble());
		this.setSupplyCurrentAmps(statusSignals.supplyCurrent().getValueAsDouble());
		this.setTorqueCurrentAmps(statusSignals.torqueCurrent().getValueAsDouble());
		this.setDeviceTempCel(statusSignals.deviceTemperature().getValueAsDouble());
	}

	public void updateFrom(SparkMax spark) {
		this.setAppliedVolts(spark.getAppliedOutput().get() * RobotController.getBatteryVoltage());
		this.setStatorCurrentAmps(spark.getOutputCurrent().get());
		this.setSupplyCurrentAmps(0.0);
		this.setTorqueCurrentAmps(0.0);
		this.setDeviceTempCel(spark.getMotorTemperature().get());
	}

	public void updateFrom(DCMotorSim sim) {
		this.setAppliedVolts(sim.getInputVoltage());
		this.setStatorCurrentAmps(sim.getCurrentDraw());
	}

	public void updateFrom(FlywheelSim sim) {
		this.setAppliedVolts(sim.getInputVoltage());
		this.setStatorCurrentAmps(sim.getCurrentDraw());
	}

	public void updateFrom(SingleJointedArmSim sim) {
		this.setAppliedVolts(sim.getInput(0));
		this.setStatorCurrentAmps(sim.getCurrentDraw());
	}

	public static final LoggedMotorStruct struct = new LoggedMotorStruct();
	public static class LoggedMotorStruct implements Struct<LoggedMotor> {
		@Override
		public Class<LoggedMotor> getTypeClass() {
			return LoggedMotor.class;
		}

		@Override
		public String getTypeName() {
			return "Motor";
		}

		@Override
		public int getSize() {
			return DOUBLE_SIZE * 5;
		}

		@Override
		public String getSchema() {
			return "double AppliedVolts;double StatorCurrentAmps;double SupplyCurrentAmps;double TorqueCurrentAmps;double DeviceTempCelsius";
		}

		@Override
		public LoggedMotor unpack(ByteBuffer bb) {
			var appliedVolts = bb.getDouble();
			var statorCurrentAmps = bb.getDouble();
			var supplyCurrentAmps = bb.getDouble();
			var torqueCurrentAmps = bb.getDouble();
			var deviceTempCel = bb.getDouble();
			return new LoggedMotor(appliedVolts, statorCurrentAmps, supplyCurrentAmps, torqueCurrentAmps, deviceTempCel);
		}

		@Override
		public void unpackInto(LoggedMotor out, ByteBuffer bb) {
			out.setAppliedVolts(bb.getDouble());
			out.setStatorCurrentAmps(bb.getDouble());
			out.setSupplyCurrentAmps(bb.getDouble());
			out.setTorqueCurrentAmps(bb.getDouble());
			out.setDeviceTempCel(bb.getDouble());
		}

		@Override
		public void pack(ByteBuffer bb, LoggedMotor value) {
			bb.putDouble(value.getAppliedVolts());
			bb.putDouble(value.getStatorCurrentAmps());
			bb.putDouble(value.getSupplyCurrentAmps());
			bb.putDouble(value.getTorqueCurrentAmps());
			bb.putDouble(value.getDeviceTempCel());
		}
	}
}
