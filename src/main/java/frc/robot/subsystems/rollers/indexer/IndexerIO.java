package frc.robot.subsystems.rollers.indexer;

import frc.util.loggerUtil.inputs.LoggedMotor;
import org.littletonrobotics.junction.AutoLog;

public interface IndexerIO {
	@AutoLog
	public static class IndexerIOInputs {
		boolean leftMotorConnected = false;
		LoggedMotor leftMotor = new LoggedMotor();
		boolean rightMotorConnected = false;
		LoggedMotor rightMotor = new LoggedMotor();
	}

	public default void updateInputs(IndexerIOInputs inputs) {}

	public default void setVolts(double volts) {}
}
