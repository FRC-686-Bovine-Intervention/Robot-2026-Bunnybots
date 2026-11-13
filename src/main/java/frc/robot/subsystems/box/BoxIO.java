package frc.robot.subsystems.box;

import frc.util.NeutralMode;
import frc.util.loggerUtil.inputs.LoggedMotor;

import java.util.Optional;

import org.littletonrobotics.junction.AutoLog;

public interface BoxIO {
	@AutoLog
	public static class BoxIOInputs {
		boolean ejectMotorConnected = false;
		boolean feedMotorConnected = false;
		LoggedMotor ejectMotor = new LoggedMotor();
		LoggedMotor feedMotor = new LoggedMotor();
	}

	public default void updateInputs(BoxIOInputs inputs) {}

	public default void setEjectVolts(double volts) {}
	public default void setFeedVolts(double volts) {}

	public default void stopEjectMotor(Optional<NeutralMode> neutralMode) {}
	public default void stopFeedMotor(Optional<NeutralMode> neutralMode) {}
}
