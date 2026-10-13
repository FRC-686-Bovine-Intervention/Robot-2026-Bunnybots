package frc.robot.subsystems.rollers.kicker;

import frc.util.loggerUtil.inputs.LoggedMotor;
import org.littletonrobotics.junction.AutoLog;

public interface KickerIO {
	@AutoLog
	public static class KickerIOInputs {
		boolean motorConnected = false;
		LoggedMotor motor = new LoggedMotor();
	}

	public default void updateInputs(KickerIOInputs inputs) {}

	public default void setVolts(double volts) {}
}
