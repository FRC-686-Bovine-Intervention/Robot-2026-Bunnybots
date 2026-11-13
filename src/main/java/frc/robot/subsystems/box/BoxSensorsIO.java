package frc.robot.subsystems.box;

import org.littletonrobotics.junction.AutoLog;

public interface BoxSensorsIO {
	@AutoLog
	public static class BoxSensorsIOInputs {
		boolean ejectSensor = false;
	}

	public default void updateInputs(BoxSensorsIOInputs inputs) {}
}
